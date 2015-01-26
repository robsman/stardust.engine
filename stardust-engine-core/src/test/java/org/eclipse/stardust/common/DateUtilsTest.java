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

import org.eclipse.stardust.common.DateUtils;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase
{
   public void testIsValidISODateFormatValid()
   {
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21"));
      assertTrue(DateUtils.isValidISODateFormat("2008-8-1"));

      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 14:34"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 14:34:27"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 14:34:37:428"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 2:04:36"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 23:04:05"));
      assertTrue(DateUtils.isValidISODateFormat("2008-12-21 14:34"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 14:59"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21 14:53:59"));

      assertTrue(DateUtils.isValidISODateFormat("2008-08-21T14:34"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21T14:34:27"));
      assertTrue(DateUtils.isValidISODateFormat("2008-08-21T14:34:37:428"));
   }

   public void testIsValidISODateFormatNotValid()
   {      
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21N"));
      assertFalse(DateUtils.isValidISODateFormat("2008-Y08-21"));
      assertFalse(DateUtils.isValidISODateFormat("2008-Z-21"));
      assertFalse(DateUtils.isValidISODateFormat("200-08-21"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-210"));

      assertFalse(DateUtils.isValidISODateFormat("2008-08-2114:34"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21 14:348"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21 14:34:277"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21 14:34:27:2578"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21 144:34:27:257"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21:34 14:34:27"));
      assertFalse(DateUtils.isValidISODateFormat("2008-13-21 14:34"));
      assertFalse(DateUtils.isValidISODateFormat("2008-12-21 24:34"));
      assertFalse(DateUtils.isValidISODateFormat("2008-03-21 231:34"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21 14:75"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21 14:53:89"));

      assertFalse(DateUtils.isValidISODateFormat("2008-0-2611:33:49.109K"));

      assertFalse(DateUtils.isValidISODateFormat("2008-08T14:34"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21T14:34T:27"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21T14:34:37Z:428"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21T14:341:37:428"));
      assertFalse(DateUtils.isValidISODateFormat("2008-08-21TT14:34"));
   }

   public void testIsValidNonInteractiveDateFormatValid()
   {
      assertTrue(DateUtils.isValidNonInteractiveFormat("2008/08/21"));
      assertTrue(DateUtils.isValidNonInteractiveFormat("2008/08/21 14:34"));
      assertTrue(DateUtils.isValidNonInteractiveFormat("2008/08/21 14:34:37"));
      assertTrue(DateUtils.isValidNonInteractiveFormat("2008/08/21 14:34:37:428"));
   }

   public void testIsValidNonInteractiveDateFormatNotValid()
   {
      assertFalse(DateUtils.isValidNonInteractiveFormat("2008/0/21 14:34:37.428K"));
      assertFalse(DateUtils.isValidNonInteractiveFormat("2008/08/21T14:34:37:428"));
   }
}
