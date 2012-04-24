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
package org.eclipse.stardust.test.department;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.test.api.junit.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UserHome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests delegation functionality regarding <i>Departments</i>,
 * especially wrt. <i>Declarative Security</i>.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DeclarativeSecurityDelegationTest extends LocalJcrH2Test
{
   private static final String USER_ID = "User";
   
   /* no permission to delegate */
   private static final String NONE_USERNAME = "none";

   /* 'delegation to others' permission */
   private static final String DTO_USERNAME = "dto";
   
   /* 'delegation to department' permission */
   private static final String DTD_USERNAME = "dtd";

   private Organization org1;
   private Organization org2;
   private Organization org3;

   private Role role1;
   private Role noneRole;
   private Role dtoRole;
   private Role dtdRole;

   private Department uDept;
   private Department vDept;
   private Department uiDept;
   private Department ujDept;
   private Department vkDept;

   private ModelParticipantInfo org1u;
   private ModelParticipantInfo org1v;
   private ModelParticipantInfo org2u;
   private ModelParticipantInfo org2v;
   private ModelParticipantInfo role1u;
   private ModelParticipantInfo role1v;
   private ModelParticipantInfo org3ui;
   private ModelParticipantInfo org3uj;
   private ModelParticipantInfo org3vk;

   private ActivityInstance ai;

   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_ID, USER_ID);
   private final ClientServiceFactory noneSf = new ClientServiceFactory(NONE_USERNAME, NONE_USERNAME);
   private final ClientServiceFactory dtoSf = new ClientServiceFactory(DTO_USERNAME, DTO_USERNAME);
   private final ClientServiceFactory dtdSf = new ClientServiceFactory(DTD_USERNAME, DTD_USERNAME);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf)
                                    .around(noneSf)
                                    .around(dtoSf)
                                    .around(dtdSf);
   
   @Before
   public void setUp()
   {
      UserHome.create(adminSf, USER_ID, ORG3_ID);
      
      initAllOrgsAndRoles();
      createAllDepartments();
      createScopedParticipants();
      
      UserHome.create(adminSf, NONE_USERNAME, noneRole, org1u, org1v, org2u, org2v, role1u, role1v, org3ui, org3uj, org3vk);
      UserHome.create(adminSf, DTO_USERNAME, dtoRole, org1u, org1v, org2u, org2v, role1u, role1v, org3ui, org3uj, org3vk);
      UserHome.create(adminSf, DTD_USERNAME, dtdRole, org1u, org1v, org2u, org2v, role1u, role1v, org3ui, org3uj, org3vk);

      startProcess();
      ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAll());
   }

   /**
    * <p>
    * If the permissions are insufficient, its's always prohibited
    * (regardless of the call parameters) to call 'delegateToParticipant()'.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testDelegateToParticipantUnsufficientPermission()
   {
      noneSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * If the permissions are insufficient, its's always prohibited
    * (regardless of the call parameters) to call 'suspendToParticipant()'.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendToParticipantUnsufficientPermission()
   {
      noneSf.getWorkflowService().suspendToParticipant(ai.getOID(), org1u, null);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests delegation to "Org1" in scope "u" with declarative security grant "Delegation to other users" (should work). Delegation 
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg1u()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests delegation to "Org1" in scope "v" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg1v()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1v);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests delegation to "Org2" in scope "u" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg2u()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org2u);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests delegation to "Org2" in scope "v" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg2v()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org2v);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests delegation to "Role1" in scope "u" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToOtherUsersPermissionRole1u()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), role1u);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests delegation to "Role1" in scope "v" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testDelegateToParticipantDelegateToOtherUsersPermissionRole1v()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), role1v);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests delegation to "Org3" in scope "(u,i)" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg3ui()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3ui);
      ensureDelegationWasSuccessful(uiDept);
   }

   /**
    * <p>
    * Tests delegation to "Org3" in scope "(u,j)" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg3uj()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3uj);
      ensureDelegationWasSuccessful(ujDept);
   }

   /**
    * <p>
    * Tests delegation to "Org3" in scope "(v,k)" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testDelegateToParticipantDelegateToOtherUsersPermissionOrg3vk()
   {
      dtoSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3vk);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests suspending to "Org1" in scope "u" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg1u()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org1u, null);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests suspending to "Org1" in scope "v" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg1v()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org1v, null);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests suspending to "Org2" in scope "u" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg2u()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org2u, null);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests suspending to "Org2" in scope "v" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg2v()
   {
         dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org2v, null);
         fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests suspending to "Role1" in scope "u" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToOtherUsersPermissionRole1u()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), role1u, null);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests suspending to "Role1" in scope "v" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendToParticipantDelegateToOtherUsersPermissionRole1v()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), role1v, null);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests suspending to "Org3" in scope "(u,i)" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg3ui()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org3ui, null);
      ensureDelegationWasSuccessful(uiDept);
   }

   /**
    * <p>
    * Tests suspending to "Org3" in scope "(u,j)" with declarative security grant "Delegation to other users" (should work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg3uj()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org3uj, null);
      ensureDelegationWasSuccessful(ujDept);
   }

   /**
    * <p>
    * Tests suspending to "Org3" in scope "(v,k)" with declarative security grant "Delegation to other users" (shouldn't work). Delegation
    * is possible to participants within the subtree below "Org1", whenever the target combination of participant and department exists.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendToParticipantDelegateToOtherUsersPermissionOrg3vk()
   {
      dtoSf.getWorkflowService().suspendToParticipant(ai.getOID(), org3vk, null);
      fail("Delegation should not be allowed.");
   }

   /**
    * <p>
    * Tests delegation to "Org1" in scope "u" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg1u()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests delegation to "Org1" in scope "v" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */   
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg1v()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1v);
      ensureDelegationWasSuccessful(vDept);
   }

   /**
    * <p>
    * Tests delegation to "Org2" in scope "u" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg2u()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org2u);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests delegation to "Org2" in scope "v" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg2v()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org2v);
      ensureDelegationWasSuccessful(vDept);
   }

   /**
    * <p>
    * Tests delegation to "Role1" in scope "u" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionRole1u()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), role1u);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests delegation to "Role1" in scope "v" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionRole1v()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), role1v);
      ensureDelegationWasSuccessful(vDept);
   }

   /**
    * <p>
    * Tests delegation to "Org3" in scope "(u,i)" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg3ui()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3ui);
      ensureDelegationWasSuccessful(uiDept);
   }

   /**
    * <p>
    * Tests delegation to "Org3" in scope "(u,j)" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg3uj()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3uj);
      ensureDelegationWasSuccessful(ujDept);
   }

   /**
    * <p>
    * Tests delegation to "Org3" in scope "(v,k)" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testDelegateToParticipantDelegateToDepartmentPermissionOrg3vk()
   {
      dtdSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3vk);
      ensureDelegationWasSuccessful(vkDept);
   }

   /**
    * <p>
    * Tests suspending to "Org1" in scope "u" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg1u()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org1u, null);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests suspending to "Org1" in scope "v" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg1v()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org1v, null);
      ensureDelegationWasSuccessful(vDept);
   }

   /**
    * <p>
    * Tests suspending to "Org2" in scope "u" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg2u()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org2u, null);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests suspending to "Org2" in scope "v" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg2v()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org2v, null);
      ensureDelegationWasSuccessful(vDept);
   }

   /**
    * <p>
    * Tests suspending to "Role1" in scope "u" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionRole1u()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), role1u, null);
      ensureDelegationWasSuccessful(uDept);
   }

   /**
    * <p>
    * Tests suspending to "Role1" in scope "v" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionRole1v()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), role1v, null);
      ensureDelegationWasSuccessful(vDept);
   }

   /**
    * <p>
    * Tests suspending to "Org3" in scope "(u,i)" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg3ui()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org3ui, null);
      ensureDelegationWasSuccessful(uiDept);
   }

   /**
    * <p>
    * Tests suspending to "Org3" in scope "(u,j)" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg3uj()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org3uj, null);
      ensureDelegationWasSuccessful(ujDept);
   }

   /**
    * <p>
    * Tests suspending to "Org3" in scope "(v,k)" with declarative security grant "Delegation to Department" (should work). Delegation
    * is possible to participants within the subtree below "Org1", if and only if the delegation target department is for the Model Participant.
    * </p>
    */
   @Test
   public void testSuspendToParticipantDelegateToDepartmentPermissionOrg3vk()
   {
      dtdSf.getWorkflowService().suspendToParticipant(ai.getOID(), org3vk, null);
      ensureDelegationWasSuccessful(vkDept);
   }

   private void initAllOrgsAndRoles()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      
      org1 = model.getOrganization(ORG1_ID);
      org2 = model.getOrganization(ORG2_ID);
      org3 = model.getOrganization(ORG3_ID);
      
      role1 = model.getRole(ROLE1_ID);
      noneRole = model.getRole(NONE_ROLE_ID);
      dtoRole = model.getRole(DTO_ROLE_ID);
      dtdRole = model.getRole(DTD_ROLE_ID);
   }

   private void createAllDepartments()
   {
      uDept = DepartmentHome.create(DEPT_ID_U, ORG1_ID, null, adminSf);
      vDept = DepartmentHome.create(DEPT_ID_V, ORG1_ID, null, adminSf);
      
      uiDept = DepartmentHome.create(SUB_DEPT_ID_I, ORG3_ID, uDept, adminSf);      
      ujDept = DepartmentHome.create(SUB_DEPT_ID_J, ORG3_ID, uDept, adminSf);
      
      vkDept = DepartmentHome.create(SUB_DEPT_ID_K, ORG3_ID, vDept, adminSf);
   }

   private void createScopedParticipants()
   {
      org1u = uDept.getScopedParticipant(org1);
      org1v = vDept.getScopedParticipant(org1);

      org2u = uDept.getScopedParticipant(org2);
      org2v = vDept.getScopedParticipant(org2);

      role1u = uDept.getScopedParticipant(role1);
      role1v = vDept.getScopedParticipant(role1);

      org3ui = uiDept.getScopedParticipant(org3);
      org3uj = ujDept.getScopedParticipant(org3);
      org3vk = vkDept.getScopedParticipant(org3);
   }

   private void startProcess()
   {
      /* start process in scope "(u,j)" */
      final Map<String, String> processData = new HashMap<String, String>();
      processData.put(X_SCOPE, DEPT_ID_U);
      processData.put(Y_SCOPE, SUB_DEPT_ID_J);

      userSf.getWorkflowService().startProcess(PROCESS_ID_7, processData, true);
   }

   private void ensureDelegationWasSuccessful(final Department expectedDept)
   {
      final ActivityInstance ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAll());
      final DepartmentInfo actualDept = ((ModelParticipantInfo) ai.getCurrentPerformer()).getDepartment();

      assertEquals(expectedDept.getOID(), actualDept.getOID());
   }
}
