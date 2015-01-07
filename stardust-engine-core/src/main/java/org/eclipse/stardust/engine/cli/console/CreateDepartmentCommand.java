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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;


/**
 * @author holger.prause
 * @version
 *
 * Class for creating {@link Department}
 */
public class CreateDepartmentCommand extends ModifyDepartmentCommand
{
   public static final String NAME_OPTION_KEY = "name";
   public static final String DESCRIPTION_OPTION_KEY = "description";
   private Options argTypes = new Options();

   public CreateDepartmentCommand()
   {
      argTypes = super.getOptions();
      // register the name of the department option
      boolean hasArg = true;
      String longname = "-name";
      String shortname = "-n";
      String keyname = NAME_OPTION_KEY;
      String summary = "The name of the department to create";
      argTypes.register(longname, shortname, keyname, summary, hasArg);

      // register the description of the department option
      longname = "-description";
      shortname = "-d";
      keyname = DESCRIPTION_OPTION_KEY;
      summary = "The description of the department to create";
      argTypes.register(longname, shortname, keyname, summary, hasArg);
   }

   @Override
   public int run(Map options)
   {
      ModifyDepartmentCommandConfig config = getConfig(options);
      String name = (String) options.get(NAME_OPTION_KEY);
      String description = (String) options.get(DESCRIPTION_OPTION_KEY);

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(config.getUserName(),
            config.getPassWord());
      AdministrationService adminService = serviceFactory.getAdministrationService();
      QueryService queryService = serviceFactory.getQueryService();

      List<String> departmentPath = config.getDepartmentPath();
      List<Organization> organizationHierachy = config.getOrganizationHierarchy();

      Organization targetOrganization = null;
      String targetDepartmentId = null;
      if (!departmentPath.isEmpty())
      {
         int lastElementIndex = departmentPath.size() - 1;
         targetDepartmentId = departmentPath.get(lastElementIndex);
         targetOrganization = organizationHierachy.get(lastElementIndex);

         departmentPath.remove(lastElementIndex);
         organizationHierachy.remove(lastElementIndex);
      }

      Department parent = null;
      for (int i = 0; i < departmentPath.size(); i++)
      {
         String departmentId = departmentPath.get(i);
         Organization org = organizationHierachy.get(i);
         Department department = findDepartment(queryService, departmentId, parent, org);
         if (department == null)
         {
            if (config.isRecursive())
            {
               department = adminService.createDepartment(departmentId, departmentId,
                     null, parent, org);
            }
            else
            {
               throw new PublicException(
                     BpmRuntimeError.CLI_INVALID_DEPARTMENT_PATH_CREATE_IT_MANUALLY_OR_SPECIFIY_OPTION
                           .raise(departmentId, org.getId()));
            }
         }
         parent = department;
      }

      if (StringUtils.isEmpty(name))
      {
         name = targetDepartmentId;
      }
      adminService.createDepartment(targetDepartmentId, name, description, parent,
            targetOrganization);
      return 1;
   }

   private Department findDepartment(QueryService queryService, String id,
         Department parent, Organization organization)
   {
      Department found = null;
      try
      {
         found = queryService.findDepartment(parent, id, organization);
      }
      catch (ObjectNotFoundException e)
      {
      }
      return found;
   }

   @Override
   public Options getOptions()
   {
      return argTypes;
   }

   @Override
   public String getSummary()
   {
      return "Creates Departments";
   }
}
