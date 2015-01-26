/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import java.util.TimeZone;

import org.eclipse.stardust.common.config.TimeZoneProvider;

/**
 * <p>
 * A {@link TimeZoneProvider} implementation allowing for returning a particular time zone regardless
 * of the test case's actual time zone.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TestTimeZoneProvider implements TimeZoneProvider
{
   public static final TimeZone FIXED_TEST_TIME_ZONE = TimeZone.getTimeZone("Europe/Berlin");

   @Override
   public TimeZone getTimeZone()
   {
      return FIXED_TEST_TIME_ZONE;
   }
}
