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
import java.util.Set;

import javax.xml.XMLConstants;

/**
 * <code>BenchmarkBusinessObjectStatistics</code> is returned by
 * {@link org.eclipse.stardust.engine.api.runtime.QueryService#getAllProcessInstances(query) QueryService.getAllProcessInstances(query)}
 * and {@link org.eclipse.stardust.engine.api.runtime.QueryService#getAllActivityInstances(query) QueryService.getAllActivityInstances(query)}
 * if the <code>query</code> was of type {@link BenchmarkProcessStatisticsQuery} resp.
 * {@link BenchmarkActivityStatisticsQuery} and if the query policy
 * {@link BusinessObjectPolicy} was set.
 *  
 * <p>
 * The following code shows an example implementation how the API can be used to
 * retrieve the statistics:
 * <pre>
 * BenchmarkBusinessObjectStatistics stats = ...
 * for(String groupBy : stats.getGroupByValues())
 * {
 *    for(String filterValue : stats.getFilterValues(groupBy))
 *    {
 *       int count = stats.getBenchmarkCategoryCount(
 *          groupBy, filterValue, benchmarkValue);
 *    }
 *    if(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE.equals(groupBy))
 *    {
 *      // no group count available
 *    }
 *    else
 *    {
 *       int groupCount = stats.getBenchmarkCategoryCount(
 *          groupBy, BenchmarkBusinessObjectStatistics.NO_FILTER_VALUE, benchmarkValue);
 *    }
 * }
 * int totalCount = stats.getBenchmarkCategoryCount(
 *    BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE,
 *    BenchmarkBusinessObjectStatistics.NO_FILTER_VALUE,
 *    benchmarkValue);
 * </pre>
 * </p>
 * @author Sven.Rottstock
 */
public interface BenchmarkBusinessObjectStatistics extends Serializable
{
   /**
    * Constant identifies a non-categorized filter business object 
    */
   String NO_GROUPBY_VALUE = XMLConstants.NULL_NS_URI;

   /**
    * Constant may be used if a grouped statistic is requested. So instead of:
    * <pre>
    * BenchmarkBusinessObjectStatistics stats = ...
    * int count = stats.getCompletedCount("groupBusinessObjectName", null);
    * </pre>
    * you can also use:
    * <pre>
    * BenchmarkBusinessObjectStatistics stats = ...
    * int count = stats.getCompletedCount("groupBusinessObjectName", NO_FILTER_VALUE);
    * </pre>
    */
   String NO_FILTER_VALUE = null;

   /**
    * Method returns all observed group names for a given business object. If the 
    * {@link BenchmarkProcessStatisticsQuery} was created without any group business object
    * then it contains {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} as a single element. 
    * Does the result set of the query returns no business objects then 
    * <code>getGroupByValues</code> returns an empty set.
    * @return A set with all observed group names, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} if the result
    *    contains non-grouped business objects or an empty set if the statistics contains
    *    no business objects
    */
   Set<String> getGroupByValues();

   /**
    * The result set of the method contains all known business objects which have a 
    * relation to the given groupBy business object name.
    * <p>
    * If the <code>groupByValue</code> argument is either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} or 
    * <code>null</code> then it is assumed that the {@link BenchmarkProcessStatisticsQuery}
    * was used without any group business objects and therefore returns all filter 
    * business objects which have no relation resp. are not grouped to other business objects.
    * </p>
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *     or <code>null</code>
    * @return All known business objects of a given group
    */
   Set<String> getFilterValues(String groupByValue);

   /**
    * Returns a set of benchmark values which was observed during the query execution.
    * Please note that it doesn't return all possible values of a benchmark. 
    * @return Set of all observed benchmark values
    */
   Set<Integer> getRegisterdBenchmarkValues();

   /**
    * Returns the count of the aborted instances.
    * <p>
    * To get the instance count for a business object which is included within a group
    * then the arguments <code>groupByValue</code> and <code>filterValue</code> must be
    * valid business object names. If the {@link BenchmarkProcessStatisticsQuery} was 
    * executed without a groupBy business object then <code>groupByValue</code> must be 
    * either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} or <code>null</code>.
    * </p>
    * <p>
    * If you want to get the aggregated count of a group of business object then you can
    * get this if you set the <code>filterValue</code> argument either to 
    * {@link #NO_FILTER_VALUE NO_FILTER_VALUE} or <code>null</code>.
    * </p>
    * <p>
    * To get the aggregated total count of aborted instances (across all groups (if any))
    * then both arguments, <code>groupByValue</code> and <code>filterValue</code>, 
    * must be either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} resp. {@link #NO_FILTER_VALUE NO_FILTER_VALUE} or 
    * <code>null</code>. 
    * </p>
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *    or <code>null</code>
    * @param filterValue Business object name of the filter, {@link #NO_FILTER_VALUE NO_FILTER_VALUE} 
    *    or <code>null</code>
    * @return Count of the aborted instances. If the given <code>groupByValue</code>
    *    and/or <code>filterValue</code> was not found it returns 0
    */
   long getAbortedCount(String groupByValue, String filterValue);

   /**
    * Returns the count of the completed instances.
    * <p>
    * To get the instance count for a business object which is included within a group
    * then the arguments <code>groupByValue</code> and <code>filterValue</code> must be
    * valid business object names. If the {@link BenchmarkProcessStatisticsQuery} was 
    * executed without a groupBy business object then <code>groupByValue</code> must be 
    * either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} or <code>null</code>.
    * </p>
    * <p>
    * If you want to get the aggregated count of a group of business object then you can
    * get this if you set the <code>filterValue</code> argument either to 
    * {@link #NO_FILTER_VALUE NO_FILTER_VALUE} or <code>null</code>.
    * </p>
    * <p>
    * To get the aggregated total count of aborted instances (across all groups (if any))
    * then both arguments, <code>groupByValue</code> and <code>filterValue</code>, 
    * must be either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} resp. {@link #NO_FILTER_VALUE NO_FILTER_VALUE} or 
    * <code>null</code>. 
    * </p>
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *    or <code>null</code>
    * @param filterValue Business object name of the filter, {@link #NO_FILTER_VALUE NO_FILTER_VALUE} 
    *    or <code>null</code>
    * @return Count of the completed instances. If the given <code>groupByValue</code>
    *    and/or <code>filterValue</code> was not found it returns 0.
    */
   long getCompletedCount(String groupByValue, String filterValue);

   /**
    * Returns the count of a given <code>benchmarkValue</code>.
    * <p>
    * To get the instance count for a business object which is included within a group
    * then the arguments <code>groupByValue</code> and <code>filterValue</code> must be
    * valid business object names. If the {@link BenchmarkProcessStatisticsQuery} was 
    * executed without a groupBy business object then <code>groupByValue</code> must be 
    * either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} or <code>null</code>.
    * </p>
    * <p>
    * If you want to get the aggregated count of a group of business object then you can
    * get this if you set the <code>filterValue</code> argument either to 
    * {@link #NO_FILTER_VALUE NO_FILTER_VALUE} or <code>null</code>.
    * </p>
    * <p>
    * To get the aggregated total count of aborted instances (across all groups (if any))
    * then both arguments, <code>groupByValue</code> and <code>filterValue</code>, 
    * must be either {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE} resp. {@link #NO_FILTER_VALUE NO_FILTER_VALUE} or 
    * <code>null</code>. 
    * </p>
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *    or <code>null</code>
    * @param filterValue Business object name of the filter, {@link #NO_FILTER_VALUE NO_FILTER_VALUE} 
    *    or <code>null</code>
    * @param benchmarkValue Benchmark value 
    * 
    * @return Count of instances for a given benchmarkValue or 0 if no business object
    *    was found the <code>groupByValue</code>/<code>filterValue</code>/<code>benchmarkValue</code> coordinates.
    */
   long getBenchmarkCategoryCount(String groupByValue, String filterValue,
         int benchmarkValue);

   /**
    * Returns the instance OIDs (e.g process instance OIDs or activity instance OIDs)
    * of a given <code>benchmarkValue</code>.
    * The behavior of the arguments is exactly the same as for the 
    * {@link #getBenchmarkCategoryCount(groupByValue, filterValue, benchmarkValue) getBenchmarkCategoryCount}
    * method.
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *    or <code>null</code>
    * @param filterValue Business object name of the filter, {@link #NO_FILTER_VALUE NO_FILTER_VALUE} 
    *    or <code>null</code>
    * @param benchmarkValue Benchmark value
    * @return Set of all instance OIDs or an empty set if no OIDs are available for the
    *   <code>groupByValue</code>/<code>filterValue</code>/<code>benchmarkValue</code> coordinates.
    */
   Set<Long> getInstanceOIDsForBenchmarkCategory(String groupByValue, String filterValue,
         int benchmarkValue);

   /**
    * Returns the instance OIDs (e.g process instance OIDs or activity instance OIDs)
    * which are in state aborted and aborting.
    * The behavior of the arguments is exactly the same as for the 
    * {@link #getAbortedCount(groupByValue, filterValue) getAbortedCount}
    * method.
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *    or <code>null</code>
    * @param filterValue Business object name of the filter, {@link #NO_FILTER_VALUE NO_FILTER_VALUE} 
    *    or <code>null</code>
    * @return Set of all instance OIDs or an empty set if no OIDs are available for the
    *   <code>groupByValue</code>/<code>filterValue</code> coordinates.
    */
   Set<Long> getAbortedInstanceOIDs(String groupByValue, String filterValue);

   /**
    * Returns the instance OIDs (e.g process instance OIDs or activity instance OIDs)
    * which are in state completed.
    * The behavior of the arguments is exactly the same as for the 
    * {@link #getCompletedCount(groupByValue, filterValue) getCompletedCount}
    * method.
    * @param groupByValue Business object name of the group, {@link #NO_GROUPBY_VALUE NO_GROUPBY_VALUE}
    *    or <code>null</code>
    * @param filterValue Business object name of the filter, {@link #NO_FILTER_VALUE NO_FILTER_VALUE} 
    *    or <code>null</code>
    * @return Set of all instance OIDs or an empty set if no OIDs are available for the
    *   <code>groupByValue</code>/<code>filterValue</code> coordinates.
    */
   Set<Long> getCompletedInstanceOIDs(String groupByValue, String filterValue);

   long getTotalCount();
}