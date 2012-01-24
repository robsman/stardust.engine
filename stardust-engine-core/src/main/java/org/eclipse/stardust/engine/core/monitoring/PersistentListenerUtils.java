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

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener.Factory;



/**
 * 
 * @author thomas.wolfram
 *
 */
public class PersistentListenerUtils
{


   
   public static IPersistentListener getPersistentListener(Persistent persistent)
   {

      GlobalParameters globals = GlobalParameters.globals();
      
      final String KEY_PERSISTENT_LISTENER_MEDIATOR = persistent.getClass().getName()
      + ".PersistentListenerMediator";
      
      IPersistentListener mediator = (IPersistentListener) globals.get(KEY_PERSISTENT_LISTENER_MEDIATOR);
      
      if (mediator == null)
      {

         List/* <Factory> */factories = ExtensionProviderUtils.getExtensionProviders(IPersistentListener.Factory.class);

         List<IPersistentListener> listeners = CollectionUtils.newList();

         for (int i = 0; i < factories.size(); ++i)
         {
            final Factory listenerFactory = (Factory) factories.get(i);

            listeners = listenerFactory.createListener(persistent.getClass());

         }
         
         mediator = new PersistentListenerMediator(listeners);         
         globals.set(KEY_PERSISTENT_LISTENER_MEDIATOR, mediator);
      }

      return mediator;
   }


}