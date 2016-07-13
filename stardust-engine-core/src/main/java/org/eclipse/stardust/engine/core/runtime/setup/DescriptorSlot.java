/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
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
import java.util.Set;

/**
 *
 * @author stephan.born
 */
public class DescriptorSlot extends AbstractDataClusterSlot
{
   private final String descriptorId;
   private final Set<ClusterSlotData> clusterSlotDatas;

   public DescriptorSlot(String descriptorId, Set<ClusterSlotData> clusterSlotDatas,
         String oidColumn, String typeColumn, String nValueColumn, String sValueColumn,
         String dValueColumn, boolean ignorePreparedStatements)
   {
      super(oidColumn, typeColumn, nValueColumn, sValueColumn, dValueColumn,
            ignorePreparedStatements);
      this.descriptorId = descriptorId;

      this.clusterSlotDatas = clusterSlotDatas;
      for (ClusterSlotData clusterSlotData : clusterSlotDatas)
      {
         clusterSlotData.setParent(this);
      }
   }

   public Set<ClusterSlotData> getClusterSlotDatas()
   {
      return Collections.unmodifiableSet(clusterSlotDatas);
   }

   public String getDescriptorId()
   {
      return descriptorId;
   }

   @Override
   public boolean hasPrimitiveData()
   {
      for (ClusterSlotData clusterSlotData : clusterSlotDatas)
      {
         if(clusterSlotData.isPrimitiveData())
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean hasStructuredData()
   {
      for (ClusterSlotData clusterSlotData : clusterSlotDatas)
      {
         if(!clusterSlotData.isPrimitiveData())
         {
            return true;
         }
      }
      return false;
   }
}
