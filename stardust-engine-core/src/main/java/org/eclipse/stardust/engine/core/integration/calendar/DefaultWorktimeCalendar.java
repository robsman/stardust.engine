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
package org.eclipse.stardust.engine.core.integration.calendar;

import java.util.Date;

/**
 * @author rsauer
 * @version $Revision$
 */
public class DefaultWorktimeCalendar implements IWorktimeCalendar
{
   //private static final Logger trace = LogManager.getLogger(DefaultWorktimeCalendar.class);
   
   //private static final String PARAM_BEGIN_OF_WORKDAY = "Carnot.DefaultCalendar.BeginOfWorkday"; 

   //private static final String PARAM_END_OF_WORKDAY = "Carnot.DefaultCalendar.EndOfWorkday";
   
/* TODO This field should be either in the calculateWorktime method or in a PropertyLayer because it is not thread-safe
   If we use it in this way a NumberFormatException can occurr if we invoke the SimpleDateFormat.parse
   method later in the method 
   private final DateFormat timeParser = new SimpleDateFormat("HH:mm");
*/ 
   public long calculateWorktime(Date start, Date end, String performerId)
   {
/*
      final Parameters params = Parameters.instance();

      Calendar beginOfDay = Calendar.getInstance();
      try
      {
         beginOfDay.setTime(timeParser.parse(params.getString(PARAM_BEGIN_OF_WORKDAY, "8:00")));
      }
      catch (ParseException e)
      {
         trace.warn("Failed parsing begin of workday, using default of 8:00");
         beginOfDay.setTimeInMillis(0l);
         beginOfDay.set(Calendar.HOUR_OF_DAY, 8);
      }
      
      Calendar endOfDay = Calendar.getInstance();
      try
      {
         endOfDay.setTime(timeParser.parse(params.getString(PARAM_END_OF_WORKDAY, "16:00")));
      }
      catch (ParseException e)
      {
         trace.warn("Failed parsing end of workday, using default of 16:00");
         beginOfDay.setTimeInMillis(0l);
         beginOfDay.set(Calendar.HOUR_OF_DAY, 16);
      }
*/
      // TODO base on working hours per day
      return end.getTime() - start.getTime();
   }
}
