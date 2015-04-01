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
package org.eclipse.stardust.engine.core.monitoring;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener;

public class PersistentListenerMediator implements IPersistentListener
{
   private static final Logger trace = LogManager.getLogger(PersistentListenerMediator.class);

   final List<IPersistentListener> listeners;

   public PersistentListenerMediator(List<IPersistentListener> listeners)
   {
      this.listeners = listeners;
   }

   public void updated(Persistent persistent)
   {
      for (int i = 0; i < listeners.size(); ++i)
      {
         IPersistentListener listener = listeners.get(i);
         try
         {
            listener.updated(persistent);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting persistent update event.", e);
         }
      }
   }

   public void created(Persistent persistent)
   {
      for (int i = 0; i < listeners.size(); ++i)
      {
         IPersistentListener listener = listeners.get(i);
         try
         {
            listener.created(persistent);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting persistent created event.", e);
         }
      }
   }
}
