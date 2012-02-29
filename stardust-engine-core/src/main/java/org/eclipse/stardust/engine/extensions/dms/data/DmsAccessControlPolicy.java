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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.Privilege;

import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;



/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class DmsAccessControlPolicy implements AccessControlPolicy, Serializable 
{

   private static final long serialVersionUID = 1L;
   
   private final Set<AccessControlEntry> aces;
   private final Set<AccessControlEntry> originalState;
   private final boolean isNew;
   private final boolean isReadonly;


   public DmsAccessControlPolicy(Set<AccessControlEntry> aces, boolean isNew, boolean isReadonly)
   {
      this.aces = aces;
      this.originalState = saveOriginalState();
      this.isNew = isNew;
      this.isReadonly = isReadonly;
   }
   
   private Set<AccessControlEntry> saveOriginalState()
   {
      Set<AccessControlEntry> result = CollectionUtils.newSet();
      for (AccessControlEntry ace : this.aces)
      {
         result.add(new DmsAccessControlEntry(ace.getPrincipal(), new HashSet<Privilege>(ace.getPrivileges())));
      }
      return result;
   }

   public Set<AccessControlEntry> getOriginalState()
   {
      return originalState;
   }

   public boolean isNew()
   {
      return isNew;
   }

   public Set<AccessControlEntry> getAccessControlEntries()
   {
      return this.aces;
   }

   public void addAccessControlEntry(Principal principal, Set<Privilege> privileges)
   {
      this.aces.add(new DmsAccessControlEntry(principal, privileges));
   }

   public void removeAccessControlEntry(AccessControlEntry ace)
   {
      this.aces.remove(ace);
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append(this.aces);
      if (isNew)
      {
         sb.append(" (new)");
      }
      return sb.toString();
   }

   public void removeAllAccessControlEntries()
   {
      this.aces.clear();
   }

   public boolean isReadonly()
   {
      return isReadonly;
   }   
   
}
