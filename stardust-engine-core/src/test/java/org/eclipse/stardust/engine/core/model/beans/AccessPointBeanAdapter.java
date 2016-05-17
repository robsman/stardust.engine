/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.beans;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

@SuppressWarnings("serial")
public class AccessPointBeanAdapter extends AccessPointBean
{
   private final AccessPoint delegate;
   
   public AccessPointBeanAdapter(AccessPoint delegate)
   {
      this.delegate = delegate;
      setId(delegate.getId());
   }
   
   public int hashCode()
   {
      return delegate.hashCode();
   }

   public String getId()
   {
      return delegate.getId();
   }

   public String getName()
   {
      return delegate.getName();
   }

   public Direction getDirection()
   {
      return delegate.getDirection();
   }

   public String toString()
   {
      return delegate.toString();
   }

   public PluggableType getType()
   {
      return delegate.getType();
   }

   public Object getAttribute(String name)
   {
      return delegate.getAttribute(name);
   }

   public <V> void setAllAttributes(Map<String, V> newAttributes)
   {
      delegate.setAllAttributes(newAttributes);
   }

   public Map<String, Object> getAllAttributes()
   {
      return delegate.getAllAttributes();
   }

   public void setAttribute(String name, Object value)
   {
      delegate.setAttribute(name, value);
   }

   public boolean getBooleanAttribute(String name)
   {
      return delegate.getBooleanAttribute(name);
   }

   public int getIntegerAttribute(String name)
   {
      return delegate.getIntegerAttribute(name);
   }

   public void markModified()
   {
      delegate.markModified();
   }

   public float getFloatAttribute(String name)
   {
      return delegate.getFloatAttribute(name);
   }

   public long getLongAttribute(String name)
   {
      return delegate.getLongAttribute(name);
   }

   public String getStringAttribute(String name)
   {
      return delegate.getStringAttribute(name);
   }

   public void removeAllAttributes()
   {
      delegate.removeAllAttributes();
   }

   public void removeAttribute(String name)
   {
      delegate.removeAttribute(name);
   }

   public boolean equals(Object obj)
   {
      return delegate.equals(obj);
   }

   public void checkId(List<Inconsistency> inconsistencies)
   {
      super.checkId(inconsistencies);
   }
}
