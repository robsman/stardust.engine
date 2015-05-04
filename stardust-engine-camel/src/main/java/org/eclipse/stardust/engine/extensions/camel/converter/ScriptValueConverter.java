package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 * Converts Rhino's NativeObjects to Structured Data.
 * 
 */
public final class ScriptValueConverter
{
   private ScriptValueConverter()
   {}

   public static Object unwrapValue(Object value, TypedXPath xPaths)
   {
      if (value == null)
      {
         return null;
      }
      if (value instanceof Undefined)
      {
         return null;
      }
      else if (value instanceof Wrapper)
      {
         // unwrap a Java object from a JavaScript wrapper
         // recursively call this method to convert the unwrapped value
         value = unwrapValue(((Wrapper) value).unwrap(), xPaths);
      }
      else if (value instanceof IdScriptableObject)
      {
         // check for special case Native object wrappers
         String className = ((IdScriptableObject) value).getClassName();
         // check for special case of the String object
         if ("String".equals(className))
         {
            value = Context.jsToJava(value, String.class);
         }
         // check for special case of a Date object
         else if ("Date".equals(className))
         {
            value = Context.jsToJava(value, Date.class);
         }
         else
         {
            // a scriptable object will probably indicate a multi-value property set
            // set using a JavaScript associative Array object
            Scriptable values = (Scriptable) value;
            Object[] propIds = values.getIds();

            // is it a JavaScript associative Array object using Integer indexes?
            if (values instanceof NativeArray && isArray(propIds))
            {
               // convert JavaScript array of values to a List of Serializable objects
               List<Object> propValues = new ArrayList<Object>(propIds.length);
               for (int i = 0; i < propIds.length; i++)
               {
                  // work on each key in turn
                  Integer propId = (Integer) propIds[i];

                  // we are only interested in keys that indicate a list of values
                  if (propId instanceof Integer)
                  {
                     // get the value out for the specified key
                     Object val = values.get(propId, values);
                     // recursively call this method to convert the value
                     propValues.add(unwrapValue(val, xPaths));
                  }
               }

               value = propValues;
            }
            else
            {
               // any other JavaScript object that supports properties - convert to a Map
               // of objects
               Map<String, Object> propValues = new HashMap<String, Object>(
                     propIds.length);
               for (int i = 0; i < propIds.length; i++)
               {
                  // work on each key in turn
                  Object propId = propIds[i];

                  // we are only interested in keys that indicate a list of values
                  if (propId instanceof String)
                  {
                     // get the value out for the specified key
                     Object val = values.get((String) propId, values);
                     TypedXPath xpath = findChildXPathByName((String) propId, xPaths);
                     // recursively call this method to convert the value
                     propValues.put(getId((String) propId, xpath),
                           unwrapValue(val, xpath));

                  }
               }
               value = propValues;
            }
         }
      }
      else if (value instanceof Object[])
      {
         // convert back a list Object Java values
         Object[] array = (Object[]) value;
         ArrayList<Object> list = new ArrayList<Object>(array.length);
         for (int i = 0; i < array.length; i++)
         {
            list.add(unwrapValue(array[i], xPaths));
         }
         value = list;
      }
      else if (value instanceof Map)
      {
         // ensure each value in the Map is unwrapped (which may have been an unwrapped
         // NativeMap!)
         Map<Object, Object> map = (Map<Object, Object>) value;
         Map<Object, Object> copyMap = new HashMap<Object, Object>(map.size());
         for (Object key : map.keySet())
         {
            TypedXPath xpath = findChildXPathByName((String) key, xPaths);
            copyMap.put(getId((String) key, xpath), unwrapValue(map.get(key), xpath));
         }
         value = copyMap;
      }
      return value;
   }

   private static boolean isArray(final Object[] ids)
   {
      boolean result = true;
      for (int i = 0; i < ids.length; i++)
      {
         if (ids[i] instanceof Integer == false)
         {
            result = false;
            break;
         }
      }
      return result;
   }

   private static TypedXPath findChildXPathByName(String key, TypedXPath parentXPath)
   {
      TypedXPath childXPath = parentXPath.getChildXPath(key);
      if (childXPath == null)
      {
         childXPath = parentXPath.getChildXPath("@" + key);
      }
      return childXPath;
   }

   private static String getId(String propId, TypedXPath xpath)
   {
      if (xpath!=null && xpath.isAttribute())
      {
         return "@" + propId;
      }
      return propId;
   }

}