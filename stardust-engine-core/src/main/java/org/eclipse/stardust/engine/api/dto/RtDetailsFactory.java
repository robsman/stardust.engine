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
package org.eclipse.stardust.engine.api.dto;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.CaseDescriptorRef;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.utils.PerformerUtils;


/**
 * @author sauer
 * @version $Revision$
 */
public class RtDetailsFactory
{
   private static final String DATA_PATH_KEY = DataPathDetails.class.getName();
   
   private Map<Object, Object> cache = CollectionUtils.newMap();
   private boolean usingCaches = false;
   
   public boolean isUsingCaches()
   {
      return usingCaches;
   }

   public void setUsingCaches(boolean usingCaches)
   {
      this.usingCaches = usingCaches;
   }

   public DataPath createDetails(IDataPath dp)
   {
      DataPath details = dp.getRuntimeAttribute(DATA_PATH_KEY);
      if (details == null)
      {
         details = new DataPathDetails(dp);
         dp.setRuntimeAttribute(DATA_PATH_KEY, details);
      }
      return details;
   }

   public DataPath createDetails(CaseDescriptorRef ref)
   {
      DataPath details = null;
      if (!usingCaches || (details = (DataPath) cache.get(ref)) == null)
      {
         details = new DataPathDetails(ref);
         cache.put(ref, details);
      }
      return details;
   }

   public ProcessInstance createDetails(IProcessInstance pi)
   {
      ProcessInstance details = null;
      if (!usingCaches || (details = (ProcessInstance) cache.get(pi)) == null)
      {
         details = new ProcessInstanceDetails(pi);
         cache.put(pi, details);
      }
      return details;
   }

   public ProcessInstanceLinkType createDetails(IProcessInstanceLinkType pilt)
   {
      ProcessInstanceLinkType details = null;
      if (!usingCaches || (details = (ProcessInstanceLinkType) cache.get(pilt)) == null)
      {
         details = new ProcessInstanceLinkTypeDetails(pilt.getOID(), pilt.getId(), pilt.getDescription());
         cache.put(pilt, details);
      }
      return details;
   }

   public ProcessInstanceLink createDetails(IProcessInstanceLink pil)
   {
      ProcessInstanceLink details = null;
      if (!usingCaches || (details = (ProcessInstanceLink) cache.get(pil)) == null)
      {
         details = new ProcessInstanceLinkDetails(pil.getProcessInstanceOID(),
               pil.getLinkedProcessInstanceOID(), DetailsFactory.create(pil.getLinkType()),
               pil.getCreateTime(), pil.getCreatingUserOID(), pil.getComment());
         cache.put(pil, details);
      }
      return details;
   }

   public UserInfo createDetails(IUser user)
   {
      UserInfo details = (UserInfo) cache.get(user);
      if (details == null)
      {
         details = new UserInfoDetails(user.getOID(), user.getId(), PerformerUtils.getQualifiedName(user),
               user.getFirstName(), user.getLastName());
         cache.put(user, details);
      }
      return details;
   }

   public User createUserDetails(IUser user)
   {
      Key key = new Key(user);
      User details = (User) cache.get(key);
      if (details == null)
      {
         details = new UserDetails(user);
         cache.put(key, details);
      }
      return details;
   }

   public UserGroupInfo createDetails(IUserGroup group)
   {
      UserGroupInfo details = (UserGroupInfo) cache.get(group);
      if (details == null)
      {
         details = new UserGroupInfoDetails(group.getOID(), group.getId(), group.getName());
         cache.put(group, details);
      }
      return details;
   }

   public DepartmentInfo createDetails(IDepartment department)
   {
      DepartmentInfo details = (DepartmentInfo) cache.get(department);
      if (details == null)
      {
         details = new DepartmentInfoDetails(department);
         cache.put(department, details);
      }
      return details;
   }
   
   public Department createDepartmentDetails(IDepartment department)
   {
      Key key = new Key(department);
      Department details = (Department) cache.get(key);
      if (details == null)
      {
         details = new DepartmentDetails(department);
         cache.put(key, details);
      }
      return details;
   }
   
   private static class Key
   {
      private Object[] items;

      public Key(Object... items)
      {
         this.items = items;
      }

      public int hashCode()
      {
         return 31 + Arrays.hashCode(items);
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         return Arrays.equals(items, ((Key) obj).items);
      }
   }
}
