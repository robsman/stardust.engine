/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import java.util.Set;

import org.eclipse.stardust.engine.core.runtime.setup.DataCluster.DataClusterEnableState;


public class DataClusterKey
{
   private String tableName;
   private Set<DataClusterEnableState> enableState;
   private String piColumn;
   private final DataCluster dataCluster;

   public DataClusterKey(DataCluster dataCluster)
   {
      this.dataCluster = dataCluster;
      if(dataCluster != null)
      {
         tableName = dataCluster.getTableName();
         enableState = dataCluster.getEnableStates();
         piColumn = dataCluster.getProcessInstanceColumn();
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((enableState == null) ? 0 : enableState.hashCode());
      result = prime * result + ((piColumn == null) ? 0 : piColumn.hashCode());
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
      DataClusterKey other = (DataClusterKey) obj;
      if (enableState == null)
      {
         if (other.enableState != null)
            return false;
      }
      else if (!enableState.equals(other.enableState))
         return false;
      if (piColumn == null)
      {
         if (other.piColumn != null)
            return false;
      }
      else if (!piColumn.equals(other.piColumn))
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

   public DataCluster getDataCluster()
   {
      return dataCluster;
   }
}
