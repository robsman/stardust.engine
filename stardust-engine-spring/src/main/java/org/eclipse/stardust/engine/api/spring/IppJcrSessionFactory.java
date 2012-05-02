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
package org.eclipse.stardust.engine.api.spring;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IScopedModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.UserUserGroupLink;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;
import org.eclipse.stardust.vfs.impl.jcr.AuthorizableOrganizationDetails;
import org.eclipse.stardust.vfs.jcr.spring.JcrSpringSessionFactory;

public class IppJcrSessionFactory extends JcrSpringSessionFactory
{

   public Session createSession() throws RepositoryException
   {
      String jcrPasswordDummy = "ipp-jcr-password";
      // code copy: see
      // org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment.getIppJcrCredentials()

      IUser user = SecurityProperties.getUser();

      Set<AuthorizableOrganizationDetails> allModelParticipants = CollectionUtils.newSet();
      // all users having Administrator Role join Jackrabbit 'administrators' group
      if (user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE))
      {
         allModelParticipants.add(new AuthorizableOrganizationDetails("administrators"));
      }

      for (Iterator i = SecurityProperties.getUser().getAllParticipants(); i.hasNext();)
      {
         IModelParticipant participant = (IModelParticipant) i.next();

         String participantId = participant.getId();

         String departmentId = null;
         if (participant instanceof IScopedModelParticipant)
         {
            IDepartment department = ((IScopedModelParticipant) participant).getDepartment();
            if (department != null)
            {
               departmentId = department.getId();
            }
         }
         String modelId = participant.getModel().getId();

         String principalName = DmsPrincipal.getModelParticipantPrincipalName(
               participantId, departmentId, modelId);
         allModelParticipants.add(new AuthorizableOrganizationDetails(principalName));
         // For backwards compatibility.
         allModelParticipants.add(new AuthorizableOrganizationDetails(participant.getId()));
      }

      for (Iterator i = SecurityProperties.getUser().getAllUserGroupLinks(); i.hasNext();)
      {
         UserUserGroupLink participant = (UserUserGroupLink) i.next();
         IUserGroup userGroup = participant.getUserGroup();

         String principalName = DmsPrincipal.getUserGroupPrincipalName(userGroup.getId());
         allModelParticipants.add(new AuthorizableOrganizationDetails(principalName));
      }

      SimpleCredentials credentials = new SimpleCredentials(
            DmsPrincipal.getUserPrincipalName(user.getId(), user.getRealm().getId()),
            jcrPasswordDummy.toCharArray());
      credentials.setAttribute(AuthorizableOrganizationDetails.DIRECT_GROUPS_ATT,
            allModelParticipants);

      Session session = getRepository().login(credentials, getWorkspaceName());

      return session;
   }

}
