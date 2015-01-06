/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import org.eclipse.stardust.engine.core.runtime.setup.DataSlotFieldInfo.SLOT_TYPE;


public class DataSlotFieldInfoKey extends FieldInfoKey
{
   private final DataSlotFieldInfo dataSlotFieldInfo;
   private DataSlotKey key;
   private SLOT_TYPE slotType;

   public DataSlotFieldInfoKey(DataSlotFieldInfo dataSlotFieldInfo)
   {
      super(dataSlotFieldInfo);
      this.dataSlotFieldInfo = dataSlotFieldInfo;
      
      if(dataSlotFieldInfo != null)
      {
         key = new DataSlotKey(dataSlotFieldInfo.getDataSlot());
         slotType = dataSlotFieldInfo.getSlotType();
      }
   }

   public DataSlot getDataSlot()
   {
      if(dataSlotFieldInfo != null)
      {
         return dataSlotFieldInfo.getDataSlot();
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
