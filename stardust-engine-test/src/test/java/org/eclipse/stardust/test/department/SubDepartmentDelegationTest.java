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

import static org.eclipse.stardust.test.department.DepartmentModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.DepartmentHome;
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
 * Tests delegation functionality regarding <i>Departments</i>,
 * especially the delegation to sub departments.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SubDepartmentDelegationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String USER_ID = "User";
   
   private Organization org1;
   private Organization org3;

   private Department deDept;
   private Department enDept;
   private Department deNorthDept;
   private Department deSouthDept;

   private ModelParticipantInfo org1de;
   private ModelParticipantInfo org1en;
   private ModelParticipantInfo org3deNorth;
   private ModelParticipantInfo org3deSouth;

   private ActivityInstance ai;

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf = new TestServiceFactory(new UsernamePasswordPair(USER_ID, USER_ID));
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf)
                                          .around(userSf);
   
   @Before
   public void setUp()
   {
      initAllOrgsAndRoles();
      createAllDepartments();
      createScopedParticipants();
      
      UserHome.create(adminSf, USER_ID, org1de, org1en, org3deNorth, org3deSouth);

      startProcess();
      ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAll());
   }

   /**
    * <p>
    * Assuming that
    * <ul>
    *    <li>there are two top level departments DE, EN and one department South of DE, North of DE and South of EN, North of EN</li>
    *    <li>the process P is started within scope (DE,South)</li>
    *    <li>a AI of P is assigned to the scoped top level department DE</li>
    * </ul>
    * delegating to the sub organization of department DE should result in a delegation to department South, without the need of
    * specifying this department explicitly.
    * </p>
    */
   @Test
   public void testDelegateToBoundSubDepartment()
   {
      final DepartmentInfo originalDept = getDepartmentFor(ai);
      assertEquals(deDept.getOID(), originalDept.getOID());

      ai = userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3);
      
      final DepartmentInfo targetDept = getDepartmentFor(ai);
      assertNotNull(targetDept);
      assertEquals(deSouthDept.getOID(), targetDept.getOID());
   }

   /**
    * <p>
    * Assuming that
    * <ul>
    *    <li>there are two top level departments DE, EN and one department South of DE, North of DE and South of EN, North of EN</li>
    *    <li>the process P is started within scope (DE,South)</li>
    *    <li>a AI of P is assigned to the scoped top level department DE</li>
    * </ul>
    * delegating to the identical department DE should be possible without having the permission to delegateToDepartment in a
    * delegation to department South, without the need of specifying this department explicitly.
    * </p>
    */
   @Test
   public void testDelegateToIdenticalDepartment()
   {
      final DepartmentInfo originalDept = getDepartmentFor(ai);
      assertEquals(deDept.getOID(), originalDept.getOID());

      ai = userSf.getWorkflowService().delegateToParticipant(ai.getOID(), ai.getCurrentPerformer());

      final DepartmentInfo targetDept = getDepartmentFor(ai);
      assertNotNull(targetDept);
      assertEquals(deDept.getOID(), targetDept.getOID());
   }

   /**
    * <p>
    * Assuming that
    * <ul>
    *    <li>there are two top level departments DE, EN and one department South of DE, North of DE and South of EN, North of EN</li>
    *    <li>the process P is started within scope (DE,South)</li>
    *    <li>a AI of P is assigned to the scoped top level department DE</li>
    * </ul>
    * delegating to the sub organization of department DE should result in a delegation to department South, without the need of
    * specifying this department explicitly.
    * </p>
    */
   @Test
   public void testDelegateToBoundSubDepartmentUsingParticpantID()
   {
      final DepartmentInfo originalDept = getDepartmentFor(ai);
      assertEquals(deDept.getOID(), originalDept.getOID());

      ai = userSf.getWorkflowService().delegateToParticipant(ai.getOID(), ORG3_ID);

      final DepartmentInfo targetDept = getDepartmentFor(ai);
      assertNotNull(targetDept);
      assertEquals(deSouthDept.getOID(), targetDept.getOID());
   }

   /**
    * <p>
    * Assuming that
    * <ul>
    *    <li>there are two top level departments DE, EN and one department South of DE, North of DE and South of EN, North of EN</li>
    *    <li>the process P is started within scope (DE,South)</li>
    *    <li>a AI of P is assigned to the scoped top level department DE</li>
    * </ul>
    * delegating to the (org3,deNorth) should result in a delegation to department North.
    * </p>
    */
   @Test
   public void testDelegateToBoundSubDepartmentUsingParticpantInfo()
   {
      final DepartmentInfo originalDept = getDepartmentFor(ai);
      assertEquals(deDept.getOID(), originalDept.getOID());

      ai = userSf.getWorkflowService().delegateToParticipant(ai.getOID(), deNorthDept.getScopedParticipant(org3));

      final DepartmentInfo targetDept = getDepartmentFor(ai);
      assertNotNull(targetDept);
      assertEquals(deNorthDept.getOID(), targetDept.getOID());
   }

   /**
    * <p>
    * Assuming that
    * <ul>
    *    <li>there are two top level departments DE, EN and one sub department South of DE</li>
    *    <li>the process P is started within scope (DE,South)</li>
    *    <li>a AI of P is assigned to the scoped top level department DE</li>
    * </ul>
    * delegating to the sub organization of department EN (after delegating to EN) should result in a delegation to the null sub
    * department of EN, because the value of the relevant process variable (DE) differs from the relevant department of the ai
    * (EN).
    * </p>
    */
   @Test
   public void testDelegateToUnboundSubDepartment()
   {
      final DepartmentInfo originalDept = getDepartmentFor(ai);
      assertEquals(deDept.getOID(), originalDept.getOID());

      final User user = userSf.getUserService().getUser();
      UserHome.addGrants(adminSf, user, DepartmentModelConstants.ROLE_ADMIN_ID);
      ai = userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1en);
      ai = userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3);

      final DepartmentInfo targetDept = getDepartmentFor(ai);
      assertNotNull(targetDept);
      assertEquals(enDept.getOID(), targetDept.getOID());
   }

   private void initAllOrgsAndRoles()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      org1 = model.getOrganization(ORG1_ID);
      org3 = model.getOrganization(ORG3_ID);
   }

   private void createAllDepartments()
   {
      deDept = DepartmentHome.create(adminSf, DEPT_ID_DE, ORG1_ID, null);
      enDept = DepartmentHome.create(adminSf, DEPT_ID_EN, ORG1_ID, null);

      deNorthDept = DepartmentHome.create(adminSf, SUB_DEPT_ID_NORTH, ORG3_ID, deDept);
      deSouthDept = DepartmentHome.create(adminSf, SUB_DEPT_ID_SOUTH, ORG3_ID, deDept);
   }

   private void createScopedParticipants()
   {
      org1de = deDept.getScopedParticipant(org1);
      org1en = enDept.getScopedParticipant(org1);

      org3deNorth = deNorthDept.getScopedParticipant(org3);
      org3deSouth = deSouthDept.getScopedParticipant(org3);
   }

   private void startProcess()
   {
      /* start process in scope "(DE,South)" */
      final Map<String, String> processData = new HashMap<String, String>();
      processData.put(X_SCOPE, DEPT_ID_DE);
      processData.put(Y_SCOPE, SUB_DEPT_ID_SOUTH);

      userSf.getWorkflowService().startProcess(PROCESS_ID_3, processData, true);
   }

   private DepartmentInfo getDepartmentFor(final ActivityInstance ai)
   {
      return ((ModelParticipantInfoDetails) ai.getCurrentPerformer()).getDepartment();
   }
}
