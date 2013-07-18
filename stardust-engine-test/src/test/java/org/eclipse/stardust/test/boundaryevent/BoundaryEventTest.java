package org.eclipse.stardust.test.boundaryevent;

import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.APP_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.DISABLED_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.ENABLED_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_DATA_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_FLOW_1_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_FLOW_2_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.EXCEPTION_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.FAIL_FLAG_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.FIRST_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.INVALID_MODEL_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_ERROR_EVENT;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_MULTIPLE_ERROR_EVENTS;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_INTERRUPTING;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_AND;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.PROCESS_ID_TIMER_EVENT_NON_INTERRUPTING_XOR;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.SECOND_NORMAL_FLOW_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.SLEEPING_ACTIVITY_ID;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.TIMEOUT_DATA_ID;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
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
 * This class contains test cases making sure the <i>Boundary Event</i> functionality, i.e.
 * interpretation of <i>BPMN 2.x</i> boundary events works correctly. The tests cover the
 * following boundary event types
 * <ul>
 *    <li>Error (always interrupting according to the <i>BPMN 2.x</i> specification)</li>
 *    <li>Timer &mdash; Interrupting</li>
 *    <li>Timer &mdash; Non-interrupting</li>
 * </ul>
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Error</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the exception flow is traversed <b>instead of</b> the normal flow,
    * if the boundary event has been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Error</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the normal flow is traversed <b>instead of</b> the exception flow,
    * if the boundary event has <b>not</b> been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Error</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the normal flow is traversed <b>instead of</b> the various exception flows,
    * if the boundary event has <b>not</b> been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Error</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the correct exception flow is traversed <b>instead of</b> the normal flow or the other exception flow,
    * if the corresponding boundary event has been fired. In this case it's the first exception flow.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Error</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the correct exception flow is traversed <b>instead of</b> the normal flow or the other exception flow,
    * if the corresponding boundary event has been fired. In this case it's the second exception flow.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Timer &mdash; Interrupting</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the exception flow is traversed <b>instead of</b> the normal flow,
    * if the corresponding boundary event has been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Timer &mdash; Interrupting</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the normal flow is traversed <b>instead of</b> the exception flow,
    * if the corresponding boundary event has <b>not</b> been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Timer &mdash; Non-interrupting</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the exception flow is traversed <b>in addition</b> to the normal flow,
    * if the corresponding boundary event has been fired.
    * </p>
    */
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

   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Timer &mdash; Non-interrupting</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the exception flow is traversed <b>in addition</b> to the various AND split normal flows,
    * if the corresponding boundary event has been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Timer &mdash; Non-interrupting</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the exception flow is traversed <b>in addition</b> to the conditional normal flow (XOR),
    * if the corresponding boundary event has been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on the boundary event type <b><i>Timer &mdash; Non-interrupting</i></b>.
    * </p>
    * 
    * <p>
    * This test makes sure that the normal flow is traversed <b>without</b> enabling the exception flow,
    * if the corresponding boundary event has <b>not</b> been fired.
    * </p>
    */
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
   
   /**
    * <p>
    * This test focuses on deployment validation of models containing boundary events.
    * </p>
    * 
    * <p>
    * This test makes sure that a boundary event without a corresponding exception flow transition issues a warning
    * during model deployment.
    * </p>
    */
   @Test
   public void testDeploymentOfModelWithMissingExceptionFlowTransitionsIssuesWarning()
   {
      final DeploymentOptions deploymentOptions = new DeploymentOptions();
      deploymentOptions.setIgnoreWarnings(true);
      
      final List<DeploymentInfo> deploymentInfos = RtEnvHome.deploy(sf.getAdministrationService(), deploymentOptions, INVALID_MODEL_ID);
      assertThat(deploymentInfos.size(), is(1));
      
      final DeploymentInfo deploymentInfo = deploymentInfos.get(0);
      final List<Inconsistency> warnings = deploymentInfo.getWarnings();
      assertThat(warnings.size(), is(1));
      
      final Inconsistency inconsistency = warnings.get(0);
      final String errorMsg = inconsistency.getMessage();
      assertThat(errorMsg, startsWith("No exception flow transition"));
   }
   
   private void doOneEventDaemonRun()
   {
      DaemonHome.startDaemon(sf.getAdministrationService(), DaemonType.EVENT_DAEMON);
      final Daemon daemon = DaemonHome.getDaemon(sf.getAdministrationService(), DaemonType.EVENT_DAEMON);
      assertNotNull(daemon.getLastExecutionTime());
   }
   
   /**
    * <p>
    * This is the application used in the model that causes the process instance to fail
    * in order to investigate the behavior in case of failures triggering the exception flow.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
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
