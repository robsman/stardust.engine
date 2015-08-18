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
import java.util.Map;

import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * Enhanced {@link Principal} interface, allowing to provide arbitrary extra properties.
 * 
 * @author rsauer
 * @version $Revision$
 */
public interface PrincipalWithProperties extends Principal
{
   /**
    * Provides extra properties associated with this principal.
    *
    * @return The properties. Must not be <code>null>/code>.
    * 
    * @see SecurityProperties#PARTITION
    * @see SecurityProperties#REALM
    * @see SecurityProperties#DOMAIN
    */
   Map getProperties();
}
