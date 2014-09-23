/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsModelUtils;


/**
 * Policy that determines if process instances are considered critical if their execution
 * time exceeds a certain limit.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class CriticalExecutionTimePolicy extends AbstractCriticalDurationPolicy
{
   static final long serialVersionUID = 4088770799703127740L;
   
   public static final CriticalExecutionTimePolicy EXCEEDING_TARGET_EXECUTION_TIME = new CriticalExecutionTimePolicy(
         1.0f, 1.0f, 1.0f);

   /**
    * PIs are considered critical if their duration exceeds a certain limit. Limits can be
    * defined per priority.
    *
    * @param lowPriorityCriticalPct The percentage of the "target execution time" parameter a process with priority LOW must exceed to be considered critical.
    * @param normalPriorityCriticalPct The percentage of the "target execution time" parameter a process with priority NORMAL must exceed to be considered critical.
    * @param highPriorityCriticalPct The percentage of the "target execution time" parameter a process with priority HIGH must exceed to be considered critical.
    *
    * @return
    */
   public static CriticalExecutionTimePolicy criticalityByDuration(
         float lowPriorityCriticalPct, float normalPriorityCriticalPct,
         float highPriorityCriticalPct)
   {
      return new CriticalExecutionTimePolicy(lowPriorityCriticalPct,
            normalPriorityCriticalPct, highPriorityCriticalPct);
   }

   public CriticalExecutionTimePolicy(float lowPriorityCriticalPct,
         float normalPriorityCriticalPct, float highPriorityCriticalPct)
   {
      super(lowPriorityCriticalPct, normalPriorityCriticalPct, highPriorityCriticalPct);
   }

   protected Period getTargetDuration(ModelElement modelElement)
   {
      return StatisticsModelUtils.getTargetExecutionTime(modelElement);
   }

}
