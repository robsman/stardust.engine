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

import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeArtifact;

public class DeployedRuntimeArtifactDetails extends RuntimeArtifactInfo implements DeployedRuntimeArtifact
{
   private static final long serialVersionUID = -91103447472360439L;

   private long oid;

   public DeployedRuntimeArtifactDetails(IRuntimeArtifact rab)
   {
      super(rab.getArtifactTypeId(), rab.getArtifactId(), rab.getArtifactName(), rab.getValidFrom());
      this.oid = rab.getOID();
   }

   public long getOid()
   {
      return oid;
   }

}
