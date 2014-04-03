package org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.getRouteId;

import java.io.Serializable;
import java.util.Map;

import org.apache.camel.Exchange;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.component.ActivityEndpoint;
import org.eclipse.stardust.engine.extensions.camel.component.exception.MissingEndpointException;
import org.eclipse.stardust.engine.extensions.camel.component.exception.UnexpectedResultException;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;

public class ActivityUtil
{
   private static Logger LOG = LogManager.getLogger(ActivityUtil.class);

   /**
    *
    * create and execute an activity instance query
    *
    * @param exchange
    * @param sf
    *           the service factory
    * @return ActivityInstances an instance of ActivityInstances class
    * @throws UnexpectedResultException
    * @throws MissingEndpointException
    */
   public static ActivityInstances findActivities(ActivityEndpoint endpoint, Exchange exchange,
         QueryService queryService) throws UnexpectedResultException, MissingEndpointException
   {
      ActivityInstanceQuery aiQuery;

      // Look for search parameters
      String activityId = endpoint.evaluateActivityId(exchange, false);
      String processId = endpoint.evaluateProcessId(exchange, false);
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, false);
      Long activityInstanceOid = endpoint.evaluateActivityInstanceOid(exchange, false);
      ActivityInstanceState[] aiStates = endpoint.getActivityInstanceStates();
      Map<String, Serializable> dataFilters = endpoint.evaluateDataFilters(exchange, false);

      // possible search combinations
      // activityId
      // activityId, processId
      // activityId, state
      // activityId, states
      // activityId, processId, state
      // activityId, processId, states
      // processId
      // processId, state
      // processId, states
      // ...

      // Apply states
      if (null != aiStates && aiStates.length > 0)
         aiQuery = ActivityInstanceQuery.findInState(aiStates);
      else
      {
         aiQuery = ActivityInstanceQuery.findAll();
      }
      // apply process filter
      if (null != processInstanceOid)
      {
         aiQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(processInstanceOid));
         if (StringUtils.isNotEmpty(processId))
            LOG.warn("Found a process instance OID (" + processInstanceOid + ") and the search parameter "
                  + "processId (" + processId + ") will be ignored!");
      }
      else if (StringUtils.isNotEmpty(processId))
      {
         if (StringUtils.isNotEmpty(activityId))
         {
            aiQuery.where(ActivityFilter.forProcess(activityId, processId));
         }
         else
         {
            aiQuery.where(new ProcessDefinitionFilter(processId));
         }
      }
      // apply activity filter
      if (null != activityInstanceOid)
      {
         aiQuery.where(new ActivityInstanceFilter(activityInstanceOid));
         if (StringUtils.isNotEmpty(activityId))
            LOG.warn("Found an activity instance OID (" + activityInstanceOid + ") and the search parameter "
                  + "activityId (" + activityId + ") will be ignored!");
      }
      else if (StringUtils.isNotEmpty(activityId))
      {
         if (StringUtils.isEmpty(processId))
         {
            aiQuery.where(ActivityFilter.forAnyProcess(activityId));
         }
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
               aiQuery.where(DataFilter.isEqual(structId, structPath, dataFilters.get(key)));
            }
            else
            {
               aiQuery.where(DataFilter.isEqual(key, dataFilters.get(key)));
            }
         }
      }

      ActivityInstances result = queryService.getAllActivityInstances(aiQuery);

      Long expectedResultSize = endpoint.evaluateExpectedResultSize(exchange, false);

      LOG.info("Expected result size is evaluated to " + expectedResultSize + ".");
      long defaultExpectedResultSize = -1;// unlimitedSize

      if (expectedResultSize == null)
      {
         LOG.info("Expected result size is set to unlimitted.");
         expectedResultSize = defaultExpectedResultSize;
      }

      if (result.size() == expectedResultSize)
      {
         LOG.info("Result size matches expected result size.");
         return result;
      }
      else
      {
         if (expectedResultSize == -1)
            return result;
         else
         {
            String error = result.size() + " activity instances found - " + expectedResultSize
                  + " activity instances expected.";

            LOG.error(error);
            throw new UnexpectedResultException(error);
         }
      }
   }

   public static boolean matches(Exchange exchange, ActivityInstance activityInstance, QueryService queryService)
   {
      boolean matches = true;

      Object origin = exchange.getIn().getHeader(CamelConstants.MessageProperty.ORIGIN);
      Object routeId = exchange.getIn().getHeader(CamelConstants.MessageProperty.ROUTE_ID);

      if (CamelConstants.OriginValue.APPLICATION_CONSUMER.equals(origin) && routeId != null)
      {
         LOG.info("Evaluate activity instance with OID " + activityInstance.getOID()
               + " against message for application consumer with route ID " + routeId + ".");

         if (activityInstance.getActivity().getApplication() != null)
         {
            Application application = activityInstance.getActivity().getApplication();

            DeployedModel model = queryService.getModel(application.getModelOID(), false);

            String id = getRouteId(application.getPartitionId(), model.getId(), null, application.getId(), false);

            if (id.equals(routeId))
            {
               matches = true;
               LOG.info("Activity intsance with OID " + activityInstance.getOID() + " matches.");
            }
            else
            {
               matches = false;
               LOG.info("Activity intsance with OID " + activityInstance.getOID() + " does not match.");

               // if (LOG.isDebugEnabled())
               // {
               LOG.info("Expected: " + routeId);
               LOG.info("Actual  : " + id);
               // }
            }
         }
         else
         {
            matches = false;
            LOG.info("Activity intsance with OID " + activityInstance.getOID() + " does not match.");
         }
      }

      return matches;

   }
}
