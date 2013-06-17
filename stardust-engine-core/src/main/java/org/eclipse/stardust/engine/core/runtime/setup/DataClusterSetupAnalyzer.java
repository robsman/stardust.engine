/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.CompareBehaviour.EntityNotFoundAction;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataSlotFieldInfo.SLOT_TYPE;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.IndexInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.CreateTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DropTableInfo;

public class DataClusterSetupAnalyzer
{
   private CompareBehaviour compareBehaviour;
   
   private IClusterChangeObserver observer;
   
   public IClusterChangeObserver analyzeChanges(DataCluster[] oldSetup, DataCluster[] newSetup)
   {
      if(oldSetup == null || oldSetup.length == 0)
      {
         compareBehaviour = new CompareBehaviour(EntityNotFoundAction.ADD_ACTION);
         observer = new ClusterChangeObserverImpl();
         computeChanges(newSetup, null);
      }
      else
      {
         observer = new ClusterChangeObserverImpl();
         
         //compute columns / indexes which were present IN the old cluster definition but NOT in the 
         //new one anymore - the will be be removed
         compareBehaviour = new CompareBehaviour(EntityNotFoundAction.REMOVE_ACTION);
         computeChanges(oldSetup, newSetup);
         
         //compute columns / indexes which were present IN the new cluster definition but NOT in the 
         //old one - they will be added
         compareBehaviour = new CompareBehaviour(EntityNotFoundAction.ADD_ACTION);
         computeChanges(newSetup, oldSetup);
         
         //compute renames on pi columns 
         computePiColumnRenameChanges(oldSetup, newSetup);
      }
      
      
      
      return observer;
   }
   
   protected void computeChanges(DataCluster[] clusterSetupA, DataCluster[] clusterSetupB)
   {
      for(DataCluster clusterA: clusterSetupA)
      {
         String clusterTableNameA = clusterA.getTableName();
         DataCluster clusterB = findCluster(clusterTableNameA, clusterSetupB);
         computeChanges(clusterA, clusterB);
      }
   }
   
   protected void computePiColumnRenameChanges(DataCluster[] oldSetup, DataCluster[] newSetup)
   {
      for(DataCluster newCluster: newSetup)
      {
         String newClusterTableName = newCluster.getTableName();
         DataCluster oldCluster = findCluster(newClusterTableName, oldSetup);
         
         ProcessInstanceFieldInfo oldPiColumn = DataClusterMetaInfoRetriever.getProcessInstanceColumn(oldCluster);
         ProcessInstanceFieldInfo newPiColumn = DataClusterMetaInfoRetriever.getProcessInstanceColumn(newCluster);
         
         if (oldPiColumn != null && newPiColumn != null
               && !newPiColumn.equals(oldPiColumn))
         {
            observer.columnRenamed(newClusterTableName, oldPiColumn, newPiColumn);
         }
      }
   }
   
   protected void computeChanges(DataCluster clusterA, DataCluster clusterB)
   {
      if(clusterB == null)
      {
         if(compareBehaviour.getEntityNotFoundAction() == EntityNotFoundAction.ADD_ACTION)
         {
            observer.clusterAdded(clusterA);
         }
         else if(compareBehaviour.getEntityNotFoundAction() 
               == EntityNotFoundAction.REMOVE_ACTION)
         {
            observer.clusterRemoved(clusterA);
         }
      }
      else
      {
         observer.clusterModified(clusterA);
      }
      
      ProcessInstanceFieldInfo piColumnA = DataClusterMetaInfoRetriever.getProcessInstanceColumn(clusterA);
      ProcessInstanceFieldInfo piColumnB = DataClusterMetaInfoRetriever.getProcessInstanceColumn(clusterB);
      computeChanges(piColumnA, piColumnB);
                  
      List<DataSlot> dataSlotsA = getDataSlots(clusterA); 
      List<DataSlot> dataSlotsB = getDataSlots(clusterB);
      for(DataSlot dataSlotA: dataSlotsA)
      {
         String dataIdA = dataSlotA.getDataId();
         String modelIdA = dataSlotA.getModelId(); 
         String attributeNameA = dataSlotA.getAttributeName();
       
         DataSlot dataSlotB = findDataSlot(modelIdA, dataIdA, attributeNameA, dataSlotsB);
         computeChanges(dataSlotA, dataSlotB);
      }
      
      Map<String, DataClusterIndex> indexesA = getIndexes(clusterA);
      Map<String, DataClusterIndex> indexesB = getIndexes(clusterB);
      for(String indexNameA: indexesA.keySet())
      {
         DataClusterIndex indexA = indexesA.get(indexNameA);
         DataClusterIndex indexB = indexesB.get(indexNameA);
         computeChanges(indexA, indexB);
      }
   }
   
   protected void computeChanges(DataSlot dataSlotA, DataSlot dataSlotB)
   {      
      DataSlotFieldInfo typeColumnA = DataClusterMetaInfoRetriever.getTypeColumn(dataSlotA);
      DataSlotFieldInfo typeColumnB = DataClusterMetaInfoRetriever.getTypeColumn(dataSlotB);    
      computeChanges(typeColumnA, typeColumnB);
      
      DataSlotFieldInfo nValueColumnA = DataClusterMetaInfoRetriever.getNValueColumn(dataSlotA);
      DataSlotFieldInfo nValueColumnB = DataClusterMetaInfoRetriever.getNValueColumn(dataSlotB);
      computeChanges(nValueColumnA, nValueColumnB);

      DataSlotFieldInfo dValueColumnA = DataClusterMetaInfoRetriever.getDValueColumn(dataSlotA);
      DataSlotFieldInfo dValueColumnB = DataClusterMetaInfoRetriever.getDValueColumn(dataSlotB);
      computeChanges(dValueColumnA, dValueColumnB); 
      
      DataSlotFieldInfo sValueColumnA = DataClusterMetaInfoRetriever.getSValueColumn(dataSlotA);
      DataSlotFieldInfo sValueColumnB = DataClusterMetaInfoRetriever.getSValueColumn(dataSlotB);
      computeChanges(sValueColumnA, sValueColumnB);
      
      DataSlotFieldInfo oidColumnA = DataClusterMetaInfoRetriever.getOidColumn(dataSlotA);
      DataSlotFieldInfo oidColumnB = DataClusterMetaInfoRetriever.getOidColumn(dataSlotB);
      computeChanges(oidColumnA, oidColumnB); 
   }
      
   protected void computeChanges(FieldInfo columnA, FieldInfo columnB)
   {
      if(columnA != null)
      {
         if(!CompareHelper.areEqual(columnA, columnB))
         {            
            if(compareBehaviour.getEntityNotFoundAction() == EntityNotFoundAction.ADD_ACTION)
            {
               observer.columnAdded(columnA);
            }
            else if(compareBehaviour.getEntityNotFoundAction() 
                  == EntityNotFoundAction.REMOVE_ACTION)
            {
               observer.columnRemoved(columnA);
            }
         }
      }
   }
   
   protected void computeChanges(DataClusterIndex indexA, DataClusterIndex indexB)
   {
      if(!CompareHelper.areEqual(indexA, indexB))
      {
         String indexName = indexA.getIndexName();
         String[] columns = (String[]) indexA.getColumnNames().toArray(new String[0]);
         boolean unique = indexA.isUnique();
         
         FieldInfo[] indexFields = new FieldInfo[columns.length];
         for(int i=0; i< columns.length; i++) 
         {
            FieldInfo slotFieldInfo = new FieldInfo(columns[i], null);
            indexFields[i] = slotFieldInfo;
         }
         
         IndexInfo indexInfo = new IndexInfo(indexName, unique, indexFields);
         if(compareBehaviour.getEntityNotFoundAction() == EntityNotFoundAction.ADD_ACTION)
         {
            observer.indexAdded(indexInfo);
         }
         else if(compareBehaviour.getEntityNotFoundAction() 
               == EntityNotFoundAction.REMOVE_ACTION)
         {
            observer.indexRemoved(indexInfo);
         }
      }
   }
    
   private Map<String, DataClusterIndex> getIndexes(DataCluster dataCluster)
   {
      if(dataCluster != null)
      {
         return dataCluster.getIndexes();
      }
      
      return new HashMap<String, DataClusterIndex>();
   }
   
   private List<DataSlot> getDataSlots(DataCluster dataCluster)
   {
      if(dataCluster != null)
      {
         return dataCluster.getAllSlots();
      }
      
      return new ArrayList<DataSlot>();
   }
   
   private DataCluster findCluster(String tableName, DataCluster[] setup)
   {
      if(setup != null)
      {
         for(DataCluster dc: setup)
         {
            if(CompareHelper.areEqual(dc.getTableName(), tableName))
            {
               return dc;
            }
         }
      }
            
      return null;
   }
   
   private DataSlot findDataSlot(String modelId, String dataId, String attributeName, List<DataSlot> slots)
   {
      if(slots != null)
      {
         for(DataSlot ds: slots)
         {
            String tmpDataId = ds.getDataId(); 
            String tmpModelId = ds.getModelId();
            String tmpAttributeName = ds.getAttributeName();
            
            if(CompareHelper.areEqual(tmpModelId, modelId)
                  && CompareHelper.areEqual(tmpDataId, dataId)
                     && CompareHelper.areEqual(tmpAttributeName, attributeName))
            {
               return ds; 
            }
         }
      }
      
      return null;
   }
      
   public interface IClusterChangeObserver
   {
      void columnAdded(FieldInfo column);
      void columnRemoved(FieldInfo column);
      void columnRenamed(String clusterTableName, FieldInfo oldColumn, FieldInfo newColumn);
      
      void indexAdded(IndexInfo index);
      void indexRemoved(IndexInfo index);
      
      void clusterAdded(DataCluster cluster);
      void clusterRemoved(DataCluster cluster);
      void clusterModified(DataCluster cluster);
      
      Collection<DropTableInfo> getDropInfos();
      Collection<CreateTableInfo> getCreateInfos();
      Collection<AlterTableInfo> getAlterInfos();
      
      DataClusterSynchronizationInfo getDataClusterSynchronizationInfo();
      
   }
   
   class ClusterChangeObserverImpl implements IClusterChangeObserver
   {
      private Map<String, AbstractTableInfo> dbStructureChanges 
         = new HashMap<String, AbstractTableInfo>();
            
      private Map<String, Map<FieldInfo, FieldInfo>> columnRenames
         = new HashMap<String, Map<FieldInfo,FieldInfo>>();
      
      private AbstractTableInfo currentClusterTable;
      
      public ClusterChangeObserverImpl()
      {
         
      }
      
      @Override
      public void columnAdded(FieldInfo column)
      {
         currentClusterTable.addField(column);  
      }

      @Override
      public void columnRemoved(FieldInfo column)
      {
         currentClusterTable.removeField(column);   
      }
      
      @Override
      public void columnRenamed(String clusterTableName, FieldInfo oldColumn, FieldInfo newColumn)
      {
         Map<FieldInfo, FieldInfo> renameFields = columnRenames.get(clusterTableName);
         if(renameFields == null)
         {
            renameFields = new HashMap<FieldInfo, FieldInfo>();
            columnRenames.put(clusterTableName, renameFields);
         }

         renameFields.put(oldColumn, newColumn);     
      }

      @Override
      public void indexAdded(IndexInfo index)
      {
         currentClusterTable.addIndex(index);
      }

      @Override
      public void indexRemoved(IndexInfo index)
      {
         currentClusterTable.removeIndex(index);   
      }

      @Override
      public void clusterAdded(DataCluster cluster)
      {
         this.currentClusterTable = new CreateTableInfo(cluster.getTableName())
         {
            @Override
            public String getSequenceName()
            {
               return null;
            }
         };
         
         dbStructureChanges.put(cluster.getTableName(), currentClusterTable);
      }

      @Override
      public void clusterRemoved(DataCluster cluster)
      {
         this.currentClusterTable = new DropTableInfo(cluster.getTableName(), null)
         {
            @Override
            public String getSequenceName()
            {
               return null;
            }
         };
         
         dbStructureChanges.put(cluster.getTableName(), currentClusterTable);
      }

      @Override
      public void clusterModified(DataCluster cluster)
      {
         if(!dbStructureChanges.containsKey(cluster.getTableName()))
         {
            this.currentClusterTable = new AlterTableInfo(cluster.getTableName())
            {
            };
            
            dbStructureChanges.put(cluster.getTableName(), currentClusterTable);
         }
      }
      
      public Collection<DropTableInfo> getDropInfos()
      {
         List<DropTableInfo> dropInfos = new ArrayList<DropTableInfo>();
         for (AbstractTableInfo info : dbStructureChanges.values())
         {
            if (info instanceof DropTableInfo)
            {
               dropInfos.add((DropTableInfo) info);
            }
         }

         return dropInfos;
      }

      public Collection<CreateTableInfo> getCreateInfos()
      {
         List<CreateTableInfo> createInfos = new ArrayList<CreateTableInfo>();
         for (AbstractTableInfo info : dbStructureChanges.values())
         {
            if (info instanceof CreateTableInfo)
            {
               createInfos.add((CreateTableInfo) info);
            }
         }

         return createInfos;
      }

      public Collection<AlterTableInfo> getAlterInfos()
      {
         List<AlterTableInfo> alterInfos = new ArrayList<AlterTableInfo>();
         for (AbstractTableInfo info : dbStructureChanges.values())
         {
            if (info instanceof AlterTableInfo)
            {
               alterInfos.add((AlterTableInfo) info);
            }
         }

         return alterInfos;
      }
      
      @Override
      public DataClusterSynchronizationInfo getDataClusterSynchronizationInfo()
      {
         
         Set<DataSlotFieldInfo> slotColumnsToSynchronize = new HashSet<DataSlotFieldInfo>();
         Collection<AlterTableInfo> modifiedClusterTables = getAlterInfos();
         for(AlterTableInfo alterInfo: modifiedClusterTables)
         {
            FieldInfo[] addedFields = alterInfo.getAddedFields();
            for(FieldInfo addedField: addedFields)
            {
               if(addedField instanceof DataSlotFieldInfo)
               {
                  slotColumnsToSynchronize.add((DataSlotFieldInfo)addedField);
               }
            }
         }
         
         Collection<CreateTableInfo> createdClusterTables = getCreateInfos();
         for(CreateTableInfo createInfo: createdClusterTables)
         {
            FieldInfo[] createdFields = createInfo.getFields();
            for(FieldInfo createdField: createdFields)
            {
               if(createdField instanceof DataSlotFieldInfo)
               {
                  slotColumnsToSynchronize.add((DataSlotFieldInfo)createdField);
               }
            }
         }
         
         Map<DataClusterKey, Set<DataSlot>> clusterToSlotMapping 
            = new HashMap<DataClusterKey, Set<DataSlot>>();
         
         Map<DataSlot, Set<DataSlotFieldInfo>> slotToColumnMapping
            = new HashMap<DataSlot, Set<DataSlotFieldInfo>>();
         for(DataSlotFieldInfo slotColumn: slotColumnsToSynchronize)
         {
            DataSlot ds = slotColumn.getDataSlot();
            DataCluster dc = ds.getParent();
            DataClusterKey dcKey = new DataClusterKey(dc);
            
            Set<DataSlot> dataSlots = clusterToSlotMapping.get(dcKey);
            if(dataSlots == null)
            {
               dataSlots = new HashSet<DataSlot>();
               clusterToSlotMapping.put(dcKey, dataSlots);
            }
            dataSlots.add(ds);
            
            Set<DataSlotFieldInfo> dataSlotColumns = slotToColumnMapping.get(ds);
            if(dataSlotColumns == null)
            {
               dataSlotColumns = new HashSet<DataSlotFieldInfo>();
               slotToColumnMapping.put(ds, dataSlotColumns);
            }
            dataSlotColumns.add(slotColumn);
         }
         
         
         
         
         return new DataClusterSynchronizationInfo(clusterToSlotMapping, slotToColumnMapping, columnRenames);
      }
   }
   
   static class CompareBehaviour
   {
      public static enum EntityNotFoundAction {
         ADD_ACTION,
         REMOVE_ACTION
      }

      private final EntityNotFoundAction entityNotFoundAction;
      public CompareBehaviour(EntityNotFoundAction entityNotFoundAction)
      {
         this.entityNotFoundAction = entityNotFoundAction;
      }

      public EntityNotFoundAction getEntityNotFoundAction()
      {
         return entityNotFoundAction;
      }
   }
   
   public static class DataClusterMetaInfoRetriever
   {      
      public static List<DataSlotFieldInfo> getDataSlotFields(DataSlot dataSlot)
      {
         List<DataSlotFieldInfo> fields = new ArrayList<DataSlotFieldInfo>();

         DataSlotFieldInfo oidColumn = getOidColumn(dataSlot);
         if (oidColumn != null)
         {
            fields.add(oidColumn);
         }

         DataSlotFieldInfo typeColumn = getTypeColumn(dataSlot);
         if (typeColumn != null)
         {
            fields.add(typeColumn);
         }

         DataSlotFieldInfo nValueColumn = getNValueColumn(dataSlot);
         if (nValueColumn != null)
         {
            fields.add(nValueColumn);
         }

         DataSlotFieldInfo dValueColumn = getDValueColumn(dataSlot);
         if (dValueColumn != null)
         {
            fields.add(dValueColumn);
         }

         DataSlotFieldInfo sValueColumn = getSValueColumn(dataSlot);
         if (sValueColumn != null)
         {
            fields.add(sValueColumn);
         }

         return fields;
      }
      
      public static List<IndexInfo> getIndexes(DataCluster cluster)
      {
         List<IndexInfo> indexInfos = new ArrayList<IndexInfo>();
         Map<String, DataClusterIndex> clusterIndexes = cluster.getIndexes();
         for(String indexName: clusterIndexes.keySet())
         {
            DataClusterIndex index = clusterIndexes.get(indexName);
            String[] columns = (String[]) index.getColumnNames().toArray(new String[0]);
            boolean unique = index.isUnique();
            
            DataSlotFieldInfo[] indexFields = new DataSlotFieldInfo[columns.length];
            for(int i=0; i< columns.length; i++) 
            {
               DataSlotFieldInfo DataSlotFieldInfo = new DataSlotFieldInfo(columns[i], null, SLOT_TYPE.INDEX, null);
               indexFields[i] = DataSlotFieldInfo;
            }
            IndexInfo indexInfo = new IndexInfo(indexName, unique, indexFields);
            indexInfos.add(indexInfo);
         }
         
         
         return indexInfos;
      }
      
      public static ProcessInstanceFieldInfo getProcessInstanceColumn(DataCluster cluster)
      {
         if(cluster != null && StringUtils.isNotEmpty(cluster.getProcessInstanceColumn()))
         {
            return new ProcessInstanceFieldInfo(cluster.getProcessInstanceColumn(), Long.class);
         }

         return null;
      }
      
      public static DataSlotFieldInfo getOidColumn(DataSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getOidColumn()))
         {
            return new DataSlotFieldInfo(dataSlot.getOidColumn(), Long.class, SLOT_TYPE.OID, dataSlot);
         }
         
         return null;
      }
      
      public static DataSlotFieldInfo getTypeColumn(DataSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getTypeColumn()))
         {
            return new DataSlotFieldInfo(dataSlot.getTypeColumn(), Integer.class, SLOT_TYPE.TYPE, dataSlot);
         }
         
         return null;
      }
     
      public static DataSlotFieldInfo getNValueColumn(DataSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getNValueColumn()))
         {
            return new DataSlotFieldInfo(dataSlot.getNValueColumn(), Long.class, SLOT_TYPE.NVALUE, dataSlot);
         }
         
         return null;
      }
      
      public static DataSlotFieldInfo getDValueColumn(DataSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getDValueColumn()))
         {
            return new DataSlotFieldInfo(dataSlot.getDValueColumn(), Double.class, SLOT_TYPE.DVALUE, dataSlot);
         }
         
         return null;
      }

      public static DataSlotFieldInfo getSValueColumn(DataSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getSValueColumn()))
         {
            return new DataSlotFieldInfo(dataSlot.getSValueColumn(), String.class, SLOT_TYPE.SVALUE, dataSlot);
         }
         
         return null;
      }
   }
   
   public static class ProcessInstanceFieldInfo extends FieldInfo
   {

      public ProcessInstanceFieldInfo(String name, Class type)
      {
         super(name, type);
      }
   }
   
   public static class DataSlotFieldInfo extends FieldInfo
   {
      private final SLOT_TYPE slotType;
      private final DataSlot dataSlot;

      public DataSlotFieldInfo(String name, Class type, SLOT_TYPE slotType, DataSlot dataSlot)
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

      public SLOT_TYPE getSlotType()
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
   
   class DataClusterKey
   {
      private final String tableName;
      private final DataCluster cluster;
      public DataClusterKey(DataCluster cluster)
      {
         this.cluster = cluster;
         if(cluster != null)
         {
            tableName = cluster.getTableName();
         }
         else
         {
            tableName = null;
         }
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
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
         if (tableName == null)
         {
            if (other.tableName != null)
               return false;
         }
         else if (!tableName.equals(other.tableName))
            return false;
         return true;
      }
      
      public DataCluster getCluster()
      {
         return cluster;
      }
   }
   
   public class DataClusterSynchronizationInfo
   {
      private final Map<DataClusterKey, Set<DataSlot>> clusterToSlotMapping;
      private final Map<DataSlot, Set<DataSlotFieldInfo>> slotToColumnMapping;
      private final Map<String, Map<FieldInfo, FieldInfo>> columnRenames;

      public DataClusterSynchronizationInfo(
            Map<DataClusterKey, Set<DataSlot>> clusterToSlotMapping,
            Map<DataSlot, Set<DataSlotFieldInfo>> slotToColumnMapping, 
            Map<String, Map<FieldInfo, FieldInfo>> columnRenames)
      {
         this.clusterToSlotMapping = clusterToSlotMapping;
         this.slotToColumnMapping = slotToColumnMapping;
         this.columnRenames = columnRenames;
      }
      
      public Collection<DataCluster> getClusters()
      {
         Set<DataCluster> clusterToSynch = new HashSet<DataCluster>();
         for(DataClusterKey key: clusterToSlotMapping.keySet())
         {
            clusterToSynch.add(key.getCluster());
         }
         
         return clusterToSynch;
      }
      
      public Collection<DataSlot> getDataSlots(DataCluster cluster)
      {
         DataClusterKey key = new DataClusterKey(cluster);         
         if(clusterToSlotMapping.containsKey(key))
         {
            return clusterToSlotMapping.get(key);
         }
         else
         {
            return new HashSet<DataSlot>();
         }
      }
      
      public Collection<DataSlotFieldInfo> getDataSlotColumns(DataSlot dataSlot)
      {
         if(slotToColumnMapping.containsKey(dataSlot))
         {
            return slotToColumnMapping.get(dataSlot);
         }
         else
         {
            return new HashSet<DataSlotFieldInfo>();
         }
      }

      public Map<String, Map<FieldInfo, FieldInfo>> getColumnRenames()
      {
         return columnRenames;
      }
   }
}
