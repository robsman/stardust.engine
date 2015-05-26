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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class CalendarUtils
{

   private static final Logger trace = LogManager.getLogger(CalendarUtils.class);

   private CalendarUtils()
   {
      // Utility class.
   }

   public static List<Date> getBlockedDays(String calendarDocumentId)
   {
      List<Date> blockedDays = Collections.emptyList();


      // TODO


      return blockedDays;
   }



}