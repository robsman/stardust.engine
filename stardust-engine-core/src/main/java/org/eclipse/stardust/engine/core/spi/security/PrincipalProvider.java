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

import java.security.Principal;

/**
 * Callback interface to provide implicit authentication information.
 * 
 * @author rsauer
 * @version $Revision$
 */
public interface PrincipalProvider
{
   /**
    * Provides the principal to be used.
    * <p />
    * To pass extra information like partition, domain or realm IDs, an instance of
    * {@link PrincipalWithProperties} should be returned. 
    * 
    * @return
    */
   Principal getPrincipal();
}
