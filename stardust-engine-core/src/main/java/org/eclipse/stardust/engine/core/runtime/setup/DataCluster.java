/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
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
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.jdbc.TableDescriptor;


/**
 *
 * @author rsauer
 * @version $Revision$
 */
public class DataCluster extends TableDescriptor
{
   private final String tableName;
   private final String processInstanceColumn;

   private final List<DataSlot> dataSlots;
   private final Map<String, Map<String, DataSlot>> dataSlotsByDataAndAttribute;

   private final List<DescriptorSlot> descriptorSlots;
   private final Map<String, DescriptorSlot> descriptorSlotsByDescriptorId;

   private final Map<String, DataClusterIndex> indexes;
   private final Set<DataClusterEnableState> enableStates;

   public enum DataClusterEnableState {
      ALL(ProcessInstanceState.Created, ProcessInstanceState.Active,
          ProcessInstanceState.Aborting, ProcessInstanceState.Aborted,
          ProcessInstanceState.Interrupted, ProcessInstanceState.Completed),  
      ALIVE(ProcessInstanceState.Created, ProcessInstanceState.Active,
            ProcessInstanceState.Aborting, ProcessInstanceState.Interrupted),  
      CREATED(ProcessInstanceState.Created),
      ACTIVE(ProcessInstanceState.Active),
      ABORTING(ProcessInstanceState.Aborting),
      ABORTED(ProcessInstanceState.Aborted),
      INTERRUPTED(ProcessInstanceState.Interrupted),
      COMPLETED(ProcessInstanceState.Completed);

      private final ProcessInstanceState[] piStates;
      DataClusterEnableState(ProcessInstanceState... piStates)
      {
         this.piStates = piStates;
      }

      public ProcessInstanceState[] getPiStates()
      {
         return piStates;
      }
   }

   public DataCluster(String schemaName, String tableName, String processInstanceColumn,
         DataSlot[] dataSlots, DescriptorSlot[] descriptorSlots,
         DataClusterIndex[] indexes, Set<DataClusterEnableState> enableStates)
   {
      super(schemaName);

      this.tableName = tableName;
      this.processInstanceColumn = processInstanceColumn;
      this.enableStates = enableStates;

      // init data slot storage
      this.dataSlots = CollectionUtils.newArrayList(dataSlots.length);
      this.dataSlotsByDataAndAttribute = CollectionUtils.newHashMap(dataSlots.length);

      for (int i = 0; i < dataSlots.length; i++ )
      {
         DataSlot dataSlot = dataSlots[i];
         dataSlot.setParent(this);

         this.dataSlots.add(dataSlot);

         Map<String, DataSlot> slotsByAttribute = this.dataSlotsByDataAndAttribute.get(dataSlot.getQualifiedDataId());
         if (slotsByAttribute == null)
         {
            slotsByAttribute = CollectionUtils.newHashMap();
            this.dataSlotsByDataAndAttribute.put(dataSlot.getQualifiedDataId(), slotsByAttribute);
         }
         slotsByAttribute.put(dataSlot.getAttributeName(), dataSlot);
      }

      // init descriptor slot storage
      this.descriptorSlots = Arrays.asList(descriptorSlots);
      this.descriptorSlotsByDescriptorId = CollectionUtils.newHashMap(descriptorSlots.length);
      for (DescriptorSlot descriptorSlot : descriptorSlots)
      {
         descriptorSlot.setParent(this);
         descriptorSlotsByDescriptorId.put(descriptorSlot.getDescriptorId(), descriptorSlot);
      }

      // init index storage
      this.indexes = CollectionUtils.newHashMap(indexes.length);
      for (int i = 0; i < indexes.length; i++ )
      {
         DataClusterIndex index = indexes[i];
         this.indexes.put(index.getIndexName(), index);
      }
   }

   public String getTableName()
   {
      return tableName;
   }

   public String getQualifiedTableName()
   {
      StringBuffer fullTableName = new StringBuffer();
      if(StringUtils.isNotEmpty(getSchemaName()))
      {
         fullTableName.append(getSchemaName());
         fullTableName.append(".");
      }
      fullTableName.append(tableName);

      return fullTableName.toString();
   }

   public String getTableAlias()
   {
      return null;
   }

   public String getProcessInstanceColumn()
   {
      return processInstanceColumn;
   }

   public List<DataSlot> getAllSlots()
   {
      return Collections.unmodifiableList(dataSlots);
   }

   public Map<String, DataSlot> getSlots(String fqDataId)
   {
      Map<String, DataSlot> slotsByAttribute = this.dataSlotsByDataAndAttribute.get(fqDataId);
      if (slotsByAttribute == null)
      {
         return Collections.emptyMap();
      }
      return Collections.unmodifiableMap(slotsByAttribute);
   }

   public DataSlot getSlot(String fqDataId, String attributeName)
   {
      Map<String, DataSlot> slotsByAttribute = this.dataSlotsByDataAndAttribute.get(fqDataId);
      if (slotsByAttribute == null)
      {
         return null;
      }
      if (attributeName == null)
      {
         attributeName = "";
      }
      return slotsByAttribute.get(attributeName);
   }

   public List<DescriptorSlot> getDescriptorSlots()
   {
      return Collections.unmodifiableList(descriptorSlots);
   }

   public DescriptorSlot getDescriptorSlot(String descriptorId)
   {
      return descriptorSlotsByDescriptorId.get(descriptorId);
   }

   public Map<String, DataClusterIndex> getIndexes()
   {
      return Collections.unmodifiableMap(indexes);
   }

   public boolean isEnabledFor(ProcessInstanceState piState)
   {
      for(DataClusterEnableState enableState: enableStates)
      {
         for(ProcessInstanceState enabledPiState: enableState.getPiStates())
         {
            if(enabledPiState == piState)
            {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isEnabledFor(Set<ProcessInstanceState> piStates)
   {
      if(!piStates.isEmpty())
      {
         boolean enabled = true;
         for(ProcessInstanceState piState: piStates)
         {
            enabled &= isEnabledFor(piState);
         }

         return enabled;
      }

      return true;
   }

   public Set<DataClusterEnableState> getEnableStates()
   {
      return enableStates;
   }
}
