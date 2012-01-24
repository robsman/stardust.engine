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
package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.reflect.Reflect;


/**
 * Mainly handles conversion of DocumentAnnotations from/to Map representation.
 *
 * @author roland.stamm
 *
 */
public class AnnotationUtils
{
   private AnnotationUtils()
   {
      // Utility
   }

   public static Map<String, Serializable> toMap(DocumentAnnotations annotations)
   {
      return toMapInternal(annotations);
   }

   public static DocumentAnnotations fromMap(Map<String, Serializable> map)
   {
      try
      {
         if (map != null && !map.isEmpty())
         {
            return fromMapInternal(map, new PrintDocumentAnnotationsImpl());
         }
      }
      catch (IllegalArgumentException e)
      {
         System.out.println(e);
      }
      catch (IllegalAccessException e)
      {
         System.out.println(e);
      }
      return null;
   }

   /**
    * Conversion is limited to typed Set<?>, typed List<?>, Number, String, Date, Boolean.
    * Primitives of boolean, int, long etc. are stored as their boxed representations.
    *
    * @param object
    * @return
    */
   private static Map<String, Serializable> toMapInternal(Object object)
   {
      if (object == null)
      {
         return null;
      }
      Map<String, Serializable> map = null;
      Collection<Field> fields = Reflect.getFields(object.getClass());
      if ( !fields.isEmpty())
      {
         map = new HashMap<String, Serializable>();
      }
      for (Field field : fields)
      {
         if ( !Modifier.isTransient(field.getModifiers()))
         {
            Serializable value = null;
            try
            {
               value = (Serializable) field.get(object);
            }
            catch (IllegalArgumentException e)
            {
               // TODO trace.
            }
            catch (IllegalAccessException e)
            {
               // TODO trace.
            }
            String name = field.getName();

            if (value == null)
            {
               continue;
            }
            if (isPrimitive(value))
            {
               map.put(name, value);
            }
            else if (value instanceof Date)
            {
               map.put(name, ((Date) value).getTime());
            }
            else if (value instanceof Set)
            {
               List convertedValue = null;
               Set set = (Set) value;
               if (set != null && !set.isEmpty())
               {
                  convertedValue = new LinkedList();
                  for (Object setEntry : set)
                  {
                     if (isPrimitive(setEntry))
                     {
                        convertedValue.add(setEntry);
                     }
                     else if (setEntry instanceof Date)
                     {
                        convertedValue.add(((Date) setEntry).getTime());
                     }
                     else if (setEntry != null)
                     {
                        // complex objects saved as map
                        convertedValue.add(toMapInternal(setEntry));
                     }
                  }
               }
               if (convertedValue != null && !convertedValue.isEmpty())
               {
                  map.put(name, (Serializable) convertedValue);
               }
            }
            else if (value instanceof List)
            {
               List valueList = (List) value;
               if (valueList != null && !valueList.isEmpty())
               {
                  Object firstListEntry = valueList.get(0);
                  if (isPrimitive(firstListEntry))
                  {
                     map.put(name, (Serializable) valueList);
                  }
                  else if (firstListEntry instanceof Date)
                  {
                     List<Long> arrayList = new ArrayList<Long>();
                     for (Object listEntry : valueList)
                     {
                        Date entry = (Date) listEntry;
                        arrayList.add(entry.getTime());
                     }
                     map.put(name, (Serializable) arrayList);
                  }
                  else
                  {
                     List<Object> arrayList = new ArrayList<Object>();
                     for (Object listEntry : valueList)
                     {
                        arrayList.add(toMapInternal(listEntry));
                     }
                     map.put(name, (Serializable) arrayList);
                  }
               }
            }
            else if (value.getClass().isArray())
            {
               throw new UnsupportedOperationException("Arrays are not supported. "
                     + value.getClass());
               // map.put(name, asList);
            }
            else
            {
               // complex type
               map.put(name, (Serializable) toMapInternal(value));
            }
         }
      }

      return map;
   }

   private static boolean isPrimitive(Object value)
   {
      if (value instanceof Number || value instanceof String || value instanceof Boolean)
      {
         return true;
      }
      return false;
   }

   /**
    * Conversion is limited to typed Set<?>, typed List<?>, Number, String, Date, Boolean.
    * Primitives of boolean, int, long etc. are stored as their boxed representations.
    *
    * @param <T>
    * @param map
    * @param targetObject
    * @return
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   private static <T extends Object> T fromMapInternal(Map<String, Serializable> map,
         T targetObject) throws IllegalArgumentException, IllegalAccessException
   {
      Collection<Field> fields = Reflect.getFields(targetObject.getClass());
      for (Field field : fields)
      {
         if ( !Modifier.isTransient(field.getModifiers()))
         {
            String name = field.getName();
            Class fieldClass = field.getType();
            Object value = map.get(name);

            if (Number.class.isAssignableFrom(fieldClass))
            {
               // unwrap Integer fields
               if (value instanceof Long && fieldClass.equals(Integer.class))
               {
                  field.set(targetObject, ((Long) value).intValue());
               }
               else
               {
                  field.set(targetObject, value);
               }
            }
            else if (String.class.isAssignableFrom(fieldClass))
            {
               field.set(targetObject, value);
            }
            else if (Boolean.class.isAssignableFrom(fieldClass))
            {
               field.set(targetObject, value);
            }
            else if (Date.class.isAssignableFrom(fieldClass))
            {
               Long date = (Long) value;
               if (date != null)
               {
                  field.set(targetObject, new Date(date));
               }
            }
            else if (Set.class.isAssignableFrom(fieldClass))
            {
               Set set = new LinkedHashSet();
               List list = (List) value;
               if (list != null)
               {
                  for (Object listEntry : list)
                  {
                     if (listEntry != null)
                     {
                        Class genericTypeClazz = getGenericType(field);
                        if (genericTypeClazz != null)
                        {
                           if (isPrimitive(listEntry))
                           {
                              set.add(listEntry);
                           }
                           else if (listEntry instanceof Date)
                           {
                              set.add(new Date((Long) listEntry));
                           }
                           else
                           {
                              set.add(fromMapInternal((Map) listEntry,
                                    getImplInstance(genericTypeClazz)));
                           }
                        }
                        else
                        {
                           throw new UnsupportedOperationException(
                                 "Generic Type not specified.");
                        }
                     }
                  }
                  field.set(targetObject, set);
               }
            }
            else if (List.class.isAssignableFrom(fieldClass))
            {
               Class genericTypeClazz = getGenericType(field);
               if (genericTypeClazz != null)
               {
                  List translatedList = new ArrayList();
                  if (value != null)
                  {
                     List valueList = (List) value;
                     for (Object listValue : valueList)
                     {
                        // unwrap Integer fields
                        if (listValue instanceof Long
                              && genericTypeClazz.equals(Integer.class))
                        {
                           translatedList.add(Integer.valueOf(((Long) listValue).intValue()));
                        }
                        else
                        {
                           translatedList.add(listValue);
                        }
                     }
                  }
                  field.set(targetObject, translatedList);
               }
               else
               {
                  field.set(targetObject, value);
               }
            }
            else if (fieldClass.isArray())
            {
               List<Object> array = (List<Object>) value;

               if (array != null)
               {
                  field.set(targetObject, array.toArray());
               }
            }
            else
            {
               // java class or primitive

               if (value instanceof Map)
               {
                  Map map2 = (Map) value;
                  if (map2 != null && !map2.isEmpty())
                  {
                     Object complexObject = fromMapInternal(map2,
                           getImplInstance(fieldClass));
                     field.set(targetObject, complexObject);
                  }
               }
               else if (value != null)
               {
                  // unwrap Integer fields
                  if (value instanceof Long && fieldClass.equals(int.class))
                  {
                     field.set(targetObject, ((Long) value).intValue());
                  }
                  else
                  {
                     field.set(targetObject, value);
                  }
               }
               else
               {
                  // value is null, nothing to set.
               }
            }
         }
      }

      return targetObject;
   }

   private static Class getGenericType(Field field)
   {
      Type genericType = field.getGenericType();
      if (genericType instanceof ParameterizedType)
      {
         Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
         Type arg0 = actualTypeArguments[0];
         if (arg0 instanceof Class)
         {
            return (Class) arg0;
         }
      }
      return null;
   }

   private static Object getImplInstance(Class< ? extends Type> clazz)
   {
      if ( !clazz.isInterface())
      {
         try
         {
            return clazz.newInstance();
         }
         catch (InstantiationException e)
         {
            throw new UnsupportedOperationException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new UnsupportedOperationException(e);
         }
      }
      else
      {
         throw new UnsupportedOperationException(clazz.getName());
      }
   }

   public static boolean isPrintDocumentAnnotations(
         DocumentAnnotations documentAnnotations)
   {
      if (documentAnnotations instanceof PrintDocumentAnnotations)
      {
         return true;
      }
      return false;
   }
}
