package org.eclipse.stardust.engine.api.query;

/**
 * Policy to include cases in the result.
 * 
 * @see CasePolicy#INCLUDE_CASES
 */
public class CasePolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;

   /**
    * This policy additionally includes case process instances of which the process instances
    * found by the query itself are members.
    */
   public static final CasePolicy INCLUDE_CASES = new CasePolicy();

   private CasePolicy()
   {
   }

}
