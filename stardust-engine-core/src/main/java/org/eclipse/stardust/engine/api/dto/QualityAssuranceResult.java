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
import java.util.Set;

import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;


public interface QualityAssuranceResult extends Serializable
{
   /**
    * @param codes
    */
   void setQualityAssuranceCodes(Set<QualityAssuranceCode> codes);
   
   /**
    * @return
    */
   Set<QualityAssuranceCode> getQualityAssuranceCodes();

   /**
    * @param passed
    */
   void setQualityAssuranceState(ResultState state);
   
   /**
    * @return
    */
   ResultState getQualityAssuranceState();
   
   void setAssignToLastActivityPerformer(boolean assign);
   
   boolean getAssignToLastActivityPerformer();
   
   public enum ResultState
   {
      PASS_WITH_CORRECTION,
      PASS_NO_CORRECTION,
      FAILED
   }
}