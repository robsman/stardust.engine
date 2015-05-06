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

   public String getArtifactTypeId()
   {
      return artifactTypeId;
   }

   public String getArtifactId()
   {
      return artifactId;
   }

   public String getArtifactName()
   {
      return artifactName;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

}
