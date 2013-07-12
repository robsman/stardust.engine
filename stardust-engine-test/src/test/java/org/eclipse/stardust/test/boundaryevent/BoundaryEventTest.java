package org.eclipse.stardust.test.boundaryevent;

import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.APP_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.DISABLED_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.ENABLED_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_DATA_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_FLOW_1_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_FLOW_2_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.FIRST_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.FAIL_FLAG_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_ERROR_EVENT;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_MULTIPLE_ERROR_EVENTS;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_INTERRUPTING;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.SECOND_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.SLEEPING_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.TIMEOUT_DATA_ID;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO (nw) javadoc
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class BoundaryEventTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);
   
   @Test
   public void testInterruptingErrorEventOccurring() throws Exception
   {
      final Map<String, ?> failFlagData = Collections.singletonMap(FAIL_FLAG_ID, Boolean.TRUE);
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_ERROR_EVENT, failFlagData, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_ERROR_EVENT, APP_ACTIVITY_ID, ActivityInstanceState.Aborted));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_ERROR_EVENT, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_ERROR_EVENT, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
   }
   
   @Test
   public void testInterruptingErrorEventNotOccurring() throws Exception
   {
      final Map<String, ?> failFlagData = Collections.singletonMap(FAIL_FLAG_ID, Boolean.FALSE);
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_ERROR_EVENT, failFlagData, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_ERROR_EVENT, APP_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_ERROR_EVENT, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_ERROR_EVENT, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
   }
   
   @Test
   public void testMultipleInterruptingErrorEventNotOccurring() throws Exception
   {
      final Map<String, ?> exceptionData = Collections.singletonMap(EXCEPTION_DATA_ID, null);
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_MULTIPLE_ERROR_EVENTS, exceptionData, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, APP_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, EXCEPTION_FLOW_1_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, EXCEPTION_FLOW_2_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
   }
   
   @Test
   public void testMultipleInterruptingErrorEventOccurringExceptionFlow1() throws Exception
   {
      final Map<String, ?> exceptionData = Collections.singletonMap(EXCEPTION_DATA_ID, NullPointerException.class.getName());
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_MULTIPLE_ERROR_EVENTS, exceptionData, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, APP_ACTIVITY_ID, ActivityInstanceState.Aborted));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, EXCEPTION_FLOW_1_ACTIVITY_ID, ActivityInstanceState.Completed));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, EXCEPTION_FLOW_2_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
   }
   
   @Test
   public void testMultipleInterruptingErrorEventOccurringExceptionFlow2() throws Exception
   {
      final Map<String, ?> exceptionData = Collections.singletonMap(EXCEPTION_DATA_ID, IllegalArgumentException.class.getName());
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_MULTIPLE_ERROR_EVENTS, exceptionData, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, APP_ACTIVITY_ID, ActivityInstanceState.Aborted));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, EXCEPTION_FLOW_1_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_MULTIPLE_ERROR_EVENTS, EXCEPTION_FLOW_2_ACTIVITY_ID, ActivityInstanceState.Completed));
   }
   
   
   @Test
   public void testInterruptingTimerEventOccurring() throws Exception
   {
      final Map<String, ?> timeoutData = Collections.singletonMap(TIMEOUT_DATA_ID, Long.valueOf(1));
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_TIMER_EVENT_INTERRUPTING, timeoutData, true);
      doOneEventDaemonRun();
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Aborted));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
   }
   
   @Test
   public void testInterruptingTimerEventNotOccurring() throws Exception
   {
      final Map<String, ?> timeoutData = Collections.singletonMap(TIMEOUT_DATA_ID, Long.MAX_VALUE);
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_TIMER_EVENT_INTERRUPTING, timeoutData, true);
      doOneEventDaemonRun();
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Hibernated));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_INTERRUPTING, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
   }
   
   
   @Test
   public void testNonInterruptingTimerEventOccurring() throws Exception
   {
      final Map<String, ?> timeoutData = Collections.singletonMap(TIMEOUT_DATA_ID, Long.valueOf(1));
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, timeoutData, true);
      doOneEventDaemonRun();
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Hibernated));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
   }

   @Test
   public void testNonInterruptingTimerEventOccurringWithActivityHavingAndSplit() throws Exception
   {
      final Map<String, ?> timeoutData = Collections.singletonMap(TIMEOUT_DATA_ID, Long.valueOf(1));
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND, timeoutData, true);
      doOneEventDaemonRun();
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Hibernated));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND, FIRST_NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND, SECOND_NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
   }
   
   @Test
   public void testNonInterruptingTimerEventOccurringWithActivityHavingXorSplit() throws Exception
   {
      final Map<String, ?> timeoutData = Collections.singletonMap(TIMEOUT_DATA_ID, Long.valueOf(1));
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR, timeoutData, true);
      doOneEventDaemonRun();
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Hibernated));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR, ENABLED_NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR, DISABLED_NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final Exception ignored)
      {
         /* expected */
      }
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
   }
   
   @Test
   public void testNonInterruptingTimerEventNotOccurring() throws Exception
   {
      final Map<String, ?> timeoutData = Collections.singletonMap(TIMEOUT_DATA_ID, Long.MAX_VALUE);
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, timeoutData, true);
      doOneEventDaemonRun();
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Hibernated));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, SLEEPING_ACTIVITY_ID, ActivityInstanceState.Completed));
      sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, NORMAL_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
      try
      {
         sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING, EXCEPTION_FLOW_ACTIVITY_ID, ActivityInstanceState.Completed));
         fail();
      }
      catch (final ObjectNotFoundException ignored)
      {
         /* expected */
      }
   }

   
   private void doOneEventDaemonRun()
   {
      DaemonHome.startDaemon(sf.getAdministrationService(), DaemonType.EVENT_DAEMON);
      final Daemon daemon = DaemonHome.getDaemon(sf.getAdministrationService(), DaemonType.EVENT_DAEMON);
      assertNotNull(daemon.getLastExecutionTime());
   }
   
   
   /**
    * <p>
    * TODO (nw) javadoc
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision: $
    */
   public static final class FailingApp
   {
      public void conditionalFail(final boolean fail)
      {
         if (fail)
         {
            throw new RuntimeException("expected");
         }
      }
      
      public void failWith(final String exceptionClass)
      {
         if (NullPointerException.class.getName().equals(exceptionClass))
         {
            throw new NullPointerException("expected");
         }
         else if (IllegalArgumentException.class.getName().equals(exceptionClass))
         {
            throw new IllegalArgumentException("expected");
         }

         /* do not fail */
      }
   }
}
