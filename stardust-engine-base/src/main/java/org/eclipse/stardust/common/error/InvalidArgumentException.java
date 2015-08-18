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

/**
 * Thrown to indicate that a method has been passed an illegal or 
 * inappropriate argument.
 *
 * @author  fherinean
 * @version $Revision: $
 * @since   IPP 5.2
 */
public class InvalidArgumentException extends PublicException
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructs an <code>IllegalArgumentException</code> with the 
    * specified detail message. 
    *
    * @param  message the detail message describing the error condition.
    */
   public InvalidArgumentException(ErrorCase message)
   {
      super(message);
   }

   /**
    * Constructs a new exception with the specified detail message and
    * cause.
    *
    * @param  message the detail message describing the error condition.
    * @param  cause the cause (which is saved for later retrieval by the
    *         {@link Throwable#getCause()} method).  (A <tt>null</tt> value
    *         is permitted, and indicates that the cause is nonexistent or
    *         unknown.)
    */
   public InvalidArgumentException(ErrorCase message, Throwable cause)
   {
      super(message, cause);
   }
}
