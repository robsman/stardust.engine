package org.eclipse.stardust.engine.extensions.camel.util;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ADDITIONAL_SPRING_BEANS_DEF_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DEFAULT_CAMEL_CONTEXT_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_ROUTES_HEADER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_ROUTES_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.Util.createSpringFileContent;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.loadBeanDefinition;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.restartCamelContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.commons.io.IOUtils;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.converter.DataConverter;
import org.eclipse.stardust.engine.extensions.camel.trigger.CamelTriggerRoute;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class CreateTriggerRouteAction implements Action<Object>
{

   public static final Logger logger = LogManager.getLogger(CreateTriggerRouteAction.class);

   private static final String CAMEL_TRIGGER_TYPE = "camel";

   private String partition;

   private BpmRuntimeEnvironment bpmRt;

   private ApplicationContext springContext;

   private List<DataConverter> dataConverters;

   private ITrigger trigger;

   public CreateTriggerRouteAction(String partition, ApplicationContext springContext,
         List<DataConverter> dataConverters)
   {

      this.partition = partition;
      this.springContext = springContext;
      this.dataConverters = dataConverters;
   }

   public CreateTriggerRouteAction(BpmRuntimeEnvironment bpmRt, String partition, ApplicationContext springContext,
         List<DataConverter> dataConverters)
   {

      this.bpmRt = bpmRt;
      this.partition = partition;
      this.springContext = springContext;
      this.dataConverters = dataConverters;
   }

   public CreateTriggerRouteAction(BpmRuntimeEnvironment bpmRt, String partition, ApplicationContext springContext,
         List<DataConverter> dataConverters, ITrigger trigger)
   {

      this(bpmRt, partition, springContext, dataConverters);
      this.trigger = trigger;
   }

   public Object execute()
   {

      if (this.bpmRt == null)
      {
         this.bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      }

      Map<String, String> properties = new HashMap<String, String>();
      properties.put(SecurityProperties.PARTITION, partition);
      LoginUtils.mergeDefaultCredentials(Parameters.instance(), properties);
      AbstractLoginInterceptor.setCurrentPartitionAndDomain(Parameters.instance(), bpmRt, properties);

      if (this.trigger == null)
      {
         createTriggerRoute();
      }
      else
      {
         createTriggerlRoute(this.trigger);
      }

      return null;
   }

   private void createTriggerRoute()
   {

      List<IModel> models = ModelManagerFactory.getCurrent().getModels();

      for (int i = 0; i < models.size(); i++)
      {

         IModel model = models.get(i);

         if (ModelManagerFactory.getCurrent().isActive(model))
         {

            ModelElementList<IProcessDefinition> processes = model.getProcessDefinitions();
            for (int pd = 0; pd < processes.size(); pd++)
            {

               IProcessDefinition process = model.getProcessDefinitions().get(pd);

               createTriggerRoute(process);
            }
         }
      }
   }

   private void createTriggerRoute(IProcessDefinition process)
   {

      for (int i = 0; i < process.getTriggers().size(); i++)
      {

         ITrigger trigger = (ITrigger) process.getTriggers().get(i);

         if (CAMEL_TRIGGER_TYPE.equals(trigger.getType().getId()))
         {

            createTriggerlRoute(trigger);
         }
      }
   }

   private void createTriggerlRoute(ITrigger trigger)
   {
      try
      {

         String contextId = (String) trigger.getAttribute(CAMEL_CONTEXT_ID_ATT);

         if (StringUtils.isEmpty(contextId))
         {
            contextId = DEFAULT_CAMEL_CONTEXT_ID;
            logger.warn("No context provided - the default context is used.");
         }

         CamelContext camelContext = (CamelContext) springContext.getBean(contextId);

         String additionalBeanDefinition = (String) trigger.getAttribute(ADDITIONAL_SPRING_BEANS_DEF_ATT);

         if (!StringUtils.isEmpty(additionalBeanDefinition))
         {
            loadBeanDefinition(createSpringFileContent(additionalBeanDefinition, false, null),
                  (AbstractApplicationContext) springContext);
            restartCamelContext(camelContext, (AbstractApplicationContext) springContext);
         }

         CamelTriggerRoute route = new CamelTriggerRoute(camelContext, trigger, dataConverters, SecurityProperties
               .getPartition().getId());

         if (route.getRouteDefinition() != null && route.getRouteDefinition().length() > 0)
         {

            StringBuilder generatedXml = new StringBuilder(SPRING_XML_ROUTES_HEADER + route.getRouteDefinition()
                  + SPRING_XML_ROUTES_FOOTER);

            logger.info("Route for trigger " + trigger.getName() + " to be added to context " + contextId
                  + " for partition " + partition + ".");

            if (logger.isDebugEnabled())
            {
               logger.debug(route.getRouteDefinition());
            }

            RoutesDefinition routes = ((ModelCamelContext) camelContext).loadRoutesDefinition(IOUtils
                  .toInputStream(generatedXml.toString()));

            ((ModelCamelContext) camelContext).addRouteDefinitions(routes.getRoutes());
         }
         else
         {
            logger.warn("No route definition found.");
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Route creation for trigger " + trigger.getName() + " failed.", e);
      }
   }
}
