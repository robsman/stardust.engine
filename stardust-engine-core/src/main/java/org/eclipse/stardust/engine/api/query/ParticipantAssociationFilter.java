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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.dto.RoleInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserGroupInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.api.runtime.UserInfo;


/**
 * Filter criterion for restricting {@link UserQuery}s producing users having granted
 * specific roles/organizations or being members of specific user groups.
 * <p />
 * A usage examples is to retrieve all users being administrators.
 * @author sborn
 * @version $Revision$
 * 
 * @since 3.1
 */
public class ParticipantAssociationFilter implements FilterCriterion
{
   private static final long serialVersionUID = 2L;

   /**
    * Constant marking a model participant grant filter.
    */
   public static final Kind FILTER_KIND_MODEL_PARTICIPANT = new Kind("MODEL_PARTICIPANT");

   /**
    * Constant marking a user group membership filter.
    */
   public static final Kind FILTER_KIND_USER_GROUP = new Kind("USER_GROUP");
   
   /**
    * Constant marking a user filter.
    */
   public static final Kind FILTER_KIND_USER = new Kind("USER");
   
   /**
    * Constant marking a team leader filter.
    */
   public static final Kind FILTER_KIND_TEAM_LEADER = new Kind("TEAM_LEADER");
   
   /**
    * Constant marking a department filter.
    */
   public static final Kind FILTER_KIND_DEPARTMENT = new Kind("DEPARTMENT");
   
   private final Kind filterKind;
   
   /**
    * The participant this filter represents.
    */
   private ParticipantInfo participant;

   /**
    * Flag indicating evaluation of this filter should traverse the participant hierarchy.
    */
   private final boolean recursively;

   /**
    * Constructs a filter criterion matching exactly the given model participant.
    *
    * @param participantID The ID of the model participant to filter for.
    * 
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)
    */
   public static ParticipantAssociationFilter forModelParticipant(String participantID)
   {
      return forParticipant(new LegacyModelParticipant(participantID), false);
   }

   /**
    * Constructs a filter criterion matching the closure of the given model participant.
    * The closure will be calculated according to worklist rules.
    * 
    * @param participantID The ID of the model participant to filter for.
    * 
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo, boolean)
    */
   public static ParticipantAssociationFilter forModelParticipant(String participantID,
         boolean recursively)
   {
      return new ParticipantAssociationFilter(new LegacyModelParticipant(participantID),
            recursively);
   }

   /**
    * Constructs a filter criterion matching the user group identified by the given id.
    *
    * @param groupID The ID of the user group to filter for.
    * 
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)
    */
   public static ParticipantAssociationFilter inUserGroup(String groupID)
   {
      return new ParticipantAssociationFilter(new LegacyUserGroup(groupID), false);
   }

   /**
    * Constructs a filter criterion matching the user identified by the given account.
    *
    * @param account The account of the user to filter for.
    * 
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)}
    */
   public static ParticipantAssociationFilter forUser(String account)
   {
      return new ParticipantAssociationFilter(new LegacyUser(account), false);
   }

   /**
    * Constructs a filter criterion matching the team leader role identified by the given id.
    *
    * @param account The account of the user to filter for.
    * 
    * @deprecated Superseded by {@link #forTeamLeader(RoleInfo)}
    */
   public static ParticipantAssociationFilter forTeamLeader(String roleId)
   {
      return new ParticipantAssociationFilter(FILTER_KIND_TEAM_LEADER, new LegacyRole(
            roleId), true);
   }
   
   /**
    * Constructs a filter criterion matching the participant identified by the given
    * {@link ParticipantInfo}.
    *
    * @param participant The participant to filter for.
    * 
    */
   public static ParticipantAssociationFilter forParticipant(ParticipantInfo participant)
   {
      return forParticipant(participant, true);
   }
   
   /**
    * Constructs a filter criterion matching the participant identified by the given
    * {@link ParticipantInfo}.
    *
    * @param participant The participant to filter for.
    * @param recursively Flag to determine if evaluation of this filter 
    * should traverse the participant hierarchy.
    * 
    */
   public static ParticipantAssociationFilter forParticipant(ParticipantInfo participant,
         boolean recursively)
   {
      return new ParticipantAssociationFilter(participant, recursively);
   }
   
   /**
    * Constructs a filter criterion matching the team leader role identified by the given 
	* {@link RoleInfo}.
    *
    * @param role The team leader role to filter for.
    */
   public static ParticipantAssociationFilter forTeamLeader(RoleInfo role)
   {
      return new ParticipantAssociationFilter(FILTER_KIND_TEAM_LEADER, role, true);
   }
   
   /**
    * Constructs a filter criterion matching the department identified by the given {@link DepartmentInfo} instance.
    * 
    * @param department The department users have to assigned to. May be null which means the default department.
    */
   public static ParticipantAssociationFilter forDepartment(DepartmentInfo department)
   {
      return new ParticipantAssociationFilter(FILTER_KIND_DEPARTMENT,
            new LegacyModelParticipant(department), false);
   }
   
   protected ParticipantAssociationFilter(ParticipantInfo participant, boolean recursively)
   {
      if (null == participant)
      {
         throw new IllegalArgumentException("participant is not allowed to be null.");
      }

      if (participant instanceof ModelParticipantInfo)
      {
         this.filterKind = FILTER_KIND_MODEL_PARTICIPANT;
         this.recursively = recursively;
      }
      else if (participant instanceof UserGroupInfo)
      {
         this.filterKind = FILTER_KIND_USER_GROUP;

         // UserGroups cannot be evaluated recursively
         this.recursively = false;
      }
      else if(participant instanceof UserInfo)
      {
         this.filterKind = FILTER_KIND_USER;

         // UserGroups cannot be evaluated recursively
         this.recursively = false;
      }
      else
      {
         throw new IllegalArgumentException(
               "Participant has to be a user group or model participant, but was: "
                     + participant.toString());
      }

      this.participant = participant;
   }
   
   private ParticipantAssociationFilter(Kind filterKind, ParticipantInfo participant,
         boolean recursively)
   {
      this(filterKind, participant.getId(), recursively);
      this.participant = participant;
   }

   
   protected ParticipantAssociationFilter(Kind filterKind, String participantID,
         boolean recursively)
   {
      this.filterKind = filterKind;
      this.participant = null;
      this.recursively = recursively;
   }
   
   /**
    * Retrieves the {@link Kind} of the filter.
    *
    * @return The filter kind.
    */
   public Kind getFilterKind()
   {
      return filterKind;
   }
   
   /**
    * Retrieves the ID of the participant to filter for.
    *
    * @return The participant ID.
    */
   public String getParticipantID()
   {
      if (null != participant)
      {
         return participant.getId();
      }
      return null;
   }
   
   /**
    * Retrieves the {@link ParticipantInfo} to filter for.
    *
    * @return The participant ID.
    */
   public ParticipantInfo getParticipant()
   {
      return participant;
   }

   /**
    * Retrieves whether this filter will match a participant hierarchy or just a specific
    * participant.
    *
    * @return <code>true</code> if this filter matches a participant hierarchy,
    *         <code>false</code> otherwise.
    */
   public boolean isRecursively()
   {
      return recursively;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   /**
    * Enumeration for participant grant filter kind definitions.
    *
    * @author sborn
    * @version $Revision$
    */
   public static final class Kind extends StringKey
   {
      private static final long serialVersionUID = 4401000892927465472L;

      private Kind(String tag)
      {
         super(tag, tag);
      }
   }
   
   protected static final class LegacyModelParticipant extends ModelParticipantInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyModelParticipant(String id)
      {
         super(0, id, null, false, false, null);
      }

      public LegacyModelParticipant(DepartmentInfo department)
      {
         super(0, department.getId(), department.getName(), false, false, department);
      }
   }
   
   private static final class LegacyRole extends RoleInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyRole(String id)
      {
         super(0, id, null, false, false, null);
      }
   }
   
   private static final class LegacyUserGroup extends UserGroupInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyUserGroup(String id)
      {
         super(0, id, null);
      }
   }
   
   private static final class LegacyUser extends UserInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyUser(String id)
      {
         super(0, id, null);
      }
   }
}
