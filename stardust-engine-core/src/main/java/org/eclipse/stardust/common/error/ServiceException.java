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
 * Indicates an invoked service has raised an exception.
 */
public class ServiceException extends PublicException
{
   private static final long serialVersionUID = 8998057499068470887L;
   
   public ServiceException(String message)
   {
      super(message);
   }

   /**
    * @deprecated
    */
   public ServiceException(String message, Throwable e)
   {
      super(message, e);
   }

   public ServiceException(ErrorCase errorCase, Throwable e)
   {
      super(errorCase, e);
   }

   public ServiceException(Throwable e)
   {
      super(e);
   }
}
