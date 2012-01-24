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
package org.eclipse.stardust.common;

import org.eclipse.stardust.common.Pair;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class PairTest extends TestCase
{
   public PairTest(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite(PairTest.class);
      return suite;
   }

   public void testValues()
   {
      Pair<String, Number> myPair = new Pair<String, Number>("pair1", 1);
      assertEquals(myPair.getFirst(), "pair1");
      assertEquals(1, myPair.getSecond());
   }
   
   public void testEquals()
   {
      Pair<String, Number> first = new Pair<String, Number>("pair1", 1);
      Pair<String, Integer> second = new Pair<String, Integer>("pair1", 1);
      assertEquals(first, second);
   }

}
