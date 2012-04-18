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
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UserHome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether
 * <ul>
 *   <li>creation, and</li>
 *   <li>assignment</li>
 * </ul>
 * of scoped work items works correctly.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ScopedWorklistTest extends LocalJcrH2Test
{
   private static final String USER_ID = "User";
   
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
      UserHome.create(adminSf, USER_ID);
   }
   
   /**
    * <p>
    * It should be possible to create a department, start a process instance comprising a
    * department and later retrieve the department from the worklist items.
    * </p>  
    */
   @Test
   public void testCreatingScopedWorklistItem()
   {
      final Department dept = DepartmentHome.create(DEPT_ID_DE, ORG_ID_1, null, adminSf);
      final ModelParticipant org1 = (ModelParticipant) adminSf.getQueryService().getParticipant(ORG_ID_1);
      final ModelParticipantInfo mpi = dept.getScopedParticipant(org1);
      
      final User user = userSf.getUserService().getUser();
      UserHome.addGrants(adminSf, user, mpi);
      
      startProcess(PROCESS_ID_2, COUNTRY_CODE_DATA_NAME);
      
      ensureWorklistAssignedTo(dept);
   }

   /**
    * <p>
    * Tests whether a created activity instance is assigned
    * to a scoped participant correctly.
    * </p>
    */
   @Test
   public void testActivityInstanceAssignedToDepartment()
   {
      final Department originalDept = DepartmentHome.create(DEPT_ID_DE, ORG1_ID, null, adminSf);
      
      startProcess(PROCESS_ID_3, X_SCOPE);
      
      final ActivityInstances ais = adminSf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive());
      assertEquals(1, ais.size());
      
      final ActivityInstanceDetails aid = (ActivityInstanceDetails) ais.get(0);
      final ModelParticipantInfo performer = (ModelParticipantInfo) aid.getCurrentPerformer();
      final DepartmentInfo retrievedDept = performer.getDepartment();
      
      assertNotNull("Department must not be null.", retrievedDept);
      assertEquals(originalDept.getOID(), retrievedDept.getOID());
   }
   
   /**
    * <p>
    * Tests whether a worklist item of a created activity instance
    * can only be seen by users who have grants to these scoped
    * participants for the matching department.
    * </p>
    */
   @Test
   public void testWorklistAssignedToDepartmentNoPermission()
   {
      startProcess(PROCESS_ID_3, X_SCOPE);
      
      final Iterator<Worklist> iter = getSubWorklists();
      while (iter.hasNext())
      {
         final Worklist wl = iter.next();
         assertTrue("There should be no work items due to insufficient grants.", wl.isEmpty());
      }
   }
   
   /**
    * <p>
    * Tests whether a worklist item of a created activity instance
    * is assigned to a scoped participant correctly.
    * </p>
    */
   @Test
   public void testWorklistAssignedToDepartment()
   {
      final Department dept = DepartmentHome.create(DEPT_ID_DE, ORG1_ID, null, adminSf);
      final Organization org = dept.getOrganization();
      
      startProcess(PROCESS_ID_3, X_SCOPE);
      
      final User user = userSf.getUserService().getUser();
      UserHome.addGrants(adminSf, user, dept.getScopedParticipant(org));
      
      final Iterator<Worklist> iter = getSubWorklists();
      Assert.assertTrue("There should be a work item.", iter.hasNext());
      
      final Worklist wl = iter.next();
      assertFalse("There should be just one work item.", iter.hasNext());
      
      final DepartmentInfo assignedDep = ((ModelParticipantInfo) wl.getOwner()).getDepartment();
      assertEquals(dept.getOID(), assignedDep.getOID());
   }
   
   private void ensureWorklistAssignedTo(final Department createdDept)
   {
      final Iterator<Worklist> worklistIter = getSubWorklists();
      
      boolean found = false;
      while (worklistIter.hasNext())
      {
         final Worklist pwl = worklistIter.next(); 
         if (pwl.getOwnerID().equals(ORG_ID_1))
         {
            final ParticipantInfo participant = pwl.getOwner();
            if ( !(participant instanceof ModelParticipantInfo))
            {
               fail("Participant must be a Model Participant.");
            }
            final DepartmentInfo deptInfo = ((ModelParticipantInfo) participant).getDepartment();
            assertEquals(createdDept.getId(), deptInfo.getId());
            
            found = true;
            break;
         }
      }
      assertTrue("No appropriate work item found.", found);
   }
   
   private void startProcess(final String processId, final String dataId)
   {
      final Map<String, String> piData = Collections.singletonMap(dataId, DEPT_ID_DE);
      userSf.getWorkflowService().startProcess(processId, piData, true);
   }
   
   @SuppressWarnings("unchecked")
   private Iterator<Worklist> getSubWorklists()
   {
      final Worklist wl = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      return wl.getSubWorklists();
   }
}
