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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MapUtils
{
   public static <K, V> Map<String, V> descope(Map<K, V> map, String scope)
   {
      int scopeLength = scope.length();
      Map<String, V> result = CollectionUtils.newHashMap();
      for (Iterator<Map.Entry<K,V>> i = map.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry<K, V> entry = i.next();
         if (entry.getKey() instanceof String)
         {
            String key = (String) entry.getKey();
            if (key.startsWith(scope))
            {
               result.put(key.substring(scopeLength), entry.getValue());
            }
         }
      }
      return result;
   }

   public static <K, V> Map<String, V> scope(Map<K, V> map, String scope)
   {
      Map<String, V> result = CollectionUtils.newHashMap();
      for (Iterator<Map.Entry<K, V>> i = map.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry<K, V> entry = i.next();
         result.put(scope + entry.getKey(), entry.getValue());
      }
      return result;
   }
   
   /**
    * Merges content of two maps, while overriding entries existing in both maps with the
    * ones from <code>rhs</code>.
    *  
    * @param lhs The basic set of map entries, may be <code>null</code>.
    * @param rhs The merged set of entries, possibly overriding entries from
    *       <code>lhs</code>, may be <code>null</code>.
    * @return A modifiable map containing the merged content of both input maps.
    */
   public static Map<Object, Object> merge(Map<?,?> lhs, Map<?,?> rhs)
   {
      Map<Object, Object> result = (null != lhs) ? new HashMap<Object,Object>(lhs) : new HashMap<Object, Object>();
      if (null != rhs)
      {
         result.putAll(rhs);
      }
      return result;
   }

   private MapUtils()
   {
      // utility class
   }
}
