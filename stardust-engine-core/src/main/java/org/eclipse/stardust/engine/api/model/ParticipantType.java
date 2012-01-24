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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.IntKey;

/**
 * Indicates all supported concrete types of participants as well as some meta-types like
 * model participants or kinds of conditional performers.
 *
 * @author rsauer
 * @version $Revision$
 */
public class ParticipantType extends IntKey
{
   /*
    *  Meta Type Bits are:
    *
    *  Bit 0 - Flag marking model participants
    *  Bit 1 - Flag marking dynamic participants
    *  
    *  Bits for Model Participants are:
    *  
    *  Bit 4 - Flag marking Organizations
    *  Bit 5 - Flag marking Roles
    *  Bit 6 - Flag marking Conditional Performers
    *  
    *  Bits for Dynamic Participants are:
    *  
    *  Bit 16 - Flag marking Users
    *  Bit 17 - Flag marking User Groups
    */
   private static final int FLAG_MODEL_PARTICIPANT = 1 << 0;
   private static final int FLAG_DYNAMIC_PARTICIPANT = 1 << 1;

   private static final int FLAG_ORGANIZATION = 1 << 4;
   private static final int FLAG_ROLE = 1 << 5;
   private static final int FLAG_CONDITIONAL_PERFORMER = 1 << 6;
   
   private static final int FLAG_USER = 1 << 16;
   private static final int FLAG_USER_GROUP = 1 << 17;

   public static final ParticipantType ModelParticipant = new ParticipantType(
         FLAG_MODEL_PARTICIPANT, "Model Participant");
   public static final ParticipantType DynamicParticipant = new ParticipantType(
         FLAG_DYNAMIC_PARTICIPANT, "Dynamic Participant");

   public static final ParticipantType Organization = new ParticipantType(
         FLAG_MODEL_PARTICIPANT + FLAG_ORGANIZATION, "Organization");
   public static final ParticipantType Role = new ParticipantType(FLAG_MODEL_PARTICIPANT
         + FLAG_ROLE, "Role");
   public static final ParticipantType ConditionalPerformer = new ParticipantType(
         FLAG_MODEL_PARTICIPANT + FLAG_CONDITIONAL_PERFORMER, "Conditional Performer");

   public static final ParticipantType User = new ParticipantType(FLAG_DYNAMIC_PARTICIPANT
         + FLAG_USER, "User");
   public static final ParticipantType UserGroup = new ParticipantType(
         FLAG_DYNAMIC_PARTICIPANT + FLAG_USER_GROUP, "User Group");

   public static final ParticipantType ModelParticipantOrUserGroup = new ParticipantType(
         FLAG_MODEL_PARTICIPANT + FLAG_DYNAMIC_PARTICIPANT + FLAG_USER_GROUP,
         "Model Participant or User Group");

   private ParticipantType(int id, String name)
   {
      super(id, name);
   }
}
