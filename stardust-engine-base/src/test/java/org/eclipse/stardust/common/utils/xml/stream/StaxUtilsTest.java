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
package org.eclipse.stardust.common.utils.xml.stream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.utils.xml.stream.StaxUtils;

import junit.framework.TestCase;


public class StaxUtilsTest extends TestCase
{
   private GlobalParameters globals;

   public void testAStreamingXmlInputFactoryShouldBeAvailable()
   {
      XMLInputFactory factory = StaxUtils.getXmlInputFactory();

      assertNotNull(factory);
   }

   public void testStreamingXmlInputFactoriesShouldOptionallyBeCached()
   {
      globals.set(StaxUtils.PRP_CACHE_XML_INPUT_FACTORY, true);

      XMLInputFactory factory1 = StaxUtils.getXmlInputFactory();
      XMLInputFactory factory2 = StaxUtils.getXmlInputFactory();

      assertSame(factory1, factory2);
   }

   public void testStreamingXmlInputFactoriesShouldOptionallyNotBeCached()
   {
      globals.set(StaxUtils.PRP_CACHE_XML_INPUT_FACTORY, false);

      XMLInputFactory factory1 = StaxUtils.getXmlInputFactory();
      XMLInputFactory factory2 = StaxUtils.getXmlInputFactory();

      assertNotSame(factory1, factory2);
   }

   public void testStreamingXmlInputFactoriesShouldBeCachedByDefault()
   {
      XMLInputFactory factory1 = StaxUtils.getXmlInputFactory();
      XMLInputFactory factory2 = StaxUtils.getXmlInputFactory();

      assertSame(factory1, factory2);
   }

   public void testAStreamingXmlOutputFactoryShouldBeAvailable()
   {
      XMLOutputFactory factory = StaxUtils.getXmlOutputFactory();

      assertNotNull(factory);
   }

   public void testStreamingXmlOutputFactoriesShouldOptionallyBeCached()
   {
      globals.set(StaxUtils.PRP_CACHE_XML_OUTPUT_FACTORY, true);

      XMLOutputFactory factory1 = StaxUtils.getXmlOutputFactory();
      XMLOutputFactory factory2 = StaxUtils.getXmlOutputFactory();

      assertSame(factory1, factory2);
   }

   public void testStreamingXmlOutputFactoriesShouldOptionallyNotBeCached()
   {
      globals.set(StaxUtils.PRP_CACHE_XML_OUTPUT_FACTORY, false);

      XMLOutputFactory factory1 = StaxUtils.getXmlOutputFactory();
      XMLOutputFactory factory2 = StaxUtils.getXmlOutputFactory();

      assertNotSame(factory1, factory2);
   }

   public void testStreamingXmlOutputFactoriesShouldBeCachedByDefault()
   {
      XMLOutputFactory factory1 = StaxUtils.getXmlOutputFactory();
      XMLOutputFactory factory2 = StaxUtils.getXmlOutputFactory();

      assertSame(factory1, factory2);
   }

   public void testAStreamingXmlEventFactoryShouldBeAvailable()
   {
      XMLEventFactory factory = StaxUtils.getXmlEventFactory();

      assertNotNull(factory);
   }

   public void testStreamingXmlEventFactoriesShouldOptionallyBeCached()
   {
      globals.set(StaxUtils.PRP_CACHE_XML_EVENT_FACTORY, true);

      XMLEventFactory factory1 = StaxUtils.getXmlEventFactory();
      XMLEventFactory factory2 = StaxUtils.getXmlEventFactory();

      assertSame(factory1, factory2);
   }

   public void testStreamingXmlEventFactoriesShouldOptionallyNotBeCached()
   {
      globals.set(StaxUtils.PRP_CACHE_XML_EVENT_FACTORY, false);

      XMLEventFactory factory1 = StaxUtils.getXmlEventFactory();
      XMLEventFactory factory2 = StaxUtils.getXmlEventFactory();

      assertNotSame(factory1, factory2);
   }

   public void testStreamingXmlEventFactoriesShouldBeCachedByDefault()
   {
      XMLEventFactory factory1 = StaxUtils.getXmlEventFactory();
      XMLEventFactory factory2 = StaxUtils.getXmlEventFactory();

      assertSame(factory1, factory2);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      this.globals = GlobalParameters.globals();
   }

   @Override
   protected void tearDown() throws Exception
   {
      Parameters.instance().flush();

      super.tearDown();
   }

}
