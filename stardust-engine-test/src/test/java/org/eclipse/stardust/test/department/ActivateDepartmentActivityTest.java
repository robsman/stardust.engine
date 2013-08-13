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
import static org.eclipse.stardust.test.department.DepartmentModelConstants.PROCESS_ID_1;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.PROCESS_ID_2;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
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
 * Tests activating an activity that is connected to a department
 * both synchronously and asynchronously.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ActivateDepartmentActivityTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String USER_ID = "User";
   
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
      UserHome.create(adminSf, USER_ID);
      DepartmentHome.create(adminSf, DEPT_ID_DE, ORG_ID_1, null);
   }
   
   /**
    * <p>
    * It should be possible to activate an activity connected to a 
    * department synchronously (<strong>with</strong> a preceding
    * route activity).
    * </p>
    */
   @Test
   public void testActivateDepartmentActivitySynchronouslyWithRouteActivity()
   {
      startProcess(PROCESS_ID_1, true);
   }
   
   /**
    * <p>
    * It should be possible to activate an activity connected to a 
    * department asynchronously (<strong>with</strong> a preceding
    * route activity).
    * </p>
    */
   @Test
   public void testActivateDepartmentActivityAsynchronouslyWithRouteActivity() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = startProcess(PROCESS_ID_1, false);
      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(pi.getOID());
   }
   
   /**
    * <p>
    * It should be possible to activate an activity connected to a 
    * department synchronously (<strong>without</strong> a preceding
    * route activity).
    * </p>
    */
   @Test
   public void testActivateDepartmentActivitySynchronouslyWithoutRouteActivity()
   {
      startProcess(PROCESS_ID_2, true);
   }
   
   /**
    * <p>
    * It should be possible to activate an activity connected to a 
    * department asynchronously (<strong>without</strong> a preceding
    * route activity).
    * </p>
    */
   @Test
   public void testActivateDepartmentActivityAsynchronouslyWithoutRouteActivity() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = startProcess(PROCESS_ID_2, false);
      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(pi.getOID());
   }   
   
   private ProcessInstance startProcess(final String processId, final boolean synchronously)
   {
      final Map<String, String> ccData = Collections.singletonMap(COUNTRY_CODE_DATA_NAME, DEPT_ID_DE);
      return userSf.getWorkflowService().startProcess(processId, ccData, synchronously);
   }
}
