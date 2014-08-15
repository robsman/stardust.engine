/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.dms;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.dms.RepositoryTestUtils.SYSTEM_REPO_ID;
import static org.eclipse.stardust.test.dms.RepositoryTestUtils.TEST_REPO_ID;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryAuditTrailUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * Tests {@link RepositoryAuditTrailUtils}.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class DmsMetaDataUtilityTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         DmsModelConstants.DMS_MODEL_NAME);

   private static final String DOC_NAME = "test.txt";

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   @Before
   public void setup()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();

//      dms.bindRepository(RepositoryTestUtils.createTestRepoConfig());
//      dms.setDefaultRepository(RepositoryTestUtils.TEST_REPO_ID);
//
//      for (int i = 1; i < 6; i++ )
//      {
         dms.removeDocument("/" + DOC_NAME);
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(DOC_NAME);
         docInfo.setContentType("text/plain");
         docInfo.setDescription("default description");

         docInfo.setProperty("myString", "metaDataString");

         dms.createDocument("/", docInfo, ("this is content no# ").getBytes(), null);
//      }

   }


   @Test
   public void testUtility()
   {
      Document document = getDms().getDocument("/"+ DOC_NAME);

      sf.getWorkflowService().execute(new DmsMetaDataUtilityServiceCommand(document, true));

      Document retrievedDocument = (Document) sf.getWorkflowService().execute(new DmsMetaDataUtilityServiceCommand(document, false));

      Assert.assertEquals(document, retrievedDocument);

   }

   public void testSeparation()
   {
      getDms().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertEquals(TEST_REPO_ID, getDms().getDocument(doc.getId()).getRepositoryId());
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));
   }


}
