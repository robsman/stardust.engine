package org.eclipse.stardust.engine.extensions.camel.component;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.IPP_ENDPOINT_PROPERTIES;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.language.bean.BeanLanguage;
import org.apache.camel.util.CamelContextHelper;
import static org.eclipse.stardust.engine.extensions.camel.Util.copyInToOut;
public class DataEndpointProducer extends AbstractIppProducer
{

   public DataEndpointProducer(AbstractIppEndpoint endpoint)
   {
      super(endpoint);
   }

   @Override
   public void process(Exchange exchange) throws Exception
   {

	  Map<String, Object> parameters = ((DataEndpointConfiguration) ((DataEndpoint) this.getEndpoint())
			 .getEndpointConfiguration()).getParams();
	  exchange.setProperty(IPP_ENDPOINT_PROPERTIES, parameters);
	  String methodName = ((DataEndpointConfiguration) ((DataEndpoint) this.getEndpoint()).getEndpointConfiguration())
            .getSubCommand();
      Expression exp = BeanLanguage.bean(
            CamelContextHelper.lookup(this.getEndpoint().getCamelContext(), "bpmTypeConverter"), methodName);
      Object response = exp.evaluate(exchange, Object.class);
      copyInToOut(exchange,response);
   }
}
