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
package org.eclipse.stardust.engine.core.security;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.security.HMAC;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author rsauer
 * @version $Revision: 10347 $
 */
public class InvokerPrincipalUtils
{
   private static final ThreadLocal CURRENT = new ThreadLocal();

   private static final String SECRET;

   static
   {
      String secret = Parameters.instance().getString(SecurityProperties.PRINCIPAL_SECRET);
      if (secret == null)
      {
         String name = ManagementFactory.getRuntimeMXBean().getName();
         String startTime = String.valueOf(ManagementFactory.getRuntimeMXBean().getStartTime());
         secret = name + startTime;
      }

      SECRET = secret;
   }

   public static InvokerPrincipal getCurrent()
   {
      return (InvokerPrincipal) CURRENT.get();
   }

   /**
    * @deprecated use {@link #setCurrent(InvokerPrincipal)} instead
    */
   @Deprecated
   public static InvokerPrincipal setCurrent(String name, Map properties)
   {
      return setCurrent(new InvokerPrincipal(name, properties));
   }

   public static InvokerPrincipal setCurrent(InvokerPrincipal principal)
   {
      InvokerPrincipal previous = getCurrent();

      CURRENT.set(principal);

      return previous;
   }

   public static InvokerPrincipal removeCurrent()
   {
      InvokerPrincipal previous = getCurrent();

      CURRENT.remove();

      return previous;
   }

   public static InvokerPrincipal generateSignedPrincipal(String name, Map properties)
   {
      byte[] signature = generateSignature(name, properties);
      return new InvokerPrincipal(name, properties, signature);
   }

   public static boolean checkPrincipalSignature(InvokerPrincipal principal)
   {
      byte[] signature = generateSignature(principal.getName(), principal.getProperties());
      return Arrays.equals(signature, principal.getSignature());
   }

   private static byte[] generateSignature(String name, Map properties)
   {
      String partition = (String) properties.get(SecurityProperties.PARTITION);
      String domain = (String) properties.get(SecurityProperties.DOMAIN);
      String realm = (String) properties.get(SecurityProperties.REALM);

      StringBuffer sb = new StringBuffer();
      sb.append(name);
      if (partition != null)
      {
         sb.append(partition);
      }
      if (domain != null)
      {
         sb.append(domain);
      }
      if (realm != null)
      {
         sb.append(realm);
      }
      try
      {
         HMAC hmac = new HMAC(HMAC.MD5);
         return hmac.hash(SECRET.getBytes(), sb.toString().getBytes());
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }
}
