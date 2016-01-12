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
 * Abstract base type containing common fields for runtime artifacts.
 *
 * @author Roland.Stamm
 */
public abstract class RuntimeArtifactInfo implements Serializable
{
   private static final long serialVersionUID = -6123498431762201931L;

   protected String artifactTypeId;

   protected String artifactId;

   protected String artifactName;

   protected Date validFrom;

   public RuntimeArtifactInfo()
   {
   }

   public RuntimeArtifactInfo(String artifactTypeId, String artifactId,
         String artifactName, Date validFrom)
   {
      super();
      this.artifactTypeId = artifactTypeId;
      this.artifactId = artifactId;
      this.artifactName = artifactName;
      this.validFrom = validFrom;
   }

   /**
    * @return Identifies the {@link ArtifactType}.
    *
    * @see ArtifactType#getId()
    */
   public String getArtifactTypeId()
   {
      return artifactTypeId;
   }

   /**
    * @return Identifies the artifact.
    */
   public String getArtifactId()
   {
      return artifactId;
   }

   /**
    * @return A human readable name for the artifact.
    */
   public String getArtifactName()
   {
      return artifactName;
   }

   /**
    * @return Specifies the point in time the artifact should start to be valid and therefore become active.
    */
   public Date getValidFrom()
   {
      return validFrom;
   }

}
