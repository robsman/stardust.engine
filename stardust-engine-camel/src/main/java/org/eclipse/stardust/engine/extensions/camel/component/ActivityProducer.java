package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_COMPLETE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_FIND;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.getServiceFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.camel.Exchange;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.app.CamelMessageHelper;
import org.eclipse.stardust.engine.extensions.camel.component.exception.MissingEndpointException;
import org.eclipse.stardust.engine.extensions.camel.component.exception.UnexpectedResultException;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;

public class ActivityProducer extends AbstractIppProducer
{
   private static Logger LOG = LogManager.getLogger(ActivityProducer.class);

   private ActivityEndpoint endpoint;

   public ActivityProducer(ActivityEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   /**
    * Processes the message exchange
    * 
    * @param exchange
    *           the message exchange
    * @throws Exception
    *            if an internal processing error has occurred.
    */
   @SuppressWarnings("unchecked")
   public void process(Exchange exchange) throws Exception
   {
      ServiceFactory sf = getServiceFactory(this.endpoint, exchange);

      // *** FIND ACTIVITIES ***
      if (COMMAND_FIND.equals(endpoint.getSubCommand()))
      {
         ActivityInstances result = findActivities(exchange, sf);
         exchange.getIn().setHeader(ACTIVITY_INSTANCES, result);
      }
      // *** COMPLETE ACTIVITIES ***
      else if (COMMAND_COMPLETE.equals(endpoint.getSubCommand()))
      {
         // Check if any activity instances are provided in the header. These
         // take precedence
         ActivityInstances result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
         // otherwise use find logic based on exchange parameters
         if (null == result)
         {
            result = findActivities(exchange, sf);
         }
         // Determine dataOutput
         Map<String, ? > dataOutput = endpoint.evaluateDataOutput(exchange);
         if (null == dataOutput)
         {
            dataOutput = Collections.EMPTY_MAP;// CamelConstants.EMPTY_MAP;
         }

         WorkflowService wf = sf.getWorkflowService();
         for (ActivityInstance ai : result)
         {

            boolean force = false; // TODO
            if (dataOutput.isEmpty())
            {
               dataOutput = force
                     ? CamelMessageHelper.getOutDataAccessPoints(exchange.getIn(), ai)
                     : CamelMessageHelper.getOutDataMappings(exchange.getIn(), ai);
            }

            // TODO introduce 'force' parameter to force completion via
            // Admin Service

            ApplicationContext context = ai.getActivity().getApplicationContext("application") != null ? ai
                  .getActivity().getApplicationContext("application") : ai.getActivity().getApplicationContext(
                  "default");

            if (matches(exchange, ai))
            {
               LOG.info("Process completion of activity instance with OID " + ai.getOID() + ".");
               if (context == null && dataOutput != null && !dataOutput.isEmpty())
                  wf.activateAndComplete(ai.getOID(), null, dataOutput);
               else if (context == null && dataOutput.isEmpty())
                  wf.activateAndComplete(ai.getOID(), null, null);
               else
                  wf.activateAndComplete(ai.getOID(), context.getId(), dataOutput);

            }
            else
            {
               LOG.info("Skip completion of activity instance with OID " + ai.getOID() + ".");
            }
         }

         // store the activities that were processed in the exchange as the
         // context for
         // further operations
         exchange.getIn().setHeader(ACTIVITY_INSTANCES, result);
      }
   }

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
   private ActivityInstances findActivities(Exchange exchange, ServiceFactory sf) throws UnexpectedResultException,
         MissingEndpointException
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

      if (sf == null)
      {
         throw new MissingEndpointException("Authentication endpoint is missing.");
      }

      ActivityInstances result = sf.getQueryService().getAllActivityInstances(aiQuery);

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

   private boolean matches(Exchange exchange, ActivityInstance activityInstance)
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

            DeployedModel model = ClientEnvironment.getCurrentServiceFactory().getQueryService()
                  .getModel(application.getModelOID());

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
