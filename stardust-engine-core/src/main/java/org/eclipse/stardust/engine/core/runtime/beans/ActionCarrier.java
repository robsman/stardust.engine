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

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class ActionCarrier<T> implements Serializable, Cloneable, IActionCarrier<T>
{
   private static final long serialVersionUID = -1580596887273865035L;

   public static final int SYSTEM_MESSAGE_TYPE_ID = 1;
   public static final int DAEMON_MESSAGE_TYPE_ID = 2;
   public static final int RESPONSE_HANDLER_MESSAGE_TYPE_ID = 3;

   public static final String MESSAGE_TYPE_TAG = "messageType";
   public static final String TRANSPORT_CLASS_TAG = "transportClass";
   public static final String USER_DOMAIN_OID_TAG = "userDomainOid";
   public static final String PARTITION_OID_TAG = "partitionOid";

   private final int messageType;

   private boolean initializedByExtract = false;
   private short partitionOid = -1;
   private long userDomainOid = 0;

   protected ActionCarrier(int messageType)
   {
      this.messageType = messageType;
   }

   protected ActionCarrier(int messageType, boolean initialized)
   {
      this.messageType = messageType;
      if (initialized)
      {
         partitionOid = SecurityProperties.getPartitionOid();
         userDomainOid = SecurityProperties.getUserDomainOid();
         initializedByExtract = true;
      }
   }

   public int getMessageType()
   {
      return messageType;
   }

   public short getPartitionOid()
   {
      if (initializedByExtract)
      {
         return partitionOid;
      }
      else
      {
         return SecurityProperties.getPartitionOid();
      }
   }

   public long getUserDomainOid()
   {
      if (initializedByExtract)
      {
         return userDomainOid;
      }
      else
      {
         return SecurityProperties.getUserDomainOid();
      }
   }

   public static final int extractMessageType(Message message)
   {
      try
      {
         return message.getIntProperty(MESSAGE_TYPE_TAG);
      }
      catch (JMSException e)
      {
         throw new InternalException(e);
      }
   }

   public final void fillMessage(Message message) throws JMSException
   {
      prepareMessage(message);

      message.setLongProperty(USER_DOMAIN_OID_TAG, getUserDomainOid());
      message.setShortProperty(PARTITION_OID_TAG, getPartitionOid());

      doFillMessage(message);
   }

   public final void extract(Message message) throws JMSException
   {
      initializedByExtract = true;

      try
      {
         for (Enumeration e = message.getPropertyNames(); e.hasMoreElements(); )
         {
            String prpName = (String) e.nextElement();

            if (USER_DOMAIN_OID_TAG.equals(prpName))
            {
               userDomainOid = message.getLongProperty(USER_DOMAIN_OID_TAG);
            }
            else if (PARTITION_OID_TAG.equals(prpName))
            {
               partitionOid = message.getShortProperty(PARTITION_OID_TAG);
            }
         }
      }
      catch (NumberFormatException e)
      {
      }

      doExtract(message);
   }

   public final Action createAction()
   {
      Action action = doCreateAction();

      if (action instanceof SecurityContextAwareAction)
      {
         action = SecurityContextAwareAction
               .actionDefinesSecurityContext((SecurityContextAwareAction) action);
      }

      return action;
   }

   public abstract Action doCreateAction();

   protected abstract void doFillMessage(Message message) throws JMSException;

   protected abstract void doExtract(Message message) throws JMSException;

   protected final void prepareMessage(Message message) throws JMSException
   {
      message.setIntProperty(MESSAGE_TYPE_TAG, messageType);
      message.setStringProperty(TRANSPORT_CLASS_TAG, getClass().getName());
   }

   protected Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }
}
