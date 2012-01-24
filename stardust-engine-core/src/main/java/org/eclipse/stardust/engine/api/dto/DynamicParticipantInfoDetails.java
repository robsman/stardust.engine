/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;

public abstract class DynamicParticipantInfoDetails extends ParticipantInfoDetails
      implements DynamicParticipantInfo
{
   private static final long serialVersionUID = 1L;
   
   private long oid;

   public DynamicParticipantInfoDetails(long oid, String id, String name)
   {
      super(id, name);
      this.oid = oid;
   }

   public long getOID()
   {
      return oid;
   }

   @Override
   public int hashCode()
   {
      return 31 * super.hashCode() + (int) (oid ^ (oid >>> 32));
   }

   @Override
   public boolean equals(Object obj)
   {
      return this == obj || obj instanceof DynamicParticipantInfo && oid == ((DynamicParticipantInfo) obj).getOID();
   }
}
