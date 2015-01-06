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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BpmRuntimeErrorMessages
{
   private static final String BUNDLE_NAME = "org.eclipse.stardust.engine.api.runtime.ipp-bpm-runtime-errors"; //$NON-NLS-1$
   private static ResourceBundle resourceBundle;
   
   private BpmRuntimeErrorMessages()
   {
   }

   public static String getString(String key)
   {
      try
      {
         if(resourceBundle == null)
         {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
         }
         
         return resourceBundle.getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
   
   
   
   
}
