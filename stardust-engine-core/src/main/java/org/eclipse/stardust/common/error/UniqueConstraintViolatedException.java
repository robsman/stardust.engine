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
 * Thrown when a SQLException was thrown for unique constraint violation.
 */
public class UniqueConstraintViolatedException extends ResourceException implements
      ExceptionLogHint
{
   private static final long serialVersionUID = 2L;
   
   public UniqueConstraintViolatedException(String message, Throwable e)
   {
      super(message, e);
   }

   public boolean getInitialLogging()
   {
      return false;
   }
}
