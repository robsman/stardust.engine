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

public class ServiceCommandException extends PublicException
{
   private static final long serialVersionUID = 1L;


   public ServiceCommandException(String error)
   {
      super(error);
   }

   public ServiceCommandException(String error, Throwable cause)
   {
      super(error, cause);
   }

   public ServiceCommandException(ErrorCase errorCase)
   {
      super(errorCase);
   }
   
   public ServiceCommandException(ErrorCase errorCase, Throwable cause)
   {
      super(errorCase, cause);
   }
}
