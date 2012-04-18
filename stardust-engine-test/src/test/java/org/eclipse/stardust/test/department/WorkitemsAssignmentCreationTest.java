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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.engine.api.dto.DepartmentDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2Test;
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
 * Tests whether the department evaluation algorithm
 * is implemented correctly.
 * </p>
 * 
 * <p>
 * This class focuses on the creation of work items.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class WorkitemsAssignmentCreationTest extends LocalJcrH2Test
{
   private static final String USER_ID = "User";
   
   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_ID, USER_ID);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf);
   
   private Organization org1;
   private Organization org2;
   private Organization org3;
   
   private Role role1;
   
   private Department deptDe;
   private Department deptDeNorth;
   
   private ModelParticipantInfo org1De;
   private ModelParticipantInfo org2De;
   private ModelParticipantInfo org3DeNorth;
   private ModelParticipantInfo role1De;
   
   @Before
   public void setUp()
   {
      initOrgsAndRoles();
      createDepts();
      createScopedParticipants();
      
      UserHome.create(adminSf, USER_ID, org1, org2, org3, role1, org1De, org2De, org3DeNorth, role1De);
   }
   
   /**
    * <p>
    * If the process is started with appropriate data for
    * department runtime binding, there should not be any
    * unscoped workitems for activity instances performed
    * by scoped organizations.
    * </p>
    */
   @Test
   public void testCreatedWorkitemIsScoped()
   {
      startProcess(PROCESS_ID_3);
      
      ensureNoUnscopedWorkitems();
   }
   
   /**
    * <p>
    * If created by "Org1", the <i>target-department</i> should
    * be "DE"
    * </p>
    */
   @Test
   public void testCreationOfWorkitemByOrg1()
   {
      startProcess(PROCESS_ID_3);
      
      ensureCorrectWorklistScopeId(deptDe.getId());
   }
   
   /**
    * <p>
    * If created by "Org2", the <i>target-department</i> should
    * be "DE".
    * </p>
    */
   @Test
   public void testCreationOfWorkitemByOrg2()
   {
      startProcess(PROCESS_ID_4);
      
      ensureCorrectWorklistScopeId(deptDe.getId());
   }
   
   /**
    * <p>
    * If created by "Org3", the <i>target-department</i> should
    * be "North" (parent should be "DE").
    * </p>
    */
   @Test
   public void testCreationOfWorkitemByOrg3()
   {
      startProcess(PROCESS_ID_5);
      
      ensureCorrectWorklistScopeId(deptDeNorth.getId());
      ensureCorrectWorklistParentScopeId(deptDe.getId());
   }
   
   /**
    * <p>
    * If created by "Role1", the <i>target-department</i> should
    * be "DE".
    * </p>
    */
   @Test
   public void testCreationOfWorkitemByRole1()
   {
      startProcess(PROCESS_ID_6);
      
      ensureCorrectWorklistScopeId(deptDe.getId());
   }
   
   private void initOrgsAndRoles()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      org1 = model.getOrganization(ORG1_ID);
      org2 = model.getOrganization(ORG2_ID);
      org3 = model.getOrganization(ORG3_ID);
      role1 = model.getRole(ROLE1_ID);
   }
   
   private void createDepts()
   {
      deptDe = DepartmentHome.create(DEPT_ID_DE, ORG1_ID, null, adminSf);
      deptDeNorth = DepartmentHome.create(SUB_DEPT_ID_NORTH, ORG3_ID, deptDe, adminSf);
   }
   
   private void createScopedParticipants()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      final Organization org1 = model.getOrganization(ORG1_ID);
      final Organization org2 = model.getOrganization(ORG2_ID);
      final Organization org3 = model.getOrganization(ORG3_ID);
      final Role role1 = model.getRole(ROLE1_ID);
      
      org1De = deptDe.getScopedParticipant(org1);
      org2De = deptDe.getScopedParticipant(org2);
      org3DeNorth = deptDeNorth.getScopedParticipant(org3);
      role1De = deptDe.getScopedParticipant(role1);
   }
   
   private void startProcess(final String processID)
   {
      final Map<String, String> piData = new HashMap<String, String>(); 
      piData.put(X_SCOPE, DEPT_ID_DE);
      piData.put(Y_SCOPE, SUB_DEPT_ID_NORTH);
      userSf.getWorkflowService().startProcess(processID, piData, true);
   }
   
   private void ensureNoUnscopedWorkitems()
   {
      final Iterator<Worklist> iter = getParticipantWorklist();
      
      while (iter.hasNext())
      {
         DepartmentInfo deptInfo = ((ModelParticipantInfo) iter.next().getOwner()).getDepartment();
         assertNotNull("Work item must not be unscoped.", deptInfo);
      }
   }
   
   private void ensureCorrectWorklistScopeId(final String expectedScopeId)
   {
      final Iterator<Worklist> iter = getParticipantWorklist();
      
      assertTrue("There should be a work item.", iter.hasNext());
      final Worklist pwl = iter.next();
      assertFalse("There should be just one work item.", iter.hasNext());
      
      final DepartmentInfo deptInfo = ((ModelParticipantInfo) pwl.getOwner()).getDepartment();
      assertNotNull("Work item must not be unscoped.", deptInfo);
      
      final String actualScopeId = deptInfo.getId();
      assertEquals(expectedScopeId, actualScopeId);
   }
   
   private void ensureCorrectWorklistParentScopeId(final String expectedParentScopeId)
   {
      final Iterator<Worklist> iter = getParticipantWorklist();
      final DepartmentInfo deptInfo = ((ModelParticipantInfo) iter.next().getOwner()).getDepartment();
      final DepartmentDetails deptDetails = (DepartmentDetails) deptInfo;
      
      final Department parent = deptDetails.getParentDepartment();
      assertEquals(expectedParentScopeId, parent.getId());
   }
   
   private Iterator<Worklist> getParticipantWorklist()
   {
      final Worklist wl = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      @SuppressWarnings("unchecked")
      final Iterator<ParticipantWorklist> iter = wl.getSubWorklists();

      final FilteringIterator<Worklist> fi = new FilteringIterator<Worklist>(
            iter, new Predicate<Worklist>()
            {
               public boolean accept(final Worklist pwl)
               {
                  return !pwl.isEmpty();
               }
            });

      return fi;
   }
}
