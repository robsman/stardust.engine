package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_COMPLETE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_FIND;
import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.getServiceFactory;
import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand.CompleteSubCommand;
import org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand.FindSubCommand;
import org.eclipse.stardust.engine.extensions.camel.component.exception.MissingEndpointException;

public class ActivityProducer extends AbstractIppProducer
{
   private ActivityEndpoint endpoint;

   public ActivityProducer(ActivityEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   /**
    * Processes the message exchange
    * 
    * @param exchange
    *           the message exchange
    * @throws Exception
    *            if an internal processing error has occurred.
    */
   public void process(Exchange exchange) throws Exception
   {
      ServiceFactory sf = getServiceFactory(this.endpoint, exchange);

      if (sf == null)
      {
         throw new MissingEndpointException("Authentication endpoint is missing.");
      }

      // *** FIND ACTIVITIES ***
      if (COMMAND_FIND.equals(endpoint.getSubCommand()))
      {
         FindSubCommand subCommand = new FindSubCommand(endpoint, sf);
         subCommand.process(exchange);
      }
      // *** COMPLETE ACTIVITIES ***
      else if (COMMAND_COMPLETE.equals(endpoint.getSubCommand()))
      {
         CompleteSubCommand subCommand = new CompleteSubCommand(endpoint, sf);
         subCommand.process(exchange);
      }
   }

}
