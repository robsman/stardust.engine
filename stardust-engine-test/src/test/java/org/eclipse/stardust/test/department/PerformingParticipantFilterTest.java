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
import static org.eclipse.stardust.test.department.DepartmentModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
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
 * Tests the user query filter
 * {@link org.eclipse.stardust.engine.api.query.PerformingParticipantFilter}
 * wrt. the <i>Department</i> functionality.
 * </p>
 * 
 * <p>
 * <table border=1>
 *    <tr>
 *       <th>Process</th>
 *       <th>Participant</th>
 *       <th>Department</th>
 *    </tr>
 *    <tr>
 *       <td>3</td>
 *       <td>Org1</td>
 *       <td>u</td>
 *    </tr>
 *    <tr>
 *       <td>3</td>
 *       <td>Org1</td>
 *       <td>v</td>
 *    </tr>
 *    <tr>
 *       <td>4</td>
 *       <td>Org2</td>
 *       <td>u</td>
 *    </tr>
 *    <tr>
 *       <td>4</td>
 *       <td>Org2</td>
 *       <td>u</td>
 *    </tr>
 *    <tr>
 *       <td>5</td>
 *       <td>Org3</td>
 *       <td>u,i</td>
 *    </tr>
 *    <tr>
 *       <td>5</td>
 *       <td>Org3</td>
 *       <td>u,j</td>
 *    </tr>
 *    <tr>
 *       <td>5</td>
 *       <td>Org3</td>
 *       <td>v,k</td>
 *    </tr>
 *    <tr>
 *       <td>6</td>
 *       <td>Role1</td>
 *       <td>u</td>
 *    </tr>
 *    <tr>
 *       <td>6</td>
 *       <td>Role1</td>
 *       <td>v</td>
 *    </tr>
 * </table>
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class PerformingParticipantFilterTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String USER_ID = "User";
   
   private static final String DEP_ID_U = "u";
   private static final String DEP_ID_V = "v";
   
   private static final String SUB_DEP_ID_I = "i";
   private static final String SUB_DEP_ID_J = "j";
   private static final String SUB_DEP_ID_K = "k";
   
   private Organization org1;
   private Organization org2;
   private Organization org3;
   private Role role1;
   
   private Department uDep;
   private Department vDep;
   private Department uiDep;
   private Department ujDep;
   private Department vkDep;
   
   private ModelParticipantInfo org1u;
   private ModelParticipantInfo org1v;
   private ModelParticipantInfo org2u;
   private ModelParticipantInfo org2v;
   private ModelParticipantInfo role1u;
   private ModelParticipantInfo role1v;
   private ModelParticipantInfo org3ui;
   private ModelParticipantInfo org3uj;
   private ModelParticipantInfo org3vk;
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf = new TestServiceFactory(new UsernamePasswordPair(USER_ID, USER_ID));
   
   @ClassRule
   public static LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, MODEL_NAME);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(testMethodSetup)
                                    .around(adminSf)
                                    .around(userSf);
   
   @Before
   public void setUp()
   {
      createAllOrgsAndRoles();
      createAllDepartments();
      createScopedParticipants();

      UserHome.create(adminSf, USER_ID, ORG1_ID);
      createActivityInstances();
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg1u()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org1u);
      Assert.assertEquals(1, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg1v()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org1v);
      assertEquals(1, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg2u()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org2u);
      assertEquals(3, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg2v()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org2v);
      assertEquals(1, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg3ui()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org3ui);
      assertEquals(2, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg3uj()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org3uj);
      assertEquals(2, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg3vk()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(org3vk);
      assertEquals(2, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>
    */
   @Test
   public void testFilterForParticipantRole1u()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(role1u);
      assertEquals(4, getActivityInstancesCount(filter));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found for a particular
    * scoped participant.
    * </p>  
    */
   @Test
   public void testFilterForParticipantRole1v()
   {
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(role1v);
      assertEquals(2, getActivityInstancesCount(filter));
   }

   /**
    * <p>
    * This method tests whether all activity instances are found that match both
    * given scoped participants.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg3uiAndOrg3vk()
   {
      final PerformingParticipantFilter f1 = PerformingParticipantFilter.forParticipant(org3ui);
      final PerformingParticipantFilter f2 = PerformingParticipantFilter.forParticipant(org2u);
      assertEquals(1, getActivityInstancesCountAndTerm(f1, f2));
   }
   
   /**
    * <p>
    * This method tests whether all activity instances are found that match one of
    * the given scoped participants.
    * </p>
    */
   @Test
   public void testFilterForParticipantOrg3uiOrOrg3vk()
   {
      final PerformingParticipantFilter f1 = PerformingParticipantFilter.forParticipant(org3ui);
      final PerformingParticipantFilter f2 = PerformingParticipantFilter.forParticipant(org3vk);
      assertEquals(4, getActivityInstancesCountOrTerm(f1, f2));
   }
   
   private void createAllOrgsAndRoles()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      org1 = model.getOrganization(ORG1_ID);
      org2 = model.getOrganization(ORG2_ID);
      org3 = model.getOrganization(ORG3_ID);
      role1 = model.getRole(ROLE1_ID);
   }
   
   private void createAllDepartments()
   {
      uDep = adminSf.getAdministrationService().createDepartment(DEP_ID_U, DEP_ID_U, null, null, org1);
      vDep = adminSf.getAdministrationService().createDepartment(DEP_ID_V, DEP_ID_V, null, null, org1);
      
      uiDep = adminSf.getAdministrationService().createDepartment(SUB_DEP_ID_I, SUB_DEP_ID_I, null, uDep, org3);
      ujDep = adminSf.getAdministrationService().createDepartment(SUB_DEP_ID_J, SUB_DEP_ID_J, null, uDep, org3);
      vkDep = adminSf.getAdministrationService().createDepartment(SUB_DEP_ID_K, SUB_DEP_ID_K, null, vDep, org3);
   }
   
   private void createScopedParticipants()
   {
      org1u = uDep.getScopedParticipant(org1);
      org1v = vDep.getScopedParticipant(org1);
      
      org2u = uDep.getScopedParticipant(org2);
      org2v = vDep.getScopedParticipant(org2);
      
      role1u = uDep.getScopedParticipant(role1);
      role1v = vDep.getScopedParticipant(role1);
      
      org3ui = uiDep.getScopedParticipant(org3);
      org3uj = ujDep.getScopedParticipant(org3);
      org3vk = vkDep.getScopedParticipant(org3);
   }
   
   private void createActivityInstances()
   {
      /* process 3 */
      startProcess(PROCESS_ID_3, DEP_ID_U, null);
      startProcess(PROCESS_ID_3, DEP_ID_V, null);
      
      /* process 4 */
      startProcess(PROCESS_ID_4, DEP_ID_U, SUB_DEP_ID_I);
      startProcess(PROCESS_ID_4, DEP_ID_U, SUB_DEP_ID_J);
      
      /* process 5 */
      startProcess(PROCESS_ID_5, DEP_ID_U, SUB_DEP_ID_I);
      startProcess(PROCESS_ID_5, DEP_ID_U, SUB_DEP_ID_J);
      startProcess(PROCESS_ID_5, DEP_ID_V, SUB_DEP_ID_K);
      
      /* process 6 */
      startProcess(PROCESS_ID_6, DEP_ID_U, SUB_DEP_ID_I);
      startProcess(PROCESS_ID_6, DEP_ID_V, SUB_DEP_ID_K);
   }
   
   private void startProcess(final String processId, final String deptID, final String subDepId)
   {
      final Map<String, String> piData = new HashMap<String, String>();
      piData.put(X_SCOPE, deptID);
      if (subDepId != null)
      {
         piData.put(Y_SCOPE, subDepId);
      }
      userSf.getWorkflowService().startProcess(processId, piData, true);
   }
   
   private long getActivityInstancesCount(final FilterCriterion filter)
   {
      final ActivityInstanceQuery ai = new ActivityInstanceQuery();
      ai.where(filter);
      return userSf.getQueryService().getActivityInstancesCount(ai);
   }
   
   private long getActivityInstancesCountAndTerm(final FilterCriterion f1, final FilterCriterion f2)
   {
      final ActivityInstanceQuery ai = new ActivityInstanceQuery();
      ai.getFilter().addAndTerm().add(f1).add(f2);
      return userSf.getQueryService().getActivityInstancesCount(ai);
   }
   
   private long getActivityInstancesCountOrTerm(final FilterCriterion f1, final FilterCriterion f2)
   {
      final ActivityInstanceQuery ai = new ActivityInstanceQuery();
      ai.getFilter().addOrTerm().or(f1).or(f2);
      return userSf.getQueryService().getActivityInstancesCount(ai);
   }
}
