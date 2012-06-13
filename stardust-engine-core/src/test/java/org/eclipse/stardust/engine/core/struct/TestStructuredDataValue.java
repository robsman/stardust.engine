/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.struct;

import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;

class TestStructuredDataValue implements IStructuredDataValue
{

   private long oid;

   private long parentOid;

   private String key;

   private long xPathOid;

   private String value;

   private long rootOid;
   
   private int typeKey;

   TestStructuredDataValue(long oid, long rootOid, long parentOid, long xPathOid, String value,
         String key, int typeKey)
   {
      this.oid = oid;
      this.rootOid = rootOid;
      this.parentOid = parentOid;
      this.key = key;
      this.xPathOid = xPathOid;
      this.value = value;
      this.typeKey = typeKey;
   }

   public long getOID()
   {
      return oid;
   }

   public long getParentOID()
   {
      return parentOid;
   }

   public String getEntryKey()
   {
      return key;
   }

   public long getXPathOID()
   {
      return xPathOid;
   }

   public Object getValue()
   {
      return value;
   }

   public String toString()
   {
      return "oid=<" + this.oid + "> parentOid=<" + this.parentOid + "> xPathOid=<"
            + this.xPathOid + "> value=<" + this.value + "> key=<" + this.key + ">";
   }

   public boolean isRootEntry()
   {
      if (this.parentOid == IStructuredDataValue.NO_PARENT)
      {
         return true;
      }
      return false;
   }

   public boolean isAttribute()
   {
      if (this.key == null)
      {
         return true;
      }
      return false;
   }

   public boolean isElement()
   {
      if (this.isAttribute() == true)
      {
         return false;
      }
      return true;
   }

   public void refresh()
   {
      throw new RuntimeException("NIY");
   }

   public long getProcessInstanceOID()
   {
      return this.rootOid;
   }

   public int getType()
   {
      return this.typeKey;
   }

   public String formatValue()
   {
      return this.value;
   }

}
