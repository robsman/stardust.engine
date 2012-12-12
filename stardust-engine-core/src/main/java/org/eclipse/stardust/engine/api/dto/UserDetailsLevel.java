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

public class UserDetailsLevel extends IntKey
{
   private static final long serialVersionUID = 1L;

   /**
   * The user details only contain first level attributes of UserBean.
   */
   public static final int CORE = 1;

   /**
    * The user details contain same attributes as with CORE plus properties.
    */
   public static final int WITH_PROPERTIES = 2;

   /**
    * The user details contain same attributes as with WITH_PROPERTIES plus all grants.
    */
   public static final int FULL = 3;

   public static final UserDetailsLevel Core = new UserDetailsLevel(CORE, "Core");
   public static final UserDetailsLevel WithProperties = new UserDetailsLevel(
         WITH_PROPERTIES, "WithProperties");
   public static final UserDetailsLevel Full = new UserDetailsLevel(FULL, "Full");

   /**
    * Factory method to get the ProcessInstanceState corresponding to the given code.
    *
    * @param value one of the ProcessInstanceState codes.
    *
    * @return one of the predefined ProcessInstanceStates or null if it's an invalid code.
    */
   public static UserDetailsLevel getlevel(int value)
   {
      return (UserDetailsLevel) getKey(UserDetailsLevel.class, value);
   }

   public static final String PRP_USER_DETAILS_LEVEL = "USER_DETAILS_LEVEL";
   public static final String PRP_USER_DETAILS_PREFERENCES = "PRP_USER_DETAILS_PREFERENCES";

   private UserDetailsLevel(int value, String name)
   {
      super(value, name);
   }

   protected Object readResolve()
   {
      return super.readResolve();
   }
}
