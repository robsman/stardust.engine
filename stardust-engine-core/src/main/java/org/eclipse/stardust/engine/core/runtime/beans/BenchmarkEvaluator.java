package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;

public class BenchmarkEvaluator implements IBenchmarkEvaluator
{

   private static final Logger trace = LogManager.getLogger(CriticalityEvaluator.class);

   private IProcessInstance processInstance;

   private BenchmarkDefinition benchmark;

   public BenchmarkEvaluator(IProcessInstance pi)
   {
      this.processInstance = pi;

      // Retrieving benchmark from cache (currently mocked)
      benchmark = new BenchmarkDefinition();
   }

   @Override
   public int getBenchmarkForProcessInstance()
   {
      trace.info("PI <" + processInstance.getOID()
            + "> is associated with Benchmark OID <" + processInstance.getBenchmark()
            + ">");

      // Evaluating Bechmark for PI

      return 0;
   }

   @Override
   public Map getBenchmarkForActivityInstances()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(processInstance.getOID());
      query.getFilter().add(ActivityStateFilter.ALIVE);

      ActivityInstanceQueryEvaluator queryEvaluator = new ActivityInstanceQueryEvaluator(
            query, QueryServiceUtils.getDefaultEvaluationContext());

      ResultIterator<IActivityInstance> rawResult = queryEvaluator.executeFetch();

      return null;
   }

   private List findActiveAisForBechmarkPI(IProcessInstance pi)
   {
      return null;

   }
}
