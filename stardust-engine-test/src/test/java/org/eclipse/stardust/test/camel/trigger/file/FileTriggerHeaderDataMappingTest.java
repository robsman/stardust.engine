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
public class FileTriggerHeaderDataMappingTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(FileTriggerHeaderDataMappingTest.class.getName());

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
   public void fileTriggerToPrimitiveInHeader() throws Exception
   {
      String fileContent = "primitiveData content from test class";
      createFile("target/FileDirectory/PrimitiveInHeader", "messageFile.xml", fileContent);

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileToPrimitiveInHeader")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileToPrimitiveInHeader"));
      if (pis.size() > 1)
         throw new RuntimeException("Please clean the audit Trial");
      ProcessInstance pi = pis.get(0);
      Object CamelFileNameOnly = sf.getWorkflowService().getInDataPath(pi.getOID(), "FileContent");
      trace.info("FileContent = " + CamelFileNameOnly);
      assertNotNull(CamelFileNameOnly);
      assertTrue(CamelFileNameOnly instanceof String);
      assertEquals("messageFile.xml", CamelFileNameOnly.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileToPrimitiveInHeader");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }
}
