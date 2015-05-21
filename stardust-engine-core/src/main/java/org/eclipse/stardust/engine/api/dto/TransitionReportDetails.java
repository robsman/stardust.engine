/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.TransitionReport;

public class TransitionReportDetails implements TransitionReport
{
   private static final long serialVersionUID = 1L;
   
   private ActivityInstance sourceActivityInstance;
   private ActivityInstance targetActivityInstance;

   public TransitionReportDetails(ActivityInstance sourceActivityInstance,
         ActivityInstance targetActivityInstance)
   {
      this.sourceActivityInstance = sourceActivityInstance;
      this.targetActivityInstance = targetActivityInstance;
   }

   @Override
   public ActivityInstance getSourceActivityInstance()
   {
      return sourceActivityInstance;
   }

   @Override
   public ActivityInstance getTargetActivityInstance()
   {
      return targetActivityInstance;
   }
}
