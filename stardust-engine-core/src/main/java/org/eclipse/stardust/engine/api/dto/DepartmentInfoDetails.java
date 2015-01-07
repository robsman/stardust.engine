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

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;


public class DepartmentInfoDetails implements DepartmentInfo
{
   private static final long serialVersionUID = 1L;

   private long oid;
   private String id;
   private String name;
   private long runtimeOrganizationOid;

   public DepartmentInfoDetails(long oid, String id, String name, long runtimeOrganizationOid)
   {
      this.oid = oid;
      this.id = id;
      this.name = name;
      this.runtimeOrganizationOid = runtimeOrganizationOid;
   }
   
   public DepartmentInfoDetails(IDepartment department)
   {
      this(department.getOID(), department.getId(), department.getName(), department.getRuntimeOrganizationOID());
   }

   public long getOID()
   {
      return oid;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public long getRuntimeOrganizationOID()
   {
      return runtimeOrganizationOid;
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

   public QualifiedModelParticipantInfo getScopedParticipant(ModelParticipantInfo participant)
         throws InvalidArgumentException
   {
      return getParticipant(this, participant);
   }

   public static QualifiedModelParticipantInfo getParticipant(final DepartmentInfo department, ModelParticipantInfo participant)
   {
      if (participant == null)
      {
         return null;
      }
      
      final long runtimeElementOID = participant.getRuntimeElementOID();
      final String id = participant.getId();
      final String qualifiedId = participant instanceof QualifiedModelParticipantInfo
            ? ((QualifiedModelParticipantInfo) participant).getQualifiedId() : id;
      final String name = participant.getName();
      final boolean isDepartmentScoped = participant.isDepartmentScoped();
      final boolean definesDepartmentScope = participant.definesDepartmentScope();
      final String departmentId = department.getId();
      
      if (participant instanceof OrganizationInfo)
      {
         return new OrganizationInfoDetails(runtimeElementOID, qualifiedId, name, isDepartmentScoped, definesDepartmentScope, department);
      }
      if (participant instanceof RoleInfo)
      {
         return new RoleInfoDetails(runtimeElementOID, qualifiedId, name, isDepartmentScoped, definesDepartmentScope, department);
      }
      
      return new QualifiedModelParticipantInfo()
      {
         private static final long serialVersionUID = 1L;
   
         public DepartmentInfo getDepartment()
         {
            return department;
         }
   
         public long getRuntimeElementOID()
         {
            return runtimeElementOID;
         }
   
         public String getId()
         {
            return id;
         }
   
         public String getQualifiedId()
         {
            return qualifiedId;
         }

         public String getName()
         {
            return name;
         }
   
         @Override
         public String toString()
         {
            return qualifiedId + '[' + departmentId + ']';
         }
   
         public boolean isDepartmentScoped()
         {
            return isDepartmentScoped;
         }

         public boolean definesDepartmentScope()
         {
            return definesDepartmentScope;
         }
      };
   }
}
