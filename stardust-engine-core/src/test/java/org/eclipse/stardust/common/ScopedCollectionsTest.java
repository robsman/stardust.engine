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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.ScopedList;
import org.eclipse.stardust.common.ScopedMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class ScopedCollectionsTest extends TestCase
{
   public ScopedCollectionsTest(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite(ScopedCollectionsTest.class);
      return suite;
   }

   public void testScopeList()
   {
      ScopedList<Number> myScopedList = new ScopedList<Number>(true);
      myScopedList.add("ints", Integer.valueOf(1));
      myScopedList.add("floats", Float.valueOf(1.0f));
      List<Integer> ints = Arrays.asList(1, 2, 3);
      myScopedList.set("list", ints);
      
      Number number = myScopedList.getIterator("ints").next();
      assertEquals(1, number);
      number = myScopedList.getIterator("floats").next();
      assertEquals(1.0f, number);
      number = myScopedList.getIterator("list").next();
      assertEquals(1, number);
   }
   
   public void testScopeMap()
   {
      ScopedMap<String, Number> myScopedMap = new ScopedMap<String, Number>(true);
      myScopedMap.set("ints", "1", 1);
      myScopedMap.set("ints", "2", 2);
      myScopedMap.set("floats", "1.0", Float.valueOf(1.0f));
      myScopedMap.set("floats", "2.0", Float.valueOf(2.0f));
      Map<String, Integer> ints = CollectionUtils.newHashMap();
      ints.put("3", 3);
      ints.put("4", 4);
      myScopedMap.set("map", ints);
      
      Map<String, Number> numbers = myScopedMap.getMap("ints");
      assertEquals(2, numbers.size());
      assertEquals(2, numbers.get("2"));
      assertEquals(1, myScopedMap.get("ints", "1"));
      assertEquals(ints, myScopedMap.getMap("map"));
   }

}
