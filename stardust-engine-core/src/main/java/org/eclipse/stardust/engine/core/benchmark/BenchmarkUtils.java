package org.eclipse.stardust.engine.core.benchmark;

import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

public class BenchmarkUtils
{

   public static boolean isBenchmarkedPI(IProcessInstance pi)
   {
      if (pi.getBenchmark() > 0)
      {
         return true;
      }
      return false;
   }
   
}
