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
package org.eclipse.stardust.engine.cli.console;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.cli.common.DepartmentClientUtils;


public class DeleteDepartmentCommand extends ModifyDepartmentCommand
{

   @Override
   public String getSummary()
   {
      return "Deletes Departments";
   }

   @Override
   public int run(Map options)
   {
      ModifyDepartmentCommandConfig config = getConfig(options);

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(config.getUserName(),
            config.getPassWord());
      AdministrationService adminService = serviceFactory.getAdministrationService();

      DepartmentClientUtils dh = DepartmentClientUtils.getInstance(globalOptions);
      List<String> departmentPath = config.getDepartmentPath();
      List<Organization> organizationHierarchy = config.getOrganizationHierarchy();
      List<Department> departmentHierarchy = dh.getDepartmentHierarchy(departmentPath,
            organizationHierarchy);
      // determine what to delete
      if (!departmentHierarchy.isEmpty())
      {
         Department toDelete = null;
         // if specified- delete the whole hierarchy
         // deleting via the api is already working recursive
         if (config.isRecursive())
         {
            toDelete = departmentHierarchy.get(0);
         }
         // otherwise only delete the last department in the hierarchy
         else
         {
            int lastElementIndex = departmentHierarchy.size() - 1;
            toDelete = departmentHierarchy.get(lastElementIndex);
         }
         adminService.removeDepartment(toDelete.getOID());
      }

      return 1;
   }

}
