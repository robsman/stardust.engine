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

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;

public class QualityAssuranceInfoImpl implements QualityAssuranceInfo
{
   private ActivityInstance failedQaInstance;
   private ActivityInstance monitoredInstance;
   
   public QualityAssuranceInfoImpl(ActivityInstance failedQaInstance, ActivityInstance monitoredInstance)
   {
      this.failedQaInstance = failedQaInstance;
      this.monitoredInstance = monitoredInstance;
   }

   public ActivityInstance getFailedQualityAssuranceInstance()
   {
      return failedQaInstance;
   }

   public ActivityInstance getMonitoredInstance()
   {
      return monitoredInstance;
   }

}
