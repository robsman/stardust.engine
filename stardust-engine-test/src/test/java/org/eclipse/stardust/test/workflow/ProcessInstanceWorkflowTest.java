/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.workflow;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.DEFAULT_ROLE_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.PD_1_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.eclipse.stardust.test.api.UserHome;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests basic functionality regarding the workflow of process instances.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ProcessInstanceWorkflowTest extends LocalJcrH2Test
{
   private static final String DEFAULT_ROLE_USER_ID = "u1";
   
   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory defaultRoleSf = new ClientServiceFactory(DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_USER_ID);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(defaultRoleSf);
   
   @Before
   public void setUp()
   {
      UserHome.create(adminSf, DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_ID);
   }
   
   /**
    * <p>
    * Tests whether starting the process instance synchronously
    * works correctly.
    * </p>
    */
   @Test
   public void testStartProcessSynchronously()
   {
      final ProcessInstance pi = defaultRoleSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      assertNotNull(pi);
      assertEquals(ProcessInstanceState.Active, pi.getState());
   }

   /**
    * <p>
    * Tests whether starting the process instance asynchronously
    * works correctly.
    * </p>
    */
   @Test
   public void testStartProcessAsynchronously()
   {
      final ProcessInstance pi = defaultRoleSf.getWorkflowService().startProcess(PD_1_ID, null, false);
      assertNotNull(pi);
      assertEquals(ProcessInstanceState.Active, pi.getState());
   }

   /**
    * <p>
    * Tests whether starting the process instance by passing a
    * qualified ID works correctly.
    * </p>
    */
   @Test
   @Ignore
   public void testStartProcessQualifiedId()
   {
      // TODO implement ...
      fail("Not Yet Implemented");
   }
   
   /**
    * <p>
    * Tests whether the process data will be initialized correctly with the
    * passed data values. 
    * </p>
    */
   @Test
   @Ignore
   public void testStartProcessPassData()
   {
      // TODO implement ...
      fail("Not Yet Implemented");
   }   
   
   /**
    * <p>
    * Tests whether the appropriate exception is thrown when the process definition
    * cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testStartProcessFailProcessDefinitionNotFound()
   {
      defaultRoleSf.getWorkflowService().startProcess("N/A", null, true);
   }
   
   // TODO write test cases for spawnSubprocessInstance()
   
   // TODO write test cases for spawnPeerProcessInstance()
   
   // TODO write test cases for createCase()
   
   // TODO write test cases for joinCase()
   
   // TODO write test cases for leaveCase()
   
   // TODO write test cases for mergeCases()
   
   // TODO write test cases for delegateCase()
   
   // TODO write test cases for joinProcessInstance()
   
   // TODO write test cases for abortProcessInstance()
   
   // TODO write test cases for getProcessInstance()
   
   // TODO write test cases for getProcessResults()
   
   // TODO write test cases for getStartableProcessDefinitions()
   
   // TODO write test cases for setProcessInstanceAttributes()
}
