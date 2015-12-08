/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectRelationship.BusinessObjectReference;

public class PropagateAccessEvaluator
{
   public static interface IPropagatedAccessEvalFunction<T>
   {
      T execute(IData otherData, BusinessObjectRelationship businessObjectRelationship);
   }

   private Set<IData> visitedData = CollectionUtils.newHashSet();

   private IData data;

   private IPropagatedAccessEvalFunction<Boolean> function;

   private Boolean result = null;


   public PropagateAccessEvaluator(IData data, IPropagatedAccessEvalFunction<Boolean> function)
   {
      this.data = data;
      this.function = function;
   }

   public IData getData()
   {
      return data;
   }

   public IPropagatedAccessEvalFunction<Boolean> getFunction()
   {
      return function;
   }

   public synchronized boolean evaluate()
   {
      if (result != null)
      {
         return result;
      }

      result = doEvaluate(data);
      return result;
   }

   private boolean doEvaluate(IData data)
   {
      // prevent cyclic revisiting of same data.
      if (visitedData.contains(data))
      {
         return false;
      }
      else
      {
         visitedData.add(data);
      }

      Map<String, BusinessObjectRelationship> businessObjectRelationships = BusinessObjectUtils.getBusinessObjectRelationships(
            data);
      Collection<BusinessObjectRelationship> values = businessObjectRelationships
            .values();
      for (BusinessObjectRelationship businessObjectRelationship : values)
      {
         BusinessObjectReference otherBusinessObject = businessObjectRelationship.otherBusinessObject;
         IData otherData = BusinessObjectUtils.findDataForUpdate(otherBusinessObject.modelId,
               otherBusinessObject.id);
         Map<String, BusinessObjectRelationship> otherBusinessObjectRelationships = BusinessObjectUtils.getBusinessObjectRelationships(
               otherData);
         Collection<BusinessObjectRelationship> otherRelationships = otherBusinessObjectRelationships
               .values();
         Boolean allowedFound = false;
         for (BusinessObjectRelationship otherBusinessObjectRelationship : otherRelationships)
         {
            if (Boolean.TRUE.equals(otherBusinessObjectRelationship.propagateAccess))
            {
               allowedFound = function.execute(otherData, businessObjectRelationship);
               if (allowedFound)
               {
                  return true;
               }
            }
         }
         if (!allowedFound)
         {
            // traverse propagated relationship for non visited data
            allowedFound = doEvaluate(otherData);
            if (allowedFound)
            {
               return true;
            }
         }
      }
      return false;
   }

}
