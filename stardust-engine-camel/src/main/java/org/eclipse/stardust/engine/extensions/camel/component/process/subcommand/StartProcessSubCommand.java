package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;

public class StartProcessSubCommand extends AbstractSubCommand
{
   public StartProcessSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint,sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      // Find the process ID
      String processId = endpoint.evaluateProcessId(exchange, true);
      String modelId = endpoint.evaluateModelId(exchange, true);
      String fullyQualifiedName = null;
      if (!StringUtils.isEmpty(modelId) && !StringUtils.isEmpty(processId))
      {
         fullyQualifiedName = "{" + modelId + "}" + processId;
      }
      else
      {
         if (!StringUtils.isEmpty(processId))
         {
            fullyQualifiedName = processId;
         }
      }

      ProcessInstance pi;
      // Determine data for start process
      @SuppressWarnings("unchecked")
      Map<String, Object> data = (Map<String, Object>) endpoint.evaluateData(exchange);
      
    	// start process
      StartProcessAndAttachDocumentCommand command = new StartProcessAndAttachDocumentCommand(fullyQualifiedName, data, endpoint.isSynchronousMode(), exchange);
      pi = (ProcessInstance) getWorkflowService().execute(command);
     
      // manipulate exchange
      if (exchange.getPattern().equals(ExchangePattern.OutOnly))
         exchange.getOut().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
      else
         exchange.getIn().setHeader(PROCESS_INSTANCE_OID, pi.getOID());

   }

}
