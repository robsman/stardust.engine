/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import org.eclipse.stardust.engine.core.runtime.setup.ClusterSlotFieldInfo.SLOT_TYPE;


public class DataSlotFieldInfoKey extends FieldInfoKey
{
   private final ClusterSlotFieldInfo clusterSlotFieldInfo;
   private DataSlotKey key;
   private SLOT_TYPE slotType;

   public DataSlotFieldInfoKey(ClusterSlotFieldInfo clusterSlotFieldInfo)
   {
      super(clusterSlotFieldInfo);
      this.clusterSlotFieldInfo = clusterSlotFieldInfo;

      if(clusterSlotFieldInfo != null)
      {
         key = new DataSlotKey(clusterSlotFieldInfo.getClusterSlot());
         slotType = clusterSlotFieldInfo.getSlotType();
      }
   }

   public AbstractDataClusterSlot getDataSlot()
   {
      if(clusterSlotFieldInfo != null)
      {
         return clusterSlotFieldInfo.getClusterSlot();
      }

      return null;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((slotType == null) ? 0 : slotType.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      DataSlotFieldInfoKey other = (DataSlotFieldInfoKey) obj;
      if (key == null)
      {
         if (other.key != null)
            return false;
      }
      else if (!key.equals(other.key))
         return false;
      if (slotType != other.slotType)
         return false;
      return true;
   }
}
