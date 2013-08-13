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
package org.eclipse.stardust.engine.core.javascript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.eclipse.xsd.XSDEnumerationFacet;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

public class EnumAccessor extends ScriptableObject
{

   private static final long serialVersionUID = 2418078829761210158L;

   List<XSDEnumerationFacet> facets;

   Field[] fields;

   Method[] methods;

   String value;

   Object realEnum;

   public EnumAccessor(List<XSDEnumerationFacet> facets)
   {
      this.facets = facets;
   }

   public EnumAccessor(List<XSDEnumerationFacet> facets, Object value)
   {
      this.facets = facets;
      if (value != null)
      {
         this.value = value.toString();
      }
   }

   public EnumAccessor(Field[] fields, Method[] methods, Object aValue)
   {
      this.fields = fields;
      this.methods = methods;
      if (aValue != null)
      {
         this.realEnum = aValue;
         this.value = aValue.toString();
      }
   }

   public Object getDefaultValue(Class hint)
   {
      return value;
   }

   public boolean has(String name, Scriptable start)
   {
      return true;
   }

   public Object get(String name, Scriptable start)
   {
      if (name.equalsIgnoreCase("equals"))
      {
         return new EnumCallable(this);
      }
      if (fields != null)
      {
         if (value != null)
         {
            if (value.toString().equalsIgnoreCase(name))
            {
               return value.toString();
            }
            else
            {
               Method method = this.getMethodForName(name);
               if (method != null && method.getReturnType().equals(String.class))
               {
                  try
                  {
                     return new EnumCallable(this, method);
                  }
                  catch (Throwable t)
                  {
                     t.printStackTrace();
                  }
               }
            }
         }
         for (int i = 0; i < fields.length; i++)
         {
            Field field = fields[i];
            if (name.equalsIgnoreCase(field.getName()))
            {
               if (field.isEnumConstant())
               {
                  try
                  {
                     Object o = field.get(realEnum);                      
                     return new EnumAccessor(o.getClass().getFields(), o.getClass().getMethods(), o);
                  }
                  catch (IllegalArgumentException e)
                  {
                     e.printStackTrace();
                  }
                  catch (IllegalAccessException e)
                  {
                     e.printStackTrace();
                  }
               }
               return name;
            }
         }
      }
      else
      {
         for (Iterator<XSDEnumerationFacet> i = this.facets.iterator(); i.hasNext();)
         {
            XSDEnumerationFacet facet = i.next();
            if (facet.getLexicalValue().equalsIgnoreCase(name))
            {
               return name;
            }
         }
      }
      return null;
   }

   private Method getMethodForName(String name)
   {
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         if (method.getName().equalsIgnoreCase(name))
         {
            return method;
         }
      }
      return null;
   }

   @Override
   public boolean equals(Object arg0)
   {
      // TODO Auto-generated method stub
      return super.equals(arg0);
   }

   public void put(String name, Scriptable start, Object value)
   {
   // do nothing, EnumValues values can not (yet) be changed by Javascript
   }

   public String getClassName()
   {
      return "EnumAccessor.class";
   }

   protected Object equivalentValues(Object val)
   {
      if (val == null)
      {
         return (val == this.value);
      }
      if (val instanceof EnumAccessor && this.value != null)
      {
         if (((EnumAccessor) val).value == null) {
            return ((EnumAccessor) val).value.equals(this.value);
         }
         return ((EnumAccessor) val).value.equalsIgnoreCase(this.value);
      }
      if (val instanceof Wrapper)
      {
         Object unwrapped = ((Wrapper) val).unwrap();
         if (unwrapped instanceof Enum)
         {
            return (((Enum) unwrapped).name().equalsIgnoreCase(this.value));
         }
      }
      if (val instanceof String && this.value != null)
      {
         return ((String) val).equalsIgnoreCase(this.value);
      }
      return super.equivalentValues(val);
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
   {
      return null;
   }

}
