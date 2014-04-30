package org.eclipse.stardust.engine.extensions.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.language.bean.BeanLanguage;
import org.apache.camel.util.CamelContextHelper;

public class DataEndpointProducer extends AbstractIppProducer
{

   public DataEndpointProducer(AbstractIppEndpoint endpoint)
   {
      super(endpoint);
   }

   @Override
   public void process(Exchange exchange) throws Exception
   {

      String methodName = ((DataEndpointConfiguration) ((DataEndpoint) this.getEndpoint()).getEndpointConfiguration())
            .getSubCommand();
      Expression exp = BeanLanguage.bean(
            CamelContextHelper.lookup(this.getEndpoint().getCamelContext(), "bpmTypeConverter"), methodName);
      Object response = exp.evaluate(exchange, Object.class);
      exchange.getOut().setHeaders(exchange.getIn().getHeaders());
      exchange.getOut().setBody(response);
   }
}
