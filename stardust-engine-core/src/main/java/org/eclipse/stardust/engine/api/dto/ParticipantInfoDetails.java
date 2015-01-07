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

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;


public abstract class ParticipantInfoDetails implements ParticipantInfo
{
   private static final long serialVersionUID = 1L;

   private String id;
   private String name;

   public ParticipantInfoDetails(String id, String name)
   {
      this.id = id;
      this.name = name;
   }

   public String getQualifiedId()
   {
      return id;
   }   
   
   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   @Override
   public String toString()
   {
      return name == null ? id : name;
   }

   @Override
   public int hashCode()
   {
      return id == null ? 31 : 31 + id.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      return this == obj || obj instanceof ParticipantInfo && CompareHelper.areEqual(id, ((ParticipantInfo) obj).getId());
   }
}