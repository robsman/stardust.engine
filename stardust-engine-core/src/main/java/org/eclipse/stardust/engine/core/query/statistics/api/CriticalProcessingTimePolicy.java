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
 * Policy that determines if process instances are considered critical if their processing
 * time exceeds a certain limit.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class CriticalProcessingTimePolicy extends AbstractStoplightDurationPolicy
{
   static final long serialVersionUID = 7109599016470640391L;
   
   public static final CriticalProcessingTimePolicy EXCEEDING_TARGET_PROCESSING_TIME = new CriticalProcessingTimePolicy(
         1.0f, 1.0f);

   /**
    * PIs are considered critical if their processing exceeds a certain limit. Limits can be
    * defined per priority.
    *
    * @param lowPriorityCriticalPct The percentage of the "target processing time" parameter a process with priority LOW must exceed to be considered critical.
    * @param normalPriorityCriticalPct The percentage of the "target processing time" parameter a process with priority NORMAL must exceed to be considered critical.
    * @param highPriorityCriticalPct The percentage of the "target processing time" parameter a process with priority HIGH must exceed to be considered critical.
    *
    * @return
    */
   public static CriticalProcessingTimePolicy criticalityByDuration(float yellowPct,
         float redPct)
   {
      return new CriticalProcessingTimePolicy(yellowPct, redPct);
   }

   public CriticalProcessingTimePolicy(float yellowPct, float redPct)
   {
      super(yellowPct, redPct);
   }

   protected Period getTargetDuration(ModelElement modelElement)
   {
      return StatisticsModelUtils.getTargetProcessingTime(modelElement);
   }

}
