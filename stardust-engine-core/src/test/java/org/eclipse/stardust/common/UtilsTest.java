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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.MapUtils;
import org.eclipse.stardust.common.StringUtils;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilsTest extends TestCase
{
   public UtilsTest(String name)
   {
      super(name);
   }
   
   public static Test suite()
   {
      TestSuite suite = new TestSuite(UtilsTest.class);
      return suite;
   }
   
   public void testStringIteration()
   {
      Iterator<String> stringIter = StringUtils.split("token1,token2,token3", ',', false);
      int count = 0;
      while (stringIter.hasNext())
      {
         String token = (String) stringIter.next();
         if(token.equals("token" + (count+1)))
         {
            count++;
         }
      }
      assertEquals(3, count);
   }
   
   public void testMapMerge()
   {
      Map<Integer, Object> map1 = CollectionUtils.newHashMap();
      map1.put(Integer.valueOf(5), Boolean.TRUE);
      Map<Double, Object> map2 = CollectionUtils.newHashMap();
      map2.put(Double.valueOf(10), "test");
      Map<Object, Object> mergedMap = MapUtils.merge(map1, map2);
      mergedMap.put("test", "test");
      assertEquals(3, mergedMap.size());
   }
   
   public void testScopeDescopedMap()
   {
      final String SCOPE = "myScope.values.";
      Map<String, String> myMap = CollectionUtils.newHashMap();
      myMap.put("value1", "value1");
      Map<String, String> scopedMap = MapUtils.scope(myMap, SCOPE);
      assertTrue(scopedMap.containsKey("myScope.values.value1"));
      
      myMap = MapUtils.descope(scopedMap, SCOPE);
      assertTrue(myMap.containsKey("value1"));
   }
}
