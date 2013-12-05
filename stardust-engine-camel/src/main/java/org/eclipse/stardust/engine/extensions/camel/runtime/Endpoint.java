package org.eclipse.stardust.engine.extensions.camel.runtime;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ENDPOINT_PKG;
public abstract class Endpoint {
	
	
	
	public abstract void parse();
		
	public abstract String getMessageId();
	
	public abstract String getMessageName();
	
	public String getMessageAccessPointProvider()
	{
		return ENDPOINT_PKG + "." + getMessageName(); 
	}
	
	public String getCamelContextProperty() {
		return null;
	}
	
	public String getEndpointPattern() {
		return null;
	}
}
