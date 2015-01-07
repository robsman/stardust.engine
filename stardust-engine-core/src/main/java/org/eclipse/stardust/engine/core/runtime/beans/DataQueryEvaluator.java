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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.DataQuery;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;



public class DataQueryEvaluator extends ModelAwareQueryPredicate<IData>
{

   private DataUsageEvaluator dataUsageEvaluator;

   public DataQueryEvaluator(DataQuery query)
   {
      super(query);
      dataUsageEvaluator = new DataUsageEvaluator();
   }

   public Object getValue(IData data, String attribute, Object expected)
   {
      if (DataQuery.DATA_TYPE_ID.getAttributeName().equals(attribute))
      {
         if (expected == null)
         {
            return "";
         }
         String typeId = data.getType().getId();
         if (typeId.equals(expected))
         {
            return typeId;
         }
         return null;
      }
      else if (DataQuery.DECLARED_TYPE_ID.getAttributeName().equals(attribute))
      {
         String declaredTypeId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         if (expected == null)
         {
            if (declaredTypeId == null)
            {
               return expected;
            }
            return "";
         }
         if (declaredTypeId != null && declaredTypeId.equals(expected))
         {
            return declaredTypeId;
         }
         return null;
      }
      else if (DataQuery.PROCESS_ID.getAttributeName().equals(attribute))
      {
         if (expected == null)
         {
            return "";
         }
         if (dataUsageEvaluator.isUsedInProcess(data, getModel(), (String) expected))
         {
            return expected;
         }
         return null;
      }
      else
      {
         return super.getValue(data, attribute, expected);
      }
   }
}
