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

import java.security.Principal;
import java.util.Set;


/**
 * AccessControlPolicy represents an access control list containing
 * AccessControlEntry objects.
 * 
 * @author rsauer
 * @version $Revision: 24736 $
 */
public interface AccessControlPolicy 
{

   /**
    * Creates a new access control entry and fills it with the principal and privileges 
    * passed.
    * @param principal
    * @param privileges
    */
   void addAccessControlEntry(Principal principal, Set<Privilege> privileges);
   
   /**
    * Removes a access control entry from this policy.
    * @param ace access control entry that is contained in this policy
    */
   void removeAccessControlEntry(AccessControlEntry ace);
   
   /**
    * Returns all access control entries contained in this policy.
    * @return
    */
   Set<AccessControlEntry> getAccessControlEntries(); 
   
   /**
    * Empties the policy. Empty policies may be removed by the underlying implementation.
    */
   void removeAllAccessControlEntries();
   
}
