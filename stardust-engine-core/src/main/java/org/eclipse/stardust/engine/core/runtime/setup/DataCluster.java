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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

   private final List<DataSlot> slots;
   private final Map<String, DataClusterIndex> indexes;
   private final Map<String, Map<String, DataSlot>> slotsByDataAndAttribute;
   private final Set<DataClusterEnableState> enableStates;
   
   public enum DataClusterEnableState {
      ALL(ProcessInstanceState.Created, ProcessInstanceState.Active, 
          ProcessInstanceState.Aborting, ProcessInstanceState.Aborted,
          ProcessInstanceState.Interrupted, ProcessInstanceState.Completed,
          ProcessInstanceState.Halted, ProcessInstanceState.Halting),  
      ALIVE(ProcessInstanceState.Created, ProcessInstanceState.Active, 
            ProcessInstanceState.Aborting, ProcessInstanceState.Interrupted,
            ProcessInstanceState.Halted, ProcessInstanceState.Halting),  
      CREATED(ProcessInstanceState.Created),      
      ACTIVE(ProcessInstanceState.Active),   
      ABORTING(ProcessInstanceState.Aborting),
      ABORTED(ProcessInstanceState.Aborted),
      INTERRUPTED(ProcessInstanceState.Interrupted),
      COMPLETED(ProcessInstanceState.Completed),
      HALTING(ProcessInstanceState.Halting),
      HALTED(ProcessInstanceState.Halted);

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
   
   public DataCluster(String schemaName, String tableName, String processInstanceColumn, DataSlot[] slots,
         DataClusterIndex[] indexes, Set<DataClusterEnableState> enableStates)
   {
      super(schemaName);
      
      this.tableName = tableName;
      this.processInstanceColumn = processInstanceColumn;
      this.enableStates = enableStates;
      this.slots = new LinkedList();
      this.slotsByDataAndAttribute = CollectionUtils.newHashMap();
      for (int i = 0; i < slots.length; i++ )
      {
         DataSlot slot = slots[i];
         slot.setParent(this);
         
         this.slots.add(slot);
         
         Map<String, DataSlot> slotsByAttribute = this.slotsByDataAndAttribute.get(slot.getQualifiedDataId());
         if (slotsByAttribute == null)
         {
            slotsByAttribute = CollectionUtils.newHashMap();
            this.slotsByDataAndAttribute.put(slot.getQualifiedDataId(), slotsByAttribute);
         }
         slotsByAttribute.put(slot.getAttributeName(), slot);
      }

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
      return Collections.unmodifiableList(slots);
   }
   
   public Map<String, DataSlot> getSlots(String fqDataId)
   {
      Map<String, DataSlot> slotsByAttribute = this.slotsByDataAndAttribute.get(fqDataId);
      if (slotsByAttribute == null)
      {
         return Collections.emptyMap();
      }
      return Collections.unmodifiableMap(slotsByAttribute);
   }
   
   public DataSlot getSlot(String fqDataId, String attributeName)
   {
      Map<String, DataSlot> slotsByAttribute = this.slotsByDataAndAttribute.get(fqDataId);
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
