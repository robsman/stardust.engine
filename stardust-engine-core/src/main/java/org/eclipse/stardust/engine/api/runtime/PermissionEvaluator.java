package org.eclipse.stardust.engine.api.runtime;

public abstract class PermissionEvaluator
{     
   protected String[] assumptions;
   
   public PermissionEvaluator(String [] assumptions)
   {
      super();
      this.assumptions = assumptions;
   }
   
   public abstract boolean isAllowed(Object object);
}
