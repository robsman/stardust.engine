/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl;

/**
 * <p>
 * This is a class other classes can inherit from
 * to become 'lockable' in a static manner.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public abstract class Lockable
{
   private static boolean testEnvLocked = false;
   
   public static void lockTestEnv()
   {
      testEnvLocked = true;
   }

   public static void unlockTestEnv()
   {
      testEnvLocked = false;
   }
   
   protected static boolean testEnvLocked()
   {
      return testEnvLocked;
   }
}
