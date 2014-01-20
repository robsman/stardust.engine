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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;


/**
 * AccessControlEntry represents a number of privileges assigned to 
 * a specific principal.
 * 
 * @author rsauer
 * @version $Revision: 24736 $
 */
public interface AccessControlEntry extends Serializable 
{

   /**
    * Returns the principal the privileges are assigned to
    * @return the principal the privileges are assigned to
    */
   public Principal getPrincipal();

   /**
    * Set of privileges assigned to the principal
    * @return set of privileges assigned to the principal
    */
   public Set<Privilege> getPrivileges();
   
   public EntryType getType();

   public static enum EntryType
   {
      ALLOW, DENY;
   }
}
