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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Collections;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.runtime.TimeoutException;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.extensions.jms.app.RecordedTimestampProvider;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public final class ActivityThreadCarrier extends ActionCarrier
{
   public static final Logger trace = LogManager.getLogger(ActivityThreadCarrier.class);

   public static final String PROCESS_INSTANCE_OID_TAG = "processInstanceOID";
   public static final String ACTIVITY_OID_TAG = "activityOID";
   public static final String ACTIVITY_INSTANCE_OID_TAG = "activityInstanceOID";
   public static final String TIMEOUT_NOTIFICATION = "timeoutNotification";

   private long processInstanceOID;
   private long activityOID;
   private long activityInstanceOID;
   private boolean timeout;
   private Map data = Collections.EMPTY_MAP;

   private long eventTime;

   public ActivityThreadCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }

   public void setProcessInstance(IProcessInstance processInstance)
   {
      if (processInstance != null)
      {
         processInstanceOID = processInstance.getOID();
      }
      else
      {
         processInstanceOID = 0;
      }
   }

   public void setActivity(IActivity activity)
   {
      if (activity != null)
      {
         activityOID = activity.getOID();
      }
      else
      {
         activityOID = 0;
      }
   }

   public void setActivityInstance(IActivityInstance activityInstance)
   {
      if (activityInstance != null)
      {
         activityInstanceOID = activityInstance.getOID();
      }
      else
      {
         activityInstanceOID = 0;
      }
   }

   public void setTimeout(Throwable timeout)
   {
      this.timeout = (timeout != null);
   }

   public Action doCreateAction()
   {
      trace.debug("activityinstance: " + activityInstanceOID);
      trace.debug("processinstance : " + processInstanceOID);

      TimeoutException timeoutException = null;
      if (timeout)
      {
         timeoutException = new TimeoutException("");
      }
      
      return new ActivityThreadRunner(this, timeoutException, eventTime);
   }

   public void setData(Map data)
   {
      this.data = data;
   }

   public String toString()
   {
      return "Activity thread carrier: pi = " + processInstanceOID
            + ", ai = " + activityInstanceOID + ", a= " + activityOID;
   }
   
   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         mapMessage.setLong(PROCESS_INSTANCE_OID_TAG, processInstanceOID);
         mapMessage.setLong(ACTIVITY_INSTANCE_OID_TAG, activityInstanceOID);
         mapMessage.setLong(ACTIVITY_OID_TAG, activityOID);
         mapMessage.setBoolean(TIMEOUT_NOTIFICATION, timeout);
      
         if (Parameters.instance().getBoolean(
               KernelTweakingProperties.EVENT_TIME_OVERRIDABLE, false)
               && (TimestampProviderUtils.getProvider() instanceof RecordedTimestampProvider))
         {
            message.setLongProperty(RecordedTimestampProvider.PROP_EVENT_TIME,
                  TimestampProviderUtils.getTimeStamp().getTime());
         }
      }
   }
   
   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         processInstanceOID = mapMessage.getLong(PROCESS_INSTANCE_OID_TAG);
         activityInstanceOID = mapMessage.getLong(ACTIVITY_INSTANCE_OID_TAG);
         activityOID = mapMessage.getLong(ACTIVITY_OID_TAG);
         timeout = mapMessage.getBoolean(TIMEOUT_NOTIFICATION);
      
         this.eventTime = -1;

         if (Parameters.instance().getBoolean(
               KernelTweakingProperties.EVENT_TIME_OVERRIDABLE, false))
         {
            try
            {
               if (message.propertyExists(RecordedTimestampProvider.PROP_EVENT_TIME))
               {
                  this.eventTime = message.getLongProperty(RecordedTimestampProvider.PROP_EVENT_TIME);
               }
            }
            catch (JMSException e)
            {
            }
         }
      }
   }

   // @todo (france, ub): beschissen
   class ActivityThreadRunner extends SecurityContextAwareAction
   {
      private Map data;
      private long processInstanceOID;
      private long activityOID;
      private long activityInstanceOID;

      private TimeoutException timeoutException;

      private long eventTime;

      public ActivityThreadRunner(ActivityThreadCarrier carrier,
            TimeoutException timeoutException,
            long eventTime)
      {
         super(carrier);
         
         this.processInstanceOID = carrier.processInstanceOID;
         this.activityOID = carrier.activityOID;
         this.activityInstanceOID = carrier.activityInstanceOID;
         
         this.timeoutException = timeoutException;
         this.data = carrier.data;
         
         this.eventTime = eventTime;
      }

      public Object execute()
      {
         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

         getLocalParameters(rtEnv);

         IProcessInstance processInstance = null;
         IActivity activity = null;
         IActivityInstance activityInstance = null;

         if (0 != activityInstanceOID)
         {
            activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);
         }

         else
         {
            processInstance = ProcessInstanceBean.findByOID(processInstanceOID);
            activity = (IActivity) ModelManagerFactory.getCurrent()
                  .lookupObjectByOID(activityOID);
         }

         ActivityThread at = new ActivityThread(processInstance, activity,
               activityInstance, timeoutException, data, false);

         at.run();

         return null;
      }

      public String toString()
      {
         return "Activity thread: pi = " + processInstanceOID + ", ai = "
               + activityInstanceOID + ", a = " + activityOID;
      }
      
      private void getLocalParameters(BpmRuntimeEnvironment rtEnv)
      {
         // optionally taking timestamp override into account
         boolean recordedEventTime = Parameters.instance().getBoolean(
               KernelTweakingProperties.EVENT_TIME_OVERRIDABLE, false);
         
         if (recordedEventTime)
         {
            if (-1 != eventTime)
            {
               rtEnv.setTimestampProvider(new RecordedTimestampProvider(eventTime));
            }
         }
      }
   }
}
