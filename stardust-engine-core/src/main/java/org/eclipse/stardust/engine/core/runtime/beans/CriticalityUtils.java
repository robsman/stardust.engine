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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ICriticalityEvaluator;


public class CriticalityUtils
{

   private static final String KEY_RT_ENV_CRITICALITY_EVALUATOR = CriticalityUtils.class.getName()
         + ".CriticalityEvaluator";

   public static double recalculateCriticality(long aiOid)
   {

      GlobalParameters globals = GlobalParameters.globals();
      ICriticalityEvaluator evaluator = (ICriticalityEvaluator) globals.get(KEY_RT_ENV_CRITICALITY_EVALUATOR);

      if (evaluator == null)
      {
         
      }
      
      
      return 0;
   }

}
