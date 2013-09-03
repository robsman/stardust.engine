package org.eclipse.stardust.engine.extensions.camel.runtime;

import java.io.Serializable;
import java.util.Map;

import org.apache.camel.impl.MessageSupport;

public class MessageDetails<T> implements Message, Serializable{

	private static final long serialVersionUID = 7576439514311430378L;
	
	private MessageSupport delegate;
	
	public MessageDetails(MessageSupport message)
	{
		this.delegate = message;
	}
	
	@SuppressWarnings("unchecked")
	public T getBody() {
		Serializable delegate = (Serializable) ProxyMessage.newInstance(this.delegate);
		return (T) delegate;
		//return (T)this.delegate.getBody();
	}
	
	public Map<String, Object> getHeaders() {
		return this.delegate.getHeaders();
	}
	
	@SuppressWarnings("rawtypes")
	public Map getBodyAsMap() {
		return this.delegate.getBody(Map.class);
	}

}
