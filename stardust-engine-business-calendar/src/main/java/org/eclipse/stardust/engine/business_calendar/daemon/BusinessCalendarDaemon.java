/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.business_calendar.daemon;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IDaemon;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

public class BusinessCalendarDaemon implements IDaemon
{
   public static final String BUSINESS_CALENDAR_DAEMON = "business_calendar.daemon";

   @Override
   public ExecutionResult execute(long batchSize)
   {
      ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance()
            .get(EngineProperties.FORKING_SERVICE_HOME);
      final IJobManager jobManager = factory.getJobManager();
      try
      {
         jobManager.performSynchronousJob(new BusinessCalendarJob());
      }
      finally
      {
         factory.release(jobManager);
      }

      return ExecutionResult.ER_WORK_DONE;
   }

   @Override
   public String getType()
   {
      return BUSINESS_CALENDAR_DAEMON;
   }

   public static final class Factory implements IDaemon.Factory
   {
      private static final Collection<IDaemon> businessCalendarDaemon = Collections.<IDaemon>singletonList(new BusinessCalendarDaemon());

      @Override
      public Collection<IDaemon> getDaemons()
      {
         return businessCalendarDaemon;
      }
   }

   @Override
   public long getDefaultPeriodicity()
   {
      return 60000;
   }
}