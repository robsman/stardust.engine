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

import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLocatorUtils;

// @todo (france, ub): integrate in the parameters class 
public class ModelManagerFactory
{
   public static final String ITEM_NAME = "model.manager";

   public static ModelManager getCurrent()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      if ((null != rtEnv) && (null != rtEnv.getModelManager()))
      {
         return rtEnv.getModelManager();
      }
      
      return (ModelManager) ItemLocatorUtils.getLocator(ITEM_NAME).get();
   }
   
   public static boolean isAvailable()
   {
      return (null != ItemLocatorUtils.getDescription(ITEM_NAME));
   }

   public static void setDirty()
   {
      ItemLocatorUtils.getLocator(ITEM_NAME).markDirty();
      getCurrent().resetLastDeployment();
   }
}
