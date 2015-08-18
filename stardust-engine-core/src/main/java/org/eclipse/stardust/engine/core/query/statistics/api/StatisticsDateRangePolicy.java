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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.engine.api.query.EvaluationPolicy;

/**
 * Allows to specify a list of date ranges to select multiple intervals for query evaluation.
 *
 * @author Roland.Stamm
 *
 */
public class StatisticsDateRangePolicy implements EvaluationPolicy
{

   private Set<DateRange> dateRanges;

   private static final long serialVersionUID = 1L;

   /**
    * Defaults to {@link DateRange#TODAY}
    */
   public StatisticsDateRangePolicy()
   {
   }

   /**
    * @param dateRanges A list of date ranges to select multiple intervals for query evaluation.
    */
   public StatisticsDateRangePolicy(List<DateRange> dateRanges)
   {
      if (dateRanges == null)
      {
         this.dateRanges = null;
      }
      else
      {
         this.dateRanges = new LinkedHashSet(dateRanges);
      }
   }

   public Set<DateRange> getDateRanges()
   {
      return dateRanges;
   }

}
