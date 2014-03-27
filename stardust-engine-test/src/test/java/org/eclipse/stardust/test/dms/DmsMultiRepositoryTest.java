package org.eclipse.stardust.test.dms;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
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

@FixMethodOrder(MethodSorters.JVM)
public class DmsMultiRepositoryTest
{
 private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static final String TEST_PROVIDER_ID = "jcr-vfs";
   
   private static final String DEFAULT_REPO_ID = "default";
   
   private static final String TEST_REPO_ID = "testRepo";
   
   private DocumentManagementService getDms()
   {
      return sf.getDocumentManagementService();
   }

   private IRepositoryConfiguration createTestRepo2Config()
   {
      Map<String, Serializable> attributes = CollectionUtils.newMap();
      attributes.put(IRepositoryConfiguration.PROVIDER_ID, TEST_PROVIDER_ID);
      attributes.put(IRepositoryConfiguration.REPOSITORY_ID, TEST_REPO_ID);
      attributes.put(JcrVfsRepositoryConfiguration.IS_IN_MEMORY_TEST_REPO, "true");
      attributes.put(JcrVfsRepositoryConfiguration.REPOSITORY_CONFIG_LOCATION, "test-repo-no-sec.xml");
      attributes.put(JcrVfsRepositoryConfiguration.CONFIG_DISABLE_VERSIONING, "true");
      return new JcrVfsRepositoryConfiguration(attributes);
   }

   @Test
   public void testBind()
   {
      getDms().bindRepository(createTestRepo2Config());
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
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), DEFAULT_REPO_ID)));
      Assert.assertNull(getDms().getDocument(RepositoryIdUtils.replaceRepositoryId(doc.getId(), null)));
   }

   @Test(expected=PublicException.class)
   public void testBindAlreadyExisting()
   {
      getDms().bindRepository(createTestRepo2Config());
   }
   
   @Test
   public void testUnbind()
   {     
      getDms().unbindRepository(TEST_REPO_ID);
   }
   
   @Test(expected=PublicException.class)
   public void testUnbindNonExisting()
   {
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
