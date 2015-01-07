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

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for {@link ActivityInstanceAttributes}
 * 
 * @author holger.prause
 * @version $Revision: $
 */
public class ActivityInstanceAttributesImpl implements ActivityInstanceAttributes
{
   /**
    * 
    */
   private static final long serialVersionUID = 2658206715507967559L;

   private final long aiOid;

   private QualityAssuranceResult result;

   private List<Note> notes = new ArrayList<Note>();
   
   private List<Note> addedNotes = new ArrayList<Note>();
   
   
   public ActivityInstanceAttributesImpl(long aiOid)
   {
      this.aiOid = aiOid;
   }

   public ActivityInstanceAttributesImpl(ActivityInstanceAttributes template)
   {
      this.aiOid = template.getActivityInstanceOid();
      this.result = template.getQualityAssuranceResult();
      
      notes = new ArrayList<Note>();
      addedNotes = new ArrayList<Note>();
   }

   /**
    * {@inheritDoc}
    */
   public long getActivityInstanceOid()
   {
      return aiOid;
   }

   /**
    * {@inheritDoc}
    */
   public void setQualityAssuranceResult(QualityAssuranceResult result)
   {
      this.result = result;
   }

   /**
    * {@inheritDoc}
    */
   public QualityAssuranceResult getQualityAssuranceResult()
   {
      return result;
   }
      
   /**
    * {@inheritDoc}
    */
   public void setNotes(List<Note> notes)
   {
      this.notes = notes;
   }

   /**
    * {@inheritDoc}
    */
   public Note addNote(String text)
   {
      Note note = new NoteDetails(text, ContextKind.ActivityInstance, aiOid, null);
      addedNotes.add(note);
      
      return note;
   }
   
   /**
    * {@inheritDoc}
    */
   public List<Note> getAddedNotes()
   {
      return addedNotes;
   }

   /**
    * {@inheritDoc}
    */
   public List<Note> getNotes()
   {
      return notes;
   }
}
