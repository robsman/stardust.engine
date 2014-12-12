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
package org.eclipse.stardust.engine.core.runtime.internal.utils;

import java.util.List;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.spi.runtime.IRuntimeExtensionConfigurator;



/**
 * @author sauer
 * @version $Revision: $
 */
public class RuntimeExtensionUtils
{
   public static void configureExtensions(MethodInvocation invocation)
   {
      List configurators = ExtensionProviderUtils.getExtensionProviders(IRuntimeExtensionConfigurator.class);
      for (int i = 0; i < configurators.size(); ++i)
      {
         IRuntimeExtensionConfigurator configurator = (IRuntimeExtensionConfigurator) configurators.get(i);
         try
         {
            configurator.initialize(invocation);
         }
         catch (PublicException pe)
         {
            throw pe;
         }
         catch (Exception e)
         {
            // Exceptions ignored
         }
      }
   }

   public static void cleanupExtensions(MethodInvocation invocation)
   {
      // cleanup in reverse order, to support potential dependencies between runtime extensions
      List configurators = ExtensionProviderUtils.getExtensionProviders(IRuntimeExtensionConfigurator.class);
      for (int i = configurators.size() - 1; i >= 0; --i)
      {
         IRuntimeExtensionConfigurator configurator = (IRuntimeExtensionConfigurator) configurators.get(i);
         try
         {
            configurator.cleanup(invocation);
         }
         catch (PublicException pe)
         {
            throw pe;
         }
         catch (Exception e)
         {
            // Exceptions ignored
         }
      }
   }

}