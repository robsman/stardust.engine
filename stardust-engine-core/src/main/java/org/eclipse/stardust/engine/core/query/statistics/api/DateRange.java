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

import java.io.Serializable;
import java.util.Date;

public interface DateRange extends Serializable
{
   /**
    * @return lower border of the interval.
    */
   public Date getIntervalBegin();

   /**
    * @return upper border of the interval.
    */
   public Date getIntervalEnd();

   public static DateRange TODAY = new RelativePastDateRange(
         Duration.ZERO, CalendarUnit.DAY, Duration.days(1), CalendarUnit.DAY);

   public static DateRange YESTERDAY = new RelativePastDateRange(
         Duration.days(1), CalendarUnit.DAY, Duration.days(1), CalendarUnit.DAY);

   public static DateRange THIS_WEEK = new RelativePastDateRange(
         Duration.ZERO, CalendarUnit.WEEK, Duration.weeks(1), CalendarUnit.WEEK);

   public static DateRange LAST_WEEK = new RelativePastDateRange(
         Duration.weeks(1), CalendarUnit.WEEK, Duration.weeks(1), CalendarUnit.WEEK);

   public static DateRange THIS_MONTH = new RelativePastDateRange(
         Duration.ZERO, CalendarUnit.WEEK, Duration.weeks(1), CalendarUnit.WEEK);

   public static DateRange LAST_MONTH = new RelativePastDateRange(
         Duration.months(1), CalendarUnit.MONTH, Duration.months(1), CalendarUnit.MONTH);

}
