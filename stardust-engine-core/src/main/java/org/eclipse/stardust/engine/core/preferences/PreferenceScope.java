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
package org.eclipse.stardust.engine.core.preferences;

/**
 * A PreferenceScope defines scoping of preferences.
 * The granularity of scopes is divided into per Partition, per UserRealm and per User.
 * <p>
 * By using the DEFAULT scope there is the option to retrieve default values provided via SPI.
 * 
 * @author sauer, roland.stamm
 */
public enum PreferenceScope
{
   
   /**
    * The DEFAULT scope is used to read default values supplied by the
    * <code>IStaticConfigurationProvider</code> SPI implementing providers.<br>
    * 
    * @see org.eclipse.stardust.engine.core.spi.preferences.IStaticConfigurationProvider
    */
   DEFAULT,
   /**
    * The PARTITION scope is used to address preferences on partition level.
    * These preferences are accessible for all users on the same partition.
    */
   PARTITION,
   /**
    * The REALM scope is used to address preferences on realm level.
    * These preferences are saved per UserRealm, meaning only Users on the same UserRealm can access the same set of preferences.
    */
   REALM,
   /**
    * The USER scope is used to address preferences on user level.
    * These preferences are saved per User. No other Users except Administrators have access to them.
    */
   USER 
}
