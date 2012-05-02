/**********************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.qc;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO javadoc
 * </p>
 * 
 * @author Holger.Prause
 * @version $Revision$
 */
public class QualityControlRuntimeTest
{
   /* package-private */ static final String MODEL_NAME = "QCModel";
   
   private static final String QC_MANAGER_ID = "QCManager";
   private static final String MONITORED_USER_ID = "MonitoredUser";
   private static final String ORGANIZATION_ID= "SungardCorp";
   private static final String DUMMY_USER_ID = "Dummy";
   
   private final String ERROR_CODE_FOR_FAIL = "100";
   private final String ERROR_CODE_FOR_PASS_WITH_CORRECTION = "101";
   private final String ERROR_CODE_FOR_PASS_WITHOUT_CORRECTION = "102";
   
   private static final String QA_DATA_VALUE = "do_perform_qa";
   
   private static final String DATA_VALUE_CORRECTED_BY_USER = "somevalue_corrected_by_user";
   private static final String DATA_VALUE_CORRECTED_BY_QC_MANAGER = "somevalue_corrected_by_qc_manager";
      
   private static final String PROCESS_DEFINITION_ID = "ProcessDefinition1";
   
   private static final String QA_ENABLED_ACTIVITY_ID = "QCEnabledActivity";
   private static final String END_ACTIVITY_ID = "EndActivity";
   
   private static final String DATA_ID = "PrimitiveData1";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private QueryService qs;
   
   private ActivityInstance currentActivityInstance;
   private ProcessInstance currentProcessInstance;
   private Set<QualityAssuranceCode> errorCodesDefinedForAI;
   
   private WorkflowService qcManagerWorkflowService;
   private WorkflowService monitoredUserWorkflowService;
   private User monitoredUser;
   private WorkflowService ws;
   private ServiceFactory qcManagerServiceFactory;
   private ServiceFactory monitoredUserServiceFactory;

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   @Before
   public void setUp()
   {
      qs = sf.getQueryService();
      ws = sf.getWorkflowService();

      currentProcessInstance = null;
      currentActivityInstance = null;
      
      monitoredUser = UserHome.create(sf, MONITORED_USER_ID, MONITORED_USER_ID, ORGANIZATION_ID);
      UserHome.create(sf, QC_MANAGER_ID, QC_MANAGER_ID, ORGANIZATION_ID);
      UserHome.create(sf, DUMMY_USER_ID, MONITORED_USER_ID, ORGANIZATION_ID);
      
      qcManagerServiceFactory = ServiceFactoryLocator.get(QC_MANAGER_ID, QC_MANAGER_ID);    
      qcManagerWorkflowService = qcManagerServiceFactory.getWorkflowService();
      
      monitoredUserServiceFactory = ServiceFactoryLocator.get(MONITORED_USER_ID, MONITORED_USER_ID); 
      monitoredUserWorkflowService = monitoredUserServiceFactory.getWorkflowService();      
   }

   @Test(expected = IllegalOperationException.class)
   public void testCompleteBeforeAttributesSet()
   {
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         
         if (currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }
      
      //completing qa instance without having set attributes before must result in exception
      currentActivityInstance = qcManagerWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null, null);
      Assert.fail();   
   }
   
   @Test
   public void testSuspendToWorkflowUser()
   {
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         
         if (currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      // data value should be written
      assertDataExists(QA_DATA_VALUE);      
      currentActivityInstance = qcManagerWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      
      //try to suspend the qa activity to the user who worked on the previous instance
      //this must result in an exception
      
      IllegalOperationException exception = null;
      try
      {
         currentActivityInstance = qcManagerWorkflowService.suspendToUser(currentActivityInstance.getOID(), monitoredUser.getOID());
         fail();
      }
      catch(Exception e)
      {
         assertThat(e, instanceOf(IllegalOperationException.class));
         exception = (IllegalOperationException) e;
      }
      
      assertEquals("BPMRT04006", exception.getError().getId());
   }
   
   @Test
   public void testActivationLogOnActivateAndComplete()
   {
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      ActivityCompletionLog completionLog = null;

      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         // the next instance for the user cannot be activated
         // because the user should not be allowed to monitor its own work
         outData.put(DATA_ID, QA_DATA_VALUE);
         completionLog = monitoredUserWorkflowService.activateAndComplete(
               currentActivityInstance.getOID(), null, outData,
               WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE);

         currentActivityInstance = completionLog.getCompletedActivity();
         if (completionLog.getCompletedActivity().getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      // data value should be written
      assertDataExists(QA_DATA_VALUE);

      ActivityInstance nextUserInstance = completionLog.getNextForUser();
      assertNull("CRNT-23053: next instance cannot be activated and should be null",
            nextUserInstance);
   }
   
   @Test
   public void testActivationLogOnComplete()
   {
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      ActivityCompletionLog completionLog = null;
      
      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         
         //the next instance for the user cannot be activated
         //because the user should not be allowed to monitor its own work
         outData.put(DATA_ID, QA_DATA_VALUE);
         completionLog = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData, WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE);
         currentActivityInstance = completionLog.getCompletedActivity();

         if (completionLog.getCompletedActivity().getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      // data value should be written
      assertDataExists(QA_DATA_VALUE);
      
      ActivityInstance nextUserInstance = completionLog.getNextForUser();
      assertNull("CRNT-23053: next instance cannot be activated and should be null", nextUserInstance);
   }
   
   @Test
   public void testActivateQAByUser()
   {
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         
         if (currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      // data value should be written
      assertDataExists(QA_DATA_VALUE);
      
      //the user should not be allowed to qa itself
      IllegalOperationException exception = null;
      try 
      {
         currentActivityInstance 
         = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance
            .getOID());
         fail();
      }
      catch(Exception e)
      {
         assertThat(e, instanceOf(IllegalOperationException.class));
         exception = (IllegalOperationException) e;
      }
      assertEquals("BPMRT04005", exception.getError().getId());
   }
   
   @Test
   public void testParticipantProbability()
   {
      DeployedModel model = qs.getActiveModel();
      ProcessDefinition pDef = model.getProcessDefinition(PROCESS_DEFINITION_ID);
      Activity qaEnabledActivity = pDef.getActivity(QA_ENABLED_ACTIVITY_ID);
      
      //set probability to 0 for the monitored user
      QualityAssuranceAdminServiceFacade command 
         = new QualityAssuranceAdminServiceFacade(sf);
      command.setQualityAssuranceParticipantProbability(qaEnabledActivity, null, 0);
      
      Map<String, String> outData = new HashMap<String, String>();
      
      //instance SHOULD NOT go into qa because the probability was set to 0% for all user
      for(int i=0; i<20; i++)
      {   
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         assertEquals("Instance should not go into qa", QualityAssuranceState.NO_QUALITY_ASSURANCE, currentActivityInstance.getQualityAssuranceState());
      }
      
      //let the admin set probability to 100 for the monitored user
      command.setQualityAssuranceParticipantProbability(qaEnabledActivity, null, 100);      
      //instance SHOULD go into qa because the probability was set to 100% for all user
      for(int i=0; i<20; i++)
      {   
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         assertEquals("Instance should go into qa", QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED, currentActivityInstance.getQualityAssuranceState());
      }
      
      //now set probability to null - the value should be ignored and no exception should occur
      command.setQualityAssuranceParticipantProbability(qaEnabledActivity, null, null);      
      //instance SHOULD go into qa because the probability is still on 100% 
      //and the null value should not have been saved
      for(int i=0; i<20; i++)
      {   
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         assertEquals("Instance should go into qa", QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED, currentActivityInstance.getQualityAssuranceState());
      }
   }
   
   @Test
   public void testQAFormula()
   {      
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      DeployedModel model = qs.getActiveModel();
      ProcessDefinition pDef = model.getProcessDefinition(PROCESS_DEFINITION_ID);
      Activity qaEnabledActivity = pDef.getActivity(QA_ENABLED_ACTIVITY_ID);
      
      //set probability to 100 for the monitored user - for easier test expectations
      QualityAssuranceAdminServiceFacade command 
         = new QualityAssuranceAdminServiceFacade(sf);
      command.setQualityAssuranceParticipantProbability(qaEnabledActivity, null, 100);
      
      
      //instance SHOULD NOT go into qa because the data entered dont match the qa formula
      for(int i=0; i<10; i++)
      {   
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         String nonQaDataValue = "some non qa value";
         outData.put(DATA_ID, nonQaDataValue);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         assertEquals("Instance should not go into qa", QualityAssuranceState.NO_QUALITY_ASSURANCE, currentActivityInstance.getQualityAssuranceState());
      }
      
      //instance SHOULD go into qa because the data entered matches the qa formula
      for(int i=0; i<10; i++)
      {   
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         assertEquals("Instance should go into qa", QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED, currentActivityInstance.getQualityAssuranceState());
      }
   }
   
   @Test
   public void testBackwardReference()
   {
      ActivityInstance monitoredInstance = null;
      ActivityInstance lastQcInstance = null;
      
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);
         monitoredInstance = currentActivityInstance;
         
         if (currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      // data value should be written
      assertDataExists(QA_DATA_VALUE);

      //every qc instance should have a backward reference to the activity instance on which they were created
      currentActivityInstance = qcManagerWorkflowService
            .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                  .getOID());
      lastQcInstance = currentActivityInstance;
      
      assertEquals(currentActivityInstance.getQualityAssuranceState(),
            QualityAssuranceState.IS_QUALITY_ASSURANCE); 
      QualityAssuranceInfo info = currentActivityInstance.getQualityAssuranceInfo();
      assertEquals("In the qc instance, a backward reference to the monitored isntance shoudl exists",monitoredInstance.getOID(), info.getMonitoredInstance().getOID());
      
      ActivityInstanceAttributes attributes = getAttributesForFail(
            errorCodesDefinedForAI, currentActivityInstance.getOID());
      qcManagerWorkflowService.setActivityInstanceAttributes(attributes);
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null, null);
      
      //all recreated activity instances should have a backward reference to the qc instance
      currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance
            .getOID());
      info = currentActivityInstance.getQualityAssuranceInfo();
      assertEquals("Abackward ref to the last qc instance should exists",
            lastQcInstance.getOID(),
            info.getFailedQualityAssuranceInstance().getOID());
      monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, null);
      
      //again - another qc instance should be created
      currentActivityInstance = qcManagerWorkflowService
      .activateNextActivityInstanceForProcessInstance(currentProcessInstance
            .getOID());      
      assertEquals(currentActivityInstance.getQualityAssuranceState(),
            QualityAssuranceState.IS_QUALITY_ASSURANCE); 
      // information about the last failed qc instance should still be available
      info = currentActivityInstance.getQualityAssuranceInfo();
      assertEquals("A backward ref to the last qc instance should exists",
            lastQcInstance.getOID(),
            info.getFailedQualityAssuranceInstance().getOID());
      
      ActivityInstanceAttributes successAttributes 
         = getAttributeForPassWithoutCorrection(errorCodesDefinedForAI, currentActivityInstance.getOID());
      qcManagerWorkflowService.setActivityInstanceAttributes(successAttributes);
      qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null, null);
      
      //the next instance should be a regular one anddont contain any backward references
      currentActivityInstance 
         = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());  
      assertNull("no backward references should exists: ", currentActivityInstance.getQualityAssuranceInfo());
   }
   
   @Test
   public void testAddNote()
   {
      currentProcessInstance = monitoredUserWorkflowService.startProcess(
            PROCESS_DEFINITION_ID, null, true);
      currentActivityInstance = monitoredUserWorkflowService
            .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                  .getOID());
      
      final String failNoteText1 = "Correct Typo";
      final String failNoteText2 = "Pay more attention";
      final String failNoteText3 = "Set correct value";
            
      //add notes to the activity instance
      ActivityInstanceAttributes noteAttributes 
         = new ActivityInstanceAttributesImpl(currentActivityInstance.getOID());
      noteAttributes.addNote(failNoteText1);
      noteAttributes.addNote(failNoteText2);
      noteAttributes.addNote(failNoteText3);
      
      monitoredUserWorkflowService.setActivityInstanceAttributes(noteAttributes);
      assertNotesExists(failNoteText1, failNoteText2, failNoteText3);
      
      //CRNT-22865: now use query service again to fetch activity instance 
      ActivityInstance checkInstance = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      List<Note> notes = checkInstance.getAttributes().getNotes();
      assertNoteExists(notes, failNoteText1);
      assertNoteExists(notes, failNoteText2);
      assertNoteExists(notes, failNoteText3);
   }
   
   @Test
   public void testWorklistQuery()
   {
      boolean qcInstanceWasCreated = false;
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>();
      Map<String, String> outData = new HashMap<String, String>();

      while (qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(
               PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService
               .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                     .getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity()
               .getAllQualityAssuranceCodes();

         outData.put(DATA_ID, QA_DATA_VALUE);
         currentActivityInstance = monitoredUserWorkflowService.complete(
               currentActivityInstance.getOID(), null, outData);

         if (currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      // data value should be written
      assertDataExists(QA_DATA_VALUE);
      
      currentActivityInstance = qcManagerWorkflowService
            .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                  .getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(),
            QualityAssuranceState.IS_QUALITY_ASSURANCE);
      
      //the qc instance should be in the worklist of the qc manager
      Worklist wl = qcManagerWorkflowService.getWorklist(WorklistQuery.findPrivateWorklist());
      assertEquals(1, wl.getTotalCount());
      
      ActivityInstance qcInstance = (ActivityInstance) wl.get(0);
      assertEquals(QualityAssuranceState.IS_QUALITY_ASSURANCE, 
            qcInstance.getQualityAssuranceState());           
   }
   
   @Test
   public void testDelegateToUser()
   {
      boolean qcInstanceWasCreated = false;      
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>(); 
      Map<String, String> outData = new HashMap<String, String>();
      
      while(qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity().getAllQualityAssuranceCodes();
         
         outData.put(DATA_ID, QA_DATA_VALUE );
         currentActivityInstance = monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, outData);

         if(currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      //data value should be written 
      assertDataExists(QA_DATA_VALUE);
      
      //activate with the qc manager
      //the next instance should be a quality control instance
      currentActivityInstance = qcManagerWorkflowService
            .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                  .getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(),
            QualityAssuranceState.IS_QUALITY_ASSURANCE);

      //the instance should go back to the user
      ActivityInstanceAttributes attributes 
         = getAttributesForFail(errorCodesDefinedForAI, currentActivityInstance.getOID());
      attributes.getQualityAssuranceResult().setAssignFailedInstanceToLastPerformer(true);
      
      qcManagerWorkflowService.setActivityInstanceAttributes(attributes);
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null,
            null);
     
      currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      long userPerformer = currentActivityInstance.getUserPerformerOID();
      
      assertEquals("Activity should be delagated", monitoredUser.getOID(), userPerformer);
   
      //the instance should be in the worklist of the user again
      Worklist wl = monitoredUserWorkflowService.getWorklist(WorklistQuery.findPrivateWorklist());
      assertEquals(1, wl.getTotalCount());
      
      ActivityInstance workflowInstance = (ActivityInstance) wl.get(0);
      assertEquals(QualityAssuranceState.IS_REVISED, 
            workflowInstance.getQualityAssuranceState()); 
   }
   
   @Test
   public void testDelegateToParticipant()
   {
      boolean qcInstanceWasCreated = false;      
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>(); 
      Map<String, String> outData = new HashMap<String, String>();
      
      while(qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity().getAllQualityAssuranceCodes();
         
         outData.put(DATA_ID, QA_DATA_VALUE );
         currentActivityInstance = monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, outData);

         if(currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      //data value should be written 
      assertDataExists(QA_DATA_VALUE);
      
      //activate with the qc manager
      //the next instance should be a quality control instance
      currentActivityInstance = qcManagerWorkflowService
            .activateNextActivityInstanceForProcessInstance(currentProcessInstance
                  .getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(),
            QualityAssuranceState.IS_QUALITY_ASSURANCE);

      //the instance should go back to the work flow participant
      ActivityInstanceAttributes attributes 
         = getAttributesForFail(errorCodesDefinedForAI, currentActivityInstance.getOID());
      attributes.getQualityAssuranceResult().setAssignFailedInstanceToLastPerformer(false);
      
      qcManagerWorkflowService.setActivityInstanceAttributes(attributes);
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null,
            null);
     
      currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      
      String partiticpantId = currentActivityInstance.getParticipantPerformerID();
      assertEquals("Activity should be delagated", MONITORED_USER_ID, partiticpantId);
   
      //the instance should be in the worklist of the user again
      Worklist wl = monitoredUserWorkflowService.getWorklist(WorklistQuery.findPrivateWorklist());
      assertEquals(1, wl.getTotalCount());
      
      ActivityInstance workflowInstance = (ActivityInstance) wl.get(0);
      assertEquals(QualityAssuranceState.IS_REVISED, 
            workflowInstance.getQualityAssuranceState()); 
   }

   @Test
   public void testFail()
   {
      boolean qcInstanceWasCreated = false;      
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>(); 
      Map<String, String> outData = new HashMap<String, String>();
      
      while(qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity().getAllQualityAssuranceCodes();
         
         outData.put(DATA_ID, QA_DATA_VALUE );
         currentActivityInstance = monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, outData);

         if(currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      //data value should be written 
      assertDataExists(QA_DATA_VALUE);

      //the next instance should be a quality control instance
      currentActivityInstance = qcManagerWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      //the qc manager should see the values from the user
      assertDataExists(QA_DATA_VALUE);
      assertEquals(currentActivityInstance.getQualityAssuranceState(), QualityAssuranceState.IS_QUALITY_ASSURANCE);
      
      ActivityInstanceAttributes attributes 
         = getAttributesForFail(errorCodesDefinedForAI, currentActivityInstance.getOID());
      qcManagerWorkflowService.setActivityInstanceAttributes(attributes);
      
      //changing data on fail must have no effect
      String modifiedData = QA_DATA_VALUE + System.currentTimeMillis();
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null, null);
      assertDataNotExists(modifiedData);
      assertDataExists(QA_DATA_VALUE);
      
      //the result of the qc instance should be marked as failed
      attributes = currentActivityInstance.getAttributes();
      QualityAssuranceResult result = attributes.getQualityAssuranceResult();
      assertEquals(result.getQualityAssuranceState(), ResultState.FAILED);
      
      //the error code set must must have effect
      assertEquals(result.getQualityAssuranceCodes().size(), 1);
      assertNotNull(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_FAIL));
      
      //when qc instance failed - the next non qc instance should be in state revise
      currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(), QualityAssuranceState.IS_REVISED);
      
      //activity instance should also contain information about the failed qc instance
      ActivityInstance lastFailedQcInstance 
         = currentActivityInstance.getQualityAssuranceInfo().getFailedQualityAssuranceInstance();
      result = lastFailedQcInstance.getAttributes().getQualityAssuranceResult();
      assertEquals(result.getQualityAssuranceState(), ResultState.FAILED);
      
      //also check the errocodes (again)
      assertEquals(result.getQualityAssuranceCodes().size(), 1);
      assertNotNull(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_FAIL)); 
   }

   @Test
   public void testPassWithCorrection()
   {
      testFail();
      
      //let the user correct his value
      Map<String, String> outData = new HashMap<String, String>();
      outData.put(DATA_ID, DATA_VALUE_CORRECTED_BY_USER);
      
      //completing and setting data value should works as usual
      currentActivityInstance = monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, outData);
      assertDataNotExists(QA_DATA_VALUE);
      assertDataExists(DATA_VALUE_CORRECTED_BY_USER);
      
      //next instance must be a qc instance again
      currentActivityInstance = qcManagerWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(), QualityAssuranceState.IS_QUALITY_ASSURANCE);
   
      ActivityInstanceAttributes attributes 
         = getAttributeForPassWithCorrection(errorCodesDefinedForAI, currentActivityInstance.getOID());
      qcManagerWorkflowService.setActivityInstanceAttributes(attributes);
      outData.put(DATA_ID, DATA_VALUE_CORRECTED_BY_QC_MANAGER);
      
      //the correction made by the qc manager should have effect
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null, outData);
      assertDataNotExists(DATA_VALUE_CORRECTED_BY_USER);
      assertDataExists(DATA_VALUE_CORRECTED_BY_QC_MANAGER);
   
      //the result of the qc instance should be marked as pass with correction
      attributes = currentActivityInstance.getAttributes();
      QualityAssuranceResult result = attributes.getQualityAssuranceResult();
      assertEquals(result.getQualityAssuranceState(), ResultState.PASS_WITH_CORRECTION);
      
      //the correct error code must be set 
      assertEquals(result.getQualityAssuranceCodes().size(), 1);
      assertNotNull(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_PASS_WITH_CORRECTION)); 
      
      //the qc process ends here - next activity instance should be a regular one
      currentActivityInstance 
         = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(), QualityAssuranceState.NO_QUALITY_ASSURANCE);
      assertEquals(currentActivityInstance.getActivity().getId(), END_ACTIVITY_ID );
   
      monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, null);
   }
   
   @Test
   public void testPassWithoutCorrection()
   {
      boolean qcInstanceWasCreated = false;      
      errorCodesDefinedForAI = new HashSet<QualityAssuranceCode>(); 
      Map<String, String> outData = new HashMap<String, String>();
      
      while(qcInstanceWasCreated == false)
      {
         currentProcessInstance = monitoredUserWorkflowService.startProcess(PROCESS_DEFINITION_ID, null, true);
         currentActivityInstance = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
         errorCodesDefinedForAI = currentActivityInstance.getActivity().getAllQualityAssuranceCodes();
         
         outData.put(DATA_ID, QA_DATA_VALUE );
         currentActivityInstance = monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, outData);

         if(currentActivityInstance.getQualityAssuranceState() == QualityAssuranceUtils.QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED)
         {
            qcInstanceWasCreated = true;
         }
      }

      //data value should be written 
      assertDataExists(QA_DATA_VALUE);
      
      //the next instance should be a quality control instance
      currentActivityInstance = qcManagerWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(), QualityAssuranceState.IS_QUALITY_ASSURANCE);
      
      ActivityInstanceAttributes attributes 
         = getAttributeForPassWithoutCorrection(errorCodesDefinedForAI, currentActivityInstance.getOID());
      qcManagerWorkflowService.setActivityInstanceAttributes(attributes);
  
      String modifiedData = QA_DATA_VALUE + System.currentTimeMillis();
      outData.put(DATA_ID, modifiedData );
      
      //the correction made by the qc manager should have no effect      
      currentActivityInstance = qcManagerWorkflowService.complete(currentActivityInstance.getOID(), null, null);
      assertDataNotExists(modifiedData);
      assertDataExists(QA_DATA_VALUE);
      
      //the result of the qc instance should be marked as pass without correction
      attributes = currentActivityInstance.getAttributes();
      QualityAssuranceResult result = attributes.getQualityAssuranceResult();
      assertEquals(result.getQualityAssuranceState(), ResultState.PASS_NO_CORRECTION);
      
      //the correct error code must be set 
      assertEquals(result.getQualityAssuranceCodes().size(), 1);
      assertNotNull(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_PASS_WITHOUT_CORRECTION)); 
      
      //the qc process ends here - next activity instance should be a regular one
      currentActivityInstance 
         = monitoredUserWorkflowService.activateNextActivityInstanceForProcessInstance(currentProcessInstance.getOID());
      assertEquals(currentActivityInstance.getQualityAssuranceState(), QualityAssuranceState.NO_QUALITY_ASSURANCE);
      assertEquals(currentActivityInstance.getActivity().getId(), END_ACTIVITY_ID );
      
      monitoredUserWorkflowService.complete(currentActivityInstance.getOID(), null, null);
   }

   private ActivityInstanceAttributes getAttributesForFail(Set<QualityAssuranceCode> errorCodesDefinedForAI, long aiOid)
   {
      Set<QualityAssuranceCode> errorCodesForFail = new HashSet<QualityAssuranceCode>();
      errorCodesForFail.add(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_FAIL));
      
      QualityAssuranceResultImpl result = new QualityAssuranceResultImpl();
      result.setQualityAssuranceState(ResultState.FAILED);      
      result.setQualityAssuranceCodes(errorCodesForFail);
      
      ActivityInstanceAttributes attributes = new ActivityInstanceAttributesImpl(aiOid);
      attributes.setQualityAssuranceResult(result);
   
      return attributes;
   }
   
   private ActivityInstanceAttributes getAttributeForPassWithCorrection(Set<QualityAssuranceCode> errorCodesDefinedForAI, long aiOid)
   {
      Set<QualityAssuranceCode> errorCodesForPassWithCorrection = new HashSet<QualityAssuranceCode>();
      errorCodesForPassWithCorrection.add(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_PASS_WITH_CORRECTION));
      
      QualityAssuranceResultImpl result = new QualityAssuranceResultImpl();
      result.setQualityAssuranceState(ResultState.PASS_WITH_CORRECTION);      
      result.setQualityAssuranceCodes(errorCodesForPassWithCorrection);
      
      ActivityInstanceAttributes attributes = new ActivityInstanceAttributesImpl(aiOid);
      attributes.setQualityAssuranceResult(result);
   
      return attributes;
   }
   
   private ActivityInstanceAttributes getAttributeForPassWithoutCorrection(Set<QualityAssuranceCode> errorCodesDefinedForAI, long aiOid)
   {
      Set<QualityAssuranceCode> errorCodesForPassWithoutCorrection = new HashSet<QualityAssuranceCode>();
      errorCodesForPassWithoutCorrection.add(getCode(errorCodesDefinedForAI, ERROR_CODE_FOR_PASS_WITHOUT_CORRECTION));
      
      QualityAssuranceResultImpl result = new QualityAssuranceResultImpl();
      result.setQualityAssuranceState(ResultState.PASS_NO_CORRECTION);      
      result.setQualityAssuranceCodes(errorCodesForPassWithoutCorrection);
      
      ActivityInstanceAttributes attributes = new ActivityInstanceAttributesImpl(aiOid);
      attributes.setQualityAssuranceResult(result);
   
      return attributes;
   }
   
   private QualityAssuranceCode getCode(Set<QualityAssuranceCode> codes, String key)
   {
      QualityAssuranceCode found = null;
      for(QualityAssuranceCode qcc: codes)
      {
         if(qcc.getCode() == key)
         {
            found = qcc;
         }
      }
      
      assertNotNull(found);
      return found;
   }
      
   private void assertNotesExists(String...expectedNoteValues)
   {
      ProcessInstance pi 
         = ws.getProcessInstance(currentProcessInstance.getOID());
      ProcessInstanceAttributes piAttributes = pi.getAttributes();
      
      //check if just created notes are available on process instance
      List<Note> notes = piAttributes.getNotes();
      assertEquals(expectedNoteValues.length, notes.size());
      for(String text: expectedNoteValues)
      {
         assertNoteExists(notes, text);
      }
      
      //check if just created notes are available on activity instance
      ActivityInstance ai = 
         ws.getActivityInstance(currentActivityInstance.getOID());
      ActivityInstanceAttributes aiAttributes = ai.getAttributes();
      
      notes = aiAttributes.getNotes();
      assertEquals(expectedNoteValues.length, notes.size());
      for(String text: expectedNoteValues)
      {
         assertNoteExists(notes, text);
      }
   }
   
   private void assertNoteExists(List<Note> notes, String text)
   {
      boolean found = false;
      for(Note n: notes)
      {
         if(n.getText().equals(text))
         {
            found = true;
         }
      }
      
      assertTrue("Note with text: "+text+" must exist", found);
   }
   
   private void assertDataExists(String expectedValue)
   {
      checkDataValue(expectedValue, true);
   }
   
   private void assertDataNotExists(String expectedValue)
   {
      checkDataValue(expectedValue, false);
   }
   
   private void checkDataValue(String expectedValue, boolean shouldExists)
   {
      long processInstanceOid = currentActivityInstance.getProcessInstanceOID();
      ActivityInstances ais 
         = qs.getAllActivityInstances(ActivityInstanceQuery.findInStateHavingData(
               PROCESS_DEFINITION_ID, DATA_ID, expectedValue, currentActivityInstance.getState()));
      
      boolean found = false;
      for(int i=0; i<  ais.getSize(); i++)
      {
         ActivityInstance ai = ais.get(i);
         if(ai.getOID() == currentActivityInstance.getOID())
         {
            found = true;
         }
      }

      if(shouldExists)
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("No matching data found for process isntance oid '");
         errorMsg.append(processInstanceOid);
         errorMsg.append("' and value '");
         errorMsg.append(expectedValue);
         errorMsg.append("'");
         
         assertTrue(errorMsg.toString(), found);
      }
      else
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Data found for process isntance oid '");
         errorMsg.append(processInstanceOid);
         errorMsg.append("' and value '");
         errorMsg.append(expectedValue);
         errorMsg.append("' but should not exist");
         
         assertFalse(errorMsg.toString(), found);
      }
   }
}
