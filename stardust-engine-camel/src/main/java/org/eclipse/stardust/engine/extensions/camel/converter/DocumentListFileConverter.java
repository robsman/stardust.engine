package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.file.GenericFile;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.DmsFileArchiver;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty;

@Converter
public class DocumentListFileConverter implements DataConverter {

   private String fromEndpoint;
   private String targetType;
   private String batchSize;

	/**
	 * @return fromEndpoint
	 */
	public String getFromEndpoint() {
      return fromEndpoint;
   }

	/**
	 * @param fromEndpoint the from Endpoint
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
	 * @param targetType the target type
	 */
	public void setTargetType(String targetType) {
      this.targetType = targetType;
   }

	/**
	 * @return batchSize
	 */
	public String getBatchSize() {
      return batchSize;
   }

	/**
	 * @param batchSize the batch size
	 */
	public void setBatchSize(String batchSize) {
      this.batchSize = batchSize;
   }

 /**
 * convert file to Document: archiving files into a JCR repository 
 * 
 * @param file
 * @param exchange
 * @return List of Document
 * @throws IOException
 */
@Converter
   @Handler
	public List<Document> genericFileToDocument(GenericFile<?> file,
			Exchange exchange) throws IOException {
		DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(
				ClientEnvironment.getCurrentServiceFactory());
		if (exchange != null) {
         file.getBinding().loadContent(exchange, file);
			String jcrDocumentContent = exchange.getContext()
					.getTypeConverter()
               .convertTo(String.class, exchange, file.getBody());
         dmsFileArchiver.setRootFolderPath("/");
         String documents = "documents";
			String path = exchange.getIn().getHeader(
					MessageProperty.PROCESS_ID, String.class);
			path += "_"
					+ exchange.getIn().getHeader("CamelFileNameOnly")
					+ "_"
					+ new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss")
							.format(new Date());
			Document newDocument = dmsFileArchiver.archiveFile(
					jcrDocumentContent.getBytes(), path, documents);
         return Arrays.asList(newDocument);
		} else {
         return null;
      }
   }
}
