package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.EXPECTED_RESULT_SIZE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_COMPLETE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_FIND;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.parseSimpleExpression;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class ActivityEndpoint extends AbstractIppEndpoint
{

   final static Logger LOG = LogManager.getLogger(ActivityEndpoint.class);

   protected Expression activityId;
   protected Expression activityInstanceOid;
   protected Long expectedResultSize;
   protected String state;
   protected String states;
   private ActivityInstanceState[] activityInstanceStates;

   public ActivityEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }

   /**
    * Creates a new producer which is used send messages into the endpoint
    * 
    * @return a newly created producer
    * @throws Exception
    *            can be thrown
    */
   public Producer createProducer() throws Exception
   {
      if ((COMMAND_COMPLETE.equals(this.subCommand)) && (StringUtils.isNotEmpty(dataOutput) && dataOutputMap != null))
      {
         throw new IllegalArgumentException("You cannot set both 'dataOutput' and 'dataOutputMap' options.");
      }
      if (COMMAND_FIND.equals(this.subCommand) | COMMAND_COMPLETE.equals(this.subCommand))
      {
         if (StringUtils.isNotEmpty(state) && StringUtils.isNotEmpty(states))
            throw new IllegalArgumentException("You cannot set both 'state' and 'states' options.");
         if (null != processId && null != processInstanceOid)
            LOG.warn("Seting both processId and processInstanceOid is obsolete. The OID takes precedence as a search parameter!");
      }
      return new ActivityProducer(this);
   }

   /**
    * Creates a new Event Driven Consumer which consumes messages from the endpoint using
    * the given processor
    * 
    * @param processor
    *           the given processor
    * @return a newly created consumer
    * @throws Exception
    *            can be thrown
    */
   public Consumer createConsumer(Processor processor) throws Exception
   {
      throw new UnsupportedOperationException("This endpoint cannot be used as a consumer:" + getEndpointUri());
   }

   /**
    * @param activityId
    */
   public void setActivityId(String activityId)
   {
//      if (activityId.startsWith("${") && activityId.endsWith("}"))
//         this.activityId = SimpleLanguage.simple(extractTokenFromExpression(activityId));
//      else
//         this.activityId = SimpleLanguage.simple(activityId);
      this.activityId=    parseSimpleExpression(activityId);
   }

   /**
    * @param activityInstanceOid
    */
   public void setActivityInstanceOid(String activityInstanceOid)
   {
//      if (activityInstanceOid.startsWith("${") && activityInstanceOid.endsWith("}"))
//         this.activityInstanceOid = SimpleLanguage.simple(extractTokenFromExpression(activityInstanceOid));
//      else
//         this.activityInstanceOid = SimpleLanguage.simple(activityInstanceOid);
      this.activityInstanceOid=    parseSimpleExpression(activityInstanceOid);
   }

   /**
    * @param state
    */
   public void setState(String state)
   {
      this.state = state;
      this.activityInstanceStates = new ActivityInstanceState[] {getActivityInstanceStateByName(this.state)};
   }

   /**
    * @param states
    */
   public void setStates(String states)
   {
      this.states = states;
      String[] stateArray = states.split(",");
      this.activityInstanceStates = new ActivityInstanceState[stateArray.length];
      for (int i = 0; i < stateArray.length; i++)
      {
         this.activityInstanceStates[i] = getActivityInstanceStateByName(stateArray[i]);
      }
   }

   /**
    * @return activityInstanceStates
    */
   public ActivityInstanceState[] getActivityInstanceStates()
   {
      return this.activityInstanceStates;
   }

   /**
    * Returns the value of activity Id on the given exchange and generate an exception if
    * (activityId == null)
    * 
    * @param exchange
    * @param strict
    *           flag
    * @return activity Id
    */
   public String evaluateActivityId(Exchange exchange, boolean strict)
   {
      if (null != this.activityId)
         return this.activityId.evaluate(exchange, String.class);
      else
      {
         String id = exchange.getIn().getHeader(ACTIVITY_ID, String.class);
         if (StringUtils.isEmpty(id) && strict)
         {
            throw new IllegalStateException("Missing required activity ID.");
         }
         return id;
      }
   }

   /**
    * Returns the value of activity instance Oid on the given exchange and generate an
    * exception if (activityInstanceOid == null)
    * 
    * @param exchange
    * @param strict
    *           flag
    * @return activity instance Oid
    */
   public Long evaluateActivityInstanceOid(Exchange exchange, boolean strict)
   {
      if (null != this.activityInstanceOid)
         return this.activityInstanceOid.evaluate(exchange, Long.class);
      else
      {
         Long id = exchange.getIn().getHeader(ACTIVITY_INSTANCE_OID, Long.class);
         if (null == id && strict)
         {
            throw new IllegalStateException("Missing required activity instance OID.");
         }
         return id;
      }
   }

   /**
    * Returns the ActivityInstanceState for the specified name.
    * 
    * @param state
    * @return activityInstanceState
    */
   public static ActivityInstanceState getActivityInstanceStateByName(String state)
   {
      if (ActivityInstanceState.Aborted.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Aborted;
      }
      else if (ActivityInstanceState.Aborting.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Aborting;
      }
      else if (ActivityInstanceState.Application.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Application;
      }
      else if (ActivityInstanceState.Completed.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Completed;
      }
      else if (ActivityInstanceState.Created.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Created;
      }
      else if (ActivityInstanceState.Hibernated.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Hibernated;
      }
      else if (ActivityInstanceState.Interrupted.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Interrupted;
      }
      else if (ActivityInstanceState.Suspended.getName().equalsIgnoreCase(state))
      {
         return ActivityInstanceState.Suspended;
      }
      else
      {
         LOG.warn("Unknown ActivityInstanceState specified: " + state);
         return null;
      }
   }

   /**
    * Returns the value of ExpectedResultSize on the given exchange 
    * 
    * @param exchange
    * @param strict
    *           flag
    * @return ExpectedResultSize
    */
   public Long evaluateExpectedResultSize(Exchange exchange, boolean strict)
   {
      if (null != this.expectedResultSize)
      {
         return this.expectedResultSize;
      }
      else
      {
         Long expectedResultSize = exchange.getIn().getHeader(EXPECTED_RESULT_SIZE, Long.class);
         return expectedResultSize;
      }
   }

   public Long getExpectedResultSize()
   {
      return expectedResultSize;
   }

   public void setExpectedResultSize(Long expectedResultSize)
   {
      this.expectedResultSize = expectedResultSize;
   }
}
