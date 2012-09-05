/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.spi.cluster;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;

/**
 * <p>
 * This class' only purpose is to ensure both safe publication and lazy initialization
 * (see 'lazy initialization class holder' idiom)
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class ClusterSafeObjectProviderHolder
{
   public static final ClusterSafeObjectProvider OBJ_PROVIDER = initClusterSafeObjProvider();
   
   private static ClusterSafeObjectProvider initClusterSafeObjProvider()
   {
      final ClusterSafeObjectProvider objProvider = ExtensionProviderUtils.getFirstExtensionProvider(ClusterSafeObjectProvider.class, KernelTweakingProperties.CLUSTER_SAFE_OBJ_PROVIDER);
      if (objProvider == null)
      {
         throw new IllegalStateException("No cluster safe object provider could be found.");
      }
      
      return objProvider;
   }
}
