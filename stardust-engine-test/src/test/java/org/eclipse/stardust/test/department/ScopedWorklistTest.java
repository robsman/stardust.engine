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

import static org.eclipse.stardust.test.department.DepartmentModelConstants.COUNTRY_CODE_DATA_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.DEPT_ID_DE;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_1;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.PROCESS_ID_2;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.DepartmentHome;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.eclipse.stardust.test.api.UserHome;
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
 * @version $Revision
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

   private Department dept;
   
   @Before
   public void setUp()
   {
      dept = DepartmentHome.create(DEPT_ID_DE, ORG_ID_1, null, adminSf);
      final ModelParticipant org1 = (ModelParticipant) adminSf.getQueryService().getParticipant(ORG_ID_1);
      final ModelParticipantInfo mpi = dept.getScopedParticipant(org1);

      UserHome.create(adminSf, USER_ID, mpi);
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
      final Map<String, String> ccData = Collections.singletonMap(COUNTRY_CODE_DATA_NAME, DEPT_ID_DE);
      userSf.getWorkflowService().startProcess(PROCESS_ID_2, ccData, false);
      
      ensureWorklistAssignedTo(dept);
   }

   private void ensureWorklistAssignedTo(final Department createdDept)
   {
      final Worklist wl = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      @SuppressWarnings("unchecked")
      final Iterator<ParticipantWorklist> worklistIter = wl.getSubWorklists();
      
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
}
