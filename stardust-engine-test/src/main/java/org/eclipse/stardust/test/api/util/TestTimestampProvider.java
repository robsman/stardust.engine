/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.stardust.common.config.TimestampProvider;

/**
 * <p>
 * A {@link TimestampProvider} implementation allowing for returning the same timestamp regardless
 * of the test case's execution time.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TestTimestampProvider implements TimestampProvider
{
   private final Calendar cal;

   public TestTimestampProvider()
   {
      /* initialize with a well-defined date such that test case results do not depend on the actual */
      /* time they are executed, e.g. when calculating the duration of not yet completed PIs         */
      try
      {
         final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
         dateFormat.setTimeZone(TestTimeZoneProvider.FIXED_TEST_TIME_ZONE);
         final Date date = dateFormat.parse("1.1.2080");
         cal = Calendar.getInstance();
         cal.setTime(date);
      }
      catch (final ParseException e)
      {
         throw new RuntimeException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.common.config.TimestampProvider#getTimestamp()
    */
   @Override
   public Date getTimestamp()
   {
      return cal.getTime();
   }

   /**
    * <p>
    * Move the timestamp held by this {@link TimestampProvider} instance to the next year, e.g. 1/1/2080 -> 1/1/2081.
    * </p>
    */
   public void nextYear()
   {
      cal.add(Calendar.YEAR, 1);
   }
   
   /**
    * <p>
    * Move the timestamp held by this {@link TimestampProvider} instance to the next hour.
    * </p>
    */
   public void nextHour()
   {
      cal.add(Calendar.HOUR_OF_DAY, 1);
   }
}
