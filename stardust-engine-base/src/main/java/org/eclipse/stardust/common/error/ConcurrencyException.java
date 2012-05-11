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
    * @deprecated Use of error codes is strongly recommended.
    */
   public ConcurrencyException(String message)
   {
      super(message);
   }

   public ConcurrencyException(ErrorCase errorCase)
   {
      super(errorCase);
   }

   /**
    * @deprecated Use of error codes is strongly recommended.
    */
   public ConcurrencyException(String message, Throwable e)
   {
      super(message, e);
   }

   public ConcurrencyException(ErrorCase errorCase, Throwable e)
   {
      super(errorCase, e);
   }

   public ConcurrencyException(Throwable e)
   {
      super(e);
   }

   public boolean getInitialLogging()
   {
      return false;
   }
}
