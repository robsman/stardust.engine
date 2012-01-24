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

public abstract class ExternalUserGroupConfiguration
{
   /**
    * Gets the name of the external user group.
    * 
    * @return The name.
    */
   public abstract String getName();

   /**
    * Gets the description of the external user group.
    * 
    * @return The description.
    */
   public abstract String getDescription();

   /**
    * Gets custom properties of the external user group.
    * 
    * @return The set of property (name, value) pairs.
    */
   public abstract Map getProperties();

   /**
    * Gets the list of model participants the external user group has grants for.
    *
    * @return A collection with IDs of the granted model participants.
    */
   //public abstract Collection getGrantedModelParticipants();
}
