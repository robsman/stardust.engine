package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.removeRouteDefinitionWithoutRunningRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopAndRemoveRunningRoute;
import static org.eclipse.stardust.engine.extensions.camel.Util.buildExceptionMessage;
import static org.eclipse.stardust.engine.extensions.camel.Util.getCamelContextId;
import static org.eclipse.stardust.engine.extensions.camel.Util.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.Util.isProducerApplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Route;
import org.apache.camel.model.ModelCamelContext;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContextFactory;
import org.eclipse.stardust.engine.extensions.camel.util.CreateApplicationRouteAction;
import org.springframework.context.support.AbstractApplicationContext;

public class CamelProducerSpringBeanValidator implements ApplicationValidator, ApplicationValidatorEx
{

   private static final transient Logger logger = LogManager.getLogger(CamelProducerSpringBeanValidator.class);

   private ModelCamelContext camelContext;

   private String routeId;

   /**
    * Checks if the application has valid attributes (routes entries and camelContextId).
    * 
    * @param attributes
    *           The application context attributes.
    * @param typeAttributes
    *           The application type attributes.
    * @param accessPoints
    * @return A list with all found
    *         {@link org.eclipse.stardust.engine.api.model.Inconsistency} instances.
    */

   @SuppressWarnings("rawtypes")
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Checks if the application has valid attributes (routes entries and camelContextId).
    *
    * @param attributes
    *           The application context attributes.
    * @param typeAttributes
    *           The application type attributes.
    * @param accessPoints
    * @return A list with all found
    *         {@link org.eclipse.stardust.engine.api.model.Inconsistency} instances.
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public List validate(IApplication application)
   {

      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      AbstractApplicationContext applicationContext = (AbstractApplicationContext) Parameters.instance().get(CamelConstants.PRP_APPLICATION_CONTEXT);
      String camelContextId = getCamelContextId(application);
      camelContext = (ModelCamelContext) applicationContext.getBean(camelContextId);
      String partitionId = SecurityProperties.getPartition().getId();
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Start validation of " + application);
      }

      List inconsistencies = CollectionUtils.newList();
      ProducerRouteContext routeContext = ProducerRouteContextFactory.getContext(application,camelContext, partitionId);
      inconsistencies= routeContext.validate();

      if (inconsistencies.isEmpty())
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("No inconsistencies found for application: " + application);
         }
         try
         {
            if (applicationContext != null && bpmRt != null
                  && bpmRt.getModelManager() != null)
            {
               IModel model = (IModel) application.getModel();
               IModel activeModel = bpmRt.getModelManager().findActiveModel(model.getId());

               // only start the contained routes if this model (the one being validated)
               // is
               // intended to be active (aka it has the same model OID as the currently
               // active model with the same ID or is the first version to be deployed
               // (model
               // OID is 0))
               if (model.getModelOID() == 0
                     || model.getModelOID() == activeModel.getModelOID())
               {
                  routeId = getRouteId(partitionId, application.getModel().getId(), null,
                        application.getId(), isProducerApplication(application));
                  if (logger.isDebugEnabled())
                  {
                     logger.debug("Camel Context " + camelContextId + " used.");
                  }

                  List<Route> routesToBeStopped = new ArrayList<Route>();
                  // select routes that are running in the current partition
                  for (Route runningRoute : camelContext.getRoutes())
                  {
                     if (runningRoute.getId().equalsIgnoreCase(routeId))
                     {
                        routesToBeStopped.add(runningRoute);
                     }
                  }

                  // stop running routes to sync up with the deployed model
                  for (Route runningRoute : routesToBeStopped)
                  {
                     Map<String, org.apache.camel.Endpoint> endpoints=camelContext.getEndpointMap();
                     if(endpoints!=null && !endpoints.isEmpty()){
                        for(String uri:endpoints.keySet()){
                           if(StringUtils.isNotEmpty(uri)&& uri.startsWith("sql://"))
                              camelContext.removeEndpoints(uri);
                        }
                     }
                     stopAndRemoveRunningRoute(camelContext, runningRoute.getId());
                     if (logger.isDebugEnabled())
                     {
                        logger.debug("Route " + runningRoute.getId()
                              + " is removed from context " + camelContext + ".");
                     }
                  }

                  Action< ? > action = new CreateApplicationRouteAction(bpmRt,
                        partitionId, applicationContext, application);
                  action.execute();
               }
            }
         }
         catch (Exception e)
         {// using e.getCause() since e is RTE thrown by the Action class
            try
            {
               removeRouteDefinitionWithoutRunningRoute(camelContext, routeId);
            }
            catch (Exception e1)
            {
               // throw new RuntimeException(e);
               inconsistencies.add(new Inconsistency(buildExceptionMessage(e),
                     application, Inconsistency.ERROR));
            }

            inconsistencies.add(new Inconsistency(buildExceptionMessage(e), application,
                  Inconsistency.ERROR));
         }
      }

      return inconsistencies;
   }
}