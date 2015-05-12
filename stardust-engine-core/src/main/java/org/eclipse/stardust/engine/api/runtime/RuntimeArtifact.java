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

import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;

/**
 * An {@link RuntimeArtifact} is an artifact which is deployable and then valid at
 * runtime.
 * <p>
 * Each runtime artifact consists of:
 * <pre>
 * artifactTypeId - Identifies the {@link ArtifactType}.
 * artifactId - Identifies the runtime artifact.
 * artifactName - A human readable name or description.
 * validFrom - Specifies the point in time the artifact should start to be valid and therefore become active.
 * content - The binary content of the runtime artifact.
 * </pre>
 * To find out which runtime artifact is active at a certain date the
 * {@link DeployedRuntimeArtifactQuery} can be used on the {@link QueryService}.
 * <p>
 *
 * @author Roland.Stamm
 */
public class RuntimeArtifact extends RuntimeArtifactInfo
{
   private static final long serialVersionUID = -5192268364263524268L;

   protected byte[] content;

   public RuntimeArtifact()
   {
      super();
   }

   /**
    * @param artifactTypeId Identifies the {@link ArtifactType}.
    * @param artifactId Identifies the runtime artifact.
    * @param artifactName A human readable name or description.
    * @param validFrom Specifies the point in time the artifact should start to be valid and therefore become active.
    * @param content The binary content of the runtime artifact.
    */
   public RuntimeArtifact(String artifactTypeId, String artifactId, String artifactName,
         byte[] content, Date validFrom)
   {
      super(artifactTypeId, artifactId, artifactName, validFrom);
      this.content = content;
   }

   /**
    * @return the content of the artifact.
    */
   public byte[] getContent()
   {
      return content;
   }

   /**
    * @param content the content of the artifact.
    */
   public void setContent(byte[] content)
   {
      this.content = content;
   }

}
