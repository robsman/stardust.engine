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

package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.engine.api.runtime.UserGroup;


// TODO: harmonize/reuse UserGroupDetailsLevel with UserDetailsLevel, ProcessInstancedetailsLevel 
/**
 * Represents the level of detail for a {@link UserGroup}.
 * 
 * @author born
 */
public class UserGroupDetailsLevel extends IntKey
{
   private static final long serialVersionUID = 1L;

   public static final String PRP_DETAILS_LEVEL = UserGroupDetailsLevel.class.getName()
         + ".Level";

   /**
   * The details object only contains first level attributes of the engine side object.
   */
   public static final int CORE = 1;

   /**
    * The details details contains same attributes as with CORE plus all extended attributes.
    */
   public static final int FULL = Integer.MAX_VALUE;

   public static final UserGroupDetailsLevel Core = new UserGroupDetailsLevel(CORE,
         "Core");

   public static final UserGroupDetailsLevel Full = new UserGroupDetailsLevel(FULL,
         "Full");

   /**
    * Factory method to get the DetailsLevel corresponding to the given code.
    *
    * @param value one of the DetailsLevel codes.
    *
    * @return one of the predefined DetailsLevel or null if it's an invalid code.
    */
   public static UserGroupDetailsLevel getlevel(int value)
   {
      return (UserGroupDetailsLevel) getKey(UserGroupDetailsLevel.class, value);
   }

   private UserGroupDetailsLevel(int value, String name)
   {
      super(value, name);
   }
}
