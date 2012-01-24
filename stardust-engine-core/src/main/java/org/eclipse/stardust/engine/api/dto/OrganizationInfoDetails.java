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

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;


public class OrganizationInfoDetails extends ModelParticipantInfoDetails
      implements QualifiedOrganizationInfo
{
   private static final long serialVersionUID = 1L;

   public OrganizationInfoDetails(String id)
   {
      this(0, id, null, false, false, null);
   }
   
   public OrganizationInfoDetails(Pair<IOrganization, DepartmentInfo> pair)
   {
      super(pair);
   }

   public OrganizationInfoDetails(long runtimeElementOID, String id, String name,
         boolean isDepartmentScoped, boolean definesDepartmentScope, DepartmentInfo department)
   {
      super(runtimeElementOID, id, name, isDepartmentScoped, definesDepartmentScope, department);
   }

   public OrganizationInfoDetails(OrganizationInfo organization, DepartmentInfo department)
   {
      this(organization.getRuntimeElementOID(), organization.getId(), organization.getName(),
            organization.isDepartmentScoped(), organization.definesDepartmentScope(), department);
   }
}
