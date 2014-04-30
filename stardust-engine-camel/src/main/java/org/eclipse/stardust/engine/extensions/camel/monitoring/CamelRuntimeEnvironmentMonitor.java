package org.eclipse.stardust.engine.extensions.camel.monitoring;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_TRIGGER_TYPE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_PATTERN_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRP_APPLICATION_CONTEXT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationPatterns.SENDRECEIVE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationTypes.ASYNCHRONOUS;
import static org.eclipse.stardust.engine.extensions.camel.Util.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.Util.isProducerApplication;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopAndRemoveRunningRoute;
import org.apache.camel.CamelContext;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.monitoring.IRuntimeEnvironmentMonitor;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Monitoring class used to remove all created routes from the camel context.
 * 
 * 
 */
public class CamelRuntimeEnvironmentMonitor implements IRuntimeEnvironmentMonitor
{
   private static final Logger logger = LogManager.getLogger(CamelRuntimeEnvironmentMonitor.class);

   public void partitionCreated(IAuditTrailPartition arg0)
   {}

   public void partitionDropped(IAuditTrailPartition partition)
   {
      if (logger.isDebugEnabled())
         logger.debug("Partition to be dropped" + partition.getId());
      AbstractApplicationContext applicationContext = (AbstractApplicationContext) Parameters.instance().get(
            PRP_APPLICATION_CONTEXT);
      if (applicationContext != null)
      {

         for (IModel model : ModelManagerFactory.getCurrent().getModels())
         {

            for (int pd = 0; pd < model.getProcessDefinitions().size(); pd++)
            {

               IProcessDefinition processDefinition = model.getProcessDefinitions().get(pd);

               for (int i = 0; i < processDefinition.getTriggers().size(); i++)
               {

                  ITrigger trigger = (ITrigger) processDefinition.getTriggers().get(i);

                  if (CAMEL_TRIGGER_TYPE.equals(trigger.getType().getId()))
                  {

                     String camelContextId = (String) trigger.getAttribute(CAMEL_CONTEXT_ID_ATT);

                     if (logger.isDebugEnabled())
                     {
                        logger.debug("Camel context " + camelContextId + " used.");
                     }

                     CamelContext camelContext = (CamelContext) applicationContext.getBean(camelContextId);
                     String processId = ((IProcessDefinition) trigger.getParent()).getId();
                     String modelId = trigger.getModel().getId();
                     stopAndRemoveRunningRoute(camelContext,
                           getRouteId(partition.getId(), modelId, processId, trigger.getId(), false));

                  }
               }

            }
            // remove consumer route
            if (ModelManagerFactory.getCurrent().isActive(model))
            {

               ModelElementList<IApplication> apps = model.getApplications();

               for (int ai = 0; ai < apps.size(); ai++)
               {

                  IApplication app = apps.get(ai);

                  if (app != null
                        && (app.getType().getId().equals(CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE) || app
                              .getType().getId().equals(CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE)))
                  {

                     {
                        if (logger.isDebugEnabled())
                           logger.debug("Stopping route associated to Application type " + app.getId() + " defined in "
                                 + model.getId());
                        String camelContextId = (String) app.getAttribute(CAMEL_CONTEXT_ID_ATT);
                        CamelContext camelContext = (CamelContext) applicationContext.getBean(camelContextId);
                        String processId = ((IProcessDefinition) app.getParent()).getId();
                        if (app.getAttribute(INVOCATION_PATTERN_EXT_ATT) != null
                              && app.getAttribute(INVOCATION_TYPE_EXT_ATT) != null
                              && app.getAttribute(INVOCATION_PATTERN_EXT_ATT).equals(SENDRECEIVE)
                              && app.getAttribute(INVOCATION_TYPE_EXT_ATT).equals(ASYNCHRONOUS))
                        {
                           // remove consumer/producer route for sendReceive Async
                           stopAndRemoveRunningRoute(camelContext,
                                 getRouteId(partition.getId(), app.getModel().getId(), processId, app.getId(), false));

                           stopAndRemoveRunningRoute(camelContext,
                                 getRouteId(partition.getId(), app.getModel().getId(), processId, app.getId(), true));

                        }
                        else
                        {
                           String routeId = getRouteId(partition.getId(), app.getModel().getId(), processId,
                                 app.getId(), isProducerApplication(app));
                           stopAndRemoveRunningRoute(camelContext, routeId);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
