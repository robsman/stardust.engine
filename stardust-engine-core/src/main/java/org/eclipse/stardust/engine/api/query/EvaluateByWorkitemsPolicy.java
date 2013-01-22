package org.eclipse.stardust.engine.api.query;

/**
 * Policy to advise QueryService to retrieve activtiy instance from Workitems only
 * 
 * @see EvaluateByWorkitemsPolicy#WORKITEMS
 * 
 * @author Thomas.Wolfra
 */
public class EvaluateByWorkitemsPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;
 
   /**
    * This policy retrieves activity instances based on the workitem table
    * which contains only manual/interactive activity instances.
    * This policy is applicable for the getActivtyInstanceCount and
    * getAllActivityInstances Query.
    */
   public static final EvaluateByWorkitemsPolicy WORKITEMS = new EvaluateByWorkitemsPolicy();
   
   private EvaluateByWorkitemsPolicy()
   {
      
   }
}
