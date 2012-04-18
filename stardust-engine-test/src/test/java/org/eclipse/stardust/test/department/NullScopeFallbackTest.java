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

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_1;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.PROCESS_ID_2;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
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
 * Tests fallbacks to null scope if there isn't any
 * appropriate department specified or found.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class NullScopeFallbackTest extends LocalJcrH2Test
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
      UserHome.create(adminSf, USER_ID, ORG_ID_1);
   }
   
   /**
    * <p>
    * If there isn't any department specified, the activity should
    * be assigned to the null scope (synchronous process start).
    * </p>
    */
   @Test
   public void testNullDepartmentSynchronous()
   {
      userSf.getWorkflowService().startProcess(PROCESS_ID_2, null, true);
      
      ensureNullScope();
   }
   
   /**
    * <p>
    * If there isn't any department specified, the activity should
    * be assigned to the null scope (asynchronous process start).
    * </p>
    */
   @Test
   public void testNullDepartmentAsynchronous()
   {
      userSf.getWorkflowService().startProcess(PROCESS_ID_2, null, false);
      
      ensureNullScope();
   }
   
   /**
    * <p>
    * If there isn't any appropriate department found based on the
    * specified data, the activity should be assigned to the null scope
    * (synchronous process start).
    * </p>
    */
   @Test
   public void testDepartmentNotFoundSynchronous()
   {
      final Map<String, String> deptData = Collections.singletonMap("CountryCode", "N/A");
      userSf.getWorkflowService().startProcess(PROCESS_ID_2, deptData, true);
      
      ensureNullScope();
   }

   /**
    * <p>
    * If there isn't any appropriate department found based on the
    * specified data, the activity should be assigned to the null scope
    * (asynchronous process start).
    * </p>
    */
   @Test
   public void testDepartmentNotFoundAsynchronous()
   {
      final Map<String, String> deptData = Collections.singletonMap("CountryCode", "N/A");
      userSf.getWorkflowService().startProcess(PROCESS_ID_2, deptData, false);
      
      ensureNullScope();
   }
   
   private void ensureNullScope()
   {
      final Worklist worklist = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      @SuppressWarnings("unchecked")
      final Iterator<ParticipantWorklist> worklistIter = worklist.getSubWorklists();
      
      boolean found = false;
      while (worklistIter.hasNext())
      {
         final Worklist wl = worklistIter.next();
         if (wl.getOwnerID().equals(ORG_ID_1))
         {
            final ParticipantInfo participant = wl.getOwner();
            if ( !(participant instanceof ModelParticipantInfo))
            {
               fail("Participant must be a Model Participant.");
            }
            final DepartmentInfo deptInfo = ((ModelParticipantInfo) participant).getDepartment();
            assertNull("Department must be null.", deptInfo);
            
            found = true;
            break;
         }
      }
      assertTrue("No appropriate work item found.", found);
   }
}
