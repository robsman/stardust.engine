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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ISignalMessage;
import org.eclipse.stardust.engine.core.runtime.beans.SignalMessageBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class SignalEventCondition implements EventHandlerInstance
{
   static final Logger trace = LogManager.getLogger(SignalEventCondition.class);

   private Long pastSignalsGracePeriod;

   private short partitionOid;

   @Override
   public void bootstrap(Map actionAttributes)
   {
      final Object gracePeriodParam = actionAttributes
            .get(SignalMessageAcceptor.PAST_SIGNALS_GRACE_PERIOD);
      if (gracePeriodParam instanceof Number)
      {
         this.pastSignalsGracePeriod = ((Number) gracePeriodParam).longValue();
      }
      else if (gracePeriodParam instanceof String)
      {
         this.pastSignalsGracePeriod = Long.parseLong((String) gracePeriodParam);
      }
      // TODO support Period?

      this.partitionOid = SecurityProperties.getPartitionOid();
   }

   @Override
   public boolean accept(Event event)
   {
      if (pastSignalsGracePeriod == null || pastSignalsGracePeriod.longValue() <= 0)
      {
         return false;
      }

      AttributedIdentifiablePersistent eventSource = EventUtils.getEventSourceInstance(event);
      if (eventSource instanceof IActivityInstance)
      {
         IActivityInstance ai = (IActivityInstance) eventSource;

         IActivity activity = ai.getActivity();
         for(IEventHandler handler : activity.getEventHandlers())
         {
            if (handler.getOID() == event.getHandlerModelElementOID())
            {
               // found the context we're invoked in
               String signalName = handler.getName();

               final Date now = TimestampProviderUtils.getTimeStamp();
               final Date validFrom = new Date(now.getTime() - pastSignalsGracePeriod.longValue() * 1000);

               final Iterator<SignalMessageBean> messageStoreIter = SignalMessageBean.findFor(partitionOid, signalName, validFrom);
               final SignalMessageAcceptor signalMsgAcceptor = new SignalMessageAcceptor();
               while (messageStoreIter.hasNext())
               {
                  final SignalMessageBean signalMsg = messageStoreIter.next();
                  if (signalMsgAcceptor.matchPredicateData(ai, signalName, signalMsg.getMessage()))
                  {
                     scheduleSignalMessageProcessing(ai, signalMsg);

                     return true;
                  }
               }

               break;
            }
         }
      }

      return false;
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
