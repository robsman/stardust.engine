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

import static org.eclipse.stardust.common.criticality.CriticalityUtils.CATEGORY_NAME_HIGH;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.CATEGORY_NAME_LOW;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.CATEGORY_NAME_MEDIUM;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.HIGH_CATEGORY_LOWER_BOUND;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.HIGH_CATEGORY_UPPER_BOUND;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.LOW_CATEGORY_LOWER_BOUND;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.LOW_CATEGORY_UPPER_BOUND;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.MEDIUM_CATEGORY_LOWER_BOUND;
import static org.eclipse.stardust.common.criticality.CriticalityUtils.MEDIUM_CATEGORY_UPPER_BOUND;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

/**
 * <p>
 * Tests {@link CriticalityUtils}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class CriticalityUtilsTest
{
   @Test
   public void testGetCriticalityDefaultCategoriesDefaultNames()
   {
      final Set<CriticalityCategory> actual = CriticalityUtils.getCriticalityDefaultCategories(null, null, null);
      final Iterator<CriticalityCategory> iter = actual.iterator();
      final CriticalityCategory actualLow = iter.next();
      final CriticalityCategory actualMedium = iter.next();
      final CriticalityCategory actualHigh = iter.next();

      assertThat(actualLow, equalTo(new CriticalityCategory(LOW_CATEGORY_LOWER_BOUND, LOW_CATEGORY_UPPER_BOUND, CATEGORY_NAME_LOW)));
      assertThat(actualMedium, equalTo(new CriticalityCategory(MEDIUM_CATEGORY_LOWER_BOUND, MEDIUM_CATEGORY_UPPER_BOUND, CATEGORY_NAME_MEDIUM)));
      assertThat(actualHigh, equalTo(new CriticalityCategory(HIGH_CATEGORY_LOWER_BOUND, HIGH_CATEGORY_UPPER_BOUND, CATEGORY_NAME_HIGH)));
   }

   @Test
   public void testGetCriticalityDefaultCategoriesGivenNames()
   {
      final String categoryNameLow = "Niedrig";
      final String categoryNameMedium = "Mittel";
      final String categoryNameHigh = "Hoch";

      final Set<CriticalityCategory> actual = CriticalityUtils.getCriticalityDefaultCategories(categoryNameLow, categoryNameMedium, categoryNameHigh);
      final Iterator<CriticalityCategory> iter = actual.iterator();
      final CriticalityCategory actualLow = iter.next();
      final CriticalityCategory actualMedium = iter.next();
      final CriticalityCategory actualHigh = iter.next();

      assertThat(actualLow, equalTo(new CriticalityCategory(LOW_CATEGORY_LOWER_BOUND, LOW_CATEGORY_UPPER_BOUND, categoryNameLow)));
      assertThat(actualMedium, equalTo(new CriticalityCategory(MEDIUM_CATEGORY_LOWER_BOUND, MEDIUM_CATEGORY_UPPER_BOUND, categoryNameMedium)));
      assertThat(actualHigh, equalTo(new CriticalityCategory(HIGH_CATEGORY_LOWER_BOUND, HIGH_CATEGORY_UPPER_BOUND, categoryNameHigh)));
   }

   @Test
   public void testFindMatchingCriticalityCategorySuccess()
   {
      final CriticalityCategory expected = new CriticalityCategory(MEDIUM_CATEGORY_LOWER_BOUND, MEDIUM_CATEGORY_UPPER_BOUND, CATEGORY_NAME_MEDIUM);
      final CriticalityCategory actual = CriticalityUtils.findMatchingCriticalityCategory(CriticalityUtils.getCriticalityDefaultCategories(null, null, null), MEDIUM_CATEGORY_UPPER_BOUND);

      assertThat(actual, equalTo(expected));
   }

   @Test
   public void testFindMatchingCriticalityCategoryFailure()
   {
      final CriticalityCategory actual = CriticalityUtils.findMatchingCriticalityCategory(CriticalityUtils.getCriticalityDefaultCategories(null, null, null), -1.0);

      assertThat(actual, nullValue());
   }
}
