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
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Modules;
import org.eclipse.stardust.engine.core.extensions.ExtensionService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemDescription;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLocatorUtils;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EngineService
{
   public static final Logger trace = LogManager.getLogger(EngineService.class);

   public static final String BOOTSTRAPPED = "Engine.Bootstrapped";

   public static void init()
   {
      if ( !Parameters.instance().getBoolean(BOOTSTRAPPED, false))
      {
         performInit();
      }
   }

   private static synchronized void performInit()
   {
      // @todo (france, ub): won't work in debug mode if you deploy/debug/deploy in that order
      if ( !Parameters.instance().getBoolean(BOOTSTRAPPED, false))
      {
         try
         {
            trace.info("Bootstrapping engine");
            
            ExtensionService.initializeModuleExtensions(Modules.ENGINE);
            
            ItemLocatorUtils.registerDescription(ModelManagerFactory.ITEM_NAME,
                  new ItemDescription(new ModelManagerLoader(),
                        Parameters.instance().getString(
                              EngineProperties.WATCHER_PROPERTY, NullWatcher.class.getName())));
            
            Parameters.instance().set(BOOTSTRAPPED, true);
         }
         catch (ApplicationException e)
         {
            PublicException pe = new PublicException(e.getError(), e.getMessage(), null);
            pe.setLogged(true);
            throw pe;
         }
      }
   }

}
