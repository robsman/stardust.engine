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
package org.eclipse.stardust.engine.core.extensions.conditions.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.EventBindingBean;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DefaultEventBinder;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TimeStampBinder extends DefaultEventBinder
{
   private static final Logger trace = LogManager.getLogger(TimeStampBinder.class);

   // circumventing lack of bind/unbind event init protocol
   public static long findTargetTimestamp(int objectType, long oid, IEventHandler handler)
   {
      EventBindingBean binding = EventBindingBean.find(objectType, oid, handler,
            SecurityProperties.getPartitionOid());
      return (null != binding) ? binding.getTargetStamp() : 0;
   }
   
   public void bind(int objectType, long oid, IEventHandler handler, Map attributes)
   {
      EventBindingBean binding = EventBindingBean.find(objectType, oid, handler,
            SecurityProperties.getPartitionOid());
      if (binding == null)
      {
         long targetStamp = getTargetTimestamp(objectType, oid, attributes);
         if (targetStamp == Unknown.LONG)
         {
            Object target = objectType == Event.ACTIVITY_INSTANCE ?
                     (Object) ActivityInstanceBean.findByOID(oid) :
                     ProcessInstanceBean.findByOID(oid);
            String message = "Could not perform binding for timer event of " + target;
            trace.error(message);
            AuditTrailLogger.getInstance(LogCode.EVENT, target).error(message);
         }
         else
         {
            // setting timestamp before clustering the instance will prevent one unneeded
            // SQL UPDATE call
            binding = new EventBindingBean(objectType, oid, handler, targetStamp,
                  SecurityProperties.getPartitionOid());
         }
      }
   }

   private static long getTargetTimestamp(int objectType, long oid, Map attributes) throws ObjectNotFoundException
   {
      Boolean ud = (Boolean) attributes
            .get(PredefinedConstants.TIMER_CONDITION_USE_DATA_ATT);
      Long tt = (Long) attributes.get(PredefinedConstants.TARGET_TIMESTAMP_ATT);

      long targetStamp = Unknown.LONG;
      // if there is a timestamp attribute it has precedence
      if (tt != null)
      {
         targetStamp = tt.longValue();
      }
      else if (ud != null && ud.booleanValue())
      {
         IProcessInstance pi;
         if (objectType == Event.ACTIVITY_INSTANCE)
         {
            pi = ActivityInstanceBean.findByOID(oid).getProcessInstance();
         }
         else
         {
            pi = ProcessInstanceBean.findByOID(oid);
         }
         Object result = pi.getInDataValue(ModelUtils.getData(pi
               .getProcessDefinition(), (String) attributes
               .get(PredefinedConstants.TIMER_CONDITION_DATA_ATT)), (String) attributes
               .get(PredefinedConstants.TIMER_CONDITION_DATA_PATH_ATT));
         if (result instanceof Date)
         {
            targetStamp = ((Date) result).getTime();
         }
         else if (result instanceof Long)
         {
            targetStamp = ((Long) result).longValue();
         }
         else if (result instanceof Period)
         {
            targetStamp =
                  ((Period) result).add(Calendar.getInstance()).getTime().getTime();
         }
      }
      else
      {
         Period period = (Period) attributes.get(PredefinedConstants.TIMER_PERIOD_ATT);
         if (period != null)
         {
            Calendar now = Calendar.getInstance();
            now.setTime(TimestampProviderUtils.getTimeStamp());
            targetStamp = period.add(now).getTime().getTime();
         }
      }
      return targetStamp;
   }
}
