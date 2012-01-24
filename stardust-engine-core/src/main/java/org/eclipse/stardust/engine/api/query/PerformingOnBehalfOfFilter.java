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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserGroupInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;


public class PerformingOnBehalfOfFilter implements FilterCriterion
{
   private static final long serialVersionUID = 2L;

   /**
    * Constant marking a model participant filter.
    */
   public static final Kind FILTER_KIND_MODEL_PARTICIPANT = new Kind("MODEL_PARTICIPANT");

   /**
    * Constant marking a user group filter.
    */
   public static final Kind FILTER_KIND_USER_GROUP = new Kind("USER_GROUP");

   /**
    * Discriminator qualifying the semantics of the participant ID.
    */
   private final Kind filterKind;

   /**
    * The participant this filter represents.
    */
   private final ParticipantInfo participant;

   /**
    * Flag indicating evaluation of this filter should traverse the participant hierarchy.
    */
   private final boolean recursively;

   private final Set<ParticipantInfo> contributors;
   
   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo, boolean)}
    */
   public static PerformingOnBehalfOfFilter forModelParticpants(Set<String> participantIDs)
   {
      return new PerformingOnBehalfOfFilter(participantIDs, false /* select deprecated constructor marked with boolean */);
   }
   
   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo, boolean)}
    */
   public static PerformingOnBehalfOfFilter forModelParticpant(String participantID, boolean recursively)
   {
      return new PerformingOnBehalfOfFilter(new LegacyModelParticipant(participantID), recursively);
   }
   
   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)}
    */
   public static PerformingOnBehalfOfFilter forModelParticpant(String participantID)
   {
      return forModelParticpant(participantID, true);
   }
   
   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)}
    */
   public static PerformingOnBehalfOfFilter forUserGroup(String groupID)
   {
      return new PerformingOnBehalfOfFilter(new UserGroupInfoDetails(0, groupID, null), false);
   }
   
   public static PerformingOnBehalfOfFilter forParticipant(ParticipantInfo participant)
   {
      return forParticipant(participant, true);
   }
   
   public static PerformingOnBehalfOfFilter forParticipant(ParticipantInfo participant,
         boolean recursively)
   {
      return new PerformingOnBehalfOfFilter(participant, recursively);
   }
   
   public static PerformingOnBehalfOfFilter forParticipants(
         Set< ? extends ParticipantInfo> participants)
   {
      return new PerformingOnBehalfOfFilter(participants);
   }
   
   private PerformingOnBehalfOfFilter(ParticipantInfo participant, boolean recursively)
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
      else
      {
         throw new IllegalArgumentException(
               "Participant has to be a user group or model participant, but was: "
                     + participant.toString());
      }

      this.participant = participant;

      this.contributors = Collections.EMPTY_SET;
   }
   
   private PerformingOnBehalfOfFilter(Kind filterKind, String participantId, boolean recursively)
   {
      this.filterKind = filterKind;
      this.participant = null;
      
      this.recursively = recursively;
      
      this.contributors = Collections.EMPTY_SET;
   }
   
   private PerformingOnBehalfOfFilter(Set<String> contributors, boolean deprecated)
   {
      this.filterKind = FILTER_KIND_MODEL_PARTICIPANT;

      this.participant = null;
      this.recursively = false;

      this.contributors = new HashSet(contributors.size());
      for (String participantId : contributors)
      {
         this.contributors.add(new LegacyModelParticipant(participantId));
      }
   }
   
   private PerformingOnBehalfOfFilter(Set< ? extends ParticipantInfo> contributors)
   {
      this.filterKind = FILTER_KIND_MODEL_PARTICIPANT;

      this.participant = null;
      this.recursively = false;

      this.contributors = new HashSet(contributors);
   }
   
   public Kind getFilterKind()
   {
      return filterKind;
   }

   public String getParticipantID()
   {
      if (null != participant)
      {
         return participant.getId();
      }
      return null;
   }
   
   public ParticipantInfo getParticipant()
   {
      return participant;
   }

   public boolean isRecursively()
   {
      return recursively;
   }
   
   public Set<ParticipantInfo> getContributors()
   {
      return Collections.unmodifiableSet(contributors);
   }
   
   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
   
   /**
    * Enumeration for participant filter kind definitions.
    */
   public static final class Kind extends StringKey
   {
      private Kind(String tag)
      {
         super(tag, tag);
      }
   }
   
   private static final class LegacyModelParticipant extends ModelParticipantInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyModelParticipant(String id)
      {
         super(0, id, null, false, false, null);
      }
   }

}
