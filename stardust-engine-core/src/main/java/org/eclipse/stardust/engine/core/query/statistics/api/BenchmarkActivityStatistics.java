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
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public abstract class BenchmarkActivityStatistics extends CustomActivityInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected Map<Long, BenchmarkResults> benchmarkResultPerBenchmarkOid;

   protected BenchmarkActivityStatistics(BenchmarkActivityStatisticsQuery query)
   {
      super(query);

      this.benchmarkResultPerBenchmarkOid = CollectionUtils.newMap();
   }

   public BenchmarkResults getStatisticsForBenchmark(long benchmarkOid)
   {
      return benchmarkResultPerBenchmarkOid.get(benchmarkOid);
   }

   public Map<Long, BenchmarkResults> getBenchmarkResults()
   {
      return Collections.unmodifiableMap(benchmarkResultPerBenchmarkOid);
   }
}
