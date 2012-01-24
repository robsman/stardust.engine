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
 * A representation of the state of a permission.
 *
 * This class also provides human readable values for the permission states.
 *
 * @author fherinean
 * @version $Revision: 28303 $
 */
public class PermissionState extends IntKey
{
   private static final long serialVersionUID = 1L;

   /**
    * The permission state is unknown.
    */
   public static final int UNKNOWN = 0;

   /**
    * The permission is granted to the current user.
    */
   public static final int GRANTED = 1;

   /**
    * The permission is denied to the current user.
    */
   public static final int DENIED = 2;

   /**
    * The permission state is unknown.
    */
   public static final PermissionState Unknown =
         new PermissionState(UNKNOWN, "Unknown");

   /**
    * The permission is granted to the current user.
    */
   public static final PermissionState Granted =
         new PermissionState(GRANTED, "Granted");

   /**
    * The permission is denied to the current user.
    */
   public static final PermissionState Denied =
         new PermissionState(DENIED, "Denied");

   private static final PermissionState[] KEYS = new PermissionState[]
   {
      Unknown,
      Granted,
      Denied,
   };
   
   private PermissionState(int value, String name)
   {
      super(value, name);
   }

   /**
    * Gets the name of the PermissionState corresponding to the given code.
    *
    * @param value one of the PermissionState codes.
    *
    * @return the name of the corresponding PermissionState.
    */
   // todo: (france, fh) remove / inline.
   public static String getString(int value)
   {
      PermissionState state = getState(value);
      return state == null ? null : state.toString();
   }

   /**
    * Factory method to get the PermissionState corresponding to the given code.
    *
    * @param value one of the PermissionState codes.
    *
    * @return one of the predefined PermissionStates or null if it's an invalid code.
    */
   public static PermissionState getState(int value)
   {
      return value >= 0 && value < KEYS.length ? KEYS[value] : null;
   }
}
