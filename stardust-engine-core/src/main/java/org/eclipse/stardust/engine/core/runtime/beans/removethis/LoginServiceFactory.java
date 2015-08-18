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
package org.eclipse.stardust.engine.core.runtime.beans.removethis;

import static org.eclipse.stardust.common.config.extensions.ApplicationConfigResolver.getStaticallyConfiguredProvider;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.security.audittrail.AuditTrailLoginService;
import org.eclipse.stardust.engine.core.security.jaas.JaasLoginService;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginProvider;
import org.eclipse.stardust.engine.core.spi.security.LoginProvider;
import org.eclipse.stardust.engine.core.spi.security.LoginProviderAdapter;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LoginServiceFactory
{
   private static final Logger trace = LogManager.getLogger(LoginServiceFactory.class);

   public static ExternalLoginProvider getService()
   {
      String loginMode = Parameters.instance().getString(
            SecurityProperties.AUTHENTICATION_MODE_PROPERTY,
            SecurityProperties.AUTHENTICATION_MODE_INTERNAL);
      if (SecurityProperties.AUTHENTICATION_MODE_INTERNAL.equals(loginMode))
      {
         try
         {
            ExternalLoginProvider loginProvider = ExtensionProviderUtils.getFirstExtensionProvider(
                  ExternalLoginProvider.class,
                  SecurityProperties.AUTHENTICATION_SERVICE_PROPERTY);
            if (null != loginProvider)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Using a non-default login provider: " + loginProvider);
               }

               return loginProvider;
            }
            else
            {
               LoginProvider oldStyleLoginProvider = getStaticallyConfiguredProvider(
                     LoginProvider.class, SecurityProperties.AUTHENTICATION_SERVICE_PROPERTY);
               if (null != oldStyleLoginProvider)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Using a non-default login provider: "
                           + oldStyleLoginProvider);
                  }
                  trace.warn("Wrapping old-style " + LoginProvider.class
                        + " implementation for backwards compatibility: "
                        + oldStyleLoginProvider);

                  return new LoginProviderAdapter(oldStyleLoginProvider);
               }
            }
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }

         trace.debug("Using the Audit Trail login service.");

         return new AuditTrailLoginService();
      }
      else if (SecurityProperties.AUTHENTICATION_MODE_JAAS.equals(loginMode))
      {
         return new JaasLoginService();
      }
      else
      {
         throw new InternalException("Unknown login mode: " + loginMode);
      }
   }
}
