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

import java.util.Map;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;




/**
 * Contract for providing login-based authentication services.
 *
 * @author rsauer
 * @version $Revision$
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Public)
public interface ExternalLoginProvider
{
   /**
    * Performs a login-based authentication request.
    *
    * @param id The identity of the party requesting authentication.
    * @param password The password proving the identity of the requesting party.
    * @param properties Map providing further login properties. Key is of type String.
    *                   Valid Values are {@link SecurityProperties#PARTITION},
    *                   {@link SecurityProperties#DOMAIN},
    *                   and {@link SecurityProperties#REALM}
    * @return A description of the outcome of the authentication request.
    */
   ExternalLoginResult login(String id, String password, Map properties);
}

