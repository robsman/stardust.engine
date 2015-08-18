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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;

/**
 * Will be thrown if filter criteria are not supported in the envisioned context.
 *
 * @author rsauer
 * @version $Revision$
 */
public class UnsupportedFilterException extends PublicException
{
   private final FilterCriterion filter;

   /**
    * @deprecated
    */
   public UnsupportedFilterException(String message)
   {
      this(message, null);
   }

   /**
    * Creates the exception with the provided message.
    *
    * @param message the exception message.
    */
   public UnsupportedFilterException(ErrorCase errorCase)
   {
      this(errorCase, null);
   }

   /**
    * @deprecated
    */
   public UnsupportedFilterException(String message, FilterCriterion filter)
   {
      super(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(message));

      this.filter = filter;
   }


   /**
    * Creates the exception with the provided message and invalid filter criterion.
    *
    * @param message the exception message.
    * @param filter the unsupported filter criterion
    */
   public UnsupportedFilterException(ErrorCase errorCase, FilterCriterion filter)
   {
      super(errorCase);

      this.filter = filter;
   }

   /**
    * Gets the unsupported filter criterion causing the exception.
    *
    * @return the filter criterion
    */
   public FilterCriterion getFilter()
   {
      return filter;
   }
}
