package org.eclipse.stardust.engine.extensions.json;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.extensions.json.GsonHandler;
import org.junit.Test;

public class TestUnMarshaller
{
   @Test
   public void testUnMarshallStructure()
   {
      String json = "{\"templateUri\":\"classpath://custom/templates/simpleTemplate.vm\",\"format\":\"text\",\"pdf\":false,\"parameters\":{\"aName\":\"some name\",\"contract\":{\"Name\":\"cba\",\"number\":\"1002\"},\"customer\":{\"lastName\":\"cba\",\"firstName\":\"abc\"},\"persons\":[{\"lastName\":\"cba\",\"address\":{\"street\":\"abc\",\"city\":\"cba\"},\"dob\":\"17 nov. 2014 11:48:21\",\"firstName\":\"abc\"},{\"lastName\":\"cba\",\"address\":{\"street\":\"abc\",\"city\":\"cba\"},\"dob\":\"17 nov. 2014 11:48:21\",\"firstName\":\"abc\"}]},\"output\":{\"activityInstance\":123,\"accessPoint\":\"key\"}}";
      GsonHandler handler = new GsonHandler();
      Object request = handler.fromJson(json, Object.class);
      assertNotNull(request);
      assertTrue(((Map) request).size() == 5);
      assertTrue(((Map) ((Map) request).get("parameters")).size() == 4);
   }

   @Test
   public void testUnMarshallComplexStructure()
   {
      String json = "{\"id\":\"769bcc37-5dd5-45d6-953f-5fc260531d70\",\"timestamp\":1416236881047,\"commandId\":\"modelElement.update\",\"modelId\":\"EmailTemplate\",\"account\":\"motu\",\"changes\":{\"modified\":[{\"oid\":494,\"id\":\"EmailTemp\",\"name\":\"EmailTemp\",\"modelId\":\"EmailTemplate\",\"uuid\":\"af1d79e7-13e3-46f9-9394-0e6934a27628\",\"type\":\"application\",\"description\":\"TEST EMAIL\",\"attributes\":{\"carnot:engine:camel::applicationIntegrationOverlay\":\"mailIntegrationOverlay\",\"carnot:engine:camel::supportsMultipleAccessPoints\":true,\"carnot:engine:camel::transactedRoute\":true,\"carnot:engine:camel::camelContextId\":\"defaultCamelContext\",\"carnot:engine:camel::invocationPattern\":\"send\",\"carnot:engine:camel::invocationType\":\"synchronous\",\"carnot:engine:camel::routeEntries\":\"\\u003cto uri\\u003d\\\"bean:documentHandler?method\\u003dprocessTemplateConfigurations\\\"/\\u003e\\n\\u003cchoice\\u003e\\n  \\u003cwhen\\u003e\\n     \\u003csimple\\u003e$simple{in.header.from} \\u003d\\u003d null\\u003c/simple\\u003e\\n     \\u003csetHeader headerName\\u003d\\\"from\\\"\\u003e\\n        \\u003cconstant\\u003e\\u0027infinity-support@sungard.com\\u0027      \\u003c/constant\\u003e\\n     \\u003c/setHeader\\u003e\\n  \\u003c/when\\u003e\\n\\u003c/choice\\u003e\\n\\u003cchoice\\u003e\\n  \\u003cwhen\\u003e\\n     \\u003csimple\\u003e$simple{in.header.to} \\u003d\\u003d null\\u003c/simple\\u003e\\n     \\u003csetHeader headerName\\u003d\\\"to\\\"\\u003e\\n        \\u003cconstant\\u003e\\u0027\\u0027 + person.lastname + \\u0027\\u0027\\u003c/constant\\u003e\\n     \\u003c/setHeader\\u003e\\n  \\u003c/when\\u003e\\n\\u003c/choice\\u003e\\n\\u003cprocess ref\\u003d\\\"customVelocityContextAppender\\\"/\\u003e\\n\\u003csetHeader headerName\\u003d\\\"CamelVelocityTemplate\\\"\\u003e\\n   \\u003cconstant\\u003e\\n\\u003c![CDATA[#parse(\\\"commons.vm\\\")\\n#getInputs()\\ntest $person.firstname\\n#setOutputs()\\n]]\\u003e\\n   \\u003c/constant\\u003e\\n\\u003c/setHeader\\u003e\\n\\u003cto uri\\u003d\\\"templating:embedded?format\\u003dtext\\\" /\\u003e\\n\\u003csetHeader headerName\\u003d\\\"subject\\\"\\u003e\\n\\u003csimple\\u003e$simple{body}\\u003c/simple\\u003e\\n\\u003c/setHeader\\u003e\\n\\u003cprocess ref\\u003d\\\"customVelocityContextAppender\\\"/\\u003e\\n\\u003csetHeader headerName\\u003d\\\"CamelVelocityResourceUri\\\"\\u003e\\n   \\u003cconstant\\u003e\\u003c/constant\\u003e\\n\\u003c/setHeader\\u003e\\n\\u003cto uri\\u003d\\\"templating:classpath?format\\u003dtext\\\" /\\u003e\\n\\u003csetHeader headerName\\u003d\\\"contentType\\\"\\u003e\\n   \\u003cconstant\\u003etext/plain\\u003c/constant\\u003e\\n\\u003c/setHeader\\u003e\\u003cto uri\\u003d\\\"smtp://10.215.25.65\\\"/\\u003e\",\"carnot:engine:camel::includeAttributesAsHeaders\":\"false\",\"carnot:engine:camel::processContextHeaders\":\"true\",\"stardust:emailOverlay::server\":\"10.215.25.65\",\"stardust:emailOverlay::mailFormat\":\"text/plain\",\"stardust:emailOverlay::protocol\":\"smtp\",\"stardust:emailOverlay::includeUniqueIdentifierInSubject\":false,\"stardust:emailOverlay::storeEmail\":false,\"stardust:emailOverlay::storeAttachments\":false,\"stardust:emailOverlay::templateSource\":\"classpath\",\"stardust:emailOverlay::templateConfigurations\":\"[]\",\"stardust:emailOverlay::subject\":\"test {{person.firstname}}\",\"stardust:emailOverlay::from\":\"infinity-support@sungard.com\",\"stardust:emailOverlay::to\":\"{{person.lastname}}\"},\"comments\":[],\"interactive\":false,\"contexts\":{\"application\":{\"accessPoints\":[{\"id\":\"to\",\"name\":\"to\",\"dataType\":\"primitive\",\"direction\":\"IN\",\"attributes\":{\"stardust:predefined\":true},\"primitiveDataType\":\"String\",\"comments\":[]},{\"id\":\"from\",\"name\":\"from\",\"dataType\":\"primitive\",\"direction\":\"IN\",\"attributes\":{\"stardust:predefined\":true},\"primitiveDataType\":\"String\",\"comments\":[]},{\"id\":\"cc\",\"name\":\"cc\",\"dataType\":\"primitive\",\"direction\":\"IN\",\"attributes\":{\"stardust:predefined\":true},\"primitiveDataType\":\"String\",\"comments\":[]},{\"id\":\"bcc\",\"name\":\"bcc\",\"dataType\":\"primitive\",\"direction\":\"IN\",\"attributes\":{\"stardust:predefined\":true},\"primitiveDataType\":\"String\",\"comments\":[]},{\"id\":\"subject\",\"name\":\"subject\",\"dataType\":\"primitive\",\"direction\":\"IN\",\"attributes\":{\"stardust:predefined\":true},\"primitiveDataType\":\"String\",\"comments\":[]},{\"id\":\"person\",\"name\":\"person\",\"dataType\":\"struct\",\"direction\":\"IN\",\"attributes\":{\"carnot:engine:path:separator\":\"/\",\"carnot:engine:data:bidirectional\":true},\"structuredDataTypeFullId\":\"EmailTemplate:person\",\"comments\":[]}]}},\"applicationType\":\"camelSpringProducerApplication\"}],\"added\":[],\"removed\":[]},\"uiState\":{\"modelLocks\":[{\"modelId\":\"Model1\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"GenericApplicationConsumerDocumentTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"Model140\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"Model141\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"DynamicEmailTemplatingModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"ModelData\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"GenericApplicationProducerTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"melekModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"Model12\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"SmsApplicationTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"SqlModelTest\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"Aligne_DCP_Midstream_1.2.10_broken\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"MailApplicationTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"SmsModelTest\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"EmailTemplate\",\"lockStatus\":\"lockedByMe\",\"ownerId\":\"default:17:motu\",\"ownerName\":\"Of the Universe, Master (motu)\",\"canBreakEditLock\":true},{\"modelId\":\"TestTemplatingOverlay\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"SMSNotificationModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"Aligne_DCP_Midstream\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"TemplatingApplicationTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"GenericApplicationDocumentTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"CamelMailTriggerModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false},{\"modelId\":\"AuthenticationCommandTestModel\",\"lockStatus\":\"\",\"canBreakEditLock\":false}]}}";
      GsonHandler handler = new GsonHandler();
      Object request = handler.fromJson(json, Object.class);
      assertNotNull(request);
      assertTrue(((Map) request).size() == 7);

   }

   @Test
   public void unMarshallTemplatingRequest()
   {
      String json = "{\"output\":{\"name\":\"Coverletter_template.docx\",\"path\":\"/artifacts/templates/pi-633/process-attachments\"},\"pdf\":false,\"templateUri\":\"repository://Coverletter_template.docx\",\"format\":\"docx\",\"parameters\":{\"processOid\":\"633\",\"templateUri\":\"repository://Coverletter_template.docx\",\"isPdf\":false,\"DOCUMENT_REQUEST\":{\"Documents\":[{\"Name\":\"Coverletter_template.docx\",\"DocumentType\":\"Letter\",\"IsTemplate\":\"true\",\"ConvertToPDF\":\"true\",\"IsAttachment\":\"true\",\"Required\":\"false\",\"Accepted\":\"false\",\"Exists\":\"false\",\"Comment\":\"Cover letter for requested documents\",\"DocumentLocation\":\"Coverletter_template.docx\",\"Requested\":\"2015-10-12\"},{\"Name\":\"PassportCopy\",\"DocumentType\":\"Passport\",\"IsTemplate\":\"false\",\"ConvertToPDF\":\"false\",\"IsAttachment\":\"false\",\"Required\":\"true\",\"Accepted\":\"false\",\"Exists\":\"false\",\"Comment\":\"Need passport copy for ID\",\"Requested\":\"2015-10-12\"},{\"Name\":\"MedicalReport\",\"DocumentType\":\"Medical Report\",\"IsTemplate\":\"false\",\"ConvertToPDF\":\"false\",\"IsAttachment\":\"false\",\"Required\":\"true\",\"Accepted\":\"false\",\"Exists\":\"false\",\"Comment\":\"Need Doctor's Report copy\",\"Requested\":\"2015-10-12\"},{\"Name\":\"Person.docx\",\"DocumentType\":\"fff\",\"IsTemplate\":true,\"ConvertToPDF\":\"true\",\"IsAttachment\":\"true\",\"Required\":\"true\",\"Accepted\":\"false\",\"Exists\":\"false\",\"Comment\":\"ggg\",\"DocumentLocation\":\"Person.docx\",\"Requested\":\"2015-10-12T04:46:28.964Z\"},{\"Name\":\"CD004LC_template.docx\",\"DocumentType\":\"hhh\",\"IsTemplate\":true,\"ConvertToPDF\":\"true\",\"IsAttachment\":\"true\",\"Required\":\"true\",\"Accepted\":\"false\",\"Exists\":\"false\",\"Comment\":\"ggghhh\",\"DocumentLocation\":\"CD004LC_template.docx\",\"Requested\":\"2015-10-12T04:46:35.491Z\"}]}}}";
      GsonHandler handler = new GsonHandler();
      Object request = handler.fromJson(json, Object.class);
      assertNotNull(request);
      assertTrue(request instanceof Map);
      Map<String, Object> parameters = (Map<String, Object>) ((Map<String, Object>) request)
            .get("parameters");
      assertNotNull(parameters);

      Map<String, Object> documentRequest = (Map<String, Object>) (parameters)
            .get("DOCUMENT_REQUEST");
      assertNotNull(documentRequest);
      List<Map<String, Object>> documents = (List<Map<String, Object>>) documentRequest
            .get("Documents");
      assertNotNull(documents);
      assertTrue(((Map) request).size() == 5);
      Map<String, Object> item = documents.get(0);
      assertNotNull(item);
      assertFalse((Boolean) ((Map) item).get("Required"));
      assertTrue((Boolean) ((Map) item).get("IsTemplate"));
   }

}
