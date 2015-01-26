package org.eclipse.stardust.engine.extensions.camel.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;

public class ProcessInstanceHandler
{

   private static Logger LOG = LogManager.getLogger(ProcessInstanceHandler.class);

   private ServiceFactory serviceFactory;

   public ProcessInstanceHandler()
   {

   }

/**
 * @param factory
 */
public ProcessInstanceHandler(ServiceFactory factory)
   {
      this.serviceFactory = factory;
   }

   /**
    * searches a process instance 
    * 
    * @param processDefinitionId
    * @param data
    * @return Process Instance
    */
   public ProcessInstance searchProcessInstance(String processDefinitionId, Map<String, Serializable> data)
   {
      if (LOG.isDebugEnabled())
      {
         StringBuilder logMsg = new StringBuilder("Searching for process instance of processDefinitionId '");
         logMsg.append(processDefinitionId);
         logMsg.append("'");
         if (null != data && data.size() > 0)
         {
            List<String> filters = new ArrayList<String>();
            for (String key : data.keySet())
               filters.add(key + "=" + data.get(key));
            logMsg.append(" using data " + StringUtils.join(filters.iterator(), ";"));
         }
         LOG.debug(logMsg.toString());
      }
      try
      {
         QueryService queryService = serviceFactory.getQueryService();
         ProcessInstanceQuery query = ProcessInstanceQuery.findAlive(processDefinitionId);
         if (null != data && data.size() > 0)
         {
            QueryUtils.addDataFilters(data, query);
         }
         ProcessInstances result = queryService.getAllProcessInstances(query);
         if (result.size() >= 1)
            return (ProcessInstance) result.get(0);
         else
            return null;
      }
      catch (ServiceNotAvailableException e)
      {
         throw e;
      }
   }

/**
 * @param serviceFactory
 */
public void setServiceFactory(ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;
   }
}

