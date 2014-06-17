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

import java.security.Principal;

import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;

public class InvokerPrincipalProvider implements PrincipalProvider
{
   public static final InvokerPrincipalProvider INSTANCE = new InvokerPrincipalProvider();

   private InvokerPrincipalProvider()
   {
      // singleton
   }

   public Principal getPrincipal()
   {
      return InvokerPrincipalUtils.getCurrent();
   }
}
