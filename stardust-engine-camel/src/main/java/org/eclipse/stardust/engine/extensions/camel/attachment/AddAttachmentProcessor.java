package org.eclipse.stardust.engine.extensions.camel.attachment;

import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.CredentialProvider;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;


/**
 * This class is used to add document attachment to the exchange
 * @author Sabri.Bousselmi
 * @deprecated will be reomved in IPP 8.2 version
 * 
 */

public class AddAttachmentProcessor implements Processor{
	
	public static final Logger logger = LogManager.getLogger(AddAttachmentProcessor.class);
	
	public void process(Exchange exchange){
		
		ServiceFactory serviceFactory 		= ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
		DocumentManagementService documentManagementService = serviceFactory.getDocumentManagementService();
		
		Map<String, Object> headers = exchange.getIn().getHeaders();
		
		for(Map.Entry<String, Object> entry: headers.entrySet()) 
		{
			Object value = entry.getValue();
			if(value instanceof DmsDocumentBean) 
			{
				DmsDocumentBean dmsDocumentBean = (DmsDocumentBean) value;
				byte [] document = documentManagementService.retrieveDocumentContent(dmsDocumentBean.getId());
				if(dmsDocumentBean.getContentType().equals("text/xml") || dmsDocumentBean.getContentType().equals("text/plain")) 
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
	
}
