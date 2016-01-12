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
package org.eclipse.stardust.test.authorization;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 *
 * @author Florin.Herinean
 */
public class DataAuthorizationTest
{
   private static final String MODEL_ID = "MyDepartmentModel";

   private static final String ONE_VALUE = "One Value";
   private static final String SECOND_VALUE = "Second Value";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String USER_READER = "UserReader";
   private static final String USER_WRITER = "UserWriter";

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory readerSf = new TestServiceFactory(new UsernamePasswordPair(USER_READER, USER_READER));
   private final TestServiceFactory writerSf = new TestServiceFactory(new UsernamePasswordPair(USER_WRITER, USER_WRITER));

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf)
                                          .around(readerSf)
                                          .around(writerSf);

   @Before
   public void setUp()
   {
      UserHome.create(adminSf, USER_READER, new QName(MODEL_ID, "RestrictedReader").toString());
      UserHome.create(adminSf, USER_WRITER, new QName(MODEL_ID, "RestrictedWriter").toString());
   }

   /**
    *
    */
   @Test
   public void testSimpleRestrictedDataAccess()
   {
      String processId = new QName(MODEL_ID, "RestrictedDataAccess").toString();
      final Map<String, String> piData = Collections.singletonMap("RestrictedData", ONE_VALUE);
      ProcessInstance pi = adminSf.getWorkflowService().startProcess(processId, piData, true);

      try
      {
         writerSf.getWorkflowService().getInDataPath(pi.getOID(), "RestrictedInput");
         fail("Writer user should not have read access.");
      }
      catch (AccessForbiddenException ex)
      {
         assertEquals("Error code", "AUTHx01000", ex.getError().getId());
         assertTrue("Error message does not contain 'data.modifyDataValues'", ex.getMessage().contains("data.readDataValues"));
      }
      writerSf.getWorkflowService().setOutDataPath(pi.getOID(), "RestrictedOutput", SECOND_VALUE);

      assertEquals("Changed value", SECOND_VALUE, readerSf.getWorkflowService().getInDataPath(pi.getOID(), "RestrictedInput"));
      try
      {
         readerSf.getWorkflowService().setOutDataPath(pi.getOID(), "RestrictedOutput", ONE_VALUE);
         fail("Writer user should not have write access.");
      }
      catch (AccessForbiddenException ex)
      {
         assertEquals("Error code", "AUTHx01000", ex.getError().getId());
         assertTrue("Error message does not contain 'data.modifyDataValues'", ex.getMessage().contains("data.modifyDataValues"));
      }
   }
}
