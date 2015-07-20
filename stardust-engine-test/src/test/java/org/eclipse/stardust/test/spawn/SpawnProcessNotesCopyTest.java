/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.spawn;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.List;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains tests for the <i>Spawn SubProcess</i> functionality,
 * which allows for ad hoc spawning of process instances (refer to the Stardust documentation
 * for details about <i>Spawn Process</i>).
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpawnProcessNotesCopyTest
{
   public static final String MODEL_NAME = "SpawnProcessModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   // ************************************
   // **             SYNC               **
   // ************************************

   @Test
   public void testSpawnSubCopyNotesFromRootProcess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance originalPI = wfs.startProcess(
            "InputData1", null, true);
      ProcessInstanceAttributes attributes = originalPI.getAttributes();
      attributes.addNote("Test note 1");
      wfs.setProcessInstanceAttributes(attributes);

      // Verify note is copied properly - CRNT-31194
      ProcessInstance spawnedOriginalPI = wfs.spawnSubprocessInstance(
            originalPI.getOID(), "InputData2", true, null);

      // Assert returned PI.
      assertNote(spawnedOriginalPI);

      // Assert additional retrieval.
      assertNotesByRetrieval(spawnedOriginalPI);

      // Assert by query retrieval.
      assertNoteByQuery(spawnedOriginalPI);
   }

   @Test
   public void testSpawnSubCopyNotesFromSyncSharedSubprocess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance originalPI = wfs.startProcess(
            "StartInputSubprocess1SyncShared", null, true);
      ProcessInstanceAttributes attributes = originalPI.getAttributes();
      attributes.addNote("Test note 1");
      wfs.setProcessInstanceAttributes(attributes);

      // get subprocess by activating next activity.
      ActivityInstance aiOriginalPI = wfs
            .activateNextActivityInstanceForProcessInstance(originalPI.getOID());
      long subProcessPiOid = aiOriginalPI.getProcessInstanceOID();

      ProcessInstance spawnedOriginalPI = wfs.spawnSubprocessInstance(
            subProcessPiOid, "InputData2", true, null);

      // Assert returned PI.
      assertNote(spawnedOriginalPI);

      // Assert additional retrieval.
      assertNotesByRetrieval(spawnedOriginalPI);

      // Assert by query retrieval.
      assertNoteByQuery(spawnedOriginalPI);
   }

   @Test
   public void testSpawnSubCopyNotesFromSyncSeparateSubprocess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance originalPI = wfs.startProcess(
            "StartInputSubprocess1SyncSeperateCopy", null, true);

      // get subprocess by activating next activity.
      ActivityInstance aiOriginalPI = wfs
            .activateNextActivityInstanceForProcessInstance(originalPI.getOID());
      long subProcessPiOid = aiOriginalPI.getProcessInstanceOID();

      ProcessInstance subPi = wfs.getProcessInstance(subProcessPiOid);
      ProcessInstanceAttributes attributes = subPi.getAttributes();
      attributes.addNote("Test note 1");
      wfs.setProcessInstanceAttributes(attributes);

      ProcessInstance spawnedOriginalPI = wfs.spawnSubprocessInstance(
            subProcessPiOid, "InputData2", true, null);

      // Assert returned PI.
      assertNote(spawnedOriginalPI);

      // Assert additional retrieval.
      assertNotesByRetrieval(spawnedOriginalPI);

      // Assert by query retrieval.
      assertNoteByQuery(spawnedOriginalPI);
   }



   // ************************************
   // **             SYNC               **
   // ************************************

   @Test
   public void testSpawnPeerCopyNotesFromRootProcess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance originalPI = wfs.startProcess(
            "InputData1", null, true);
      ProcessInstanceAttributes attributes = originalPI.getAttributes();
      attributes.addNote("Test note 1");
      wfs.setProcessInstanceAttributes(attributes);

      ProcessInstance spawnedOriginalPI = wfs.spawnPeerProcessInstance(
            originalPI.getOID(), "InputData2", true, null, true, null);

      // Assert returned PI.
      assertNote(spawnedOriginalPI);

      // Assert additional retrieval.
      assertNotesByRetrieval(spawnedOriginalPI);

      // Assert by query retrieval.
      assertNoteByQuery(spawnedOriginalPI);
   }

   /**
    * SpawnPeerProcessInstance not allowed from subprocess.
    */
   @Test(expected=IllegalOperationException.class)
   public void testSpawnPeerCopyNotesFromSyncSharedSubprocess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance originalPI = wfs.startProcess(
            "StartInputSubprocess1SyncShared", null, true);
      ProcessInstanceAttributes attributes = originalPI.getAttributes();
      attributes.addNote("Test note 1");
      wfs.setProcessInstanceAttributes(attributes);

      // get subprocess by activating next activity.
      ActivityInstance aiOriginalPI = wfs
            .activateNextActivityInstanceForProcessInstance(originalPI.getOID());
      long subProcessPiOid = aiOriginalPI.getProcessInstanceOID();

      ProcessInstance spawnedOriginalPI = wfs.spawnPeerProcessInstance(
            subProcessPiOid, "InputData2", true, null, true, null);

      // Assert returned PI.
      assertNote(spawnedOriginalPI);

      // Assert additional retrieval.
      assertNotesByRetrieval(spawnedOriginalPI);

      // Assert by query retrieval.
      assertNoteByQuery(spawnedOriginalPI);
   }

   /**
    * SpawnPeerProcessInstance not allowed from subprocess.
    */
   @Test(expected=IllegalOperationException.class)
   public void testSpawnPeerCopyNotesFromSyncSeparateSubprocess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance originalPI = wfs.startProcess(
            "StartInputSubprocess1SyncSeperateCopy", null, true);

      // get subprocess by activating next activity.
      ActivityInstance aiOriginalPI = wfs
            .activateNextActivityInstanceForProcessInstance(originalPI.getOID());
      long subProcessPiOid = aiOriginalPI.getProcessInstanceOID();

      ProcessInstance subPi = wfs.getProcessInstance(subProcessPiOid);
      ProcessInstanceAttributes attributes = subPi.getAttributes();
      attributes.addNote("Test note 1");
      wfs.setProcessInstanceAttributes(attributes);

      ProcessInstance spawnedOriginalPI = wfs.spawnPeerProcessInstance(
            subProcessPiOid, "InputData2", true, null, true, null);

      // Assert returned PI.
      assertNote(spawnedOriginalPI);

      // Assert additional retrieval.
      assertNotesByRetrieval(spawnedOriginalPI);

      // Assert by query retrieval.
      assertNoteByQuery(spawnedOriginalPI);
   }

   private void assertNotesByRetrieval(ProcessInstance spawnedOriginalPI)
   {
      WorkflowService wfs = sf.getWorkflowService();
      ProcessInstance retrievedSpawnedOriginalPI = wfs.getProcessInstance(spawnedOriginalPI.getOID());

      assertNote(retrievedSpawnedOriginalPI);
   }

   private void assertNoteByQuery(ProcessInstance spawnedOriginalPI)
   {
      ProcessInstanceQuery piq = ProcessInstanceQuery.findInState("InputData2",
            ProcessInstanceState.Active);
      ProcessInstance spawnedProcess = sf.getQueryService().findFirstProcessInstance(piq);
      Assert.assertEquals("Spawned process should be returned",
            spawnedOriginalPI.getOID(), spawnedProcess.getOID());

      assertNote(spawnedProcess);
   }

   private void assertNote(ProcessInstance retrievedSpawnedOriginalPI)
   {
      ProcessInstanceAttributes attributes3 = retrievedSpawnedOriginalPI.getAttributes();
      List<Note> notes3 = attributes3.getNotes();
      Assert.assertFalse(notes3.isEmpty());
      Assert.assertEquals("Notes should not be empty", 1, notes3.size());
      Assert.assertEquals(
            "Note should be properly copied to same spawned process instance",
            "Test note 1", notes3.get(0).toString());
   }
}
