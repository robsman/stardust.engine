/***********************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 ***********************************************************************************/
package org.eclipse.stardust.engine.extensions.events.signal;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ISignalMessage;
import org.eclipse.stardust.engine.core.runtime.beans.SecurityContextAwareAction;
import org.eclipse.stardust.engine.core.runtime.beans.SignalMessageBean;
import org.eclipse.stardust.engine.extensions.events.signal.SignalMessageAcceptor.SignalMessageActivityInstanceMatch;

/**
 * <p>
 * Carries an {@code ProcessMessageStoreSignalMessageAction}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class ProcessMessageStoreSignalMessageActionCarrier extends ActionCarrier<Void>
{
   private static final long serialVersionUID = -6521900004912521405L;

   private static final String AI_OID_NAME = "aiOid";
   private static final String MESSAGE_OID_NAME = "messageOid";

   private Long aiOid;
   private Long messageOid;

   public ProcessMessageStoreSignalMessageActionCarrier()
   {
      super(SYSTEM_MESSAGE_TYPE_ID);
   }

   public void setActivityInstanceOid(final long aiOid)
   {
      this.aiOid = aiOid;
   }

   public void setMessageOid(final long messageOid)
   {
      this.messageOid = messageOid;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier#doCreateAction()
    */
   @Override
   public Action doCreateAction()
   {
      return new ProcessMessageStoreSignalMessageAction(this);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier#doFillMessage(javax.jms.Message)
    */
   @Override
   protected void doFillMessage(final Message message) throws JMSException
   {
      ensureIsMapMessage(message);
      ensureMandatoryFieldsAreInitialized();

      final MapMessage mapMsg = (MapMessage) message;
      mapMsg.setLong(AI_OID_NAME, aiOid);
      mapMsg.setLong(MESSAGE_OID_NAME, messageOid);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier#doExtract(javax.jms.Message)
    */
   @Override
   protected void doExtract(final Message message) throws JMSException
   {
      ensureIsMapMessage(message);

      final MapMessage mapMsg = (MapMessage) message;
      this.aiOid = mapMsg.getLong(AI_OID_NAME);
      this.messageOid = mapMsg.getLong(MESSAGE_OID_NAME);

      ensureMandatoryFieldsAreInitialized();
   }

   private void ensureIsMapMessage(final Message message)
   {
      if ( !(message instanceof MapMessage))
      {
         throw new IllegalArgumentException("Map message expected.");
      }
   }

   private void ensureMandatoryFieldsAreInitialized()
   {
      if (aiOid == null)
      {
         throw new IllegalStateException("Activity Instance OID must be initialized.");
      }
      if (messageOid == null)
      {
         throw new IllegalStateException("Signal Message OID must be initialized.");
      }
   }

   /**
    * <p>
    * Completes the given <i>Activity Instance</i> by accepting the signal fired by means of a <i>Signal Message</i>
    * persisted in the <i>Audit Trail Database</i> and applying the <i>Signal Message</i> data.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   private static final class ProcessMessageStoreSignalMessageAction extends SecurityContextAwareAction<Void>
   {
      private final long aiOid;
      private final long messageOid;

      public ProcessMessageStoreSignalMessageAction(final ProcessMessageStoreSignalMessageActionCarrier carrier)
      {
         super(carrier);

         this.aiOid = carrier.aiOid.longValue();
         this.messageOid = carrier.messageOid.longValue();
      }

      @Override
      public Void execute()
      {
         final IActivityInstance ai = ActivityInstanceBean.findByOID(aiOid);
         final ISignalMessage msg = SignalMessageBean.findByOid(messageOid);

         new SignalMessageActivityInstanceMatch(new SignalMessageAcceptor(), ai).process(null, msg.getMessage());

         return null;
      }
   }
}
