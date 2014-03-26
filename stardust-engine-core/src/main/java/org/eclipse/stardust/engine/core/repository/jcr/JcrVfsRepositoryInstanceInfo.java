/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.repository.jcr;

import javax.jcr.Repository;

import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;

public class JcrVfsRepositoryInstanceInfo implements IRepositoryInstanceInfo
{
   private static final long serialVersionUID = -3437673937195283480L;
   
   private String repositoryId;

   private String repositoryType;

   private String repositoryName;

   private String repositoryVersion;

   private boolean accessControlPolicySupported;
   private boolean fullTextSearchSupported;
   private boolean metaDataSearchSupported;
   private boolean metaDataStorageSupported;
   private boolean streamingIOSupported;
   private boolean transactionSupported;
   private boolean versioningSupported;
   private boolean writeSupported;


   public JcrVfsRepositoryInstanceInfo(String repositoryId)
   {
      this.repositoryId = repositoryId;
      this.repositoryType = "JCR2.0";
      this.repositoryName = "Jackrabbit default repository";
      this.repositoryVersion = "2.6.1";
      
      this.accessControlPolicySupported = true;
      this.fullTextSearchSupported = true;
      this.metaDataSearchSupported = true;
      this.metaDataStorageSupported = true;
      this.streamingIOSupported = true;
      this.transactionSupported = true;
      this.versioningSupported = true;
   }

   public JcrVfsRepositoryInstanceInfo(String repositoryId, Repository repository, IRepositoryConfiguration configuration)
   {
      this.repositoryId = repositoryId;
      this.repositoryType = repository.getDescriptor(Repository.SPEC_NAME_DESC) + " " + repository.getDescriptor(Repository.SPEC_VERSION_DESC);
      this.repositoryName = repository.getDescriptor(Repository.REP_NAME_DESC);
      this.repositoryVersion = repository.getDescriptor(Repository.REP_VERSION_DESC);
      
      this.accessControlPolicySupported = Boolean.valueOf(repository.getDescriptor(Repository.OPTION_ACCESS_CONTROL_SUPPORTED));
      this.fullTextSearchSupported = Boolean.valueOf(repository.getDescriptor(Repository.QUERY_FULL_TEXT_SEARCH_SUPPORTED));
      this.metaDataSearchSupported = true;
      this.metaDataStorageSupported = true;
      this.streamingIOSupported = true;
      this.transactionSupported = Boolean.valueOf(repository.getDescriptor(Repository.OPTION_TRANSACTIONS_SUPPORTED));
      this.versioningSupported = Boolean.valueOf(repository.getDescriptor(Repository.OPTION_VERSIONING_SUPPORTED))
            && !configuration.getAttributes().containsKey(JcrVfsRepositoryConfiguration.CONFIG_DISABLE_VERSIONING);
   }

   @Override
   public String getProviderId()
   {
      return JcrVfsRepositoryProvider.PROVIDER_ID;
   }

   @Override
   public String getRepositoryId()
   {
      return this.repositoryId;
   }

   @Override
   public String getRepositoryType()
   {
      return repositoryType;
   }
   
   @Override
   public String getRepositoryName()
   {
      return repositoryName;
   }

   @Override
   public String getRepositoryVersion()
   {
      return repositoryVersion;
   }

   @Override
   public boolean isVersioningSupported()
   {
      return versioningSupported;
   }

   @Override
   public boolean isTransactionSupported()
   {
      return transactionSupported;
   }

   @Override
   public boolean isStreamingIOSupported()
   {
      return streamingIOSupported;
   }

   @Override
   public boolean isMetaDataSearchSupported()
   {
      return metaDataSearchSupported;
   }

   @Override
   public boolean isMetaDataStorageSupported()
   {
      return metaDataStorageSupported;
   }

   @Override
   public boolean isFullTextSearchSupported()
   {
      return fullTextSearchSupported;
   }

   @Override
   public boolean isAccessControlPolicySupported()
   {
      return accessControlPolicySupported;
   }
   
   @Override
   public boolean isWriteSupported()
   {
      return writeSupported;
   }
   
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("JcrVfsRepositoryInstanceInfo [getProviderId()=");
      builder.append(getProviderId());
      builder.append(", getRepositoryId()=");
      builder.append(getRepositoryId());
      builder.append(", getRepositoryType()=");
      builder.append(getRepositoryType());
      builder.append(", getRepositoryName()=");
      builder.append(getRepositoryName());
      builder.append(", getRepositoryVersion()=");
      builder.append(getRepositoryVersion());
      builder.append(", isVersioningSupported()=");
      builder.append(isVersioningSupported());
      builder.append(", isTransactionSupported()=");
      builder.append(isTransactionSupported());
      builder.append(", isStreamingIOSupported()=");
      builder.append(isStreamingIOSupported());
      builder.append(", isMetaDataSearchSupported()=");
      builder.append(isMetaDataSearchSupported());
      builder.append(", isMetaDataStorageSupported()=");
      builder.append(isMetaDataStorageSupported());
      builder.append(", isFullTextSearchSupported()=");
      builder.append(isFullTextSearchSupported());
      builder.append(", isAccessControlPolicySupported()=");
      builder.append(isAccessControlPolicySupported());
      builder.append(", isWriteSupported()=");
      builder.append(isWriteSupported());
      builder.append("]");
      return builder.toString();
   }
}