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
package org.eclipse.stardust.engine.core.runtime.setup;

import javax.xml.namespace.QName;

/**
 * 
 * @author rsauer
 * @version $Revision$
 */
public class DataSlot
{
   private DataCluster parent;
   private final QName fqDataId;
   private final String attributeName; 
   
   private final String oidColumn;
   private final String typeColumn;
   private final String nValueColumn;
   private final String sValueColumn;
   private final String dValueColumn;
   private final boolean ignorePreparedStatements;

   public DataSlot(String modelId, String dataId, String attributeName, String oidColumn,
         String typeColumn, String nValueColumn, String sValueColumn,
         String dValueColumn, boolean ignorePreparedStatements)
   {
      this.fqDataId = new QName(modelId, dataId);
      this.attributeName = attributeName;
      this.oidColumn = oidColumn;
      this.typeColumn = typeColumn;
      this.nValueColumn = nValueColumn;
      this.sValueColumn = sValueColumn;
      this.dValueColumn = dValueColumn;
      this.ignorePreparedStatements = ignorePreparedStatements;
   }
   
   public String getModelId()
   {
      return fqDataId.getNamespaceURI();
   }

   public String getDataId()
   {
      return fqDataId.getLocalPart();
   }
   
   public String getQualifiedDataId()
   {
      return fqDataId.toString();
   }

   public String getAttributeName()
   {
      return this.attributeName;
   }

   public String getOidColumn()
   {
      return oidColumn;
   }

   public String getTypeColumn()
   {
      return typeColumn;
   }

   public String getNValueColumn()
   {
      return nValueColumn;
   }
   
   public String getSValueColumn()
   {
      return sValueColumn;
   }
   
   public String getDValueColumn()
   {
      return dValueColumn;
   }

   public boolean isIgnorePreparedStatements()
   {
      return ignorePreparedStatements;
   }

   public DataCluster getParent()
   {
      return parent;
   }

   public void setParent(DataCluster parent)
   {
      this.parent = parent;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
      result = prime * result + ((dValueColumn == null) ? 0 : dValueColumn.hashCode());
      result = prime * result + ((fqDataId == null) ? 0 : fqDataId.hashCode());
      result = prime * result + ((nValueColumn == null) ? 0 : nValueColumn.hashCode());
      result = prime * result + ((oidColumn == null) ? 0 : oidColumn.hashCode());
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
      DataSlot other = (DataSlot) obj;
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
      if (fqDataId == null)
      {
         if (other.fqDataId != null)
            return false;
      }
      else if (!fqDataId.equals(other.fqDataId))
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
}
