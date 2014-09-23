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

import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsModelUtils;


/**
 * Policy that determines if process instances are considered critical if their execution
 * costs exceeds a certain limit.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class CriticalCostPerExecutionPolicy extends AbstractStoplightCostPolicy
{
   static final long serialVersionUID = 4217265658852643759L;
   
   public static final CriticalCostPerExecutionPolicy EXCEEDING_TARGET_COST_PER_EXECUTION = new CriticalCostPerExecutionPolicy(
         1.0f, 1.0f);

   /**
    * PIs are considered critical if their execution costs exceeds a certain limit. Limits
    * can be defined per priority.
    *
    * @param yellowPct
    *           The percentage of the "target cost per execution" parameter a process with
    *           priority LOW must exceed to be considered critical.
    * @param redPct
    *           The percentage of the "target processing time" parameter a process with
    *           priority HIGH must exceed to be considered critical.
    *
    * @return
    */
   public static CriticalCostPerExecutionPolicy criticalityByCost(float yellowPct,
         float redPct)
   {
      return new CriticalCostPerExecutionPolicy(yellowPct, redPct);
   }

   public CriticalCostPerExecutionPolicy(float yellowPct, float redPct)
   {
      super(yellowPct, redPct);
   }

   protected Number getTargetCost(ModelElement modelElement)
   {
      return StatisticsModelUtils.getTargetCostPerExecution(modelElement);
   }

}
