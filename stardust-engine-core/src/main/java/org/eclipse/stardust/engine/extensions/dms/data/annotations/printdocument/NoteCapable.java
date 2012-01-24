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
package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.util.Set;

public interface NoteCapable
{
   /**
    * Adds the Note.
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the id of the Note already exists.
    *
    * @param highlight
    */
   void addNote(Note note);

   Note getNote(String id);

   Set<Note> getNotes();

   void removeNote(String id);

   /**
    * Replaces the stored notes with the given set. <br>
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.<br>
    *
    * @param notes
    */
   void setNotes(Set<Note> notes);

   /**
    * Adds the given set of notes to the stored ones.<br>
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.<br>
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains at least one note with the same Identifiable#id
    *            as a stored note.
    *
    * @param notes
    */
   void addAllNotes(Set<Note> notes);

   /**
    * Removes all notes.
    */
   void removeAllNotes();
}
