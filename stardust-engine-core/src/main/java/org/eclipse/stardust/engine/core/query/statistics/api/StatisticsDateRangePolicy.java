/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.List;

import org.eclipse.stardust.engine.api.query.EvaluationPolicy;

public class StatisticsDateRangePolicy implements EvaluationPolicy
{

   private List<DateRange> dateRanges;

   private static final long serialVersionUID = 1L;

   public StatisticsDateRangePolicy()
   {
   }

   public StatisticsDateRangePolicy(List<DateRange> dateRanges)
   {
      this.dateRanges = dateRanges;
   }

   public List<DateRange> getDateRanges()
   {
      return dateRanges;
   }

}
