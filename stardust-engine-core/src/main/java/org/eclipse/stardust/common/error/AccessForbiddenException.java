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
 * Thrown when the performing user is not valid or if he doesn't have the neccessary
 * permissions.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AccessForbiddenException extends PublicException
{
   private static final long serialVersionUID = 1945735401980305808L;
   
   /**
    * Creates the exception with the provided message.
    *
    * @param message the exception message.
    * 
    * @deprecated Use of error codes is strongly recommended.
    */
   public AccessForbiddenException(String message)
   {
      super(message);
   }

   /**
    * Creates the exception with the error.
    *
    * @param errorCase the error code.
    */
   public AccessForbiddenException(ErrorCase errorCase)
   {
      super(errorCase);
   }

}
