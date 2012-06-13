/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.xml.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.extensions.xml.data.XPathEvaluator;
import org.junit.Assert;
import org.junit.Before;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XMLAccessPathTest
{
   private static String ex1 =
         "<AAA>" +
            "<BBB/>" +
            "<CCC id=\"7\"/>" +
            "<BBB/>" +
            "<BBB>Hello</BBB>" +
            "<DDD>" +
               "<EEE/>" +
            "</DDD>" +
            "<CCC/>" +
         "</AAA>";

   private static String ex2 =
         "<ns1:AAA xmlns:ns1=\"http://bla.bla.com\">" +
            "<BBB/>" +
            "<CCC id=\"7\"/>" +
            "<BBB/>" +
            "<BBB>Hello</BBB>" +
            "<DDD>" +
               "<EEE/>" +
            "</DDD>" +
            "<CCC/>" +
         "</ns1:AAA>";

   private XPathEvaluator evaluator;

   @Before
   public void setUp() throws Exception
   {
      evaluator = new XPathEvaluator();
   }

/*   public void testSelectNumber()
   {
      Object result1 = evaluator.evaluate(null, ex3, "number('1.233324242exp2')");
      System.out.println("result1 = " + result1);
      Object result2 = evaluator.evaluate(null, ex3, "number('1.23332424244242448E17')");
      System.out.println("result2 = " + result2);
   }*/

   @org.junit.Test
   public void testAbla()
   {
      String stringDoc = "<ns1:data xmlns:ns1=\"http://www.data.ro\" xmlns:xsi=\"http://www.schema.ro\"><ns6:in_string xsi:type=\"xsd:string\" xmlns:ns6=\"http://www.carnot.ag/wfxml/3.1/second/result/\">ddd</ns6:in_string></ns1:data>";
      Document doc = XmlUtils.parseString(stringDoc);
      Element root = doc.getDocumentElement();
      Element start = (Element) root.getFirstChild();
      Object result = evaluator.evaluate(null, start, "xmlns:fix=http://www.data.ro xmlns:bla=http://www.carnot.ag/wfxml/3.1/second/result/ string(text())");
      Assert.assertEquals("Value", "ddd", result);
   }

   @org.junit.Test
   public void testSetValue()
   {
      Object result = evaluator.evaluate(null, null, "", ex1);
      Assert.assertTrue("Expected an " + String.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof String);
//      Assert.assertTrue("Expected an XML Document", ((String) result).startsWith("<?xml "));
   }

   @org.junit.Test
   public void testSelectUsingNamespaces()
   {
      Object result = evaluator.evaluate(null, ex2, "xmlns:bla=http://bla.bla.com /bla:AAA");
      if (result instanceof List)
      {
         result = ((List) result).get(0);
      }
      Assert.assertNotNull("Failed to select the root.", result);
      Assert.assertTrue("Expected an " + Element.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof Element);
      Assert.assertEquals("Element name", "AAA", ((Element) result).getLocalName());
   }

   @org.junit.Test
   public void testSelectInexistentElement()
   {
      Object result = evaluator.evaluate(null, ex1, "/ZZZ");
      List l = (List) result;
      assertThat(l.isEmpty(), is(true));
   }

   @org.junit.Test
   public void testSelectRoot()
   {
      Object result = evaluator.evaluate(null, ex1, "/AAA");
      if (result instanceof List)
      {
         result = ((List) result).get(0);
      }
      Assert.assertNotNull("Failed to select the root.", result);
      Assert.assertTrue("Expected an " + Element.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof Element);
      Assert.assertEquals("Element name", "AAA", ((Element) result).getTagName());
   }

   @org.junit.Test
   public void testSelectDeepPath()
   {
      Object result = evaluator.evaluate(null, ex1, "/AAA/DDD/EEE");
      if (result instanceof List)
      {
         result = ((List) result).get(0);
      }
      Assert.assertNotNull("Failed to select the root.", result);
      Assert.assertTrue("Expected an " + Element.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof Element);
      Assert.assertEquals("Element name", "EEE", ((Element) result).getTagName());
   }

   @org.junit.Test
   public void testSelectMultipleElements()
   {
      Object result = evaluator.evaluate(null, ex1, "/AAA/CCC");
      Assert.assertNotNull("Failed to select values.", result);
      Assert.assertTrue("Expected an " + NodeList.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof List);
      List list = (List) result;
      Assert.assertEquals("Count: ", 2, list.size());
      for (int i = 0; i < list.size(); i++)
      {
         Node node = (Node) list.get(i);
         Assert.assertTrue("Expected an " + Element.class.getName() + ", not a "
               + node.getClass().getName(), node instanceof Element);
         Assert.assertEquals("Element name", "CCC", ((Element) node).getTagName());
      }
   }

   @org.junit.Test
   public void testSelectAttribute()
   {
      Object result = evaluator.evaluate(null, ex1, "/AAA/CCC/@id");
      if (result instanceof List)
      {
         result = ((List) result).get(0);
      }
      Assert.assertNotNull("Failed to select the attribute.", result);
      Assert.assertTrue("Expected an " + Attr.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof Attr);
      Assert.assertEquals("Attribute name", "id", ((Attr) result).getName());
   }

   @org.junit.Test
   public void testSelectElementNode()
   {
      Object result = evaluator.evaluate(null, ex1, "/AAA/BBB[last()]/text()");
      Assert.assertNotNull("Failed to select the text.", result);
      Assert.assertTrue("Expected an " + List.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof List);
      Assert.assertEquals(1, ((List) result).size());
      Assert.assertTrue("Expected a " + Text.class.getName() + " element, not a "
            + ((List) result).get(0).getClass().getName(), ((List) result).get(0) instanceof Text);
      Assert.assertEquals("Text", "Hello", ((Text) ((List) result).get(0)).getWholeText());
   }

   @org.junit.Test
   public void testSelectElementValue()
   {
      Object result = evaluator.evaluate(null, ex1, "string(/AAA/BBB[last()]/text())");
      Assert.assertNotNull("Failed to select the text.", result);
      Assert.assertTrue("Expected an " + String.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof String);
      Assert.assertEquals("Text", "Hello", result);
   }

   @org.junit.Test
   public void testSelectAttributeNode()
   {
      Object result = evaluator.evaluate(null, ex1, "/AAA/CCC/@id");
      Assert.assertNotNull("Failed to select the attribute value.", result);
      Assert.assertTrue("Expected an " + List.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof List);
      Assert.assertEquals(1, ((List) result).size());
      Assert.assertTrue("Expected a " + Attr.class.getName() + " element, not a "
            + ((List) result).get(0).getClass().getName(), ((List) result).get(0) instanceof Attr);
      Assert.assertEquals("Attribute value", "7", ((Attr) ((List) result).get(0)).getValue());
   }

   @org.junit.Test
   public void testSelectAttributeValue()
   {
      Object result = evaluator.evaluate(null, ex1, "number(/AAA/CCC/@id)");
      Assert.assertNotNull("Failed to select the attribute value.", result);
      Assert.assertTrue("Expected an " + Double.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof Double);
      Assert.assertEquals("Attribute value", new Double(7), result);
   }

   @org.junit.Test
   public void testSetAttributeValue()
   {
      Object changed = evaluator.evaluate(null, ex1, "/AAA/CCC/@id", new Integer(33));
      Object result = evaluator.evaluate(null, changed, "/AAA/CCC/@id");
      Assert.assertNotNull("Failed to select the attribute value.", result);
      Assert.assertTrue("Expected an " + List.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof List);
      Assert.assertEquals(1, ((List) result).size());
      Assert.assertTrue("Expected a " + Attr.class.getName() + " element, not a "
            + ((List) result).get(0).getClass().getName(), ((List) result).get(0) instanceof Attr);
      Assert.assertEquals("Attribute value", "33", ((Attr) ((List) result).get(0)).getValue());
   }

   @org.junit.Test
   public void testSetElementText()
   {
      Object changed = evaluator.evaluate(null, ex1, "/AAA/BBB[1]", "Okidoki");
      Object result = evaluator.evaluate(null, changed, "/AAA/BBB[1]/text()");
      Assert.assertNotNull("Failed to select the attribute value.", result);
      Assert.assertTrue("Expected an " + List.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof List);
      Assert.assertEquals(1, ((List) result).size());
      Assert.assertTrue("Expected a " + Text.class.getName() + " element, not a "
            + ((List) result).get(0).getClass().getName(), ((List) result).get(0) instanceof Text);
      Assert.assertEquals("Text", "Okidoki", ((Text) ((List) result).get(0)).getWholeText());
   }

   @org.junit.Test
   public void testSetValueWithNameRestriction()
   {
      Map restrictions = new HashMap();

      restrictions.put(PredefinedConstants.PLAINXML_TYPE_ID_ATT, "AAA");
      Object result = evaluator.evaluate(restrictions, null, "", ex1);
      if (result instanceof List)
      {
         result = ((List) result).get(0);
      }
      Assert.assertTrue("Expected an " + String.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof String);
//      Assert.assertTrue("Expected an XML Document", ((String) result).startsWith("<?xml "));

      result = evaluator.evaluate(restrictions, null, "", ex2);
      Assert.assertTrue("Expected an " + String.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof String);
//      Assert.assertTrue("Expected an XML Document", ((String) result).startsWith("<?xml "));

      try
      {
         restrictions.put(PredefinedConstants.PLAINXML_TYPE_ID_ATT, "NNN");
         result = evaluator.evaluate(restrictions, null, "", ex1);
         fail("Expected PublicException.");
      }
      catch (Exception ex)
      {
         Assert.assertTrue("Expected a org.eclipse.stardust.common.error.PublicException but was "
               + ex.getClass().getName(), ex instanceof PublicException);
         Assert.assertTrue("Error message:",
               ex.getMessage().contains("The resulting document does not have a root of type NNN"));
      }

      try
      {
         restrictions.put(PredefinedConstants.PLAINXML_TYPE_ID_ATT, "{http://bla.bla.com}AAA");
         result = evaluator.evaluate(restrictions, null, "", ex1);
         fail("Expected PublicException.");
      }
      catch (Exception ex)
      {
         Assert.assertTrue("Expected a org.eclipse.stardust.common.error.PublicException but was "
               + ex.getClass().getName(), ex instanceof PublicException);
         Assert.assertTrue("Error message:",
               ex.getMessage().contains("The resulting document does not have a root of type {http://bla.bla.com}AAA"));
      }

      result = evaluator.evaluate(restrictions, null, "", ex2);
      Assert.assertTrue("Expected an " + String.class.getName() + ", not a "
            + result.getClass().getName(), result instanceof String);
//      Assert.assertTrue("Expected an XML Document", ((String) result).startsWith("<?xml "));

      try
      {
         restrictions.put(PredefinedConstants.PLAINXML_TYPE_ID_ATT, "{http://bla.blup.com}AAA");
         result = evaluator.evaluate(restrictions, null, "", ex2);
         fail("Expected PublicException.");
      }
      catch (Exception ex)
      {
         Assert.assertTrue("Expected a org.eclipse.stardust.common.error.PublicException but was "
               + ex.getClass().getName(), ex instanceof PublicException);
         Assert.assertTrue("Error message:",
               ex.getMessage().contains("The resulting document does not have a root of type {http://bla.blup.com}AAA"));
      }

      try
      {
         restrictions.put(PredefinedConstants.PLAINXML_TYPE_ID_ATT, "{http://bla.bla.com}NNN");
         result = evaluator.evaluate(restrictions, null, "", ex2);
         fail("Expected PublicException.");
      }
      catch (Exception ex)
      {
         Assert.assertTrue("Expected a org.eclipse.stardust.common.error.PublicException but was "
               + ex.getClass().getName(), ex instanceof PublicException);
         Assert.assertTrue("Error message:",
               ex.getMessage().contains("The resulting document does not have a root of type {http://bla.bla.com}NNN"));
      }
   }
}
