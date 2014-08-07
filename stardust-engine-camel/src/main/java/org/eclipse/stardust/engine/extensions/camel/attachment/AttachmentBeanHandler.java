package org.eclipse.stardust.engine.extensions.camel.attachment;

import static org.apache.camel.Exchange.FILE_NAME_ONLY;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
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

public class AttachmentBeanHandler
{
   public static final Logger logger = LogManager.getLogger(AttachmentBeanHandler.class);
   
   /**
    * Add DmsDocumentBean from Header to message Attachment
    * @param exchange
    */
   public void toAttachment(Exchange exchange)
   {
      ServiceFactory sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
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
   
   /**
    * Convert message body to Document
    * @param exchange
    * @return Document
    * @throws IOException
    * @throws CreateDocumentException
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public Document toDocument(Exchange exchange) throws IOException, CreateDocumentException
   {
      Document document = null;
      if(exchange != null)
      {
         ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
         if(sf == null)
         {
            sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
         }
         DocumentManagementService dms = sf.getDocumentManagementService();
         byte[] jcrDocumentContent = null;
         String fileName = "";
         
         List<ActivityInstance> instances = BpmTypeConverter.lookupActivityInstance(exchange);
         for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
         {
            ActivityInstance activityInstance = i.next();
            ProcessInstance pi = activityInstance.getProcessInstance();
            Object messageContent = exchange.getIn().getBody();
            if(messageContent instanceof DmsDocumentBean)
            {
               DmsDocumentBean dmsDocumentBean = (DmsDocumentBean) messageContent;
               StringBuilder defaultPath = new StringBuilder(
                     org.eclipse.stardust.engine.api.runtime.DmsUtils.composeDefaultPath(
                           pi.getScopeProcessInstanceOID(), pi.getStartTime()))
                     .append("/")
                     .append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER)
                     .append("/")
                     .append(dmsDocumentBean.getName());
                     
               document = dms.getDocument(defaultPath.toString());
           
            } else if (messageContent instanceof GenericFile<?>) 
            {
               ((GenericFile) messageContent).getBinding().loadContent(exchange, ((GenericFile) messageContent));
                jcrDocumentContent = exchange
                     .getContext()
                     .getTypeConverter()
                     .convertTo(byte[].class, exchange, ((GenericFile) messageContent).getBody());
                fileName = (String) exchange.getIn().getHeader(FILE_NAME_ONLY);
                document = CamelDmsUtils.storeDocument(dms, pi, jcrDocumentContent, fileName, false);
            }
         }
      }
      
      return document;
   }
   
}
