/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Collections;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class PrefStoreAwareConfigurationVariablesProvider
      extends DefaultConfigurationVariablesProvider
{
   private AdministrationService as = null;

   public PrefStoreAwareConfigurationVariablesProvider()
   {
      super();
   }

   /*
    * public PrefStoreAwareConfigurationVariablesProvider(AdministrationService as) {
    * super(); this.as = as; }
    */

   @Override
   protected void init(String modelId)
   {
      if (StringUtils.isEmpty(modelId))
      {
         super.init(modelId);
      }
      else
      {
         if (as == null)
         {
            boolean hasLayer = false;
            if (SecurityProperties.getUser() == null)
            {
               hasLayer = pushTransientUserPropertyLayer();

            }
            try
            {
               setConfVariables(ConfigurationVariableUtils.getConfigurationVariables(
                     PreferenceStorageFactory.getCurrent(), modelId, false, true));
            }
            finally
            {
               if (hasLayer)
               {
                  ParametersFacade.popLayer();
               }
            }

         }
         else
         {
            // as.getConfigurationVariables(model)
         }
      }
   }

   private boolean pushTransientUserPropertyLayer()
   {
      IAuditTrailPartition partition = SecurityProperties.getPartition();

      if (partition == null)
      {
         throw new PublicException(BpmRuntimeError.MDL_PARTITION_NOT_INITIALIZED.raise());
      }

      IUser systemUser = UserBean.getSystemUser(partition);

      PropertyLayer pushLayer = ParametersFacade.pushLayer(
            Collections.singletonMap(SecurityProperties.CURRENT_USER, systemUser));
      pushLayer.setProperty(SecurityProperties.CURRENT_PARTITION_OID, partition.getOID());
      pushLayer.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, systemUser.getDomainOid());

      pushLayer.setProperty(SynchronizationService.PRP_DISABLE_SYNCHRONIZATION, true);
      pushLayer.setProperty(SecurityProperties.AUTHORIZATION_SYNC_LOAD_PROPERTY, false);
      return true;
   }
}
