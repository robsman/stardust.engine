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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.cli.common.DepartmentClientUtils;


public abstract class DepartmentCommand extends ConsoleCommand
{
   private Options argTypes = new Options();
   public static final String DEPARTMENT_PATH_OPTION_KEY = "departmentpath";

   protected DepartmentCommand()
   {
      String longname = "departmentpath";
      String shortname = "-dp";
      String keyname = DepartmentCommand.DEPARTMENT_PATH_OPTION_KEY;
      String summary = "The Path to the department";
      boolean hasArg = true;
      argTypes.register(longname, shortname, keyname, summary, hasArg);
      argTypes.addMandatoryRule(keyname);
   }

   protected DepartmentCommandConfig getConfig(Map options)
   {
      DepartmentCommandConfig config = new DepartmentCommandConfig();
      String userName = (String) globalOptions.get("user");
      String passWord = (String) globalOptions.get("password");

      // userName and password have no mandatory rule set - so do validation here
      if (StringUtils.isEmpty(userName))
      {
         throw new IllegalArgumentException("the global option user must be provided");
      }
      if (StringUtils.isEmpty(passWord))
      {
         throw new IllegalArgumentException("the global option password must be provided");
      }

      String departmentPath = (String) options.get(DEPARTMENT_PATH_OPTION_KEY);
      DepartmentClientUtils dh = DepartmentClientUtils.getInstance(globalOptions);
      List<String> parsedDepartmentPath = dh.parseDepartmentPath(departmentPath);
      if (parsedDepartmentPath.isEmpty())
      {
         throw new PublicException(
               BpmRuntimeError.CLI_INVALID_DEPARTMENT_PATH_PROVIDED.raise());
      }

      config.setDepartmentPath(parsedDepartmentPath);
      config.setUserName(userName);
      config.setPassWord(passWord);
      return config;
   }

   protected class DepartmentCommandConfig
   {
      private String userName;
      private String passWord;
      private List<Organization> organizationHierarchy = new LinkedList<Organization>();
      private List<String> departmentPath = new LinkedList<String>();

      public String getUserName()
      {
         return userName;
      }

      public void setUserName(String userName)
      {
         this.userName = userName;
      }

      public String getPassWord()
      {
         return passWord;
      }

      public void setPassWord(String passWord)
      {
         this.passWord = passWord;
      }

      public List<Organization> getOrganizationHierarchy()
      {
         return organizationHierarchy;
      }

      public void setOrganizationHierarchy(List<Organization> organizationHierarchy)
      {
         this.organizationHierarchy = organizationHierarchy;
      }

      public List<String> getDepartmentPath()
      {
         return departmentPath;
      }

      public void setDepartmentPath(List<String> departmentPath)
      {
         this.departmentPath = departmentPath;
      }
   }

   @Override
   public Options getOptions()
   {
      return argTypes;
   }
}
