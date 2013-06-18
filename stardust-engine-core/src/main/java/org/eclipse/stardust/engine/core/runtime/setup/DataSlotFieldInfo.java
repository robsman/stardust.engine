/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;

public class DataSlotFieldInfo extends FieldInfo
{
   private final DataSlotFieldInfo.SLOT_TYPE slotType;
   private final DataSlot dataSlot;

   public DataSlotFieldInfo(String name, Class type, DataSlotFieldInfo.SLOT_TYPE slotType, DataSlot dataSlot)
   {
      super(name, type);
      this.slotType = slotType;
      this.dataSlot = dataSlot;
   }

   public enum SLOT_TYPE {
      OID,
      TYPE,
      NVALUE,
      DVALUE,
      SVALUE,
      INDEX
   }
         
   public boolean isOidColumn()
   {
      return this.slotType == SLOT_TYPE.OID;
   }
   
   public boolean isTypeColumn()
   {
      return this.slotType == SLOT_TYPE.TYPE;
   }
   
   public boolean isNValueColumn()
   {
      return this.slotType == SLOT_TYPE.NVALUE;
   }
   
   public boolean isDValueColumn()
   {
      return this.slotType == SLOT_TYPE.DVALUE;
   }
   
   public boolean isSValueColumn()
   {
      return this.slotType == SLOT_TYPE.SVALUE;
   }
   
   public boolean isIndexColumn()
   {
      return this.slotType == SLOT_TYPE.INDEX;
   }

   public DataSlotFieldInfo.SLOT_TYPE getSlotType()
   {
      return slotType;
   }

   public DataSlot getDataSlot()
   {
      return dataSlot;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
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
      DataSlotFieldInfo other = (DataSlotFieldInfo) obj;
      if (slotType != other.slotType)
         return false;
      return true;
   }
}