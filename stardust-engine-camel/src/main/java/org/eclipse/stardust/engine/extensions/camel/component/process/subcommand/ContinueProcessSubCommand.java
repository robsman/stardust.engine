package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;

import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;
import org.eclipse.stardust.engine.extensions.camel.util.search.ActivityInstanceSearch;

public class ContinueProcessSubCommand extends AbstractSubCommand
{

   public ContinueProcessSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      // Find the process instance context
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);

      // Find waiting AIs
      // QueryService qService = getServiceFactory(this.endpoint,
      // exchange).getQueryService();
      ActivityInstances result = ActivityInstanceSearch.findWaitingForProcessInstance(getQueryService(),
            processInstanceOid);
      if (result.size() == 0)
      {
         // TODO implement a "strict" mode to signal that at least one AI
         // must have
         // been found and no result should throw an exception
         LOG.warn("No waiting activity instance found for process instance OID: " + processInstanceOid);
         return;
      }
      // Determine dataOutput
      Map<String, ? > dataOutput = endpoint.evaluateDataOutput(exchange);
      // Complete AIs
      // WorkflowService wfService =
      // ClientEnvironment.getCurrentServiceFactory().getWorkflowService();
      for (ActivityInstance ai : (List<ActivityInstance>) result)
      {
         getWorkflowService().activateAndComplete(ai.getOID(), null, dataOutput);
      }
      // TODO set a list of the activity OIDs in the message header to
      // provide context
      // ... can be reused for activity:search

   }

}
