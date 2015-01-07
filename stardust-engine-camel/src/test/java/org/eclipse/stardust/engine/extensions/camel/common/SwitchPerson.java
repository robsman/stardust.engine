package org.eclipse.stardust.engine.extensions.camel.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class SwitchPerson implements Processor{

	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> inBody = (Map<String, Object>) exchange.getIn().getBody();
		inBody.get("firstName");
		
		Map<String, Object> outBody = new HashMap<String, Object>();
		outBody.put("lastName", inBody.get("firstName"));
		outBody.put("firstName", inBody.get("lastName"));
		
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		exchange.getOut().setBody(outBody);
		
	}
	
	

}
