package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ADDITIONAL_SPRING_BEANS_DEF_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DEFAULT_CAMEL_CONTEXT_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.GREATER_THAN_SIGN;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.IPP_DIRECT_TAG;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ROUTE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRODUCER_ROUTE_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_HEADER;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class Util
{
   public static final Logger logger = LogManager.getLogger(Util.class);

   /**
    * if partition is populated then its value is returned; otherwise lookup to
    * SecurityProperties.DEFAULT_PARTITION from the context
    *
    * @param partition
    * @return
    */
   public static String getCurrentPartition(final String partition)
   {
      if (!StringUtils.isEmpty(partition))
      {
         return partition;
      }
      return Parameters.instance().getString(SecurityProperties.DEFAULT_PARTITION, "default");
   }

   /**
    * return the value of carnot:engine:camel::username attribute defined in the trigger.
    *
    * @param trigger
    * @return
    */
   public static String getUserName(final ITrigger trigger)
   {
      return (String) trigger.getAllAttributes().get("carnot:engine:camel::username");
   }

   /**
    * return the value of carnot:engine:camel::password attribute defined in the trigger.
    *
    * @param trigger
    * @return
    */
   public static String getPassword(final ITrigger trigger)
   {
      return (String) trigger.getAllAttributes().get("carnot:engine:camel::password");
   }

   /**
    * return the current processID
    *
    * @param trigger
    * @return
    */
   public static String getProcessId(final ITrigger trigger)
   {
      return (String) ((IProcessDefinition) trigger.getParent()).getId();
   }

   /**
    * returns the current ModelID
    *
    * @param trigger
    * @return
    */
   public static String getModelId(final ITrigger trigger)
   {
      return (String) trigger.getModel().getId();
   }

   /**
    * Returns provided route configuration for the camel Trigger. it's persisted in
    * carnot:engine:camel::camelRouteExt attribute.
    *
    * @param trigger
    * @return
    */
   public static String getProvidedRouteConfiguration(final ITrigger trigger)
   {
      return (String) (String) trigger.getAttribute(ROUTE_EXT_ATT);
   }

   /**
    * Returns true if the application is a consumer Application
    *
    * @param application
    * @return
    */
   public static boolean isConsumerApplication(final IApplication application)
   {
      Boolean isConsumer = CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE.equals(application.getType().getId());
      // mail application should be set as consumerApp to be able to set the activity
      // instance in hibernated state
      if ((application.getAttribute("carnot:engine:camel::applicationIntegrationOverlay") != null)
            && ((String) application.getAttribute("carnot:engine:camel::applicationIntegrationOverlay"))
                  .equalsIgnoreCase("mailIntegrationOverlay"))
         return false;

      String invocationPattern = getInvocationPattern(application);
      String invocationType = getInvocationType(application);

      if ((StringUtils.isNotEmpty(invocationPattern) && StringUtils.isNotEmpty(invocationType))
            && (CamelConstants.InvocationPatterns.SENDRECEIVE.equals(invocationPattern) && CamelConstants.InvocationTypes.ASYNCHRONOUS
                  .equals(invocationType)))
      {
         isConsumer = true;
      }
      if (StringUtils.isNotEmpty(invocationPattern)
            && CamelConstants.InvocationPatterns.RECEIVE.equals(invocationPattern))
      {
         isConsumer = true;
      }

      return isConsumer;
   }

   /**
    * According to the application instance type; the provided route configuration will be
    * returned. if the application is a producer application then the value of
    * carnot:engine:camel::routeEntries will be returned. otherwise
    * carnot:engine:camel::routeEntries
    *
    * @param application
    * @return
    */
   public static String getConsumerRouteConfiguration(final IApplication application)
   {
      return (String) application.getAttribute(CamelConstants.CONSUMER_ROUTE_ATT);

   }

   public static String getProducerRouteConfiguration(final IApplication application)
   {
      return (String) application.getAttribute(PRODUCER_ROUTE_ATT);

   }

   /**
    * Extracts the value of carnot:engine:className attribute.
    *
    * @param data
    * @return
    */
   public static String extractBodyMainType(final IData data)
   {
      return (String) data.getAttribute("carnot:engine:className");
   }

   /**
    * Returns the provided bean definitions (attribute ID :
    * carnot:engine:camel::additionalSpringBeanDefinitions")
    *
    * @param application
    * @return
    */
   public static String getAdditionalBeansDefinition(final IApplication application)
   {
      return (String) application.getAttribute(ADDITIONAL_SPRING_BEANS_DEF_ATT);
   }

   /**
    * if the camelContextId is provided in carnot:engine:camel::camelContextId Returns the
    * name of camelContext to be used.
    *
    * @param application
    * @return
    */
   public static String getCamelContextId(final IApplication application)
   {
      return checkNotNull((String) application.getAttribute(CAMEL_CONTEXT_ID_ATT), DEFAULT_CAMEL_CONTEXT_ID);
   }

   /**
    *
    * @param input
    * @param defaultValue
    * @return
    */
   private static String checkNotNull(final String input, final String defaultValue)
   {
      if (StringUtils.isEmpty(input))
      {
         return defaultValue;
      }
      return input;
   }

   /**
    *
    * @param application
    * @return
    */
   public static String getCamelContextId(final Application application)
   {

      return checkNotNull((String) application.getAttribute(CamelConstants.CAMEL_CONTEXT_ID_ATT),
            DEFAULT_CAMEL_CONTEXT_ID);
   }

   /**
    *
    * @param application
    * @return
    */
   public static String getInvocationPattern(final IApplication application)
   {
      return (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
   }

   /**
    *
    * @param application
    * @return
    */
   public static String getInvocationPattern(final Application application)
   {
      return (String) application.getAttribute(CamelConstants.INVOCATION_PATTERN_EXT_ATT);
   }

   /**
    *
    * @param application
    * @return
    */
   public static String getInvocationType(final IApplication application)
   {
      return (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);
   }

   /**
    *
    * @param application
    * @return
    */
   public static Object getBodyOutAccessPoint(final Application application)
   {
      return application.getAttribute(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
   }

   /**
    *
    * @param application
    * @return
    */
   public static Object getBodyInAccessPoint(final Application application)
   {
      return application.getAttribute(CamelConstants.CAT_BODY_IN_ACCESS_POINT);
   }

   /**
    *
    * @param application
    * @return
    */
   public static Object getSupportMultipleAccessPointAttribute(final Application application)
   {
      return application.getAttribute(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS);
   }

   /**
    *
    * @param ai
    * @return
    */
   public static ApplicationContext getActivityInstanceApplicationContext(final ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext("application");
   }

   /**
    *
    * @param ai
    * @return
    */
   public static ApplicationContext getActivityInstanceDefaultContext(final ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext("default");
   }

   /**
    *
    * @param application
    * @return
    */
   public static boolean isProducerApplication(final IApplication application)
   {
      Boolean isProducer = application.getType().getId().equalsIgnoreCase(CAMEL_PRODUCER_APPLICATION_TYPE);

      String invocationPattern = getInvocationPattern(application);
      String invocationType = getInvocationType(application);

      if ((StringUtils.isNotEmpty(invocationPattern) && StringUtils.isNotEmpty(invocationType))
            && (CamelConstants.InvocationPatterns.SENDRECEIVE.equals(invocationPattern) && CamelConstants.InvocationTypes.SYNCHRONOUS
                  .equals(invocationPattern)))
      {
         isProducer = true;
      }else if ((StringUtils.isNotEmpty(invocationPattern) && StringUtils.isNotEmpty(invocationType))
            && (CamelConstants.InvocationPatterns.SENDRECEIVE.equals(invocationPattern) && CamelConstants.InvocationTypes.ASYNCHRONOUS
                  .equals(invocationType)))
      {
         isProducer = true;
      }
      if (StringUtils.isNotEmpty(invocationPattern) && CamelConstants.InvocationPatterns.SEND.equals(invocationPattern))
      {
         isProducer = true;
      }

      return isProducer;
   }

   /**
    *
    * @param partitionId
    * @param modelId
    * @param parentModelElementId
    * @param modelElementId
    * @param isProducer
    * @return
    */
   public static String getRouteId(final String partition, final String modelId, final String parentModelElementId,
         final String modelElementId, boolean isProducer)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Calculating RouteId for Camel Application Type <" + modelElementId
               + "> with the following parameters :");
         logger.debug("< Partition = " + partition + ", modelId = " + modelId + ", parentModelElementId = "
               + ((parentModelElementId == null) ? "" : parentModelElementId) + ", Is Producer Application = "
               + isProducer + ">");
      }
      String type = isProducer ? "Producer" : "Consumer";
      StringBuilder routeId = new StringBuilder();
      routeId.append(partition);
      routeId.append("|");
      routeId.append(modelId);
      routeId.append("|");
      if (parentModelElementId != null)
      {
         routeId.append(parentModelElementId);
         routeId.append("|");
      }
      routeId.append(modelElementId);
      return type + routeId.toString().hashCode();
   }

   /**
    * creates a standard Spring config file
    *
    * @param providedBeanConfiguration
    * @param fieldMappingProvided
    * @param mapAppenderBeanDefinition
    * @return beanDefinition the content of a spring file
    */
   public static StringBuilder createSpringFileContent(final String providedBeanConfiguration,
         final boolean fieldMappingProvided, StringBuilder mapAppenderBeanDefinition)
   {

      StringBuilder beanDefinition = new StringBuilder();
      beanDefinition.append(SPRING_XML_HEADER);
      beanDefinition.append(providedBeanConfiguration);
      if (fieldMappingProvided)
         beanDefinition.append(mapAppenderBeanDefinition);
      beanDefinition.append(SPRING_XML_FOOTER);
      return beanDefinition;
   }

   /**
    *
    * @param providedRouteDefinition
    * @param replacementUri
    * @return
    */
   public static String replaceSymbolicEndpoint(final String providedRouteDefinition, final String replacementUri)
   {
      if (!StringUtils.isEmpty(providedRouteDefinition))
      {
         if (providedRouteDefinition.contains(IPP_DIRECT_TAG))
         {
            int indexOfUri = providedRouteDefinition.indexOf(IPP_DIRECT_TAG);
            int indexOfEndStatement = providedRouteDefinition.substring(indexOfUri, providedRouteDefinition.length())
                  .indexOf("/" + GREATER_THAN_SIGN);
            String partToBeReplaced = providedRouteDefinition.substring(indexOfUri, indexOfUri + indexOfEndStatement);
            String replacedUri = providedRouteDefinition.replace(partToBeReplaced, replacementUri);
            return replacedUri;
         }

      }
      return providedRouteDefinition;
   }
}
