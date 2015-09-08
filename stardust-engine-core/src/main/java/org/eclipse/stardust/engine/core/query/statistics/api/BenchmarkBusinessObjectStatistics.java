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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;

public abstract class BenchmarkBusinessObjectStatistics 
   extends BenchmarkStatistics<String /* {groupById}filterId */, Set<Long /* piOID */>>
{
   private static final long serialVersionUID = 1L;
   
   public static final String NO_GROUPBY_VALUE = XMLConstants.NULL_NS_URI;
   public static final String NO_FILTER_VALUE = null;
   
   private final Set<String /* groupById */> groupByValues;
   private final Map<String /* groupById */, Set<String /* {groupById}filterId */>> filterValuesPerGroupBy;
   
   private final Map<Integer /* benchmarkValue */, Map<String /* {groupById}filterId */, Set<Long /* piOID */>>> pisPerBenchmarkValueAndItem;
   
   enum HierarchyLevel {
      TOTAL,
      GROUPBY,
      FILTER_IN_GROUPBY,
      FILTER_WITHOUT_GROUPBY
   }

   public BenchmarkBusinessObjectStatistics(BenchmarkProcessStatisticsQuery query)
   {
      super(query);
      
      groupByValues = CollectionUtils.newSet();
      filterValuesPerGroupBy = CollectionUtils.newMap();
      
      pisPerBenchmarkValueAndItem = CollectionUtils.newMap();
   }
   
   @Override
   protected long getAbortedCount(String key)
   {
      Set<Long> pisPerAbortedItem = getAbortedPerItem().get(key);
      return pisPerAbortedItem == null ? 0l : pisPerAbortedItem.size();
   }
   
   @Override
   protected long getCompletedCount(String key)
   {
      Set<Long> pisPerCompletedItem = getCompletedPerItem().get(key);
      return pisPerCompletedItem == null ? 0l : pisPerCompletedItem.size();
   }
   
   /* Internal API used by the statistics query engine */
   
   protected void incrementAbortedPerItem(String groupByValue,
         String filterValue, long piOID)
   {
      String itemKey = addItemKey(groupByValue, filterValue);
      
      Map<String, Set<Long>> pisPerAbortedItem = getAbortedPerItem();
      
      addProcessInstance(pisPerAbortedItem, itemKey, piOID);
   }
   
   protected void incrementCompletedPerItem(String groupByValue,
         String filterValue, long piOID)
   {
      String itemKey = addItemKey(groupByValue, filterValue);
      
      Map<String, Set<Long>> pisPerCompletedItem = getCompletedPerItem();
      
      addProcessInstance(pisPerCompletedItem, itemKey, piOID);
   }
   
   protected void registerBenchmarkValue(String groupByValue,
         String filterValue, long piOID, int benchmarkValue)
   {
      String itemKey = addItemKey(groupByValue, filterValue);
            
      Map<String, Set <Long>> pisPerItem = pisPerBenchmarkValueAndItem.get(benchmarkValue);
      
      if(pisPerItem == null)
      {
         pisPerItem = CollectionUtils.newMap();
         pisPerBenchmarkValueAndItem.put(benchmarkValue, pisPerItem);
      }
      
      addProcessInstance(pisPerItem, itemKey, piOID);
   }
   
   /* Helper methods */
   
   private void addProcessInstance(Map<String, Set<Long>> itemMap,
         String itemKey, long piOID)
   {
      Set<Long> processInstanceOIDs = itemMap.get(itemKey);
      if(processInstanceOIDs == null)
      {
         processInstanceOIDs = CollectionUtils.newSet();
         itemMap.put(itemKey, processInstanceOIDs);
      }
      processInstanceOIDs.add(piOID);
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
         groupByValue = NO_GROUPBY_VALUE;
      }
      return groupByValue;
   }
   
   private static String generateItemKey(String groupByValue, String filterValue)
   {
      groupByValue = getNormalizedGroupByValue(groupByValue);
      return new QName(groupByValue, filterValue).toString();
   }
   
   private static String getGroupByValueFromItemKey(String qualifiedItemKey)
   {
      QName name = QName.valueOf(qualifiedItemKey);
      return name.getNamespaceURI();
   }
   
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
            Set<Long> processInstanceOIDs = map.get(itemKey);
            if(processInstanceOIDs != null)
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
   
   /* Public API */
   
   public Set<String> getGroupByValues()
   {
      return Collections.unmodifiableSet(groupByValues);
   }
   
   public Set<String> getFilterValues(String groupByValue)
   {
      if(groupByValue == null || groupByValue.trim().length() == 0)
      {
         groupByValue = NO_GROUPBY_VALUE;
      }
      Set<String> filterValues = filterValuesPerGroupBy.get(groupByValue);
      return filterValues == null ? Collections.<String>emptySet() : filterValues;
   }
   
   public Set<Integer> getRegisterdBenchmarkValues()
   {
      return Collections.unmodifiableSet(pisPerBenchmarkValueAndItem.keySet());
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
      Map<String, Set <Long>> pisPerItem = pisPerBenchmarkValueAndItem.get(benchmarkValue);
      
      if(pisPerItem == null)
      {
         return 0l;
      }
      
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedCount(level, groupByValue, pisPerItem);
      }
      
      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> processInstances = pisPerItem.get(itemKey);
      return processInstances == null ? 0l : processInstances.size();
   }
   
   public Set<Long> getProcessInstanceOIDsForBenchmarkCategory(
         String groupByValue, String filterValue, int benchmarkValue)
   {
      Map<String, Set <Long>> pisPerItem = pisPerBenchmarkValueAndItem.get(benchmarkValue);
      
      if(pisPerItem == null)
      {
         return Collections.emptySet();
      }
      
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedPIs(level, groupByValue, pisPerItem);
      }
      
      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> processInstances = pisPerItem.get(itemKey);
      return processInstances == null ? 
            Collections.<Long>emptySet() : Collections.unmodifiableSet(processInstances);
   }

   public Set<Long> getAbortedProcessInstanceOIDs(String groupByValue, String filterValue)
   {
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedPIs(level, groupByValue, getAbortedPerItem());
      }
      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> processInstances = getAbortedPerItem().get(itemKey);
      return processInstances == null ? 
            Collections.<Long>emptySet() : Collections.unmodifiableSet(processInstances);
   }

   public Set<Long> getCompletedProcessInstanceOIDs(String groupByValue, String filterValue)
   {
      HierarchyLevel level = getHierarchyLevel(groupByValue, filterValue);
      if(level == HierarchyLevel.TOTAL || level == HierarchyLevel.GROUPBY)
      {
         return getCumulatedPIs(level, groupByValue, getCompletedPerItem());
      }
      String itemKey = generateItemKey(groupByValue, filterValue);
      Set<Long> processInstances = getCompletedPerItem().get(itemKey);
      return processInstances == null ? 
         Collections.<Long>emptySet() : Collections.unmodifiableSet(processInstances);
   }
}
