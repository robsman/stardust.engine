package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCES;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;

public class FindProcessSubCommand extends AbstractSubCommand
{
   public FindProcessSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint,sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      ProcessInstances result = findProcesses(exchange, getQueryService());
      exchange.getIn().setHeader(PROCESS_INSTANCES, result);

   }

   private ProcessInstances findProcesses(Exchange exchange, QueryService queryService)
   {
      ProcessInstanceQuery piQuery;
      // Look for search parameters
      String processId = endpoint.evaluateProcessId(exchange, false);
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, false);
      List<ProcessInstanceState> piStates = endpoint.getProcessInstanceStates();
      Map<String, Serializable> dataFilters = endpoint.evaluateDataFilters(exchange, false);
      // possible search combinations
      // state
      // states
      // processId
      // processId, state
      // processId, states
      // ...

      // Apply states
      if (null != piStates && piStates.size() > 0)
         piQuery = ProcessInstanceQuery.findInState(piStates.toArray(new ProcessInstanceState[] {}));
      else
      {
         piQuery = ProcessInstanceQuery.findAll();
      }
      // apply process filter
      if (null != processInstanceOid)
      {
         piQuery.where(new ProcessInstanceFilter(processInstanceOid));
         if (StringUtils.isNotEmpty(processId))
            LOG.warn("Found a process instance OID (" + processInstanceOid + ") and the search parameter "
                  + "processId (" + processId + ") will be ignored!");
      }
      else if (StringUtils.isNotEmpty(processId))
      {
         piQuery.where(new ProcessDefinitionFilter(processId, false));
      }
      // apply data filters
      if (null != dataFilters)
      {
         for (String key : dataFilters.keySet())
         {
            // detect structured data filter
            int idx = key.indexOf(KeyValueList.STRUCT_PATH_DELIMITER);
            if (idx != -1)
            {
               String structId = key.substring(0, idx);
               String structPath = key.substring(idx + KeyValueList.STRUCT_PATH_DELIMITER.length());
               piQuery.where(DataFilter.isEqual(structId, structPath, dataFilters.get(key)));
            }
            else
            {
               piQuery.where(DataFilter.isEqual(key, dataFilters.get(key)));
            }
         }
      }

      ProcessInstances result = queryService.getAllProcessInstances(piQuery);
      // TODO implement result size matching logic to throw UnexpectedResultException

      return result;
   }
}
