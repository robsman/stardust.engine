package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

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
	@Handler
	public Object genericFileToDocument(Object messageContent,
			Exchange exchange) throws IOException, MessagingException{

	   return messageContent;
	}
	
}
