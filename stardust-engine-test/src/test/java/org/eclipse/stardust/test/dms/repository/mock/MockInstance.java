package org.eclipse.stardust.test.dms.repository.mock;

import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.UserContext;

public class MockInstance implements IRepositoryInstance
{

   private String repositoryId;

   public MockInstance(String repositoryId)
   {
      this.repositoryId = repositoryId;
   }

   @Override
   public String getRepositoryId()
   {
      return repositoryId;
   }

   @Override
   public String getProviderId()
   {
      return "mock";
   }

   @Override
   public String getPartitionId()
   {
      return null;
   }

   @Override
   public IRepositoryInstanceInfo getRepositoryInstanceInfo()
   {
      return new IRepositoryInstanceInfo()
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
         public String getRepositoryVersion()
         {

            return null;
         }

         @Override
         public String getRepositoryType()
         {

            return null;
         }

         @Override
         public String getRepositoryName()
         {

            return null;
         }

         @Override
         public String getRepositoryId()
         {

            return null;
         }

         @Override
         public String getProviderId()
         {

            return null;
         }
      };
   }

   @Override
   public IRepositoryService getService(UserContext userContext)
   {
      return new MockService();
   }

   @Override
   public void close(IRepositoryService repositoryService)
   {

   }

}
