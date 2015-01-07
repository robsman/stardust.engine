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

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MultiAttribute implements Attribute, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private final String name;
   private final List<Attribute> attributes = newArrayList();
   
   public MultiAttribute(String name)
   {
      super();

      if (isEmpty(name))
      {
         throw new IllegalArgumentException("Argument name is not allowed to be null.");
      }

      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   /**
    * @return a list of {@link Attribute}s with the same name.
    */
   public Object getValue()
   {
      return attributes;
   }

   public void setValue(Object object)
   {
      throw new UnsupportedOperationException();
   }

   public String getStringifiedValue()
   {
      throw new UnsupportedOperationException();
   }

   public Date getLastModificationTime()
   {
      throw new UnsupportedOperationException();
   }
   
   public void add(Attribute attribute)
   {
      if (null == attribute)
      {
         throw new IllegalArgumentException(
               "Argument attribute is not allowerd to be null.");
      }

      if ( !attribute.getName().equals(name))
      {
         throw new IllegalArgumentException("Only attributes with name " + name
               + " are allowed.");
      }
      
      attributes.add(attribute);
   }
}
