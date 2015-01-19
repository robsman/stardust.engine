package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;

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
import org.eclipse.stardust.engine.extensions.camel.component.exception.UnexpectedResultException;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;

public class FindProcessSubCommand extends AbstractSubCommand
{
   public FindProcessSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      ProcessInstances result = findProcesses(exchange, getQueryService());

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
      processResult(exchange,expectedResultSize,  result);
   }

   private ProcessInstances findProcesses(Exchange exchange, QueryService queryService)
         throws UnexpectedResultException
   {
      ProcessInstanceQuery piQuery;
      String processId = endpoint.evaluateProcessId(exchange, false);
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, false);
      List<ProcessInstanceState> piStates = endpoint.getProcessInstanceStates();
      Map<String, Serializable> dataFilters = endpoint.evaluateDataFilters(exchange,
            false);
      // possible search combinations
      // state
      // states
      // processId
      // processId, state
      // processId, states
      // ...

      // Apply states
      if (null != piStates && piStates.size() > 0)
         piQuery = ProcessInstanceQuery.findInState(piStates
               .toArray(new ProcessInstanceState[] {}));
      else
      {
         piQuery = ProcessInstanceQuery.findAll();
      }
      // apply process filter
      if (null != processInstanceOid)
      {
         piQuery.where(new ProcessInstanceFilter(processInstanceOid));
         if (StringUtils.isNotEmpty(processId))
            LOG.warn("Found a process instance OID (" + processInstanceOid
                  + ") and the search parameter " + "processId (" + processId
                  + ") will be ignored!");
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
               String structPath = key.substring(idx
                     + KeyValueList.STRUCT_PATH_DELIMITER.length());
               piQuery.where(DataFilter.isEqual(structId, structPath,
                     dataFilters.get(key)));
            }
            else
            {
               piQuery.where(DataFilter.isEqual(key, dataFilters.get(key)));
            }
         }
      }
      ProcessInstances result = queryService.getAllProcessInstances(piQuery);
      return result;
   }

   private void processResult(Exchange exchange, long expectedResultSize, Object instances) throws UnexpectedResultException
   {
      ProcessInstances result=(ProcessInstances)instances;

      if (result.size() == expectedResultSize && result.size() == 1)
      {
         LOG.info("Result size matches expected result size.");
         exchange.getIn().setHeader(PROCESS_INSTANCES, result.get(0));
         exchange.getIn().setHeader(PROCESS_INSTANCE_OID, result.get(0).getOID());
      }
      else if (result.size() == expectedResultSize)
      {
         LOG.info("Result size matches expected result size.");
         exchange.getIn().setHeader(PROCESS_INSTANCES, result);
      }
      else
      {
         if (expectedResultSize == -1)
            exchange.getIn().setHeader(PROCESS_INSTANCES, result);
         else
         {
            String error = result.size() + " process instances found - "
                  + expectedResultSize + " process instances expected.";
            LOG.error(error);
            throw new UnexpectedResultException(error);
         }
      }

   }
}
