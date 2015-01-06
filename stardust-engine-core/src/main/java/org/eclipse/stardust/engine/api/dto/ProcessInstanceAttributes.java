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

/**
 * This class contains attributes for a process instance. Changing these attributes does
 * not affect the workflow behavior.
 * 
 * @author born
 */
public interface ProcessInstanceAttributes extends Serializable
{
   /**
    * @return the oid of the corresponding process instance. 
    */
   long getProcessInstanceOid();
   
   /** 
    * Adds a note to the process instance.
    * 
    * @param text the note text.
    * @return The note.
    */
   Note addNote(String text);
   
   /**
    * Adds a note to the process instance with context .
    * 
    * @param text the note text.
    * @param contextKind the kind of context object.
    * @param contextOid the Oid of the context object.
    * @return the note.
    */
   Note addNote(String text, ContextKind contextKind, long contextOid);
   
   /**
    * @return a list of {@link Note}s.
    */
   List<Note> getNotes();
}