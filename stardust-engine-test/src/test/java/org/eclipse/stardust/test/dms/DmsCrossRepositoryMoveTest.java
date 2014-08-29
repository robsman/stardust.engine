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
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.After;
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
 * Tests move operations across repository borders.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class DmsCrossRepositoryMoveTest
{
 private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final TestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DmsModelConstants.DMS_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   @Before
   public void setup() throws InterruptedException
   {
      IRepositoryConfiguration config = RepositoryTestUtils.createTestRepoConfig();
      getDms().bindRepository(config);

      getDms().removeDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      getDms().createDocument(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), DmsUtils.createDocumentInfo("test.txt"), "content".getBytes(), null);
   }

   @After
   public void cleanup() throws InterruptedException
   {
      getDms().removeFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), true);
      getDms().unbindRepository(TEST_REPO_ID);
   }

   @Test
   public void testCrossRepositoryMoveWithTargetPath() throws InterruptedException
   {
      Document doc = getDms().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      // move from testRepo to default
      Document movedDoc = getDms().moveDocument(doc.getId(),
            RepositoryIdUtils.addRepositoryId("/", SYSTEM_REPO_ID));

      Assert.assertNull(getDms().getDocument(doc.getId()));
      Assert.assertEquals(SYSTEM_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), TEST_REPO_ID)));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), null)));

      // move from default to testRepo
      Document movedDoc2 = getDms().moveDocument(movedDoc.getId(),
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID));

      Assert.assertNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), null)));
   }

   @Test
   public void testCrossRepositoryMoveWithTargetId() throws InterruptedException
   {
      Document doc = getDms().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      // move from testRepo to default
      Folder targetFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId("/", SYSTEM_REPO_ID));
      Document movedDoc = getDms().moveDocument(doc.getId(), targetFolder.getId());

      Assert.assertNull(getDms().getDocument(doc.getId()));
      Assert.assertEquals(SYSTEM_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), TEST_REPO_ID)));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), null)));

      // move from default to testRepo
      Folder targetFolder2 = getDms().getFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID));
      Document movedDoc2 = getDms().moveDocument(movedDoc.getId(), targetFolder2.getId());

      Assert.assertNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), null)));
   }


   @Test
   public void testSameRepositoryMoveWithTargetPath() throws InterruptedException
   {
      Document doc = getDms().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));
      getDms().createFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), DmsUtils.createFolderInfo("test"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      // move from testRepo to testRepo
      Document movedDoc = getDms().moveDocument(doc.getId(),
            RepositoryIdUtils.addRepositoryId("/test", TEST_REPO_ID));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), null)));

      // move from testRepo to testRepo
      Document movedDoc2 = getDms().moveDocument(movedDoc.getId(),
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID));

      Assert.assertNotNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), null)));
   }

   @Test
   public void testSameRepositoryMoveWithTargetId() throws InterruptedException
   {
      Document doc = getDms().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));
      getDms().createFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), DmsUtils.createFolderInfo("test"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      // move from testRepo to testRepo
      Folder targetFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId("/test", TEST_REPO_ID));
      Document movedDoc = getDms().moveDocument(doc.getId(), targetFolder.getId());

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc.getId(), null)));

      // move from testRepo to testRepo
      Folder targetFolder2 = getDms().getFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID));
      Document movedDoc2 = getDms().moveDocument(movedDoc.getId(), targetFolder2.getId());

      Assert.assertNotNull(getDms().getDocument(movedDoc.getId()));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(movedDoc2.getId()));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), TEST_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(movedDoc2.getId(), null)));
   }

}
