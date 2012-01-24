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
 *
 */
public class PublicException extends ApplicationException
{
   private static final long serialVersionUID = 3236696384405573222L;
   
   // stands for silent, abbreviated for obfuscation reasons
   private boolean s;

   /**
    *
    */
   public PublicException(ErrorCase errorCase)
   {
      this(errorCase, errorCase.toString(), false);
   }

   public PublicException(String message)
   {
      this(null, message, false);
   }

   public PublicException(ErrorCase errorCase, boolean s)
   {
      this(errorCase, errorCase.toString(), s);
   }

   public PublicException(String message, boolean s)
   {
      this(null, message, s);
   }

   public PublicException(ErrorCase errorCase, String message, boolean s)
   {
      super(errorCase, message);

      this.s = s;
   }

   /**
    * This constructor is used for exception conversion.
    */
   public PublicException(Throwable e)
   {
      super(e);
   }

   /**
    * This constructor is used for exception conversion.
    */
   public PublicException(String message, Throwable e)
   {
      super(message, e);
   }

   /**
    * This constructor is used for exception conversion.
    */
   public PublicException(ErrorCase errorCase, Throwable e)
   {
      super(errorCase, errorCase.toString(), e);
   }
   
   /**
    * This constructor is used for exception conversion.
    */
   public PublicException(ErrorCase errorCase, String message, Throwable e)
   {
      super(errorCase, message, e);
   }

   //todo: remove from API:
   public boolean isS()
   {
      return s;
   }
}