/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

/**
 * This predicate accepts all elements which are equal to fixed value.
 *
 * @author Stephan.Born
 *
 * @param <E>
 */
public class EqualPredicate<E> implements Predicate<E>
{
   private final E value;

   /**
    * Constructs predicate which accepts all elements which are equal to given value.
    *
    * @param value value to compare elements with
    */
   public EqualPredicate(E value)
   {
      if (value == null)
      {
         new IllegalArgumentException();
      }

      this.value = value;
   }

   @Override
   public boolean accept(E o)
   {
      return value.equals(o);
   }
}
