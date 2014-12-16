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
   private static final String CATEGORY_NAME_LOW = "Low";
   private static final String CATEGORY_NAME_MEDIUM = "Medium";
   private static final String CATEGORY_NAME_HIGH = "High";

   /**
    * <p>
    * The default <i>Criticality Categories</i> that apply, if no explicit <i>Criticality Categories</i> have been defined.
    * </p>
    *
    * @param categoryNameLow the name of the default {@link CriticalityCategory} <i>Low</i>; may be null, in that case {@code "Low"} will be used
    * @param categoryNameMedium the name of the default {@link CriticalityCategory} <i>Medium</i>; may be null, in that case {@code "Medium"} will be used
    * @param categoryNameHigh the name of the default {@link CriticalityCategory} <i>High</i>; may be null, in that case {@code "High"} will be used
    * @return the default categories
    */
   public static Set<CriticalityCategory> getCriticalityDefaultCategories(final String categoryNameLow, final String categoryNameMedium, final String categoryNameHigh)
   {
      final Set<CriticalityCategory> result = newTreeSet();
      result.add(new CriticalityCategory(0.0, 0.333, categoryNameLow != null ? categoryNameLow : CATEGORY_NAME_LOW));
      result.add(new CriticalityCategory(0.334, 0.666, categoryNameMedium != null ? categoryNameMedium : CATEGORY_NAME_MEDIUM));
      result.add(new CriticalityCategory(0.667, 1.0, categoryNameHigh != null ? categoryNameHigh : CATEGORY_NAME_HIGH));
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
