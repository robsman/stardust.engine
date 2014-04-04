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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;

public class JcrVfsRepositoryProviderInfo implements IRepositoryProviderInfo
{
   private static final long serialVersionUID = 333606184663729328L;

   @Override
   public IRepositoryConfiguration getConfigurationTemplate()
   {
      Map<String, Serializable> configurationTemplate = new TreeMap<String, Serializable>();
      configurationTemplate.put(IRepositoryConfiguration.PROVIDER_ID, JcrVfsRepositoryProvider.PROVIDER_ID);
      configurationTemplate.put(IRepositoryConfiguration.REPOSITORY_ID, "newJcrRepository");
      configurationTemplate.put(JcrVfsRepositoryConfiguration.JNDI_NAME, "java:/jcr/newJcrRepository");
      
      return new JcrVfsRepositoryConfiguration(configurationTemplate);
   }

   @Override
   public String getProviderId()
   {
      return JcrVfsRepositoryProvider.PROVIDER_ID;
   }

   @Override
   public String getProviderName()
   {
      return "JCR 2.0 Provider";
   }

   @Override
   public boolean isVersioningSupported()
   {
      return true;
   }

   @Override
   public boolean isTransactionSupported()
   {
      return true;
   }

   @Override
   public boolean isStreamingIOSupported()
   {
      return true;
   }

   @Override
   public boolean isMetaDataSearchSupported()
   {
      return true;
   }
   
   @Override
   public boolean isMetaDataWriteSupported()
   {
      return true;
   }

   @Override
   public boolean isFullTextSearchSupported()
   {
      return true;
   }

   @Override
   public boolean isAccessControlPolicySupported()
   {
      return true;
   }
   
   @Override
   public boolean isWriteSupported()
   {
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("JcrVfsRepositoryProviderInfo [getConfigurationTemplate()=");
      builder.append(getConfigurationTemplate());
      builder.append(", getProviderId()=");
      builder.append(getProviderId());
      builder.append(", getProviderName()=");
      builder.append(getProviderName());
      builder.append(", isVersioningSupported()=");
      builder.append(isVersioningSupported());
      builder.append(", isTransactionSupported()=");
      builder.append(isTransactionSupported());
      builder.append(", isStreamingIOSupported()=");
      builder.append(isStreamingIOSupported());
      builder.append(", isMetaDataSearchSupported()=");
      builder.append(isMetaDataSearchSupported());
      builder.append(", isMetaDataWriteSupported()=");
      builder.append(isMetaDataWriteSupported());
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
