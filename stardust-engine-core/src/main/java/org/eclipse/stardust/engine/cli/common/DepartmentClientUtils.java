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
package org.eclipse.stardust.engine.cli.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public class DepartmentClientUtils
{
   private static DepartmentClientUtils instance;
   private QueryService queryService;

   private DepartmentClientUtils(QueryService queryService)
   {
      this.queryService = queryService;
   }

   public static DepartmentClientUtils getInstance(String userName, String passWord)
   {
      Map<String, String> options = new HashMap<String, String>();
      options.put(SecurityProperties.CRED_USER, userName);
      options.put(SecurityProperties.CRED_PASSWORD, passWord);
      return getInstance(options);
   }

   public static DepartmentClientUtils getInstance(Map globalOptions)
   {
      if (instance == null)
      {
         ServiceFactory factory = ServiceFactoryLocator.get(globalOptions);
         QueryService queryService = factory.getQueryService();
         instance = new DepartmentClientUtils(queryService);
      }
      return instance;
   }

   public static DepartmentClientUtils getInstance(QueryService queryService)
   {
      if (instance == null)
      {
         instance = new DepartmentClientUtils(queryService);
      }
      return instance;
   }

   /**
    * Splits the department path and returns the path segments as {@link List}
    *
    * @param path - the path to split, each segment separated with a '/'
    * @return the splitted path as {@link List}
    */
   public List<String> parseDepartmentPath(String path)
   {
      LinkedList<String> parsedPath = new LinkedList<String>();
      String exp = "/(?!/)";
      String[] splittedPath = path.split(exp);
      for (String s : splittedPath)
      {
         if (!StringUtils.isEmpty(s))
         {
            parsedPath.add(s);
         }
      }
      return parsedPath;
   }

   /**
    * Finds the department for the given {@link Participant} and department path
    * @param departmentPath - the path to the department
    * @param participant - the participant
    * @return the department found, or null otherwise
    */
   public Department findDepartment(String departmentPath, Participant participant)
   {
      Department found = null;
      List<String> parsedPath = parseDepartmentPath(departmentPath);
      List<Organization> organizationHierarchy = getOrganizationHierarchy(participant,
            true);
      List<Department> departmentHierarchy = getDepartmentHierarchy(parsedPath,
            organizationHierarchy);
      if (!departmentHierarchy.isEmpty())
      {
         int lastElementIndex = departmentHierarchy.size() - 1;
         found = departmentHierarchy.get(lastElementIndex);
      }
      return found;
   }

   /**
    * Gets a Hierarchy of Departments for the given path and participant
    * @param departmentPath - the path to the department
    * @param participant - the participant
    * @throws PublicException if the department hierarchy is deeper than the organization hierarchy
    * @throws ObjectNotFoundException if any path segment could not be located as a department
    * @return the hierarchy found for the given path
    */
   public List<Department> getDepartmentHierarchy(String departmentPath,
         Participant participant)
   {
      List<String> parsedPath = parseDepartmentPath(departmentPath);
      List<Organization> organizationHierarchy = getOrganizationHierarchy(participant,
            true);
      return getDepartmentHierarchy(parsedPath, organizationHierarchy);
   }

   /**
    * Gets a Hierarchy of Departments for the given (splitted)path and organization hierarchy
    * @param departmentPath - the path to the department(splitted in segments)
    * @param organizationHierarchy - the (scoped)organization tree
    * @throws PublicException if the department hierarchy is deeper than the organization hierarchy
    * @throws ObjectNotFoundException if any path segment could not be located as a department
    * @return the department hierarchy found for the given path
    */
   public List<Department> getDepartmentHierarchy(List<String> departmentPath,
         List<Organization> organizationHierarchy)
   {
      if (departmentPath.size() > organizationHierarchy.size())
      {
         throw new PublicException(
               BpmRuntimeError.CLI_INVALID_DEPARTMENT_PATH_PROVIDED.raise());
      }
      List<Department> departments = new LinkedList<Department>();
      Department parent = null;
      for (int i = 0; i < departmentPath.size(); i++)
      {
         String departmentId = departmentPath.get(i);
         Organization org = organizationHierarchy.get(i);
         Department d = queryService.findDepartment(parent, departmentId, org);
         departments.add(d);
         parent = d;
      }
      return departments;
   }

   /**
    * Get the Organization hierarchy for the give participant
    * @param participant - the participant
    * @param scopedOnly - if only scoped organization should be included in the hierarchy
    * @return the organization hierarchy found,or an empty list if no hierarchy can be found
    */
   public List<Organization> getOrganizationHierarchy(Participant participant,
         boolean scopedOnly)
   {
      Organization organization = null;
      if (participant instanceof Organization)
      {
         organization = (Organization) participant;
      }
      else
      {
         organization = getParentOrganization(participant);
      }
      List<Organization> hierarchy = getOrganizationHierarchy(
            new LinkedList<Organization>(), organization, scopedOnly);
      return hierarchy;
   }

   private List<Organization> getOrganizationHierarchy(List<Organization> hierarchy,
         Organization organization, boolean scopedOnly)
   {
      if (organization != null)
      {
         if (organization.definesDepartmentScope() || !scopedOnly)
         {
            hierarchy.add(0, organization);
         }
         Organization parent = getParentOrganization(organization);
         getOrganizationHierarchy(hierarchy, parent, scopedOnly);
      }
      return hierarchy;
   }

   /**
    * Gets the parent organization for the given participant
    * @param participant - the participant
    * @return the parent organization if existent, null otherwise
    * @throws PublicException if a model with multiple parent organizations was found
    */
   public Organization getParentOrganization(Participant participant)
   {
      Organization parent = null;
      List<Organization> superOrganizations = participant.getAllSuperOrganizations();
      if (superOrganizations.size() > 1)
      {
         throw new PublicException(
               BpmRuntimeError.CLI_DEPRECATED_PROCESS_MODEL_ONLY_ONE_PARENT_ORG_ALLOWED
                     .raise());
      }
      if (!superOrganizations.isEmpty())
      {
         parent = superOrganizations.get(0);
      }
      return parent;
   }
}
