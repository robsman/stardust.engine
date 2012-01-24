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
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.IntKey;

/**
 * Wrapper class for supported performer types.
 * It provides human readable names for the performer type codes.
 *
 * @author rsauer
 * @version $Revision$
 */
public class PerformerType extends IntKey
{
   /**
    * No performer.
    */
   public static final int NONE = 0;

   /**
    * User.
    */
   public static final int USER = 1;

   /**
    * Model participant.
    */
   public static final int MODEL_PARTICIPANT = 2;

   /**
    * User group.
    */
   public static final int USER_GROUP = 3;

   public static final PerformerType None = new PerformerType(NONE, "None");

   public static final PerformerType User = new PerformerType(USER, "User");

   public static final PerformerType ModelParticipant = new PerformerType(
         MODEL_PARTICIPANT, "Model participant");

   public static final PerformerType UserGroup = new PerformerType(USER_GROUP,
         "User group");

   private static final PerformerType[] KEYS = new PerformerType[] {
      None,
      User,
      ModelParticipant
   };
   
   public static PerformerType get(int value)
   {
      if ((value < KEYS.length) && (value >= 0) && (null != KEYS[value]))
      {
         return KEYS[value];
      }
      return (PerformerType) getKey(PerformerType.class, value);
   }
   
   private PerformerType(int id, String defaultName)
   {
      super(id, defaultName);
   }
}
