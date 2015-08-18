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


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class InterruptionJanitorCarrier extends ActionCarrier
{
   private long processInstance;
   private static final String PROCESS_INSTANCE_KEY = "processInstance";

   /**
    * Default constructor, needed for creating instances via reflection.
    *
    * @see org.eclipse.stardust.engine.core.runtime.beans.InterruptionJanitorCarrier(IProcessInstance, long)
    */
   public InterruptionJanitorCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }

   public InterruptionJanitorCarrier(long processInstance)
   {
      this();

      this.processInstance = processInstance;
   }
   
   public long getProcessInstance()
   {
      return processInstance;
   }

   public Action doCreateAction()
   {
      return new ProcessInterruptionJanitor(this);
   }

   public String toString()
   {
      return "Process instance interruption janitor carrier: pi = " + processInstance;
   }
   
   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         mapMessage.setLong(PROCESS_INSTANCE_KEY, processInstance);
      }
   }
   
   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {  
         MapMessage mapMessage = (MapMessage) message;
         
         processInstance = mapMessage.getLong(PROCESS_INSTANCE_KEY);
      }
   }
}


