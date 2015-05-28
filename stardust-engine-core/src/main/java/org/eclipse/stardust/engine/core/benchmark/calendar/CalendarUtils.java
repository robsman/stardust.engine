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
package org.eclipse.stardust.engine.core.benchmark.calendar;

import java.util.Date;

public class CalendarUtils
{

   private CalendarUtils()
   {
      // Utility class.
   }

   public static boolean isBlocked(Date date, String calendarDocumentId)
   {
      TimeOffCalendarFinder timeOffCalendarFinder = new TimeOffCalendarFinder(date,
            calendarDocumentId);

      // TODO caching

      return timeOffCalendarFinder.isBlocked();
   }

}