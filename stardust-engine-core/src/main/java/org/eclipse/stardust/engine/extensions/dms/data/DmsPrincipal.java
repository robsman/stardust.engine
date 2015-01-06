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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.security.Principal;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.api.runtime.UserInfo;


/**
 * A DmsPrincipal represents an identifiable IPP entity; either User, UserGroup or
 * ModelParticipant.
 *
 * It is used to identify the IPP entity as a unique Principal when assigning access
 * control policies for documents or folders.
 *
 * @author roland.stamm
 *
 */
public class DmsPrincipal implements Serializable, Principal
{

   private static final String POSTFIX_OPEN = "[";

   private static final String POSTFIX_CLOSE = "]";

   public static final String IPP_USERGROUP = "ipp-usergroup";

   public static final String IPP_USER = "ipp-user";

   public static final String IPP_PARTICIPANT = "ipp-participant";

   private static final String PREFIX_CLOSE = "}";

   private static final String PREFIX_OPEN = "{";

   private static final long serialVersionUID = 1L;

   private String name;

   /**
    * @param name
    *           principal name (will not be prefixed)
    * @deprecated please use the other constructors which ensure an unique name based on
    *             an IPP entity.
    */
   public DmsPrincipal(String name)
   {
      this.name = name;
   }

   /**
    * Created a DmsPrincipal which identifies a ModelParticipant.
    *
    * @param modelParticipantInfo
    * @param modelId
    */
   public DmsPrincipal(ModelParticipantInfo modelParticipantInfo, String modelId)
   {
      String participantId = modelParticipantInfo.getId();

      String departmentId = null;
      DepartmentInfo department = modelParticipantInfo.getDepartment();
      if (department != null)
      {
         departmentId = department.getId();
      }

      this.name = getModelParticipantPrincipalName(participantId, departmentId, modelId);

   }

   /**
    * Created a DmsPrincipal which identifies a User.
    *
    * @param userInfo
    * @param realmId
    */
   public DmsPrincipal(UserInfo userInfo, String realmId)
   {
      this.name = getUserPrincipalName(userInfo.getId(), realmId);
   }

   /**
    * Created a DmsPrincipal which identifies a UserGroup.
    *
    * @param userGroupInfo
    */
   public DmsPrincipal(UserGroupInfo userGroupInfo)
   {
      this.name = getUserGroupPrincipalName(userGroupInfo.getId());
   }

   /**
    * Builds a unique principal name for a ModelParticipant.
    *
    * @param participantId
    * @param departmentId
    * @param modelId
    * @return The unique name.
    */
   public static String getModelParticipantPrincipalName(String participantId,
         String departmentId, String modelId)
   {
      String name = getAsPrefix(IPP_PARTICIPANT);

      if ( !isEmpty(modelId))
      {
         name += getAsPrefix(modelId);
      }

      name += participantId;

      if (departmentId != null)
      {
         name += POSTFIX_OPEN + departmentId + POSTFIX_CLOSE;
      }

      return name;
   }

   /**
    * Builds a unique principal name for a User.
    * @param userId
    * @param realmId
    * @return The unique name.
    */
   public static String getUserPrincipalName(String userId, String realmId)
   {
      String name = getAsPrefix(IPP_USER) + userId;

      if ( !isEmpty(realmId))
      {
         name += POSTFIX_OPEN + realmId + POSTFIX_CLOSE;
      }

      return name;
   }

   /**
    * Builds a unique principal name for a UserGroup.
    *
    * @param userGroupId
    * @return The unique name.
    */
   public static String getUserGroupPrincipalName(String userGroupId)
   {
      return getAsPrefix(IPP_USERGROUP) + userGroupId;
   }

   /**
    * Returns the string as a valid prefix e.g. "{string}"
    *
    * @param string
    * @return string the string with prefix markers.
    */
   private static String getAsPrefix(String string)
   {
      return PREFIX_OPEN + string + PREFIX_CLOSE;
   }

   public String getName()
   {
      return name;
   }

}
