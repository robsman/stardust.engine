package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import java.util.Map;

import org.apache.camel.Exchange;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;

public class SetPropertiesSubCommand extends AbstractSubCommand
{

   public SetPropertiesSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      // Find the process instance context
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
      // which properties?
      Map<String, ? > properties = endpoint.evaluateProperties(exchange, true);
      // update
      // WorkflowService wfService =
      // ClientEnvironment.getCurrentServiceFactory().getWorkflowService();
      getWorkflowService().setOutDataPaths(processInstanceOid, properties);
   }

}
