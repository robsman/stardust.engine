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
package org.eclipse.stardust.test.dms;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.dms.RepositoryTestUtils.SYSTEM_REPO_ID;
import static org.eclipse.stardust.test.dms.RepositoryTestUtils.TEST_REPO_ID;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.pojo.AuditTrailPartitionManager;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryProvider;
import org.eclipse.stardust.engine.core.spi.dms.*;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * Tests binding and unbinding repository instances and separation of documents.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DmsMultiRepositoryMultiPartitionTest
{
   private static final String PARTITION1 = "dms_multi_partition_1";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   // partition 1 SF
   private final TestServiceFactory sf_p1 = new TestServiceFactory(ADMIN_USER_PWD_PAIR, PARTITION1);


   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         DmsModelConstants.DMS_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf).around(sf_p1);

   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   private DocumentManagementService getDmsP1()
   {
      return sf_p1.getDocumentManagementService();
   }

   private IRepositoryProviderInfo getJcrVfs(List<IRepositoryProviderInfo> providerInfos)
   {
      for (IRepositoryProviderInfo iRepositoryProviderInfo : providerInfos)
      {
         if (JcrVfsRepositoryProvider.PROVIDER_ID.equals(iRepositoryProviderInfo.getProviderId()))
         {
            return iRepositoryProviderInfo;
         }
      }
      Assert.fail(JcrVfsRepositoryProvider.PROVIDER_ID + " provider not found.");
      return null;
   }

   @BeforeClass
   public static void setupOnce() throws SQLException
   {
      AuditTrailPartitionManager.createAuditTrailPartition(PARTITION1, "sysop");
   }

   @AfterClass
   public static void teardownOnce() throws SQLException
   {
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTITION1, "sysop");
   }

   @Test
   public void test01ConfigurationTemplate()
   {
      List<IRepositoryProviderInfo> providerInfos = getDms().getRepositoryProviderInfos();

      IRepositoryProviderInfo jcrVfsProviderInfo = getJcrVfs(providerInfos);
      IRepositoryConfiguration jcrVfsConfigurationTemplate = jcrVfsProviderInfo.getConfigurationTemplate();

      Assert.assertEquals(
            jcrVfsProviderInfo.getProviderId(),
            jcrVfsConfigurationTemplate.getAttributes().get(
                  JcrVfsRepositoryConfiguration.PROVIDER_ID));
      Assert.assertTrue(jcrVfsConfigurationTemplate.getAttributes().containsKey(
            JcrVfsRepositoryConfiguration.REPOSITORY_ID));
      Assert.assertTrue(jcrVfsConfigurationTemplate.getAttributes().containsKey(
            JcrVfsRepositoryConfiguration.JNDI_NAME));

   }

   @Test
   public void test02ConfigurationTemplate_p1()
   {
      List<IRepositoryProviderInfo> providerInfos = getDmsP1().getRepositoryProviderInfos();

      IRepositoryProviderInfo jcrVfsProviderInfo = getJcrVfs(providerInfos);
      IRepositoryConfiguration jcrVfsConfigurationTemplate = jcrVfsProviderInfo.getConfigurationTemplate();

      Assert.assertEquals(
            jcrVfsProviderInfo.getProviderId(),
            jcrVfsConfigurationTemplate.getAttributes().get(
                  JcrVfsRepositoryConfiguration.PROVIDER_ID));
      Assert.assertTrue(jcrVfsConfigurationTemplate.getAttributes().containsKey(
            JcrVfsRepositoryConfiguration.REPOSITORY_ID));
      Assert.assertTrue(jcrVfsConfigurationTemplate.getAttributes().containsKey(
            JcrVfsRepositoryConfiguration.JNDI_NAME));

   }


   @Test
   public void test03Bind()
   {
      IRepositoryConfiguration config = RepositoryTestUtils.createTestRepoConfig();
      getDms().bindRepository(config);

      Preferences preferences = sf.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_CONFIGURATIONS, TEST_REPO_ID);
      Assert.assertEquals(config.getAttributes(), preferences.getPreferences());
   }

   @Test
   public void test04Bind_p1()
   {
      IRepositoryConfiguration config = RepositoryTestUtils.createTestRepoConfig();
      getDmsP1().bindRepository(config);

      Preferences preferences = sf_p1.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_CONFIGURATIONS, TEST_REPO_ID);
      Assert.assertEquals(config.getAttributes(), preferences.getPreferences());
   }

   @Test
   public void test05Listing()
   {

      List<IRepositoryProviderInfo> repositoryProviderInfos = getDms().getRepositoryProviderInfos();
      for (IRepositoryProviderInfo iRepositoryProviderInfo : repositoryProviderInfos)
      {
         System.out.println(iRepositoryProviderInfo);
      }
      Assert.assertEquals(2, repositoryProviderInfos.size());

      List<IRepositoryInstanceInfo> repositoryInstanceInfos = getDms().getRepositoryInstanceInfos();
      for (IRepositoryInstanceInfo iRepositoryInstanceInfo : repositoryInstanceInfos)
      {
         System.out.println(iRepositoryInstanceInfo);
      }
      Assert.assertEquals(3, repositoryInstanceInfos.size());
   }

   @Test
   public void test06Listing_p1()
   {

      List<IRepositoryProviderInfo> repositoryProviderInfos = getDmsP1().getRepositoryProviderInfos();
      for (IRepositoryProviderInfo iRepositoryProviderInfo : repositoryProviderInfos)
      {
         System.out.println(iRepositoryProviderInfo);
      }
      Assert.assertEquals(2, repositoryProviderInfos.size());

      List<IRepositoryInstanceInfo> repositoryInstanceInfos = getDmsP1().getRepositoryInstanceInfos();
      for (IRepositoryInstanceInfo iRepositoryInstanceInfo : repositoryInstanceInfos)
      {
         System.out.println(iRepositoryInstanceInfo);
      }
      Assert.assertEquals(3, repositoryInstanceInfos.size());
   }

   @Test
   public void test07LegacyIdAccess()
   {
      getDms().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", SYSTEM_REPO_ID));

      Document doc = getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/", SYSTEM_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      // access by id (including reopositoryId)
      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      // legacy access should default to system repository
      Assert.assertNotNull(getDms().getDocument(
            RepositoryIdUtils.stripRepositoryId(doc.getId())));
   }

   @Test
   public void test08LegacyIdAccessTestRepo()
   {
      getDms().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      // access by id (including reopositoryId)
      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      // legacy access should default to system repository
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.stripRepositoryId(doc.getId())));
   }

   @Test
   public void test09LegacyIdAccessTestRepo_p1()
   {
      getDmsP1().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDmsP1().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      // access by id (including reopositoryId)
      Assert.assertNotNull(getDmsP1().getDocument(doc.getId()));
      // legacy access should default to system repository
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.stripRepositoryId(doc.getId())));
   }

   @Test
   public void test10Separation()
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

   @Test
   public void test11Separation_p1()
   {
      getDmsP1().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDmsP1().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDmsP1().getDocument(doc.getId()));
      Assert.assertEquals(TEST_REPO_ID, getDmsP1().getDocument(doc.getId()).getRepositoryId());
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));
   }

   @Test
   public void test12SeparationWithVersioning()
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

      Document v0 = getDms().versionDocument(doc.getId(), "v0", "v0");
      Assert.assertNotEquals(v0.getRevisionId(), doc.getRevisionId());

      Assert.assertNotNull(getDms().getDocument(v0.getRevisionId()));
      Assert.assertEquals(TEST_REPO_ID, getDms().getDocument(v0.getRevisionId()).getRepositoryId());
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v0.getRevisionId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v0.getRevisionId(), null)));

      Document v1 = getDms().versionDocument(doc.getId(), "v1", "v1");
      Assert.assertNotEquals(v1.getRevisionId(), v0.getRevisionId());

      Assert.assertNotNull(getDms().getDocument(v1.getRevisionId()));
      Assert.assertEquals(TEST_REPO_ID, getDms().getDocument(v1.getRevisionId()).getRepositoryId());
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v1.getRevisionId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v1.getRevisionId(), null)));
   }

   @Test
   public void test13SeparationWithVersioning_p1()
   {
      getDmsP1().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDmsP1().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDmsP1().getDocument(doc.getId()));
      Assert.assertEquals(TEST_REPO_ID, getDmsP1().getDocument(doc.getId()).getRepositoryId());
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Document v0 = getDmsP1().versionDocument(doc.getId(), "v0", "v0");
      Assert.assertNotEquals(v0.getRevisionId(), doc.getRevisionId());

      Assert.assertNotNull(getDmsP1().getDocument(v0.getRevisionId()));
      Assert.assertEquals(TEST_REPO_ID, getDmsP1().getDocument(v0.getRevisionId()).getRepositoryId());
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v0.getRevisionId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v0.getRevisionId(), null)));

      Document v1 = getDmsP1().versionDocument(doc.getId(), "v1", "v1");
      Assert.assertNotEquals(v1.getRevisionId(), v0.getRevisionId());

      Assert.assertNotNull(getDmsP1().getDocument(v1.getRevisionId()));
      Assert.assertEquals(TEST_REPO_ID, getDmsP1().getDocument(v1.getRevisionId()).getRepositoryId());
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v1.getRevisionId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(v1.getRevisionId(), null)));
   }

   @Test
   public void test14FolderHierarchyIds()
   {
      getDms().createFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createFolderInfo("testFolder"));
      getDms().createFolder(
            RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID),
            DmsUtils.createFolderInfo("subFolder"));
      getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Folder testFolder = getDms().getFolder(
            RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID));
      Assert.assertEquals(TEST_REPO_ID,
            RepositoryIdUtils.extractRepositoryId(testFolder.getId()));

      Folder subFolder = testFolder.getFolders().get(0);
      Assert.assertEquals("subFolder", subFolder.getName());
      Assert.assertEquals(TEST_REPO_ID,
            RepositoryIdUtils.extractRepositoryId(subFolder.getId()));

      Document actualDocInFolder = testFolder.getDocuments().get(0);
      Assert.assertEquals("test.txt", actualDocInFolder.getName());
      Assert.assertEquals(TEST_REPO_ID,
            RepositoryIdUtils.extractRepositoryId(actualDocInFolder));

      Assert.assertNotNull(getDms().getFolder(subFolder.getId()));
      Assert.assertNotNull(getDms().getDocument(actualDocInFolder.getId()));

   }

   @Test
   public void test15FolderHierarchyIds_p1()
   {
      getDmsP1().createFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createFolderInfo("testFolder"));
      getDmsP1().createFolder(
            RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID),
            DmsUtils.createFolderInfo("subFolder"));
      getDmsP1().createDocument(
            RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Folder testFolder = getDmsP1().getFolder(
            RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID));
      Assert.assertEquals(TEST_REPO_ID,
            RepositoryIdUtils.extractRepositoryId(testFolder.getId()));

      Folder subFolder = testFolder.getFolders().get(0);
      Assert.assertEquals("subFolder", subFolder.getName());
      Assert.assertEquals(TEST_REPO_ID,
            RepositoryIdUtils.extractRepositoryId(subFolder.getId()));

      Document actualDocInFolder = testFolder.getDocuments().get(0);
      Assert.assertEquals("test.txt", actualDocInFolder.getName());
      Assert.assertEquals(TEST_REPO_ID,
            RepositoryIdUtils.extractRepositoryId(actualDocInFolder));

      Assert.assertNotNull(getDmsP1().getFolder(subFolder.getId()));
      Assert.assertNotNull(getDmsP1().getDocument(actualDocInFolder.getId()));

   }

   @Test
   public void test16SwitchDefaultRepository()
   {
      Assert.assertEquals(SYSTEM_REPO_ID, getDms().getDefaultRepository());

      getDms().setDefaultRepository(TEST_REPO_ID);

      getDms().removeDocument("/test.txt");

      Document doc = getDms().createDocument("/", DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNotNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Preferences preferences = sf.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_MANAGER,
            RepositoryProviderUtils.PREFERENCES_ID_SETTINGS);
      Assert.assertEquals(
            TEST_REPO_ID,
            preferences.getPreferences().get(
                  RepositoryProviderUtils.DEFAULT_REPOSITORY_ID));

      Assert.assertEquals(TEST_REPO_ID, getDms().getDefaultRepository());
   }

   @Test
   public void test17SwitchDefaultRepository_p1()
   {
      Assert.assertEquals(SYSTEM_REPO_ID, getDmsP1().getDefaultRepository());

      getDmsP1().setDefaultRepository(TEST_REPO_ID);

      getDmsP1().removeDocument("/test.txt");

      Document doc = getDmsP1().createDocument("/", DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDmsP1().getDocument(doc.getId()));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNotNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Preferences preferences = sf_p1.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_MANAGER,
            RepositoryProviderUtils.PREFERENCES_ID_SETTINGS);
      Assert.assertEquals(
            TEST_REPO_ID,
            preferences.getPreferences().get(
                  RepositoryProviderUtils.DEFAULT_REPOSITORY_ID));

      Assert.assertEquals(TEST_REPO_ID, getDmsP1().getDefaultRepository());
   }

   @Test
   public void test18SwitchBackDefaultRepository()
   {
      Assert.assertEquals(TEST_REPO_ID, getDms().getDefaultRepository());

      getDms().setDefaultRepository(null);

      getDms().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDms().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Preferences preferences = sf.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_MANAGER,
            RepositoryProviderUtils.PREFERENCES_ID_SETTINGS);
      Assert.assertEquals(
            SYSTEM_REPO_ID,
            preferences.getPreferences().get(
                  RepositoryProviderUtils.DEFAULT_REPOSITORY_ID));

      Assert.assertEquals(SYSTEM_REPO_ID, getDms().getDefaultRepository());
   }

   @Test
   public void test19SwitchBackDefaultRepository_p1()
   {
      Assert.assertEquals(TEST_REPO_ID, getDmsP1().getDefaultRepository());

      getDmsP1().setDefaultRepository(null);

      getDmsP1().removeDocument(
            RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDmsP1().createDocument(
            RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID),
            DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDmsP1().getDocument(doc.getId()));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDmsP1().getDocument(
            RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Preferences preferences = sf_p1.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_MANAGER,
            RepositoryProviderUtils.PREFERENCES_ID_SETTINGS);
      Assert.assertEquals(
            SYSTEM_REPO_ID,
            preferences.getPreferences().get(
                  RepositoryProviderUtils.DEFAULT_REPOSITORY_ID));

      Assert.assertEquals(SYSTEM_REPO_ID, getDmsP1().getDefaultRepository());
   }

   @Test(expected = DocumentManagementServiceException.class)
   public void test20SetInvalidDefaultRepository()
   {
      getDms().setDefaultRepository("invalid");

      Assert.fail();
   }

   @Test(expected = DocumentManagementServiceException.class)
   public void test21SetInvalidDefaultRepository_p1()
   {
      getDmsP1().setDefaultRepository("invalid");

      Assert.fail();
   }

   @Test(expected = DocumentManagementServiceException.class)
   public void test22BindAlreadyExisting()
   {
      getDms().bindRepository(RepositoryTestUtils.createTestRepoConfig());
   }

   @Test(expected = DocumentManagementServiceException.class)
   public void test23BindAlreadyExisting_p1()
   {
      getDmsP1().bindRepository(RepositoryTestUtils.createTestRepoConfig());
   }

   @Test
   public void test24Unbind()
   {
      getDms().unbindRepository(TEST_REPO_ID);

      Preferences preferences = sf.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_CONFIGURATIONS, TEST_REPO_ID);
      Assert.assertEquals(Collections.EMPTY_MAP, preferences.getPreferences());

      Assert.assertEquals(SYSTEM_REPO_ID, getDms().getDefaultRepository());
   }

   @Test
   public void test25Unbind_p1()
   {
      getDmsP1().unbindRepository(TEST_REPO_ID);

      Preferences preferences = sf_p1.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_CONFIGURATIONS, TEST_REPO_ID);
      Assert.assertEquals(Collections.EMPTY_MAP, preferences.getPreferences());

      Assert.assertEquals(SYSTEM_REPO_ID, getDmsP1().getDefaultRepository());
   }

   @Test(expected = PublicException.class)
   public void test26RequestDocumentNonExistingRepository()
   {
      getDms().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));
      Assert.fail("Exception should be thrown.");
   }

   @Test(expected = PublicException.class)
   public void test27RequestDocumentNonExistingRepository_p1()
   {
      getDmsP1().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));
      Assert.fail("Exception should be thrown.");
   }

}
