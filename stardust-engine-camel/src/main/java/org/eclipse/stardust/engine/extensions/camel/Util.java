package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ADDITIONAL_SPRING_BEANS_DEF_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DEFAULT_CAMEL_CONTEXT_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ROUTE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRODUCER_ROUTE_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_ROUTES_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_ROUTES_HEADER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_TRIGGER_TYPE;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.createAndStartConsumerRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.createAndStartProducerRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.createSpringFileContent;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.loadBeanDefinition;

import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.converter.DataConverter;
import org.eclipse.stardust.engine.extensions.camel.trigger.CamelTriggerRoute;
import org.springframework.context.support.AbstractApplicationContext;

public class Util
{
   public static final Logger logger = LogManager.getLogger(Util.class);

   public static String getCurrentPartition(String partition)
   {
      if (!StringUtils.isEmpty(partition))
      {
         return partition;
      }
      return Parameters.instance().getString(SecurityProperties.DEFAULT_PARTITION, "default");
   }

   public static String getUserName(ITrigger trigger)
   {
      return (String) trigger.getAllAttributes().get("carnot:engine:camel::username");
   }

   public static String getPassword(ITrigger trigger)
   {
      return (String) trigger.getAllAttributes().get("carnot:engine:camel::password");
   }

   public static String getProcessId(ITrigger trigger)
   {
      return (String) ((IProcessDefinition) trigger.getParent()).getId();
   }

   public static String getModelId(ITrigger trigger)
   {
      return (String) trigger.getModel().getId();
   }

   public static String getProvidedRouteConfiguration(ITrigger trigger)
   {
      return (String) (String) trigger.getAttribute(ROUTE_EXT_ATT);
   }

   public static boolean isConsumerApplication(IApplication application)
   {
      return CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE.equals(application.getType().getId())
            && ((application.getAttribute("carnot:engine:camel::applicationIntegrationOverlay") != null) && !((String) application
                  .getAttribute("carnot:engine:camel::applicationIntegrationOverlay"))
                  .equalsIgnoreCase("mailIntegrationOverlay"));
   }

   public static String getProvidedRouteConfiguration(IApplication application)
   {
      if (isConsumerApplication(application))
         return (String) application.getAttribute(CamelConstants.CONSUMER_ROUTE_ATT);
      // return (String) (String) application.getAttribute(ROUTE_EXT_ATT);
      return (String) (String) application.getAttribute(PRODUCER_ROUTE_ATT);

   }

   public static String extractBodyMainType(IData data)
   {
      return (String) data.getAttribute("carnot:engine:className");
   }

   public static String getAdditionalBeansDefinition(IApplication application)
   {
      return (String) application.getAttribute(ADDITIONAL_SPRING_BEANS_DEF_ATT);
   }

   public static String getCamelContextId(IApplication application)
   {

      return checkNotNull((String) application.getAttribute(CAMEL_CONTEXT_ID_ATT), DEFAULT_CAMEL_CONTEXT_ID);

   }

   private static String checkNotNull(String input, String defaultValue)
   {
      if (StringUtils.isEmpty(input))
      {
         input = defaultValue;
      }
      return input;
   }

   public static String getCamelContextId(Application application)
   {

      return checkNotNull((String) application.getAttribute(CamelConstants.CAMEL_CONTEXT_ID_ATT),
            DEFAULT_CAMEL_CONTEXT_ID);
   }

   public static String getInvocationPattern(IApplication application)
   {
      return (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
   }

   public static String getInvocationPattern(Application application)
   {
      return (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
   }

   public static String getInvocationType(IApplication application)
   {
      return (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);
   }

   public static Object getBodyOutAccessPoint(Application application)
   {
      return application.getAttribute(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
   }

   public static Object getBodyInAccessPoint(Application application)
   {
      return application.getAttribute(CamelConstants.CAT_BODY_IN_ACCESS_POINT);
   }

   public static Object getSupportMultipleAccessPointAttribute(Application application)
   {
      return application.getAttribute(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS);
   }

   public static ApplicationContext getActivityInstanceApplicationContext(ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext("application");
   }

   public static ApplicationContext getActivityInstanceDefaultContext(ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext("default");
   }

   public static void createTriggerRoute(String partitionId, IProcessDefinition process,
         AbstractApplicationContext springContext)
   {

      for (int i = 0; i < process.getTriggers().size(); i++)
      {

         ITrigger trigger = (ITrigger) process.getTriggers().get(i);

         if (CAMEL_TRIGGER_TYPE.equals(trigger.getType().getId()))
         {
            createTriggerlRoute(partitionId, trigger, springContext);
         }
      }
   }

   public static void createApplicationRoute(String partitionId,ModelElementList<IApplication> apps,AbstractApplicationContext springContext)
   {
      for (int ai = 0; ai < apps.size(); ai++)
      {

         IApplication app = apps.get(ai);

         if (app != null
               && app.getType() != null
               && (app.getType().getId().equals(CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE) || app.getType()
                     .getId().equals(CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE)))
         {
             createApplicationRoute(partitionId,app,springContext);
         }

      }
   }

   private static void createApplicationRoute(String partition,IApplication application,AbstractApplicationContext springContext)
   {

      try
      {
         String contextId = getCamelContextId(application);

         String springBeans = getAdditionalBeansDefinition(application);

         String invocationPattern = getInvocationPattern(application);

         CamelContext camelContext = (CamelContext) springContext.getBean(contextId);

         if (!StringUtils.isEmpty(springBeans))
         {
            loadBeanDefinition(createSpringFileContent(springBeans, false, null),
                  (AbstractApplicationContext) springContext);
         }

         if (isConsumerApplication(application))
         {

            if (StringUtils.isNotEmpty(invocationPattern)
                  && CamelConstants.InvocationPatterns.SENDRECEIVE.equals(invocationPattern))
            {
               createAndStartProducerRoute(application, camelContext, partition);
            }

            createAndStartConsumerRoute(application, camelContext, partition);

         }
         else if (CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE.equals(application.getType().getId()))
         {

            createAndStartProducerRoute(application, camelContext, partition);

         }
         else
         {

            // old behaviour
            createAndStartProducerRoute(application, camelContext, partition);

         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Exception creating route for application " + application.getId(), e);
      }
   }

   private static void createTriggerlRoute(String partitionId, ITrigger trigger,
         AbstractApplicationContext springContext)
   {
      try
      {

         String contextId = (String) trigger.getAttribute(CAMEL_CONTEXT_ID_ATT);

         if (StringUtils.isEmpty(contextId))
         {
            contextId = DEFAULT_CAMEL_CONTEXT_ID;
            logger.warn("No context provided - the default context is used.");
         }
         Map<String,DataConverter> converters= springContext.getBeansOfType(DataConverter.class);
         
         CamelContext camelContext = (CamelContext) springContext.getBean(contextId);

         String additionalBeanDefinition = (String) trigger.getAttribute(ADDITIONAL_SPRING_BEANS_DEF_ATT);

         if (!StringUtils.isEmpty(additionalBeanDefinition))
         {
            loadBeanDefinition(createSpringFileContent(additionalBeanDefinition, false, null), springContext);
         }

         CamelTriggerRoute route = new CamelTriggerRoute(camelContext, trigger,new ArrayList<DataConverter>(converters.values()), SecurityProperties.getPartition()
               .getId());

         if (route.getRouteDefinition() != null && route.getRouteDefinition().length() > 0)
         {

            StringBuilder generatedXml = new StringBuilder(SPRING_XML_ROUTES_HEADER + route.getRouteDefinition()
                  + SPRING_XML_ROUTES_FOOTER);

            logger.info("Route for trigger " + trigger.getName() + " to be added to context " + contextId
                  + " for partition " + partitionId + ".");

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
