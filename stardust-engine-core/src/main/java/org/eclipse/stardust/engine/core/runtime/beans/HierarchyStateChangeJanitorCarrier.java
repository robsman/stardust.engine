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
package org.eclipse.stardust.engine.core.runtime.beans;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

public abstract class HierarchyStateChangeJanitorCarrier extends ActionCarrier
{
   private static final long serialVersionUID = 1L;

   protected long processInstanceOid;

   protected int triesLeft;

   protected long userOid;

   private static final String PI_OID_KEY = "piOid";

   private static final String TRIES_LEFT_KEY = "triesLeft";

   private static final String USER_OID_KEY = "userOid";

   public HierarchyStateChangeJanitorCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }

   public HierarchyStateChangeJanitorCarrier(long processInstanceOid, long abortingUserOid, int triesLeft)
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
      this.processInstanceOid = processInstanceOid;
      this.triesLeft = triesLeft;
      this.userOid = abortingUserOid;
   }

   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }

   public int getTriesLeft()
   {
      return triesLeft;
   }

   public long getUserOid()
   {
      return userOid;
   }

   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;

         mapMessage.setLong(PI_OID_KEY, processInstanceOid);
         mapMessage.setInt(TRIES_LEFT_KEY, triesLeft);
         mapMessage.setLong(USER_OID_KEY, userOid);
      }
   }

   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;

         processInstanceOid = mapMessage.getLong(PI_OID_KEY);
         triesLeft = mapMessage.getInt(TRIES_LEFT_KEY);
         userOid = mapMessage.getLong(USER_OID_KEY);
      }
   }

}