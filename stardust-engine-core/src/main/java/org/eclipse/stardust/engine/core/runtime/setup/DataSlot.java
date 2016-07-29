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

/**
 *
 * @author rsauer
 */
public class DataSlot extends AbstractDataClusterSlot
{
   private final ClusterSlotData clusterSlotData;

   public DataSlot(ClusterSlotData clusterSlotData, String oidColumn,
         String typeColumn, String nValueColumn, String sValueColumn, String dValueColumn,
         boolean ignorePreparedStatements)
   {
      super(oidColumn, typeColumn, nValueColumn, sValueColumn, dValueColumn,
            ignorePreparedStatements);
      this.clusterSlotData = clusterSlotData;
      clusterSlotData.setParent(this);
   }

   public ClusterSlotData getClusterSlotData()
   {
      return clusterSlotData;
   }

   @Deprecated
   public String getModelId()
   {
      return clusterSlotData.getModelId();
   }

   @Deprecated
   public String getDataId()
   {
      return clusterSlotData.getFqDataId().getLocalPart();
   }

   @Deprecated
   public String getQualifiedDataId()
   {
      return clusterSlotData.getFqDataId().toString();
   }

   @Deprecated
   public String getAttributeName()
   {
      return clusterSlotData.getAttributeName();
   }

   @Override
   public boolean hasPrimitiveData()
   {
      return clusterSlotData.isPrimitiveData();
   }

   @Override
   public boolean hasStructuredData()
   {
      return !clusterSlotData.isPrimitiveData();
   }

   @Override
   public boolean isSingleDataSlot()
   {
      return true;
   }

   @Override
   public String qualifiedDataToString()
   {
      return clusterSlotData.qualifiedDataToString();
   }
}
