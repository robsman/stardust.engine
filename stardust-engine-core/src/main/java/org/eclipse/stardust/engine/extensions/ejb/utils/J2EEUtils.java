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
package org.eclipse.stardust.engine.extensions.ejb.utils;

import java.security.Principal;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


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

      String principalNameProviderClassName = Parameters.instance().getString(
            PrincipalNameProvider.PRP_PRINCIPAL_NAME_PROVIDER,
            DefaultPrincipalNameProvider.class.getName());
      
      Object rawPrincipalNameProvider = null;
      try
      {
         rawPrincipalNameProvider = Reflect
               .createInstance(principalNameProviderClassName);
      }
      catch (Exception x)
      {
         trace.warn(
               "Could not load PrincipalNameProvider. Will use principal.getName().", x);
      }

      if (rawPrincipalNameProvider instanceof PrincipalNameProvider)
      {
         PrincipalNameProvider principalNameProvider = (PrincipalNameProvider) rawPrincipalNameProvider;
         result = principalNameProvider.getName(principal);
      }
      else
      {
         // default implementation
         result = principal.getName();
      }

      return result;
   }

   private J2EEUtils()
   {
   }

}