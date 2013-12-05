package org.eclipse.stardust.engine.extensions.camel.runtime;

public class RestletEndpoint extends Endpoint {

	@Override
	public void parse() {
	}

	@Override
	public String getMessageId() {
		return "RestletMessage";
	}

	@Override
	public String getMessageName() {
		return "RestletMessage";
	}
	
	@Override
	public String getCamelContextProperty() {
		return "restletUrlPattern";
	}

	@Override
	public String getEndpointPattern() {
		return "restlet:";
	}
}
