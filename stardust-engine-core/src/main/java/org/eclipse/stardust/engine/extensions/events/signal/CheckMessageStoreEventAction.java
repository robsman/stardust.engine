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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ISignalMessage;
import org.eclipse.stardust.engine.core.runtime.beans.SignalMessageBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;

/**
 * <p>
 * Checks whether the signal the <i>Activity Instance</i> is waiting for has already been fired
 * and persisted in the <i>Audit Trail Database</i>'s <i>Message Store</i> (see {@code SignalMessageBean}).
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class CheckMessageStoreEventAction implements EventActionInstance
{
   private String signalName;

   private long partitionOid;

   @Override
   public void bootstrap(final Map actionAttributes, final Iterator ignored)
   {
      final Object signalCode = actionAttributes.get(SignalMessageAcceptor.BPMN_SIGNAL_CODE);
      this.signalName = (signalCode != null) ? signalCode.toString() : "";

      this.partitionOid = SecurityProperties.getPartitionOid();
   }

   @Override
   public Event execute(final Event event)
   {
      final ActivityInstanceBean ai = ActivityInstanceBean.findByOID(event.getObjectOID());

      final Iterator<SignalMessageBean> messageStoreIter = SignalMessageBean.findFor(partitionOid, signalName);
      final SignalMessageAcceptor signalMsgAcceptor = new SignalMessageAcceptor();
      while (messageStoreIter.hasNext())
      {
         final SignalMessageBean signalMsg = messageStoreIter.next();
         if (signalMsgAcceptor.matchPredicateData(ai, signalName, signalMsg.getMessage()))
         {
            scheduleSignalMessageProcessing(ai, signalMsg);
            break;
         }
      }

      return null;
   }

   private void scheduleSignalMessageProcessing(final IActivityInstance ai, final ISignalMessage signalMsg)
   {
      final ForkingServiceFactory fsFactory = (ForkingServiceFactory) Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
      ForkingService forkingService = null;
      try
      {
         final ProcessMessageStoreSignalMessageActionCarrier carrier = new ProcessMessageStoreSignalMessageActionCarrier();
         carrier.setActivityInstanceOid(ai.getOID());
         carrier.setMessageOid(signalMsg.getOID());

         forkingService = fsFactory.get();
         forkingService.fork(carrier, true);
      }
      catch (final Exception e)
      {
         // TODO - bpmn-2-events - review exception handling
      }
      finally
      {
         fsFactory.release(forkingService);
      }
   }
}
