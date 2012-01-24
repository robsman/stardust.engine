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
package org.eclipse.stardust.engine.core.spi.security;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;



/**
 * Contract for providing login-based authentication services.
 *
 * @author ubirkemeyer
 * @version $Revision$
 *
 * @deprecated Use {@link ExternalLoginProvider} instead.
 */
@SPI(status = Status.Deprecated, useRestriction = UseRestriction.Public)
@Deprecated
public interface LoginProvider
{
   /**
    * Performs a login-based authentication request.
    *
    * @param id The identity of the party requesting authentication.
    * @param password The password proving the identity of the requesting party.
    * @return A description of the outcome of the authentication request.
    */
   LoginResult login(String id, String password);
}

