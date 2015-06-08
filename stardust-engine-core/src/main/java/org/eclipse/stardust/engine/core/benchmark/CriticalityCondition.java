/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class CriticalityCondition implements ConditionEvaluator
{
   protected double from;
   protected double to;

   public CriticalityCondition(long from, long to)
   {
      super();
      this.from = Double.parseDouble("0." + from);
      this.to = Double.parseDouble("0." + to);
   }

   public CriticalityCondition(double from, double to)
   {
      super();
      this.from = from;
      this.to = to;
   }

   @Override
   public Boolean evaluate(ActivityInstanceBean ai)
   {
      double criticality = ai.getCriticality();
      return (from <= criticality && to >= criticality);
   }

   @Override
   public Boolean evaluate(ProcessInstanceBean pi)
   {
      throw new UnsupportedOperationException("Criticality is only defined on ActivityInstances.");
   }

   @Override
   public String toString()
   {
      return "CriticalityCondition [from=" + from + ", to=" + to + "]";
   }

}
