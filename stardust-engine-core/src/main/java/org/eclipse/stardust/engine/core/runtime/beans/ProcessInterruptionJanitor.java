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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author fherinean
 * @version $Revision$
 */
public class ProcessInterruptionJanitor extends SecurityContextAwareAction
{
   public static final Logger trace = LogManager.getLogger(ProcessInterruptionJanitor.class);

   private long processInstanceOID;

   public ProcessInterruptionJanitor(InterruptionJanitorCarrier carrier)
   {
      super(carrier);
      this.processInstanceOID = carrier.getProcessInstance();
   }

   public Object execute()
   {
      boolean performed = false;

      IProcessInstance pi = ProcessInstanceBean.findByOID(processInstanceOID);
      if (ProcessInstanceState.Interrupted.equals(pi.getState()))
      {
         try
         {
            pi.lock();
            // update state of the process after getting the lock and check it again.
            try
            {
               ((IdentifiablePersistentBean) pi).reloadAttribute("state");
            }
            catch (PhantomException e)
            {
               throw new InternalException(e);
            }

            if (ProcessInstanceState.Interrupted.equals(pi.getState()))
            {
               long interrupted = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
                     .getCount(
                           ActivityInstanceBean.class,
                           QueryExtension.where(Predicates.andTerm(Predicates.isEqual(
                                 ActivityInstanceBean.FR__STATE,
                                 ActivityInstanceState.INTERRUPTED), Predicates.isEqual(
                                 ActivityInstanceBean.FR__PROCESS_INSTANCE,
                                 pi.getOID()))), true);
               if (interrupted == 0)
               {
                  trace.info("Reset interruption state for process: " + pi);
                  pi.resetInterrupted();
                  performed = true;
               }
            }
         }
         catch (ConcurrencyException e)
         {
            trace.info("Cannot run interruption janitor for " + processInstanceOID
                  + " due to a locking conflict, scheduling a new one.");

            InterruptionJanitorCarrier carrier = new InterruptionJanitorCarrier(processInstanceOID);
            ForkingServiceFactory factory = (ForkingServiceFactory)
                  Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
            ForkingService service = null;
            try
            {
               service = factory.get();
               service.fork(carrier, true);
            }
            finally
            {
               factory.release(service);
            }
         }
      }

      return performed ? Boolean.TRUE : Boolean.FALSE;
   }

   public String toString()
   {
      return "Process interruption janitor, pi = " + processInstanceOID;
   }
}
