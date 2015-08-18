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
package org.eclipse.stardust.engine.core.struct.sxml;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.utils.xml.stream.StaxUtils;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * TODO only works with JDK 6
 */
public class ConcurrentXmlReadersTest
{
   public static final int N_THREADS = 25;

   public static final int N_DOCUMENTS = 150;

   private GlobalParameters globals;

   private ExecutorService executor;

   @Ignore
   @Test
   public void testConcurrentXmlReadersShouldNotInterfereWithEachOther() throws Exception
   {
      globals.set(StaxUtils.PRP_XML_INPUT_FACTORY, "com.sun.xml.internal.stream.XMLInputFactoryImpl");

      doTestConcurrentXmlReadersShouldNotInterfereWithEachOther();
   }

   @Ignore
   @Test
   public void testConcurrentXmlReadersShouldNotInterfereWithEachOtherUsingSjsx() throws Exception
   {
      globals.set(StaxUtils.PRP_XML_INPUT_FACTORY, "com.sun.xml.internal.stream.XMLInputFactoryImpl");

      doTestConcurrentXmlReadersShouldNotInterfereWithEachOther();
   }

   @Ignore
   @Test
   public void testConcurrentXmlReadersShouldNotInterfereWithEachOtherUsingWoodstox() throws Exception
   {
      globals.set(StaxUtils.PRP_XML_INPUT_FACTORY, "com.ctc.wstx.stax.WstxInputFactory");

      doTestConcurrentXmlReadersShouldNotInterfereWithEachOther();
   }

   private void doTestConcurrentXmlReadersShouldNotInterfereWithEachOther() throws Exception
   {
      final URL testModelUrl = getClass().getResource("FlowModel.xml");

      List<Callable<Integer>> parsers = newArrayList();
      while (parsers.size() < N_DOCUMENTS)
      {
         parsers.add(new Callable<Integer>()
         {
            public Integer call() throws Exception
            {
               Document doc = DocumentBuilder.buildDocument(testModelUrl.openStream());

               return doc.getChildCount();
            }
         });
      }

      List<Future<Integer>> docs = executor.invokeAll(parsers);

      for (Future<Integer> doc : docs)
      {
         assertTrue("Document must be ready", doc.isDone());
         assertSame(1, doc.get());
      }
   }

   @Before
   public void setUp() throws Exception
   {
      this.globals = GlobalParameters.globals();

      this.executor = Executors.newFixedThreadPool(N_THREADS);
   }

   @After
   public void tearDown() throws Exception
   {
      executor.shutdownNow();

      Parameters.instance().flush();
   }

}
