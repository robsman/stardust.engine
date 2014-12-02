package org.eclipse.stardust.engine.extensions.camel.intercept;

import org.apache.camel.*;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;

import org.eclipse.stardust.engine.extensions.camel.CamelMessage;

public class MessageInterceptor implements InterceptStrategy
{
   @Override
   public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition,
         Processor target, Processor nextTarget)
         throws Exception
   {

      return new DelegateAsyncProcessor(target){
         @Override
         public boolean process(Exchange exchange,    AsyncCallback callback){
            if(exchange.getIn()!=null && exchange.getIn() instanceof Message && !(exchange.getIn() instanceof CamelMessage)) {
               CamelMessage inMessage=new CamelMessage();
               inMessage.copyFrom(exchange.getIn());
               exchange.setIn(inMessage);
            }
            return super.process(exchange, callback);
         }
       };
   }
}
