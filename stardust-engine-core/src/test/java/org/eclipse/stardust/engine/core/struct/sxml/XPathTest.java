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
package org.eclipse.stardust.engine.core.struct.sxml;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathEvaluator.compileXPath;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.eclipse.stardust.common.utils.io.CloseableUtil;
import org.eclipse.stardust.common.utils.xml.StaticNamespaceContext;
import org.eclipse.stardust.common.utils.xml.XPathUtils;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathEvaluator;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;


public class XPathTest
{

   private Document featuresDocument;

   private org.w3c.dom.Document testDoc;

   @Test
   public void mustEvaluateExpressionsYieldingNumericResults() throws Exception
   {
      assertThat((Double) XPathUtils.evaluateXPath(testDoc, "number(doc/pi)"), is(3.1415));
   }

   @Test
   public void mustEvaluateExpressionsYieldingStringResults() throws Exception
   {
      assertThat((String) XPathUtils.evaluateXPath(testDoc, "string(doc/text)"), is("bla"));
   }

   @Test
   public void mustEvaluateExpressionsYieldingBoolean() throws Exception
   {
      assertThat((Boolean) XPathUtils.evaluateXPath(testDoc, "boolean(doc/true)"), is(true));
   }

   @Test
   public void mustEvaluateExpressionsYieldingSingleNodes() throws Exception
   {
      List<Node> nodes = (List<Node>) XPathUtils.evaluateXPath(testDoc, "doc/pi");
      assertThat(nodes, is(not(nullValue())));
      assertThat(nodes.size(), is(1));
      assertThat(nodes.get(0), is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(nodes.get(0).getTextContent(), is("3.1415"));

      nodes = (List<Node>) XPathUtils.evaluateXPath(testDoc, "doc/true");
      assertThat(nodes, is(not(nullValue())));
      assertThat(nodes.size(), is(1));
      assertThat(nodes.get(0), is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(nodes.get(0).getTextContent(), is("true"));

      nodes = (List<Node>) XPathUtils.evaluateXPath(testDoc, "doc/text");
      assertThat(nodes, is(not(nullValue())));
      assertThat(nodes.size(), is(1));
      assertThat(nodes.get(0), is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(nodes.get(0).getTextContent(), is("bla"));

      nodes = (List<Node>) XPathUtils.evaluateXPath(testDoc, "doc/node");
      assertThat(nodes, is(not(nullValue())));
      assertThat(nodes.size(), is(1));
      assertThat(nodes.get(0), is(instanceOf(org.w3c.dom.Element.class)));
   }

   @Test
   public void mustEvaluateExpressionsYieldingANodeList() throws Exception
   {
      List<Node> nodes = (List<Node>) XPathUtils.evaluateXPath(testDoc, "doc/*");
      assertThat(nodes, is(not(nullValue())));
      assertThat(nodes.size(), is(4));
      for (org.w3c.dom.Node node : nodes)
      {
         assertThat(node, is(instanceOf(org.w3c.dom.Element.class)));
      }
   }

   @Test
   public void mustFindSxmlElementsViaAbsoluteJXPath() throws Exception
   {
      XPathEvaluator xPath = compileXPath("/m:main",
            singletonMap("m", "http://tempuri.org/main"));
      List nRoot = xPath.selectNodes(featuresDocument);
//      List nRoot = xPath.selectNodes(featuresDocument.getRootElement());
      assertThat(nRoot, is(not(nullValue())));
      assertThat(nRoot.size(), is(1));
      assertThat(((List<Element>) nRoot).get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));

      List nSub1as = compileXPath("/m:main/m:sub1a",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(featuresDocument.getRootElement());
      assertThat(nSub1as, is(not(nullValue())));
      assertThat(nSub1as.size(), is(1));
      assertThat(nSub1as.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));

      List nSub1bs = compileXPath("/m:main/m:sub1b",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(
            featuresDocument.getRootElement());
      assertThat(nSub1bs, is(not(nullValue())));
      assertThat(nSub1bs.size(), is(2));
      assertThat(nSub1bs.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));
      assertThat(nSub1bs.get(1), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));

      List nSub1b_1 = compileXPath("/m:main/m:sub1b[1]",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(
            featuresDocument.getRootElement());
      assertThat(nSub1b_1, is(not(nullValue())));
      assertThat(nSub1b_1.size(), is(1));
      assertThat(nSub1b_1.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));
      assertThat(((List<Element>) nSub1b_1).get(0).getValue(), is("sub1b1"));

      List nSub1b_2 = compileXPath("/m:main/m:sub1b[2]",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(
            featuresDocument.getRootElement());
      assertThat(nSub1b_2, is(not(nullValue())));
      assertThat(nSub1b_2.size(), is(1));
      assertThat(nSub1b_2.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));
      assertThat(((List<Element>) nSub1b_2).get(0).getValue(), is("sub1b2"));
   }

   @Test
   public void mustFindSxmlElementsViaRelativeJXPath() throws Exception
   {
      XPathEvaluator xPath = compileXPath("/m:main",
            singletonMap("m", "http://tempuri.org/main"));
      List nRoot = xPath.selectNodes(featuresDocument.getRootElement());
      assertThat(nRoot, is(not(nullValue())));
      assertThat(nRoot.size(), is(1));
      assertThat(((List<Element>) nRoot).get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));

      Element eMain = ((List<Element>) nRoot).get(0);

      List nSub1as = compileXPath("m:sub1a",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(eMain);
      assertThat(nSub1as, is(not(nullValue())));
      assertThat(nSub1as.size(), is(1));
      assertThat(nSub1as.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));

      List nSub1bs = compileXPath("m:sub1b",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(eMain);
      assertThat(nSub1bs, is(not(nullValue())));
      assertThat(nSub1bs.size(), is(2));
      assertThat(nSub1bs.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));
      assertThat(nSub1bs.get(1), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));

      List nSub1b_1 = compileXPath("m:sub1b[1]",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(eMain);
      assertThat(nSub1b_1, is(not(nullValue())));
      assertThat(nSub1b_1.size(), is(1));
      assertThat(nSub1b_1.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));
      assertThat(((List<Element>) nSub1b_1).get(0).getValue(), is("sub1b1"));

      List nSub1b_2 = compileXPath("m:sub1b[2]",//
            singletonMap("m", "http://tempuri.org/main")).selectNodes(eMain);
      assertThat(nSub1b_2, is(not(nullValue())));
      assertThat(nSub1b_2.size(), is(1));
      assertThat(nSub1b_2.get(0), is(instanceOf(org.eclipse.stardust.engine.core.struct.sxml.Element.class)));
      assertThat(((List<Element>) nSub1b_2).get(0).getValue(), is("sub1b2"));
   }

   @Test
   public void mustFindDomElementsViaAbsoluteJXPath() throws Exception
   {
      JXPathContext xPathContext = JXPathContext.newContext(DOMConverter.convert(featuresDocument));
      xPathContext.registerNamespace("m", "http://tempuri.org/main");
      List nRoot = xPathContext.selectNodes("/m:main");
      assertThat(nRoot, is(not(nullValue())));
      assertThat(nRoot.size(), is(not(0)));
      assertThat(((List<org.w3c.dom.Element>) nRoot).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));

      List nSub1as = xPathContext.selectNodes("/m:main/m:sub1a");
      assertThat(nSub1as, is(not(nullValue())));
      assertThat(nSub1as.size(), is(1));
      assertThat(((List<org.w3c.dom.Element>) nSub1as).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));


      List nSub1bs = xPathContext.selectNodes("/m:main/m:sub1b");
      assertThat(nSub1bs, is(not(nullValue())));
      assertThat(nSub1bs.size(), is(2));
      assertThat(((List<org.w3c.dom.Element>) nSub1bs).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<Element>) nSub1bs).get(1),
            is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<org.w3c.dom.Element>) nSub1bs).get(0).getTextContent(), is("sub1b1"));
      assertThat(((List<org.w3c.dom.Element>) nSub1bs).get(1).getTextContent(), is("sub1b2"));

      List nSub1b_1 = xPathContext.selectNodes("/m:main/m:sub1b[1]");
      assertThat(nSub1b_1, is(not(nullValue())));
      assertThat(nSub1b_1.size(), is(1));
      assertThat(((List<org.w3c.dom.Element>) nSub1b_1).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<org.w3c.dom.Element>) nSub1b_1).get(0).getTextContent(), is("sub1b1"));

      List nSub1b_2 = xPathContext.selectNodes("/m:main/m:sub1b[2]");
      assertThat(nSub1b_2, is(not(nullValue())));
      assertThat(nSub1b_2.size(), is(1));
      assertThat(((List<org.w3c.dom.Element>) nSub1b_2).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<org.w3c.dom.Element>) nSub1b_2).get(0).getTextContent(), is("sub1b2"));
   }

   @Test
   public void mustFindDomElementsViaRelativeJXPath() throws Exception
   {
      JXPathContext xPathContext = JXPathContext.newContext(DOMConverter.convert(featuresDocument));
      xPathContext.registerNamespace("m", "http://tempuri.org/main");
      List nRoot = xPathContext.selectNodes("/m:main");
      assertThat(nRoot, is(not(nullValue())));
      assertThat(nRoot.size(), is(not(0)));
      assertThat(((List<org.w3c.dom.Element>) nRoot).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));

      org.w3c.dom.Element eMain = ((List<org.w3c.dom.Element>) nRoot).get(0);

      JXPathContext eMainContext = JXPathContext.newContext(eMain);
      eMainContext.registerNamespace("m", "http://tempuri.org/main");

      List nSub1as = eMainContext.selectNodes("m:sub1a");
      assertThat(nSub1as, is(not(nullValue())));
      assertThat(nSub1as.size(), is(1));
      assertThat(((List<org.w3c.dom.Element>) nSub1as).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));


      List nSub1bs = eMainContext.selectNodes("m:sub1b");
      assertThat(nSub1bs, is(not(nullValue())));
      assertThat(nSub1bs.size(), is(2));
      assertThat(((List<org.w3c.dom.Element>) nSub1bs).get(0),
            is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(nSub1bs.get(1), is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<org.w3c.dom.Element>) nSub1bs).get(0).getTextContent(), is("sub1b1"));
      assertThat(((List<org.w3c.dom.Element>) nSub1bs).get(1).getTextContent(), is("sub1b2"));

      List nSub1b_1 = eMainContext.selectNodes("m:sub1b[1]");
      assertThat(nSub1b_1, is(not(nullValue())));
      assertThat(nSub1b_1.size(), is(1));
      assertThat(nSub1b_1.get(0), is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<org.w3c.dom.Element>) nSub1b_1).get(0).getTextContent(), is("sub1b1"));

      List nSub1b_2 = eMainContext.selectNodes("m:sub1b[2]");
      assertThat(nSub1b_2, is(not(nullValue())));
      assertThat(nSub1b_2.size(), is(1));
      assertThat(nSub1b_2.get(0), is(instanceOf(org.w3c.dom.Element.class)));
      assertThat(((List<org.w3c.dom.Element>) nSub1b_2).get(0).getTextContent(), is("sub1b2"));
   }

   @Test
   public void mustFindElementsViaLocationPath() throws Exception
   {
      XPath xPath = XPathFactory.newInstance().newXPath();
      xPath.setNamespaceContext(new StaticNamespaceContext(singletonMap("",
            "http://tempuri.org/main")));
      XPathExpression compiledXPath = xPath.compile("/:main");

      Object nRoot = compiledXPath.evaluate(DOMConverter.convert(featuresDocument),
            XPathConstants.NODE);
      assertThat(nRoot, is(not(nullValue())));
      assertThat(nRoot, is(instanceOf(org.w3c.dom.Element.class)));

      nRoot = compiledXPath.evaluate(DOMConverter.convert(featuresDocument).getDocumentElement(),
            XPathConstants.NODE);
      assertThat(nRoot, is(not(nullValue())));
      assertThat(nRoot, is(instanceOf(org.w3c.dom.Element.class)));
   }

   @Before
   public void init() throws Exception
   {
      final URL featuresDocUrl = getClass().getResource("converters/features.xml");

      InputStream isFeaturesDoc = featuresDocUrl.openStream();
      try
      {
         this.featuresDocument = DocumentBuilder.buildDocument(isFeaturesDoc);
      }
      finally
      {
         CloseableUtil.closeQuietly(isFeaturesDoc);
      }

      this.testDoc = XmlUtils.parseString("<doc><pi>3.1415</pi><true>true</true><text>bla</text><node><subNode /></node></doc>");
   }
}
