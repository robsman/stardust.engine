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
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;

/**
 * This class contains attributes for an activity instance. 
 * 
 * @author barry.grotjahn
 */
public interface ActivityInstanceAttributes extends Serializable
{
   /**
    * Return the oid for the activity instance this attributes belongs to
    * 
    * @return the oid of the corresponding activity instance
    */
   long getActivityInstanceOid();
   
   /**
    * Adds a {@link Note} for the given text
    * @param text
    * @return the note created
    */
   Note addNote(String text);
   
   /**
    * Gets the notes which were currently added
    * for this instance.See {@link ActivityInstanceAttributes#addNote(String)}
    * @return the current added notes for this instance
    */
   List<Note> getAddedNotes();
   
   /**
    * Gets the {@link Note} set for this activity instance
    * @return the notes set for this instance
    * 
    */
   List<Note> getNotes();
   
   /**
    * Sets notes to this instance 
    * @param notes - the notes to set for this instance
    */
   void setNotes(List<Note> notes);
   /**
    * Sets the {@link QualityAssuranceResult} for a specific activity instance.
    * 
    * @param result - the result object to set
    */
   void setQualityAssuranceResult(QualityAssuranceResult result);
   
   /**
    * Gets the {@link QualityAssuranceResult} for a specific activity instance.
    * This will be null for non quality assurance activity instances 
    * ({@link ActivityInstance#getQualityAssuranceState()} != {@link QualityAssuranceState#IS_QUALITY_ASSURANCE}).
    * or a newly created quality assurance instance.
    * @return the {@link QualityAssuranceResult}
    */
   QualityAssuranceResult getQualityAssuranceResult();
}