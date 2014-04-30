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

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * <p>
 * An SPI allowing to validate the given principal.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Public)
public interface PrincipalValidator
{
   boolean isValid(Principal principal);
}
