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

import org.eclipse.stardust.common.config.FactoryFinder;
import org.eclipse.stardust.common.config.Parameters;

/**
 * @author rsauer
 * @version $Revision$
 */
public class WorktimeCalendarUtils
{

   private static final String CACHED_INSTANCE = WorktimeCalendarUtils.class.getName() + ".Instance";

   public static IWorktimeCalendar getWorktimeCalendar()
   {
      final Parameters params = Parameters.instance();

      IWorktimeCalendar calendar = (IWorktimeCalendar) params.get(CACHED_INSTANCE);
      
      if (null == calendar)
      {
         IWorktimeCalendarFactory factory = (IWorktimeCalendarFactory) FactoryFinder.findFactory(
               IWorktimeCalendarFactory.class, DefaultWorktimeCalendarFactory.class, null);

         calendar = factory.getWorktimeCalendar();

         params.set(CACHED_INSTANCE, calendar);
      }

      return calendar;
   }

   public static class DefaultWorktimeCalendarFactory implements IWorktimeCalendarFactory
   {

      public IWorktimeCalendar getWorktimeCalendar()
      {
         return new DefaultWorktimeCalendar();
      }

   }
}
