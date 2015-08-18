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
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.PublicException;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class BindingException extends PublicException
{
   /**
   * @deprecated
   */
   public BindingException(String message)
   {
      super(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(message));
   }

   /**
   *
   */
   public BindingException(ErrorCase errorCase)
   {
      super(errorCase);
   }
}
