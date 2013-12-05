package org.eclipse.stardust.engine.extensions.camel.util.test;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AssertionFailedException;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.extensions.camel.util.ProcessInstanceHandler;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;


public class BpmAssert
{

   private static Logger LOG = LogManager.getLogger(BpmAssert.class);

   /**
    * Returns <code>true</code> if the number of alive process instances for the specified
    * process definition equals the expected number.
    *
    * @param expected
    * @param processDefinitionId
    * @return <code>true</code> if the number of alive process instances for the specified process definition equals the expected number.
    */
   public static boolean numberOfAliveProcessInstances(long expected, String processDefinitionId)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive(processDefinitionId);
      return (getProcessInstancesCount(query) == expected);
   }

   /**
    * Returns <code>true</code> if the number of alive process instances for the specified
    * process definition which possed the specified data objects and values equals the
    * expected number. The keys in the data map control whether a filter for simple data
    * or for structured data is used.<br/>
    * <br/>
    * Simple Key: "MySimpleData" Structured Key: "MyStructuredData#client.street"
    *
    * @param expected
    * @param processDefinitionId
    * @param data
    * @return <code>true</code> if the number of filtered alive process instances for the specified process equals to the expected number
    */
   public static boolean numberOfAliveProcessInstancesWithData(long expected, String processDefinitionId,
         Map<String, Serializable> data)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive(processDefinitionId);
      for (String key : data.keySet())
      {
         int index;
         if (-1 != (index = key.indexOf("#")))
         {
            String struct = key.substring(0, index);
            String path = key.substring(index + 1);
            query.where(DataFilter.isEqual(struct, path, data.get(key)));
         }
         else
         {
            query.where(DataFilter.isEqual(key, data.get(key)));
         }
      }
      return (getProcessInstancesCount(query) == expected);
   }

   /**
     * return alive process instances having data found for process definition
     *  
	 * @param processDefinitionId
	 * @param data
	 * @return alive process instances having data found for process definition
	 */
public static ProcessInstance aliveProcessExistsHavingData(String processDefinitionId, Map<String, Serializable> data)
   {
      ProcessInstanceHandler piHandler = new ProcessInstanceHandler();
      ProcessInstance pi = piHandler.searchProcessInstance(processDefinitionId, data);
      if (null == pi)
      {
         StringBuilder errorMsg = new StringBuilder("No alive process instance found for process definition '");
         errorMsg.append(processDefinitionId);
         errorMsg.append("'");
         if (null != data && data.size() > 0)
         {
            errorMsg.append(" having data ");
            List<String> filters = new ArrayList<String>();
            for (String key : data.keySet())
               filters.add(key + "=" + data.get(key));
            errorMsg.append(StringUtils.join(filters.iterator(), ";"));
         }
         throw new AssertionFailedException(errorMsg.toString());
      }
      return pi;
   }

   /**
     * returns Process Instance that have START_TIME greater or equal to parameter startTime 
     * 
	 * @param processDefinitionId
	 * @param startTime
	 * @return Process Instance that have START_TIME greater or equal to parameter startTime
	 * @throws AssertionFailedException
	 */
public static ProcessInstance processCreated(String processDefinitionId, Date startTime)
         throws AssertionFailedException
   {
      ProcessInstances result = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess(processDefinitionId);
      query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(startTime.getTime()));
      query.where(ProcessStateFilter.ALIVE);
      ServiceFactory sf = null;
      try
      {
         sf = ClientEnvironment.instance().getServiceFactory("motu", null, null, null);
         QueryService queryService = sf.getQueryService();
         result = queryService.getAllProcessInstances(query);
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      if (null != result && result.size() == 1)
         return (ProcessInstance) result.get(0);
      else
         throw new AssertionFailedException("No process instance found for process definition '" + processDefinitionId
               + "' with start time > " + startTime);
   }

   /**
     * returns <code>true</code> if activity instance with ID equal to 'activityId' is completed in process instance OID processInstanceOID 
     * 
	 * @param processInstanceOID
	 * @param activityId
	 * @return <code>true</code> if activity instance with ID equal to 'activityId' is completed in process instance OID processInstanceOID else <code>false</code>
	 */
public static boolean activityCompleted(long processInstanceOID, String activityId)
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("Verifying if activity instance with ID '" + activityId + "' is completed in process instance OID "
               + processInstanceOID);
      }
      ActivityInstances result = null;
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(processInstanceOID);
      query.where(ActivityFilter.forAnyProcess(activityId));
      query.where(ActivityStateFilter.COMPLETED);
      ServiceFactory sf = null;
      try
      {
         sf = ClientEnvironment.instance().getServiceFactory("motu", null, null, null);
         if(sf==null)
            sf=ClientEnvironment.getCurrentServiceFactory();
         QueryService queryService = sf.getQueryService();
         result = queryService.getAllActivityInstances(query);
      }
      finally
      {
         if (null != sf)
            sf.close();
      }
      return (null != result && result.size() == 1);
   }

/**
 * Counts the number of process instances satisfying the criteria specified in the provided query.
 * @param query
 * @return the number of process instances
 */
private static long getProcessInstancesCount(ProcessInstanceQuery query)
   {
      ServiceFactory sf = null;
      QueryService queryService = null;
      try
      {
         sf = ClientEnvironment.instance().getServiceFactory("motu", null, null, null);
         queryService = sf.getQueryService();
         return queryService.getProcessInstancesCount(query);
      }
      catch (ServiceNotAvailableException e)
      {
         LOG.error("Service not available during assertion");
         throw e;
      }
      finally
      {
         if (null != sf && null != queryService)
            sf.release(queryService);
      }
   }
}
