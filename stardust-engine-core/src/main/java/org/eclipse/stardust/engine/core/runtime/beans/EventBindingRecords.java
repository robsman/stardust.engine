/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IEventHandler;

public class EventBindingRecords
{
   private Set<Key> deleted = CollectionUtils.newSet();

   public void markDeleted(int objectType, long oid, IEventHandler handler, short partitionOid)
   {
      deleted.add(new Key(objectType, oid, handler, partitionOid));
   }
   
   public boolean isDeleted(int objectType, long oid, IEventHandler handler, short partitionOid)
   {
      return deleted.contains(new Key(objectType, oid, handler, partitionOid));
   }

   private static class Key
   {
      private int objectType;
      private long oid;
      private IEventHandler handler;
      private short partitionOid;
      
      private Key(int objectType, long oid, IEventHandler handler, short partitionOid)
      {
         this.objectType = objectType >= EventUtils.DEACTIVE_TYPE ? objectType - EventUtils.DEACTIVE_TYPE : objectType;
         this.oid = oid;
         this.handler = handler;
         this.partitionOid = partitionOid;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((handler == null) ? 0 : handler.hashCode());
         result = prime * result + objectType;
         result = prime * result + (int) (oid ^ (oid >>> 32));
         result = prime * result + partitionOid;
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         Key other = (Key) obj;
         if (handler == null)
         {
            if (other.handler != null)
               return false;
         }
         else if (!handler.equals(other.handler))
            return false;
         if (objectType != other.objectType)
            return false;
         if (oid != other.oid)
            return false;
         if (partitionOid != other.partitionOid)
            return false;
         return true;
      }
   }
}
