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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.ICallable;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.extensions.dms.data.TransientUser;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class BusinessCalendarJob implements ICallable<Date>
{
   public static final Logger daemonLogger = RuntimeLog.DAEMON;

   @Override
   public Date call() throws PublicException
   {
      Date now = new Date();
      daemonLogger.info("Executing calendar daemon at: " + TimestampProviderUtils.getTimeStamp());

      @SuppressWarnings("unchecked")
      Iterator<AuditTrailPartitionBean> partitionsIterator = AuditTrailPartitionBean.findAll();
      while (partitionsIterator.hasNext())
      {
         IAuditTrailPartition partition = partitionsIterator.next();

         Map<String, Object> props = CollectionUtils.newHashMap();
         props.put(SecurityProperties.CURRENT_USER, TransientUser.getInstance());
         if (partition != null)
         {
            props.put(SecurityProperties.CURRENT_PARTITION, partition);
         }
         PropertyLayer layer = ParametersFacade.pushLayer(props);

         try
         {
            ScheduledCalendarFinder documentFinder = new ScheduledCalendarFinder(now);
            for (ScheduledCalendar scheduledCalendar : documentFinder.readAllDefinitions())
            {
               scheduledCalendar.execute();
            }
         }
         finally
         {
            if (layer != null)
            {
               ParametersFacade.popLayer();
            }
         }
      }

      return now;
   }
}
