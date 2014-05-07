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

import static org.eclipse.stardust.test.dms.RepositoryTestUtils.SYSTEM_REPO_ID;
import static org.eclipse.stardust.test.dms.RepositoryTestUtils.TEST_REPO_ID;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryProvider;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderUtils;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * Tests binding and unbinding repository instances and separation of documents.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class DmsMultiRepositoryTest
{
 private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DmsModelConstants.DMS_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);


   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   @Test
   public void testConfigurationTemplate()
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

   @Test
   public void testBind()
   {
      IRepositoryConfiguration config = RepositoryTestUtils.createTestRepoConfig();
      getDms().bindRepository(config);

      Preferences preferences = sf.getQueryService().getPreferences(PreferenceScope.PARTITION, RepositoryProviderUtils.MODULE_ID_REPOSITORY_CONFIGURATIONS, TEST_REPO_ID);
      Assert.assertEquals(config.getAttributes(), preferences.getPreferences());
   }

   @Test
   public void testListing()
   {

      List<IRepositoryProviderInfo> repositoryProviderInfos = getDms().getRepositoryProviderInfos();
      System.out.println(repositoryProviderInfos);
      for (IRepositoryProviderInfo iRepositoryProviderInfo : repositoryProviderInfos)
      {
         System.out.println(iRepositoryProviderInfo);
      }

      List<IRepositoryInstanceInfo> repositoryInstanceInfos = getDms().getRepositoryInstanceInfos();
      for (IRepositoryInstanceInfo iRepositoryInstanceInfo : repositoryInstanceInfos)
      {
         System.out.println(iRepositoryInstanceInfo);
      }
   }

   @Test
   public void testSeparation()
   {
      getDms().removeDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDms().createDocument(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));
   }

   @Test
   public void testFolderHierarchyIds()
   {
      getDms().createFolder(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), DmsUtils.createFolderInfo("testFolder"));
      getDms().createFolder(RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID), DmsUtils.createFolderInfo("subFolder"));
      getDms().createDocument(RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID), DmsUtils.createDocumentInfo("test.txt"));

      Folder testFolder = getDms().getFolder(RepositoryIdUtils.addRepositoryId("/testFolder", TEST_REPO_ID));
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(testFolder.getId()));

      Folder subFolder = testFolder.getFolders().get(0);
      Assert.assertEquals("subFolder", subFolder.getName());
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(subFolder.getId()));

      Document actualDocInFolder = testFolder.getDocuments().get(0);
      Assert.assertEquals("test.txt", actualDocInFolder.getName());
      Assert.assertEquals(TEST_REPO_ID, RepositoryIdUtils.extractRepositoryId(actualDocInFolder));

      Assert.assertNotNull(getDms().getFolder(subFolder.getId()));
      Assert.assertNotNull(getDms().getDocument(actualDocInFolder.getId()));

   }

   @Test
   public void testSwitchDefaultRepository()
   {
      getDms().setDefaultRepository(TEST_REPO_ID);

      getDms().removeDocument("/test.txt");

      Document doc = getDms().createDocument("/", DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNotNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Preferences preferences = sf.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_MANAGER,
            RepositoryProviderUtils.PREFERENCES_ID_SETTINGS);
      Assert.assertEquals(
            TEST_REPO_ID,
            preferences.getPreferences().get(
                  RepositoryProviderUtils.DEFAULT_REPOSITORY_ID));
   }

   @Test
   public void testSwitchBackDefaultRepository()
   {
      getDms().setDefaultRepository(null);

      getDms().removeDocument(RepositoryIdUtils.addRepositoryId("/test.txt", TEST_REPO_ID));

      Document doc = getDms().createDocument(RepositoryIdUtils.addRepositoryId("/", TEST_REPO_ID), DmsUtils.createDocumentInfo("test.txt"));

      Assert.assertNotNull(getDms().getDocument(doc.getId()));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), SYSTEM_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));

      Preferences preferences = sf.getQueryService().getPreferences(
            PreferenceScope.PARTITION,
            RepositoryProviderUtils.MODULE_ID_REPOSITORY_MANAGER,
            RepositoryProviderUtils.PREFERENCES_ID_SETTINGS);
      Assert.assertEquals(
            SYSTEM_REPO_ID,
            preferences.getPreferences().get(
                  RepositoryProviderUtils.DEFAULT_REPOSITORY_ID));
   }

   @Test(expected=DocumentManagementServiceException.class)
   public void testSetInvalidDefaultRepository()
   {
      getDms().setDefaultRepository("invalid");

      Assert.fail();
   }

   @Test(expected=DocumentManagementServiceException.class)
   public void testBindAlreadyExisting()
   {
      getDms().bindRepository(RepositoryTestUtils.createTestRepoConfig());
   }

   @Test
   public void testUnbind()
   {
      getDms().unbindRepository(TEST_REPO_ID);

      Preferences preferences = sf.getQueryService().getPreferences(PreferenceScope.PARTITION, RepositoryProviderUtils.MODULE_ID_REPOSITORY_CONFIGURATIONS, TEST_REPO_ID);
      Assert.assertEquals(Collections.EMPTY_MAP, preferences.getPreferences());
   }

   public void testUnbindNonExisting()
   {
      // Unbinding a non existing repository throws no exception, it just removes the
      // configuration from the preference store if it exists.
      getDms().unbindRepository(TEST_REPO_ID);
   }

   @Test(expected=PublicException.class)
   public void testRequestDocumentNonExistingRepository()
   {
      getDms().getDocument(RepositoryIdUtils.addRepositoryId("/test.txt",
            TEST_REPO_ID));
      Assert.fail("Exception should be thrown.");
   }

}
