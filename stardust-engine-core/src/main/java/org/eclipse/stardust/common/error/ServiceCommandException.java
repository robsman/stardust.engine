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
 * Thrown if the execution of a service command failed.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ServiceCommandException extends PublicException
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructs a new ServiceCommandException with a message and a cause.
    * 
    * @param error the exception message.
    * @param cause the cause of the exception.
    */
   public ServiceCommandException(String error, Throwable cause)
   {
      super(error, cause);
   }

   /**
    * Constructs a new ServiceCommandException with an error case and a cause.
    * 
    * @param errorCase the error case describing the exception.
    * @param cause the cause of the exception.
    */
   public ServiceCommandException(ErrorCase errorCase, Throwable cause)
   {
      super(errorCase, cause);
   }
}
