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

import org.eclipse.stardust.engine.api.runtime.ArtifactType;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;

public interface IArtifactHandler
{

   /**
    * Factory for {@link IArtifactHandler}.
    */
   public interface Factory
   {
      IArtifactHandler getInstance();
   }

   /**
    * @return the supported artifact type.
    */
   ArtifactType getArtifactType();

   /**
    * Should return the MIME-type for the artifact.
    *
    * @param runtimeArtifact
    * @return MIME-type string.
    */
   String getArtifactContentType(RuntimeArtifact runtimeArtifact);

   /**
    * Handler can pre-process the artifact before it is deployed.
    * E.g. compile, convert, validate
    *
    * @param runtimeArtifact The input artifact.
    * @return pre-processed artifact.
    */
   RuntimeArtifact preProcess(RuntimeArtifact runtimeArtifact);

   /**
    * Handler can check reference integrity and prevent delete.
    *
    * @param deployedRuntimeArtifact
    */
   void beforeDelete(DeployedRuntimeArtifact deployedRuntimeArtifact);
}
