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
package org.eclipse.stardust.engine.core.compatibility.spi.security;

import java.security.Principal;

/**
 * Default implementation for interface PrincipalNameProvider. This will be used if no
 * other is specified. It simply will return the name of the given principal.
 *
 * @author stephan.born
 *
 */
public class DefaultPrincipalNameProvider implements PrincipalNameProvider
{
   public String getName(Principal principal)
   {
      return principal.getName();
   }
}
