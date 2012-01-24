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
public class JanitorCarrier extends ActionCarrier
{
   private long processInstance;
   private long count;
   private static final String PROCESS_INSTANCE_KEY = "processInstance";
   private static final String COUNT_KEY = "tokenCount";

   /**
    * Default constructor, needed for creating instances via reflection.
    *
    * @see JanitorCarrier(IProcessInstance, long)
    */
   public JanitorCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }

   public JanitorCarrier(long processInstance)
   {
      this(processInstance, 0);
   }

   public JanitorCarrier(long processInstance, long count)
   {
      this();

      this.processInstance = processInstance;
      this.count = count;
   }

   public Action doCreateAction()
   {
      return new ProcessCompletionJanitor(this);
   }

   public String toString()
   {
      return "Process instance janitor carrier: pi = " + processInstance + ", count = " + count;
   }
   
   protected long getCount()
   {
      return count;
   }
   
   protected long getProcessInstance()
   {
      return processInstance;
   }
   
   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         mapMessage.setLong(PROCESS_INSTANCE_KEY, processInstance);
         mapMessage.setLong(COUNT_KEY, count);
      }
   }
   
   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         processInstance = mapMessage.getLong(PROCESS_INSTANCE_KEY);
         count = mapMessage.getLong(COUNT_KEY);
      }
   }
}


