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
package org.eclipse.stardust.engine.core.struct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import junit.framework.TestCase;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.xsd.XSDSchema;


public class StructuredDataConverterTest extends TestCase
{
   private final static SimpleDateFormat XSD_DATE_TIME_FORMAT = new SimpleDateFormat(
         "yyyy-MM-dd'T'hh:mm:ss");

   public StructuredDataConverterTest(String name)
   {
      super(name);
   }

   public void testDomToCollectionWholeDocument() throws Exception
   {
      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/orderbook_elements.xsd");
      Set xPaths = XPathFinder.findAllXPaths(xsdSchema, "orderbook", false);
      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Document originalDocument = DocumentBuilder.buildDocument(XPathFinderTest.class.getResource("orderbook_elements.xml")
                  .openStream());

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(
            xPathMap);

      verifyCollection(structuredDataConverter, originalDocument.getRootElement());
   }

   private void verifyCollection(StructuredDataConverter structuredDataConverter,
         Element rootNode) throws Exception
   {
      Map order1 = (Map) structuredDataConverter.toCollection(rootNode, "order[1]", true);

      assertEquals(3, order1.size());
      assertTrue(order1.containsKey("customer"));
      assertEquals("N1", order1.get("@ordernr"));
      assertEquals(new Integer(100), order1.get("@qty"));

      Map customer = (Map) order1.get("customer");
      assertEquals("Smith", customer.get("@name"));

      List addresses = (List) customer.get("address");
      assertEquals(2, addresses.size());
      assertEquals(
            "Baker NORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTH",
            ((Map) addresses.get(0)).get("@street"));
      assertEquals("North", ((Map) addresses.get(1)).get("@street"));

      assertEquals(new Integer(200), structuredDataConverter.toCollection(rootNode,
            "order[2]/@qty", true));
      assertEquals("North", structuredDataConverter.toCollection(rootNode,
            "order[1]/customer/address[2]/@street", true));
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
      assertEquals(df.parseObject("1979-04-07T01:02:03"),
            structuredDataConverter.toCollection(rootNode, "@date", true));

      try
      {
         structuredDataConverter.toCollection(rootNode, "NO_FIELD", true);
         fail("Should throw exception instead");
      }
      catch (Exception e)
      {
         assertEquals(PublicException.class, e.getClass());
      }

      assertNull(structuredDataConverter.toCollection(rootNode, "order[3]/@qty", true));

      List addressesOrder1 = (List) structuredDataConverter.toCollection(rootNode,
            "order[1]/customer/address", true);

      assertEquals(2, addressesOrder1.size());
      assertEquals(
            "Baker NORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTH",
            ((Map) addressesOrder1.get(0)).get("@street"));
      assertEquals("North", ((Map) addressesOrder1.get(1)).get("@street"));

      List addressesOrder2 = (List) structuredDataConverter.toCollection(rootNode,
            "order[2]/customer/address", true);

      assertEquals(1, addressesOrder2.size());
      assertEquals("North", ((Map) addressesOrder2.get(0)).get("@street"));

      Map wholeData = (Map) structuredDataConverter.toCollection(rootNode, "", true);

      assertEquals(createOrderBookCollection().get("order"), wholeData.get("order"));
      assertTrue(wholeData.get("@date") instanceof Date);
      assertEquals("OPEN", wholeData.get("@status"));

      assertEquals(structuredDataConverter.toCollection(rootNode, null, true),
            structuredDataConverter.toCollection(rootNode, "", true));
      assertEquals(structuredDataConverter.toCollection(rootNode, null, true),
            structuredDataConverter.toCollection(rootNode, ".", true));

      assertEquals(3, ((List) structuredDataConverter.toCollection(rootNode,
            "order/customer/address", true)).size());

      List allNames = (List) structuredDataConverter.toCollection(rootNode,
            "order/customer/@name", true);
      assertEquals(2, allNames.size());
      assertEquals("Smith", allNames.get(0));
      assertEquals("James", allNames.get(1));

      assertEquals(new Integer(100), structuredDataConverter.toCollection(rootNode,
            "order[1.0]/@qty", true));
   }

   public void testCollectionToDom() throws Exception
   {
      Transformer serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");

      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/orderbook_elements.xsd");
      Set xPaths = XPathFinder.findAllXPaths(xsdSchema, "orderbook", false);

      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      Document originalDocument = DocumentBuilder.buildDocument(XPathFinderTest.class.getResource("orderbook_elements.xml")
                  .openStream());

      Map orderBook = createOrderBookCollection();

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(
            xPathMap);
      Node[] nodes = structuredDataConverter.toDom(orderBook, "", true);
      assertEquals(1, nodes.length);
      System.out.println("original document: "+originalDocument.toXML());
      System.out.println("result document: "+nodes[0].toXML());
      this.verifyCollection(structuredDataConverter, (Element) nodes[0]);

      nodes = structuredDataConverter.toDom(orderBook.get("order"), "order", true);
      assertEquals(2, nodes.length);
      for (int i = 0; i < nodes.length; i++ )
      {
         System.out.println("result document[" + i + "]: "+nodes[i].toXML());
      }

      nodes = structuredDataConverter.toDom(new Integer(100), "order[2]/@qty", true);
      assertEquals(1, nodes.length);
      assertEquals("100", StructuredDataXPathUtils.findNodeValue(nodes[0]));

      nodes = structuredDataConverter.toDom(
            XSD_DATE_TIME_FORMAT.parse("1979-04-07T01:02:03"), "@date", true);
      assertEquals(1, nodes.length);
      assertEquals("1979-04-07T01:02:03",
            StructuredDataXPathUtils.findNodeValue(nodes[0]));

      try
      {
         nodes = structuredDataConverter.toDom(new Integer(100), "NO_SUCH_FIELD", true);
         fail("Should throw exception instead");
      }
      catch (Exception e)
      {
         assertEquals(PublicException.class, e.getClass());
      }

      nodes = structuredDataConverter.toDom(orderBook, null, true);
      assertEquals(1, nodes.length);
      this.verifyCollection(structuredDataConverter, (Element) nodes[0]);
   }

   public static Map createOrderBookCollection() throws ParseException
   {
      Map addressBaker = new HashMap();
      addressBaker.put(
            "@street",
            "Baker NORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTHNORTH");
      Map addressNorth = new HashMap();
      addressNorth.put("@street", "North");

      Map customer1 = new HashMap();
      List addresses1 = new ArrayList();
      addresses1.add(addressBaker);
      addresses1.add(addressNorth);
      customer1.put("address", addresses1);
      customer1.put("@name", "Smith");

      Map customer2 = new HashMap();
      List addresses2 = new ArrayList();
      addresses2.add(addressNorth);
      customer2.put("address", addresses2);
      customer2.put("@name", "James");

      Map order1 = new HashMap();
      order1.put("customer", customer1);
      order1.put("@qty", new Integer(100));
      order1.put("@ordernr", "N1");

      Map order2 = new HashMap();
      order2.put("customer", customer2);
      order2.put("@qty", new Integer(200));
      order2.put("@ordernr", "N2");

      List orders = new ArrayList();
      orders.add(order1);
      orders.add(order2);

      Map orderBook = new HashMap();
      orderBook.put("@date", XSD_DATE_TIME_FORMAT.parse("1979-04-07T01:02:03"));
      orderBook.put("@status", "OPEN");
      orderBook.put("order", orders);

      return orderBook;
   }

   public void testCreateInitialValue1() throws Exception
   {
      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/orderbook_elements.xsd");
      Set xPaths = XPathFinder.findAllXPaths(xsdSchema, "orderbook", false);
      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Object initialValue = StructuredDataXPathUtils.createInitialValue(xPathMap, "");

      Map expectedInitialValue = new HashMap();

      Map address = new HashMap();
      List addresses = new ArrayList();
      addresses.add(address);
      Map customer = new HashMap();
      customer.put("address", addresses);
      Map order = new HashMap();
      order.put("customer", customer);
      List orders = new LinkedList();
      orders.add(order);
      expectedInitialValue.put("order", orders);

      assertEquals(expectedInitialValue, initialValue);

   }

   public void testCreateInitialValue2() throws Exception
   {

      Set /* <TypedXPath> */xPaths = new HashSet();
      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 1, "level2", null,  "", BigData.NULL, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 2, "level2/level3", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 3, "level2/level3/p3", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 4, "level2/level3/p4", null,  "", BigData.SHORT, true, XPathAnnotations.DEFAULT_ANNOTATIONS));

      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Object initialValue = StructuredDataXPathUtils.createInitialValue(xPathMap, "");

      Map expectedInitialValue = new HashMap();

      Map level3 = new HashMap();
      level3.put("p4", new LinkedList());

      List level2list = new LinkedList();
      Map level2 = new HashMap();
      level2.put("level3", level3);
      level2list.add(level2);
      expectedInitialValue.put("level2", level2list);

      assertEquals(expectedInitialValue, initialValue);

   }

   public void testCreateInitialValue3() throws Exception
   {

      Set /* <TypedXPath> */xPaths = new HashSet();
      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 1, "level2", null,  "", BigData.NULL, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 2, "level2/level3", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 3, "level2/level3/p3", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 4, "level2/level3/p4", null,  "", BigData.SHORT, true, XPathAnnotations.DEFAULT_ANNOTATIONS));

      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Object initialValue = StructuredDataXPathUtils.createInitialValue(xPathMap, "level2");

      List expectedInitialValue = new LinkedList();

      Map level2 = new HashMap();
      Map level3 = new HashMap();
      level3.put("p4", new LinkedList());
      level2.put("level3", level3);
      expectedInitialValue.add(level2);

      assertEquals(expectedInitialValue, initialValue);

   }

   public void testCreateInitialValueWithPrimitives() throws Exception
   {
      Set /* <TypedXPath> */xPaths = new HashSet();
      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 1, "level2", null,  "", BigData.NULL, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 2, "level2/level3", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 3, "level2/level3/p3", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 4, "level2/level3/p4", null,  "", BigData.SHORT, true, XPathAnnotations.DEFAULT_ANNOTATIONS));

      TestXPathMap xPathMap = new TestXPathMap(xPaths);
      Object initialValue = StructuredDataXPathUtils.createInitialValue(xPathMap, "", true);

      Map expectedInitialValue = CollectionUtils.newMap();

      Map level2 = new HashMap();
      Map level3 = new HashMap();
      List p4List = CollectionUtils.newList();
      p4List.add(new Short((short)0));
      level3.put("p4", p4List);
      level3.put("p3", "");
      level2.put("level3", level3);

      List level2List = CollectionUtils.newList();
      level2List.add(level2);
      expectedInitialValue.put("level2", level2List);

      assertEquals(expectedInitialValue, initialValue);
   }

   public void testEmptyListConversion1() throws Exception
   {

      Set /* <TypedXPath> */xPaths = new HashSet();
      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 3, "p4", null,  "", BigData.SHORT, true, XPathAnnotations.DEFAULT_ANNOTATIONS));

      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Map level3 = new HashMap();
      // no entry for p4 although a list...

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(
            xPathMap);
      Node [] elements = structuredDataConverter.toDom(level3, "", true);
      Assert.condition(elements.length == 1);
      Document document = new Document((Element)elements[0]);

      assertEquals(level3, structuredDataConverter.toCollection(
            document.getRootElement(), "", true));

   }

   public void testEmptyListConversion2() throws Exception
   {

      Set /* <TypedXPath> */xPaths = new HashSet();
      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 1, "level2", null,  "", BigData.NULL, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 2, "level2/level3", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 3, "level2/level3/p3", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 4, "level2/level3/p4", null,  "", BigData.SHORT, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 5, "level2/level3_1", null,  "", BigData.NULL, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 6, "level2/level3_1/p1", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));

      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Map complexType = new HashMap();
      Map level3 = new HashMap();
      // no entry for p4 although a list...
      List level2list = new LinkedList();
      Map level2 = new HashMap();
      level2.put("level3", level3);
      level2list.add(level2);

      List level3_1list = new LinkedList();
      level3_1list.add(new HashMap());
      level2.put("level3_1", level3_1list);

      complexType.put("level2", level2list);

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(
            xPathMap);
      Node [] elements = structuredDataConverter.toDom(complexType, "", true);

      Document document = new Document((Element) elements[0]);

      assertEquals(complexType, structuredDataConverter.toCollection(
            document.getRootElement(), "", true));

   }

   public void testEmptyStructures()
   {
      Set /* <TypedXPath> */xPaths = new HashSet();
      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 1, "name", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 2, "id", null,  "", BigData.INTEGER, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 3, "address", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 4, "address/street",  "", null, BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 5, "address/city",  "", null, BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));

      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Map complexType = new HashMap();

      complexType.put("name", "Meier");
      complexType.put("id", new Integer(2342344));

      Map address = new HashMap();
//      address.put("street", null);
//      address.put("city", null);
      complexType.put("address", address);

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(
            xPathMap);
      Node [] elements = structuredDataConverter.toDom(complexType, "", true);

      Document document = new Document((Element) elements[0]);

      assertEquals(complexType, structuredDataConverter.toCollection(
            document.getRootElement(), "", true));
   }

   public void testConvertDocumentFragment () throws Exception
   {
      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/orderbook_elements.xsd");
      Set xPaths = XPathFinder.findAllXPaths(xsdSchema, "orderbook", false);
      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Map orderBook = createOrderBookCollection();

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(xPathMap);

      // depth=1
      Map expectedOrder = (Map) ((List)orderBook.get("order")).get(0);
      Node[] nodes = structuredDataConverter.toDom(expectedOrder, "order[1]", true);
      assertEquals(1, nodes.length);
      Map order = (Map) structuredDataConverter.toCollection((Element) nodes[0], "order[1]", true);
      assertEquals(expectedOrder, order);

      // depth=2
      Object expectedCustomer = expectedOrder.get("customer");
      nodes = structuredDataConverter.toDom(expectedCustomer, "order[1]/customer", true);
      assertEquals(1, nodes.length);
      Map customer = (Map) structuredDataConverter.toCollection((Element) nodes[0], "order[1]/customer", true);
      assertEquals(expectedCustomer, customer);

   }

   public void testFillStructuredData () throws Exception
   {
      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/orderbook_elements.xsd");
      Set xPaths = XPathFinder.findAllXPaths(xsdSchema, "orderbook", false);
      TestXPathMap xPathMap = new TestXPathMap(xPaths);

      Map values = new HashMap();
      values.put("@date", XSD_DATE_TIME_FORMAT.parse("1979-04-07T01:02:03"));
      values.put("@status", "PROCESSED");
      values.put("order[1]/@qty", new Integer(123));
      values.put("order[1]/customer/address/@street", "Solmsstrasse");

      org.eclipse.stardust.engine.core.struct.sxml.Document document = new org.eclipse.stardust.engine.core.struct.sxml.Document(new org.eclipse.stardust.engine.core.struct.sxml.Element(IStructuredDataValue.ROOT_ELEMENT_NAME));

      StructuredDataXPathUtils.putValues(document, xPathMap, values, true, false);

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(xPathMap);

      Map result = (Map)structuredDataConverter.toCollection(document.getRootElement(), "", true);

      assertEquals(result.get("@date"), XSD_DATE_TIME_FORMAT.parse("1979-04-07T01:02:03"));
      assertEquals(result.get("@status"), "PROCESSED");
      assertEquals(((Map)((List)result.get("order")).get(0)).get("@qty"), new Integer(123));
      assertEquals(((Map)((List)((Map)((Map)((List)result.get("order")).get(0)).get("customer")).get("address")).get(0)).get("@street"), "Solmsstrasse");

      System.out.println(result);
   }

}
