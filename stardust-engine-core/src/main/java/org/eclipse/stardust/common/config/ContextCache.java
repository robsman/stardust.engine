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
package org.eclipse.stardust.common.config;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * @author fherinean
 * @version $Revision$
 */
public class ContextCache extends AbstractPropertyCache
{
   private Context context;

   ContextCache(ContextCache predecessor)
   {
      super(predecessor, true);
   }

   public void setContext(Context context)
   {
      this.context = context;
   }

   protected Object resolveProperty(String name)
   {
      Object value = null;
      if (null != context)
      {
         try
         {
            value = context.lookup(name);
            if (null == value)
            {
               value = Parameters.NULL_VALUE;
            }
         }
         catch (NamingException e)
         {
            value = null;
         }
      }
      return value;
   }
}
