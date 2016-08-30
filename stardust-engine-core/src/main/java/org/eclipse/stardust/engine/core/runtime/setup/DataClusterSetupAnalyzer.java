/*******************************************************************************
 * Copyright (c) 2013, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.setup;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.setup.ClusterSlotFieldInfo.SLOT_TYPE;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.CompareBehaviour.EntityNotFoundAction;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
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

         if (oldCluster != null)
         {
            ProcessInstanceFieldInfo oldPiColumn = DataClusterMetaInfoRetriever
                  .getProcessInstanceColumn(oldCluster);
            ProcessInstanceFieldInfo newPiColumn = DataClusterMetaInfoRetriever
                  .getProcessInstanceColumn(newCluster);

            FieldInfoKey oldPiColumnKey = new FieldInfoKey(oldPiColumn);
            FieldInfoKey newPiColumnKey = new FieldInfoKey(newPiColumn);
            if (!CompareHelper.areEqual(oldPiColumnKey, newPiColumnKey))
            {
               // record if the pi column changes for saving pi column value(s)
               observer.columnRenamed(newClusterTableName, oldPiColumn, newPiColumn);
            }
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

      // DataSlots
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

      // DescriptorSlots
      List<DescriptorSlot> descriptorSlotsA = getDescriptorSlots(clusterA);
      List<DescriptorSlot> descriptorSlotsB = getDescriptorSlots(clusterB);
      for(DescriptorSlot descriptorSlotA: descriptorSlotsA)
      {
         String descriptorIdA = descriptorSlotA.getDescriptorId();

         DescriptorSlot descriptorSlotB = findDescriptorSlot(descriptorIdA, descriptorSlotsB);
         computeChanges(descriptorSlotA, descriptorSlotB);
      }

      // Indexes
      Map<String, DataClusterIndex> indexesA = getIndexes(clusterA);
      Map<String, DataClusterIndex> indexesB = getIndexes(clusterB);
      for(String indexNameA: indexesA.keySet())
      {
         DataClusterIndex indexA = indexesA.get(indexNameA);
         DataClusterIndex indexB = indexesB.get(indexNameA);
         computeChanges(indexA, indexB);
      }
   }

   protected void computeChanges(AbstractDataClusterSlot dataSlotA, AbstractDataClusterSlot dataSlotB)
   {
      ClusterSlotFieldInfo typeColumnA = DataClusterMetaInfoRetriever.getTypeColumn(dataSlotA);
      ClusterSlotFieldInfo typeColumnB = DataClusterMetaInfoRetriever.getTypeColumn(dataSlotB);
      computeChanges(typeColumnA, typeColumnB);

      ClusterSlotFieldInfo nValueColumnA = DataClusterMetaInfoRetriever.getNValueColumn(dataSlotA);
      ClusterSlotFieldInfo nValueColumnB = DataClusterMetaInfoRetriever.getNValueColumn(dataSlotB);
      computeChanges(nValueColumnA, nValueColumnB);

      ClusterSlotFieldInfo dValueColumnA = DataClusterMetaInfoRetriever.getDValueColumn(dataSlotA);
      ClusterSlotFieldInfo dValueColumnB = DataClusterMetaInfoRetriever.getDValueColumn(dataSlotB);
      computeChanges(dValueColumnA, dValueColumnB);

      ClusterSlotFieldInfo sValueColumnA = DataClusterMetaInfoRetriever.getSValueColumn(dataSlotA);
      ClusterSlotFieldInfo sValueColumnB = DataClusterMetaInfoRetriever.getSValueColumn(dataSlotB);
      computeChanges(sValueColumnA, sValueColumnB);

      ClusterSlotFieldInfo oidColumnA = DataClusterMetaInfoRetriever.getOidColumn(dataSlotA);
      ClusterSlotFieldInfo oidColumnB = DataClusterMetaInfoRetriever.getOidColumn(dataSlotB);
      computeChanges(oidColumnA, oidColumnB);
   }

   protected void computeChanges(FieldInfo columnA, FieldInfo columnB)
   {
      if(columnA != null)
      {
         FieldInfoKey columnAKey = new FieldInfoKey(columnA);
         FieldInfoKey columnBKey = new FieldInfoKey(columnB);
         if(!CompareHelper.areEqual(columnAKey, columnBKey))
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
      if (dataCluster != null)
      {
         return dataCluster.getIndexes();
      }

      return new HashMap<String, DataClusterIndex>();
   }

   private List<DataSlot> getDataSlots(DataCluster dataCluster)
   {
      if (dataCluster != null)
      {
         return dataCluster.getAllDataSlots();
      }

      return Collections.emptyList();
   }

   private List<DescriptorSlot> getDescriptorSlots(DataCluster dataCluster)
   {
      if (dataCluster != null)
      {
         return dataCluster.getDescriptorSlots();
      }

      return Collections.emptyList();
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

   private DescriptorSlot findDescriptorSlot(String descriptorId,
         List<DescriptorSlot> slots)
   {
      if (slots != null)
      {
         for (DescriptorSlot ds : slots)
         {
            String tmpDescriptorId = ds.getDescriptorId();

            if (CompareHelper.areEqual(tmpDescriptorId, descriptorId))
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
         else
         {
            this.currentClusterTable = dbStructureChanges.get(cluster.getTableName());
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

      private void addColumnToSynchronize(ClusterSlotFieldInfo clusterSlotFieldInfo, Set<ClusterSlotFieldInfo> slotColumnsToSynchronize)
      {
         DataSlotFieldInfoKey key = new DataSlotFieldInfoKey(clusterSlotFieldInfo);
         if(!slotColumnsToSynchronize.contains(key))
         {
            slotColumnsToSynchronize.add(clusterSlotFieldInfo);
         }
      }

      @Override
      public DataClusterSynchronizationInfo getDataClusterSynchronizationInfo()
      {
         //collect all modified fields
         Set<ClusterSlotFieldInfo> slotColumnsToSynchronize = new HashSet<ClusterSlotFieldInfo>();
         Collection<AlterTableInfo> modifiedClusterTables = getAlterInfos();
         for(AlterTableInfo alterInfo: modifiedClusterTables)
         {
            FieldInfo[] addedFields = alterInfo.getAddedFields();
            for(FieldInfo addedField: addedFields)
            {
               if(addedField instanceof ClusterSlotFieldInfo)
               {
                  addColumnToSynchronize((ClusterSlotFieldInfo)addedField, slotColumnsToSynchronize);
               }
            }
         }

         Collection<CreateTableInfo> createdClusterTables = getCreateInfos();
         for(CreateTableInfo createInfo: createdClusterTables)
         {
            FieldInfo[] createdFields = createInfo.getFields();
            for(FieldInfo createdField: createdFields)
            {
               if(createdField instanceof ClusterSlotFieldInfo)
               {
                  addColumnToSynchronize((ClusterSlotFieldInfo)createdField, slotColumnsToSynchronize);
               }
            }
         }

         Map<DataClusterKey, Set<AbstractDataClusterSlot>> clusterToSlotMapping
            = CollectionUtils.newHashMap();
         Map<DataSlotKey, Map<ClusterSlotFieldInfo.SLOT_TYPE, ClusterSlotFieldInfo>> slotToColumnMapping
            = CollectionUtils.newHashMap();

         for(ClusterSlotFieldInfo slotColumn: slotColumnsToSynchronize)
         {
            AbstractDataClusterSlot ds = slotColumn.getClusterSlot();
            DataCluster dc = ds.getParent();
            DataClusterKey dcKey = new DataClusterKey(dc);
            DataSlotKey dsKey = new DataSlotKey(ds);

            Set<AbstractDataClusterSlot> dataSlots = clusterToSlotMapping.get(dcKey);
            if(dataSlots == null)
            {
               dataSlots = CollectionUtils.<AbstractDataClusterSlot>newHashSet();
               clusterToSlotMapping.put(dcKey, dataSlots);
            }
            dataSlots.add(ds);

            Map<ClusterSlotFieldInfo.SLOT_TYPE, ClusterSlotFieldInfo> dataSlotColumns = slotToColumnMapping.get(dsKey);
            if(dataSlotColumns == null)
            {
               dataSlotColumns = new HashMap<ClusterSlotFieldInfo.SLOT_TYPE, ClusterSlotFieldInfo>();
               slotToColumnMapping.put(dsKey, dataSlotColumns);
            }
            dataSlotColumns.put(slotColumn.getSlotType(), slotColumn);
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
      public static HashMap<SLOT_TYPE, ClusterSlotFieldInfo> getDataSlotFields(AbstractDataClusterSlot dataSlot)
      {
         HashMap<SLOT_TYPE, ClusterSlotFieldInfo> fields = CollectionUtils
               .newHashMap(SLOT_TYPE.values().length);

         ClusterSlotFieldInfo oidColumn = getOidColumn(dataSlot);
         putSafely(fields, oidColumn);

         ClusterSlotFieldInfo typeColumn = getTypeColumn(dataSlot);
         putSafely(fields, typeColumn);

         ClusterSlotFieldInfo nValueColumn = getNValueColumn(dataSlot);
         putSafely(fields, nValueColumn);

         ClusterSlotFieldInfo dValueColumn = getDValueColumn(dataSlot);
         putSafely(fields, dValueColumn);

         ClusterSlotFieldInfo sValueColumn = getSValueColumn(dataSlot);
         putSafely(fields, sValueColumn);

         return fields;
      }

      private static void putSafely(HashMap<SLOT_TYPE, ClusterSlotFieldInfo> fields,
            ClusterSlotFieldInfo column)
      {
         if (column != null)
         {
            fields.put(column.getSlotType(), column);
         }
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

            ClusterSlotFieldInfo[] indexFields = new ClusterSlotFieldInfo[columns.length];
            for(int i=0; i< columns.length; i++)
            {
               ClusterSlotFieldInfo ClusterSlotFieldInfo = new ClusterSlotFieldInfo(columns[i], null, SLOT_TYPE.INDEX, null);
               indexFields[i] = ClusterSlotFieldInfo;
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

      public static ClusterSlotFieldInfo getOidColumn(AbstractDataClusterSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getOidColumn()))
         {
            return new ClusterSlotFieldInfo(dataSlot.getOidColumn(), Long.class, SLOT_TYPE.OID, dataSlot);
         }

         return null;
      }

      public static ClusterSlotFieldInfo getTypeColumn(AbstractDataClusterSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getTypeColumn()))
         {
            return new ClusterSlotFieldInfo(dataSlot.getTypeColumn(), Integer.class, SLOT_TYPE.TYPE, dataSlot);
         }

         return null;
      }

      public static ClusterSlotFieldInfo getNValueColumn(AbstractDataClusterSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getNValueColumn()))
         {
            return new ClusterSlotFieldInfo(dataSlot.getNValueColumn(), Long.class, SLOT_TYPE.NVALUE, dataSlot);
         }

         return null;
      }

      public static ClusterSlotFieldInfo getDValueColumn(AbstractDataClusterSlot dataSlot)
      {
         if(dataSlot != null && StringUtils.isNotEmpty(dataSlot.getDValueColumn()))
         {
            return new ClusterSlotFieldInfo(dataSlot.getDValueColumn(), Double.class, SLOT_TYPE.DVALUE, dataSlot);
         }

         return null;
      }

      public static ClusterSlotFieldInfo getSValueColumn(AbstractDataClusterSlot dataSlot)
      {
         if (dataSlot != null && StringUtils.isNotEmpty(dataSlot.getSValueColumn()))
         {
            // Data slot fields for "SValue" store the same data as DV or SDV.
            // Use the max length (should be equal) to be sure that all values can be stored
            final int length = Math.max(
                  StructuredDataValueBean.string_value_COLUMN_LENGTH,
                  DataValueBean.string_value_COLUMN_LENGTH);

            return new ClusterSlotFieldInfo(dataSlot.getSValueColumn(), String.class,
                  SLOT_TYPE.SVALUE, dataSlot, length);
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

   public static class DataClusterSynchronizationInfo
   {
      private final Map<DataClusterKey, Set<AbstractDataClusterSlot>> clusterToSlotMapping;
      private final Map<String, Map<FieldInfo, FieldInfo>> columnRenames;
      private final Map<DataSlotKey, Map<ClusterSlotFieldInfo.SLOT_TYPE, ClusterSlotFieldInfo>> slotToColumnMapping;

      private long scopePiOid = 0;
      private boolean performClusterVerification = true;

      public DataClusterSynchronizationInfo(
            Map<DataClusterKey, Set<AbstractDataClusterSlot>> clusterToSlotMapping,
            Map<DataSlotKey, Map<ClusterSlotFieldInfo.SLOT_TYPE, ClusterSlotFieldInfo>> slotToColumnMapping,
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
            clusterToSynch.add(key.getDataCluster());
         }

         return clusterToSynch;
      }

      public Collection<AbstractDataClusterSlot> getDataSlots(DataCluster cluster)
      {
         DataClusterKey key = new DataClusterKey(cluster);
         if(clusterToSlotMapping.containsKey(key))
         {
            return clusterToSlotMapping.get(key);
         }
         else
         {
            return CollectionUtils.newHashSet();
         }
      }

      public Map<SLOT_TYPE, ClusterSlotFieldInfo> getDataSlotColumns(AbstractDataClusterSlot dataSlot)
      {
         DataSlotKey key = new DataSlotKey(dataSlot);
         if(slotToColumnMapping.containsKey(key))
         {
            return slotToColumnMapping.get(key);
         }
         else
         {
            return new HashMap<SLOT_TYPE, ClusterSlotFieldInfo>();
         }
      }

      public void setPerformClusterVerification(boolean performClusterVerification)
      {
         this.performClusterVerification = performClusterVerification;
      }

      public boolean getPerformClusterVerification()
      {
         return performClusterVerification;
      }

      public void setScopePiOid(long scopePiOid)
      {
         this.scopePiOid = scopePiOid;
      }

      public long getScopePiOid()
      {
         return scopePiOid;
      }

      public Map<String, Map<FieldInfo, FieldInfo>> getColumnRenames()
      {
         return columnRenames;
      }
   }
}
