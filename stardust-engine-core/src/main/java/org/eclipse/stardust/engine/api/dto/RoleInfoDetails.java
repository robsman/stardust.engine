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
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.QualifiedRoleInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;


public class RoleInfoDetails extends ModelParticipantInfoDetails implements QualifiedRoleInfo
{
   private static final long serialVersionUID = 1L;

   public RoleInfoDetails(String id)
   {
      this(0, id, null, false, false, null);
   }
   
   public RoleInfoDetails(Pair<IRole, DepartmentInfo> pair)
   {
      super(pair);
   }
   
   public RoleInfoDetails(long runtimeElementOID, String id, String name,
         boolean isDepartmentScoped, boolean supportsDepartments, DepartmentInfo department)
   {
      super(runtimeElementOID, id, name, isDepartmentScoped, supportsDepartments, department);
   }

   public RoleInfoDetails(RoleInfo role, DepartmentInfo department)
   {
      this(role.getRuntimeElementOID(), role.getId(), role.getName(),
            role.isDepartmentScoped(), role.definesDepartmentScope(), department);
   }
}
