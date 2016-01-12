package org.eclipse.stardust.engine.extensions.camel.producer;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;

public class InOutProducer extends CamelProducer
{
   public InOutProducer(String endpointName, CamelContext camelContext)
   {
      super(endpointName, camelContext);
   }

   /**
    * Sends the body to an endpoint with the specified headers values
    *
    * @param message
    *           the payload to send
    * @param headers
    *           headers
    * @return the result if {@link ExchangePattern} is OUT capable, otherwise
    *         <tt>null</tt>
    * @throws Exception
    *            if the processing of the exchange failed
    */
   public Object sendBodyInOut(Object message, Map<String, Object> headers)
         throws Exception
   {
      return sendMessage(message, headers, ExchangePattern.InOut);
   }

   @Override
   public Object send(Object message, Map<String, Object> headers) throws Exception
   {
      return sendBodyInOut(message, headers);
   }
}
