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
package org.eclipse.stardust.common.reflect;

import java.lang.reflect.Method;

public class MethodWrapper
{
   Method method;
   String methodName = "not available";
   /**
    *
    */
   public MethodWrapper(Method method)
   {
      this.method = method;
      if (method != null)
      {
         methodName = Reflect.getSortableMethodName(method);
      }
   }

   /**
    *
    */
   public String toString()
   {
      return methodName;
   }

   /**
    *
    */
   public Method getMethod()
   {
      return method;
   }

   /**
    *
    */
   public String getName()
   {
      return toString();
   }
   /**
    *
    */
   public int hashCode()
   {
      return method.hashCode();
   }

   /**
    *
    */
   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof MethodWrapper)
      {
         return ((MethodWrapper)obj).getMethod().equals(getMethod());
      }
      else if (obj instanceof String)
      {
         return obj.equals(toString());
      }
      return false;
   }
}
