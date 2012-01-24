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
 * @author rsauer
 * @version $Revision$
 */
public class ExpectedFailureException extends PublicException
{
   private static final long serialVersionUID = -5188805240482648300L;
   
   /**
    * @deprecated
    */
   public ExpectedFailureException(String message)
   {
      super(message);
   }

   public ExpectedFailureException(ErrorCase errorCase)
   {
      super(errorCase);
   }

   /**
    * @deprecated
    */
   public ExpectedFailureException(Throwable e)
   {
      super(e);
   }

   public ExpectedFailureException(ErrorCase errorCase, Throwable e)
   {
      super(errorCase, e);
   }

   /**
    * @deprecated
    */
   public ExpectedFailureException(String message, Throwable e)
   {
      super(message, e);
   }
}
