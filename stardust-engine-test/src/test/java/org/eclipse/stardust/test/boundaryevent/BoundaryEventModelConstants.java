package org.eclipse.stardust.test.boundaryevent;

/**
 * <p>
 * This class contains constants related to the model used for tests
 * dealing with the <i>Boundary Event</i> functionality.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
/* package-private */ class BoundaryEventModelConstants
{
   /**
    * the ID of the valid model
    */
   /* package-private */ static final String MODEL_ID = "BoundaryEventModel";
   
   /**
    * the ID of the invalid model
    */
   /* package-private */ static final String INVALID_MODEL_ID = "InvalidBoundaryEventModel";
   
   
   /**
    * the ID of the process definition containing an error boundary event
    */
   /* package-private */ static final String PROCESS_ID_ERROR_EVENT = "ProcessDefinition_Error";
   
   /**
    * the ID of the process definition containing multiple error boundary events
    */
   /* package-private */ static final String PROCESS_ID_MULTIPLE_ERROR_EVENTS = "ProcessDefinition_Error_Multiple";
   
   /**
    * the ID of the process definition containing an interrupting timer event
    */
   /* package-private */ static final String PROCESS_ID_TIMER_EVENT_INTERRUPTING = "ProcessDefinition_Timer_Interrupting";
   
   /**
    * the ID of the process definition containing an non-interrupting timer event
    */
   /* package-private */ static final String PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING = "ProcessDefinition_Timer_Non_interrupting";
   
   /**
    * the ID of the process definition containing an non-interrupting timer event and an XOR split gateway on the "normal" flow
    */
   /* package-private */ static final String PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR = "ProcessDefinition_Timer_Non_interrupting_XOR";
   
   /**
    * the ID of the process definition containing an non-interrupting timer event and an AND split gateway on the "normal" flow
    */
   /* package-private */ static final String PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND = "ProcessDefinition_Timer_Non_interrupting_AND";

   
   /**
    * the ID of the application activity having the event handler attached
    */
   /* package-private */ static final String APP_ACTIVITY_ID = "AppActivity";
   
   /**
    * the ID of the initially hibernated route activity having the event handler attached
    */
   /* package-private */ static final String SLEEPING_ACTIVITY_ID = "SleepingActivity";
   
   /**
    * the ID of the activity enabled on the "normal flow"
    */
   /* package-private */ static final String NORMAL_FLOW_ACTIVITY_ID = "NormalFlow";
   
   /**
    * the ID of the activity enabled on the XOR split "normal flow"
    */
   /* package-private */ static final String ENABLED_NORMAL_FLOW_ACTIVITY_ID = "EnabledNormalFlow";
   
   /**
    * the ID of the activity disabled on the XOR split "normal flow"
    */
   /* package-private */ static final String DISABLED_NORMAL_FLOW_ACTIVITY_ID = "DisabledNormalFlow";
   
   /**
    * the ID of the first activity enabled on the AND split "normal flow"
    */
   /* package-private */ static final String FIRST_NORMAL_FLOW_ACTIVITY_ID = "FirstNormalFlow";
   
   /**
    * the ID of the second activity enabled on the AND split "normal flow"
    */
   /* package-private */ static final String SECOND_NORMAL_FLOW_ACTIVITY_ID = "SecondNormalFlow";
   
   /**
    * the ID of the activity enabled on the "exception flow"
    */
   /* package-private */ static final String EXCEPTION_FLOW_ACTIVITY_ID = "ExceptionFlow";
   
   /**
    * the ID of the activity enabled on the "exception flow 1"
    */
   /* package-private */ static final String EXCEPTION_FLOW_1_ACTIVITY_ID = "ExceptionFlow1";
   
   /**
    * the ID of the activity enabled on the "exception flow 2"
    */
   /* package-private */ static final String EXCEPTION_FLOW_2_ACTIVITY_ID = "ExceptionFlow2";
   
   /**
    * the ID of the end activity
    */
   /* package-private */ static final String END_ACTIVITY_ID = "END";
   
   
   /**
    * the ID of the flag controlling whether the process should fail or not
    */
   /* package-private */ static final String FAIL_FLAG_ID = "Fail";
   
   /**
    * the ID of the data indicating which exception should be thrown
    */
   /* package-private */ static final String EXCEPTION_DATA_ID = "Exception";
   
   /**
    * the ID of the data holding the timeout
    */
   /* package-private */ static final String TIMEOUT_DATA_ID = "Timeout";
}
