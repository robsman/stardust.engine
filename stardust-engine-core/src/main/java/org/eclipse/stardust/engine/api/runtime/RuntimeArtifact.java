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

import java.util.Date;

public class RuntimeArtifact extends RuntimeArtifactInfo
{
   private static final long serialVersionUID = -5192268364263524268L;

   protected byte[] content;

   public RuntimeArtifact()
   {
      super();
   }

   public RuntimeArtifact(String artifactTypeId, String artifactId, String artifactName,
         byte[] content, Date validFrom)
   {
      super(artifactTypeId, artifactId, artifactName, validFrom);
      this.content = content;
   }

   public byte[] getContent()
   {
      return content;
   }

   public void setContent(byte[] content)
   {
      this.content = content;
   }

}
