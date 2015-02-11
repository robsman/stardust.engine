/*******************************************************************************
 * Copyright (c) 2013, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.setup;

import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;

public class DataSlotFieldInfo extends FieldInfo
{
   private final DataSlotFieldInfo.SLOT_TYPE slotType;
   private final DataSlot dataSlot;

   public DataSlotFieldInfo(String name, Class type, DataSlotFieldInfo.SLOT_TYPE slotType, DataSlot dataSlot)
   {
      this(name, type, slotType, dataSlot, 0);
   }

   public DataSlotFieldInfo(String name, Class type, DataSlotFieldInfo.SLOT_TYPE slotType, DataSlot dataSlot, int size)
   {
      super(name, type, size);
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