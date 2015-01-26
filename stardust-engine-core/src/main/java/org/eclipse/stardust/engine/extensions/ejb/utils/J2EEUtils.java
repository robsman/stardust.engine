/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.ejb.utils;

import java.security.Principal;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.compatibility.spi.security.DefaultPrincipalNameProvider;
import org.eclipse.stardust.engine.core.compatibility.spi.security.PrincipalNameProvider;

public class J2EEUtils
{
   private static final Logger trace = LogManager.getLogger(J2EEUtils.class);

   public static String getPrincipalName(Principal principal)
   {
      if (null == principal)
      {
         throw new IllegalArgumentException(
               "Argument principal is not allowed to be null.");
      }

      String result;

      PrincipalNameProvider principalNameProvider = ExtensionProviderUtils
            .getFirstExtensionProvider(PrincipalNameProvider.class);
      if (principalNameProvider == null)
      {
         trace.debug("Could not load PrincipalNameProvider. Will use default approach: principal.getName().");
         principalNameProvider = new DefaultPrincipalNameProvider();
      }

      result = principalNameProvider.getName(principal);

      return result;
   }

   private J2EEUtils()
   {
   }

}