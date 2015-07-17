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

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
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
public class SpawnSubProcessTest
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
   public void testCopyNotesFromSyncSharedSubprocess()
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

      // Verify note is copied properly - CRNT-31194
      ProcessInstance spawnedOriginalPI = wfs.spawnSubprocessInstance(
            subProcessPiOid, "InputData2", true, null);
      ProcessInstanceAttributes attributes2 = spawnedOriginalPI.getAttributes();
      List<Note> notes = attributes2.getNotes();
      Assert.assertFalse(notes.isEmpty());
      Assert.assertEquals("Notes should not be empty", 1, notes.size());
      Assert.assertEquals(
            "Note should be properly copied to same spawned process instance",
            "Test note 1", notes.get(0).toString());


      // Complete originating process so that easy to find using PIQuery
      wfs.complete(aiOriginalPI.getOID(), null, null);


      // Verify that Link_type=spawn to/spawn from is properly set - CRNT-31046
      ProcessInstanceQuery piq = ProcessInstanceQuery.findAll();
      piq.where(ProcessInstanceQuery.OID.isEqual(subProcessPiOid));
      ProcessInstance originatingProcess = sf.getQueryService().findFirstProcessInstance(
            piq);
      Assert.assertEquals("Originating process should be returned", subProcessPiOid,
            originatingProcess.getOID());

      piq = null;
      piq = ProcessInstanceQuery.findInState("InputData2",
            ProcessInstanceState.Active);
      ProcessInstance spawnedProcess = sf.getQueryService().findFirstProcessInstance(piq);
      Assert.assertEquals("Spawned process should be returned",
            spawnedOriginalPI.getOID(), spawnedProcess.getOID());
   }
}
