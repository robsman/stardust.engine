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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.ConcatenatedList;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


/**
 * <p>
 * Many methods of the CARNOT EJBs return detail objects. Detail objects are
 * serializable helper objects passed by value to the client. They can, for
 * instance, pass the necessary information from the audit trail to the
 * embedding application in a dynamic way to guarantee an optimum of
 * performance.
 * </p>
 *
 * @author mgille
 * @version $Revision$
 */
public class OrganizationDetails extends ModelParticipantDetails implements Organization
{
   private static final long serialVersionUID = -2566690736520843818L;
   private final List superOrganizations;
   private final List subOrganizations;
   private final List roles;
   
   private final Role teamLead;
   private Department department;
   
   OrganizationDetails(IOrganization organization, DetailsCache detailsCache)
   {
      this(organization, detailsCache, organization);
   }

   private OrganizationDetails(IOrganization organization, DetailsCache detailsCache, Object cacheKey)
   {
      super(organization);
      
      if (null != detailsCache)
      {
         detailsCache.put(cacheKey, this);
      }

      superOrganizations = new ArrayList();
      for (Iterator i = organization.getAllOrganizations(); i.hasNext();)
      {
         superOrganizations.add(DetailsFactory.create(i.next(), IOrganization.class,
               OrganizationDetails.class));
      }

      subOrganizations = new ArrayList();
      roles = new ArrayList();

      for (Iterator members = organization.getAllParticipants(); members.hasNext(); )
      {
         IModelParticipant member = (IModelParticipant) members.next();

         if (member instanceof IOrganization)
         {
            // prevents infinite recursion as already created OrganizationDetails will get
            // reused from the details object cache

            subOrganizations.add(DetailsFactory.create(member, IOrganization.class,
                  OrganizationDetails.class));
         }
         else if (member instanceof IRole)
         {
            // prevents infinite recursion as already created RoleDetails will get reused
            // from the details object cache

            roles.add(DetailsFactory.create(member, IRole.class, RoleDetails.class));
         }
      }
      
      this.teamLead = (null != organization.getTeamLead())
            ? (Role) DetailsFactory.create(organization.getTeamLead(), IRole.class,
                  RoleDetails.class)
            : null;
   }

   public List<Organization> getAllSuperOrganizations()
   {
      return Collections.unmodifiableList(superOrganizations);
   }

   public List getAllSubOrganizations()
   {
      return Collections.unmodifiableList(subOrganizations);
   }

   public List getAllSubRoles()
   {
      return Collections.unmodifiableList(roles);
   }

   public List getAllSubParticipants()
   {
      return new ConcatenatedList(roles, subOrganizations);
   }

   public Role getTeamLead()
   {
      return teamLead;
   }

   public String toString()
   {
      return "Organization: '" + getId() + (department == null ? "" : "', department: '" + department) + "', model oid: " + getModelOID();
   }

   @Override
   public DepartmentInfo getDepartment()
   {
      return department;
   }
}
