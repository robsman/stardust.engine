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

import java.io.Serializable;

/**
 * Holds pairs of values.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class Pair<K,V> implements Serializable
{
   private static final long serialVersionUID = 8078760612210601910L;

   private final K first;
   private final V second;

   /**
    * Initializes the value pair.
    * 
    * @param first The first value.
    * @param second The second value.
    */
   public Pair(K first, V second)
   {
      this.first = first;
      this.second = second;
   }

   /**
    * Retrieves the first value of the pair.
    * 
    * @return The first value.
    */
   public K getFirst()
   {
      return first;
   }

   /**
    * Retrieves the second value of the pair.
    * 
    * @return The second value.
    */
   public V getSecond()
   {
      return second;
   }

   /**
    * Compares for equality with another value pair. Instances of pair are equal if both
    * values are equal.
    * 
    * @return <code>true</code> if this pair is equal the given other pair,
    *         <code>false</code> if the pairs are not equal or the other value is not an
    *         instance of {@link Pair}.
    */
   public boolean equals(Object other)
   {
      boolean isEqual = false;

      if (this == other)
      {
         isEqual = true;
      }
      else if (other instanceof Pair)
      {
         final Pair<?,?> pair = (Pair<?,?>) other;

         isEqual = (first != null ? first.equals(pair.first) : pair.first == null)
               && (second != null ? second.equals(pair.second) : pair.second == null);
      }

      return isEqual;
   }

   public int hashCode()
   {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((first == null) ? 0 : first.hashCode());
      result = PRIME * result + ((second == null) ? 0 : second.hashCode());
      return result;
   }

   public String toString()
   {
      return "(" + first + "," + second + ")";
   }
}
