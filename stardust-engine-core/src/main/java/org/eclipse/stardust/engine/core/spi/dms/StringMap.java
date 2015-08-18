/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.pojo.data.PrimitiveXmlUtils;

public class StringMap implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final List<StringEntry> entries = new ArrayList<StringEntry>();

   public StringMap()
   {
   }

   public StringMap(Map<String, Serializable> map)
   {
      for (Map.Entry<String, Serializable> entry : map.entrySet())
      {
         String key = entry.getKey();
         String type = null;

         Serializable sourceValue = entry.getValue();
         if (sourceValue != null)
         {
            if (sourceValue instanceof Map)
            {
               type = Map.class.getSimpleName();
               StringMap collection = new StringMap(
                     (Map<String, Serializable>) sourceValue);
               entries.add(new StringEntry(key, type, null, collection));
            }
            else if (sourceValue instanceof List)
            {
               type = List.class.getSimpleName();
               StringMap collection = new StringMap(
                     (Collection<Serializable>) sourceValue);
               entries.add(new StringEntry(key, type, null, collection));
            }
            else if (sourceValue instanceof Set)
            {
               type = Set.class.getSimpleName();
               StringMap collection = new StringMap(
                     (Collection<Serializable>) sourceValue);
               entries.add(new StringEntry(key, type, null, collection));
            }
            else
            {
               // type =
               // PrimitiveXmlUtils.marshalSimpleTypeXsdType(sourceValue.getClass())
               // .toString();
               type = Reflect.getAbbreviatedName(sourceValue.getClass());
               String value = PrimitiveXmlUtils.marshalSimpleTypeXsdValue(sourceValue);
               entries.add(new StringEntry(key, type, value, null));
            }
         }
      }
   }

   private StringMap(Collection<Serializable> sourceCollection)
   {
      for (Serializable sourceValue : sourceCollection)
      {
         String type = null;

         if (sourceValue != null)
         {
            if (sourceValue instanceof Map)
            {
               type = Map.class.getSimpleName();
               StringMap collection = new StringMap(
                     (Map<String, Serializable>) sourceValue);
               entries.add(new StringEntry(null, type, null, collection));
            }
            else if (sourceValue instanceof List)
            {
               type = List.class.getSimpleName();
               StringMap collection = new StringMap(
                     (Collection<Serializable>) sourceValue);
               entries.add(new StringEntry(null, type, null, collection));
            }
            else if (sourceValue instanceof Set)
            {
               type = Set.class.getSimpleName();
               StringMap collection = new StringMap(
                     (Collection<Serializable>) sourceValue);
               entries.add(new StringEntry(null, type, null, collection));
            }
            else
            {
               // type =
               // PrimitiveXmlUtils.marshalSimpleTypeXsdType(sourceValue.getClass())
               // .toString();
               type = Reflect.getAbbreviatedName(sourceValue.getClass());
               String value = PrimitiveXmlUtils.marshalSimpleTypeXsdValue(sourceValue);
               entries.add(new StringEntry(null, type, value, null));
            }
         }
      }
   }

   public Map toMap()
   {
      Map map = new HashMap<String, Serializable>();
      for (StringEntry entry : entries)
      {
         String key = entry.getKey();
         String type = entry.getType();

         if (Map.class.getSimpleName().equals(type))
         {
            map.put(key, entry.getCollection().toMap());
         }
         else if (List.class.getSimpleName().equals(type))
         {
            map.put(key, entry.getCollection().toList());
         }
         else if (Set.class.getSimpleName().equals(type))
         {
            map.put(key, entry.getCollection().toSet());
         }
         else
         {
            Class< ? > clazz = Reflect.getClassFromAbbreviatedName(type);
            QName xsdType = PrimitiveXmlUtils.marshalSimpleTypeXsdType(clazz);
            map.put(key,
                  PrimitiveXmlUtils.unmarshalPrimitiveValue(xsdType, entry.getValue()));
         }
      }

      return map;
   }

   private List toList()
   {
      List list = new ArrayList<Serializable>();
      for (StringEntry entry : entries)
      {
         String type = entry.getType();

         if (Map.class.getSimpleName().equals(type))
         {
            list.add(entry.getCollection().toMap());
         }
         else if (List.class.getSimpleName().equals(type))
         {
            list.add(entry.getCollection().toList());
         }
         else if (Set.class.getSimpleName().equals(type))
         {
            list.add(entry.getCollection().toSet());
         }
         else
         {
            Class< ? > clazz = Reflect.getClassFromAbbreviatedName(type);
            QName xsdType = PrimitiveXmlUtils.marshalSimpleTypeXsdType(clazz);
            list.add(PrimitiveXmlUtils.unmarshalPrimitiveValue(xsdType, entry.getValue()));
         }
      }

      return list;
   }

   private Set toSet()
   {
      Set set = new HashSet<Serializable>();
      for (StringEntry entry : entries)
      {
         String type = entry.getType();

         if (Map.class.getSimpleName().equals(type))
         {
            set.add(entry.getCollection().toMap());
         }
         else if (List.class.getSimpleName().equals(type))
         {
            set.add(entry.getCollection().toList());
         }
         else if (Set.class.getSimpleName().equals(type))
         {
            set.add(entry.getCollection().toSet());
         }
         else
         {
            Class< ? > clazz = Reflect.getClassFromAbbreviatedName(type);
            QName xsdType = PrimitiveXmlUtils.marshalSimpleTypeXsdType(clazz);
            set.add(PrimitiveXmlUtils.unmarshalPrimitiveValue(xsdType, entry.getValue()));
         }
      }

      return set;
   }

   @Override
   public String toString()
   {
      return entries.toString();
   }

}
