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
package org.eclipse.stardust.engine.core.compatibility.el;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.compatibility.el.EvaluationError;
import org.eclipse.stardust.engine.core.compatibility.el.Interpreter;
import org.eclipse.stardust.engine.core.compatibility.el.Result;
import org.eclipse.stardust.engine.core.compatibility.el.SyntaxError;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;



/**
 *  Test class for the class Interpreter
 */
public class InterpreterTest
{
   TestSymbolTable testSymbolTable;
   protected static final String WHITESPACE = " ";
   protected static final String DOUBLETAB = "    ";
   protected static final String TEST = "Test: ";

   /**
    *
    */
   public InterpreterTest() throws EvaluationError, SyntaxError
   {
      // create TestSymbolTable
      testSymbolTable = new org.eclipse.stardust.engine.core.compatibility.el.TestSymbolTable();

      // add TestSymbols
      testSymbolTable.registerSer("Character1", new Character('c'));
      testSymbolTable.registerSer("Character2", new Character('m'));
      testSymbolTable.registerSer("Character3", new Character('g'));
      testSymbolTable.registerSer("String1", new String("Fritz"));
      testSymbolTable.registerSer("String2", new String("blue"));
      testSymbolTable.registerSer("String3", new String("brown"));
      testSymbolTable.registerSer("String4", new String("white"));
      testSymbolTable.registerSer("ORANGENPREIS", new String("12,--DM"));
      testSymbolTable.registerSer("Integer1", new Integer(12));
      testSymbolTable.registerSer("Integer2", new Integer(80000));
      testSymbolTable.registerSer("Integer3", new Integer(5000));
      testSymbolTable.registerSer("Integer4", new Integer(75000));
      testSymbolTable.registerSer("Long1", new Long(-500));
      testSymbolTable.registerSer("Long2", new Long(250));
      testSymbolTable.registerSer("Double1", new Double(0.123));
      testSymbolTable.registerSer("Double2", new Double(145.88999));
      testSymbolTable.registerSer("Double3", new Double(146.0));
      testSymbolTable.registerSer("Double4", new Double(25.333666));
      testSymbolTable.registerSer("Boolean1", new Boolean(true));
      testSymbolTable.registerSer("Boolean2", new Boolean(false));

      testSymbolTable.registerSer("sym_bol_5", new Double(8));

      // create Object
      Object _testObject = new Object();

      testSymbolTable.registerSer("Object", _testObject);

      // create Vector
      List _testVector = CollectionUtils.newList();

      // add TestElements
      _testVector.add("Blau");
      _testVector.add("Grün");
      _testVector.add("Gelb");
      _testVector.add("Orange");
      _testVector.add("Schwarz");

      testSymbolTable.registerSer("Farbliste", _testVector);

      // add TestString
      testSymbolTable.registerSer("StringPath", new String("Interpreter.interpreterOne.test8()"));

      // add an xml data
      testSymbolTable.registerXml("xml", "<city>Frankfurt</city>");
      testSymbolTable.registerXml("xml_ns", "<cx:city xmlns:cx=\"http:\\\\bla.bla.com\\blub\">Frankfurt</cx:city>");
   }

   public String getName()
   {
      return InterpreterTest.class.getSimpleName();
   }

   /**
    * Put all code to set up your test fixture (environment) here
    *
    */
   @Before
   public void setUp()
   {
   }

   /**
    * here goes code to tear down your test fixture
    * (e. g. release ressources, close streams or files, etc.)
    */
   @After
   public void tearDown()
   {

   }

   /**
    * test of valid symbol name - bug fix
    */
   @org.junit.Test
   public void testValidSymbolName()
   {
      try
      {
         String eval = "sym_bol_5 > 4";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of greater comparison
    */
   @org.junit.Test
   public void testGreaterComparison()
   {
      try
      {
         String eval = "5 > 4";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of greater comparison
    */
   @org.junit.Test
   public void testGreaterComparison2()
   {
      try
      {
         String eval = "5 > 6";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test3 of greater comparison
    */
   @org.junit.Test
   public void testGreaterComparison3()
   {
      try
      {
         String eval = "(7.0 > 6.5)";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test4 of greater comparison
    */
   @org.junit.Test
   public void testGreaterComparison4()
   {
      try
      {
         String eval = "((7.0 >6.999999999999))";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test5 of greater comparison
    */
   @org.junit.Test
   public void testGreaterComparison5()
   {
      try
      {
         String eval = "    ( 7           >  6.9               )      ";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of less comparison
    */
   @org.junit.Test
   public void testLessComparison()
   {
      try
      {
         String eval = "3 < 4";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of less comparison
    */
   @org.junit.Test
   public void testLessComparison2()
   {
      try
      {
         String eval = "5 < 4";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of equal comparison
    */
   @org.junit.Test
   public void testEqualComparison()
   {
      try
      {
         String eval = "'c'='c'";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of equal comparison
    */
   @org.junit.Test
   public void testEqualComparison2()
   {
      try
      {
         String eval = "'c' = 'C'";
         Result result = Interpreter.evaluate(eval, testSymbolTable);
         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of equal comparison
    */
   @org.junit.Test
   public void testEqualComparison3()
   {
      try
      {
         String eval = "String3 = \"brown\"";
         Result result = Interpreter.evaluate(eval, testSymbolTable);
         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         x.printStackTrace();
         reportException(x);
      }
   }

   /**
    * test of less or equal comparison
    */
   @org.junit.Test
   public void testLessOrEqualComparison()
   {
      try
      {
         String eval = "3 <= 4";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of less or equal comparison
    */
   @org.junit.Test
   public void testLessOrEqualComparison2()
   {
      try
      {
         String eval = "5<=4";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of greater or equal comparison
    */
   @org.junit.Test
   public void testGreaterOrEqualComparison()
   {
      try
      {
         String eval = "4.5>=4.5";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of greater or equal comparison
    */
   @org.junit.Test
   public void testGreaterOrEqualComparison2()
   {
      try
      {
         String eval = "3.5>=4.5";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of unsupported chars
    */
   @org.junit.Test
   public void testUnsupportedChars()
   {
      try
      {
         String eval = "( 7 >$ 6)";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of unsupported chars
    */
   @org.junit.Test
   public void testUnsupportedChars2()
   {
      try
      {
         String eval = "b.isEmpty() AND !a.isEmpty()";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong brace number
    */
   @org.junit.Test
   public void testWrongBraceNumber()
   {
      try
      {
         String eval = "(( 1= 1 ) AND a.isEmpty()))";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of wrong brace number
    */
   @org.junit.Test
   public void testWrongBraceNumber2()
   {
      try
      {
         String eval = "(( 1= 1 ) AND ( 0<2)";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong syntax
    */
   @org.junit.Test
   public void testWrongSyntax()
   {
      try
      {
         String eval = "(( 1= 1 ) ANDa.isEmpty()=TRUE )";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong syntax
    */
   @org.junit.Test
   public void testWrongSyntax2()
   {
      try
      {
         String eval = "(( 1= 1 ) a.isEmpty()=TRUE )";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong syntax
    */
   @org.junit.Test
   public void testWrongSyntax3()
   {
      try
      {
         String eval = "(( 1= 1 ) Oberschlau a.isEmpty()=TRUE )";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong syntax
    */
   @org.junit.Test
   public void testWrongSyntax4()
   {
      try
      {
         String eval = "'c' = 'Xs and 'b' = 'c'";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong syntax
    */
   @org.junit.Test
   public void testWrongSyntax5()
   {
      try
      {
         String eval = "'c' = 5+2";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of wrong syntax
    */
   @org.junit.Test
   public void testWrongSyntax6()
   {
      try
      {
         String eval = "'m'= Character2 AND (5+2)>3";
         Interpreter.evaluate(eval, testSymbolTable);

         fail("Error: SyntaxError expected but doesnt appear");

      }
      catch (SyntaxError _se)
      {
         // intentionally left empty
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of negative values
    */
   @org.junit.Test
   public void testNegativeValue()
   {
      try
      {
         String eval = "-500 < -60";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with the testSymbolTable to test character value
    */
   @org.junit.Test
   public void testCharacter()
   {
      try
      {
         String eval = "'c' = Character1";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with testSymbolTable to test boolean value
    */
   @org.junit.Test
   public void testVector()
   {
      try
      {
         String eval = "FALSE = Farbliste.isEmpty()";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with testSymbolTable to test long value
    */
   @org.junit.Test
   public void testNegativeLong()
   {
      try
      {
         String eval = "-21<23";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with testSymbolTable to test double value
    */
   @org.junit.Test
   public void testDouble()
   {
      try
      {
         String eval = "0.123 = Double1";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with testSymbolTable to test braces and boolean values
    */
   @org.junit.Test
   public void testVectorBraceAndOr()
   {
      try
      {
         String eval = "(3<5)OR FALSE=Farbliste.isEmpty()AND false = Farbliste.isEmpty()OR(45>3)";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with testSymbolTable to test braces and long values
    */
   @org.junit.Test
   public void testBraceLong()
   {
      try
      {
         String eval = "(-500 = Long1 AND -500 = Long1) OR -500 = Long1";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string with testSymbolTable
    */
   @org.junit.Test
   public void testString()
   {
      try
      {
         String eval = "\"Fritz\" = String1";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * compare this string for DereferencePath with testSymbolTable
    */
   @org.junit.Test
   public void testDereferencePath()
   {
      try
      {
         String eval = "\"Interpreter.interpreterOne.test8()\" = StringPath";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * Comparison of integer object value with testSymbolTable
    */
   @org.junit.Test
   public void testIntegerObject()
   {
      try
      {
         String eval = ("(Integer1 = Integer2)");
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * Comparison of integer and boolean values with testSymbolTable
    */
   @org.junit.Test
   public void testIntegerBoolean()
   {
      try
      {
         String eval = ("Integer1.intValue() < Integer2.intValue()AND(6000 > Integer3.intValue())OR \n" +
               "Double1.doubleValue < Double2.doubleValue");

         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * Comparison of integer, double, boolean and string value with testSymbolTable
    */
   @org.junit.Test
   public void testTypes()
   {
      try
      {
         String eval = ("5 < Integer1.intValue() AND 6000 > Integer3.intValue() OR \n" +
               "Double1.doubleValue() < Double2.doubleValue() AND TRUE = Boolean1.booleanValue() \n" +
               " OR (String1 != String2) AND String1 = \"Markus\"");
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * Comparison with Greater, Less, GreaterEqual and LessEqual
    */
   @org.junit.Test
   public void testComparisonTypes()
   {
      try
      {
         String eval = ("5 < Integer1.intValue() AND 6000 > Integer3.intValue() OR \n" +
               "Double1.doubleValue() < Double2.doubleValue() AND Long2.intValue() >= Long1.intValue() \n" +
               " OR Double1.intValue() <= 0.123");
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * Test with different types of comparison, without whitespace around
    * comparisonsymbols
    */
   @org.junit.Test
   public void testComparisonWithoutWhitespace()
   {
      try
      {
         String eval = ("(45>Integer1.intValue()AND 3000>Integer1.intValue())OR \n" +
               "(Double1.doubleValue()<Double4.doubleValue()AND \n" +
               "Long2.intValue()=>Long1.intValue())OR \n" +
               "Double1.doubleValue()<=0.129");

         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of not equal comparison
    */
   @org.junit.Test
   public void testNotEqualComparison()
   {
      try
      {
         String eval = "5 != 9";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of not equal comparison
    */
   @org.junit.Test
   public void testNotEqualComparison2()
   {
      try
      {
         String eval = "'a' != 'a'";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of not equal comparison
    */
   @org.junit.Test
   public void testNotEqualComparison3()
   {
      try
      {
         String eval = "Farbliste.size() != 0";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of OR operation
    */
   @org.junit.Test
   public void testOR()
   {
      try
      {
         String eval = "Boolean1 = TRUE OR Boolean2 = FALSE";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of OR operation
    */
   @org.junit.Test
   public void testOR2()
   {
      try
      {
         String eval = "(Boolean1 = FALSE OR (Boolean1 = TRUE) )";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test3 of OR operation
    */
   @org.junit.Test
   public void testOR3()
   {
      try
      {
         String eval = "(Boolean1 = FALSE OR Boolean2=TRUE  )";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test3 of OR operation
    */
   @org.junit.Test
   public void testOR4()
   {
      try
      {
         // check if the "OR" in ORANGENPREIS would not recognize as an operator
         String eval = " ORANGENPREIS = \"??\" OR ORANGENPREIS = \"12,--DM\"   ";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test of AND operation
    */
   @org.junit.Test
   public void testAND()
   {
      try
      {
         String eval = "(Boolean2 != Boolean1) AND Farbliste.isEmpty()=TRUE";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test2 of AND operation
    */
   @org.junit.Test
   public void testAND2()
   {
      try
      {
         String eval = "(('c' =Character1 ) OR Character1=Character2 ) AND Double1<= Double2 ";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test3 of AND operation
    */
   @org.junit.Test
   public void testAND3()
   {
      try
      {
         String eval = "('w' ='w'  AND (1<2 OR (2=4)) )";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test Xml paths
    */
   @org.junit.Test
   @Ignore("CRNT-22880")
   public void testXML()
   {
      try
      {
         String eval = "xml.string(//*[local-name()=\"city\"]/text())=\"Frankfurt\"";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);

         String eval2 = "xml.string(//*[local-name()=\"city\"]/text())=\"Frankfurts\"";
         Result result2 = Interpreter.evaluate(eval2, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.FALSE, result2);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * test Xml paths
    */
   @org.junit.Test
   @Ignore("CRNT-22880")
   public void testXML_NS()
   {
      try
      {
         String eval = "xml_ns.string(//*[local-name()=\"city\"]/text())=\"Frankfurt\"";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);

         String eval2 = "xml_ns[xmlns:bla=http:\\\\bla.bla.com\\blub string(//bla:city/text())]=\"Frankfurt\"";
         Result result2 = Interpreter.evaluate(eval2, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result2);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   @org.junit.Test
   public void testBooleanEqual()
   {
      try
      {
         String eval = "5 > 3 = 4 > 3";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   @org.junit.Test
   public void testBooleanNotEqual()
   {
      try
      {
         String eval = "(5 > 3) != (2 > 3)";
         Result result = Interpreter.evaluate(eval, testSymbolTable);

         assertEquals(TEST + this.getName(), Result.TRUE, result);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   /**
    * method to give information about occurred errors
    */
   public static void reportException(Exception x)
   {
      if (x.getClass().equals(EvaluationError.class))
      {
         fail(x.getMessage() + DOUBLETAB + " Errorclass: " + x.getClass());
      }
      else if (x.getClass().equals(SyntaxError.class))
      {
         fail(x.getMessage() + DOUBLETAB + " Errorclass: " + x.getClass());
      }
      else
      {
         fail(x.getMessage());
      }
   }
}

