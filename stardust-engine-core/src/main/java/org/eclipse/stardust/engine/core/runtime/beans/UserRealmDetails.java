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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.text.MessageFormat;

import org.eclipse.stardust.engine.api.runtime.UserRealm;


public class UserRealmDetails implements UserRealm
{
   private static final long serialVersionUID = 1L;
   
   private long oid;
   private String id;
   private String name;
   private String description;
   private short partitionOid;
   private String partitionId;
   
   public UserRealmDetails(IUserRealm realm)
   {
      oid = realm.getOID();
      id = realm.getId();
      name = realm.getName();
      partitionOid = realm.getPartition().getOID();
      partitionId = realm.getPartition().getId();
      description = realm.getDescription();
   }

   public long getOID()
   {
      return oid;
   }

   public int getModelOID()
   {
      return 0;
   }

   public int getModelElementOID()
   {
      return 0;
   }

   public String getModelElementID()
   {
      return null;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   public short getPartitionOid()
   {
      return partitionOid;
   }

   public String getPartitionId()
   {
      return partitionId;
   }

   public boolean equals(Object rawRhs)
   {
      return this == rawRhs
         || rawRhs instanceof UserRealmDetails
            && ((UserRealmDetails) rawRhs).getOID() == oid;
   }
   
   public int hashCode()
   {
      return 31 + (int) (oid ^ (oid >>> 32));
   }

   public String toString()
   {
      return MessageFormat.format("User realm ''{0}''.", new Object[] { getId() });
   }
}
