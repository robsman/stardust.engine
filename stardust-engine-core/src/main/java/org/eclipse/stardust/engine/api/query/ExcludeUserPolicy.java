package org.eclipse.stardust.engine.api.query;

/**
 * Policy to exclude AI´s from excluded users in the result.
 * 
 * @see ExcludeUserPolicy#EXCLUDE_USER
 */
public class ExcludeUserPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;

   /**
    * This policy excluded activity instances in activity instance queries 
    * where the user is an excluded user for that activity instance.
    */
   public static final ExcludeUserPolicy EXCLUDE_USER = new ExcludeUserPolicy();

   private ExcludeUserPolicy()
   {
   }
}