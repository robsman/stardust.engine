package org.eclipse.stardust.engine.extensions.camel.producer;

import java.util.Collections;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.eclipse.stardust.engine.extensions.camel.CamelMessage;

public abstract class CamelProducer implements ProducerService
{
   public static final String METHOD="send(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>) throws java.lang.Exception";
   protected ProducerTemplate template;

   protected CamelContext camelContext;

   protected String endpointName;

   protected CamelProducer(String endpointName, CamelContext camelContext)
   {
      this.camelContext = camelContext;
      this.template = camelContext.createProducerTemplate();
      this.endpointName = endpointName;
   }

   /**
    * send message to an endpointName
    *
    * @param message
    *           the payload
    * @throws Exception
    *            if the processing of the exchange failed
    * @deprecated
    */
   public void executeMessage(Object message) throws Exception
   {
      template.sendBody(this.endpointName, message);
   }

   protected Object sendMessage(Object message, Map<String, Object> headers,
         ExchangePattern pattern)
   {
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setPattern(pattern);
      CamelMessage inMessage = new CamelMessage();
      inMessage.setBody(message);
      inMessage.setHeaders(headers == null
            ? Collections.<String, Object> emptyMap()
            : headers);
      exchange.setIn(inMessage);
      return template.send(endpointName, exchange);
   }
}
