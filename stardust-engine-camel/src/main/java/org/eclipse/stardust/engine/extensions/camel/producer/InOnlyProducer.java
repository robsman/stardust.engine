package org.eclipse.stardust.engine.extensions.camel.producer;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class InOnlyProducer extends CamelProducer
{
   protected static final Logger logger = LogManager.getLogger(InOnlyProducer.class
         .getCanonicalName());

   public InOnlyProducer(String endpointName, CamelContext camelContext)
   {
      super(endpointName, camelContext);
   }

   /**
    * send message to an endpointName
    *
    * @param message
    *           the payload
    * @throws Exception
    *            if the processing of the exchange failed
    *
    */
   public Object executeMessage(Object message, Map<String, Object> headers)
         throws Exception
   {
      return sendMessage(message, headers, ExchangePattern.InOnly);
   }

   @Override
   public Object send(Object message, Map<String, Object> headers) throws Exception
   {
      return executeMessage(message, headers);

   }
}
