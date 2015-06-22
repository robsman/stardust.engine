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

import org.eclipse.stardust.engine.core.query.statistics.api.*;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkActivityStatisticsResult extends BenchmarkActivityStatistics
{
   static final long serialVersionUID = -6036388240512274629L;

   public BenchmarkActivityStatisticsResult(BenchmarkActivityStatisticsQuery asq)
   {
      super(asq);
   }

   public void addBenchmarkedInstances(String processId, String activityId, long aiOid, long benchmarkOid, int benchmarkValue)
   {
      BenchmarkResults benchmarkedInstance = benchmarkResultPerBenchmarkOid.get(benchmarkOid);
      if (null == benchmarkedInstance)
      {
         benchmarkedInstance = new BenchmarkResults();
         benchmarkResultPerBenchmarkOid.put(benchmarkOid, benchmarkedInstance);
      }

      benchmarkedInstance.registerActivity(processId, activityId, benchmarkValue);
   }
}
