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

package org.eclipse.stardust.engine.api.query;

import java.util.EnumSet;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;


/**
 * Evaluation policy affecting the level of details for process instances.
 * 
 * @author born
 *
 */
public class ProcessInstanceDetailsPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 2L;
   
   private ProcessInstanceDetailsLevel level;
   private EnumSet<ProcessInstanceDetailsOptions> options;

   public ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel level)
   {
      this(level, EnumSet.noneOf(ProcessInstanceDetailsOptions.class));
   }
   
   public ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel level,
         EnumSet<ProcessInstanceDetailsOptions> options)
   {
      this.level = level;
      this.options = options.clone();
   }
   
   /**
    * @return the set of details options. Altering the returned set will alter the policy as well.
    */
   public EnumSet<ProcessInstanceDetailsOptions> getOptions()
   {
      return options;
   }

   public ProcessInstanceDetailsLevel getLevel()
   {
      return level;
   }
}
