/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkResults;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsResult extends BenchmarkProcessStatistics
{
   static final long serialVersionUID = -5825543169280211775L;

   public BenchmarkProcessStatisticsResult(BenchmarkProcessStatisticsQuery query)
   {
      super(query);
   }

   public void addBenchmarkedInstances(String processId, long benchmarkOid, int benchmarkValue)
   {
      BenchmarkResults benchmarkedInstance = benchmarkResultPerBenchmarkOid.get(benchmarkOid);
      if (null == benchmarkedInstance)
      {
         benchmarkedInstance = new BenchmarkResults();
         benchmarkResultPerBenchmarkOid.put(benchmarkOid, benchmarkedInstance);
      }

      benchmarkedInstance.registerProcess(processId, benchmarkValue);
   }
}
