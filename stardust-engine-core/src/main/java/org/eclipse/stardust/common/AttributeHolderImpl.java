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

import static org.eclipse.stardust.common.CollectionUtils.copyMap;
import static org.eclipse.stardust.common.CollectionUtils.newTreeMap;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.engine.core.model.utils.RuntimeAttributeHolder;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class AttributeHolderImpl implements AttributeHolder, RuntimeAttributeHolder
{
   private Map<String, Object> attributes = null;

   private volatile Map<String, Object> roAttributesView = null;

   private volatile Map<String, Object> rtAttributes = null;
   
   public <T> T getRuntimeAttribute(String name)
   {
      if (rtAttributes == null)
      {
         return null;
      }
      final Map<String, Object> attribs = rtAttributes;
      return attribs == null ? null : (T) attribs.get(name);
   }

   public Object setRuntimeAttribute(String name, Object value)
   {
      final Map<String, Object> currentAttribs = rtAttributes;
      
      Object oldValue;

      final Map<String, Object> updatedAttribs;
      if (null == currentAttribs)
      {
         updatedAttribs = Collections.singletonMap(name, value);
         oldValue = null;
      }
      else
      {
         updatedAttribs = copyMap(currentAttribs);
         
         if (null != value)
         {
            oldValue = updatedAttribs.put(name, value);
         }
         else
         {
            oldValue = updatedAttribs.remove(name);
         }
      }
      rtAttributes = updatedAttribs;
      
      return oldValue;
   }

   public Object getAttribute(String name)
   {
      if (attributes == null)
      {
         return null;
      }
      return attributes.get(name);
   }

   public <V extends Object> void setAllAttributes(Map<String, V> newAttributes)
   {
      markModified();
      if (attributes == null)
      {
         attributes = newTreeMap();
      }
      else
      {
         attributes.clear();
      }
      if (newAttributes != null)
      {
         attributes.putAll(newAttributes);
         /*Iterator<Map.Entry<String, V>> i = newAttributes.entrySet().iterator();
         while (i.hasNext())
         {
            Map.Entry<String, V> entry = i.next();
            // @todo/hiob (ub) make deep copy
            attributes.put(entry.getKey(), entry.getValue());
         }*/
      }
   }

   public Map<String, Object> getAllAttributes()
   {
      if (roAttributesView == null)
      {
         if (attributes == null)
         {
            return Collections.emptyMap();
         }
         roAttributesView = Collections.unmodifiableMap(attributes);
      }
      return roAttributesView;
   }

   public void setAttribute(String name, Object value)
   {
      markModified();
      if (attributes == null)
      {
         attributes = newTreeMap();
      }
      attributes.put(name, value);
   }

   public boolean getBooleanAttribute(String name)
   {
      Boolean value = (Boolean) getAttribute(name);
      if (value == null)
      {
         return false;
      }
      return value.booleanValue();
   }

   public int getIntegerAttribute(String name)
   {
      Object attribute = getAttribute(name);
      return attribute == null ? 0: ((Integer) attribute).intValue();
   }

   public float getFloatAttribute(String name)
   {
      Object attribute = getAttribute(name);
      return attribute == null ? 0: ((Float) attribute).floatValue();
   }

   public long getLongAttribute(String name)
   {
      Object attribute = getAttribute(name);
      return attribute == null ? 0: ((Long) attribute).longValue();
   }

   public String getStringAttribute(String name)
   {
      return (String) getAttribute(name);
   }

   public void removeAllAttributes()
   {
      markModified();
      if (attributes != null)
      {
         attributes.clear();
      }
   }

   public void removeAttribute(String name)
   {
      markModified();
      if (attributes != null)
      {
         attributes.remove(name);
      }
   }
}
