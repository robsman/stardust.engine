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

import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserGroupInfoDetails;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;


/**
 * Class for filtering participants 
 * @see ParticipantInfo
 * @see Participant
 * 
 * @author rsauer
 * @version $Revision$
 */
public class PerformingParticipantFilter implements FilterCriterion
{
   private static final long serialVersionUID = 2L;

   /**
    * Constant marking the special {@link #ANY_FOR_USER} filter.
    */
   public static final Kind FILTER_KIND_ANY_FOR_USER = new Kind("ANY_FOR_USER");

   /**
    * Constant marking a model participant filter.
    */
   public static final Kind FILTER_KIND_MODEL_PARTICIPANT = new Kind("MODEL_PARTICIPANT");

   /**
    * Constant marking a user group filter.
    */
   public static final Kind FILTER_KIND_USER_GROUP = new Kind("USER_GROUP");

   /**
    * Filter for retrieving items performed by any participant (roles, organizations or
    * user groups) associated with the calling user. Finding such participants will
    * perform a deep search.
    */
   public static final PerformingParticipantFilter ANY_FOR_USER =
         new PerformingParticipantFilter(FILTER_KIND_ANY_FOR_USER, null, true);

   /**
    * @deprecated Replaced by {@link #ANY_FOR_USER}.
    */
   public static final PerformingParticipantFilter USER_ROLES_AND_ORGANIZATIONS =
         ANY_FOR_USER;

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

   private final Set<IParticipant> contributors;

   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)}
    */
   public static PerformingParticipantFilter forModelParticipant(String participantID)
   {
      return forModelParticipant(participantID, true);
   }

   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo, boolean)}
    */
   public static PerformingParticipantFilter forModelParticipant(String participantID,
         boolean recursively)
   {
      return new PerformingParticipantFilter(new LegacyModelParticipant(participantID),
            recursively);
   }
   
   /**
    * @deprecated Superseded by {@link #forParticipant(ParticipantInfo)}
    */
   public static PerformingParticipantFilter forUserGroup(String groupID)
   {
      return new PerformingParticipantFilter(new UserGroupInfoDetails(0, groupID, null),
            false);
   }

   /**
    * Gets the {@link PerformingParticipantFilter} for the given arguments.
    * All sub participants are also included.
    * 
    * @param participant - the participant for which the filter should be constructed
    * @return the constructed {@link PerformingParticipantFilter}
    */
   public static PerformingParticipantFilter forParticipant(ParticipantInfo participant)
   {
      return forParticipant(participant, true);
   }
   
   /**
    * Gets the {@link PerformingParticipantFilter} for the given arguments
    * 
    * @param participant - the participant for which the filter should be constructed
    * @param recursively - if sub participant should be included
    * @return the constructed {@link PerformingParticipantFilter}
    */
   public static PerformingParticipantFilter forParticipant(ParticipantInfo participant,
         boolean recursively)
   {
      return new PerformingParticipantFilter(participant, recursively);
   }
   
   /**
    * @deprecated Use {@link #forModelParticipant(String)} instead.
    */
   public PerformingParticipantFilter(String participantID)
   {
      this(participantID, true);
   }

   /**
    * @deprecated Use {@link #forModelParticipant(String, boolean)} instead.
    */
   public PerformingParticipantFilter(String participantID, boolean recursively)
   {
      this(new LegacyModelParticipant(participantID), recursively);
   }
   
   private PerformingParticipantFilter(ParticipantInfo participant, boolean recursively)
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
   
   /**
    * @deprecated 
    */
   private PerformingParticipantFilter(Kind filterKind, final String participantID,
         boolean recursively)
   {
      this.filterKind = filterKind;
      this.participant = null;
      
      this.recursively = recursively;
      
      this.contributors = Collections.EMPTY_SET;
   }
   
   protected PerformingParticipantFilter(Set< ? extends IParticipant> contributors)
   {
      this.filterKind = FILTER_KIND_MODEL_PARTICIPANT;

      this.participant = null;
      this.recursively = false;

      this.contributors = new HashSet(contributors);
   }
   
   /**
    * Gets the {@link Kind} of the filter
    * Also see 
    * @see PerformingParticipantFilter#FILTER_KIND_ANY_FOR_USER
    * @see PerformingParticipantFilter#FILTER_KIND_USER_GROUP
    * @see PerformingParticipantFilter#FILTER_KIND_MODEL_PARTICIPANT
    * 
    * @return the filters {@link Kind}
    */
   public Kind getFilterKind()
   {
      return filterKind;
   }

   /**
    * Returns the id for the participant
    * @return the id for the participant, can be null
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
    * Returns the {@link ParticipantInfo} for the participant 
    * @return the {@link ParticipantInfo} for the participant, can be null
    */
   public ParticipantInfo getParticipant()
   {
      return participant;
   }

   /** Returns if this filter should 
    *  include all sub participants
    * @return true if all sub participant should be considered,
    *         false otherwise
    * */
   public boolean isRecursively()
   {
      return recursively;
   }
   
   protected Set<IParticipant> getContributors()
   {
      return Collections.unmodifiableSet(contributors);
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   protected Object readResolve() throws ObjectStreamException
   {
      Object me = this;
      if (FILTER_KIND_ANY_FOR_USER.equals(filterKind))
      {
         me = USER_ROLES_AND_ORGANIZATIONS;
      }
      return me;
   }
   
   /**
    * Enumeration for participant filter kind definitions.
    *
    * @author rsauer
    * @version $Revision$
    */
   public static final class Kind extends StringKey
   {
      private static final long serialVersionUID = 4422645792430167111L;

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
