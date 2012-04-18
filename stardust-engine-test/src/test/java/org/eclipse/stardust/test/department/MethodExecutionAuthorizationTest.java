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

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.eclipse.stardust.test.api.util.UserHome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether declarative security rules for method
 * execution depending and the algorithm described in
 * chapter "Department Evaluation" of the functional
 * specification are implemented correctly, i.e. method
 * execution should only be possible if the performing
 * user is part of the sub hierarchy of the target
 * participant (always true in this test case) and has
 * either a grant on the <i>target-department</i> or a
 * grant on a sub department of <i>target-department</i>.
 * </p>
 * 
 * <p>
 * <table border="1">
 *    <tr>
 *       <th>No.</th>
 *       <th><i>reference-department</i></th>
 *       <th><i>reference-organization</i></th>
 *       <th>process variables</th>
 *       <th><i>target-participant</i></th>
 *       <th><i>target-organization</i></th>
 *       <th><i>target-department</i></th>
 *       <th>grants allowed</th>
 *       <th>grants NOT allowed</th>
 *    </tr>
 *    <tr>
 *       <td>1</td>
 *       <td>(u)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(u,i)</td>
 *       <td>Org3&lt;u,i&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5&lt;u,i&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;v,null,null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,j&gt;, Org5&lt;v,null&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>2</td>
 *       <td>(u,i)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(u,i)</td>
 *       <td>Org3&lt;u,i&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5&lt;u,i&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;v,null,null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,j&gt;, Org5&lt;v,null&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>3</td>
 *       <td>(u,i,m)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(u,i)</td>
 *       <td>Org3&lt;u,i&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5&lt;u,i&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;v,null,null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,j&gt;, Org5&lt;v,null&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>4</td>
 *       <td>(u)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>5</td>
 *       <td>(u,i)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>6</td>
 *       <td>(u,i,m)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>7</td>
 *       <td>(v)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(v,null)</td>
 *       <td>Org3&lt;v,null&gt;, Org4&lt;v,null,null&gt;, Org5&lt;v,null&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;u,i&gt;, Org3&lt;v,j&gt;, Org4&lt;null&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org4&lt;u,null,null&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;u,i&gt;, Org5&lt;v,j&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>8</td>
 *       <td>(v,j)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(v,j)</td>
 *       <td>Org3&lt;v,j&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5&lt;v,j&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;u,i&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org4&lt;v,null,null&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,null&gt;, Org5&lt;u,i&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>9</td>
 *       <td>(v,j,n)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Org3</td>
 *       <td>Org3</td>
 *       <td>(v,j)</td>
 *       <td>Org3&lt;v,j&gt;, Org4&lt;v,j,null&gt;, Org4&lt;v,j,n&gt;, Org5&lt;v,j&gt;</td>
 *       <td>Org3&lt;null&gt;, Org3&lt;u,null&gt;, Org3&lt;v,null&gt;, Org3&lt;u,i&gt;, Org4&lt;null&gt;, Org4&lt;u,null,null&gt;, Org4&lt;u,i,null&gt;, Org4&lt;u,i,m&gt;, Org4&lt;v,null,null&gt;, Org5, Org5&lt;u,null&gt;, Org5&lt;v,null&gt;, Org5&lt;u,i&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>10</td>
 *       <td>(v)</td>
 *       <td>Org1</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>11</td>
 *       <td>(v,j)</td>
 *       <td>Org3</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 *    <tr>
 *       <td>12</td>
 *       <td>(v,j,n)</td>
 *       <td>Org4</td>
 *       <td>(u,i,m,a)</td>
 *       <td>Reader</td>
 *       <td>Reader</td>
 *       <td>(a)</td>
 *       <td>Reader&lt;a&gt;</td>
 *       <td>Reader&lt;null&gt;, Reader&lt;b&gt;</td>
 *    </tr>
 * </table>
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MethodExecutionAuthorizationTest extends LocalJcrH2Test
{
   private static final String USER_ID = "User";
   
   private Role admin;
   
   private Organization org1;
   private Organization org3;
   private Organization org4;
   private Organization org5;
   private Organization readerOrg;
   
   private Department deptU;
   private Department deptV;
   private Department deptUi;
   private Department deptVj;
   private Department deptUim;
   private Department deptVjn;
   private Department deptA;
   private Department deptB;
   
   private ModelParticipantInfo org1u;
   private ModelParticipantInfo org1v;
   private ModelParticipantInfo org3ui;
   private ModelParticipantInfo org3uNull;
   private ModelParticipantInfo org3vj;
   private ModelParticipantInfo org3vNull;
   private ModelParticipantInfo org5ui;
   private ModelParticipantInfo org5uNull;
   private ModelParticipantInfo org5vj;
   private ModelParticipantInfo org5vNull;
   private ModelParticipantInfo org4uim;
   private ModelParticipantInfo org4uiNull;
   private ModelParticipantInfo org4uNullNull;
   private ModelParticipantInfo org4vjn;
   private ModelParticipantInfo org4vjNull;
   private ModelParticipantInfo org4vNullNull;
   private ModelParticipantInfo readerOrgA;
   private ModelParticipantInfo readerOrgB;
   
   private ActivityInstance ai;

   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_ID, USER_ID);

   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf);
   
   @Before
   public void setUp()
   {
      createOrgsAndDepartments();
      createScopedParticipants();
      
      UserHome.create(adminSf, USER_ID, org1u, org1v, org3ui, org4uim, org3vj, org4vjn, admin);

      startProcess();
      ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAll());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase1()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3vj, org4, org4uNullNull, org4vNullNull, org4vjNull, org4vjn, org5, org5uNull, org5vj, org5vNull };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3ui, org4uiNull, org4uim, org5ui };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase2()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3ui);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3vj, org4, org4uNullNull, org4vNullNull, org4vjNull, org4vjn, org5, org5uNull, org5vj, org5vNull };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3ui, org4uiNull, org4uim, org5ui };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase3()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4uim);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3vj, org4, org4uNullNull, org4vNullNull, org4vjNull, org4vjn, org5, org5uNull, org5vj, org5vNull };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3ui, org4uiNull, org4uim, org5ui };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase4()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1u);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase5()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3ui);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase6()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4uim);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }

   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase7()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1v);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3ui, org3vj, org4, org4vjNull, org4vjn, org4uNullNull, org4uiNull, org4uim, org5, org5uNull, org5ui, org5vj };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3vNull, org4vNullNull, org5vNull };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase8()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3vj);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3ui, org4, org4uNullNull, org4uiNull, org4uim, org4vNullNull, org5, org5uNull, org5vNull, org5ui };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3vj, org4vjNull, org4vjn, org5vj };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase9()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4vjn);
      
      final ModelParticipantInfo[] insufficientGrants = { org3, org3uNull, org3vNull, org3ui, org4, org4uNullNull, org4uiNull, org4uim, org4vNullNull, org5, org5uNull, org5vNull, org5ui };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { org3vj, org4vjNull, org4vjn, org5vj };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase10()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org1v);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase11()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org3vj);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   /**
    * see {@link ag.carnot.hydra.regression.runtime.query.authorization.scoped.MethodExecutionAuthorizationTest}}
    */
   @Test
   public void testCase12()
   {
      userSf.getWorkflowService().delegateToParticipant(ai.getOID(), org4vjn);
      
      final ModelParticipantInfo[] insufficientGrants = { readerOrg, readerOrgB };
      ensureReadActivityInstanceEventHandlerFailsFor(insufficientGrants, ai.getOID());

      final ModelParticipantInfo[] sufficientGrants = { readerOrgA };
      ensureReadActivityInstanceEventHandlerSucceedsFor(sufficientGrants, ai.getOID());
   }
   
   private void createOrgsAndDepartments()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      admin = model.getRole(ROLE_ADMIN_ID);
      org1 = model.getOrganization(ORG1_ID);
      org3 = model.getOrganization(ORG3_ID);
      org4 = model.getOrganization(ORG4_ID);
      org5 = model.getOrganization(ORG5_ID);
      readerOrg = model.getOrganization(READER_ORG_ID);
      
      deptU = adminSf.getAdministrationService().createDepartment(DEPT_ID_U, DEPT_ID_U, null, null, org1);
      deptV = adminSf.getAdministrationService().createDepartment(DEPT_ID_V, DEPT_ID_V, null, null, org1);
      deptA = adminSf.getAdministrationService().createDepartment(DEP_ID_A, DEP_ID_A, null, null, readerOrg);
      deptB = adminSf.getAdministrationService().createDepartment(DEP_ID_B, DEP_ID_B, null, null, readerOrg);
      
      deptUi = adminSf.getAdministrationService().createDepartment(SUB_DEPT_ID_I, SUB_DEPT_ID_I, null, deptU, org3);
      deptUim = adminSf.getAdministrationService().createDepartment(SUB_SUB_DEP_ID_M, SUB_SUB_DEP_ID_M, null, deptUi, org4);
      
      deptVj = adminSf.getAdministrationService().createDepartment(SUB_DEPT_ID_J, SUB_DEPT_ID_J, null, deptV, org3);
      deptVjn = adminSf.getAdministrationService().createDepartment(SUB_SUB_DEP_ID_N, SUB_SUB_DEP_ID_N, null, deptVj, org4);
   }
   
   private void createScopedParticipants()
   {
      org1u = deptU.getScopedParticipant(org1);
      org1v = deptV.getScopedParticipant(org1);
      
      org3ui = deptUi.getScopedParticipant(org3);
      org3uNull = deptU.getScopedParticipant(org3);
      org3vj = deptVj.getScopedParticipant(org3);
      org3vNull = deptV.getScopedParticipant(org3);

      org5ui = deptUi.getScopedParticipant(org5);
      org5uNull = deptU.getScopedParticipant(org5);
      org5vj = deptVj.getScopedParticipant(org5);
      org5vNull = deptV.getScopedParticipant(org5);
      
      org4uim = deptUim.getScopedParticipant(org4);
      org4uiNull = deptUi.getScopedParticipant(org4);
      org4uNullNull = deptU.getScopedParticipant(org4);
      org4vjn = deptVjn.getScopedParticipant(org4);
      org4vjNull = deptVj.getScopedParticipant(org4);
      org4vNullNull = deptV.getScopedParticipant(org4);
      
      readerOrgA = deptA.getScopedParticipant(readerOrg);
      readerOrgB = deptB.getScopedParticipant(readerOrg);
   }
   
   private void startProcess()
   {
      /* start process in scope (u,i,m,a) */
      final Map<String, String> piData = new HashMap<String, String>();
      piData.put(X_SCOPE, DEPT_ID_U);
      piData.put(Y_SCOPE, SUB_DEPT_ID_I);
      piData.put(Z_SCOPE, SUB_SUB_DEP_ID_M);
      piData.put(A_SCOPE, DEP_ID_A);
      userSf.getWorkflowService().startProcess(PROCESS_ID_8, piData, true);
   }
   
   private void ensureReadActivityInstanceEventHandlerFailsFor(final ModelParticipantInfo[] grants, final long aiOID)
   {
      final User user = userSf.getWorkflowService().getUser();
      UserHome.removeAllGrants(adminSf, user);
      ensureReadActivityInstanceEventHandlerFailsInternal(null, aiOID);
      
      for (final ModelParticipantInfo grant : grants)
      {
         UserHome.removeAllGrants(adminSf, user);
         UserHome.addGrants(adminSf, user, grant);
         
         ensureReadActivityInstanceEventHandlerFailsInternal(grant, aiOID);
      }
   }

   private void ensureReadActivityInstanceEventHandlerFailsInternal(final ModelParticipantInfo grant, final long aiOID)
   {
      final StringBuffer sb = new StringBuffer("Attempt to read AI data should throw AccessForbiddenException for grant: ");
      sb.append(getDepartmentStringFor(grant) + " ... ");
      
      try
      {
         userSf.getWorkflowService().getActivityInstanceEventHandler(aiOID, ON_EXCEPTION_HANDLER_ID);
         Assert.fail("Read should fail due to insufficient permissions.");
      }
      catch (AccessForbiddenException e)
      {
         /* expected */
      }
      catch (RuntimeException e)
      {
         throw e;
      }
   }
   
   private void ensureReadActivityInstanceEventHandlerSucceedsFor(final ModelParticipantInfo[] grants, final long aiOID)
   {
      final User user = userSf.getWorkflowService().getUser();
      
      for (final ModelParticipantInfo grant : grants)
      {
         UserHome.removeAllGrants(adminSf, user);
         UserHome.addGrants(adminSf, user, grant);

         final StringBuffer sb = new StringBuffer("Attempt to read AI data should succeed for grant: ");
         sb.append(getDepartmentStringFor(grant) + " ... ");
         
         try
         {
            userSf.getWorkflowService().getActivityInstanceEventHandler(aiOID, ON_EXCEPTION_HANDLER_ID);
         }
         catch (AccessForbiddenException e)
         {
            throw e;
         }
      }
   }
   
   private String getDepartmentStringFor(final ModelParticipantInfo grant)
   {
      if (grant == null) return "<no grant>";
      
      final String orgStr = grant.getId();

      final DepartmentInfo deptInfo = grant.getDepartment();
      
      String deptStr;
      if (deptInfo != null)
      {
         final Department dept = adminSf.getAdministrationService().getDepartment(deptInfo.getOID());
         deptStr = getDepartmentId(dept);
      }
      else
      {
         deptStr = "null";
      }         
      
      return orgStr + "<" + deptStr + ">";
   }
   
   private String getDepartmentId(final Department department)
   {
      final Department parent = department.getParentDepartment(); 
      if (parent == null)
      {
         return department.getId();
      }
      else
      {
         final String parentStr = getDepartmentId(parent);
         return parentStr + (parentStr.length() > 0 ? "," : "") + department.getId();
      }
   }
}
