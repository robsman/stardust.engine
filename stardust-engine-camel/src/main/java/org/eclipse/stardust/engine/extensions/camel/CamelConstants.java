package org.eclipse.stardust.engine.extensions.camel;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

public final class CamelConstants
{
   public static final String GREATER_THAN_SIGN = ">";
   public static final String LESS_THAN_SIGN = "<";
   public static final String BLANK_SPACE = " ";
   public static final String NEW_LINE = "\n";
   public static final String HORIZONTAL_TAB = "\t";
   public static final String COLON=":";
   public static final String DOUBLE_SLASH="//";
   public static final String DIRECT_COMPONENT="direct";
   public static final String DIRECT_ENDPOINT=DIRECT_COMPONENT+COLON+DOUBLE_SLASH;
   public static final String ENDPOINT_PREFIX = NEW_LINE+"<to uri=\"";
   public static final String ENDPOINT_SUFFIX = "\"/>";
   public static final String QUOTATION = "\"";
   public static final String ACCESS_POINT_MESSAGE = "message";
   public static final String ACCESS_POINT_HEADERS = "headers";
   public static final String DOCUMENT_LIST = "dmsDocumentList";
   public static final String DOCUMENT = "dmsDocument";
   public static final String PRP_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.applicationContext";
   public static final String SEND_METHOD = "executeMessage(java.lang.Object)"; 
   public static final String SEND_METHOD_WITH_HEADER = "executeMessage(java.lang.Object,java.util.Map"+LESS_THAN_SIGN+"java.lang.String,java.lang.Object"+GREATER_THAN_SIGN+")"; 
   public static final String SEND_RECEIVE_METHOD_WITH_HEADER = "sendBodyInOut(java.lang.Object,java.util.Map"+LESS_THAN_SIGN+"java.lang.String,java.lang.Object"+GREATER_THAN_SIGN+")"; 
   
   public static final String SPRING_XML_ROUTES_HEADER = LESS_THAN_SIGN+"routes xmlns=\"http"+COLON+"//camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_ROUTES_FOOTER = LESS_THAN_SIGN+"/routes"+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_ROUTE_FOOTER = LESS_THAN_SIGN+"/route"+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_MAP_ELT_HEADER= LESS_THAN_SIGN+"map"+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_MAP_ELT_FOOTER= LESS_THAN_SIGN+"/map"+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_VALUE_ELT_HEADER= LESS_THAN_SIGN+"value"+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_VALUE_ELT_FOOTER= LESS_THAN_SIGN+"/value"+GREATER_THAN_SIGN+"";
   public static final String SPRING_XML_ENTRY_ELT_HEADER= "entry";
   public static final String SPRING_XML_ENTRY_ELT_FOOTER= LESS_THAN_SIGN+"/entry"+GREATER_THAN_SIGN+"";
   
   public static final String SPRING_XML_HEADER = LESS_THAN_SIGN+"?xml version=\"1.0\" encoding=\"UTF-8\"?"+GREATER_THAN_SIGN+"\n"+LESS_THAN_SIGN+"beans xmlns=\"http"+COLON+"//www.springframework.org/schema/beans\"\nxmlns"+COLON+"xsi=\"http"+COLON+"//www.w3.org/2001/XMLSchema-instance\" \nxsi"+COLON+"schemaLocation=\"http"+COLON+"//www.springframework.org/schema/beans\nhttp"+COLON+"//www.springframework.org/schema/beans/spring-beans.xsd\""+GREATER_THAN_SIGN+"\n";
   public static final String SPRING_XML_FOOTER = "\n"+LESS_THAN_SIGN+"/beans"+GREATER_THAN_SIGN+"";
   public static final String IPP_DIRECT_TAG = "ipp"+COLON+"direct";
   public static final String IPP_AUTHENTICATE_TAG = "ipp"+COLON+"authenticate";

   public static final String CAMEL_TRIGGER_TYPE = "camel";
   public static final String CAMEL_SCOPE = PredefinedConstants.ENGINE_SCOPE + "camel"+COLON; 
   public static final String ENRICHER_CLASSNAME= "org.eclipse.stardust.engine.extensions.camel.enricher.MapAppenderProcessor";
   
   public static final String PRODUCER_METHOD_NAME_ATT = CAMEL_SCOPE + ""+COLON+"producerMethodName"; 
   public static final String INVOCATION_TYPE_EXT_ATT = CAMEL_SCOPE + ""+COLON+"invocationType"; 
   public static final String ADDITIONAL_SPRING_BEANS_DEF_ATT = CAMEL_SCOPE + ""+COLON+"additionalSpringBeanDefinitions"; 
   public static final String CAMEL_CONTEXT_ID_ATT = CAMEL_SCOPE + ""+COLON+"camelContextId"; 
   public static final String PRODUCER_ROUTE_ATT = CAMEL_SCOPE + ""+COLON+"routeEntries"; 
   public static final String CONSUMER_ROUTE_ATT = CAMEL_SCOPE + ""+COLON+"consumerRoute"; 
   public static final String PRODUCER_BPM_TYPE_CONVERTER = CAMEL_SCOPE + ""+COLON+"producerBpmTypeConverter"; 
   public static final String PRODUCER_OUTBOUND_CONVERSION = CAMEL_SCOPE + ""+COLON+"producerOutboundConversion"; 
   public static final String PRODUCER_INBOUND_CONVERSION = CAMEL_SCOPE + ""+COLON+"producerInboundConversion"; 
   public static final String CONSUMER_BPM_TYPE_CONVERTER = CAMEL_SCOPE + ""+COLON+"consumerBpmTypeConverter"; 
   public static final String CONSUMER_INBOUND_CONVERSION = CAMEL_SCOPE + ""+COLON+"consumerInboundConversion"; 
   public static final String BODY_PARAM_ATT = CAMEL_SCOPE + ""+COLON+"exchange"+COLON+"body"; 
   public static final String ENDPOINT_URI_ATT = CAMEL_SCOPE + ""+COLON+"endpointURI"; 
   public static final String ENDPOINT_TYPE_CLASS_ATT = CAMEL_SCOPE + ""+COLON+"endpointTypeClass";
   public static final String ROUTE_EXT_ATT = CAMEL_SCOPE + ""+COLON+"camelRouteExt";
   public static final String CORRELATION_PATTERN_EXT_ATT = CAMEL_SCOPE + ""+COLON+"correlationPattern";
   public static final String INVOCATION_PATTERN_EXT_ATT = CAMEL_SCOPE + ""+COLON+"invocationPattern";
   public static final String PROCESS_CONTEXT_HEADERS_EXT_ATT = CAMEL_SCOPE + ""+COLON+"processContextHeaders";
   public static final String INCLUDE_ATTRIBUTES_AS_HEADERS_EXT_ATT = CAMEL_SCOPE + ""+COLON+"includeAttributesAsHeaders";
   public static final String TRANSACTED_ROUTE_EXT_ATT = CAMEL_SCOPE + ""+COLON+"transactedRoute";
   public static final String AUTO_STARTUP_ROUTE_EXT_ATT = CAMEL_SCOPE + ""+COLON+"autoStartup";

   public static final String DEFAULT_CAMEL_CONTEXT_ID = "defaultCamelContext";
   
   public static final String SUPPORT_MULTIPLE_ACCESS_POINTS = CAMEL_SCOPE+""+COLON+"supportsMultipleAccessPoints";
   public static final String CAT_BODY_IN_ACCESS_POINT = CAMEL_SCOPE+""+COLON+"inBodyAccessPoint";
   public static final String CAT_BODY_OUT_ACCESS_POINT = CAMEL_SCOPE+""+COLON+"outBodyAccessPoint";
   public static final String CAT_HEADERS_OUT_ACCESS_POINT = CAMEL_SCOPE+""+COLON+"outputHeadersAccessPoint";

   public static final String METHOD_PARAMETER_PREFIX = "Param"; 
   public static final String GENERIC_ENDPOINT_KEY = "Generic Endpoint";
   public static final String FILE_ENDPOINT_KEY = "File";
   public static final String JMS_ENDPOINT_KEY = "Jms";
   public static final String MAIL_ENDPOINT_KEY = "Mail";
   public static final String QUARTZ_ENDPOINT_KEY = "Quartz";
   public static final String CAMEL_CONSUMER_APPLICATION_TYPE = "camelConsumerApplication";
   public static final String CAMEL_PRODUCER_APPLICATION_TYPE = "camelSpringProducerApplication";

   public static final String IPP_ENDPOINT_PROPERTIES = "IPP_ENDPOINT_PROPERTIES";
   public static final char CSV_DEFAULT_DELIMITER = ',';
   public static final String CSV_DELIMITER_KEY = "delimiter";
   public static final String CSV_AUTOGENHEADERS_KEY = "autogenHeaders";
   
   public static final String VFS_DOCUMENT_ACCESS_PATHE_EVALUATOR_CLASS ="org.eclipse.stardust.engine.extensions.dms.data.VfsDocumentAccessPathEvaluator";
   public static final String APPLICATION_INTEGRATION_OVERLAY_ATT = CAMEL_SCOPE+""+COLON+"applicationIntegrationOverlay";
   public static final String GENERIC_ENDPOINT_OVERLAY= "genericEndpointOverlay";
   public static final String TRIGGER_INTEGRATION_OVERLAY_ATT = PredefinedConstants.ENGINE_SCOPE +"integration"+COLON+""+COLON+"overlay";
   public static final String GENERIC_CAMEL_ROUTE_EVENT= "genericCamelRouteEvent";
   public static final String MAIL_ATTACHMENTS_AP_ID= "mailAttachmentsAP";
   public static final String MAIL_TEMPLATE_CONFIGURATION_ATT = "stardust:emailOverlay::templateConfigurations";
   public static final String CORRESPONDANCE_AP_ID= "CORRESPONDENCE";

   public static final String SCRIPTING_OVERLAY= "scriptingIntegrationOverlay";
   public static final String SCRIPTING_LANGUAGE_EA_KEY="stardust:scriptingOverlay::language";
   public static final String SCRIPT_CODE_CONTENT="stardust:scriptingOverlay::scriptCode";
   public static final String JAVASCRIPT="JavaScript";
   public static final String GROOVY="Groovy";
   public static final String PYTHON="Python";
   public static final String TEMPLATING_OVERLAY="templatingIntegrationOverlay";
   public static final String DOCX_FORMAT="docx";
   public static final String EMBEDDED_LOCATION="embedded";
   public static final String DATA_LOCATION="data";
   public static final String PRIMITIVE_TYPE="primitive"; 
   
   public static final String TEMPLATING_LOCATION = "CamelTemplatingLocation";
   public static final String TEMPLATING_TEMPLATE_CONTENT = "CamelTemplatingTemplateContent";
   public static final String TEMPLATING_FORMAT = "CamelTemplatingFormat";
   public static final String TEMPLATING_TEMPLATE = "CamelTemplatingTemplate";
   public static final String TEMPLATING_OUTPUT_NAME = "CamelTemplatingOutputName";
   public static final String TEMPLATING_CONVERT_TO_PDF = "CamelTemplatingConvertToPdf";
   public static final String TEMPLATING_REQUEST_METADATA= "CamelTemplatingMetaData";
   
   public static final String SQL_OVERLAY= "sqlIntegrationOverlay";
   public static final String SQL_QUERY="stardust:sqlScriptingOverlay::sqlQuery";
   public static final String SQL_OUTPUT_TYPE="stardust:sqlScriptingOverlay::outputType";
   public static final String SQL_DATASOURCE_ID="stardust:sqlScriptingOverlay::dataSourceId";
   
   
   private CamelConstants()
   {}
   
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
      public static final String DATA = "data";
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
         public static final String COMMAND_CURRENT_TX = "currentTx";
      }
      public static final class Activity
      {
         public static final String COMMAND_FIND = "find";
         public static final String COMMAND_COMPLETE = "complete";
      }
      public static final class Document
      {
         public static final String COMMAND_MOVE = "move";
         
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
         public static final String TRIGGER_ID = "ippTriggerId";
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
         public static final String DOCUMENT_ID = "ippDmsDocumentId";
         public static final String DOCUMENT_NAME = "ippDmsDocumentName";
         public static final String DOCUMENT_CONTENT = "ippDmsDocumentContent";
         public static final String TARGET_PATH = "ippDmsTargetPath";
   }
}
