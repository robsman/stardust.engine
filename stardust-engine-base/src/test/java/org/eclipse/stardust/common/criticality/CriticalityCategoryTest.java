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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>
 * Tests {@link CriticalityCategory}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class CriticalityCategoryTest
{
   private static final double DELTA = 0.1;

   @Test
   public void testMatchesLowCategory()
   {
      final CriticalityCategory category = new CriticalityCategory(LOW_CATEGORY_LOWER_BOUND, LOW_CATEGORY_UPPER_BOUND, CATEGORY_NAME_LOW);

      assertFalse(category.matches(LOW_CATEGORY_LOWER_BOUND - DELTA));
      assertTrue(category.matches(LOW_CATEGORY_LOWER_BOUND));
      assertTrue(category.matches(LOW_CATEGORY_LOWER_BOUND + DELTA));
      assertTrue(category.matches(LOW_CATEGORY_UPPER_BOUND));
      assertFalse(category.matches(LOW_CATEGORY_UPPER_BOUND + DELTA));
   }

   @Test
   public void testMatchesMediumCategory()
   {
      final CriticalityCategory category = new CriticalityCategory(MEDIUM_CATEGORY_LOWER_BOUND, MEDIUM_CATEGORY_UPPER_BOUND, CATEGORY_NAME_MEDIUM);

      assertFalse(category.matches(MEDIUM_CATEGORY_LOWER_BOUND - DELTA));
      assertFalse(category.matches(MEDIUM_CATEGORY_LOWER_BOUND));
      assertTrue(category.matches(MEDIUM_CATEGORY_LOWER_BOUND + DELTA));
      assertTrue(category.matches(MEDIUM_CATEGORY_UPPER_BOUND));
      assertFalse(category.matches(MEDIUM_CATEGORY_UPPER_BOUND + DELTA));
   }

   @Test
   public void testMatchesHighCategory()
   {
      final CriticalityCategory category = new CriticalityCategory(HIGH_CATEGORY_LOWER_BOUND, HIGH_CATEGORY_UPPER_BOUND, CATEGORY_NAME_HIGH);

      assertFalse(category.matches(HIGH_CATEGORY_LOWER_BOUND - DELTA));
      assertFalse(category.matches(HIGH_CATEGORY_LOWER_BOUND));
      assertTrue(category.matches(HIGH_CATEGORY_LOWER_BOUND + DELTA));
      assertTrue(category.matches(HIGH_CATEGORY_UPPER_BOUND));
      assertFalse(category.matches(HIGH_CATEGORY_UPPER_BOUND + DELTA));
   }

   @Test
   public void testToStringLowCategory()
   {
      final String expected = "Low: [0.0, 0.333]";
      final String actual = new CriticalityCategory(LOW_CATEGORY_LOWER_BOUND, LOW_CATEGORY_UPPER_BOUND, CATEGORY_NAME_LOW).toString();

      assertThat(actual, equalTo(expected));
   }

   @Test
   public void testToStringMediumCategory()
   {
      final String expected = "Medium: (0.333, 0.666]";
      final String actual = new CriticalityCategory(MEDIUM_CATEGORY_LOWER_BOUND, MEDIUM_CATEGORY_UPPER_BOUND, CATEGORY_NAME_MEDIUM).toString();

      assertThat(actual, equalTo(expected));
   }

   @Test
   public void testToStringHighCategory()
   {
      final String expected = "High: (0.666, 1.0]";
      final String actual = new CriticalityCategory(HIGH_CATEGORY_LOWER_BOUND, HIGH_CATEGORY_UPPER_BOUND, CATEGORY_NAME_HIGH).toString();

      assertThat(actual, equalTo(expected));
   }
}
