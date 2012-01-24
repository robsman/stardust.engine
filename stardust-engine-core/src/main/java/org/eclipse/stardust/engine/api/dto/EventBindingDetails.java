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
package org.eclipse.stardust.engine.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

import org.eclipse.stardust.common.MapUtils;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventBindingDetails implements Serializable
{
   private static final long serialVersionUID = -6772606868370576319L;
   private final int modelOID;
   private final int elementOID;
   private final String id;
   private final String name;
   private final Map staticAttributes = new HashMap();
   private final Map dynamicAttributes = new HashMap();

   public EventBindingDetails(int modelOID, int elementOID, String id, String name)
   {
      this.modelOID = modelOID;
      this.elementOID = elementOID;
      this.id = id;
      this.name = name;
   }

   public EventBindingDetails(ModelElement element, String id, String name)
   {
      this.modelOID = ModelUtils.nullSafeGetModelOID(element);
      this.elementOID = element.getElementOID();
      this.id = id;
      this.name = name;
      // @todo (france, ub): deepcopy
      staticAttributes.putAll(element.getAllAttributes());
   }

   public EventBindingDetails(IdentifiableElement element)
   {
      this(element, element.getId(), element.getName());
   }

   public int getModelOID()
   {
      return modelOID;
   }

   public int getElementOID()
   {
      return elementOID;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public Map getAllAttributes()
   {
      return MapUtils.merge(staticAttributes, dynamicAttributes);
   }

   public Object getAttribute(String name)
   {
      return dynamicAttributes.containsKey(name)
            ? dynamicAttributes.get(name)
            : staticAttributes.get(name);
   }
   
   public Map getAllDynamicAttributes()
   {
      return Collections.unmodifiableMap(dynamicAttributes);
   }

   protected void injectAttributes(Map attributes)
   {
      this.dynamicAttributes.putAll(attributes);
   }

   public Object setAttribute(String name, Object value)
   {
      final Object oldValue = getAttribute(name);
      dynamicAttributes.put(name, value);
      return oldValue;
   }

   public Object removeAttribute(String name)
   {
      final Object oldValue = getAttribute(name);
      if (staticAttributes.containsKey(name))
      {
         dynamicAttributes.put(name, null);
      }
      else
      {
         dynamicAttributes.remove(name);
      }
      return oldValue;
   }

   public boolean equals(Object other)
   {
      if (other == null)
      {
         return false;
      }
      if (!(other instanceof EventBindingDetails))
      {
         return false;
      }
      ;
      EventBindingDetails otherElement = (EventBindingDetails) other;
      if (otherElement.getElementOID() == getElementOID()
            && otherElement.getModelOID() == getModelOID())
      {
         return true;
      }
      return false;
   }
}
