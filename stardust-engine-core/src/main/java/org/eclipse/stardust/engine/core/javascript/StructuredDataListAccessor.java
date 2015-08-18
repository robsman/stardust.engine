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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;


/**
 * @author sauer
 * @version $Revision$
 */
public class StructuredDataListAccessor extends AbstractStructuredDataAccessPointAdapter
{

   public static final String NAME_LENGTH = "length";
   
   private List list;

   public StructuredDataListAccessor(TypedXPath xPath, List list, boolean constant)
   {
      super(xPath, constant);
      
      this.list = list;
   }

   public Object getValue()
   {
      return list;
   }

   public void bindValue(Object value)
   {
      this.list = (List) value;
   }

   public Object unwrap()
   {
      return list;
   }

   public boolean has(String name, Scriptable start)
   {
      if (NAME_LENGTH.equals(name))
      {
         return true;
      }
      else
      {
         return super.has(name, start);
      }
   }
   
   public Object get(String name, Scriptable start)
   {
      if (NAME_LENGTH.equals(name))
      {
         return new Integer(list.size());
      }
      else
      {
         return super.get(name, start);
      }
   }

   public void put(String name, Scriptable start, Object value)
   {
      if (NAME_LENGTH.equals(name))
      {
         int newLength = 0;
         if (value instanceof Number)
         {
            newLength = ((Number) value).intValue();
         }
         else if (value instanceof String)
         {
            newLength = Integer.parseInt((String) value);
         }
         
         while (list.size() < newLength)
         {
            list.add(list.size(), null);
         }
      }
      else
      {
         super.put(name, start, value);
      }
   }

   public boolean has(int index, Scriptable start)
   {
      if ( !isConstant())
      {
         // TODO only true for valid child nodes
         return true;
      }
      else
      {
         return isValidIndex(index);
      }
   }

   public Object get(int index, Scriptable start)
   {
      Object value = isValidIndex(index) ? list.get(index) : null;
      
      if ((null == value) && !isConstant())
      {
         // TODO must be based on XPath
         value = wrapElement(getChildXPath("[]"), CollectionUtils.newMap());
         put(index, start, value);
      }
      else if ((value instanceof Map) || (value instanceof List))
      {
         TypedXPath childXPath = getChildXPath("[]");
         if ((null != childXPath) && (BigData.NULL == childXPath.getType() || childXPath.getChildXPaths().size() > 0))
         {
            value = wrapElement(childXPath, value);
         }
      }
      
      return value;
   }

   public void put(int index, Scriptable start, Object value)
   {
      while (index >= list.size())
      {
         list.add(list.size(), null);
      }

      if (value instanceof AbstractStructuredDataAccessPointAdapter)
      {
         value = ((AbstractStructuredDataAccessPointAdapter) value).getValue();
      }
      list.set(index, value);
   }

   private boolean isValidIndex(int index)
   {
      return (0 <= index) && (index < list.size());
   }
   
   /* (non-Javadoc)
    * @see org.mozilla.javascript.Delegator#getDefaultValue(java.lang.Class)
    */
   public Object getDefaultValue(Class hint)
   {
      return (hint == null || 
            hint == ScriptRuntime.ScriptableClass || 
            hint == ScriptRuntime.FunctionClass) 
               ? (Object) this
               : (Object) CollectionUtils.newList();
   }
   
   public String toString()
   {
      if (this.list == null)
      {
         return "[]";
      }
      else
      {
         return this.list.toString();
      }
   }

}
