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

import static org.eclipse.stardust.common.CollectionUtils.newTreeSet;

import java.util.Set;

/**
 * <p>
 * Some utility methods dealing with <i>Criticality</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class CriticalityUtils
{
   /* package-private */ static final double LOW_CATEGORY_LOWER_BOUND = 0.0;
   /* package-private */ static final double LOW_CATEGORY_UPPER_BOUND = 0.333;
   /* package-private */ static final double MEDIUM_CATEGORY_LOWER_BOUND = LOW_CATEGORY_UPPER_BOUND;
   /* package-private */ static final double MEDIUM_CATEGORY_UPPER_BOUND = 0.666;
   /* package-private */ static final double HIGH_CATEGORY_LOWER_BOUND = MEDIUM_CATEGORY_UPPER_BOUND;
   /* package-private */ static final double HIGH_CATEGORY_UPPER_BOUND = 1.0;

   /* package-private */ static final String CATEGORY_NAME_LOW = "Low";
   /* package-private */ static final String CATEGORY_NAME_MEDIUM = "Medium";
   /* package-private */ static final String CATEGORY_NAME_HIGH = "High";

   /**
    * <p>
    * The default <i>Criticality Categories</i> that apply, if no explicit <i>Criticality Categories</i> have been defined. The returned
    * {@link Set} is sorted by ascending value of {@link CriticalityCategory#lowerBound()}.
    * </p>
    *
    * @param categoryNameLow the name of the default {@link CriticalityCategory} <i>Low</i>; may be null, in that case {@code "Low"} will be used
    * @param categoryNameMedium the name of the default {@link CriticalityCategory} <i>Medium</i>; may be null, in that case {@code "Medium"} will be used
    * @param categoryNameHigh the name of the default {@link CriticalityCategory} <i>High</i>; may be null, in that case {@code "High"} will be used
    * @return the sorted default categories
    */
   public static Set<CriticalityCategory> getCriticalityDefaultCategories(final String categoryNameLow, final String categoryNameMedium, final String categoryNameHigh)
   {
      final Set<CriticalityCategory> result = newTreeSet();
      result.add(new CriticalityCategory(LOW_CATEGORY_LOWER_BOUND, LOW_CATEGORY_UPPER_BOUND, categoryNameLow != null ? categoryNameLow : CATEGORY_NAME_LOW));
      result.add(new CriticalityCategory(MEDIUM_CATEGORY_LOWER_BOUND, MEDIUM_CATEGORY_UPPER_BOUND, categoryNameMedium != null ? categoryNameMedium : CATEGORY_NAME_MEDIUM));
      result.add(new CriticalityCategory(HIGH_CATEGORY_LOWER_BOUND, HIGH_CATEGORY_UPPER_BOUND, categoryNameHigh != null ? categoryNameHigh : CATEGORY_NAME_HIGH));
      return result;
   }

   /**
    * <p>
    * Returns the appropriate {@link CriticalityCategory} for the given <i>Criticality</i>.
    * </p>
    *
    * @param categories the {@link CriticalityCategory} to browse
    * @param criticalityValue the <i>Criticality</i> to test
    * @return the {@link CriticalityCategory} for the given <i>Criticality</i>, or {@code null} if a matching {@link CriticalityCategory} cannot be found
    */
   public static CriticalityCategory findMatchingCriticalityCategory(final Set<CriticalityCategory> categories, final double criticalityValue)
   {
      for (final CriticalityCategory c : categories)
      {
         if (c.matches(criticalityValue))
         {
            return c;
         }
      }

      return null;
   }
}
