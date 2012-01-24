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

import java.io.Serializable;

import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;


/**
 * Class containing information considering the qa workflow
 * 
 * @author holger.prause
 * @version $Revision: $
 */
public interface QualityAssuranceInfo extends Serializable
{
   public static String MONITORED_INSTANCE_OID = "MONITORED_INSTANCE_OID";
   
   public static String FAILED_QUALITY_CONTROL_INSTANCE_OID = "QUALITY_CONTROL_INSTANCE_OID";
   
   /**
    * Get the {@link ActivityInstance} which was in state {@link QualityAssuranceState#IS_QUALITY_ASSURANCE}
    * and has {@link ResultState#FAILED}, null otherwise
    * 
    * @return the failed qa activity instance if any exists - null otherwise
    */
   ActivityInstance getFailedQualityAssuranceInstance();

   /**
    * Get the {@link ActivityInstance} which was in state {@link QualityAssuranceState#QUALITY_ASSURANCE_TRIGGERED}
    * or {@link QualityAssuranceState#IS_REVISED}, null otherwise
    * 
    * @return
    */
   ActivityInstance getMonitoredInstance();
}
