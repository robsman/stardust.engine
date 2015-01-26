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

import org.eclipse.stardust.engine.api.query.EvaluationPolicy;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractCriticalityPolicy implements EvaluationPolicy
{

   private final float lowPriorityCriticalPct;

   private final float normalPriorityCriticalPct;

   private final float highPriorityCriticalPct;

   public AbstractCriticalityPolicy(float lowPriorityCriticalPct,
         float normalPriorityCriticalPct, float highPriorityCriticalPct)
   {
      this.lowPriorityCriticalPct = lowPriorityCriticalPct;
      this.normalPriorityCriticalPct = normalPriorityCriticalPct;
      this.highPriorityCriticalPct = highPriorityCriticalPct;
   }

   public float getCriticalityFactor(ProcessInstancePriority priority)
   {
      switch (priority.getValue())
      {
      case ProcessInstancePriority.LOW:
         return lowPriorityCriticalPct;

      case ProcessInstancePriority.HIGH:
         return highPriorityCriticalPct;

      default:
         return normalPriorityCriticalPct;
      }
   }

}
