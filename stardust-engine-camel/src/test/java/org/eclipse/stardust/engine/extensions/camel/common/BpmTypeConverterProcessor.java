package org.eclipse.stardust.engine.extensions.camel.common;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BpmTypeConverterProcessor implements Processor
{

   public void process(Exchange exchange) throws Exception
   {
      Object inBody = exchange.getIn().getBody();
      exchange.getOut().setBody(inBody);

      Map<String, Object> headers = exchange.getIn().getHeaders();
      exchange.getOut().setHeaders(headers);
   }
}
