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
package org.eclipse.stardust.engine.core.runtime.beans.daemons;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonCarrier extends ActionCarrier
{
   private static final long serialVersionUID = 1L;
   
   public static final String DAEMON_TYPE_TAG = "daemonType";
   public static final String START_TIME_STAMP_TAG = "startTimeStamp";

   private String type;
   private long startTimeStamp;

   private DaemonCarrier()
   {
      super(DAEMON_MESSAGE_TYPE_ID);
   }

   public DaemonCarrier(String type)
   {
      super(DAEMON_MESSAGE_TYPE_ID, true);
      this.type = type;
   }

   public String getType()
   {
      return type;
   }

   public long getStartTimeStamp()
   {
      return startTimeStamp;
   }

   public void setStartTimeStamp(long startTimeStamp)
   {
      this.startTimeStamp = startTimeStamp;
   }

   public Action doCreateAction()
   {
      return new DaemonAction(this);
   }
   
   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         mapMessage.setString(DAEMON_TYPE_TAG, type);
         mapMessage.setLong(START_TIME_STAMP_TAG, startTimeStamp);
      }
   }
   
   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         this.type = mapMessage.getString(DAEMON_TYPE_TAG);
         this.startTimeStamp = mapMessage.getLong(START_TIME_STAMP_TAG);
      }
   }
   
   @Override
   public int hashCode()
   {
      // only partition and type participate to hash code.
      
      final int prime = 31;
      int result = 1;
      result = prime * result + getPartitionOid();
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      // only partition and type participate to equals.
      
      if (obj instanceof DaemonCarrier)
      {
         DaemonCarrier other = (DaemonCarrier) obj;
         if (type == null)
         {
            if (other.type != null)
            {
               return false;
            }
         }
         else if (!type.equals(other.type))
         {
            return false;
         }
         return getPartitionOid() == other.getPartitionOid();
      }
      return false;
   }

   public DaemonCarrier copy()
   {
      try
      {
         return (DaemonCarrier) super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new InternalException("Unable to clone DaemonCarrier.", e);
      }
   }
   
   public static DaemonCarrier extract(MapMessage message) throws JMSException
   {
      DaemonCarrier carrier = new DaemonCarrier();
      carrier.extract((Message) message);
      return carrier;
   }
}
