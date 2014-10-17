package org.eclipse.stardust.engine.extensions.jms.app;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerCarrier.ResponseHandlerFactory;
import org.eclipse.stardust.engine.extensions.jms.utils.JMSUtils;

/**
 * @author rsauer
 * @author Simon Nikles
 *
 */
public class BroadcastResponseHandlerFactory implements ResponseHandlerFactory
{
	private static final Logger trace = LogManager.getLogger(BroadcastResponseHandlerFactory.class);
	
	@Override
	public Action<Object> createResponseHandler(Message message)
	{
		try {
			if (message instanceof MapMessage)
			{
				Object signal = message.getObjectProperty("stardust.bpmn.signal");
				if (null != signal) {
					return new BroadcastResponseHandler(message);
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
	         trace.warn("Failed accessing properties of JMS message " + JMSUtils.messageToString(message) + "'." + e.getMessage());
		}
		return null;
	}
}
