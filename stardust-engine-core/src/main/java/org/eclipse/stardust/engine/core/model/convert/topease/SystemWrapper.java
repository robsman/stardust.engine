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

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class SystemWrapper
{
   String identifier;
   String name;
   String description;
   Package parent;
   boolean gui;

   public SystemWrapper(String identifier, String name, String description, boolean gui, Package parent)
   {
      this.identifier = identifier;
      this.name = name;
      this.description = description;
      this.gui = gui;
      this.parent = parent;
   }

   public String getName()
   {
      return name;
   }

   public Package getParent()
   {
      return parent;
   }

   public boolean isGui()
   {
      return gui;
   }

   public String getFullName()
   {
      if (parent == null)
         return name;

      return parent.getFullName() + "." + name;
   }

   public String getIdentifier()
   {
      return identifier;
   }

   public String getDescription()
   {
      return description;
   }
}
