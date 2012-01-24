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

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Unknown;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Simple Test class for the class Money
 *
 */

public class CompareHelperTest extends TestCase
{

   Money tenMarks;
   Money fiveMarks;
   Money oneEur;

   /**
    *
    *
    */
   public CompareHelperTest(String name)
   {
      super(name);
   }

   /**
    *
    *
    */
   protected void setUp()
   {
      tenMarks = new Money(10, Money.DM);
      fiveMarks = new Money(5, Money.DM);

      oneEur = new Money(1, Money.EUR);
   }

   /**
    *
    *
    */
   protected void tearDown()
   {

   }

   /**
    *
    *
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite(CompareHelperTest.class);

      return suite;
   }

   /**
    * runs the TestSuite with the Text (Batch-) TestRunner
    *
    */
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public void testGetValue()
   {

      assertTrue(CompareHelper.getValue(null) == Unknown.INT);
   }

   public void testAreEqual()
   {

      assertTrue(CompareHelper.areEqual(null, null) == true);

      assertTrue(CompareHelper.areEqual(null, "String") == false);

      assertTrue(CompareHelper.areEqual("String", null) == false);

      assertTrue(CompareHelper.areEqual("String", "String") == true);

   }

}
