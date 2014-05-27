package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

public class NativeMap implements Scriptable, Wrapper
{
   private static final long serialVersionUID = 3664761893203964569L;

   private Map<Object, Object> map;

   private Scriptable parentScope;

   private Scriptable prototype;

   /**
    * Construct
    * 
    * @param scope
    * @param map
    * @return native map
    */
   public static NativeMap wrap(Scriptable scope, Map<Object, Object> map)
   {
      return new NativeMap(scope, map);
   }

   /**
    * Construct
    * 
    * @param scope
    * @param map
    */
   public NativeMap(Scriptable scope, Map<Object, Object> map)
   {
      this.parentScope = scope;
      this.map = map;
   }

   public Object unwrap()
   {
      return map;
   }

   public String getClassName()
   {
      return "NativeMap";
   }

   public Object get(String name, Scriptable start)
   {
      // get the property from the underlying QName map
      if ("length".equals(name))
      {
         return map.size();
      }
      else
      {
         return map.get(name);
      }
   }

   public Object get(int index, Scriptable start)
   {
      Object value = null;
      int i = 0;
      Iterator itrValues = map.values().iterator();
      while (i++ <= index && itrValues.hasNext())
      {
         value = itrValues.next();
      }
      return value;
   }

   public boolean has(String name, Scriptable start)
   {
      // locate the property in the underlying map
      return map.containsKey(name);
   }

   public boolean has(int index, Scriptable start)
   {
      return (index >= 0 && map.values().size() > index);
   }

   public void put(String name, Scriptable start, Object value)
   {
      map.put(name, value);
   }

   public void put(int index, Scriptable start, Object value)
   {}

   public void delete(String name)
   {
      map.remove(name);
   }

   public void delete(int index)
   {
      int i = 0;
      Iterator itrKeys = map.keySet().iterator();
      while (i <= index && itrKeys.hasNext())
      {
         Object key = itrKeys.next();
         if (i == index)
         {
            map.remove(key);
            break;
         }
      }
   }

   public Scriptable getPrototype()
   {
      return this.prototype;
   }

   public void setPrototype(Scriptable prototype)
   {
      this.prototype = prototype;
   }

   public Scriptable getParentScope()
   {
      return this.parentScope;
   }

   public void setParentScope(Scriptable parent)
   {
      this.parentScope = parent;
   }

   public Object[] getIds()
   {
      return map.keySet().toArray();
   }

   public Object getDefaultValue(Class hint)
   {
      return null;
   }

   public boolean hasInstance(Scriptable value)
   {
      if (!(value instanceof Wrapper))
         return false;
      Object instance = ((Wrapper) value).unwrap();
      return Map.class.isInstance(instance);
   }
}