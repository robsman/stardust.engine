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
package org.eclipse.stardust.engine.spring.web;

import org.eclipse.stardust.common.config.GlobalParametersProviderFactory;
import org.eclipse.stardust.common.config.PropertyProvider;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class SpringAppContextPropertiesProviderFactory
      implements GlobalParametersProviderFactory
{
   private static Logger logger = LogManager
         .getLogger(SpringAppContextPropertiesProviderFactory.class);

   public int getPriority()
   {
      return 4;
   }

   public PropertyProvider getPropertyProvider()
   {
      PropertyProvider provider = null;

      try
      {
         // try to load a spring class, if that fails spring is not used, the application
         // context must not be loaded and a debug message is logged
         Class.forName("org.springframework.web.context.ContextLoader");
         Class clazz = Class
               .forName("org.eclipse.stardust.engine.spring.web.SpringAppContextPropertiesProvider");
         provider = (PropertyProvider) clazz.newInstance();
      }
      catch (ClassNotFoundException e)
      {
         logLoadingMsg();
      }
      catch (InstantiationException e)
      {
         logLoadingMsg();
      }
      catch (IllegalAccessException e)
      {
         logLoadingMsg();
      }

      return provider;
   }

   private void logLoadingMsg()
   {
      if (logger.isDebugEnabled())
      {
         logger
               .debug("Failed loading class ag.carnot.web.jsf.common.SpringAppContextPropertiesProvider");
      }
   }
}
