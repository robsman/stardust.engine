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
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;


/**
 * @author sauer
 * @version $Revision$
 */
public class StructuredDataMapAccessor extends AbstractStructuredDataAccessPointAdapter
{

   private static final String SET_CONTENT_METHOD = "setContent";
   private static final String GET_CONTENT_METHOD = "getContent";
   private Map map;

   public StructuredDataMapAccessor(TypedXPath xPath, Map map, boolean constant)
   {
      super(xPath, constant);
     
      if (map == null)
      {
         this.map = CollectionUtils.newMap();
      }
      else
      {
         this.map = map;
      }
   }

   public Object getValue()
   {
      return map;
   }

   public void bindValue(Object value)
   {
      if (value == null)
      {
         this.map = CollectionUtils.newMap();
      }
      else
      {
         this.map = (Map) value;
      }
   }

   public Object unwrap()
   {
      return map;
   }

   public boolean has(String name, Scriptable start)
   {
      TypedXPath childXPath = getChildXPath(name);
      
      if ( !isConstant())
      {
         // only true for valid child nodes
         return (null != childXPath);
      }
      else
      {
         if (StructuredDataXPathUtils.canHaveContentAndAttributes(this.getXPath()))
         {
            if (GET_CONTENT_METHOD.equals(name) || StructuredDataConverter.NODE_VALUE_KEY.equals(name))
            {
               return true;
            }
         }
         if (childXPath == null)
         {
            return false;
         }
         else
         {
            return map.containsKey(StructuredDataXPathUtils.getLastXPathPart(childXPath.getXPath()));
         }
      }
   }
   
   public Object get(String name, Scriptable start)
   {
      if (StructuredDataXPathUtils.canHaveContentAndAttributes(this.getXPath()))
      {
         if (GET_CONTENT_METHOD.equals(name) || StructuredDataConverter.NODE_VALUE_KEY.equals(name))
         {
            return new GetContentCallable(this.map);
         }
         if (SET_CONTENT_METHOD.equals(name))
         {
            return new SetContentCallable(this.map);
         }
      }

      TypedXPath childXPath = getChildXPath(name);
      if (childXPath == null)
      {
         return null;
      }
      Object value = map.get(StructuredDataXPathUtils.getLastXPathPart(childXPath.getXPath()));
      
      if (null == value)
      {
         // if child is complex sub structure, build it on the fly while dereferentiation
         // is done
         
         if (null != childXPath) 
         {
            if (childXPath.isList())
            {
               value = CollectionUtils.newList();
            }
            else if (BigData.NULL == childXPath.getType() || childXPath.getChildXPaths().size() > 0)
            {
               value = CollectionUtils.newMap();
            }
            map.put(StructuredDataXPathUtils.getLastXPathPart(childXPath.getXPath()), value);
   
            value = wrapElement(childXPath, value);
         }
      }
      else if ((value instanceof Map) || (value instanceof List))
      {
         Object wrappedValue = null;
         if (null != childXPath)
         {
            wrappedValue = wrapElement(childXPath, value);
         }
         if (wrappedValue != null)
         {
            value = wrappedValue;
         }
      } 
      else if (value instanceof Float) {
         value = floatToDouble((Float)value);
      }
      return value;
   }

   public void put(String name, Scriptable start, Object value)
   {
      TypedXPath childXPath = getChildXPath(name);
      if (childXPath == null)
      {
         throw new RuntimeException("Property '"+name+"' can not be set");
      }

      if ( !isConstant())
      {
         if (value instanceof AbstractStructuredDataAccessPointAdapter)
         {
            value = ((AbstractStructuredDataAccessPointAdapter) value).getValue();
         }
         if (StructuredDataXPathUtils.canHaveContentAndAttributes(this.getXPath()) && 
               StructuredDataConverter.NODE_VALUE_KEY.equals(name))
         {
            map.put(StructuredDataConverter.NODE_VALUE_KEY, value);
         }
         else
         {
            map.put(StructuredDataXPathUtils.getLastXPathPart(childXPath.getXPath()), value);
         }
      }
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
               : (Object) CollectionUtils.newMap();
   }
   
   public String toString()
   {
      if (this.map == null)
      {
         return "{}";
      }
      else
      {
         return this.map.toString();
      }
   }
   
   private Double floatToDouble (Float floatValue) {
      String floatNumberInString = String.valueOf(floatValue.floatValue());
      double floatNumberInDouble = Double.parseDouble(floatNumberInString);
      return new Double(floatNumberInDouble);
   }
   
}
