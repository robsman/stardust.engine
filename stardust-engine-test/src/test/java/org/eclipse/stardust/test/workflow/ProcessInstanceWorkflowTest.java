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

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
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
public class ProcessInstanceWorkflowTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String DEFAULT_ROLE_USER_ID = "u1";
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf = new TestServiceFactory(new UsernamePasswordPair(DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_USER_ID));
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf)
                                          .around(userSf);
   
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
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
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
   public void testStartProcessAsynchronously() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PD_1_ID, null, false);
      ActivityInstanceStateBarrier.instance().awaitAlive(pi.getOID());
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
   public void testStartProcessQualifiedId()
   {
      final String modelId = MODEL_NAME;
      final String fqProcessId = "{" + modelId + "}" + PD_1_ID;
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(fqProcessId, null, true);
      assertNotNull(pi);
      assertEquals(ProcessInstanceState.Active, pi.getState());
   }
   
   /**
    * <p>
    * Tests whether the process data will be initialized correctly with the
    * passed data values (tests only String and Integer data).
    * </p>
    */
   @Test
   public void testStartProcessPassData()
   {
      final String originalString = "a string";
      final int originalInt = 81;
      final Map<String, Object> data = CollectionUtils.newHashMap();
      data.put(MY_STRING_DATA_ID, originalString);
      data.put(MY_INT_DATA_ID, originalInt);
      
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PD_1_ID, data, true);
      
      final String actualString = (String) userSf.getWorkflowService().getInDataPath(pi.getOID(), MY_STRING_IN_DATA_PATH_ID);
      final int actualInt = (Integer) userSf.getWorkflowService().getInDataPath(pi.getOID(), MY_INT_IN_DATA_PATH_ID);
      
      assertThat(actualString, equalTo(originalString));
      assertThat(actualInt, equalTo(originalInt));
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
      userSf.getWorkflowService().startProcess("N/A", null, true);
   }
   
   /**
    * <p>
    * Tests whether aborting a process instance works correctly.
    * </p>
    */
   @Test
   public void testAbortProcessInstance() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      final ProcessInstance abortedPi = adminSf.getWorkflowService().abortProcessInstance(pi.getOID(), AbortScope.SubHierarchy);
      assertThat(abortedPi.getState(), isOneOf(ProcessInstanceState.Aborting, ProcessInstanceState.Aborted));
      ProcessInstanceStateBarrier.instance().await(abortedPi.getOID(), ProcessInstanceState.Aborted);
   }
   
   /**
    * <p>
    * Tests whether the process instance abortion throws the correct exception
    * when the process instance cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testAbortProcessInstanceFailProcessInstanceNotFound()
   {
      userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      userSf.getWorkflowService().abortProcessInstance(-1, AbortScope.SubHierarchy);
   }

   /**
    * <p>
    * Tests whether the process instance abortion throws the correct exception
    * when the user has insufficient grants.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testAbortProcessInstanceFailInsufficientGrants()
   {
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      userSf.getWorkflowService().abortProcessInstance(pi.getOID(), AbortScope.SubHierarchy);
   }
   
   /**
    * <p>
    * Tests whether retrieving the process instance works correctly.
    * </p>
    */
   @Test
   public void testGetProcessInstance()
   {
      final ProcessInstance originalPi = userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      final ProcessInstance retrievedPi = userSf.getWorkflowService().getProcessInstance(originalPi.getOID());
      
      assertThat(retrievedPi, equalTo(originalPi));
   }
   
   /**
    * <p>
    * Tests whether the process instance abortion throws the correct exception
    * when the process instance cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testGetProcessInstanceFailProcessInstanceNotFound()
   {
      userSf.getWorkflowService().getProcessInstance(-1);
   }

   /**
    * <p>
    * Tests whether the retrieval of the startable process definitions for a user
    * holding the 'Default Role' grant works correctly.
    * </p>
    */
   @Test
   public void testGetStartableProcessDefinitionsForDefaultRole()
   {
      final List<ProcessDefinition> userPds = userSf.getWorkflowService().getStartableProcessDefinitions();
      
      assertThat(userPds.size(), is(1));
      final ProcessDefinition pd = userPds.get(0);
      assertThat(pd.getId(), is(PD_3_ID));
   }

   /**
    * <p>
    * Tests whether the retrieval of the startable process definitions for a user
    * holding the 'Administrator' grant works correctly.
    * </p>
    */
   @Test
   public void testGetStartableProcessDefinitionsForAdminRole()
   {
      final List<ProcessDefinition> adminPds = adminSf.getWorkflowService().getStartableProcessDefinitions();
      
      assertThat(adminPds.size(), is(1));
      final ProcessDefinition pd = adminPds.get(0);
      assertThat(pd.getId(), is(PD_2_ID));
   }
   
   /**
    * <p>
    * Tests whether setting of process instance attributes works correctly.
    * </p>
    */
   @Test
   public void testSetProcessInstanceAttributes()
   {
      final String testText = "This is a test";
      
      final ProcessInstance originalPi = userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      final ProcessInstanceAttributes originalAttributes = originalPi.getAttributes();
      originalAttributes.addNote(testText);
      userSf.getWorkflowService().setProcessInstanceAttributes(originalAttributes);
      
      final ProcessInstance retrievedPi = userSf.getWorkflowService().getProcessInstance(originalPi.getOID());
      final ProcessInstanceAttributes retrievedAttributes = retrievedPi.getAttributes();
      final List<Note> notes = retrievedAttributes.getNotes();
      assertThat(notes.size(), is(1));
      final Note note = notes.get(0);
      assertThat(note.getText(), equalTo(testText));
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the process instance cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testSetProcessInstanceAttributesFailProcessInstanceNotFound()
   {
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      final ProcessInstanceAttributes attributes = pi.getAttributes();
      adminSf.getAdministrationService().cleanupRuntime(true);
      
      userSf.getWorkflowService().setProcessInstanceAttributes(attributes);
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the process instance is not
    * a scope process instance.
    * </p>
    */
   @Test(expected = PublicException.class)
   public void testSetProcessInstanceAttributesFailNotScopeProcessInstance()
   {
      final String testText = "This is a test.";
      
      userSf.getWorkflowService().startProcess(PD_4_ID, null, true);
      final ProcessInstance pi = adminSf.getQueryService().findFirstProcessInstance(ProcessInstanceQuery.findForProcess(PD_1_ID));
      final ProcessInstanceAttributes attributes = pi.getAttributes();
      attributes.addNote(testText);
      userSf.getWorkflowService().setProcessInstanceAttributes(attributes);
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the process instance attribute
    * is <code>null</code>.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testSetProcessInstanceAttributesFailNullAttribute()
   {
      adminSf.getWorkflowService().setProcessInstanceAttributes(null);
   }
}
