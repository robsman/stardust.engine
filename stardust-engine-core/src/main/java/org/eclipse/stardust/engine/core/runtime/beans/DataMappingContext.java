package org.eclipse.stardust.engine.core.runtime.beans;

public class DataMappingContext
{
   public IActivityInstance ai;

   public DataMappingContext(IActivityInstance ai)
   {
      this.ai = ai;      
   }
   
   public IActivityInstance getActivityInstance()
   {
      return this.ai;
   }
}