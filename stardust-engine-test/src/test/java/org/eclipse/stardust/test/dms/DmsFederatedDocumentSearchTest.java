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

import static org.eclipse.stardust.test.dms.DmsModelConstants.DMS_MODEL_NAME;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.RepositoryPolicy;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.impl.ClassPathFile;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.core.io.ClassPathResource;

/**
 * <p>
 * Tests DMS document search functionality for multiple Repositories.<br>
 * Each of the two Repository has three Documents.
 * </p>
 *
 * @author Roland.Stamm
 * @version $Revision: 66871 $
 */
public class DmsFederatedDocumentSearchTest
{
   private static final Log LOG = LogFactory.getLog(DmsFederatedDocumentSearchTest.class);
   
   // doc 1
   private static final String DOC_NAME1 = "test.txt";

   private static final String OWNER1 = "motu";

   private static final String CONTENT_TYPE1 = "text/plain";

   private static final String META_VALUE1 = "someMetaString";

   private static final String META_KEY1 = "name";

   private static final Map<String, String> META_DATA1 = Collections.singletonMap(META_KEY1, META_VALUE1);

   // doc 2
   private static final String DOC_NAME2 = "test.pdf";

   private static final String OWNER2 = "user2";

   private static final String CONTENT_TYPE2 = "application/pdf";

   private static final String META_VALUE2 = "someMetaString2";

   private static final String META_KEY2 = "stringValue";

   private static final Map<String, String> META_DATA2 = Collections.singletonMap(META_KEY2, META_VALUE2);

   // doc 3
   private static final String DOC_NAME3 = "test.html";

   private static final String OWNER3 = null;

   private static final String CONTENT_TYPE3 = "text/html";

   private static final Integer META_VALUE3 = Integer.valueOf(5);

   private static final String META_KEY3 = "typeKey";

   private static final Map<String, Integer> META_DATA3 = Collections.singletonMap(META_KEY3, META_VALUE3);

   private static final int TOTAL_DOCS = 6;
   
   private static final String TEST_PROVIDER_ID = "jcr-vfs";
   
   private static final String DEFAULT_REPO_ID = "default";
   
   private static final String TEST_REPO_ID = "testRepo";

   // temporary documents
   private static final String DOC_NAME_TEMP = "test.tmp";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DMS_MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);


   private IRepositoryConfiguration createTestRepoConfig(String repositoryId)
   {
      Map<String, Serializable> attributes = CollectionUtils.newMap();
      attributes.put(IRepositoryConfiguration.PROVIDER_ID, TEST_PROVIDER_ID);
      attributes.put(IRepositoryConfiguration.REPOSITORY_ID, repositoryId);
      attributes.put(JcrVfsRepositoryConfiguration.IS_IN_MEMORY_TEST_REPO, true);
      attributes.put(JcrVfsRepositoryConfiguration.USER_LEVEL_AUTHORIZATION, true);
      attributes.put(JcrVfsRepositoryConfiguration.REPOSITORY_CONFIG_LOCATION, getClasspathPath("test-repo-no-sec.xml"));
   
      return new JcrVfsRepositoryConfiguration(attributes);
   }

   private String getClasspathPath(String classpathResource)
   {
      try
      {
         return new ClassPathFile(new ClassPathResource(classpathResource)).file().toURI().toString();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private DocumentQuery getFederatedQuery()
   {
      DocumentQuery query = DocumentQuery.findAll();
      query.setPolicy(RepositoryPolicy.includeAllRepositories());
      return query;
   }

   @Before
   public void setUp()
   {
      
      initDocument(DOC_NAME1, OWNER1, CONTENT_TYPE1, META_DATA1, null);
      initDocument(DOC_NAME2, OWNER2, CONTENT_TYPE2, META_DATA2, null);
      initDocument(DOC_NAME3, OWNER3, CONTENT_TYPE3, META_DATA3, null);
      
      sf.getDocumentManagementService().bindRepository(createTestRepoConfig(TEST_REPO_ID));

      initDocument(DOC_NAME1, OWNER1, CONTENT_TYPE1, META_DATA1, TEST_REPO_ID);
      initDocument(DOC_NAME2, OWNER2, CONTENT_TYPE2, META_DATA2, TEST_REPO_ID);
      initDocument(DOC_NAME3, OWNER3, CONTENT_TYPE3, META_DATA3, TEST_REPO_ID);
   }
   
   @After
   public void cleanup()
   {
      sf.getDocumentManagementService().unbindRepository(TEST_REPO_ID);      
   }

   @Test
   public void testFindAll()
   {
      QueryService qs = sf.getQueryService();
      Documents docs = qs.getAllDocuments(getFederatedQuery());

      assertEquals("Total Documents", TOTAL_DOCS, docs.size());
   }

   @Test
   public void testFindByID()
   {
      final String DOC1_ID = initDocument(DOC_NAME_TEMP, OWNER1, CONTENT_TYPE1, META_DATA1, null);

      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.ID.isEqual(DOC1_ID));

      Documents docs = sf.getQueryService().getAllDocuments(query);

      DocumentManagementService dms = sf.getDocumentManagementService();
      dms.removeDocument("/" + DOC_NAME_TEMP);

      assertEquals("Documents", 1, docs.size());
   }

   @Test
   public void testSubSet1()
   {
      QueryService qs = sf.getQueryService();

      DocumentQuery dq = getFederatedQuery();

      dq.setPolicy(new SubsetPolicy( 1000, 1));
      Documents docs = qs.getAllDocuments(dq);

      assertEquals("SubsetPolicy( 1000, 1) Documents", TOTAL_DOCS - 1, docs.size());
   }

   @Test
   public void testSubSet2()
   {
      QueryService qs = sf.getQueryService();

      DocumentQuery dq = getFederatedQuery();

      dq.setPolicy(new SubsetPolicy(Integer.MAX_VALUE, 2));
      Documents docs = qs.getAllDocuments(dq);

      assertEquals("SubsetPolicy(Integer.MAX_VALUE, 2) Documents", TOTAL_DOCS - 2, docs.size());
   }

   @Test
   public void testSubSet3()
   {
      QueryService qs = sf.getQueryService();

      DocumentQuery dq = getFederatedQuery();

      dq.setPolicy(new SubsetPolicy(2, 0));
      Documents docs = qs.getAllDocuments(dq);

      assertEquals("SubSetPolicy(2, 0) Documents", 2, docs.size());
   }
   
   @Test
   public void testSubSet4()
   {
      QueryService qs = sf.getQueryService();

      DocumentQuery dq = getFederatedQuery();

      dq.setPolicy(new SubsetPolicy(4, 1));
      Documents docs = qs.getAllDocuments(dq);

      assertEquals("SubSetPolicy(4, 1) Documents", 4, docs.size());
   }
   
   @Test
   public void testSubSet5()
   {
      QueryService qs = sf.getQueryService();

      DocumentQuery dq = getFederatedQuery();

      dq.setPolicy(new SubsetPolicy(6, 3));
      Documents docs = qs.getAllDocuments(dq);

      assertEquals("SubSetPolicy(6, 3) Documents", TOTAL_DOCS - 3, docs.size());
   }

   @Test
   public void testAfterDate()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.DATE_CREATED.greaterOrEqual("2010-10-13T09:57:31.381+02:00"));
      query.orderBy(DocumentQuery.DATE_CREATED);
      Documents docs = sf.getQueryService().getAllDocuments(query);

      assertEquals("Total Documents", TOTAL_DOCS, docs.size());
   }

   @Test
   public void testBetweenDate()
   {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      Date date = cal.getTime();
      LOG.info(date);
      cal.add(Calendar.DATE, 2);
      Date date2 = cal.getTime();
      LOG.info(date2);

      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.DATE_CREATED.between(date.getTime(), date2.getTime()));
      query.orderBy(DocumentQuery.DATE_CREATED);
      Documents docs = sf.getQueryService().getAllDocuments(query);

      assertEquals("Total Documents", TOTAL_DOCS, docs.size());
   }

   @Test
   public void testOwnerNotNull()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.OWNER.isNotNull());
      query.orderBy(DocumentQuery.DATE_CREATED);
      Documents docs = sf.getQueryService().getAllDocuments(query);

      assertEquals("Owner != null Documents", TOTAL_DOCS - 2, docs.size());
   }

   @Test
   public void testOwnerNull()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.OWNER.isNull());
      query.orderBy(DocumentQuery.DATE_CREATED);
      Documents docs = sf.getQueryService().getAllDocuments(query);

      assertEquals("Owner == null Documents", 2, docs.size());
   }

   @Test
   public void testFindByOwner()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.OWNER.isEqual(OWNER1));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 2, docs.size());
   }

   @Test
   public void testFindByContentType()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.CONTENT_TYPE.isEqual(CONTENT_TYPE1));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 2, docs.size());
   }

   @Test
   public void testFindByNameOrTerm()
   {
      DocumentQuery query = getFederatedQuery();

      FilterOrTerm orTerm = query.getFilter().addOrTerm();

      orTerm.add(DocumentQuery.NAME.isEqual(DOC_NAME1)) //
            .add(DocumentQuery.NAME.isEqual(DOC_NAME2));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", TOTAL_DOCS-2, docs.size());
   }

   @Test
   public void testFindMetaDataNamedStringLike()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.META_DATA.withName(META_KEY1).like(META_VALUE1+"*"));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 2, docs.size());
   }

   @Test
   public void testFindMetaDataNamedIntegerEqual()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.META_DATA.withName(META_KEY3).isEqual(META_VALUE3));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 2, docs.size());
   }

   @Test
   public void testFindMetaDataAnyLike()
   {
      DocumentQuery query = getFederatedQuery();
      // META_DATA.any() only supports LIKE with jackrabbit.
      query.where(DocumentQuery.META_DATA.any().like(META_VALUE1 + "*"));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 2, docs.size());
   }

   @Test
   public void testFindNameLike()
   {
      DocumentQuery query = getFederatedQuery();

      query.where(DocumentQuery.CONTENT_TYPE.isEqual("text/plain"))
      .and(DocumentQuery.NAME.like("*.txt"));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 2, docs.size());
   }

   @Test
   public void testFindContentLike() throws InterruptedException
   {
      final int expectedDocSize = TOTAL_DOCS;
      final int retryCount = 3;
      
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.CONTENT.like("this is a test content"));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      /* full text search indexers run asnychronously, and we don't have a means to determine   */
      /* when they are completed ==> wait and retry seems dirty, but is the only option we have */
      for (int i=0; docs.size() != expectedDocSize && i<retryCount; i++)
      {
         Thread.sleep(1000L);
         docs = sf.getQueryService().getAllDocuments(query);
      }
      
      assertEquals("Documents", expectedDocSize, docs.size());
      assertEquals("text/plain", docs.get(0).getContentType());
   }

   @Test
   public void testFindContentLikeMetaData()
   {
      DocumentQuery query = getFederatedQuery();

      // MetaData content should produce no hits.
      query.where(DocumentQuery.CONTENT.like(META_VALUE1));

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", 0, docs.size());
   }

   @Test
   public void testFindContentNotNull()
   {
      DocumentQuery query = getFederatedQuery();
      query.where(DocumentQuery.CONTENT.isNotNull());

      Documents docs = sf.getQueryService().getAllDocuments(query);
      
      assertEquals("Documents", TOTAL_DOCS, docs.size());
   }

   @Test
   public void testOrderByDocumentName()
   {
      DocumentQuery query = getFederatedQuery();
      query.orderBy(DocumentQuery.NAME,true);
      Documents docs = sf.getQueryService().getAllDocuments(query);
      Document result1= docs.get(0);

      assertEquals("Document Name","test.html", result1.getName());

      query = getFederatedQuery();
      query.orderBy(DocumentQuery.NAME,false);

      docs = sf.getQueryService().getAllDocuments(query);
      Document result2= docs.get(0);

      assertEquals("Document Name", "test.txt",result2.getName());
   }

   private String initDocument(String docName, String owner, String contentType,
         Map<String, ? extends Serializable> metaData, String repositoryId)
   {
      DocumentManagementService dms = sf.getDocumentManagementService();

      dms.removeDocument(RepositoryIdUtils.addRepositoryId("/" + docName, repositoryId));

      DocumentInfo doc = new DmsDocumentBean();
      byte[] content;

      // version 0 (unversioned)
      doc.setContentType(contentType);
      doc.setName(docName);
      doc.setDescription("testFile");
      doc.setOwner(owner);
      doc.setProperties(metaData);
      content = "this is a test content".getBytes();
      Document v0 = dms.createDocument(
            RepositoryIdUtils.addRepositoryId("/", repositoryId), doc, content, "");
      return v0.getId();
   }
}
