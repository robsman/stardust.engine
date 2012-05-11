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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodStep extends DereferenceStep
{
   protected Method getMethod;
   protected Method setMethod;

   /** */
   public MethodStep(Method newSetMethod, Method newGetMethod)
   {
      getMethod = newGetMethod;
      setMethod = newSetMethod;
   }

   /** */
   public Object getValue(Object startObject)
   {
      try
      {
         return getMethod.invoke(startObject);
      }
      catch (IllegalAccessException E)
      {
         return null;
      }
      catch (IllegalArgumentException E)
      {
         return null;
      }
      catch (InvocationTargetException E)
      {
         E.getTargetException().printStackTrace();
         return null;
      }

   }

   public void setValue(Object startObject,
         Object newValue)
   {
      Object args[] = {newValue};
      try
      {
         setMethod.invoke(startObject,
               args);
      }
      catch (IllegalAccessException E)
      {
      }
      catch (IllegalArgumentException E)
      {
      }
      catch (InvocationTargetException E)
      {
         E.getTargetException().printStackTrace();
      }
   }

   /** */
   public String getName()
   {
      return getMethod.getName();
   }
} // MethodStep

