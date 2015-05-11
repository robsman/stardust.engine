package org.eclipse.stardust.engine.core.runtime.beans;

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
