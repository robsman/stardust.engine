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

import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.api.runtime.User;


/**
 * @author born
 * @version $Revision: $
 */
public class NoteDetails implements Note
{
   private static final long serialVersionUID = 1L;
   
   private String text;
   private ContextKind contextKind;
   private long contextOid;
   private RuntimeObject contextObject;
   private Date timestamp;
   private long userOid;
   private User user;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.Note#getText()
    */
   public String getText()
   {
      return text;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.Note#getContextKind()
    */
   public ContextKind getContextKind()
   {
      return contextKind;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.Note#getContextOid()
    */
   public long getContextOid()
   {
      return contextOid;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.Note#getContextObject()
    */
   public RuntimeObject getContextObject()
   {
      return contextObject;
   }
   
   public void setContextObject(RuntimeObject contextObject)
   {
      this.contextObject = contextObject;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.Note#getTimestamp()
    */
   public Date getTimestamp()
   {
      return timestamp;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.Note#getUserOid()
    */
   public long getUserOid()
   {
      return userOid;
   }
   
   public User getUser()
   {
      return user;
   }

   public NoteDetails(String text, ContextKind contextKind, long contextOid,
         RuntimeObject contextObject)
   {
      this(text, contextKind, contextOid, contextObject, null, null);
   }

   public NoteDetails(String text, ContextKind contextKind, long contextOid,
         RuntimeObject contextObject, Date timestamp, User user)
   {
      super();

      this.text = text;
      this.contextKind = contextKind;
      this.contextOid = contextOid;
      this.contextObject = contextObject;
      this.timestamp = timestamp;
      this.userOid = user != null ? user.getOID() : 0;
      this.user = user;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(text);
      if(!ContextKind.ProcessInstance.equals(contextKind))
      {
         buffer.append(" (").append(contextKind).append(")");
      }
      
      return buffer.toString();
   }
}
