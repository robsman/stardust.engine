package org.eclipse.stardust.engine.extensions.camel.attachment;

import static org.apache.camel.Exchange.FILE_NAME_ONLY;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MAIL_ATTACHMENTS_AP_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MAIL_TEMPLATE_CONFIGURATION_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.DOCUMENT_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.DOCUMENT_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.TARGET_PATH;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ModelCamelContext;

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
import org.eclipse.stardust.engine.extensions.camel.app.mail.TemplateConfiguration;
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
      ServiceFactory sf = getServiceFactory();
      DocumentManagementService dms = sf.getDocumentManagementService();
      Map<String, Object> headers = exchange.getIn().getHeaders();
      for (Map.Entry<String, Object> entry : headers.entrySet())
      {
         Object value = entry.getValue();
         if (value instanceof DmsDocumentBean)
         {
            DmsDocumentBean dmsDocumentBean = (DmsDocumentBean) value;
            byte[] document = dms.retrieveDocumentContent(dmsDocumentBean.getId());
            if (dmsDocumentBean.getContentType().equals("text/xml")
                  || dmsDocumentBean.getContentType().equals("text/plain"))
            {
               exchange.getIn().addAttachment(dmsDocumentBean.getName(),
                     new DataHandler(document, "plain/text"));
            }
            else
            {
               exchange.getIn().addAttachment(dmsDocumentBean.getName(),
                     new DataHandler(document, dmsDocumentBean.getContentType()));
            }
            if (logger.isDebugEnabled())
            {
               logger.debug("Attachment " + dmsDocumentBean.getName() + " added.");
            }
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
         ByteArrayOutputStream out = exchange.getIn()
               .getBody(ByteArrayOutputStream.class);
         if (out != null)
            exchange.getIn().addAttachment(outputName + ".pdf",
                  new DataHandler(out.toByteArray(), "application/pdf"));
         else
            exchange.getIn().addAttachment(outputName + ".pdf",
                  new DataHandler(exchange.getIn().getBody(), "application/pdf"));
      }
      else
      {
         if (((String) exchange.getIn().getHeader("CamelTemplatingTemplate"))
               .endsWith(".docx"))
         {
            ByteArrayOutputStream out = exchange.getIn().getBody(
                  ByteArrayOutputStream.class);
            exchange.getIn().addAttachment(outputName + ".docx",
                  new DataHandler(out.toByteArray(), "application/msword"));
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
            ProcessDefinition processDefinition = sf.getQueryService().getProcessDefinition(pi.getProcessID());
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
         ServiceFactory sf = getServiceFactory();
         DocumentManagementService dms = sf.getDocumentManagementService();
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

   private ServiceFactory getServiceFactory()
   {
      ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
      if (sf == null)
      {
         sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
      }
      return sf;
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
            if(dynamicTemplateConfigurations != null)
            {
               if(dynamicTemplateConfigurations.size() == 1)
               {
                  List<TemplateConfiguration> listTemplateConfiguration = (List<TemplateConfiguration>) dynamicTemplateConfigurations
                        .get(dynamicTemplateConfigurations.keySet().iterator().next());
                  templateConfigurationsEA = new Gson().toJson(listTemplateConfiguration);
                  processTemplateConfigurations(exchange, camelContext, producer, templateConfigurationsEA);
               }
            } else
            {
               templateConfigurationsEA = (String) extendedAttributes
               .get(MAIL_TEMPLATE_CONFIGURATION_ATT);
               if (StringUtils.isNotEmpty(templateConfigurationsEA))
               {
                  processTemplateConfigurations(exchange, camelContext, producer, templateConfigurationsEA);
               }
            }
            
         }
      }
   }
   
   private static void processTemplateConfigurations(Exchange exchange,
         ModelCamelContext camelContext, ProducerTemplate producer,
         String templateConfigurationsEA)
   {
      Gson gson = new Gson();
      Type token = new TypeToken<List<TemplateConfiguration>>()
      {
      }.getType();
      List<TemplateConfiguration> templateConfigurations = gson.fromJson(
            templateConfigurationsEA, token);
      for (TemplateConfiguration template : templateConfigurations)
      {
         Exchange newExchange = new DefaultExchange(camelContext);
         newExchange.getIn().setHeaders(exchange.getIn().getHeaders());
         newExchange.getIn().setBody(exchange.getIn().getBody());
         newExchange.getIn().setAttachments(exchange.getIn().getAttachments());
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
            ProcessDefinition processDefinition = sf.getQueryService().getProcessDefinition(pi.getProcessID());
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
}
