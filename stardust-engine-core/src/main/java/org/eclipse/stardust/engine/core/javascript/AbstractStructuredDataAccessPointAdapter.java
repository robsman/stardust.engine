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

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.mozilla.javascript.Delegator;


/**
 * @author sauer
 * @version $Revision$
 */
public abstract class AbstractStructuredDataAccessPointAdapter extends Delegator
{
   
   private final TypedXPath xPath;
   
   private final boolean constant;
   
   private Map adapters;

   protected AbstractStructuredDataAccessPointAdapter(TypedXPath xPath, boolean constant)
   {
      this.xPath = xPath;

      this.constant = constant;
   }
   
   public TypedXPath getXPath()
   {
      return xPath;
   }

   abstract public Object getValue();
   
   abstract public void bindValue(Object value);
   
   abstract public Object unwrap();
   
   protected TypedXPath getChildXPath(String name)
   {
      TypedXPath result = null;
      
      if (null != xPath)
      {
         // if the "@" property is requested, assume it points to the 
         // content of the current element
         if (StructuredDataConverter.NODE_VALUE_KEY.equals(name))
         {
            return this.xPath;
         }
         
         // TODO evaluate type of attribute, if defined
         
         if (xPath.isList() && "[]".equals(name))
         {
            return new ListValuedXPathAdapter(xPath);
         }

         result = xPath.getChildXPath(name);
         
         if (result == null && !name.startsWith(StructuredDataConverter.NODE_VALUE_KEY))
         {
            // try to search for an attribute xpath if no element xpath was found
            // this allows to access attributes using element.attributeName 
            // as well as element["@attributeName"
            result = xPath.getChildXPath(StructuredDataConverter.NODE_VALUE_KEY+name);
         }
      }
      
      return result;
   }
   
   protected AbstractStructuredDataAccessPointAdapter wrapElement(TypedXPath xPath,
         Object value)
   {
      AbstractStructuredDataAccessPointAdapter adapter = null;

        
      if (null != adapters)
      {
          // was this value wrapped before?
          adapter = (AbstractStructuredDataAccessPointAdapter) adapters.get(xPath);
      }  
      if (null == adapter)
      {
        if (xPath.isList())
        {
           adapter = new StructuredDataListAccessor(xPath, null, isConstant());
        }
        else if (BigData.NULL == xPath.getType() || xPath.getChildXPaths().size() > 0)
        {
           adapter = new StructuredDataMapAccessor(xPath, null, isConstant());        		
        }
        if (null == adapters)
        {
          this.adapters = CollectionUtils.newMap();
        }
        adapters.put(xPath, adapter);      
      }
      
      if (null != adapter)
      {
         if (adapter.getValue() != value)
         {
            adapter.bindValue(value);
         }
      }

      return adapter;
   }

   public boolean isConstant()
   {
      return constant;
   }

}