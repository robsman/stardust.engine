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
package org.eclipse.stardust.engine.core.model.utils;

import org.eclipse.stardust.engine.api.model.ImplementationType;

public class ActivityReportUtils
{
   // utility class
   private ActivityReportUtils()
   {
   }
   
   public static boolean isAuxiliaryActivity(Boolean auxiliaryAttr, ImplementationType implType)
   {
      boolean auxiliaryActivity = false;
      if(auxiliaryAttr != null)
      {
         auxiliaryActivity = auxiliaryAttr.booleanValue();
      }
      else
      {
         auxiliaryActivity = ImplementationType.Route.equals(implType);
      }
      return auxiliaryActivity;
   }
}
