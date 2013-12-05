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
}