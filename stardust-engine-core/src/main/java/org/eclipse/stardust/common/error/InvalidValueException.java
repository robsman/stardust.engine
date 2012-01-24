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
 * Thrown if a write operation to workflow data fails as of an invalid input value. Most
 * probably in consequence of a type conflict between the input value and the static type
 * of the data or data's attribute.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class InvalidValueException extends PublicException
{
   private static final long serialVersionUID = -6023130582045548793L;
   
   /**
    * @deprecated
    */
   public InvalidValueException(String message)
   {
      super(message);
   }

   /**
    * Creates a new exception instance, initializing it with the given message.
    * 
    * @param errorCase The error case further explaining the error condition.
    */
   public InvalidValueException(ErrorCase errorCase)
   {
      super(errorCase);
   }

   /**
    * @deprecated
    */
   public InvalidValueException(String message, Exception cause)
   {
      super(message, cause);
   }
   
   /**
    * Creates a new exception instance, initializing it with the given message.
    * 
    * @param errorCase The error case further explaining the error condition.
    * @param cause The cause of this error.
    */
   public InvalidValueException(ErrorCase errorCase, Exception cause)
   {
      super(errorCase, cause);
   }
}
