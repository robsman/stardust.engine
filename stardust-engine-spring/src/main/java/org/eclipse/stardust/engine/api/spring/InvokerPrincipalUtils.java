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

/**
 * @author rsauer
 * @version $Revision$
 */
public class InvokerPrincipalUtils
{
   private static final ThreadLocal CURRENT = new ThreadLocal();

   public static InvokerPrincipal getCurrent()
   {
      return (InvokerPrincipal) CURRENT.get();
   }

   public static void setCurrent(String name, Map properties)
   {
      setCurrent(new InvokerPrincipal(name, properties));
   }

   public static void setCurrent(InvokerPrincipal principal)
   {
      CURRENT.set(principal);
   }

   public static void removeCurrent()
   {
      CURRENT.set(null);
   }
}
