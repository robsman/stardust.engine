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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Date;

/**
 * A deployed runtime artifact is including a unique oid.<br>
 * It does not contain binary content like the {@link RuntimeArtifact}.
 * <p>
 * The {@link RuntimeArtifact} including the content can be retrieved by oid
 * using other service methods.
 *
 * @author Roland.Stamm
 *
 * @see AdministrationService#getRuntimeArtifact(long)
 * @see QueryService#getRuntimeArtifact(long)
 */
public interface DeployedRuntimeArtifact extends Serializable
{
   /**
    * @return A unique identifier for a deployed runtime artifact.
    */
   public long getOid();

   /**
    * @return Identifies the {@link ArtifactType}.
    *
    * @see ArtifactType#getId()
    */
   public String getArtifactTypeId();

   /**
    * @return Identifies the artifact.
    */
   public String getArtifactId();

   /**
    * @return A human readable name for the artifact.
    */
   public String getArtifactName();

   /**
    * @return Specifies the point in time the artifact should start to be valid and therefore become active.
    */
   public Date getValidFrom();

}

