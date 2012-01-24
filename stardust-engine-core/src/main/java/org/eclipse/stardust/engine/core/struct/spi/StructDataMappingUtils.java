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
package org.eclipse.stardust.engine.core.struct.spi;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.ImplementationType;

public class StructDataMappingUtils
{
   private static final String VISUAL_RULES_APP_TYPE_ID = "visualRulesEngineBean";

   // TODO: Move this information to VisualRules specific packages? 
   public static boolean isVizRulesApplication(IActivity activity)
   {
      if (null != activity
            && ImplementationType.Application == activity.getImplementationType()
            && !activity.isInteractive()
            && null != activity.getApplication().getType()
            && VISUAL_RULES_APP_TYPE_ID.equals(activity.getApplication().getType()
                  .getId()))
      {
         return true;
      }

      return false;
   }
   
   private StructDataMappingUtils()
   {
      // utility class
   }

}
