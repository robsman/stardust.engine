package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetailsLevel;

/**
 * Evaluation policy affecting the level of detail for process definitions.
 *
 * @author Roland.Stamm
 */
public class ProcessDefinitionDetailsPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 7425782529833869050L;

   ProcessDefinitionDetailsLevel detailsLevel;

   public ProcessDefinitionDetailsPolicy(ProcessDefinitionDetailsLevel detailsLevel)
   {
      this.detailsLevel = detailsLevel;
   }

   /**
    * @return The requested level of detail.
    */
   public ProcessDefinitionDetailsLevel getDetailsLevel()
   {
      return detailsLevel;
   }

}
