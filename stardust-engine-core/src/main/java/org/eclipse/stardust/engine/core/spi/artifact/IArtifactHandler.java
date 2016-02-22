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

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.api.runtime.ArtifactType;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;

/**
 *
 *
 * @author Roland.Stamm
 */
@SPI(status=Status.Stable, useRestriction=UseRestriction.Public)
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
    * This method is called before deploy and overwrite.
    * <p>
    * Handler can pre-process the artifact before it is deployed. E.g. compile, convert,
    * validate.
    * <p>
    * It is possible to change all fields of the RuntimeArtifact prior to deployment,<br>
    * e.g. the the artifactId to change the file ending for a compiled/processed artifact.
    *
    * @param runtimeArtifact
    *           The input artifact.
    * @return pre-processed artifact.
    */
   RuntimeArtifact preProcess(RuntimeArtifact runtimeArtifact);

   /**
    * Notifies after a runtime artifact is overwritten.
    *
    * @param deployedRuntimeArtifact
    */
   void afterOverwrite(DeployedRuntimeArtifact deployedRuntimeArtifact);

   /**
    * With this the artifact handler can e.g. check referential integrity and prevent
    * delete if the artifact is still being required.
    *
    * @param deployedRuntimeArtifact
    */
   void beforeDelete(DeployedRuntimeArtifact deployedRuntimeArtifact);

   /**
    * With this the artifact handler can purge caches after the artifact was deleted.
    *
    * @param oid
    */
   void afterDelete(long oid);
}
