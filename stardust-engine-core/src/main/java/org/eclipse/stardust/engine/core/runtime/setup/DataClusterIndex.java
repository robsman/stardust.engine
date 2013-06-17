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
package org.eclipse.stardust.engine.core.runtime.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author sborn
 * @version $Revision$
 */
public class DataClusterIndex
{
   private final String tableName;
   private final String indexName;
   private final boolean isUnique;
   private final List<String> columnNames;
   
   public DataClusterIndex(String tableName, String indexName, boolean isUnique, List<String> columnNames)
   {
      this.tableName = tableName;
      this.indexName = indexName;
      this.isUnique = isUnique;
      this.columnNames = new ArrayList(columnNames);
   }

   public List<String> getColumnNames()
   {
      return Collections.unmodifiableList(columnNames);
   }

   public String getIndexName()
   {
      return indexName;
   }

   public boolean isUnique()
   {
      return isUnique;
   }

   public String getTableName()
   {
      return tableName;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((columnNames == null) ? 0 : columnNames.hashCode());
      result = prime * result + ((indexName == null) ? 0 : indexName.hashCode());
      result = prime * result + (isUnique ? 1231 : 1237);
      result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
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
      DataClusterIndex other = (DataClusterIndex) obj;
      if (columnNames == null)
      {
         if (other.columnNames != null)
            return false;
      }
      else if (!columnNames.equals(other.columnNames))
         return false;
      if (indexName == null)
      {
         if (other.indexName != null)
            return false;
      }
      else if (!indexName.equals(other.indexName))
         return false;
      if (isUnique != other.isUnique)
         return false;
      if (tableName == null)
      {
         if (other.tableName != null)
            return false;
      }
      else if (!tableName.equals(other.tableName))
         return false;
      return true;
   }
   
   
}
