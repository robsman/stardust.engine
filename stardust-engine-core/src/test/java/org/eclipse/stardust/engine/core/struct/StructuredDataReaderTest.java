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

import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataReader;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;

import junit.framework.TestCase;

public class StructuredDataReaderTest extends TestCase
{

   public StructuredDataReaderTest(String name)
   {
      super(name);
   }

   public void testReadReport() throws Exception
   {
      IXPathMap xPathMap = this.buildXPathMap();

      Set entries = new HashSet();
      TestStructuredDataValueFactory testStructuredDataValueFactory = new TestStructuredDataValueFactory();
      IStructuredDataValue e = testStructuredDataValueFactory.createRootElementEntry(0,
            xPathMap.getRootXPathOID().longValue(), "0", null);
      entries.add(e);
      e = testStructuredDataValueFactory.createKeyedElementEntry(0, 0,
            xPathMap.getXPathOID("testChild1").longValue(), "0", "testValue", BigData.STRING);
      entries.add(e);

      StructuredDataReader reader = new StructuredDataReader(xPathMap);
      Document newDocument = reader.read(entries);

      Document originalDocument = DocumentBuilder.buildDocument((XPathFinderTest.class.getResource("simple.xml")
                        .openStream()));

      System.out.println("original document: "+originalDocument.toXML());
      System.out.println("  result document: "+newDocument.toXML());

      assertTrue("new DOM differs from original", originalDocument.toXML().equals(newDocument.toXML()));
   }

   private IXPathMap buildXPathMap()
   {
      Set xPaths = new HashSet();

      xPaths.add(new TypedXPath(null, 0, "", null,  "", BigData.NULL, false, XPathAnnotations.DEFAULT_ANNOTATIONS));
      xPaths.add(new TypedXPath(null, 1, "testChild1", "xsd:string",  "", BigData.STRING, false, XPathAnnotations.DEFAULT_ANNOTATIONS));

      return new TestXPathMap(xPaths);
   }

}
