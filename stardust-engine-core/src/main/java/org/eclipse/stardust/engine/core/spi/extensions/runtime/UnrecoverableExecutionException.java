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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import org.eclipse.stardust.common.error.ApplicationException;

/**
 * Indicates abrupt termination of event action execution due to severe errors.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class UnrecoverableExecutionException extends ApplicationException
{
   public UnrecoverableExecutionException(String message)
   {
      super(message);
   }

   public UnrecoverableExecutionException(String message, Throwable e)
   {
      super(message, e);
   }
}
