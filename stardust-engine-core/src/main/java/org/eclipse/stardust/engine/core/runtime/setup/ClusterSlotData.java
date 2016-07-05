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

import javax.xml.namespace.QName;

/**
*
* @author stephan.born
*/public class ClusterSlotData
{
   private final QName fqDataId;
   private final String attributeName;

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
}
