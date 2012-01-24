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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModelElement
{
   private int elementOID;
   private TreeMap attributes = new TreeMap();
   protected boolean predefined;
   private String description;

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public void setAttribute(Attribute attribute)
   {
      if (attribute == null)
      {
         return;
      }
      attributes.put(attribute.getName(), attribute);
   }

   public void setAttribute(String key, String val)
   {
      attributes.put(key, new Attribute(key, val));
   }

   public void removeAttribute(String key)
   {
      attributes.remove(key);
   }

   public void setPredefined(boolean b)
   {
      predefined = b;
   }

   public int getElementOID()
   {
      return elementOID;
   }

   public boolean isPredefined()
   {
      return predefined;
   }

   public Iterator getAllAttributes()
   {
      return attributes.values().iterator();
   }

   public Object getAttribute(String key)
   {
      Attribute att = (Attribute) attributes.get(key);
      if (att != null)
      {
         return att.getValue();
      }
      return null;
   }

   public void setElementOID(int elementOID)
   {
    this.elementOID = elementOID;
   }
}
