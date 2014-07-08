/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.ejb;

import org.eclipse.stardust.common.error.ExceptionLogHint;
import org.eclipse.stardust.common.error.InternalException;

public class ExecuterServiceException extends InternalException
      implements ExceptionLogHint
{
   private static final long serialVersionUID = 8036687162023541520L;

   /**
    * Constructs the Exception
    */
   public ExecuterServiceException(String message)
   {
      super(message);
   }

   /**
    * Constructs the Exception
    */
   public ExecuterServiceException(Throwable e)
   {
      super(e);
   }

   /**
    * Constructs the Exception
    */
   public ExecuterServiceException(String message, Throwable e)
   {
      super(message, e);
   }

   @Override
   public boolean getInitialLogging()
   {
      return false;
   }
}