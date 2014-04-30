package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.getServiceFactory;
import org.apache.camel.Exchange;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand;
import org.eclipse.stardust.engine.extensions.camel.component.exception.MissingEndpointException;
import org.eclipse.stardust.engine.extensions.camel.component.process.subcommand.*;

/**
 * 
 * @author JanHendrik.Scheufen
 */
public class ProcessProducer extends AbstractIppProducer
{
   static Logger LOG = LogManager.getLogger(ProcessProducer.class);

   private ProcessEndpoint endpoint;

   public ProcessProducer(ProcessEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   private AbstractSubCommand determineSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {

      if (SubCommand.Process.COMMAND_FIND.equals(endpoint.getSubCommand()))
      {
         return new FindProcessSubCommand(endpoint, sf);
      }
      // *** START PROCESS ***
      else if (SubCommand.Process.COMMAND_START.equals(endpoint.getSubCommand()))
      {
         return new StartProcessSubCommand(endpoint, sf);
      }
      // *** ATTACH DOCUMENT ***
      else if (SubCommand.Process.COMMAND_ATTACH.equals(endpoint.getSubCommand()))
      {
         return new AttachDocumentSubCommand(endpoint, sf);
      }// *** CONTINUE ***
      else if (SubCommand.Process.COMMAND_CONTINUE.equals(endpoint.getSubCommand()))
      {
         return new ContinueProcessSubCommand(endpoint, sf);
      }// *** SET PROPERTIES ***
      else if (SubCommand.Process.COMMAND_SET_PROPERTIES.equals(endpoint.getSubCommand()))
      {
         return new SetPropertiesSubCommand(endpoint, sf);
      }
      else if (SubCommand.Process.COMMAND_GET_PROPERTIES.equals(endpoint.getSubCommand()))
      {
         return new GetPropertiesSubCommand(endpoint, sf);
      }
      else if (SubCommand.Process.COMMAND_SPAWN_SUB_PROCESS.equals(endpoint.getSubCommand()))
      {
         return new SpawnSubProcessSubCommand(endpoint, sf);
      }
      return null;
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
      if (getServiceFactory(this.endpoint, exchange) == null)
      {
         throw new MissingEndpointException("Authentication Endpoint is missing. You have to specify one.");
      }
      ServiceFactory sf = getServiceFactory(this.endpoint, exchange);
      AbstractSubCommand subCommand = determineSubCommand(endpoint, sf);
      subCommand.process(exchange);
   }
}