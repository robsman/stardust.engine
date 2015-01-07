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
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_HEADERS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_MESSAGE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.APPLICATION_INTEGRATION_OVERLAY_ATT;




import java.io.IOException;
import java.util.*;

import org.apache.camel.Exchange;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.model.beans.*;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;

public class Util
{
   public static final Logger logger = LogManager.getLogger(Util.class);

   private static String extractModelIdFromFullyQualifiedName(String fullyQualifiedId){
	   if(StringUtils.isNotEmpty(fullyQualifiedId)){
		 int  startIndex=fullyQualifiedId.indexOf('{');
		 int endIndex=fullyQualifiedId.indexOf('}');
		 return fullyQualifiedId.substring(startIndex+1, endIndex);

	   }
	   return null;
   }
      /**
    * returns "direct:partitionId_modelId_applicationId"
    * @return
    */
   public static String getEndpoint(ActivityInstance activityInstance){
	 String partitionId=activityInstance.getActivity().getApplication().getPartitionId();
	 String modelId=extractModelIdFromFullyQualifiedName(activityInstance.getActivity().getApplication().getQualifiedId());
	 String activityId=activityInstance.getActivity().getApplication().getId();
	 if(StringUtils.isNotEmpty(partitionId) && StringUtils.isNotEmpty(modelId) && StringUtils.isNotEmpty(activityId))
		return partitionId+"_"+modelId+"_"+activityId;
	 if( StringUtils.isNotEmpty(modelId) && StringUtils.isNotEmpty(activityId))
			return modelId+"_"+activityId;
	 if( StringUtils.isNotEmpty(activityId))
			return activityId;
	 return null ;
   }

   /**
    * copy the content of the IN Message to the Out Message.
    * The headers, attachments and the body are copied.
    * @param exchange
    */
   public static void copyInToOut(Exchange exchange)
   {
      exchange.getOut().setAttachments(exchange.getIn().getAttachments());
      exchange.getOut().setHeaders(exchange.getIn().getHeaders());
      exchange.getOut().setBody(exchange.getIn().getBody());
   }
   /**
    * copy the content of the IN Message to the Out Message and set outBody
    * The headers, attachments and the body are copied.
    * @param exchange
    */
   public static void copyInToOut(Exchange exchange, Object outBOdy)
   {
      exchange.getOut().setAttachments(exchange.getIn().getAttachments());
      exchange.getOut().setHeaders(exchange.getIn().getHeaders());
      exchange.getOut().setBody(outBOdy);
   }

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
      if ((application.getAttribute(APPLICATION_INTEGRATION_OVERLAY_ATT) != null)
            && ((String) application.getAttribute(APPLICATION_INTEGRATION_OVERLAY_ATT))
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
    * return the value of carnot:engine:camel::producerBpmTypeConverter attribute defined in the trigger.
    *
    * @param trigger
    * @return
    */
   public static boolean includeConversionStrategy(final ITrigger trigger)
   {

      Object includeConverter= trigger.getAllAttributes().get("carnot:engine:camel::producerBpmTypeConverter");
     if(includeConverter!=null){
        Boolean includeConversionStrategy=Boolean.parseBoolean(includeConverter.toString());
        return includeConversionStrategy;
     }
        return false;

   }


   /**
    * return the value of carnot:engine:camel::producerInboundConversion attribute defined in the trigger.
    * otherwise fromXML as a default value
    * @param trigger
    * @return
    */
   public static String getConversionStrategy(final ITrigger trigger)
   {

      String strategy= (String) trigger.getAllAttributes().get("carnot:engine:camel::producerInboundConversion");
     if(StringUtils.isNotEmpty(strategy) && !strategy.equalsIgnoreCase("None")){
        return strategy;
     }
        return "fromXML";

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
  public static String getInvocationType(final Application application)
  {
     return (String) application.getAttribute(INVOCATION_TYPE_EXT_ATT);
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
      return ai.getActivity().getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT);
   }

   /**
    *
    * @param ai
    * @return
    */
   public static ApplicationContext getActivityInstanceDefaultContext(final ActivityInstance ai)
   {
      return ai.getActivity().getApplicationContext(PredefinedConstants.DEFAULT_CONTEXT);
   }
   
   /**
   * Return the first ApplicationContext for the given ActivityInstance which provides out data mappings.
   * If no context with out data mappings is found, <code>null</code> is returned.
   * @param ai
   * @return ApplicationContext, or <code>null</code> if no context with OUT data mappings is found
   */
  public static ApplicationContext getFirstApplicationContextWithOutMappings(final ActivityInstance ai)
  {
     List<ApplicationContext> applicationContexts = ai.getActivity().getAllApplicationContexts();
     for (ApplicationContext applicationContext : applicationContexts)
     {
        if(applicationContext.getAllOutDataMappings() != null && !applicationContext.getAllOutDataMappings().isEmpty())
        {
           return applicationContext;
        }
     }
     return null;
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

   public static String getDescription(final String partition, final String modelId, final String elementId)
   {
      StringBuilder description = new StringBuilder("This route is related to "+elementId+" defined in "+modelId+". The partition is :"+partition);
      return description.toString();
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

   /**
    * Returns list of AccessPointProperties; based on the provided trigger configuration
    * @return
    * @throws IOException
    */
   @SuppressWarnings({"rawtypes"})
   public static List<AccessPointProperties> performParameterMapping(ITrigger trigger)
         throws IOException
   {
      Map<String, String> schemaRefs = new HashMap<String, String>();
      List<AccessPointProperties> accessPointList = new ArrayList<AccessPointProperties>();
      ModelElementList parameterMappings = trigger.getParameterMappings();

      Link typeBean = ((Link) ((ModelBean) trigger.getModel()).getTypeDeclarations());
      if (!typeBean.isEmpty())
      {

         Iterator iter = typeBean.iterator();
         while (iter.hasNext())
         {
            TypeDeclarationBean extType = (TypeDeclarationBean) iter.next();

            if (extType.getXpdlType() instanceof ExternalReferenceBean)
            {
               ExternalReferenceBean xpdlType = ((ExternalReferenceBean) extType
                     .getXpdlType());
               schemaRefs.put(extType.getId(), xpdlType.getLocation().replace('.', '*'));

            }
            else
            {
               schemaRefs.put(extType.getId(),
                     "internal:" + ((ModelBean) trigger.getModel()).getId() + "::"
                           + extType.getId());
            }

            // the last replace is to overcome a camel bug in parsing of
            // bean
            // name when the
            // method/param
            // contains a . character

         }
      }
      else
      {// check for external references
         for (Object parameter : parameterMappings)
         {
            ParameterMappingBean parameterMapping = (ParameterMappingBean) parameter;
            if (parameterMapping != null && parameterMapping.getData() != null)
            {
               if (parameterMapping.getData().getExternalReference() != null)
               {
                  IReference ref = parameterMapping.getData().getExternalReference();

                  schemaRefs.put(ref.getId(), "reference:"
                        + ref.getExternalPackage().getReferencedModel().getId() + "::"
                        + ref.getId());
               }
               else if ((parameterMapping.getData().getStringAttribute(
                     "carnot:engine:dataType") != null)
                     && (((ModelBean) parameterMapping.getData().getParent()).getId() != null))
               {

                  schemaRefs.put(
                        parameterMapping.getData().getStringAttribute(
                              "carnot:engine:dataType"),
                        "reference:"
                              + ((ModelBean) parameterMapping.getData().getParent())
                                    .getId()
                              + "::"
                              + parameterMapping.getData().getStringAttribute(
                                    "carnot:engine:dataType"));
               }

            }
         }

      }

      for (int i = 0; i < parameterMappings.size(); ++i)
      {

         IParameterMapping mapping = (IParameterMapping) parameterMappings.get(i);
         AccessPointProperties accessPtProps = new AccessPointProperties();
         accessPtProps.setParamId(mapping.getParameterId());
         String outBodyAccesPoint = (String) trigger.getAllAttributes().get(
               "carnot:engine:camel::outBodyAccessPoint");
         if ((outBodyAccesPoint != null && outBodyAccesPoint.equalsIgnoreCase(mapping
               .getParameterId()))
               || (mapping != null && mapping.getParameterId() != null && mapping
                     .getParameterId().equalsIgnoreCase(ACCESS_POINT_MESSAGE)))
         {
            accessPtProps.setAccessPointLocation(ACCESS_POINT_MESSAGE);
            accessPtProps.setAccessPointPath(mapping.getParameterPath());
         }
         else
         {
            accessPtProps.setAccessPointLocation(ACCESS_POINT_HEADERS);
            accessPtProps
                  .setAccessPointPath("get"
                        + getOutAccessPointNameUsingDataMappingName((ParameterMappingBean) mapping)
                        + "()");
         }
         // accessPtProps.setAccessPointLocation(mapping.getParameterId());

         accessPtProps.setData(mapping.getData());
         accessPtProps.setDataPath(mapping.getDataPath());
         if (mapping.getData() != null
               && mapping.getData().getExternalReference() != null)
            accessPtProps.setXsdName(schemaRefs.get(mapping.getData()
                  .getExternalReference().getId()));
         else
         {

            accessPtProps.setXsdName(schemaRefs.get(mapping.getData().getStringAttribute(
                  "carnot:engine:dataType")));
         }
         /*
          * if(mapping.getData().getAllAttributes().containsKey(
          * "carnot:engine:dms:resourceMetadataSchema")) { if(! accessPtProps.
          * getAccessPointType().startsWith(STARDUST_ENGINE_CLASS))
          * accessPtProps.setAccessPointType(DOCUMENT_LIST); }
          */
         accessPtProps.setAccessPointType(mapping.getData().getType().getId());
         // accessPtProps.setEndPoint(endpoint);
         accessPointList.add(accessPtProps);
      }
      return accessPointList;
   }

   //

   @SuppressWarnings("rawtypes")
   private static String getOutAccessPointNameUsingDataMappingName(
         ParameterMappingBean mapping)
   {
      if (mapping != null
            && ((TriggerBean) mapping.getParent()).getAllOutAccessPoints() != null)
      {
         Iterator itr = ((TriggerBean) mapping.getParent()).getAllOutAccessPoints();
         while (itr.hasNext())
         {
            AccessPointBean accessPoint = (AccessPointBean) itr.next();
            if (accessPoint.getId().equalsIgnoreCase(mapping.getParameterId()))
            {
               return accessPoint.getName();
            }
         }

      }
      return null;
   }

   /**
    *
    * @param Trigger
    * @return Body Out AccessPoint ID
    */
   public static Object getBodyOutAccessPoint(final Trigger trigger)
   {
      return trigger.getAttribute(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
   }

   /**
    *
    * @param trigger
    * @return Trigger Event Implementation
    */
   public static String getEventImplementation(final ITrigger trigger)
   {
      return (String) trigger.getAttribute(CamelConstants.TRIGGER_INTEGRATION_OVERLAY_ATT);
   }
   
   /**
    * Lookup Exception Detail Message.
    * @param throwable
    * @return
    */
   private static String getThrowableDetailMessage(Throwable throwable)
   {
      return throwable.getMessage() != null
            ? throwable.getMessage()
            : getThrowableDetailMessage(throwable.getCause());
   }
   
   /**
    * Build exception msg for inconsistencies
    * @param Exception
    * @return
    */
   public static String buildExceptionMessage(Exception e)
   {
      String msg =e.getMessage();
      if(e.getCause() != null)
      {
         msg += " Cause ";
         msg += e.getCause().getClass().getName();
         msg += ": ";
         msg += getThrowableDetailMessage(e.getCause());
      }
      return msg;
   }
}
