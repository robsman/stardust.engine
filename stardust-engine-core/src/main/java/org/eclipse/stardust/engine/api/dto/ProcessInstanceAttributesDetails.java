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
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;


/**
 * This class contains attributes for a process instance. Changing these attributes does
 * not affect the workflow behavior.
 * 
 * @author born
 */
public class ProcessInstanceAttributesDetails implements Serializable,
      ProcessInstanceAttributes
{
   private static final long serialVersionUID = 3L;
   
   private ProcessInstance processInstance;

   private List<Note> notes = Collections.emptyList();
   private List<Note> addedNotes = CollectionUtils.newArrayList();
   
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes#getProcessInstanceOid()
    */
   public long getProcessInstanceOid()
   {
      return processInstance.getOID();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes#addNote(java.lang.String)
    */
   public Note addNote(String text)
   {
      return addNote(text, ContextKind.ProcessInstance, processInstance.getOID());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes#addNote(java.lang.String, org.eclipse.stardust.engine.api.dto.ContextKind, long)
    */
   public Note addNote(String text, ContextKind contextKind, long contextOid)
   {
      NoteDetails note = new NoteDetails(text, contextKind, contextOid, null);
      addedNotes.add(note);
      
      return note;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes#getNotes()
    */
   public List<Note> getNotes()
   {
      List<Note> list = CollectionUtils.newArrayListFromElements(notes);
      list.addAll(addedNotes);

      return Collections.unmodifiableList(list);
   }

   public List <Note> getAddedNotes()
   {
      return Collections.unmodifiableList(addedNotes);
   }
   
   public void initNotes(List<Note> notes)
   {
      this.notes = notes;
   }
   
   /**
    * @param processInstance back reference to containing {@link ProcessInstance}.
    */
   ProcessInstanceAttributesDetails(ProcessInstance processInstance)
   {
      super();

      this.processInstance = processInstance;
   }


}
