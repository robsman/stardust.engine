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

import java.security.Principal;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.vfs.IAccessControlEntry;
import org.eclipse.stardust.vfs.IAccessControlEntry.EntryType;
import org.eclipse.stardust.vfs.IAccessControlPolicy;
import org.eclipse.stardust.vfs.IPrivilege;

/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class IAccessControlPolicyAdapter implements IAccessControlPolicy
{

   private final DmsAccessControlPolicy policy;

   public IAccessControlPolicyAdapter(DmsAccessControlPolicy policy)
   {
      this.policy = policy;
   }

   public void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges)
   {
      throw new RuntimeException("Not implemented.");
   }

   public void addAccessControlEntry(Principal principal, Set<IPrivilege> privileges,
         EntryType type)
   {
      throw new RuntimeException("Not implemented.");
   }
   
   public Set<IAccessControlEntry> getAccessControlEntries()
   {
      Set<IAccessControlEntry> result = CollectionUtils.newSet();
      for (AccessControlEntry ace : policy.getAccessControlEntries())
      {
         result.add(new IAccessControlEntryAdapter(ace));
      }
      return result;
   }

   public Set<IAccessControlEntry> getOriginalState()
   {
      Set<IAccessControlEntry> result = CollectionUtils.newSet();
      for (AccessControlEntry ace : policy.getOriginalState())
      {
         result.add(new IAccessControlEntryAdapter(ace));
      }
      return result;

   }

   public boolean isNew()
   {
      return this.policy.isNew();
   }

   public boolean isReadonly()
   {
      return this.policy.isReadonly();
   }

   public void removeAccessControlEntry(IAccessControlEntry ace)
   {
      throw new RuntimeException("Not implemented.");
   }

   
   public void removeAllAccessControlEntries()
   {
      throw new RuntimeException("Not implemented.");
   }

}
