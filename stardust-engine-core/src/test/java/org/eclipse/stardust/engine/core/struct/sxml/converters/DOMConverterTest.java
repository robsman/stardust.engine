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
package org.eclipse.stardust.engine.core.struct.sxml.converters;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import org.eclipse.stardust.common.utils.io.CloseableUtil;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class DOMConverterTest
{

   @Test
   public void aComplexDomMustBeConvertibleIntoSxml() throws IOException
   {
      final URL testModelUrl = getClass().getResource("features.xml");

      InputStream isTestModel = testModelUrl.openStream();
      try
      {
         Document parsedDom = XmlUtils.parseStream(isTestModel);
         assertDom(parsedDom);

         org.eclipse.stardust.engine.core.struct.sxml.Document convertedDoc = DOMConverter.convert(parsedDom);
         assertSxml(convertedDoc);

//         System.out.println("After conversion:\n" + convertedDoc.toXML());

         Document reparsedDom = XmlUtils.parseString(convertedDoc.toXML());
         assertDom(reparsedDom);
      }
      finally
      {
         CloseableUtil.closeQuietly(isTestModel);
      }
   }

   @Test
   public void aComplexSxmlMustBeConvertibleIntoDom() throws Exception
   {
      final URL testModelUrl = getClass().getResource("features.xml");

      InputStream isTestModel = testModelUrl.openStream();
      try
      {
         org.eclipse.stardust.engine.core.struct.sxml.Document parsedDoc = DocumentBuilder.buildDocument(isTestModel);
         assertSxml(parsedDoc);

         org.w3c.dom.Document convertedDom = DOMConverter.convert(parsedDoc, null);
         assertDom(convertedDom);

//         System.out.println("After conversion:\n" + XmlUtils.toString(convertedDom));

         org.eclipse.stardust.engine.core.struct.sxml.Document reparsedDoc = DocumentBuilder.buildDocument(new StringReader(
               XmlUtils.toString(convertedDom)));
         assertSxml(reparsedDoc);
      }
      finally
      {
         CloseableUtil.closeQuietly(isTestModel);
      }
   }

   private void assertDom(org.w3c.dom.Document doc)
   {
      assertThat(doc, is(not(nullValue())));

      Element eMain = doc.getDocumentElement();
      assertThat(eMain, is(not(nullValue())));
      assertThat(eMain.getLocalName(), is("main"));
      assertThat(eMain.getNamespaceURI(), is("http://tempuri.org/main"));

      assertThat(eMain.getAttribute("a"), is("a-val"));
      assertThat(eMain.getAttribute("b"), is("b-val"));

      assertThat(eMain.getAttributeNS("http://tempuri.org/bla", "a"), is("bla-a-val"));
      assertThat(eMain.getAttributeNS("http://tempuri.org/bla", "b"), is("bla-b-val"));

      assertThat(eMain.getElementsByTagNameNS("http://tempuri.org/bla", "e1").getLength(), is(1));
      assertThat(((org.w3c.dom.Element) eMain.getElementsByTagNameNS("http://tempuri.org/bla", "e1").item(0)).getAttribute("a"), is("a-val"));

      assertThat(eMain.getElementsByTagNameNS("http://tempuri.org/main", "sub1a").getLength(), is(1));
      Element eSub1a = (org.w3c.dom.Element) eMain.getElementsByTagNameNS("http://tempuri.org/main", "sub1a").item(0);
      assertThat(eSub1a.getAttributes().getLength(), is(0));

      assertThat(eSub1a.getElementsByTagNameNS("http://tempuri.org/main", "sub2").getLength(), is(1));
      Element eSub2 = (org.w3c.dom.Element) eSub1a.getElementsByTagNameNS("http://tempuri.org/main", "sub2").item(0);

      assertThat(eSub2.getElementsByTagNameNS("http://tempuri.org/bla", "sub").getLength(), is(1));
      Element eBla = (org.w3c.dom.Element) eSub2.getElementsByTagNameNS("http://tempuri.org/bla", "sub").item(0);
      assertThat(eBla.getTextContent(), is("bla-sub-val"));

      NodeList esSub1b = eMain.getElementsByTagNameNS("http://tempuri.org/main", "sub1b");
      assertThat(esSub1b.getLength(), is(2));
      assertThat(((org.w3c.dom.Element) esSub1b.item(0)).getAttributes().getLength(), is(0));
      assertThat(((org.w3c.dom.Element) esSub1b.item(0)).getTextContent(), is("sub1b1"));
      assertThat(((org.w3c.dom.Element) esSub1b.item(1)).getAttributes().getLength(), is(0));
      assertThat(((org.w3c.dom.Element) esSub1b.item(1)).getTextContent(), is("sub1b2"));

      assertThat(eMain.getTextContent(), containsString("sub1b1"));
      assertThat(eMain.getTextContent(), containsString("sub1b2"));
   }

   private void assertSxml(org.eclipse.stardust.engine.core.struct.sxml.Document doc)
   {
      assertThat(doc, is(not(nullValue())));

      org.eclipse.stardust.engine.core.struct.sxml.Element eMain = doc.getRootElement();
      assertThat(eMain, is(not(nullValue())));

      assertThat(eMain.getLocalName(), is("main"));
      assertThat(eMain.getNamespaceURI(), is("http://tempuri.org/main"));
      assertThat(eMain.getAttribute("a").getValue(), is("a-val"));
      assertThat(eMain.getAttribute("b").getValue(), is("b-val"));

      assertThat(eMain.getAttribute("a", "http://tempuri.org/bla").getValue(), is("bla-a-val"));
      assertThat(eMain.getAttribute("b", "http://tempuri.org/bla").getValue(), is("bla-b-val"));

      assertThat(eMain.getChildElements("e1", "http://tempuri.org/bla").size(), is(1));
      assertThat(eMain.getChildElements("e1", "http://tempuri.org/bla").get(0).getAttribute("a").getValue(), is("a-val"));

      assertThat(eMain.getChildElements("sub1a", "http://tempuri.org/main").size(), is(1));
      org.eclipse.stardust.engine.core.struct.sxml.Element eSub1a = eMain.getChildElements("sub1a", "http://tempuri.org/main").get(0);
      assertThat(eSub1a.getAttributeCount(), is(0));

      assertThat(eSub1a.getChildElements("sub2", "http://tempuri.org/main").size(), is(1));
      org.eclipse.stardust.engine.core.struct.sxml.Element eSub2 = eSub1a.getChildElements("sub2", "http://tempuri.org/main").get(0);

      assertThat(eSub2.getChildElements("sub", "http://tempuri.org/bla").size(), is(1));
      org.eclipse.stardust.engine.core.struct.sxml.Element eBla = eSub2.getChildElements("sub", "http://tempuri.org/bla").get(0);
      assertThat(eBla.getValue(), is("bla-sub-val"));

      List<org.eclipse.stardust.engine.core.struct.sxml.Element> esSub1b = eMain.getChildElements("sub1b", "http://tempuri.org/main");
      assertThat(esSub1b.size(), is(2));
      assertThat(esSub1b.get(0).getAttributeCount(), is(0));
      assertThat(esSub1b.get(0).getValue(), is("sub1b1"));
      assertThat(esSub1b.get(1).getAttributeCount(), is(0));
      assertThat(esSub1b.get(1).getValue(), is("sub1b2"));

      assertThat(eMain.getValue(), containsString("sub1b1"));
      assertThat(eMain.getValue(), containsString("sub1b2"));
   }
}
