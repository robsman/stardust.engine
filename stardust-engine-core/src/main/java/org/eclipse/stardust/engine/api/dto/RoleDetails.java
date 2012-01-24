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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


/**
 * @author mgille
 * @version $Revision$
 */
public class RoleDetails extends ModelParticipantDetails implements Role
{
   private static final long serialVersionUID = 8231944061460892426L;

   private final List organizations;
   
   private final List teams;

   private final List clientOrganizations;

   private Department department;

   RoleDetails(IRole role, DetailsCache detailsCache)
   {
      this(role, detailsCache, role);
   }

   private RoleDetails(IRole role, DetailsCache detailsCache, Object cacheKey)
   {
      super(role);
      
      if (null != detailsCache)
      {
         detailsCache.put(cacheKey, this);
      }

      this.organizations = CollectionUtils.newList();

      this.teams = CollectionUtils.newList();
      for (Iterator i = role.getAllTeams(); i.hasNext();)
      {
         IOrganization team = (IOrganization) i.next();

         Object details = DetailsFactory.create(team, IOrganization.class,
               OrganizationDetails.class);
         organizations.add(details);
         teams.add(details);
      }
      
      this.clientOrganizations = CollectionUtils.newList();
      for (Iterator i = role.getAllClientOrganizations(); i.hasNext();)
      {
         IOrganization organization = (IOrganization) i.next();

         Object details = DetailsFactory.create(organization, IOrganization.class,
               OrganizationDetails.class);

         organizations.add(details);
         clientOrganizations.add(details);
      }
   }

   public List getTeams()
   {
      return Collections.unmodifiableList(teams);
   }

   public List getClientOrganizations()
   {
      return Collections.unmodifiableList(clientOrganizations);
   }

   public List<Organization> getAllSuperOrganizations()
   {
      return Collections.unmodifiableList(organizations);
   }

   public String toString()
   {
      return "Role: '" + getQualifiedId() + (department == null ? "" : "', department: '" + department) + "', model oid: " + getModelOID();
   }

   @Override
   public DepartmentInfo getDepartment()
   {
      return department;
   }
   
   public String getQualifiedId()
   {
      String qualifiedId_ = super.getQualifiedId();      
      
      if (qualifiedId_ != null && qualifiedId_.startsWith("{"))
      {
         QName qname = QName.valueOf(qualifiedId_);
         String id = qname.getLocalPart();
         if(PredefinedConstants.ADMINISTRATOR_ROLE.equals(id))
         {
            return id;
         }
      }         
      return qualifiedId_;
   }
}