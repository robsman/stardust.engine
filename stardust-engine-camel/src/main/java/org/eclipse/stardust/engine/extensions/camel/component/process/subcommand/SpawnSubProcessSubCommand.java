package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;

public class SpawnSubProcessSubCommand extends AbstractSubCommand
{

   public SpawnSubProcessSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      String processId = endpoint.evaluateProcessId(exchange, true);
      String modelId = endpoint.evaluateModelId(exchange, false);
      Long refPiOid = endpoint.evaluateParentProcessInstanceOid(exchange, false);
      if (refPiOid == null)
      {

         refPiOid = endpoint.evaluateProcessInstanceOid(exchange, false);
         if (refPiOid == null)
            throw new IllegalStateException("Missing required parent OID.");
      }
      String fullyQualifiedName = processId;
      if (StringUtils.isNotEmpty(modelId) && StringUtils.isNotEmpty(processId))
      {
         fullyQualifiedName = "{" + modelId + "}" + processId;
      }

      Boolean copyData = endpoint.evaluateCopyData(exchange, false);
      if (copyData == null)
         copyData = true;
      // Determine data for start process
      Map<String, Object> data = null;
      if (!copyData)
      {
         data = (Map<String, Object>) endpoint.evaluateData(exchange);
      }
      ProcessInstance pi = getWorkflowService().spawnSubprocessInstance(refPiOid, fullyQualifiedName, copyData, data);

      // manipulate exchange
      if (exchange.getPattern().equals(ExchangePattern.OutOnly))
         exchange.getOut().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
      else
         exchange.getIn().setHeader(PROCESS_INSTANCE_OID, pi.getOID());

   }

}
