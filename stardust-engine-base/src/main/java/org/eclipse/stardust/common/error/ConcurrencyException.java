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
 * Thrown whenever the workflow engine detects concurrency problems with
 * workflow enactment.
 */
public class ConcurrencyException extends PublicException implements ExceptionLogHint
{
   private static final long serialVersionUID = 2L;

   /**
    * Constructs the Exception
    * @deprecated - use {@link #ConcurrencyException(ErrorCase)} instead
    * @param message - the error message for this exception
    */
   public ConcurrencyException(String message)
   {
      super(message);
   }

   /**
    * Constructs the Exception
    * @param errorCase - the {@link ErrorCase} for this exception
    */
   public ConcurrencyException(ErrorCase errorCase)
   {
      super(errorCase);
   }

   /**
    * Constructs the Exception
    * @deprecated - use {@link #ConcurrencyException(ErrorCase, Throwable)} instead
    * @param message - the error message for this exception
    * @param e - the root cause for this exception
    */
   public ConcurrencyException(String message, Throwable e)
   {
      super(message, e);
   }

   /**
    * Constructs the Exception
    * @param errorCase - the {@link ErrorCase} for this exception
    * @param e - the root cause for this exception 
    */
   public ConcurrencyException(ErrorCase errorCase, Throwable e)
   {
      super(errorCase, e);
   }

   /**
    * Constructs the Exception
    * @param e - the root cause for this exception 
    */
   public ConcurrencyException(Throwable e)
   {
      super(e);
   }

   /**
    * {@inheritDoc}
    */
   public boolean getInitialLogging()
   {
      return false;
   }
}
