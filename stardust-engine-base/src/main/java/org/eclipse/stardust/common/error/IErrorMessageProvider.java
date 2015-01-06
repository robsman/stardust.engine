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

import java.util.Locale;

/**
 * @author sauer
 * @version $Revision: $
 */
public interface IErrorMessageProvider
{

   String getErrorMessage(ErrorCase errorCase, Object[] context, Locale locale);
   
   String getErrorMessage(ApplicationException exception, Locale locale);
   
   interface Factory
   {
      IErrorMessageProvider getProvider(ErrorCase errorCase);
   }
   
}
