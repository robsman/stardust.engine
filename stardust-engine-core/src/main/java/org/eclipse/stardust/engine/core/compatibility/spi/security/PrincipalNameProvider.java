/*******************************************************************************
 * Copyright (c) 2011. 2013 SunGard CSA LLC and others.
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
 * @author born
 *
 */
public interface PrincipalNameProvider
{
   /**
    * Provides the name for a given principal. Default implementation would be to return principal.getName().
    * It is possible to test for a certain implementation and use method fo these implementation
    * in order to return a more specific name for the given principal.
    *
    * @param principal the principal
    * @return the name of the principal. Not allowed to be null.
    */
   public String getName(Principal principal);
}
