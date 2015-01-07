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

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;



/**
 * @author Florin.Herinean
 */
public class DepartmentDetails implements Department
{
   private static final long serialVersionUID = 1L;

   private long oid;
   private String id;
   private String name;
   private String description;
   private Department parentDepartment;
   private Organization organization;

   public DepartmentDetails(IDepartment department)
   {
      this.oid = department.getOID();
      this.id = department.getId();
      this.name = department.getName();
      this.description = department.getDescription();
      
      long runtimeOrganizationOID = department.getRuntimeOrganizationOID();
      IOrganization participant = (IOrganization) ModelManagerFactory.getCurrent().findModelParticipant(
            PredefinedConstants.ANY_MODEL, runtimeOrganizationOID);
      organization = DetailsFactory.create((IOrganization) participant, IOrganization.class, OrganizationDetails.class);
      
      if (null != department.getParentDepartment())
      {
         parentDepartment = DetailsFactory.create(department.getParentDepartment(), IDepartment.class, DepartmentDetails.class);
      }
   }

   public String getDescription()
   {
      return description;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public long getOID()
   {
      return oid;
   }

   public Department getParentDepartment()
   {
      return parentDepartment;
   }

   public Organization getOrganization()
   {
      return organization;
   }

   public long getRuntimeOrganizationOID()
   {
      return organization.getRuntimeElementOID();
   }

   public String toString()
   {
      return parentDepartment == null ? id : parentDepartment.toString() + "/" + id;
   }

   @Override
   public int hashCode()
   {
      // (fh) assuming that there will never be so many scopes to exceed the int value,
      // then just return the truncated int value of the oid which is unique for each
      // organization scope.
      return (int) oid;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      return (obj instanceof DepartmentInfo) && oid == ((DepartmentInfo) obj).getOID();
   }

   public QualifiedModelParticipantInfo getScopedParticipant(ModelParticipant participant)
      throws InvalidArgumentException
   {
      if (participant instanceof ModelParticipant && !isChild((ModelParticipant) participant, CollectionUtils.<String>newSet()))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ORGANIZATION_HIERARCHY.raise());
      }
      return DepartmentInfoDetails.getParticipant(new DepartmentInfoDetails(oid, id, name, organization.getRuntimeElementOID()), participant);
   }
   
   private boolean isChild(ModelParticipant participant, Set<String> visited)
   {
      if (visited.contains(participant.getId()))
      {
         return false;
      }
      if (CompareHelper.areEqual(participant.getId(), organization.getId()))
      {
         return true;
      }
      visited.add(participant.getId());
      List<Organization> superOrgs = participant.getAllSuperOrganizations(); 
      for (Organization org : superOrgs)
      {
         if (isChild(org, visited))
         {
            return true;
         }
      }
      return false;
   }
}
