package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class UpdateBodyContentProcessor implements Processor
{

   @Override
   public void process(Exchange exchange) throws Exception
   {
      String data = exchange.getIn().getBody(String.class);
      if (data != null)
      {
         data += " updated in the additional bean";
      }
      exchange.getIn().setBody(data);
   }

}
