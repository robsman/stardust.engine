package org.eclipse.stardust.engine.extensions.camel.converter;

import static org.apache.camel.Exchange.FILE_NAME_ONLY;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_DOCUMENT_CONTENT_KEY;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_DOCUMENT_NAME_KEY;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.mail.MailEndpoint;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;



@Converter
public class DocumentDataConverter implements DataConverter {
   static Logger logger = LogManager.getLogger(DocumentDataConverter.class);
	DmsUtils dmsUtils;
	private String fromEndpoint;
	private String targetType;

	/**
	 * @return fromEndpoint
	 */
	public String getFromEndpoint() {
		return fromEndpoint;
	}

	/**
	 * @param fromEndpoint
	 */
	public void setFromEndpoint(String fromEndpoint) {
		this.fromEndpoint = fromEndpoint;
	}

	/**
	 * @return targetType
	 */
	public String getTargetType() {
		return targetType;
	}

	/**
	 * @param targetType
	 */
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	/**
	 * create a document from a generic file
	 * 
	 * @param file
	 *            input file
	 * @param exchange
	 *            Camel exchange
	 * @return document from the input file
	 * @throws IOException
	 * @throws MessagingException
	 */
	@SuppressWarnings("unchecked")
	@Handler
	public Object genericFileToDocument(Object messageContent,
			Exchange exchange) throws IOException, MessagingException{
		/*byte[] jcrDocumentContent = null;

		if (exchange != null) {
			if (messageContent instanceof GenericFile<?>) {
				((GenericFile) messageContent).getBinding().loadContent(
						exchange, ((GenericFile) messageContent));
				 jcrDocumentContent = exchange
						.getContext()
						.getTypeConverter()
						.convertTo(byte[].class, exchange,
						//.convertTo(String.class, exchange,
								((GenericFile) messageContent).getBody());
				 if (logger.isDebugEnabled())
		         {
					 logger.debug("*** FileLength = " +((GenericFile) messageContent).getFileLength());
		         }
			} else if (messageContent instanceof String) {
				jcrDocumentContent =  ((String) messageContent).getBytes();
			} else if(messageContent instanceof MimeMultipart) {
				MimeMultipart mimeMultipart = (MimeMultipart)messageContent;
				jcrDocumentContent = MimeMultipartTypeConverter.mimeMultipartToString(mimeMultipart).getBytes();
			}

			// check the invoked Endpoint
			String fileName = "";
			if (exchange.getFromEndpoint() instanceof FileEndpoint) {
				fileName = (String) exchange.getIn().getHeader(FILE_NAME_ONLY);
			} else if (exchange.getFromEndpoint() instanceof MailEndpoint) {
				String senderAddress = (String) exchange.getIn().getHeader("From");
				int begintIndex = senderAddress.indexOf("<");
				int endIndex = senderAddress.indexOf("@");
				String senderName = senderAddress.substring(begintIndex + 1, endIndex);
				fileName = exchange.getExchangeId() + "-" + senderName + "-mailDoc.txt";
			} else if (exchange.getFromEndpoint() instanceof JmsEndpoint) {
				fileName = exchange.getExchangeId() + "-jmsDoc.txt";
			}
			else {
				fileName = exchange.getExchangeId() + ".txt";
			}

			exchange.getIn().getHeaders().put(CAMEL_DOCUMENT_NAME_KEY, fileName);
			exchange.getIn().getHeaders().put(CAMEL_DOCUMENT_CONTENT_KEY, jcrDocumentContent);
			String document = fileName + "_";
			exchange.getIn().setBody(document);
			return document;
			
		} else {
			return null;
		}*/
	   return messageContent;
	}
	
	private String getDocumentType(String filename){
		
		String extension = StringUtils.substringAfterLast(filename, ".");
		
		if ("pdf".equalsIgnoreCase(extension)) //PDF
			return "application/pdf";
		
		if ("tiff".equalsIgnoreCase(extension) || "tif".equalsIgnoreCase(extension)) //TIFF
			return "image/tiff";
		
		if ("xml".equalsIgnoreCase(extension) || "xpdl".equalsIgnoreCase(extension)) //XML
			return "text/xml";
		
		if ("xhtml".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) //XHTML
			return "text/xhtml";
		
		if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension))//HTML
			return "text/html";
		
		if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) //JPG
			return "image/jpeg";
		
		if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) //PJPG
			return "image/jpeg";

		if ("jpg".equalsIgnoreCase(extension))//XPNG
			return "image/x-png";		

		if ("gif".equalsIgnoreCase(extension))//GIF
			return "image/gif";	
		
		if ("rtf".equalsIgnoreCase(extension))//RTF
			return "text/rtf";
		
		if ("doc".equalsIgnoreCase(extension))//DOC
			return "application/msword";
		
		if ("mov".equalsIgnoreCase(extension))//MOV
			return "video/quicktime";
		
		if ("wmf".equalsIgnoreCase(extension))//WMF
			return "video/x-ms-wmv";
		
		if ("avi".equalsIgnoreCase(extension))//AVI
			return "video/x-msvideo";
		
		if ("swf".equalsIgnoreCase(extension))//SWF
			return "application/x-shockwave-flash";
		
		if ("wma".equalsIgnoreCase(extension))//WMA
			return "audio/x-ms-wma";
		
		if ("mp3".equalsIgnoreCase(extension))//MP3
			return "audio/mpeg";
		
		if ("zip".equalsIgnoreCase(extension))//ZIP
			return "application/zip";
		
		if ("txt".equalsIgnoreCase(extension))//TXT
			return "text/plain";
		
		if ("zip".equalsIgnoreCase(extension))//ZIP
			return "application/zip";
		
		if ("ppt".equalsIgnoreCase(extension))//PPT
			return "application/vnd.ms-powerpoint";
		
		if ("xls".equalsIgnoreCase(extension))//XLS
			return "application/vnd.ms-excel";
		
		if ("png".equalsIgnoreCase(extension))//PNG
			return "image/png";
		
		if ("".equalsIgnoreCase(extension))//DEFAULT
			return "application/octet-stream";
		
		if ("docx".equalsIgnoreCase(extension))//MS2007_DOC
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		
		if ("xlsx".equalsIgnoreCase(extension))//MS2007_XLS
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		
		if ("pptx".equalsIgnoreCase(extension))//MS2007_PPT
			return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
		
		if ("rptdesign".equalsIgnoreCase(extension))//RPT_DESIGN
			return "application/rptdesign";
		
		if ("css".equalsIgnoreCase(extension))//CSS
			return "text/css";
		
		if ("mp3".equalsIgnoreCase(extension))//X_MPEG
			return "audio/x-mpeg";
		
		if ("csv".equalsIgnoreCase(extension))//CSV
			return "text/csv";
		
		return "text/plain";
		
	}

}
