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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener.Factory;



/**
 * 
 * @author thomas.wolfram
 * 
 */
public class PersistentListenerFactory implements Factory
{
   private static final Logger trace = LogManager.getLogger(PersistentListenerFactory.class);

   public List<IPersistentListener> createListener(Class< ? extends Persistent> clazz)
   {
      List<IPersistentListener> listeners = CollectionUtils.newList();
      if (clazz == ProcessInstanceBean.class)
      {
         listeners.add(new ProcessInstancePersistentListener());
      }
      return listeners;
   }

}
