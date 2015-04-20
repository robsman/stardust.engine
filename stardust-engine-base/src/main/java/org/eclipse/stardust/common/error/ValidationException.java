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
package org.eclipse.stardust.common.error;

import java.util.Collection;
import java.util.Collections;

/**
 * Thrown to indicate that validation of a model element has failed.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ValidationException extends PublicException
{
   private static final long serialVersionUID = -2269661497754018925L;
   
   private Collection messages;
   private boolean canClose;

   /**
    * Creates a new exception instance with the specified detail message.
    * 
    * @param summary
    *           The detail message describing the error condition.
    * @param canClose
    *           Flag if displayed deployment warnings can be closed.
    */
   public ValidationException(String summary, boolean canClose)
   {
      this(summary, Collections.EMPTY_LIST, canClose);
   }

   /**
    * Creates a new exception instance with the specified detail message.
    * 
    * @param summary
    *           The detail message describing the error condition.
    * @param messages
    *           All validation messages for this validation exception.
    * @param canClose
    *           Flag if displayed deployment warnings can be closed.
    */
   public ValidationException(String summary, Collection messages, boolean canClose)
   {
      super(summary);
      this.messages = messages;
      this.canClose = canClose;
   }

   /**
    * Returns all validation messages for this validation exception.
    * 
    * @return validation messages
    */
   public Collection getMessages()
   {
      return Collections.unmodifiableCollection(messages);
   }

   /**
    * Returns if displayed deployment validation messages can be closed.
    * 
    * @return Flag if displayed warnings can be closed.
    */
   public boolean canClose()
   {
      return canClose;
   }
}
