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
import static org.eclipse.stardust.test.dms.RepositoryTestUtils.TEST_REPO_ID;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
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
 * Tests the dms application.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class DmsApplicationTest
{
 private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final TestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DmsModelConstants.DMS_APPLICATION_MODEL_NAME);

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
   public void testCreateDocumentProcess()
   {
      WorkflowService wfs = sf.getWorkflowService();
   
      Map<String, Serializable> data = CollectionUtils.newMap();
      data.put("TestDocument", DmsUtils.createDocumentInfo("document1"));
      data.put("TestDocument2", DmsUtils.createDocumentInfo("document2"));
      data.put("TestDocument3", DmsUtils.createDocumentInfo("document3"));
   
      ProcessInstance pi = wfs.startProcess("{DmsApplicationModel}AddDocuments",
            data, true);
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
   
      Folder testRepoFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId(defaultPath, TEST_REPO_ID));
      Assert.assertNotNull(testRepoFolder);
      Assert.assertEquals(1, testRepoFolder.getDocumentCount());
      Assert.assertTrue(folderContainsDocument( "document3", testRepoFolder));
   
      Folder defaultFolder = getDms().getFolder(defaultPath);
      Assert.assertNotNull(defaultFolder);
      Assert.assertEquals(2, defaultFolder.getDocumentCount());
      Assert.assertTrue(folderContainsDocument( "document1", defaultFolder));
      Assert.assertTrue(folderContainsDocument( "document2", defaultFolder));
   
   }

   @Test
   public void testCreateFolderProcess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Serializable> data = CollectionUtils.newMap();
      data.put("TestFolder", DmsUtils.createFolderInfo("folder1"));
      data.put("TestFolder2", DmsUtils.createFolderInfo("folder2"));
      data.put("TestFolder3", DmsUtils.createFolderInfo("folder3"));

      ProcessInstance pi = wfs.startProcess("{DmsApplicationModel}CreateFolders",
            data, true);
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());

      Folder testRepoFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId(defaultPath, TEST_REPO_ID));
      Assert.assertNotNull(testRepoFolder);
      Assert.assertEquals(1, testRepoFolder.getFolderCount());
      Assert.assertTrue(folderContainsFolder( "folder3", testRepoFolder));

      Folder defaultFolder = getDms().getFolder(defaultPath);
      Assert.assertNotNull(defaultFolder);
      Assert.assertEquals(2, defaultFolder.getFolderCount());
      Assert.assertTrue(folderContainsFolder( "folder1", defaultFolder));
      Assert.assertTrue(folderContainsFolder( "folder2", defaultFolder));

   }

   @Test
   public void testRemoveDocumentProcess()
   {
      WorkflowService wfs = sf.getWorkflowService();
   
      Map<String, Serializable> data = CollectionUtils.newMap();
      data.put("TestDocument", DmsUtils.createDocumentInfo("document1"));
      data.put("TestDocument2", DmsUtils.createDocumentInfo("document2"));
      data.put("TestDocument3", DmsUtils.createDocumentInfo("document3"));
   
      ProcessInstance pi = wfs.startProcess("{DmsApplicationModel}RemoveDocuments",
            data, true);
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
   
      Folder testRepoFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId(defaultPath, TEST_REPO_ID));
      Assert.assertNotNull(testRepoFolder);
      Assert.assertEquals(0, testRepoFolder.getDocumentCount());
      Assert.assertFalse(folderContainsDocument( "document3", testRepoFolder));
   
      Folder defaultFolder = getDms().getFolder(defaultPath);
      Assert.assertNotNull(defaultFolder);
      Assert.assertEquals(0, defaultFolder.getDocumentCount());
      Assert.assertFalse(folderContainsDocument( "document1", defaultFolder));
      Assert.assertFalse(folderContainsDocument( "document2", defaultFolder));
   
   }

   @Test
   public void testRemoveFolderProcess()
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Serializable> data = CollectionUtils.newMap();
      data.put("TestFolder", DmsUtils.createFolderInfo("folder1"));
      data.put("TestFolder2", DmsUtils.createFolderInfo("folder2"));
      data.put("TestFolder3", DmsUtils.createFolderInfo("folder3"));

      ProcessInstance pi = wfs.startProcess("{DmsApplicationModel}RemoveFolders",
            data, true);
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());

      Folder testRepoFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId(defaultPath, TEST_REPO_ID));
      Assert.assertNotNull(testRepoFolder);
      Assert.assertEquals(0, testRepoFolder.getFolderCount());
      Assert.assertFalse(folderContainsFolder( "folder3", testRepoFolder));

      Folder defaultFolder = getDms().getFolder(defaultPath);
      Assert.assertNotNull(defaultFolder);
      Assert.assertEquals(0, defaultFolder.getFolderCount());
      Assert.assertFalse(folderContainsFolder( "folder1", defaultFolder));
      Assert.assertFalse(folderContainsFolder( "folder2", defaultFolder));

   }

   private boolean folderContainsDocument(String documentName, Folder parentFolder)
   {
      List<Document> documents = parentFolder.getDocuments();

      for (Document document : documents)
      {
         if (documentName.equals(document.getName()))
         {
            return true;
         }
      }
      return false;
   }

   private boolean folderContainsFolder(String subFolderName, Folder parentFolder)
   {
      List<Folder> folders = parentFolder.getFolders();

      for (Folder folder : folders)
      {
         if (subFolderName.equals(folder.getName()))
         {
            return true;
         }
      }
      return false;
   }

}
