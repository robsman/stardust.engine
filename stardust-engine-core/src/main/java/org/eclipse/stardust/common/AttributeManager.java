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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AttributeManager implements IAttributeManager, Serializable
{

   private static final long serialVersionUID = 1L;

   private Map<String, Serializable> attributes;

   private Map<String, Serializable> readOnlyView;

   public Serializable getAttribute(String name)
   {
      return (null != attributes) ? (Serializable) attributes.get(name) : null;
   }

   public Serializable setAttribute(String name, Serializable value)
   {
      if (null != value)
      {
         ensureAttributesMapIsNotNull();
         
         return attributes.put(name, value);
      }
      else
      {
         return removeAttribute(name);
      }
   }
   
   public Map<String, Serializable> getAllAttributes()
   {
      if (null != attributes)
      {
         if (null == readOnlyView)
         {
            this.readOnlyView = Collections.unmodifiableMap(attributes);
         }
         
         return readOnlyView;
      }
      else
      {
         return Collections.emptyMap();
      }
   }
   
   public <V extends Serializable> void setAllAttributes(Map<String, V> attributes)
   {
      for (Map.Entry<String, V> attribute : attributes.entrySet())
      {
         setAttribute(attribute.getKey(), attribute.getValue());
      }
   }

   public Serializable removeAttribute(String name)
   {
      if (null != attributes)
      {
         return attributes.remove(name);
      }
      else
      {
         return null;
      }
   }

   public void removeAllAttributes()
   {
      if (null != attributes)
      {
         this.readOnlyView = null;
         this.attributes = null;
      }
   }
   
   private void ensureAttributesMapIsNotNull()
   {
      if (null == attributes)
      {
         this.attributes = CollectionUtils.newMap();
      }
   }

}
