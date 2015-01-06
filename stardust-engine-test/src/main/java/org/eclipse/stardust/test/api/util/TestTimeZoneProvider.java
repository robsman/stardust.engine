package org.eclipse.stardust.test.api.util;

import java.util.TimeZone;

import org.eclipse.stardust.common.config.TimeZoneProvider;

public class TestTimeZoneProvider implements TimeZoneProvider
{
   public static final TimeZone FIXED_TEST_TIME_ZONE = TimeZone.getTimeZone("Europe/Berlin");

   @Override
   public TimeZone getTimeZone()
   {
      return FIXED_TEST_TIME_ZONE;
   }
}
