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
package org.eclipse.stardust.engine.api.query;


public class DataPrefetchHint extends AbstractDataFilter
{
   private static final long serialVersionUID = 1L;
   private int prefetchNumberValueColumnIdx;

   public DataPrefetchHint(String dataId)
   {
      this(dataId, null);
   }

   public DataPrefetchHint(String dataId, String attributeName)
   {
      super(dataId, attributeName, null, null, MODE_ALL_FROM_SCOPE);
   }

   /**
    * Returns a string representation of the filter definition.
    */
   public String toString()
   {
      String attributeString = this.getAttributeName() == null ? "" : "("
            + this.getAttributeName() + ")";

      return "data['" + getDataID() + "'" + attributeString + "] " + getOperator();
   }


   @Override
   public int hashCode()
   {
      String dataID = getDataID();
      String attributeName = getAttributeName();
      final int prime = 31;
      int result = 1;
      result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
      result = prime * result + ((dataID == null) ? 0 : dataID.hashCode());
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
      DataPrefetchHint other = (DataPrefetchHint) obj;
      String dataID = getDataID();
      String attributeName = getAttributeName();
      if (attributeName == null)
      {
         if (other.getAttributeName() != null)
            return false;
      }
      else if (!attributeName.equals(other.getAttributeName()))
         return false;
      if (dataID == null)
      {
         if (other.getDataID() != null)
            return false;
      }
      else if (!dataID.equals(other.getDataID()))
         return false;
      return true;
   }

   public int getPrefetchNumberValueColumnIdx()
   {
      return prefetchNumberValueColumnIdx;
   }

   public void setPrefetchNumberValueColumnIdx(int colIdx)
   {
      prefetchNumberValueColumnIdx = colIdx;
   }
}
