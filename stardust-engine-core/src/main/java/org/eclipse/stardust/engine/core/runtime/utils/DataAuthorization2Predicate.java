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
package org.eclipse.stardust.engine.core.runtime.utils;

import org.eclipse.stardust.engine.api.model.IData;

/**
 * Predicate class which is used to restrict access to work items based on its activity
 * declarative security permission.
 *
 * @author stephan.born
 * @version $Revision: 5162 $
 */
public class DataAuthorization2Predicate extends AbstractAuthorization2Predicate
{
   public DataAuthorization2Predicate(AuthorizationContext context)
   {
      super(context);
   }

   public boolean accept(Object o)
   {
      if (o instanceof IData)
      {
         context.setModelElementData((IData) o);
         return Authorization2.hasPermission(context);
      }
      return false;
   }
}
