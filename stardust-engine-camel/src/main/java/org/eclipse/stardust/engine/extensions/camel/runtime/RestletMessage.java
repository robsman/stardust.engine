package org.eclipse.stardust.engine.extensions.camel.runtime;


public interface RestletMessage extends Message {
	public abstract String getContentType();
	public abstract String getCamelHttpMethod();
	public abstract String getCamelHttpQuery();
	public abstract String getCamelHttpResponseCode();
	public abstract String getCamelHttpUri();
	public abstract String getCamelRestletLogin();
	public abstract String getCamelRestletPassword();
	public abstract RestletRequest getCamelRestletRequest();
	public abstract RestletResponse getCamelRestletResponse();
}
