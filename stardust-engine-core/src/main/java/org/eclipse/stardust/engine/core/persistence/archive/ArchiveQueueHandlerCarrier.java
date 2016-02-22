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
package org.eclipse.stardust.engine.core.persistence.archive;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;

/**
 * 
 * Carrier for sending messages containing ExportResults received from jms/CarnotArchiveQueue to ExportProcessesCommand for archiving
 * @author jsaayman
 *
 */
public class ArchiveQueueHandlerCarrier extends ActionCarrier
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String source;
   private ObjectMessage message;
   
   private String partitionId;
   
   public ArchiveQueueHandlerCarrier(ObjectMessage message)
   {
      super(ARCHIVE_MESSAGE_TYPE_ID);
      
      this.message = message;
   }
   
   public ObjectMessage getMessage()
   {
      return message;
   }
   
   public String getSource()
   {
      return source;
   }
   
   public Action doCreateAction()
   {
      return new ArchiveQueueHandler(this);
   }

   protected void doExtract(Message message) throws JMSException
   {
      partitionId = message.getStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER);
   }

   protected void doFillMessage(Message message) throws JMSException
   {
   }

   public String getPartitionId()
   {
      return partitionId;
   }

   public void setPartitionId(String partitionId)
   {
      this.partitionId = partitionId;
   }
}
