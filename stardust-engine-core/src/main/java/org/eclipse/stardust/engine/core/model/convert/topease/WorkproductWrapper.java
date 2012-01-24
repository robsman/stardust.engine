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
package org.eclipse.stardust.engine.core.model.convert.topease;

import org.eclipse.stardust.engine.api.model.IData;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class WorkproductWrapper
{
   ClassWrapper classWrapper;
   String identifier;
   String name;
   String description;
   Package parent;
   IData data;

   public WorkproductWrapper(ClassWrapper classWrapper, String identifier, String name,
                             String description, Package parent)
   {
      this.identifier = identifier;
      this.classWrapper = classWrapper;
      this.name = name;
      this.description = description;
      this.parent = parent;
   }

   public ClassWrapper getClassWrapper()
   {
      return classWrapper;
   }

   public String getName()
   {
      return name;
   }

   public IData getData()
   {
      return data;
   }

   public void setData(IData data)
   {
      this.data = data;
   }

   public String getFullName()
   {
      if (parent == null)
         return name;

      return parent.getFullName() + "." + name;
   }

   public String getIdentifier()
   {
      //@todo workaround!!! identifier with number cannot be used as data id because of
      //transition conditions
      //return identifier;
      return name;
   }

   public String getDescription()
   {
      //@todo workaround!!! identifier with number cannot be used as data id because of
      //transition conditions
      //return description;
      return identifier;
   }
}
