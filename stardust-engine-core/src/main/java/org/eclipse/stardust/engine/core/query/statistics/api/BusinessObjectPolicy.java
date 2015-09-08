/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.api.query.EvaluationPolicy;

/**
 * <p>BusinessObjectPolicy can be used in the <code>BenchmarkProcessStatisticsQuery</code> or
 * <code>BenchmarkActivityStatisticsQuery</code> in order to restrict the result set for a
 * given business object resp. business object instances. Furthermore it can also be
 * used to categorize the returned instances for business objects or
 * business object instances for which they have a relation to. This can be expressed by
 * the <code>groupBy()</code> method.</p>
 * <p>To create such a policy you must use the <code>filterFor()</code> factory method.
 * For instance you can create a policy like follows:<br><br>
 *
 * <pre>
 * Set primaryKeyValuesForFilter = ...
 * Set primaryKeyValuesForGroupBy = ...
 *
 * BusinessObjectPolicy.filterFor("{modelId}businessObjectFilter", "primaryKeyId", primaryKeyValuesForFilter)
 *    .groupBy("{modelId}businessObjectGroupBy", "primaryKeyId", primaryKeyValuesForGroupBy);
 * </pre>
 * </p>
 * @author Sven.Rottstock
 */
public class BusinessObjectPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;

   private final BusinessObjectData filter;
   private BusinessObjectData groupBy;

   public static BusinessObjectPolicy filterFor(String modelId, String businessObjectId,
         String primaryKey, Set<Serializable> primaryKeyValues)
   {
      return new BusinessObjectPolicy(new QName(modelId, businessObjectId),
            primaryKey, primaryKeyValues);
   }

   public static BusinessObjectPolicy filterFor(String qualifiedBusinessObjectId,
         String primaryKey, Set<Serializable> primaryKeyValues)
   {
      return new BusinessObjectPolicy(QName.valueOf(qualifiedBusinessObjectId),
            primaryKey, primaryKeyValues);
   }

   private BusinessObjectPolicy(QName businessObject,
         String primaryKey, Set<Serializable> primaryKeyValues)
   {
      filter = new BusinessObjectData(businessObject.getNamespaceURI(),
            businessObject.getLocalPart(), primaryKey, primaryKeyValues);
   }

   public BusinessObjectPolicy groupBy(String modelId, String businessObjectId,
         String primaryKey, Set<Serializable> primaryKeyValues)
   {
      groupBy = new BusinessObjectData(modelId, businessObjectId,
            primaryKey, primaryKeyValues);
      return this;
   }

   public BusinessObjectPolicy groupBy(String qualifiedBusinessObjectId,
         String primaryKey, Set<Serializable> primaryKeyValues)
   {
      QName businessObjectName = QName.valueOf(qualifiedBusinessObjectId);
      return groupBy(businessObjectName.getNamespaceURI(),
            businessObjectName.getLocalPart(), primaryKey, primaryKeyValues);
   }

   public BusinessObjectData getFilter()
   {
      return filter;
   }

   public BusinessObjectData getGroupBy()
   {
      return groupBy;
   }

   public static class BusinessObjectData
   {
      private final String modelId;
      private final String businessObjectId;
      private final String primaryKey;
      private final Set<Serializable> primaryKeyValues;

      private BusinessObjectData(String modelId, String businessObjectId,
         String primaryKey, Set<Serializable> primaryKeyValues)
      {
         this.modelId = modelId;
         this.businessObjectId = businessObjectId;
         this.primaryKey = primaryKey;
         this.primaryKeyValues = primaryKeyValues == null ? null : new HashSet<Serializable>(primaryKeyValues);
      }

      public String getModelId()
      {
         return modelId;
      }

      public String getBusinessObjectId()
      {
         return businessObjectId;
      }

      public String getPrimaryKey()
      {
         return primaryKey;
      }

      public Set<Serializable> getPrimaryKeyValues()
      {
         return Collections.unmodifiableSet(primaryKeyValues);
      }
   }
}
