/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.common.criticality;

import org.eclipse.stardust.common.StringUtils;

/**
 * <p>
 * This class represents a <i>Criticality Category</i> comprising a lower and upper bound as well as
 * a name. A <i>Criticality</i> falls into a particular <i>Criticality Category</i>, if and only if
 * its corresponding value is between lower and upper bounds (including boundaries).
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class CriticalityCategory implements Comparable<CriticalityCategory>
{
   private final double lowerBound;
   private final double upperBound;
   private final String name;

   /**
    * <p>
    * Initializing a new instance of {@link CriticalityCategory} with the given values.
    * </p>
    *
    * @param lowerBound the lower bound of this {@link CriticalityCategory}, must not be {@code null}
    * @param upperBound the upper bound of this {@link CriticalityCategory}, must not be {@code null}
    * @param name the name of this {@link CriticalityCategory}, must not be {@code null} or empty
    */
   public CriticalityCategory(final double lowerBound, final double upperBound, final String name)
   {
      if (lowerBound >= upperBound)
      {
         throw new IllegalArgumentException("Lower bound must not be greater or equal to upper bound.");
      }
      if (StringUtils.isEmpty(name))
      {
         throw new IllegalArgumentException("Name must not be null.");
      }

      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.name = name;
   }

   /**
    * @return the lower bound of this {@link CriticalityCategory}
    */
   public double lowerBound()
   {
      return lowerBound;
   }

   /**
    * @return the upper bound of this {@link CriticalityCategory}
    */
   public double upperBound()
   {
      return upperBound;
   }

   /**
    * @return the name of this {@link CriticalityCategory}
    */
   public String name()
   {
      return name;
   }

   /**
    * <p>
    * Tests whether the given value falls into this {@link CriticalityCategory}.
    * </p>
    *
    * @param criticalityValue the <i>Criticality</i> to test
    * @return {@code true} if and only if the given {@code criticalityValue} falls into this {@link CriticalityCategory}
    */
   public boolean matches(final double criticalityValue)
   {
      return criticalityValue >= lowerBound && criticalityValue <= upperBound;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + Double.valueOf(lowerBound).hashCode();
      result = 31 * result + Double.valueOf(upperBound).hashCode();
      result = 31 * result + name.hashCode();
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if ( !(obj instanceof CriticalityCategory))
      {
         return false;
      }

      final CriticalityCategory that = (CriticalityCategory) obj;
      return this.upperBound == that.upperBound
          && this.lowerBound == that.lowerBound
          && this.name.equals(that.name);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(name).append(": ");
      sb.append("[").append(lowerBound).append(", ").append(upperBound).append("]");
      return sb.toString();
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(final CriticalityCategory that)
   {
      return Double.valueOf(this.lowerBound).compareTo(that.lowerBound);
   }
}
