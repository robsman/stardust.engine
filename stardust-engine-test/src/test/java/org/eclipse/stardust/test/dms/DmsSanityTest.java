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

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
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
 * Tests basic operations of the {@link DocumentManagementService}.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class DmsSanityTest
{
   private static final String DOC_NAME = "test.txt";

   private static final String DOC_PATH = "/" + DOC_NAME;

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final TestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         DmsModelConstants.DMS_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   @Before
   public void setup() throws InterruptedException
   {
      getDms().removeDocument(DOC_PATH);

      getDms().createDocument("/", DmsUtils.createDocumentInfo(DOC_NAME),
            "content".getBytes(), null);
   }

   @After
   public void cleanup() throws InterruptedException
   {
   }

   @Test
   public void testGetFolderByPath()
   {
      getDms().createFolder("/", DmsUtils.createFolderInfo("test"));

      Assert.assertNotNull(getDms().getFolder("/test"));
      Assert.assertNotNull(getDms().getFolder("/test/"));
   }

   @Test
   public void testHierarchy()
   {
      Folder folder = getDms().createFolder("/", DmsUtils.createFolderInfo("test"));
      Document document = getDms().createDocument("/test", DmsUtils.createDocumentInfo("test.txt"));
      Folder subFolder = getDms().createFolder("/test", DmsUtils.createFolderInfo("test2"));
      Document subDocument = getDms().createDocument("/test/test2", DmsUtils.createDocumentInfo("test2.txt"));

      Assert.assertNotNull(getDms().getFolder("/test"));
      Assert.assertNotNull(getDms().getFolder("/test/test2"));
      Assert.assertNotNull(getDms().getFolder(folder.getId()));
      Assert.assertNotNull(getDms().getFolder(subFolder.getId()));
      Assert.assertNotNull(getDms().getFolder(folder.getPath()));
      Assert.assertNotNull(getDms().getFolder(subFolder.getPath()));

      Assert.assertNotNull(getDms().getDocument("/test/test.txt"));
      Assert.assertNotNull(getDms().getDocument("/test/test2/test2.txt"));
      Assert.assertNotNull(getDms().getDocument(document.getId()));
      Assert.assertNotNull(getDms().getDocument(subDocument.getId()));
      Assert.assertNotNull(getDms().getDocument(document.getPath()));
      Assert.assertNotNull(getDms().getDocument(subDocument.getPath()));
   }

   @Test
   public void testGetNonExistingDocument()
   {
      Assert.assertNull(getDms().getDocument("nonexisting"));
   }

   @Test
   public void testGetNonExistingFolder()
   {
      Assert.assertNull(getDms().getFolder("nonexisting"));
      Assert.assertNull(getDms().getFolder("/nonexisting"));
      Assert.assertNull(getDms().getFolder("/nonexisting/"));
   }

   @Test(expected = DocumentManagementServiceException.class)
   public void testSameNameSiblingsForbidden()
   {
      getDms().createDocument("/", DmsUtils.createDocumentInfo(DOC_NAME));
      Assert.fail();
   }

   @Test
   public void testVersioning()
   {
      Document doc = getDms().versionDocument(DOC_PATH, null, null);

      List<Document> versions = getDms().getDocumentVersions(doc.getId());
      Assert.assertEquals(1, versions.size());
      Document v1 = versions.get(0);
      Assert.assertEquals(doc.getId(), v1.getId());
      Assert.assertEquals(doc.getRevisionId(), v1.getRevisionId());
      Assert.assertEquals(doc.getPath(), v1.getPath());

      Document doc2 = getDms().versionDocument(DOC_PATH, null, null);

      List<Document> versions2 = getDms().getDocumentVersions(doc.getId());
      Assert.assertEquals(2, versions2.size());
      Document v2 = versions2.get(1);
      Assert.assertEquals(doc2.getId(), v2.getId());
      Assert.assertEquals(doc2.getRevisionId(), v2.getRevisionId());
      Assert.assertEquals(doc2.getPath(), v2.getPath());

      Assert.assertNotEquals(v1.getRevisionId(), v2.getRevisionId());
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testMetaData()
   {
      Document doc = getDms().getDocument(DOC_PATH);

      doc.getProperties().put("string1", "stringValue1");
      doc.getProperties().put("int1", "intValue1");

      Document updatedDoc = getDms().updateDocument(doc, false, (String) null,
            (String) null, false);

      Assert.assertEquals("stringValue1", updatedDoc.getProperties().get("string1"));
      Assert.assertEquals("intValue1", updatedDoc.getProperties().get("int1"));

      Document fetchedDoc = getDms().getDocument(DOC_PATH);
      Assert.assertEquals("stringValue1", fetchedDoc.getProperties().get("string1"));
      Assert.assertEquals("intValue1", fetchedDoc.getProperties().get("int1"));
   }
}
