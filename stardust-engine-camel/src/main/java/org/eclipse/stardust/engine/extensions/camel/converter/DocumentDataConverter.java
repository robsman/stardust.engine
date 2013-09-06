package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.file.GenericFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty;
import org.eclipse.stardust.engine.extensions.camel.util.DmsFileArchiver;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

@Converter
public class DocumentDataConverter implements DataConverter
{
	private static final Logger logger = LogManager.getLogger(DocumentDataConverter.class);

   private String fromEndpoint;
   private String targetType;

   /**
    * @return fromEndpoint
    */
   public String getFromEndpoint()
   {
      return fromEndpoint;
   }

   /**
    * @param fromEndpoint
    */
   public void setFromEndpoint(String fromEndpoint)
   {
      this.fromEndpoint = fromEndpoint;
   }

   /**
    * @return targetType
    */
   public String getTargetType()
   {
      return targetType;
   }

   /**
    * @param targetType
    */
   public void setTargetType(String targetType)
   {
      this.targetType = targetType;
   }

   /**
    * create a document from a generic file
    * 
    * @param file
    *           input file
    * @param exchange
    *           Camel exchange
    * @return document from the input file
    * @throws IOException
    * @throws MessagingException 
    */
   @Converter
   @Handler
   public Document genericFileToDocument(Object messageContent, Exchange exchange) throws IOException
   {
      String jcrDocumentContent = null;
      if (exchange != null)
      {
         if (messageContent instanceof GenericFile< ? >)
         {
            ((GenericFile) messageContent).getBinding().loadContent(exchange, ((GenericFile) messageContent));
            jcrDocumentContent = exchange.getContext().getTypeConverter()
                  .convertTo(String.class, exchange, ((GenericFile) messageContent).getBody());
         }
         else if (messageContent instanceof String)
         {
            jcrDocumentContent = (String) messageContent;
         }
         DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(ClientEnvironment.getCurrentServiceFactory());
         dmsFileArchiver.setRootFolderPath("/");
         String folder = "documents";

         String path = "";
         if (exchange.getIn().getHeader(MessageProperty.PROCESS_ID, String.class) != null)
            path += exchange.getIn().getHeader(MessageProperty.PROCESS_ID, String.class) + "_";
         if (exchange.getIn().getHeader("CamelFileNameOnly") != null)
            path += exchange.getIn().getHeader("CamelFileNameOnly") + "_";
         else
         {
            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd-hhmmsss");
            if (exchange.getIn().getHeader("breadcrumbId") != null)
               path += ((String) exchange.getIn().getHeader("breadcrumbId")).replaceAll("-", "_").replaceAll(":", "_")
                     + "_"+df.format(new Date());
         }

         MimeMultipart  mimeMultipart = null;
         if (jcrDocumentContent == null){
        	 mimeMultipart = (MimeMultipart) exchange.getIn().getBody();
        	 if (mimeMultipart != null){
        		 try {
					jcrDocumentContent = (String) mimeMultipart.getBodyPart(1).getContent();
				} catch (Exception e) {
					e.printStackTrace();
					jcrDocumentContent = "";
				}
        	 }
        	 else {
        		 jcrDocumentContent = "";
        	 }
         }
         logger.debug("*** jcrDocumentContent = "+jcrDocumentContent);
         Document newDocument = dmsFileArchiver.archiveFile(jcrDocumentContent.getBytes(), path, folder);
         newDocument.setProperties(null);
         
         // }
         return newDocument;

      }
      else
      {
         return null;
      }
   }
}
