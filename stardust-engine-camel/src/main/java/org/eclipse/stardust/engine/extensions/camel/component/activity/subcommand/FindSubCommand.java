package org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCE_OID;

import org.apache.camel.Exchange;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ActivityEndpoint;
import org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand.AbstractSubCommand;
import org.eclipse.stardust.engine.extensions.camel.component.exception.UnexpectedResultException;

public class FindSubCommand extends AbstractSubCommand
{

   public FindSubCommand(ActivityEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      ActivityInstances result = ActivityUtil.findActivities(endpoint, exchange, getQueryService());
      Long expectedResultSize = endpoint.evaluateExpectedResultSize(exchange, false);

      long defaultExpectedResultSize = -1;// unlimitedSize
      if (expectedResultSize == null)
      {
         LOG.info("Expected result size is set to unlimitted.");
         expectedResultSize = defaultExpectedResultSize;
      }
      else
      {
         LOG.info("Expected result size is evaluated to " + expectedResultSize + ".");
      }
      processResult(exchange,expectedResultSize,result);

   }

   private void processResult(Exchange exchange, long expectedResultSize, Object instances) throws UnexpectedResultException{
      ActivityInstances result=(ActivityInstances)instances;

      if (result.size() == expectedResultSize && result.size() == 1)
      {
         LOG.info("Result size matches expected result size.");
         exchange.getIn().setHeader(ACTIVITY_INSTANCES, result.get(0));
         exchange.getIn().setHeader(ACTIVITY_INSTANCE_OID, result.get(0).getOID());
      }
      else if (result.size() == expectedResultSize)
      {
         LOG.info("Result size matches expected result size.");
         exchange.getIn().setHeader(ACTIVITY_INSTANCES, result);
      }
      else
      {
         if (expectedResultSize == -1)
            exchange.getIn().setHeader(ACTIVITY_INSTANCES, result);
         else
         {
            String error = result.size() + " activity instances found - "
                  + expectedResultSize + " activity instances expected.";
            LOG.error(error);
            throw new UnexpectedResultException(error);
         }
      }
   }
}
