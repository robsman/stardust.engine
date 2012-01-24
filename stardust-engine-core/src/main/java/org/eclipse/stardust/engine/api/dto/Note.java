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
import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.api.runtime.User;


public interface Note extends Serializable
{
   /**
    * @return the note itself.
    */
   String getText();
   
   /**
    * @return the kind of context.
    */
   ContextKind getContextKind();
   
   /**
    * @return  the oid of the context object.
    */
   long getContextOid();
   
   
   /**
    * @return the context object itself if details level allows it, otherwise null. 
    */
   RuntimeObject getContextObject();
   
   /**
    * @return the oid of the user who created the note.
    * @deprecated Use {@link #getUser().getOID()} instead.
    */
   long getUserOid();
   
   /**
    * @return the user who created the note. The user will be initialized with details
    *         level {@link UserDetailsLevelUser#CORE}. May be null if no user is
    *         specified.
    */
   User getUser();
   
   /**
    * @return when was the note created.
    */
   Date getTimestamp(); 
}
