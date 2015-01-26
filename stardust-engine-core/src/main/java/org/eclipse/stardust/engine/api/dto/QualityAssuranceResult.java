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

/**
 * Represents the result of a quality assurance instance resolution
 * @see ActivityInstanceAttributes#setQualityAssuranceResult(QualityAssuranceResult)
 * 
 * @author Holger.Prause
 * @version $Revision: $
 */
public interface QualityAssuranceResult extends Serializable
{
   /**
    * Sets the {@link QualityAssuranceCode} for this result
    * 
    * @param codes - the {@link QualityAssuranceCode} to set
    */
   void setQualityAssuranceCodes(Set<QualityAssuranceCode> codes);
   
   /**
    * Get the {@link QualityAssuranceCode} for this result
    * 
    * @return the {@link QualityAssuranceCode} for this result
    */
   Set<QualityAssuranceCode> getQualityAssuranceCodes();

   /**
    * Set the {@link ResultState} for this resul
    * 
    * @param state - the state to set
    */
   void setQualityAssuranceState(ResultState state);
   
   /**
    * Returns the {@link ResultState} for this result
    * 
    * @return the {@link ResultState} for this result
    */
   ResultState getQualityAssuranceState();
   
   /**
    * When set to true and the {@link ResultState} is set to {@link ResultState#FAILED},
    * the activity instance will be assigned to the performer of the activity instance which 
    * triggered the quality assurance instance
    * @param assignToLastPerformer -if the instance should be assigned to the last activity performer
    */
   void setAssignFailedInstanceToLastPerformer(boolean assignToLastPerformer);
   
   /**
    * Return if failed quality assurance instances should be assigned to the last activity performer
    * of the instance who triggered the quality assurance instance
    * @return if failed quality assurance instances should be assigned to the last activity performer
    */
   boolean isAssignFailedInstanceToLastPerformer();
   
   /**
    * 
    * @author Holger.Prause
    * @version $Revision: $
    */
   public enum ResultState
   {
      /**
       * the activity instance contained errors and they were corrected by the quality assurance manager
       */
      PASS_WITH_CORRECTION,
      /**
       * the activity instance contained no errors
       */
      PASS_NO_CORRECTION,
      /**
       * the activity instance contained errors which must be corrected
       */
      FAILED
   }
}