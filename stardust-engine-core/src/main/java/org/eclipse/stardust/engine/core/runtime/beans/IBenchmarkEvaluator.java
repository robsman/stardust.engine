package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Map;

public interface IBenchmarkEvaluator
{

   public int getBenchmarkForProcessInstance();
   
   public Map getBenchmarkForActivityInstances();
   
}
