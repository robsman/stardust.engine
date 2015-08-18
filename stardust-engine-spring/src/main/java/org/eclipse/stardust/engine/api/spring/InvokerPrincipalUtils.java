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
package org.eclipse.stardust.engine.api.spring;

import java.util.Map;

import org.eclipse.stardust.engine.core.security.InvokerPrincipal;

/**
 * @author rsauer
 * @version $Revision$
 *
 * @deprecated use {@link org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils} instead
 */
@Deprecated
public class InvokerPrincipalUtils
{
   public static InvokerPrincipal getCurrent()
   {
      return org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils.getCurrent();
   }

   public static void setCurrent(String name, Map properties)
   {
      org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils.setCurrent(name, properties);
   }

   public static void setCurrent(InvokerPrincipal principal)
   {
      org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils.setCurrent(principal);
   }

   public static void removeCurrent()
   {
      org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils.removeCurrent();
   }

   public static InvokerPrincipal generateSignedPrincipal(String name, Map properties)
   {
      return org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils.generateSignedPrincipal(name, properties);
   }

   public static boolean checkPrincipalSignature(InvokerPrincipal principal)
   {
      return org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils.checkPrincipalSignature(principal);
   }
}
