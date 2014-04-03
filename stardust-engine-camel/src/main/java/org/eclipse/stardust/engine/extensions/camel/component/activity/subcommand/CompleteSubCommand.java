package org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.component.activity.subcommand.ActivityUtil.*;
import java.util.Collections;
import java.util.Map;
import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.app.CamelMessageHelper;
import org.eclipse.stardust.engine.extensions.camel.component.ActivityEndpoint;

public class CompleteSubCommand extends AbstractSubCommand
{

   public CompleteSubCommand(ActivityEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      // Check if any activity instances are provided in the header. These
      // take precedence
      ActivityInstances result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
      // otherwise use find logic based on exchange parameters
      if (null == result)
      {
         result = findActivities(endpoint,exchange,getQueryService());
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
            dataOutput = force ? CamelMessageHelper.getOutDataAccessPoints(exchange.getIn(), ai) : CamelMessageHelper
                  .getOutDataMappings(exchange.getIn(), ai);
         }

         // TODO introduce 'force' parameter to force completion via
         // Admin Service

         ApplicationContext context = ai.getActivity().getApplicationContext("application") != null ? ai.getActivity()
               .getApplicationContext("application") : ai.getActivity().getApplicationContext("default");

         if (matches(exchange, ai,getQueryService()))
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
