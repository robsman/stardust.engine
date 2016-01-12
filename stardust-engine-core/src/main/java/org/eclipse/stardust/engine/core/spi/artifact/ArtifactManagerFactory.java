/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.core.spi.artifact;

import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

public class ArtifactManagerFactory
{

   public static ArtifactManager getCurrent()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      if (rtEnv == null)
      {
         rtEnv = new BpmRuntimeEnvironment(null);
         PropertyLayerProviderInterceptor.setCurrent(rtEnv);
      }

      ArtifactManager artifactManager = rtEnv.getArtifactManager();
      if (artifactManager == null)
      {
         artifactManager = new ArtifactManager();
         rtEnv.setArtifactManager(artifactManager);
      }

      return artifactManager;
   }

}
