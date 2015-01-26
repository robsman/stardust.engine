/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

public class ExtendedAccessPathEvaluatorRegistry
{
   Map<EvaluatorKey, Class<? extends ExtendedAccessPathEvaluator>> registry
      = new HashMap<EvaluatorKey, Class<? extends ExtendedAccessPathEvaluator>>();
   
   public void register(AccessPoint accessPoint, String accessPath, Class<? extends ExtendedAccessPathEvaluator> evaluatorClass)
   {
      EvaluatorKey evaluatorKey = new EvaluatorKey(accessPoint, accessPath);
      registry.put(evaluatorKey, evaluatorClass);
   }
   
   public Class<? extends ExtendedAccessPathEvaluator> getEvaluatorClass(AccessPoint accessPoint, String accessPath)
   {
      EvaluatorKey evaluatorKey = new EvaluatorKey(accessPoint, accessPath);
      return registry.get(evaluatorKey);
   }
   
   public boolean hasEvaluatorClass(AccessPoint accessPoint, String accessPath)
   {
      EvaluatorKey evaluatorKey = new EvaluatorKey(accessPoint, accessPath);
      return registry.containsKey(evaluatorKey);
   }
   
   private class EvaluatorKey
   {      
      private final AccessPoint accessPoint;
      private final String accessPath;

      public EvaluatorKey(AccessPoint accessPoint, String accessPath)
      {
         this.accessPoint = accessPoint;
         this.accessPath = accessPath;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + getOuterType().hashCode();
         result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
         result = prime * result + ((accessPoint == null) ? 0 : accessPoint.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         EvaluatorKey other = (EvaluatorKey) obj;
         if (!getOuterType().equals(other.getOuterType()))
            return false;
         if (accessPath == null)
         {
            if (other.accessPath != null)
               return false;
         }
         else if (!accessPath.equals(other.accessPath))
            return false;
         if (accessPoint == null)
         {
            if (other.accessPoint != null)
               return false;
         }
         else if (!accessPoint.equals(other.accessPoint))
            return false;
         return true;
      }

      private ExtendedAccessPathEvaluatorRegistry getOuterType()
      {
         return ExtendedAccessPathEvaluatorRegistry.this;
      }
   }
}
