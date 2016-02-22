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
package org.eclipse.stardust.test.authorization;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PREDEFINED_MODEL_ID;
import static org.eclipse.stardust.engine.api.query.DeployedModelQuery.findForId;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests functionality regarding the auditor role authorization.
 * </p>
 * 
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class AuditorRoleAuthorizationTest
{
   private static final String MODEL_NAME = "AuthorizationModel";

   private static final String AUDITOR_ROLE_USER_ID = "auditor_user";
   private static final String AUDITOR_ROLE_USER_ID2 = "auditor_user2";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   private TestServiceFactory auditorUserSf = new TestServiceFactory(
         new UsernamePasswordPair(AUDITOR_ROLE_USER_ID, AUDITOR_ROLE_USER_ID));
   private TestServiceFactory auditorUser2Sf = new TestServiceFactory(
         new UsernamePasswordPair(AUDITOR_ROLE_USER_ID2, AUDITOR_ROLE_USER_ID2));

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
         .around(serviceFactory).around(auditorUserSf).around(auditorUser2Sf);

   private DocumentManagementService adminDMS;

   private WorkflowService adminWorkflowService;

   private DocumentManagementService auditorDMS;

   private WorkflowService auditorWorkflowService;

   private AdministrationService auditorAdminService;

   private AdministrationService adminService;

   @Before
   public void setUp()
   {
      adminDMS = serviceFactory.getDocumentManagementService();
      adminWorkflowService = serviceFactory.getWorkflowService();
      adminService = serviceFactory.getAdministrationService();
      QueryService queryService = serviceFactory.getQueryService();
      Models models = queryService.getModels(findForId(PREDEFINED_MODEL_ID));
      Participant auditorParticipant = queryService.getParticipant(models.get(0)
            .getModelOID(), PredefinedConstants.AUDITOR_ROLE);
      models = queryService.getModels(findForId(MODEL_NAME));
      Participant role1Participant = queryService.getParticipant(models.get(0)
            .getModelOID(), "Role1");
      UserHome.create(serviceFactory, AUDITOR_ROLE_USER_ID,
            (ModelParticipantInfo) auditorParticipant);      
      UserHome.create(serviceFactory, AUDITOR_ROLE_USER_ID2,
            (ModelParticipantInfo) auditorParticipant, (ModelParticipantInfo) role1Participant);
            
      auditorDMS = auditorUserSf.getDocumentManagementService();
      auditorWorkflowService = auditorUserSf.getWorkflowService();
      auditorAdminService = auditorUserSf.getAdministrationService();
   }

   @Test
   public void testSGlobalPermissions()
   {
      AdministrationService adminService = serviceFactory.getAdministrationService();
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();
      adminService.setGlobalPermissions(globalPermissions);
   }

   @Test
   public void testCreateDocumentWithAuditorUser()
   {
      adminDMS.removeFolder("/", true);
      DocumentInfo doc = new DmsDocumentBean();
      Object META_STRING = null;

      byte[] content;

      // version 0 (unversioned)
      doc.setContentType("text/plain");
      doc.setName("test.txt");
      doc.setDescription("testFile");

      doc.setProperties(Collections.singletonMap("name", META_STRING));
      content = "abc".getBytes();

      exception.expect(AccessForbiddenException.class);
      auditorDMS.createDocument("/", doc, content, "");
   }

   @Test
   public void testStartProcessWithAuditorUser()
   {
      exception.expect(AccessForbiddenException.class);
      auditorWorkflowService.startProcess("SourceProcess", null, true);
   }

   @Test
   public void testStartProcessWithAuditorUserRole1()
   {
      exception.expect(AccessForbiddenException.class);      
      ProcessInstance rootPI1 = auditorUser2Sf.getWorkflowService().startProcess("Role1Process", null,
            true);
   }
      
   @Test
   public void testAbortProcessWithAuditorUser()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);

      exception.expect(AccessForbiddenException.class);
      auditorWorkflowService.abortProcessInstance(rootPI1.getOID(),
            AbortScope.RootHierarchy);
   }

   @Test
   public void testDeleteProcessWithAuditorUser()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);
      adminService.abortProcessInstance(rootPI1.getOID());

      exception.expect(AccessForbiddenException.class);
      auditorAdminService.deleteProcesses(Collections.singletonList(new Long(rootPI1
            .getOID())));
   }

   @Test
   public void testModifyProcessInstanceAuditorUser()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);
      adminWorkflowService.activateNextActivityInstanceForProcessInstance(rootPI1
            .getOID());

      exception.expect(AccessForbiddenException.class);
      auditorAdminService.setProcessInstancePriority(rootPI1.getOID(),
            ProcessInstancePriority.HIGH);
   }

   @Test
   public void testModifyProcessInstanceAttributes()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);
      adminWorkflowService.activateNextActivityInstanceForProcessInstance(rootPI1
            .getOID());
      ProcessInstanceAttributes attributes = rootPI1.getAttributes();
      attributes.addNote("Test note 1");

      exception.expect(AccessForbiddenException.class);
      auditorWorkflowService.setProcessInstanceAttributes(attributes);
   }

   @Test
   public void testAbortAndStartAuditorUser()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);
      adminWorkflowService.activateNextActivityInstanceForProcessInstance(rootPI1
            .getOID());

      exception.expect(AccessForbiddenException.class);
      auditorWorkflowService.spawnPeerProcessInstance(rootPI1.getOID(), "TargetProcess",
            SpawnOptions.DEFAULT);
   }

   @Test
   public void testSpawnProcessAuditorUser()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);
      adminWorkflowService.activateNextActivityInstanceForProcessInstance(rootPI1
            .getOID());

      exception.expect(AccessForbiddenException.class);
      auditorWorkflowService.spawnSubprocessInstance(rootPI1.getOID(), "TargetProcess",
            false, null);
   }

   @Test
   public void testJoinProcessAuditorUser()
   {
      ProcessInstance rootPI1 = adminWorkflowService.startProcess("SourceProcess", null,
            true);
      ProcessInstance rootPI2 = adminWorkflowService.startProcess("TargetProcess", null,
            true);
      adminWorkflowService.activateNextActivityInstanceForProcessInstance(rootPI1
            .getOID());
      adminWorkflowService.activateNextActivityInstanceForProcessInstance(rootPI2
            .getOID());

      exception.expect(AccessForbiddenException.class);
      auditorWorkflowService.joinProcessInstance(rootPI1.getOID(), rootPI2.getOID(),
            "Join Source and Taget Process");
   }

}
