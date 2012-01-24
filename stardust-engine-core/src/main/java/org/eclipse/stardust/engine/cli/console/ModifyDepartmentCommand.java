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

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.cli.common.DepartmentClientUtils;


public abstract class ModifyDepartmentCommand extends DepartmentCommand
{
   private Options argTypes = new Options();
   public static final String PARTICIPANT_OPTION_KEY = "participantid";
   public static final String RECURSIVE_OPTION_KEY = "recursive";

   public ModifyDepartmentCommand()
   {
      argTypes = super.getOptions();

      // register the id of the organization option
      String longname = "-participantid";
      String shortname = "-pid";
      String keyname = PARTICIPANT_OPTION_KEY;
      String summary = "The id of the participant";
      boolean hasArg = true;
      argTypes.register(longname, shortname, keyname, summary, hasArg);
      argTypes.addMandatoryRule(keyname);

      longname = "-recursive";
      shortname = "-r";
      keyname = RECURSIVE_OPTION_KEY;
      summary = "If specified, the command action is performed on all path elements";
      hasArg = false;
      argTypes.register(longname, shortname, keyname, summary, hasArg);
   }

   protected class ModifyDepartmentCommandConfig extends DepartmentCommandConfig
   {
      private Organization organization;
      private boolean recursive = false;

      public Organization getOrganization()
      {
         return organization;
      }

      public void setOrganization(Organization organization)
      {
         this.organization = organization;
      }

      public boolean isRecursive()
      {
         return recursive;
      }

      public void setRecursive(boolean recursive)
      {
         this.recursive = recursive;
      }
   }

   @Override
   protected ModifyDepartmentCommandConfig getConfig(Map options)
   {
      DepartmentCommandConfig template = super.getConfig(options);
      ModifyDepartmentCommandConfig config = new ModifyDepartmentCommandConfig();
      config.setUserName(template.getUserName());
      config.setPassWord(template.getPassWord());
      config.setDepartmentPath(template.getDepartmentPath());

      String participantId = (String) options.get(PARTICIPANT_OPTION_KEY);
      boolean recursive = false;
      if (options.containsKey(RECURSIVE_OPTION_KEY))
      {
         recursive = true;
      }

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(config.getUserName(),
            config.getPassWord());
      WorkflowService workflowService = serviceFactory.getWorkflowService();

      DeployedModel model = workflowService.getModel();
      Participant participant = model.getParticipant(participantId);
      if (participant == null)
      {
         throw new PublicException("Organisation: " + participantId + " no found");
      }
      
      
      DepartmentClientUtils dh = DepartmentClientUtils.getInstance(globalOptions);
      List<Organization> organizationHierarchy = dh.getOrganizationHierarchy(participant,
            true);
      List<String> departmentPath = config.getDepartmentPath();
      if (departmentPath.size() != organizationHierarchy.size())
      {
         throw new PublicException("Invalid Department path provided");
      }

      int lastElementIndex = organizationHierarchy.size() - 1;
      Organization targetOrganization = organizationHierarchy.get(lastElementIndex);

      config.setOrganization(targetOrganization);
      config.setOrganizationHierarchy(organizationHierarchy);
      config.setRecursive(recursive);
      return config;
   }
}
