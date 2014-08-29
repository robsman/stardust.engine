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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryAuditTrailUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryResourceUtils;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.dms.repository.mock.MockProvider;
import org.eclipse.stardust.test.dms.repository.mock.MockService;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.junit.Assert;
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
public class DmsAuditTrailMetaDataTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         DmsModelConstants.DMS_MODEL_NAME);

   private static final String DOC_NAME = "test.txt";

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   private Document createMockDocument(String id)
   {
      Document mockDocument = RepositoryResourceUtils.createDocument(id, "/", 0,
            "text/plain", new Date(), new Date());
      mockDocument.setName(MockService.MOCK_DOCUMENT_ID);

      Map<String, Serializable> metaDataProperties = CollectionUtils.newMap();
      metaDataProperties.put("string", "string");
      metaDataProperties.put("int", 123);
      metaDataProperties.put("long", 12345L);
      metaDataProperties.put("date", new Date());
      metaDataProperties.put("boolean", Boolean.TRUE);

      mockDocument.setProperties(metaDataProperties);

      return mockDocument;
   }

   @Test
   public void testUtility()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();

      dms.removeDocument("/" + DOC_NAME);
      Document docInfo = createMockDocument(MockService.MOCK_DOCUMENT_ID);
      docInfo.setName(DOC_NAME);
      Document document = dms.createDocument("/", docInfo,
            ("this is content no# ").getBytes(), null);

      // store meta data in AuditTrail via utility
      sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(document, true));

      // check via utlity
      Document retrievedDocument = (Document) sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(document, false));
      Assert.assertEquals(document, retrievedDocument);

   }

   @Test
   public void testTransparentMetaDataRead()
   {
      String id = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_ID,
            MockProvider.DEFAULT_REPO_ID);

      // use repository qualified id here, because it is stored via utility.
      Document mockDocument = createMockDocument(id);

      // store meta data in AuditTrail via utility
      sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(mockDocument, true));

      // check via service
      Assert.assertNotNull(getDms().getDocument(id));
      Assert.assertNotNull(getDms().getDocument(id).getProperties());
      Assert.assertEquals(mockDocument.getProperties(), getDms().getDocument(id)
            .getProperties());
   }

   @Test
   public void testTransparentMetaDataReadVersion()
   {
      String id = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_ID,
            MockProvider.DEFAULT_REPO_ID);
      String revid = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_REV_ID,
            MockProvider.DEFAULT_REPO_ID);

      // use repository qualified id here, because it is stored via utility.
      Document mockDocument = createMockDocument(id);

      // store meta data in AuditTrail via service (does not have meta data)
      Document v0 = getDms().versionDocument(id, null, null);

      // store updated meta data in AuditTrail via utility (includes meta data properties)
      v0.setProperties(mockDocument.getProperties());
      sf.getWorkflowService().execute(new DmsMetaDataUtilityServiceCommand(v0, true));

      // check via service
      Document document = getDms().getDocument(id);
      Assert.assertNotNull(document);
      Assert.assertNotNull(document.getProperties());
      Assert.assertEquals(mockDocument.getProperties(), document.getProperties());

      Document documentVersion = getDms().getDocument(revid);
      Assert.assertNotNull(documentVersion);
      Assert.assertNotNull(documentVersion.getProperties());
      Assert.assertEquals(mockDocument.getProperties(), documentVersion.getProperties());
   }

   @Test
   public void testTransparentMetaDataWrite()
   {
      String id = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_ID,
            MockProvider.DEFAULT_REPO_ID);

      Document mockDocument = createMockDocument(MockService.MOCK_DOCUMENT_ID);

      // store meta data in AuditTrail via service
      getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/", MockProvider.DEFAULT_REPO_ID),
            mockDocument);

      // check via service
      Document retrievedDocument = getDms().getDocument(id);
      Assert.assertNotNull(retrievedDocument);
      Assert.assertNotNull(retrievedDocument.getProperties());
      Assert.assertEquals(mockDocument.getProperties(), retrievedDocument.getProperties());

      // check via utility
      Document retrievedViaCommandDocument = (Document) sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(retrievedDocument, false));
      Assert.assertEquals(mockDocument, retrievedViaCommandDocument);
   }

   @Test
   public void testTransparentMetaDataWriteVersion()
   {
      String id = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_ID,
            MockProvider.DEFAULT_REPO_ID);
      String revid = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_REV_ID,
            MockProvider.DEFAULT_REPO_ID);

      Document mockDocument = createMockDocument(id);

      // store meta data in AuditTrail via service
      getDms().updateDocument(mockDocument, true, null, null, false);

      // check via utility
      Document retrievedViaCommandDocument = (Document) sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(mockDocument, false));
      Assert.assertEquals(mockDocument, retrievedViaCommandDocument);

      // check via service
      Document retrievedDocument = getDms().getDocument(id);
      Assert.assertNotNull(retrievedDocument);
      Assert.assertNotNull(retrievedDocument.getProperties());
      Assert.assertEquals(mockDocument.getProperties(), retrievedDocument.getProperties());

      Document documentVersion = getDms().getDocument(revid);
      Assert.assertNotNull(documentVersion);
      Assert.assertNotNull(documentVersion.getProperties());
      Assert.assertEquals(mockDocument.getProperties(), documentVersion.getProperties());
   }

   @Test
   public void testTransparentMetaDataDelete()
   {
      String id = RepositoryIdUtils.addRepositoryId(MockService.MOCK_DOCUMENT_ID,
            MockProvider.DEFAULT_REPO_ID);

      Document mockDocument = createMockDocument(MockService.MOCK_DOCUMENT_ID);

      // store meta data in AuditTrail via service
      getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/", MockProvider.DEFAULT_REPO_ID),
            mockDocument);

      // remove document
      getDms().removeDocument(id);

      // check via service not possible because the mock service always returns document.

      // check via utility
      Document retrievedDocument = (Document) sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(mockDocument, false));

      Assert.assertNull(retrievedDocument);
   }

   @Test
   public void testTransparentMetaDataDeleteFolderRecursive()
   {
      Document mockDocument = createMockDocument(MockService.MOCK_DOCUMENT_ID);

      // store meta data in AuditTrail via service
      String root = RepositoryIdUtils.addRepositoryId("/", MockProvider.DEFAULT_REPO_ID);
      getDms().createDocument(
            root,
            mockDocument);

      // remove document
      getDms().removeFolder(root, true);

      // check via service not possible because the mock service always returns document.

      // check via utility
      Document retrievedDocument = (Document) sf.getWorkflowService().execute(
            new DmsMetaDataUtilityServiceCommand(mockDocument, false));

      Assert.assertNull(retrievedDocument);
   }

}
