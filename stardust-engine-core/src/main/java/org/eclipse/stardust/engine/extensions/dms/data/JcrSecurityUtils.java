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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.SimpleCredentials;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserParticipantLink;
import org.eclipse.stardust.engine.core.runtime.beans.UserUserGroupLink;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;

/**
 * Utility class handling JCR security related functionality on engine side.
 *
 * @author Roland.Stamm
 */
public class JcrSecurityUtils
{
   private JcrSecurityUtils()
   {
      // utility class
   }

   /**
    * Creates credentials which contain the users role and organization hierarchy and user groups for jcr access control security.
    *
    * @param user The user used for the username and to calculate the corresponding participant hierarchy.
    * @param password a password to be included in the credentials
    * @return The initialized credentials.
    */
   public static SimpleCredentials getCredentialsIncludingParticipantHierarchy(IUser user, String password)
   {
      Set<AuthorizableOrganizationDetails> allModelParticipants = CollectionUtils.newSet();
      // all users having Administrator Role join Jackrabbit 'administrators' group (Jackrabbit constant)
      if (user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE))
      {
         allModelParticipants.add(new AuthorizableOrganizationDetails("administrators"));
      }

      if (user instanceof UserBean)
      {
         UserBean ub = (UserBean) user;
         Iterator<UserParticipantLink> links = ub.getAllParticipantLinks();
         while (links.hasNext())
         {
            UserParticipantLink link = links.next();
            IModelParticipant participant = link.getParticipant();
            if (participant != null)
            {
               IDepartment department = link.getDepartment();

               String modelId = participant.getModel().getId();
               String participantId = participant.getId();
               String departmentId = (null != department) ? department.getId() : null;

               String principalName = DmsPrincipal.getModelParticipantPrincipalName(
                     participantId, departmentId, modelId);
               allModelParticipants.add(new AuthorizableOrganizationDetails(principalName));
               // For backwards compatibility add the participant's plain text id.
               allModelParticipants.add(new AuthorizableOrganizationDetails(
                     participant.getId()));

               // traverse parent organizations
               IDepartment parentDepartment = null;
               if (participant instanceof IOrganization)
               {
                  if (department != null)
                  {
                     parentDepartment = department.getParentDepartment();
                  }
               }
               else
               {
                  // first parent organization of the implicitly scoped role has the same
                  // department.
                  parentDepartment = department;
               }

               IOrganization parentOrg = DepartmentUtils.getParentOrg(participant);

               ModelManager modelManager = ModelManagerFactory.getCurrent();

               while (parentOrg != null)
               {
                  String parentOrgDepartmentId = null;
                  if (parentDepartment != null
                        && DepartmentUtils.isRestrictedModelParticipant(parentOrg))
                  {
                     // there is a parent department somewhere in the parent
                     // organizations hierarchy.
                     long rtOidParentOrg = modelManager.getRuntimeOid(parentOrg);
                     if (rtOidParentOrg == parentDepartment.getRuntimeOrganizationOID())
                     {
                        // parent is scoped
                        parentOrgDepartmentId = parentDepartment.getId();
                     }
                  }

                  String parentOganizationName = DmsPrincipal.getModelParticipantPrincipalName(
                        parentOrg.getId(), parentOrgDepartmentId, modelId);
                  allModelParticipants.add(new AuthorizableOrganizationDetails(
                        parentOganizationName));

                  if (parentOrgDepartmentId != null)
                  {
                     // next parent department
                     parentDepartment = parentDepartment.getParentDepartment();
                  }

                  // next parent organization
                  parentOrg = DepartmentUtils.getParentOrg(parentOrg);
               }

            }

         }
      }

      for (Iterator< ? > i = SecurityProperties.getUser().getAllUserGroupLinks(); i.hasNext();)
      {
         UserUserGroupLink participant = (UserUserGroupLink) i.next();
         IUserGroup userGroup = participant.getUserGroup();

         String principalName = DmsPrincipal.getUserGroupPrincipalName(userGroup.getId());
         allModelParticipants.add(new AuthorizableOrganizationDetails(principalName));
      }

      SimpleCredentials credentials = new SimpleCredentials(
            DmsPrincipal.getUserPrincipalName(user.getId(), user.getRealm().getId()),
            password.toCharArray());
      credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT,
            allModelParticipants);

      return credentials;
   }

}
