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
package org.eclipse.stardust.engine.core.preferences;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ReconfigurationInfo;


public class PreferenceChangeHandler
{
   private List<IPreferenceChangeListener> listeners;

   private String moduleId;

   public String getModuleId()
   {
      return moduleId;
   }

   public PreferenceChangeHandler(String moduleId)
   {
      this.moduleId = moduleId;
      this.listeners = CollectionUtils.newLinkedList();
   }

   public synchronized void addListener(IPreferenceChangeListener listener)
   {
      listeners.add(listener);
   }

   public synchronized void removeListener(IPreferenceChangeListener listener)
   {
      listeners.remove(listener);
   }

   public List<ReconfigurationInfo> fireEvent(PreferenceChangeEvent event)
   {
      List<ReconfigurationInfo> infos = CollectionUtils.newArrayList();
      for (IPreferenceChangeListener listener : listeners)
      {
         infos.addAll(listener.onPreferenceChange(event));
      }
      return infos;
   }

}
