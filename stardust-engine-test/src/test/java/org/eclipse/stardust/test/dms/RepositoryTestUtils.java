package org.eclipse.stardust.test.dms;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;

public class RepositoryTestUtils
{
   public static final String DEFAULT_REPO_ID = "default";

   public static final String TEST_REPO_ID = "testRepo";

   public static final String TEST_PROVIDER_ID = "jcr-vfs";

   public static final String TEST_REPO_JNDI = "jcr/ContentRepositoryNoSec";

   public static IRepositoryConfiguration createTestRepoConfig()
   {
      Map<String, Serializable> attributes = CollectionUtils.newMap();
      attributes.put(IRepositoryConfiguration.PROVIDER_ID, TEST_PROVIDER_ID);
      attributes.put(IRepositoryConfiguration.REPOSITORY_ID, TEST_REPO_ID);
      attributes.put(JcrVfsRepositoryConfiguration.JNDI_NAME, TEST_REPO_JNDI);

      attributes.put(JcrVfsRepositoryConfiguration.DISABLE_CAPABILITY_VERSIONING, true);
      return new JcrVfsRepositoryConfiguration(attributes);
   }

}
