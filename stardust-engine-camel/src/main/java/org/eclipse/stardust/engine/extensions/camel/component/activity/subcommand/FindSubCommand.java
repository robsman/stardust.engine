package org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCES;

import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ActivityEndpoint;
import org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand.AbstractSubCommand;

public class FindSubCommand extends AbstractSubCommand
{

   public FindSubCommand(ActivityEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      ActivityInstances result = ActivityUtil.findActivities(endpoint, exchange, getQueryService());
      exchange.getIn().setHeader(ACTIVITY_INSTANCES, result);

   }
}
