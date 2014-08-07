package org.eclipse.stardust.engine.extensions.camel.common;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelTestUtils {
	
	private static final transient Logger LOG = LoggerFactory.getLogger( CamelTestUtils.class );
	
	@Produce(uri = "direct:camelTestUtils")
    private static ProducerTemplate producerTemplate;

	public static Exchange invokeEndpoint( String uri, Exchange exchange, Map<String,Object> headerMap, Object body ) {
    	Message message = new DefaultMessage();
    	if( null != headerMap )
    		message.setHeaders( headerMap );
    	if( null != body )
    		message.setBody(body);
		exchange.setIn( message );
		LOG.info("Invoking endpoint URI: "+uri);
   		return producerTemplate.send(uri, exchange);
    }


}
