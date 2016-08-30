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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;

/**
*
* @author stephan.born
*/public class ClusterSlotData
{
   private final QName fqDataId;
   private final String attributeName;
   private AbstractDataClusterSlot parent;

   public ClusterSlotData(String modelId, String dataId, String attributeName)
   {
      this.fqDataId = new QName(modelId, dataId);
      this.attributeName = attributeName;
   }

   public String getModelId()
   {
      return getFqDataId().getNamespaceURI();
   }

   public String getDataId()
   {
      return getFqDataId().getLocalPart();
   }

   public String getQualifiedDataId()
   {
      return getFqDataId().toString();
   }

   /**
    * @return the fqDataId
    */
   public QName getFqDataId()
   {
      return fqDataId;
   }

   /**
    * @return the attributeName
    */
   public String getAttributeName()
   {
      return attributeName;
   }

   public void setParent(AbstractDataClusterSlot parent)
   {
      this.parent = parent;
   }

   public AbstractDataClusterSlot getParent()
   {
      return parent;
   }

   public boolean isPrimitiveData()
   {
      return StringUtils.isEmpty(getAttributeName());
   }

   public Map<Long, IData> findAllPrimitiveDataRtOids()
   {
      Map<Long, IData> dataRtOids = new HashMap();

      if (isPrimitiveData())
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();

         Iterator modelItr = modelManager.getAllModelsForId(getModelId());

         while (modelItr.hasNext())
         {
            IModel model = (IModel) modelItr.next();
            IData data = model.findData(getDataId());
            if (null != data)
            {
               dataRtOids.put(new Long(modelManager.getRuntimeOid(data)), data);
            }
         }
      }
      return dataRtOids;
   }

   public Map<Long, Pair<IData, String>> findAllStructuredDataRtOids()
   {
      Map<Long, Pair<IData, String>> dataRtOids = new HashMap();

      if (!isPrimitiveData())
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();

         Iterator modelItr = modelManager.getAllModelsForId(getModelId());

         while (modelItr.hasNext())
         {
            IModel model = (IModel) modelItr.next();
            IData data = model.findData(getDataId());
            if (null != data)
            {
               dataRtOids.put(new Long(modelManager.getRuntimeOid(data, attributeName)), new Pair(data, attributeName));
            }
         }
      }
      return dataRtOids;
   }

   /**
    * Compares for equality with another ClusterSlotData. Instances of ClusterSlotData are equal if both
    * values are equal.
    *
    * @return <code>true</code> if this ClusterSlotData is equal the given other ClusterSlotData,
    *         <code>false</code> if the ClusterSlotDatas are not equal or the other value is not an
    *         instance of {@link ClusterSlotData}.
    */
   @Override
   public boolean equals(Object other)
   {
      boolean isEqual = false;

      if (this == other)
      {
         isEqual = true;
      }
      else if (other instanceof ClusterSlotData)
      {
         final ClusterSlotData pair = (ClusterSlotData) other;

         isEqual = (fqDataId != null ? fqDataId.equals(pair.fqDataId) : pair.fqDataId == null)
               && (attributeName != null ? attributeName.equals(pair.attributeName) : pair.attributeName == null);
      }

      return isEqual;
   }

   @Override
   public int hashCode()
   {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((fqDataId == null) ? 0 : fqDataId.hashCode());
      result = PRIME * result + ((attributeName == null) ? 0 : attributeName.hashCode());
      return result;
   }

   @Override
   public String toString()
   {
      return "(" + fqDataId + "," + attributeName + ")";
   }

   public String qualifiedDataToString()
   {
      return getQualifiedDataId() + "' attributeName '" + getAttributeName();
   }
}
