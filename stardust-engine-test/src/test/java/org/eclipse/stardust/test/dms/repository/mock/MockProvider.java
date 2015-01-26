package org.eclipse.stardust.test.dms.repository.mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProvider;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;

public class MockProvider implements IRepositoryProvider, IRepositoryProvider.Factory
{

   public static final String PROVIDER_ID = "mock";

   public static final String DEFAULT_REPO_ID = PROVIDER_ID + "_repo";

   @Override
   public IRepositoryProvider getInstance()
   {
      return new MockProvider();
   }

   @Override
   public String getProviderId()
   {
      return PROVIDER_ID;
   }

   @Override
   public List<IRepositoryConfiguration> getDefaultConfigurations()
   {
      IRepositoryConfiguration defaultConfig = new IRepositoryConfiguration()
      {
         private static final long serialVersionUID = 1L;
         private Map<String, Serializable> map = CollectionUtils.newMap();

         @Override
         public Map<String, Serializable> getAttributes()
         {
            map.put(IRepositoryConfiguration.PROVIDER_ID, MockProvider.PROVIDER_ID);
            map.put(IRepositoryConfiguration.REPOSITORY_ID, MockProvider.DEFAULT_REPO_ID);
            return map;
         }
      };

      return Collections.singletonList(defaultConfig);
   }

   @Override
   public IRepositoryInstance createInstance(IRepositoryConfiguration configuration,
         String partitionId)
   {
      return new MockInstance((String) configuration.getAttributes().get(
            IRepositoryConfiguration.REPOSITORY_ID));
   }

   @Override
   public void destroyInstance(IRepositoryInstance instance)
   {

   }

   @Override
   public IRepositoryProviderInfo getProviderInfo()
   {
      return new IRepositoryProviderInfo()
      {
         private static final long serialVersionUID = 1L;

         @Override
         public boolean isWriteSupported()
         {
            return false;
         }

         @Override
         public boolean isVersioningSupported()
         {
            return false;
         }

         @Override
         public boolean isTransactionSupported()
         {
            return false;
         }

         @Override
         public boolean isMetaDataWriteSupported()
         {
            return false;
         }

         @Override
         public boolean isMetaDataSearchSupported()
         {
            return false;
         }

         @Override
         public boolean isFullTextSearchSupported()
         {
            return false;
         }

         @Override
         public boolean isAccessControlPolicySupported()
         {
            return false;
         }

         @Override
         public String getProviderName()
         {
            return "Mocked Repostory";
         }

         @Override
         public String getProviderId()
         {
            return PROVIDER_ID;
         }

         @Override
         public IRepositoryConfiguration getConfigurationTemplate()
         {
            return null;
         }
      };
   }

}
