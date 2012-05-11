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
package org.eclipse.stardust.common;

import java.util.Stack;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class StackedThreadLocal<T> extends ThreadLocal<Stack<T>>
{
   protected Stack<T> initialValue()
   {
      return new Stack<T>();
   }

   public void push(T o)
   {
      Stack<T> inner = get();
      inner.push(o);
   }

   public T peek()
   {
      Stack<T> inner = get();
      if (inner.isEmpty())
      {
         return null;
      }
      else
      {
         return inner.peek();
      }
   }

   public T pop()
   {
      Stack<T> inner = get();
      return inner.pop();
   }

   public int size()
   {
      Stack<T> inner = get();
      return inner.size();
   }
}
