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
 * Base exception class for all exceptions that should be viewable by the client
 * 
 * @author mgille
 * @version $Revision$
 */
public class PublicException extends ApplicationException
{
   private static final long serialVersionUID = 3236696384405573222L;
   
   /**
    * Constructs the Exception
    * @param errorCase - the {@link ErrorCase} for this message
    */
   public PublicException(ErrorCase errorCase)
   {
      super(errorCase, errorCase.toString());
   }
   
   /**
    * Constructs the Exception
    * @param message - the error message for this exception
    */
   public PublicException(String message)
   {
      super(message);
   }

   /**
    * Constructs the Exception
    * @param e - the root cause for this exception 
    */
   public PublicException(Throwable e)
   {
      super(e);
   }

   /**
    * Constructs the Exception
    * @param message - the error message for this exception
    * @param e - the root cause for this exception
    */
   public PublicException(String message, Throwable e)
   {
      super(message, e);
   }

   /**
    * Constructs the Exception
    * @param errorCase - the {@link ErrorCase} for this message
    * @param e - the root cause for this exception
    */
   public PublicException(ErrorCase errorCase, Throwable e)
   {
      super(errorCase, errorCase.toString(), e);
   }
   
   /**
    * Constructs the Exception
    * @param errorCase - the {@link ErrorCase} for this message
    * @param message - the error message for this exception 
    * @param e  - the root cause for this exception
    */
   public PublicException(ErrorCase errorCase, String message, Throwable e)
   {
      super(errorCase, message, e);
   }
}