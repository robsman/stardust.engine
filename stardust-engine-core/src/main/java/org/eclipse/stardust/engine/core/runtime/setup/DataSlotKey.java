/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;


public class DataSlotKey
{
   private DataClusterKey parentKey;
   private String dValueColumn;
   private String sValueColumn;
   private String nValueColumn;
   private String typeColumn;
   private String oidColumn;
   private String attributeName;
   private String qualifiedDataId;
   private final DataSlot dataSlot;

   public DataSlotKey(DataSlot dataSlot)
   {
      this.dataSlot = dataSlot;
      if(dataSlot != null)
      {
         qualifiedDataId = dataSlot.getQualifiedDataId();
         attributeName = dataSlot.getAttributeName();
         oidColumn = dataSlot.getOidColumn();
         typeColumn = dataSlot.getTypeColumn();
         nValueColumn = dataSlot.getNValueColumn();
         sValueColumn = dataSlot.getSValueColumn();
         dValueColumn = dataSlot.getDValueColumn();
         parentKey = new DataClusterKey(dataSlot.getParent());         
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
      result = prime * result + ((dValueColumn == null) ? 0 : dValueColumn.hashCode());
      result = prime * result + ((nValueColumn == null) ? 0 : nValueColumn.hashCode());
      result = prime * result + ((oidColumn == null) ? 0 : oidColumn.hashCode());
      result = prime * result + ((parentKey == null) ? 0 : parentKey.hashCode());
      result = prime * result
            + ((qualifiedDataId == null) ? 0 : qualifiedDataId.hashCode());
      result = prime * result + ((sValueColumn == null) ? 0 : sValueColumn.hashCode());
      result = prime * result + ((typeColumn == null) ? 0 : typeColumn.hashCode());
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
      DataSlotKey other = (DataSlotKey) obj;
      if (attributeName == null)
      {
         if (other.attributeName != null)
            return false;
      }
      else if (!attributeName.equals(other.attributeName))
         return false;
      if (dValueColumn == null)
      {
         if (other.dValueColumn != null)
            return false;
      }
      else if (!dValueColumn.equals(other.dValueColumn))
         return false;
      if (nValueColumn == null)
      {
         if (other.nValueColumn != null)
            return false;
      }
      else if (!nValueColumn.equals(other.nValueColumn))
         return false;
      if (oidColumn == null)
      {
         if (other.oidColumn != null)
            return false;
      }
      else if (!oidColumn.equals(other.oidColumn))
         return false;
      if (parentKey == null)
      {
         if (other.parentKey != null)
            return false;
      }
      else if (!parentKey.equals(other.parentKey))
         return false;
      if (qualifiedDataId == null)
      {
         if (other.qualifiedDataId != null)
            return false;
      }
      else if (!qualifiedDataId.equals(other.qualifiedDataId))
         return false;
      if (sValueColumn == null)
      {
         if (other.sValueColumn != null)
            return false;
      }
      else if (!sValueColumn.equals(other.sValueColumn))
         return false;
      if (typeColumn == null)
      {
         if (other.typeColumn != null)
            return false;
      }
      else if (!typeColumn.equals(other.typeColumn))
         return false;
      return true;
   }

   public DataSlot getDataSlot()
   {
      return dataSlot;
   }
}
