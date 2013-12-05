package org.eclipse.stardust.engine.extensions.camel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;

public final class CamelConstants
{

   public static final String PRP_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.applicationContext";
	
   public static final String SPRING_XML_ROUTES_HEADER = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
   public static final String SPRING_XML_ROUTES_FOOTER = "</routes>";
   public static final String SPRING_XML_ROUTE_FOOTER = "</route>";
   public static final String SPRING_XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<beans xmlns=\"http://www.springframework.org/schema/beans\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \nxsi:schemaLocation=\"http://www.springframework.org/schema/beans\nhttp://www.springframework.org/schema/beans/spring-beans.xsd\">\n";
   public static final String SPRING_XML_FOOTER = "\n</beans>";
   public static final String IPP_DIRECT_TAG = "ipp:direct";
   public static final String IPP_AUTHENTICATE_TAG = "ipp:authenticate";

   public final static String CAMEL_TRIGGER_TYPE = "camel";
   public final static String CAMEL_SCOPE = PredefinedConstants.ENGINE_SCOPE + "camel:"; //$NON-NLS-1$
   public final static String ENRICHER_CLASSNAME= "org.eclipse.stardust.engine.extensions.camel.enricher.MapAppenderProcessor";
   
   public final static String PRODUCER_METHOD_NAME_ATT = CAMEL_SCOPE + ":producerMethodName"; //$NON-NLS-1$
   public final static String INVOCATION_TYPE_EXT_ATT = CAMEL_SCOPE + ":invocationType"; //$NON-NLS-1$
   public final static String ADDITIONAL_SPRING_BEANS_DEF_ATT = CAMEL_SCOPE + ":additionalSpringBeanDefinitions"; //$NON-NLS-1$
   public final static String CAMEL_CONTEXT_ID_ATT = CAMEL_SCOPE + ":camelContextId"; //$NON-NLS-1$
   public final static String PRODUCER_ROUTE_ATT = CAMEL_SCOPE + ":routeEntries"; //$NON-NLS-1$
   public final static String CONSUMER_ROUTE_ATT = CAMEL_SCOPE + ":consumerRoute"; //$NON-NLS-1$
   public final static String BODY_PARAM_ATT = CAMEL_SCOPE + ":exchange:body"; //$NON-NLS-1$
   public final static String ENDPOINT_URI_ATT = CAMEL_SCOPE + ":endpointURI"; //$NON-NLS-1$
   public final static String ENDPOINT_TYPE_CLASS_ATT = CAMEL_SCOPE + ":endpointTypeClass";
   public final static String ROUTE_EXT_ATT = CAMEL_SCOPE + ":camelRouteExt";
   public static final String CORRELATION_PATTERN_EXT_ATT = CAMEL_SCOPE + ":correlationPattern";
   public static final String INVOCATION_PATTERN_EXT_ATT = CAMEL_SCOPE + ":invocationPattern";
   public static final String PROCESS_CONTEXT_HEADERS_EXT_ATT = CAMEL_SCOPE + ":processContextHeaders";
   public static final String DEFAULT_CAMEL_CONTEXT_ID = "defaultCamelContext";
   
   public static final String SUPPORT_MULTIPLE_ACCESS_POINTS = CAMEL_SCOPE+":supportsMultipleAccessPoints";
   public static final String CAT_BODY_IN_ACCESS_POINT = CAMEL_SCOPE+":inBodyAccessPoint";
   public static final String CAT_BODY_OUT_ACCESS_POINT = CAMEL_SCOPE+":outBodyAccessPoint";
   public static final String CAT_HEADERS_OUT_ACCESS_POINT = CAMEL_SCOPE+":outputHeadersAccessPoint";
   

   public static final String METHOD_PARAMETER_PREFIX = "Param"; //$NON-NLS-1$
   public static final String ENDPOINT_PKG = "org.eclipse.stardust.engine.extensions.camel.runtime";

   public static final String GENERIC_ENDPOINT = ENDPOINT_PKG + "." + "GenericEndpoint";
   public static final String JMS_ENDPOINT = ENDPOINT_PKG + "." + "JmsEndpoint";
   public static final String File_ENDPOINT = ENDPOINT_PKG + "." + "FileEndpoint";
   public static final String Mail_ENDPOINT = ENDPOINT_PKG + "." + "MailEndpoint";
   public static final String QUARTZ_ENDPOINT = ENDPOINT_PKG + "." + "QuartzEndpoint";
   public static final String RESTLET_ENDPOINT = ENDPOINT_PKG + "." + "RestletEndpoint";

   public static final String GENERIC_ENDPOINT_KEY = "Generic Endpoint";
   public static final String FILE_ENDPOINT_KEY = "File";
   // public static final String WS_ENDPOINT_KEY= "Ws";
   public static final String JMS_ENDPOINT_KEY = "Jms";
   public static final String MAIL_ENDPOINT_KEY = "Mail";
   public static final String QUARTZ_ENDPOINT_KEY = "Quartz";
   //public static final String RESTLET_ENDPOINT_KEY = "Restlet";
   
   public static final String CAMEL_CONSUMER_APPLICATION_TYPE = "camelConsumerApplication";
   public static final String CAMEL_PRODUCER_APPLICATION_TYPE = "camelSpringProducerApplication";

   /**
    * Key value mapping the class to be used should be unique per key
    * 
    * @return
    */
   public static final Map<String, String> getManagedEndpoints()
   {

      Map<String, String> edpoints = new HashMap<String, String>();
      edpoints.put(GENERIC_ENDPOINT_KEY, GENERIC_ENDPOINT);
      edpoints.put(FILE_ENDPOINT_KEY, File_ENDPOINT);
      // endpointType.add("Ws");
//      edpoints.put(JMS_ENDPOINT_KEY, JMS_ENDPOINT);
//      edpoints.put(MAIL_ENDPOINT_KEY, Mail_ENDPOINT);
//      edpoints.put(QUARTZ_ENDPOINT_KEY, QUARTZ_ENDPOINT);
    //  edpoints.put(RESTLET_ENDPOINT_KEY, RESTLET_ENDPOINT);
      return edpoints;

   }

   /**
    * Returns the key of a value in the map
    * 
    * @param map
    * @param value
    * @return
    */
   public static String getKeyByValue(Map<String, String> map, String value)
   {
      for (Entry<String, String> entry : map.entrySet())
      {
         if (value.equals(entry.getValue()))
         {
            return entry.getKey();
         }
      }
      return null;
   }

   private CamelConstants()
   {}
    
   
   public static final class CorrelationValue
   {
      public static final String PROCESS = "process";
      public static final String ACTIVITY = "activity";
      public static final String DATA = "data";
   }
   
   public static final class OriginValue
   {
      public static final String TRIGGER_CONSUMER = "triggerConsumer";
      public static final String APPLICATION_CONSUMER = "applicationConsumer";
      public static final String APPLICATION_PRODUCER = "applicationProducer";
   }
   
   public static final class InvocationTypes
   {
      public static final String SYNCHRONOUS  = "synchronous";
      public static final String ASYNCHRONOUS = "asynchronous";
   }
   
   public static final class InvocationPatterns
   {
      public static final String SEND  = "send";
      public static final String SENDRECEIVE = "sendReceive";
      public static final String RECEIVE = "receive";
   }
   
   public static final class Endpoint
   {
      public static final String PROCESS = "process";
      public static final String ACTIVITY = "activity";
      public static final String AUTHENTICATE = "authenticate";
      public static final String DMS = "dms";
   }

   public static final class SubCommand
   {
      public static final class Process
      {
         public static final String COMMAND_CONTINUE = "continue";
         public static final String COMMAND_SET_PROPERTIES = "setProperties";
         public static final String COMMAND_GET_PROPERTIES = "getProperties";
         public static final String COMMAND_START = "start";
         public static final String COMMAND_ATTACH = "attach";
         public static final String COMMAND_FIND = "find";
         public static final String COMMAND_SPAWN_SUB_PROCESS="spawnSubprocess";
      }

      public static final class Authenticate
      {
         public static final String COMMAND_SET_CURRENT = "setCurrent";
         public static final String COMMAND_REMOVE_CURRENT = "removeCurrent";
      }

      public static final class Activity
      {
         public static final String COMMAND_FIND = "find";
         public static final String COMMAND_COMPLETE = "complete";
      }
   }

   public static final class MessageProperty
   {

         public static final String PROCESS_ID = "ippProcessId";
         public static final String MODEL_ID = "ippModelId";
         public static final String ACTIVITY_INSTANCE_OID = "ippActivityInstanceOid";
         public static final String EXPECTED_RESULT_SIZE = "ippExpectedResultSize";
         public static final String USER = "ippUser";
         public static final String PASSWORD = "ippPassword";
         public static final String PARTITION = "ippPartition";
         public static final String REALM = "ippRealm";
         public static final String DOMAIN = "ippDomain";
         public static final String PROCESS_INSTANCE_OID = "ippProcessInstanceOid";
         public static final String PROCESS_INSTANCE_PROPERTIES = "ippProcessInstanceProperties";
         public static final String ATTACHMENT_FILE_NAME = "ippAttachmentFileName";
         public static final String ATTACHMENT_FOLDER_NAME = "ippAttachmentFolderName";
         public static final String ATTACHMENT_FILE_CONTENT = "ippAttachmentFileContent";
         public static final String PROCESS_ATTACHMENTS = "PROCESS_ATTACHMENTS";//TODO use constant for attachments key declared in IPP
         public static final String ACTIVITY_ID = "ippActivityId";
         public static final String APPLICATION_ID = "ippApplicationId";
         public static final String DATA_ID = "ippDataId";
         public static final String DATA_MAP_ID = "ippDataFilterMap";
         public static final String ATTRIBUTE_NAME= "ippAttributeName";
         public static final String DATA_VALUE = "ippDataValue";
         public static final String ACTIVITY_INSTANCES = "ippActivityInstances";
         public static final String PROCESS_INSTANCES = "ippProcessInstances";
         public static final String ORIGIN = "ippOrigin";
         public static final String ROUTE_ID = "ippRouteId";
         public static final String COPY_DATA="ippCopyData";
         public static final String PARENT_PROCESS_INSTANCE_OID="ippParentProcessInstanceOid";
   }
}
