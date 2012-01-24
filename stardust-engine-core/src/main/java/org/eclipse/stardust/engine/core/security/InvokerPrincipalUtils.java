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

import java.util.Map;

/**
 * @author rsauer
 * @version $Revision: 10347 $
 */
public class InvokerPrincipalUtils
{
   private static final ThreadLocal CURRENT = new ThreadLocal();

   public static InvokerPrincipal getCurrent()
   {
      return (InvokerPrincipal) CURRENT.get();
   }

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
      
      CURRENT.set(null);
      
      return previous;
   }
}
