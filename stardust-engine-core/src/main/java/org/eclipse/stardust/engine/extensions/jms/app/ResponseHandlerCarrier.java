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
package org.eclipse.stardust.engine.extensions.jms.app;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;


public class ResponseHandlerCarrier extends ActionCarrier
{
   public static final String PARTITION_ID_TAG = DefaultMessageHelper.PARTITION_ID_HEADER;
   
   private String source;
   private Message message;
   
   private String userDomainId;
   private String partitionId;
   
   public ResponseHandlerCarrier(String source, Message message)
   {
      super(RESPONSE_HANDLER_MESSAGE_TYPE_ID);
      
      this.source = source;
      this.message = message;
   }
   
   public Message getMessage()
   {
      return message;
   }
   
   public String getSource()
   {
      return source;
   }
   
   public Action doCreateAction()
   {
      return new ResponseHandlerImpl(this);
   }

   protected void doExtract(Message message) throws JMSException
   {
      partitionId = message.getStringProperty(PARTITION_ID_TAG);
   }

   protected void doFillMessage(Message message) throws JMSException
   {
   }

   public String getPartitionId()
   {
      return partitionId;
   }

   public String getUserDomainId()
   {
      return userDomainId;
   }

   public void setPartitionId(String partitionId)
   {
      this.partitionId = partitionId;
   }

   public void setUserDomainId(String userDomainId)
   {
      this.userDomainId = userDomainId;
   }
}
