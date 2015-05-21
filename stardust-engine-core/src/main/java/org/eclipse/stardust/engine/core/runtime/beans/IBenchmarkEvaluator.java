package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Map;

public interface IBenchmarkEvaluator
{

   public int getBenchmarkForProcessInstance(long piOid);
   
   public int getBenchmarkForActivityInstance(long aiOid, String activityId);
   
}
