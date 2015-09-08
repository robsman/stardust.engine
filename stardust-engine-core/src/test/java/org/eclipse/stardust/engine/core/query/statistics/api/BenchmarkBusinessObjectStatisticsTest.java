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

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsCollectionContaining.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.eclipse.stardust.engine.core.query.statistics.evaluation.BenchmarkBusinessObjectStatisticsResult;

public class BenchmarkBusinessObjectStatisticsTest
{
   @Mock private BenchmarkProcessStatisticsQuery statisticsQuery;
   
   private BenchmarkBusinessObjectStatisticsResult bbosr;
   
   @Before
   public void setUp() throws Exception
   {
      bbosr = new BenchmarkBusinessObjectStatisticsResult(statisticsQuery);
   }

   @Test
   public void testGroupByValues()
   {
      Set<String> groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(0));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group1", "filter1", 1, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(1));
      assertThat(groupByValues, hasItem("group1"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group1", "filter2", 2, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(1));
      assertThat(groupByValues, hasItem("group1"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group2", "filter3", 3, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(2));
      assertThat(groupByValues, hasItems("group1", "group2"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group2", "filter2", 4, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(2));
      assertThat(groupByValues, hasItems("group1", "group2"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group3", "filter4", 2, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(3));
      assertThat(groupByValues, hasItems("group1", "group2", "group3"));
      
      // same PI for the same group/filter shouldn't increase the count
      bbosr.registerBusinessObjectBenchmarkCategory("group3", "filter4", 2, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(3));
      assertThat(groupByValues, hasItems("group1", "group2", "group3"));
   }
   
   @Test
   public void testEmptyGetGroupByValues()
   {
      Set<String> groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(0));
      
      bbosr.registerBusinessObjectBenchmarkCategory("", "filter1", 1, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(1));
      assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      
      bbosr.registerBusinessObjectBenchmarkCategory("", "filter1", 2, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(1));
      assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      
      bbosr.registerBusinessObjectBenchmarkCategory(null, "filter3", 3, 1);
      groupByValues = bbosr.getGroupByValues();
      assertNotNull(groupByValues);
      assertThat(groupByValues.size(), is(1));
      assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
   }

   @Test
   public void testFilterValues()
   {
      Set<String> filterValues = bbosr.getFilterValues(null);
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(0));
      
      filterValues = bbosr.getFilterValues("");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(0));
      
      filterValues = bbosr.getFilterValues("    ");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(0));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group1", "filter1", 1, 1);
      filterValues = bbosr.getFilterValues(null);
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(0));
      filterValues = bbosr.getFilterValues("group1");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter1"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group1", "filter2", 2, 1);
      filterValues = bbosr.getFilterValues("group1");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      
      // same PI for the same group/filter shouldn't increase the count
      bbosr.registerBusinessObjectBenchmarkCategory("group1", "filter2", 2, 1);
      filterValues = bbosr.getFilterValues("group1");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group2", "filter3", 3, 1);
      filterValues = bbosr.getFilterValues("group1");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      filterValues = bbosr.getFilterValues("group2");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter3"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group2", "filter2", 4, 1);
      filterValues = bbosr.getFilterValues("group1");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      filterValues = bbosr.getFilterValues("group2");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter3", "filter2"));
      filterValues = bbosr.getFilterValues("group3");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(0));
      
      bbosr.registerBusinessObjectBenchmarkCategory("group3", "filter4", 2, 1);
      filterValues = bbosr.getFilterValues("group1");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      filterValues = bbosr.getFilterValues("group2");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter3", "filter2"));
      filterValues = bbosr.getFilterValues("group3");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter4"));
      
      filterValues = bbosr.getFilterValues("group4");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(0));
      
      bbosr.addAbortedInstance("group4", "filter4", 2);
      filterValues = bbosr.getFilterValues("group4");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter4"));
      
      bbosr.addAbortedInstance("group3", "filter4", 2);
      filterValues = bbosr.getFilterValues("group3");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter4"));
      
      bbosr.addAbortedInstance("group3", "filter5", 3);
      filterValues = bbosr.getFilterValues("group3");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter4", "filter5"));
            
      bbosr.addCompletedInstance("group3", "filter4", 2);
      filterValues = bbosr.getFilterValues("group3");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter4", "filter5"));
      
      bbosr.addCompletedInstance("group3", "filter6", 2);
      filterValues = bbosr.getFilterValues("group3");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(3));
      assertThat(filterValues, hasItems("filter4", "filter5", "filter6"));
   }
   
   @Test
   public void testFilterValuesWithEmptyGroupBys()
   {
      bbosr.registerBusinessObjectBenchmarkCategory("", "filter1", 1, 1);
      Set<String> filterValues = bbosr.getFilterValues(null);
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      filterValues = bbosr.getFilterValues("");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter1"));
      filterValues = bbosr.getFilterValues(null);
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(1));
      assertThat(filterValues, hasItem("filter1"));
      
      bbosr.registerBusinessObjectBenchmarkCategory("", "filter2", 2, 1);
      filterValues = bbosr.getFilterValues(null);
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      filterValues = bbosr.getFilterValues("");
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));
      
      // same PI for the same filter shouldn't increase the count
      bbosr.registerBusinessObjectBenchmarkCategory(null, "filter2", 2, 1);
      filterValues = bbosr.getFilterValues(null);
      assertNotNull(filterValues);
      assertThat(filterValues.size(), is(2));
      assertThat(filterValues, hasItems("filter1", "filter2"));      
   }

   @Test
   public void testGetAbortedPerBusinessObject()
   {
      final String GROUP1 = "group1";
      final String GROUP2 = "group2";
      
      Set<Long> processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(0));
      
      bbosr.addAbortedInstance(GROUP1, "filter1", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItem(2l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(2l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(2l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(2l));
      
      bbosr.addAbortedInstance(GROUP1, "filter1", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addAbortedInstance(GROUP1, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addAbortedInstance(GROUP2, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addAbortedInstance(GROUP1, "filter3", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 3)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addAbortedInstance(GROUP2, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 3)
      //   group2: 1 (OID: 3)
      //     filter2: 1 (OID: 3)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP2, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP2, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addAbortedInstance(GROUP2, "filter3", 4);
      // Total: 1 (OID: 2,3,4)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 3)
      //   group2: 1 (OID: 3,4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP2, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(4l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP2, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP2, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(3l,4l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getAbortedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(3));
      assertThat(processInstancesOIDs, hasItems(2l, 3l, 4l));
   }

   @Test
   public void testGetAbortedCountForBusinessObject()
   {
      final String GROUP1 = "group1";
      final String GROUP2 = "group2";
      
      // active PI shouldn't influence the aborted count
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter1", 1, 1);
      long count = bbosr.getAbortedCount(GROUP1, "filter");
      assertThat(count, is(0l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(0l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(0l));
      
      // insert a PI for a given groupBy and filter
      bbosr.addAbortedInstance(GROUP1, "filter1", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      count = bbosr.getAbortedCount(GROUP1, "filter1");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(1l));
      // validate if filter1 was only set for group1
      count = bbosr.getAbortedCount("group0", "filter1");
      assertThat(count, is(0l));
      
      // insert the same PI for a given groupBy but a different filter
      bbosr.addAbortedInstance(GROUP1, "filter2", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      count = bbosr.getAbortedCount(GROUP1, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP1, "filter1");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(1l));

      // insert the same PI for the same groupBy and filter shouldn't count
      bbosr.addAbortedInstance(GROUP1, "filter2", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      count = bbosr.getAbortedCount(GROUP1, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(1l));
      
      // insert another PI for the same groupbBy but a different filter
      bbosr.addAbortedInstance(GROUP1, "filter3", 3);
      // Total: 2 (OID: 2,3)
      //   group1: 2 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      //     filter3: 3 (OID: 3)
      count = bbosr.getAbortedCount(GROUP1, "filter3");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(2l));
      
      // insert same PI for another groupbBy
      bbosr.addAbortedInstance(GROUP2, "filter2", 3);
      // Total: 2 (OID: 2,3)
      //   group1: 2 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      //     filter3: 3 (OID: 3)
      //   group2: 1 (OID: 3)
      //     filter2: 1 (OID: 3)
      count = bbosr.getAbortedCount(GROUP2, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP2, null);
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(2l));
      
      bbosr.addAbortedInstance(GROUP2, "filter3", 4);
      // Total: 2 (OID: 2,3,4)
      //   group1: 2 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      //     filter3: 3 (OID: 3)
      //   group2: 1 (OID: 3,4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)
      count = bbosr.getAbortedCount(GROUP2, "filter3");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP2, null);
      assertThat(count, is(2l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(3l));
      
      // completed instance shouldn't increase the aborted count
      bbosr.addCompletedInstance(GROUP1, "filter2", 4);
      count = bbosr.getAbortedCount(GROUP1, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getAbortedCount(GROUP2, null);
      assertThat(count, is(2l));
      count = bbosr.getAbortedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getAbortedCount(null, null);
      assertThat(count, is(3l));
   }

   @Test
   public void testGetCompletedCountForBusinessObject()
   {
      final String GROUP1 = "group1";
      final String GROUP2 = "group2";
      
      // active PI shouldn't influence the completed count
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter1", 1, 1);
      long count = bbosr.getCompletedCount(GROUP1, "filter");
      assertThat(count, is(0l));
                  
      bbosr.addCompletedInstance(GROUP1, "filter1", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      count = bbosr.getCompletedCount(GROUP1, "filter1");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount("group0", "filter1");
      assertThat(count, is(0l));
      
      bbosr.addCompletedInstance(GROUP1, "filter2", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      count = bbosr.getCompletedCount(GROUP1, "filter1");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(1l));
      
      bbosr.addCompletedInstance(GROUP1, "filter2", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      count = bbosr.getCompletedCount(GROUP1, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(1l));
      
      bbosr.addCompletedInstance(GROUP1, "filter3", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      //     filter3: 1 (OID: 3)
      count = bbosr.getCompletedCount(GROUP1, "filter3");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, "filter1");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(2l));
      
      bbosr.addCompletedInstance(GROUP1, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)
      //     filter3: 1 (OID: 3)
      count = bbosr.getCompletedCount(GROUP1, "filter2");
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(GROUP1, "filter3");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, "filter1");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(2l));
      
      // insert same PI for another groupbBy
      bbosr.addCompletedInstance(GROUP2, "filter2", 3);
      // Total: 2 (OID: 2,3)
      //   group1: 2 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)
      //     filter3: 3 (OID: 3)
      //   group2: 1 (OID: 3)
      //     filter2: 1 (OID: 3)
      count = bbosr.getCompletedCount(GROUP2, "filter2");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, "filter2");
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(GROUP2, null);
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(2l));
      
      bbosr.addCompletedInstance(GROUP2, "filter3", 4);
      // Total: 2 (OID: 2,3,4)
      //   group1: 2 (OID: 2,3)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)
      //     filter3: 3 (OID: 3)
      //   group2: 1 (OID: 3,4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)
      count = bbosr.getCompletedCount(GROUP2, "filter3");
      assertThat(count, is(1l));
      count = bbosr.getCompletedCount(GROUP2, null);
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(GROUP1, null);
      assertThat(count, is(2l));
      count = bbosr.getCompletedCount(null, null);
      assertThat(count, is(3l));
      
      // aborted instance shouldn't increase the completed count
      bbosr.addAbortedInstance(GROUP1, "filter2", 4);
      count = bbosr.getCompletedCount(GROUP1, "filter2");
      assertThat(count, is(2l));
   }
   
   @Test
   public void testGetCompletedPerBusinessObject()
   {
      final String GROUP1 = "group1";
      final String GROUP2 = "group2";
      
      Set<Long> processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(0));
      
      bbosr.addCompletedInstance(GROUP1, "filter1", 2);
      // Total: 1 (OID: 2)
      //   group1: 1 (OID: 2)
      //     filter1: 1 (OID: 2)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItem(2l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(2l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(2l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(2l));
      
      bbosr.addCompletedInstance(GROUP1, "filter1", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addCompletedInstance(GROUP1, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addCompletedInstance(GROUP2, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addCompletedInstance(GROUP1, "filter3", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 3)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addCompletedInstance(GROUP2, "filter2", 3);
      // Total: 1 (OID: 2,3)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 3)
      //   group2: 1 (OID: 3)
      //     filter2: 1 (OID: 3)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP2, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP2, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      
      bbosr.addCompletedInstance(GROUP2, "filter3", 4);
      // Total: 1 (OID: 2,3,4)
      //   group1: 1 (OID: 2,3)
      //     filter1: 1 (OID: 2,3)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 3)
      //   group2: 1 (OID: 3,4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP2, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(4l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP2, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP2, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(3l,4l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter3");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter2");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(1));
      assertThat(processInstancesOIDs, hasItems(3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, "filter1");
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(GROUP1, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(2));
      assertThat(processInstancesOIDs, hasItems(2l, 3l));
      processInstancesOIDs = bbosr.getCompletedProcessInstanceOIDs(null, null);
      assertNotNull(processInstancesOIDs);
      assertThat(processInstancesOIDs.size(), is(3));
      assertThat(processInstancesOIDs, hasItems(2l, 3l, 4l));
   }

   @Test
   public void testGetBenchmarkCategoryCounts()
   {
      final String GROUP1 = "group1";
      final String GROUP2 = "group2";
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter1", 2, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)
      //   group1:    1 (OID: 2)
      //     filter1: 1 (OID: 2)
      long count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount("group0", "filter1", 0);
      assertThat(count, is(0l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 2, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)
      //   group1:    1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(1l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 2, 1);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)       1 (OID: 2)
      //   group1:    1 (OID: 2)       1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)       1 (OID: 2)
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(1l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 2, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)       1 (OID: 2)
      //   group1:    1 (OID: 2)       1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)       1 (OID: 2)
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(1l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter3", 3, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3)     1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)       1 (OID: 2)
      //     filter3: 1 (OID: 3)
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(1l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 3, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3)     1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(1l));
      
      // insert same PI for another groupbBy
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP2, "filter2", 3, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3)     1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      //   group2:    1 (OID: 3)
      //     filter2: 1 (OID: 3)
      count = bbosr.getBenchmarkCategoryCount(GROUP2, "filter2", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, "filter2", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(1l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP2, "filter3", 4, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3,4)   1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      //   group2:    1 (OID: 3,4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)
      count = bbosr.getBenchmarkCategoryCount(GROUP2, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, "filter3", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(3l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, null, 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(1l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP2, "filter3", 4, 1);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3,4)   1 (OID: 2,4)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      //   group2:    1 (OID: 3,4)     1 (OID: 4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)       1 (OID: 4)
      count = bbosr.getBenchmarkCategoryCount(GROUP2, "filter3", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter3", 1);
      assertThat(count, is(0l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter1", 0);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, "filter2", 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, null, 0);
      assertThat(count, is(2l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 0);
      assertThat(count, is(3l));
      count = bbosr.getBenchmarkCategoryCount(GROUP1, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(GROUP2, null, 1);
      assertThat(count, is(1l));
      count = bbosr.getBenchmarkCategoryCount(null, null, 1);
      assertThat(count, is(2l));
   }

   @Test
   public void testGetBenchmarkCategoryCountsStringString()
   {
      final String GROUP1 = "group1";
      final String GROUP2 = "group2";
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter1", 2, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)
      //   group1:    1 (OID: 2)
      //     filter1: 1 (OID: 2)
      Set<Long> processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 2, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)
      //   group1:    1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 2, 1);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)       1 (OID: 2)
      //   group1:    1 (OID: 2)       1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)       1 (OID: 2)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 2, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2)       1 (OID: 2)
      //   group1:    1 (OID: 2)       1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)       1 (OID: 2)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter3", 3, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3)     1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2)       1 (OID: 2)
      //     filter3: 1 (OID: 3)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter3", 0);
      assertThat(processInstances, hasItems(3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP1, "filter2", 3, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3)     1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter3", 0);
      assertThat(processInstances, hasItems(3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l));
      
      // insert same PI for another groupbBy
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP2, "filter2", 3, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3)     1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      //   group2:    1 (OID: 3)
      //     filter2: 1 (OID: 3)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, "filter2", 0);
      assertThat(processInstances, hasItems(3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter3", 0);
      assertThat(processInstances, hasItems(3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP2, "filter3", 4, 0);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3,4)   1 (OID: 2)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      //   group2:    1 (OID: 3,4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, "filter3", 0);
      assertThat(processInstances, hasItems(4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter3", 0);
      assertThat(processInstances, hasItems(3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, null, 0);
      assertThat(processInstances, hasItems(3l, 4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(3l, 4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l));
      
      bbosr.registerBusinessObjectBenchmarkCategory(GROUP2, "filter3", 4, 1);
      // Hierarchy    BenchmarkValue0  BenchmarkValue1
      // Total:       1 (OID: 2,3,4)   1 (OID: 2,4)
      //   group1:    1 (OID: 2,3)     1 (OID: 2)
      //     filter1: 1 (OID: 2)
      //     filter2: 1 (OID: 2,3)     1 (OID: 2)
      //     filter3: 1 (OID: 3)
      //   group2:    1 (OID: 3,4)     1 (OID: 4)
      //     filter2: 1 (OID: 3)
      //     filter3: 1 (OID: 4)       1 (OID: 4)
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, "filter3", 1);
      assertThat(processInstances, hasItems(4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, "filter3", 0);
      assertThat(processInstances, hasItems(4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter3", 0);
      assertThat(processInstances, hasItems(3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter1", 0);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, "filter2", 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 0);
      assertThat(processInstances, hasItems(2l, 3l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, null, 0);
      assertThat(processInstances, hasItems(3l, 4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, null, 1);
      assertThat(processInstances, hasItems(4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 0);
      assertThat(processInstances, hasItems(2l, 3l, 4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP1, null, 1);
      assertThat(processInstances, hasItems(2l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(GROUP2, null, 1);
      assertThat(processInstances, hasItems(4l));
      processInstances = bbosr.getProcessInstanceOIDsForBenchmarkCategory(null, null, 1);
      assertThat(processInstances, hasItems(2l, 4l));
   }
}
