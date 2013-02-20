/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

import java.text.DecimalFormatSymbols;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Simple Test class for the class Money
 *
 */

public class MoneyTest extends TestCase
{

   Money tenMarks;
   Money fiveMarks;
   Money oneEur;

   /**
    *
    *
    */
   public MoneyTest(String name)
   {
      super(name);
   }

   /**
    * Put all code to set up your test fixture (environment) here
    *
    */
   protected void setUp()
   {
      String abbreviation = Money.getEuropeanCurrencyList()[Money.DM];
      int identitiy = Money.getCurrencyValueFor(abbreviation);
      tenMarks = new Money(10, identitiy);
      fiveMarks = new Money(5, identitiy);

      oneEur = new Money(1, Money.EUR);
   }

   /**
    * here goes code to tear down your test fixture
    * (e. g. release ressources, close streams or files, etc.)
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
      TestSuite suite = new TestSuite(MoneyTest.class);
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

   public void testGetCurrencyFor()
   {

      assertEquals("Testing a round Trip with Money.getCurrencyFor(Money.getCurrencyValueFor", Money.getCurrencyFor(Money.getCurrencyValueFor("DM")), "DM");
      fiveMarks = new Money(5, Money.DM);

   }

   public void testSimpleAdd()
   {
      Money result = fiveMarks.add(tenMarks);
      String abbreviation = Money.getEuropeanCurrencyList()[Money.DM];
      int identitiy = Money.getCurrencyValueFor(abbreviation);
      assertTrue("Test adding Money Objects representing 5 DM and 10 DM", result.equals(new Money(15, identitiy)));
   }

   public void testSimpleAdd2()
   {
      Money result = oneEur.add(oneEur);
      assertTrue("Test adding Money Objects representing 1 EUR and 1 EUR", result.equals(new Money(2, Money.EUR)));
   }

   public void testToString()
   {
      char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
      assertEquals("Test toString() for an Object representing 5 DM, should be \"5"
            + decimalSeparator + "00 DM\"", fiveMarks.toString(), "5" + decimalSeparator
            + "00 DM");
   }

}
