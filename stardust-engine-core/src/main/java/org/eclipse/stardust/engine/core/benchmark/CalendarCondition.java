/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import java.util.Date;

import org.eclipse.stardust.engine.core.benchmark.calendar.CalendarUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class CalendarCondition extends DateCondition
{
   protected String calendarDocumentId;

   public CalendarCondition(String calendarDocumentId, Comperator comperator, String qualifiedDataId)
   {
      super(comperator, qualifiedDataId);
      this.calendarDocumentId = calendarDocumentId;
   }

   @Override
   public Boolean evaluate(ActivityInstanceBean ai)
   {
      Boolean result = super.evaluate(ai);

      result &= !isBlockedDay();

      return result;
   }

   @Override
   public Boolean evaluate(ProcessInstanceBean pi)
   {
      Boolean result = super.evaluate(pi);

      result &= !isBlockedDay();

      return result;
   }

   private boolean isBlockedDay()
   {
      return CalendarUtils.isBlocked(new Date(), calendarDocumentId);
   }
}
