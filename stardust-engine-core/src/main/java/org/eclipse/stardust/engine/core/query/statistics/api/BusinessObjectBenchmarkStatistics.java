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
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;

public class BusinessObjectBenchmarkStatistics
   extends BenchmarkStatistics<String /* {groupById}filterId */, Set<Long /* instanceOID */>>
   implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final Set<String /* groupById */> groupByValues;
   private final Map<String /* groupById */, Set<String /* {groupById}filterId */>> filterValuesPerGroupBy;

   private final Map<Integer /* benchmarkValue */, Map<String /* {groupById}filterId */, Set<Long /* instanceOID */>>> instancesPerBenchmarkValueAndItem;

   enum HierarchyLevel {
      TOTAL,
      GROUPBY,
      FILTER_IN_GROUPBY,
      FILTER_WITHOUT_GROUPBY
   }

   public BusinessObjectBenchmarkStatistics()
   {
      groupByValues = CollectionUtils.newSet();
      filterValuesPerGroupBy = CollectionUtils.newMap();

      instancesPerBenchmarkValueAndItem = CollectionUtils.newMap();
   }

   @Override
   public long getCompletedCount(String key)
   {
      Set<Long> instancesPerCompletedItem = getCompletedPerItem().get(key);
      return instancesPerCompletedItem == null ? 0l : instancesPerCompletedItem.size();
   }

   @Override
   public long getAbortedCount(String key)
   {
      Set<Long> instancesPerAbortedItem = getAbortedPerItem().get(key);
      return instancesPerAbortedItem == null ? 0l : instancesPerAbortedItem.size();
   }

   public Set<String> getGroupByValues()
   {
      return Collections.unmodifiableSet(groupByValues);
   }

   public Set<String> getFilterValues(String groupByValue)
   {
      if(groupByValue == null || groupByValue.trim().length() == 0)
      {
         groupByValue = BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE;
      }
      Set<String> filterValues = filterValuesPerGroupBy.get(groupByValue);
      return filterValues == null ? Collections.<String>emptySet() : filterValues;
   }

   public Set<Integer> getRegisterdBenchmarkValues()
   {
      return Collections.unmodifiableSet(instancesPerBenchmarkValueAndItem.keySet());
   }

   public long getAbortedCount(String groupByValue, String filterValue)
   {
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedCount(level, groupByValue, getAbortedPerItem());
      }
      String itemKey = generateItemKey(groupByValue, filterValue);
      return getAbortedCount(itemKey);
   }

   public long getCompletedCount(String groupByValue, String filterValue)
   {
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedCount(level, groupByValue, getCompletedPerItem());
      }
      String itemKey = generateItemKey(groupByValue, filterValue);
      return getCompletedCount(itemKey);
   }

   public long getBenchmarkCategoryCount(String groupByValue, String filterValue,
         int benchmarkValue)
   {
      Map<String, Set <Long>> instancesPerItem = instancesPerBenchmarkValueAndItem.get(benchmarkValue);

      if(instancesPerItem == null)
      {
         return 0l;
      }

      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedCount(level, groupByValue, instancesPerItem);
      }

      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> instances = instancesPerItem.get(itemKey);
      return instances == null ? 0l : instances.size();
   }

   public Set<Long> getInstanceOIDsForBenchmarkCategory(
         String groupByValue, String filterValue, int benchmarkValue)
   {
      Map<String, Set <Long>> instancesPerItem = instancesPerBenchmarkValueAndItem.get(benchmarkValue);

      if(instancesPerItem == null)
      {
         return Collections.emptySet();
      }

      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedPIs(level, groupByValue, instancesPerItem);
      }

      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> instances = instancesPerItem.get(itemKey);
      return instances == null ?
            Collections.<Long>emptySet() : Collections.unmodifiableSet(instances);
   }

   public Set<Long> getAbortedInstanceOIDs(String groupByValue, String filterValue)
   {
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedPIs(level, groupByValue, getAbortedPerItem());
      }
      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> instances = getAbortedPerItem().get(itemKey);
      return instances == null ?
            Collections.<Long>emptySet() : Collections.unmodifiableSet(instances);
   }

   public Set<Long> getCompletedInstanceOIDs(String groupByValue, String filterValue)
   {
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedPIs(level, groupByValue, getCompletedPerItem());
      }
      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> instances = getCompletedPerItem().get(itemKey);
      return instances == null ?
         Collections.<Long>emptySet() : Collections.unmodifiableSet(instances);
   }

   /* Internal API used by the statistics query engine */

   public void incrementAbortedPerItem(String groupByValue,
         String filterValue, long instanceOID)
   {
      String itemKey = addItemKey(groupByValue, filterValue);

      Map<String, Set<Long>> instancesPerAbortedItem = getAbortedPerItem();

      addInstance(instancesPerAbortedItem, itemKey, instanceOID);
   }

   public void incrementCompletedPerItem(String groupByValue,
         String filterValue, long instanceOID)
   {
      String itemKey = addItemKey(groupByValue, filterValue);

      Map<String, Set<Long>> instancesPerCompletedItem = getCompletedPerItem();

      addInstance(instancesPerCompletedItem, itemKey, instanceOID);
   }

   public void registerBenchmarkValue(String groupByValue,
         String filterValue, long instanceOID, int benchmarkValue)
   {
      String itemKey = addItemKey(groupByValue, filterValue);

      Map<String, Set <Long>> instancesPerItem = instancesPerBenchmarkValueAndItem.get(benchmarkValue);

      if(instancesPerItem == null)
      {
         instancesPerItem = CollectionUtils.newMap();
         instancesPerBenchmarkValueAndItem.put(benchmarkValue, instancesPerItem);
      }

      addInstance(instancesPerItem, itemKey, instanceOID);
   }

   /* Helper methods */

   private void addInstance(Map<String, Set<Long>> itemMap,
         String itemKey, long instanceOID)
   {
      Set<Long> instanceOIDs = itemMap.get(itemKey);
      if(instanceOIDs == null)
      {
         instanceOIDs = CollectionUtils.newSet();
         itemMap.put(itemKey, instanceOIDs);
      }
      instanceOIDs.add(instanceOID);
   }

   private String addItemKey(String groupByValue, String filterValue)
   {
      groupByValue = getNormalizedGroupByValue(groupByValue);
      groupByValues.add(groupByValue);

      Set<String> filterValues = filterValuesPerGroupBy.get(groupByValue);
      if(filterValues == null)
      {
         filterValues = CollectionUtils.newSet();
         filterValuesPerGroupBy.put(groupByValue, filterValues);
      }
      filterValues.add(filterValue);
      return generateItemKey(groupByValue, filterValue);
   }

   private static String getNormalizedGroupByValue(String groupByValue)
   {
      if(groupByValue == null || groupByValue.trim().length() == 0)
      {
         groupByValue = BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE;
      }
      return groupByValue;
   }

   private static String generateItemKey(String groupByValue, String filterValue)
   {
      groupByValue = getNormalizedGroupByValue(groupByValue);
      return new QName(groupByValue, filterValue).toString();
   }

   /*private static String getGroupByValueFromItemKey(String qualifiedItemKey)
   {
      QName name = QName.valueOf(qualifiedItemKey);
      return name.getNamespaceURI();
   }*/

   private Set<Long> getCumulatedPIs(HierarchyLevel level, String groupByValue, Map<String, Set<Long>> map)
   {
      Set<Long> uniqueProcessInstanceOIDs = CollectionUtils.newSet();

      Set<String> groupByValues =
            level == HierarchyLevel.GROUPBY
               ? Collections.singleton(groupByValue) : getGroupByValues();

      for(String groupBy : groupByValues)
      {
         Set<String> filterValues = getFilterValues(groupBy);
         for(String filterValue : filterValues)
         {
            String itemKey = generateItemKey(groupBy, filterValue);
            Set<Long> instanceOIDs = map.get(itemKey);
            if(instanceOIDs != null)
            {
               uniqueProcessInstanceOIDs.addAll(map.get(itemKey));
            }
         }
      }
      return uniqueProcessInstanceOIDs;
   }

   private long getCumulatedCount(HierarchyLevel level, String groupByValue, Map<String, Set<Long>> map)
   {
      return getCumulatedPIs(level, groupByValue, map).size();
   }

   private HierarchyLevel getHierarchyLevel(String groupByValue, String filterValue)
   {
      boolean emptyGroupByValue = StringUtils.isEmpty(getNormalizedGroupByValue(groupByValue));
      boolean emptyFilterValue = StringUtils.isEmpty(filterValue);

      HierarchyLevel level = null;
      if(emptyGroupByValue && emptyFilterValue)
      {
         level = HierarchyLevel.TOTAL;
      }
      else if(!emptyGroupByValue && emptyFilterValue)
      {
         level = HierarchyLevel.GROUPBY;
      }
      else if(!emptyGroupByValue && !emptyFilterValue)
      {
         level = HierarchyLevel.FILTER_IN_GROUPBY;
      }
      else /* (emptyGroupByValue && !emptyFilterValue) */
      {
         level = HierarchyLevel.FILTER_WITHOUT_GROUPBY;
      }
      return level;
   }

}
