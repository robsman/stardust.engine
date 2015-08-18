package org.eclipse.stardust.engine.extensions.camel.attachment;

import static org.apache.camel.Exchange.FILE_NAME_ONLY;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCUMENT_REQUEST_AP_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MAIL_ATTACHMENTS_AP_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MAIL_TEMPLATE_CONFIGURATION_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.DOCUMENT_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.DOCUMENT_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.TARGET_PATH;
import static org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfigurationUtils.toTemplateConfigurations;
import static org.eclipse.stardust.engine.extensions.camel.util.CamelDmsUtils.getDocumentUsingRepositoryLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ModelCamelContext;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.extensions.camel.CamelMessage;
import org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfiguration;
import org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfigurationUtils;
import org.eclipse.stardust.engine.extensions.camel.converter.BpmTypeConverter;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.CreateDocumentException;
import org.eclipse.stardust.engine.extensions.camel.util.CamelDmsUtils;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;

/**
 * This class is used to handle document attachment
 *
 * @author Sabri.Bousselmi
 * @version $Revision: $
 */

public class DocumentHandler
{

   public static final Logger logger = LogManager.getLogger(DocumentHandler.class);

   /**
    * Add DmsDocumentBean from Header to message Attachment
    *
    * @param exchange
    */
   public void toAttachment(Exchange exchange)
   {
      DocumentManagementService dms = getDocumentManagementService();
      Map<String, Object> headers = exchange.getIn().getHeaders();
      for (Map.Entry<String, Object> entry : headers.entrySet())
      {
         Object value = entry.getValue();
         if (value instanceof DmsDocumentBean)
         {
            DmsDocumentBean dmsDocumentBean = (DmsDocumentBean) value;
            byte[] content = dms.retrieveDocumentContent(dmsDocumentBean.getId());
            addDocumentToExchangeAttachment(exchange, content,
                  dmsDocumentBean.getName(), dmsDocumentBean.getContentType());
         }
      }
   }

   public void createAttachmentFromExchangeBody(Exchange exchange)
   {
      boolean convertedToPdf = (Boolean) exchange.getIn().getHeader(
            "CamelTemplatingConvertToPdf");
      String outputName = (String) exchange.getIn()
            .getHeader("CamelTemplatingOutputName");
      if (convertedToPdf)
      {
         exchange.getIn().addAttachment(outputName + ".pdf",
               new DataHandler(exchange.getIn().getBody(), "application/pdf"));
      }
      else
      {
         if (((String) exchange.getIn().getHeader("CamelTemplatingTemplate") != null && ((String) exchange
               .getIn().getHeader("CamelTemplatingTemplate")).endsWith(".docx"))
               || (exchange.getIn().getHeader("CamelTemplatingFormat") != null && exchange
                     .getIn().getHeader("CamelTemplatingFormat").equals("docx")))
         {
            exchange.getIn().addAttachment(outputName + ".docx",
                  new DataHandler(exchange.getIn().getBody(), "application/msword"));
         }
         else
         {
            String content = exchange.getContext().getTypeConverter()
                  .convertTo(String.class, exchange, exchange.getIn().getBody());
            exchange.getIn().addAttachment(outputName + ".txt",
                  new DataHandler(content, "text/plain"));
         }
      }
      exchange.getIn().setBody(null);
   }

   private ProcessDefinition findProcessDefinitionByProcessInstance(ServiceFactory sf,ProcessInstance pi){
      DeployedModel model=sf.getQueryService().getModel(pi.getModelOID());
      ProcessDefinition processDefinition = sf.getQueryService().getProcessDefinition("{"+model.getId()+"}"+pi.getProcessID());
      return processDefinition;
   }
   /**
    * Convert message body to Document
    *
    * TODO: make it more generic in a way to handle exchange hearders use similar logic
    * than the bpmConverter (location from body or header) should be extended to use
    * headerDOCUMENT_NAME for GenericFile
    *
    * @param exchange
    * @return Document
    * @throws IOException
    * @throws CreateDocumentException
    * @throws MessagingException
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public Document toDocument(Exchange exchange) throws IOException,
         CreateDocumentException, MessagingException
   {
      Document document = null;
      if (exchange != null)
      {
         ServiceFactory sf = getServiceFactory();
         DocumentManagementService dms = sf.getDocumentManagementService();
         byte[] jcrDocumentContent = null;
         String fileName = "";
         List<ActivityInstance> instances = BpmTypeConverter
               .lookupActivityInstance(exchange);
         for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
         {
            ActivityInstance activityInstance = i.next();
            ProcessInstance pi = activityInstance.getProcessInstance();
            ProcessDefinition processDefinition = findProcessDefinitionByProcessInstance(sf,pi);
            boolean processAttachmentSupport = processDefinition.getDataPath(PROCESS_ATTACHMENTS) != null ? true : false;
            Object messageContent = exchange.getIn().getBody();
            if (messageContent instanceof DmsDocumentBean)
            {
               DmsDocumentBean dmsDocumentBean = (DmsDocumentBean) messageContent;
               StringBuilder defaultPath = new StringBuilder(
                     org.eclipse.stardust.engine.api.runtime.DmsUtils.composeDefaultPath(
                           pi.getScopeProcessInstanceOID(), pi.getStartTime()))
                     .append("/");
               if(processAttachmentSupport)
               {
                  defaultPath.append(DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER);
               } else
               {
                  defaultPath.append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER);
               }
               defaultPath.append("/").append(dmsDocumentBean.getName());
               document = dms.getDocument(defaultPath.toString());
            }
            else
            {
               if (messageContent instanceof GenericFile< ? >)
               {
                  ((GenericFile) messageContent).getBinding().loadContent(exchange,
                        ((GenericFile) messageContent));
                  jcrDocumentContent = exchange
                        .getContext()
                        .getTypeConverter()
                        .convertTo(byte[].class, exchange,
                              ((GenericFile) messageContent).getBody());
                  fileName = (String) exchange.getIn().getHeader(FILE_NAME_ONLY);// TODO:
                                                                                 // replace
                                                                                 // by
                                                                                 // DOCUMENT_NAME
               }else if(messageContent instanceof MimeMessage){
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  ((MimeMessage) messageContent).writeTo(baos);
                  jcrDocumentContent=  baos.toByteArray();
                  fileName = ((String) exchange.getIn().getHeader(DOCUMENT_NAME)).trim().replaceAll("\n", "");
               }
               else
               // OutputStream, String
               {
                  jcrDocumentContent = exchange.getContext().getTypeConverter()
                        .convertTo(byte[].class, exchange, exchange.getIn().getBody());
                  fileName = (String) exchange.getIn().getHeader(DOCUMENT_NAME);
               }
               document = CamelDmsUtils.storeDocument(dms, pi, jcrDocumentContent,
                     fileName, processAttachmentSupport);
               if(document != null && processAttachmentSupport)
               {
                  List<Document> listProcessAttachments = (List<Document>) sf.getWorkflowService().getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
                  // initialize it if necessary
                  if (null == listProcessAttachments)
                  {
                     listProcessAttachments = new ArrayList<Document>();
                  }
                  listProcessAttachments.add(document);
                  sf.getWorkflowService().setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS, listProcessAttachments);
               }
            }
         }
         
      }
      return document;
   }

   @SuppressWarnings("unchecked")
   public void retrieveContent(Exchange exchange)
   {
      String inputDocumentTemplateAccessPointId = null;
      if (exchange != null)
      {
         DocumentManagementService dms = getDocumentManagementService();
         String repositoryLocation = (String) exchange.getIn().getHeader(TARGET_PATH);
         Document document = null;
         if (StringUtils.isNotEmpty(repositoryLocation))
         {
            if (!repositoryLocation.startsWith("/"))
               repositoryLocation = "/artifacts/" + repositoryLocation;
            document = dms.getDocument(repositoryLocation);
         }
         else
         {
            List<ActivityInstance> instances = BpmTypeConverter
                  .lookupActivityInstance(exchange);
            for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
            {
               ActivityInstance activityInstance = i.next();
               ApplicationContext ctx = BpmTypeConverter
                     .lookupApplicationContext(activityInstance);
               List<DataMapping> inDataMappings = ctx.getAllInDataMappings();
               inDataMappings.get(0).getApplicationAccessPoint().getId();
               for (DataMapping dataMapping : inDataMappings)
               {
                  if (dataMapping.getMappedType().getName()
                        .equalsIgnoreCase(Document.class.getName()))
                  {
                     inputDocumentTemplateAccessPointId = dataMapping
                           .getApplicationAccessPoint().getId();
                     break;
                  }
               }
            }
            document = (Document) exchange.getIn().getHeader(
                  inputDocumentTemplateAccessPointId);
         }
         if (document != null)
         {
            byte[] content = dms.retrieveDocumentContent(document.getId());
            exchange.getIn().setHeader(DOCUMENT_CONTENT, content);
            exchange.getIn().removeHeader(TARGET_PATH);
            exchange.getIn().removeHeader(inputDocumentTemplateAccessPointId);
         }
      }
   }

   private static ServiceFactory getServiceFactory()
   {
      ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
      if (sf == null)
      {
         sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
      }
      return sf;
   }
   
   private static DocumentManagementService getDocumentManagementService()
   {
      return getServiceFactory().getDocumentManagementService();
   }

   /**
    * processes the template configuration provided in EA. the class is then propagated to
    * direct://default_Classpath_Handler_Route route to generate the file and attach it to
    * the exchange.
    *
    *
    * @param exchange
    */
   @SuppressWarnings("unchecked")
   public void processTemplateConfigurations(Exchange exchange)
   {
      ModelCamelContext camelContext = (ModelCamelContext) exchange.getContext();
      ProducerTemplate producer = camelContext.createProducerTemplate();
      String templateConfigurationsEA;
      Map<String, Object> dynamicTemplateConfigurations = null;
      Map<String, Object> documentRequest = null;
      List<TemplateConfiguration> templateConfigurations = null;
      List<ActivityInstance> instances = BpmTypeConverter
            .lookupActivityInstance(exchange);
      for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
      {
         ActivityInstance activityInstance = i.next();
         Map<String, Object> extendedAttributes = activityInstance.getActivity()
               .getApplication().getAllAttributes();
         if (extendedAttributes != null && extendedAttributes.size() > 0)
         {
            dynamicTemplateConfigurations = exchange.getIn().getHeader(MAIL_ATTACHMENTS_AP_ID, Map.class);
            documentRequest = exchange.getIn().getHeader(DOCUMENT_REQUEST_AP_ID, Map.class);
            if(dynamicTemplateConfigurations != null)
            {
               if(dynamicTemplateConfigurations.size() == 1)
               {
                  templateConfigurations =toTemplateConfigurations((List<Map<String,Object>>)(dynamicTemplateConfigurations.values().iterator().next()));
                  processTemplateConfigurations(exchange, camelContext, producer, templateConfigurations);
               }
            } else if(documentRequest != null)
            {
               templateConfigurations = toTemplateConfigurations(documentRequest);
               processTemplateConfigurations(exchange, camelContext, producer, templateConfigurations);
               processRepositoryDocumentForDocumentRequest(exchange, camelContext, producer, documentRequest);
               // other IN document will be ignored
               removeDocumentsFromExchangeHeader(exchange);
               
            } else
            {
               templateConfigurationsEA = (String) extendedAttributes
               .get(MAIL_TEMPLATE_CONFIGURATION_ATT);
               if (StringUtils.isNotEmpty(templateConfigurationsEA))
               {
                  Gson gson = new Gson();
                  Type token = new TypeToken<List<TemplateConfiguration>>()
                  {
                  }.getType();
                  templateConfigurations = gson.fromJson(templateConfigurationsEA, token);
                  processTemplateConfigurations(exchange, camelContext, producer, templateConfigurations);
               }
            }
            
         }
      }
   }

   private static void processTemplateConfigurations(Exchange exchange,
         ModelCamelContext camelContext, ProducerTemplate producer,
         List<TemplateConfiguration> templateConfigurations)
   {
      for (TemplateConfiguration template : templateConfigurations)
      {
          // only process document template
         if(template.isTemplate())
         {
            Exchange newExchange = new DefaultExchange(camelContext);
            CamelMessage camelMessage = new CamelMessage();
            camelMessage.copyFrom(exchange.getIn());
            newExchange.setIn(camelMessage);
            newExchange.getIn().setHeader("CamelTemplatingLocation", template.getSource());
            newExchange
                  .getIn()
                  .setHeader(
                        "CamelTemplatingFormat",
                        (template != null && StringUtils.isNotEmpty(template.getPath()) && template
                              .getPath().endsWith(".docx")) ? "docx" : "text");
            newExchange.getIn().setHeader("CamelTemplatingTemplate", (template.getPath()));
            newExchange.getIn().setHeader("CamelTemplatingOutputName", template.getName());
            newExchange
                  .getIn()
                  .setHeader(
                        "CamelTemplatingConvertToPdf",
                        (template != null && StringUtils.isNotEmpty(template.getFormat()) && template
                              .getFormat().equalsIgnoreCase("pdf")) ? true : false);
            Exchange reponse = null;
            if (template.getSource().equalsIgnoreCase("repository"))
            {
               reponse = producer.send("direct://templateFromRepository", newExchange);
               exchange.getIn().setAttachments(reponse.getIn().getAttachments());
            }
            else if (template.getSource().equalsIgnoreCase("classpath"))
            {
               reponse = producer.send("direct://templateFromClasspath", newExchange);
               exchange.getIn().setAttachments(reponse.getIn().getAttachments());
               
            } else if(template.getSource().equalsIgnoreCase("data"))
            {
               
               DocumentManagementService dms = getDocumentManagementService();
               DmsDocumentBean document = (DmsDocumentBean) exchange.getIn().getHeader(template.getName());
               if(document != null)
               {
                  //override CamelTemplatingFormat using dmsDocumentBean data Type
                  if(document.getContentType().equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                  {
                     newExchange
                     .getIn()
                     .setHeader(
                           "CamelTemplatingFormat", "docx");
                  }
                  byte[] content = dms.retrieveDocumentContent(document.getId());
                  newExchange.getIn().setHeader("CamelTemplatingTemplateContent", content);
                  reponse = producer.send("direct://templateFromData", newExchange);
                  // delete processed template document from header
                  exchange.getIn().removeHeader(template.getName());
                  exchange.getIn().setAttachments(reponse.getIn().getAttachments());
               } else
               {
                  if(logger.isDebugEnabled())
                  {
                     logger.error("The provided document " + template.getName() +" is null ");
                  }
               }
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   public void storeExchangeAttachments(Exchange exchange) throws CreateDocumentException, IOException
   {
      if (exchange != null)
      {
         ServiceFactory sf = getServiceFactory();
         DocumentManagementService dms = sf.getDocumentManagementService();
         List<ActivityInstance> instances = BpmTypeConverter
               .lookupActivityInstance(exchange);
         for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
         {
            ActivityInstance activityInstance = i.next();
            ProcessInstance pi = activityInstance.getProcessInstance();
            ProcessDefinition processDefinition = findProcessDefinitionByProcessInstance(sf,pi);
            boolean processAttachmentSupport = processDefinition.getDataPath(PROCESS_ATTACHMENTS) != null ? true : false;
            List<Document> listProcessAttachments = null;
            
            if (!processAttachmentSupport)
            {
               logger.warn("Process attachments is not enabled for "+ processDefinition.getId() + " process");
               
            } else{
               listProcessAttachments = (List<Document>) sf.getWorkflowService().getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
               // initialize it if necessary
               if (null == listProcessAttachments)
               {
                  listProcessAttachments = new ArrayList<Document>();
               }
            }
            
            Document attachmentDocument = null;
            Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
            
            for (String attachmentName : attachments.keySet())
            {
               DataHandler attachment = attachments.get(attachmentName);
               if(attachment.getContent() instanceof byte[]){
                  attachmentDocument = CamelDmsUtils.storeDocument(dms, pi, (byte[]) attachment.getContent(),attachmentName, processAttachmentSupport);
               }else if (attachment.getContent() instanceof String){
                  attachmentDocument = CamelDmsUtils.storeDocument(dms, pi,  ((String)attachment.getContent()).getBytes(),attachmentName, processAttachmentSupport);
               }else{
                  // TODO
               }

               if(processAttachmentSupport && attachmentDocument != null)
               {
                  listProcessAttachments.add(attachmentDocument);
               }
            }
            
            if(processAttachmentSupport && !listProcessAttachments.isEmpty())
            {
               sf.getWorkflowService().setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS, listProcessAttachments);
            }
         }

      }
   }
   
   @SuppressWarnings("unchecked")
   private static void processRepositoryDocumentForDocumentRequest(Exchange exchange,
         ModelCamelContext camelContext, ProducerTemplate producer, Map<String, Object> documentRequest)
   {
      if (documentRequest.size() == 1)
      {
         List<Map<String, Object>> documents = (List<Map<String, Object>>) documentRequest
         .get("Documents");
         
         for (Map<String, Object> requestItem : documents)
         {
            if (TemplateConfigurationUtils.IsAttachment(requestItem)
                  && !((Boolean) requestItem.get("IsTemplate")))
            {
               String repositoryLocation = (String) requestItem.get("DocumentLocation");
               String documentRequestItemExtension = FilenameUtils.getExtension(repositoryLocation);
               if(Arrays.asList("docx","xml","csv","txt").contains(documentRequestItemExtension)
                     && (Boolean) requestItem.get("ConvertToPDF"))
               {
                  // only process txt, docx, xml, csv document: better exploit of pdf conversion functionality 
                  // provided by engine template
                  Exchange newExchange = new DefaultExchange(camelContext);
                  CamelMessage camelMessage = new CamelMessage();
                  camelMessage.copyFrom(exchange.getIn());
                  newExchange.setIn(camelMessage);
                  newExchange.getIn().setHeader("CamelTemplatingConvertToPdf",
                        (Boolean) requestItem.get("ConvertToPDF"));
                  newExchange.getIn().setHeader("CamelTemplatingOutputName", (String) requestItem.get("Name"));
                  newExchange.getIn().setBody(requestItem);
                  Exchange reponse = null;
                  reponse = producer.send("direct://templateDocumentRequest", newExchange);
                  exchange.getIn().setAttachments(reponse.getIn().getAttachments());
                  
               } else
               {
                  DocumentManagementService dms = getDocumentManagementService();
                  Document document = getDocumentUsingRepositoryLocation(dms, repositoryLocation);
                  if(document != null)
                  {
                     byte[] content = dms.retrieveDocumentContent(document.getId());
                     addDocumentToExchangeAttachment(exchange, content,
                           (String) requestItem.get("Name") + "."
                                 + documentRequestItemExtension,
                           document.getContentType());
                  }
               }
               
            }
         }
      }
   }
   
   private static void addDocumentToExchangeAttachment(Exchange exchange, byte[] content,
         String documentName, String documentType)
   {
      if (documentType.equals("text/xml") || documentType.equals("text/plain"))
      {
         documentType = "plain/text";
      }
      exchange.getIn()
            .addAttachment(documentName, new DataHandler(content, documentType));

      if (logger.isDebugEnabled())
      {
         logger.debug("Attachment " + documentName + " added.");
      }
   }
   
   private void removeDocumentsFromExchangeHeader(Exchange exchange)
   {
      Map<String, Object> headers = exchange.getIn().copy().getHeaders();
      for (Map.Entry<String, Object> entry : headers.entrySet())
      {
         Object value = entry.getValue();
         if (value instanceof DmsDocumentBean)
         {
            exchange.getIn().removeHeader(entry.getKey());
         }
      }
   }
}
