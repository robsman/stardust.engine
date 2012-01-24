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
package org.eclipse.stardust.engine.core.struct;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.xsd.XSDSchema;


public class XPathFinderTest extends TestCase
{

   public XPathFinderTest(String name)
   {
      super(name);
   }

   public void testReportXsd() throws Exception
   {
      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/report.xsd"); 
      Set allXPaths = XPathFinder.findAllXPaths(xsdSchema, "structureddataroot", false);

      assertEquals(9, allXPaths.size());
      assertTrue(allXPaths.contains(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 1, "@date", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 2, "order", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 3, "order/@qty", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 4, "order/@ordernr", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 5, "order/customer", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 6, "order/customer/@name", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 7, "order/customer/address", null, "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 8, "order/customer/address/@street", null, "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
   }

   public void testOrderbookElementsXsd() throws Exception
   {
      XSDSchema xsdSchema = StructuredTypeRtUtils.loadExternalSchema("org/eclipse/stardust/engine/core/struct/orderbook_elements.xsd"); 
      Set allXPaths = XPathFinder.findAllXPaths(xsdSchema, "orderbook", false);

      assertEquals(10, allXPaths.size());
      assertTrue(allXPaths.contains(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 1, "@date", null,  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 2, "@status", null, "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 3, "order", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 4, "order/@qty", null,  "", BigData.INTEGER, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 5, "order/@ordernr", null, "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 6, "order/customer", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 7, "order/customer/@name", null, "", 
            BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 8, "order/customer/address", null, "", 
            BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
      assertTrue(allXPaths.contains(new TypedXPath(null, 9, "order/customer/address/@street", null, "", 
            BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS)));
   }

   public void testParentXPath ()
   {
      assertEquals("a/b", StructuredDataXPathUtils.getParentXPath("a/b/c"));
      assertEquals("", StructuredDataXPathUtils.getParentXPath(""));
      assertEquals("", StructuredDataXPathUtils.getParentXPath("a"));
      
      assertEquals("a[0]/b[1]", StructuredDataXPathUtils.getParentXPath("a[0]/b[1]/c[2]"));
      assertEquals("", StructuredDataXPathUtils.getParentXPath("a[0]"));
   }
   
   public void testListTypes () throws Exception
   {
      final Set /* <TypedXPath> */allXPaths = new HashSet();
      
      allXPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      allXPaths.add(new TypedXPath(null, 1, "primitives", null, "",  BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      allXPaths.add(new TypedXPath(null, 2, "primitives/stringList", null, "",  BigData.STRING, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      allXPaths.add(new TypedXPath(null, 3, "primitivesList", null,  "", BigData.NULL, true, XPathAnnotations.DEFAULT_ANNOTATIONS));
      allXPaths.add(new TypedXPath(null, 4, "primitivesList/stringList", null, "",  BigData.STRING, true, XPathAnnotations.DEFAULT_ANNOTATIONS));

      IXPathMap xPathMap = new ClientXPathMap(allXPaths);
      
      assertTrue(StructuredDataXPathUtils.canReturnList("primitives/stringList", xPathMap));
      assertFalse(StructuredDataXPathUtils.canReturnList("primitives/stringList[1]", xPathMap));
      assertFalse(StructuredDataXPathUtils.canReturnList("primitives", xPathMap));
      assertFalse(StructuredDataXPathUtils.canReturnList("", xPathMap));
      
      assertTrue(StructuredDataXPathUtils.canReturnList("primitivesList", xPathMap));
      assertTrue(StructuredDataXPathUtils.canReturnList("primitivesList/stringList", xPathMap));
      assertTrue(StructuredDataXPathUtils.canReturnList("primitivesList[1]/stringList", xPathMap));
      assertTrue(StructuredDataXPathUtils.canReturnList("primitivesList/stringList[1]", xPathMap));
      
      assertFalse(StructuredDataXPathUtils.canReturnList("primitivesList[1]/stringList[1]", xPathMap));
      
      assertFalse(StructuredDataXPathUtils.returnsSinglePrimitive("primitives/stringList", xPathMap));
   }
   
   
}
