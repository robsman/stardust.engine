package org.eclipse.stardust.engine.extensions.camel.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateUserInformations implements Processor
{
   private static final transient Logger logger = LoggerFactory.getLogger(UpdateUserInformations.class);

   @Override
   public void process(Exchange exchange) throws Exception
   {
      String person = exchange.getIn().getBody(String.class);
      logger.info("old exchange = " + person);
      person = "{\"creditCard\":{\"creditCardNumber\":411152,\"creditCardType\":\"MasterCard\"},\"lastName\":\"Last Name Updated In Addtional Bean\",\"firstName\":\"First Name Updated In Addtional Bean\"}";
      exchange.getIn().setBody(person);
   }

}
