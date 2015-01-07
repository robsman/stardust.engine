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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Scripting;
import org.eclipse.stardust.engine.core.compatibility.el.DataTypeResolver;
import org.eclipse.stardust.engine.core.compatibility.el.EvaluationError;
import org.eclipse.stardust.engine.core.compatibility.el.Interpreter;
import org.eclipse.stardust.engine.core.compatibility.el.JsConverter;
import org.eclipse.stardust.engine.core.compatibility.el.Result;
import org.eclipse.stardust.engine.core.compatibility.el.SyntaxError;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.builder.ModelBuilder;
import org.eclipse.stardust.engine.core.pojo.data.Type;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *  Test class for the class Converter
 */
public class ConverterTest extends TestCase
{
   protected static final String WHITESPACE = " ";
   protected static final String DOUBLETAB = "    ";
   protected static final String TEST = "Test: ";
   
   private static final Scripting ECMA_SCRIPT = new Scripting(Scripting.ECMA_SCRIPT, null, null);
   private static final ModelBuilder BUILDER = DefaultModelBuilder.create();

   private ITransition transition;
   private JsConverter converter;
   private IModel model;
   private TestSymbolTable testSymbolTable;
   
   public ConverterTest(String name) throws EvaluationError, SyntaxError
   {
      super(name);
   }

   protected void setUp()
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

      List/*<String>*/ vector1 = new ArrayList/*<String>*/();
      vector1.add("Blau");
      vector1.add("Grün");
      vector1.add("Gelb");
      vector1.add("Orange");
      vector1.add("Schwarz");
      testSymbolTable.registerSer("Farbliste1", vector1);

      List/*<String>*/ vector2 = new ArrayList/*<String>*/();
      vector2.add("Blau");
      vector2.add("Grün");
      vector2.add("Gelb");
      vector2.add("Orange");
      vector2.add("Schwarz");
      testSymbolTable.registerSer("Farbliste2", vector2);

      List/*<String>*/ vector3 = new ArrayList/*<String>*/();
      vector3.add("Blau");
      vector3.add("Gelb");
      vector3.add("Orange");
      vector3.add("Schwarz");
      testSymbolTable.registerSer("Farbliste3", vector3);

      // add TestString
      testSymbolTable.registerSer("StringPath", new String("Interpreter.interpreterOne.test8()"));

      // add an xml data
      testSymbolTable.registerXml("xml", "<city>Frankfurt</city>");
      testSymbolTable.registerXml("xml_ns", "<cx:city xmlns:cx=\"http://bla.bla.com/blub\">Frankfurt</cx:city>");
      testSymbolTable.registerXml("xml2",
            "<address>" +
            "   <street>Solmsstrasse</street>" +
            "   <streetno>18</streetno>" +
            "   <city>Frankfurt am Main</city>" +
            "   <plz>60486</plz>" +
            "</address>");

      model = BUILDER.createModel("model");
      model.setScripting(ECMA_SCRIPT);

      BUILDER.createPrimitiveData(model, "Character1", "Character1", Type.Char, null);
      BUILDER.createPrimitiveData(model, "Character2", "Character2", Type.Char, null);
      BUILDER.createPrimitiveData(model, "Character3", "Character3", Type.Char, null);
      BUILDER.createPrimitiveData(model, "Double1", "Double1", Type.Double, null);
      BUILDER.createPrimitiveData(model, "Double2", "Double2", Type.Double, null);
      BUILDER.createPrimitiveData(model, "Double3", "Double3", Type.Double, null);
      BUILDER.createPrimitiveData(model, "Double4", "Double4", Type.Double, null);
      BUILDER.createPrimitiveData(model, "Integer1", "Integer1", Type.Integer, null);
      BUILDER.createPrimitiveData(model, "Integer2", "Integer2", Type.Integer, null);
      BUILDER.createPrimitiveData(model, "Integer3", "Integer3", Type.Integer, null);
      BUILDER.createPrimitiveData(model, "Integer4", "Integer4", Type.Integer, null);
      BUILDER.createPrimitiveData(model, "Long1", "Long1", Type.Long, null);
      BUILDER.createPrimitiveData(model, "Long2", "Long2", Type.Long, null);
      BUILDER.createPrimitiveData(model, "Boolean1", "Boolean1", Type.Boolean, null);
      BUILDER.createPrimitiveData(model, "Boolean2", "Boolean2", Type.Boolean, null);
      BUILDER.createPrimitiveData(model, "String1", "String1", Type.String, null);
      BUILDER.createPrimitiveData(model, "String2", "String2", Type.String, null);
      BUILDER.createPrimitiveData(model, "String3", "String3", Type.String, null);
      BUILDER.createPrimitiveData(model, "String4", "String4", Type.String, null);
      BUILDER.createPrimitiveData(model, "StringPath", "StringPath", Type.String, null);
      BUILDER.createPrimitiveData(model, "ORANGENPREIS", "Orangenpreis", Type.String, null);
      BUILDER.createPrimitiveData(model, "sym_bol_5", "sym_bol_5", Type.Double, null);
      BUILDER.createSerializableData(model, "Farbliste1", "Farbliste1", List.class.getName());
      BUILDER.createSerializableData(model, "Farbliste2", "Farbliste2", List.class.getName());
      BUILDER.createSerializableData(model, "Farbliste3", "Farbliste3", List.class.getName());
      BUILDER.createPlainXMLData(model, "xml", "xml");
      BUILDER.createPlainXMLData(model, "xml_ns", "xml_ns");
      BUILDER.createPlainXMLData(model, "xml2", "xml2");
      
      IProcessDefinition process = BUILDER.createProcessDefinition(model, "process");
      IActivity fromActivity = BUILDER.createRouteActivity(process);
      IActivity toActivity = BUILDER.createRouteActivity(process);
      transition = BUILDER.createTransition(fromActivity, toActivity, "true");
      
      converter = new JsConverter(new DataTypeResolver()
      {
         public String resolveDataType(String dataId)
         {
            IData data = model.findData(dataId);
            if (data == null)
            {
               return null;
            }
            PluggableType type = data.getType();
            if (type == null)
            {
               return null;
            }
            return type.getId();
         }
      });      
   }

   protected void tearDown()
   {
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite(ConverterTest.class);
      return suite;
   }

   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public void testValidSymbolName()
   {
      String source = "sym_bol_5 > 4";
      String expected = "sym_bol_5 > 4.0";
      doConvertTest(source, expected);
   }

   public void testGreaterComparison()
   {
      String source = "5 > 4";
      String expected = "5.0 > 4.0";
      doConvertTest(source, expected);
   }

   public void testGreaterComparison2()
   {
      String source = "5 > 6";
      String expected = "5.0 > 6.0";
      doConvertTest(source, expected);
   }

   public void testGreaterComparison3()
   {
      String source = "(7.0 > 6.5)";
      String expected = "7.0 > 6.5";
      doConvertTest(source, expected);
   }

   public void testGreaterComparison4()
   {
      String source = "((7.0 >6.999999999999))";
      String expected = "7.0 > 6.999999999999";
      doConvertTest(source, expected);
   }

   public void testGreaterComparison5()
   {
      String source = "    ( 7           >  6.9               )      ";
      String expected = "7.0 > 6.9";
      doConvertTest(source, expected);
   }

   public void testLessComparison()
   {
      String source = "3 < 4";
      String expected = "3.0 < 4.0";
      doConvertTest(source, expected);
   }

   public void testLessComparison2()
   {
      String source = "5 < 4";
      String expected = "5.0 < 4.0";
      doConvertTest(source, expected);
   }

   public void testEqualComparison()
   {
      String source = "'c'='c'";
      String expected = "'c' == 'c'";
      doConvertTest(source, expected);
   }

   public void testEqualComparison2()
   {
      String source = "'c' = 'C'";
      String expected = "'c' == 'C'";
      doConvertTest(source, expected);
   }

   public void testEqualComparison3()
   {
      String source = "String3 = \"brown\"";
      String expected = "String3 == \"brown\"";
      doConvertTest(source, expected);
   }

   public void testLessOrEqualComparison()
   {
      String source = "3 <= 4";
      String expected = "3.0 <= 4.0";
      doConvertTest(source, expected);
   }

   public void testLessOrEqualComparison2()
   {
      String source = "5<=4";
      String expected = "5.0 <= 4.0";
      doConvertTest(source, expected);
   }

   public void testGreaterOrEqualComparison()
   {
      String source = "4.5>=4.5";
      String expected = "4.5 >= 4.5";
      doConvertTest(source, expected);
   }

   public void testGreaterOrEqualComparison2()
   {
      String source = "3.5>=4.5";
      String expected = "3.5 >= 4.5";
      doConvertTest(source, expected);
   }

   public void testUnsupportedChars()
   {
      String source = "( 7 >$ 6)";
      doFailTest(source);
   }

   public void testUnsupportedChars2()
   {
      String source = "b.isEmpty() AND !a.isEmpty()";
      doFailTest(source);
   }

   public void testWrongBraceNumber()
   {
      String source = "(( 1= 1 ) AND a.isEmpty()))";
      doFailTest(source);
   }

   public void testWrongBraceNumber2()
   {
      String source = "(( 1= 1 ) AND ( 0<2)";
      doFailTest(source);
   }

   public void testWrongSyntax()
   {
      String source = "(( 1= 1 ) ANDa.isEmpty()=TRUE )";
      doFailTest(source);
   }

   public void testWrongSyntax2()
   {
      String source = "(( 1= 1 ) a.isEmpty()=TRUE )";
      doFailTest(source);
   }

   public void testWrongSyntax3()
   {
      String source = "(( 1= 1 ) Oberschlau a.isEmpty()=TRUE )";
      doFailTest(source);
   }

   public void testWrongSyntax4()
   {
      String source = "'c' = 'Xs and 'b' = 'c'";
      doFailTest(source);
   }

   public void testWrongSyntax5()
   {
      String source = "'c' = 5+2";
      doFailTest(source);
   }

   public void testWrongSyntax6()
   {
      String source = "'m'= Character2 AND (5+2)>3";
      doFailTest(source);
   }

   public void testNegativeValue()
   {
      String source = "-500 < -60";
      String expected = "-500.0 < -60.0";
      doConvertTest(source, expected);
   }

   public void testCharacter()
   {
      String source = "'c' = Character1";
      String expected = "'c' == Character1";
      doConvertTest(source, expected);
   }

   public void testVector()
   {
      String source = "FALSE = Farbliste1.isEmpty()";
      String expected = "false == Farbliste1.isEmpty()";
      doConvertTest(source, expected);
   }

   public void testVector2()
   {
      String source = "\"Blau\" = Farbliste1.iterator().next()";
      String expected = "\"Blau\" == Farbliste1.iterator().next()";
      doConvertTest(source, expected, true);
   }

   public void testVector3()
   {
      String source = "\"Gelb\" = Farbliste1.iterator().next()";
      String expected = "\"Gelb\" == Farbliste1.iterator().next()";
      doConvertTest(source, expected, false);
   }

   public void testVector4()
   {
      String source = "Farbliste1 = Farbliste2";
      String expected = "Farbliste1 == Farbliste2";
      doConvertTest(source, expected, false);
   }

   public void testVector5()
   {
      String source = "Farbliste1 = Farbliste3";
      String expected = "Farbliste1 == Farbliste3";
      doConvertTest(source, expected, false);
   }

   public void testNegativeLong()
   {
      String source = "-21<23";
      String expected = "-21.0 < 23.0";
      doConvertTest(source, expected);
   }

   public void testDouble()
   {
      String source = "0.123 = Double1";
      String expected = "0.123 == Double1";
      doConvertTest(source, expected);
   }

   public void testVectorBraceAndOr()
   {
      String source = "(3<5)OR FALSE=Farbliste1.isEmpty()AND false = Farbliste1.isEmpty()OR(45>3)";
      String expected = "(3.0 < 5.0) || (((false == Farbliste1.isEmpty()) && (false == Farbliste1.isEmpty())) || (45.0 > 3.0))";
      doConvertTest(source, expected);
   }

   public void testBraceLong()
   {
      String source = "(-500 = Long1 AND -500 = Long1) OR -500 = Long1";
      String expected = "((-500.0 == Long1) && (-500.0 == Long1)) || (-500.0 == Long1)";
      doConvertTest(source, expected);
   }

   public void testString()
   {
      String source = "\"Fritz\" = String1";
      String expected = "\"Fritz\" == String1";
      doConvertTest(source, expected);
   }

   public void testDereferencePath()
   {
      String source = "\"Interpreter.interpreterOne.test8()\" = StringPath";
      String expected = "\"Interpreter.interpreterOne.test8()\" == StringPath";
      doConvertTest(source, expected);
   }

   public void testIntegerObject()
   {
      String source = "(Integer1 = Integer2)";
      String expected = "Integer1 == Integer2";
      doConvertTest(source, expected);
   }

   public void testIntegerBoolean()
   {
      String source = "Integer1.intValue() < Integer2.intValue()AND(6000 > Integer3.intValue())OR \n" +
            "Double1.doubleValue < Double2.doubleValue";
      String expected = "((Integer1.intValue() < Integer2.intValue()) && (6000.0 > Integer3.intValue())) || " +
            "(Double1.doubleValue < Double2.doubleValue)";
      doConvertTest(source, expected);
   }

   public void testTypes()
   {
      String source = "5 < Integer1.intValue() AND 6000 > Integer3.intValue() OR \n" +
            "Double1.doubleValue() < Double2.doubleValue() AND TRUE = Boolean1.booleanValue() \n" +
            " OR (String1 != String2) AND String1 = \"Markus\"";
      String expected = "((5.0 < Integer1.intValue()) && (6000.0 > Integer3.intValue())) || " +
            "(((Double1.doubleValue() < Double2.doubleValue()) && (true == Boolean1.booleanValue())) " +
            "|| ((String1 != String2) && (String1 == \"Markus\")))";
      doConvertTest(source, expected);
   }

   public void testComparisonTypes()
   {
      String source = "5 < Integer1.intValue() AND 6000 > Integer3.intValue() OR \n" +
            "Double1.doubleValue() < Double2.doubleValue() AND Long2.intValue() >= Long1.intValue() \n" +
            " OR Double1.intValue() <= 0.123";
      String expected = "((5.0 < Integer1.intValue()) && (6000.0 > Integer3.intValue())) || " +
            "(((Double1.doubleValue() < Double2.doubleValue()) && (Long2.intValue() >= Long1.intValue())) " +
            "|| (Double1.intValue() <= 0.123))";
      doConvertTest(source, expected);
   }

   public void testComparisonWithoutWhitespace()
   {
      String source = "(45>Integer1.intValue()AND 3000>Integer1.intValue())OR \n" +
            "(Double1.doubleValue()<Double4.doubleValue()AND \n" +
            "Long2.intValue()=>Long1.intValue())OR \n" +
            "Double1.doubleValue()<=0.129";
      String expected = "((45.0 > Integer1.intValue()) && (3000.0 > Integer1.intValue())) || " +
            "(((Double1.doubleValue() < Double4.doubleValue()) && " +
            "(Long2.intValue() >= Long1.intValue())) || " +
            "(Double1.doubleValue() <= 0.129))";
      doConvertTest(source, expected);
   }

   public void testNotEqualComparison()
   {
      String source = "5 != 9";
      String expected = "5.0 != 9.0";
      doConvertTest(source, expected);
   }

   public void testNotEqualComparison2()
   {
      String source = "'a' != 'a'";
      String expected = "'a' != 'a'";
      doConvertTest(source, expected);
   }

   public void testNotEqualComparison3()
   {
      String source = "Farbliste1.size() != 0";
      String expected = "Farbliste1.size() != 0.0";
      doConvertTest(source, expected);
   }

   public void testOR()
   {
      String source = "Boolean1 = TRUE OR Boolean2 = FALSE";
      String expected = "(Boolean1 == true) || (Boolean2 == false)";
      doConvertTest(source, expected);
   }

   public void testOR2()
   {
      String source = "(Boolean1 = FALSE OR (Boolean1 = TRUE) )";
      String expected = "(Boolean1 == false) || (Boolean1 == true)";
      doConvertTest(source, expected);
   }

   public void testOR3()
   {
      String source = "(Boolean1 = FALSE OR Boolean2=TRUE  )";
      String expected = "(Boolean1 == false) || (Boolean2 == true)";
      doConvertTest(source, expected);
   }

   public void testOR4()
   {
      String source = " ORANGENPREIS = \"??\" OR ORANGENPREIS = \"12,--DM\"   ";
      String expected = "(ORANGENPREIS == \"??\") || (ORANGENPREIS == \"12,--DM\")";
      doConvertTest(source, expected);
   }

   public void testAND()
   {
      String source = "(Boolean2 != Boolean1) AND Farbliste1.isEmpty()=TRUE";
      String expected = "(Boolean2 != Boolean1) && (Farbliste1.isEmpty() == true)";
      doConvertTest(source, expected);
   }

   public void testAND2()
   {
      String source = "(('c' =Character1 ) OR Character1=Character2 ) AND Double1<= Double2 ";
      String expected = "(('c' == Character1) || (Character1 == Character2)) && (Double1 <= Double2)";
      doConvertTest(source, expected);
   }

   public void testAND3()
   {
      String source = "('w' ='w'  AND (1<2 OR (2=4)) )";
      String expected = "('w' == 'w') && ((1.0 < 2.0) || (2.0 == 4.0))";
      doConvertTest(source, expected);
   }

   public void testXML()
   {
      String source = "xml.string(//*[local-name()=\"city\"]/text())=\"Frankfurt\"";
      String expected = "xml[\"string(//*[local-name()=\" + '\"' + \"city\" + '\"' + \"]/text())\"] == \"Frankfurt\"";
      doConvertTest(source, expected);
   }

   public void testXML2()
   {
      String source = "xml.string(//*[local-name()=\"city\"]/text())=\"Frankfurts\"";
      String expected = "xml[\"string(//*[local-name()=\" + '\"' + \"city\" + '\"' + \"]/text())\"] == \"Frankfurts\"";
      doConvertTest(source, expected);
   }

   public void testXML3()
   {
      String source = "xml2.city=\"Frankfurt am Main\"";
      String expected = "xml2[\"city\"] == \"Frankfurt am Main\"";
      doConvertTest(source, expected, true);
   }

   public void testXML_NS()
   {
      String source = "xml_ns.string(//*[local-name()=\"city\"]/text())=\"Frankfurt\"";
      String expected = "xml_ns[\"string(//*[local-name()=\" + '\"' + \"city\" + '\"' + \"]/text())\"] == \"Frankfurt\"";
      doConvertTest(source, expected);
   }

   public void testXML_NS2()
   {
      String source = "xml_ns[xmlns:bla=http://bla.bla.com/blub string(//bla:city/text())]=\"Frankfurt\"";
      String expected = "xml_ns[\"xmlns:bla=http://bla.bla.com/blub string(//bla:city/text())\"] == \"Frankfurt\"";
      doConvertTest(source, expected);
   }

   public void testBooleanEqual()
   {
      String source = "5 > 3 = 4 > 3";
      String expected = "(5.0 > 3.0) == (4.0 > 3.0)";
      doConvertTest(source, expected);
   }

   public void testBooleanNotEqual()
   {
      String source = "(5 > 3) != (2 > 3)";
      String expected = "(5.0 > 3.0) != (2.0 > 3.0)";
      doConvertTest(source, expected);
   }

   private void doConvertTest(String source, String expected)
   {
      try
      {
         doConvertTest(source, expected, Interpreter.evaluate(source, testSymbolTable) == Result.TRUE);
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   private void doConvertTest(String source, String expected, boolean result)
   {
      try
      {
         String label = TEST + this.getName();
         transition.setCondition(converter.convert(source));
         assertEquals(label, expected, transition.getCondition());
         assertEquals(label, result, transition.isEnabled(testSymbolTable));
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   private void doFailTest(String source)
   {
      try
      {
         String label = TEST + this.getName();
         String expected = PredefinedConstants.CARNOT_EL_PREFIX + source;
         transition.setCondition(converter.convert(source));
         assertEquals(label, expected, transition.getCondition());
      }
      catch (Exception x)
      {
         reportException(x);
      }
   }

   public static void reportException(Exception x)
   {
      x.printStackTrace();
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

