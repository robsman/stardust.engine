/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.spi.security;

import java.security.Principal;

/**
 * <p>
 * The default principal validator, which always returns <code>true</code>, even for
 * principals being <code>null</code>.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class AlwaysValidPrincipalValidator implements PrincipalValidator
{
   @Override
   public boolean isValid(Principal ignored)
   {
      return true;
   }
}
