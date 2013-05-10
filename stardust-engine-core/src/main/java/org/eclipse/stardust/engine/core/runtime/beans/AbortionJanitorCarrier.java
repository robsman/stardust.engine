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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;


/**
 *
 * @author sborn
 * @version $Revision: $
 */
public class AbortionJanitorCarrier extends ActionCarrier
{
   private static final long serialVersionUID = 1L;

   private long processInstanceOid;
   private int triesLeft;
   private long abortingUserOid;

   private static final String PI_OID_KEY = "piOid";
   private static final String TRIES_LEFT_KEY = "triesLeft";
   private static final String ABORTING_USER_OID_KEY = "userOid";

   /**
    * Default constructor, needed for creating instances via reflection.
    *
    */
   public AbortionJanitorCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }

   public AbortionJanitorCarrier(long processInstanceOid, long abortingUserOid)
   {
      this(processInstanceOid,  abortingUserOid, Parameters.instance().getInteger(
            ProcessAbortionJanitor.PRP_RETRY_COUNT, 10));
   }
   
   public AbortionJanitorCarrier(long processInstanceOid, long abortingUserOid, int triesLeft)
   {
      this();

      this.processInstanceOid = processInstanceOid;
      this.triesLeft = triesLeft;
      this.abortingUserOid = abortingUserOid;
   }

   public Action doCreateAction()
   {
      return new ProcessAbortionJanitor(this);
   }

   public String toString()
   {
      return "Process instance abortion janitor carrier: pi = " + processInstanceOid
            + ".";
   }

   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }
   
   public int getTriesLeft()
   {
      return triesLeft;
   }

   public long getAbortingUserOid()
   {
      return abortingUserOid;
   }   
   
   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;

         mapMessage.setLong(PI_OID_KEY, processInstanceOid);
         mapMessage.setInt(TRIES_LEFT_KEY, triesLeft);
         mapMessage.setLong(ABORTING_USER_OID_KEY, abortingUserOid);
      }
   }

   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;

         processInstanceOid = mapMessage.getLong(PI_OID_KEY);
         triesLeft = mapMessage.getInt(TRIES_LEFT_KEY);
         abortingUserOid = mapMessage.getLong(ABORTING_USER_OID_KEY);
      }
   }
}
