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

public interface BenchmarkBusinessObjectStatistics extends Serializable
{
   String NO_GROUPBY_VALUE = XMLConstants.NULL_NS_URI;

   String NO_FILTER_VALUE = null;

   Set<String> getGroupByValues();

   Set<String> getFilterValues(String groupByValue);

   Set<Integer> getRegisterdBenchmarkValues();

   long getAbortedCount(String groupByValue, String filterValue);

   long getCompletedCount(String groupByValue, String filterValue);

   long getBenchmarkCategoryCount(String groupByValue, String filterValue,
         int benchmarkValue);

   Set<Long> getInstanceOIDsForBenchmarkCategory(String groupByValue, String filterValue,
         int benchmarkValue);

   Set<Long> getAbortedInstanceOIDs(String groupByValue, String filterValue);

   Set<Long> getCompletedInstanceOIDs(String groupByValue, String filterValue);

   long getTotalCount();
}