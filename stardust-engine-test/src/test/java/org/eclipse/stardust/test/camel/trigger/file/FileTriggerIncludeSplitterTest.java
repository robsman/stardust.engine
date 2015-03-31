/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.trigger.file;

import static org.eclipse.stardust.test.camel.common.Util.createFile;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class FileTriggerIncludeSplitterTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(FileTriggerIncludeSplitterTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   public static final String MODEL_ID = "FileTriggerTestModel";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test(timeout = 10000)
   public void testSplitWithFileTrigger() throws Exception
   {
      createFile("target/FileDirectory/SplitTest", "SplitFile.txt", "line1\nline2");

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{FileTriggerTestModel}SplitProcess")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}SplitProcess"));
      ProcessInstance pi1 = pis.get(0);
      Object responseLine1 = sf.getWorkflowService().getInDataPath(pi1.getOID(),
            "SplitFile");
      trace.info("Split File Content = " + responseLine1);
      assertNotNull(responseLine1);
      assertTrue(responseLine1 instanceof String);
      assertEquals("line1", responseLine1.toString());

      ProcessInstance pi2 = pis.get(1);
      Object responseLine2 = sf.getWorkflowService().getInDataPath(pi2.getOID(),
            "SplitFile");
      trace.info("Split File Content = " + responseLine2);
      assertNotNull(responseLine2);
      assertTrue(responseLine2 instanceof String);
      assertEquals("line2", responseLine2.toString());

      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}SplitProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);

      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

      trace.info("activityInstance state = " + activityInstances.get(1).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(1).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(1).getState());
   }
}
