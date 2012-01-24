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
public class ClassWrapper
{
   private String name;
   private Package pakage;
   private String identifier;
   private String description;

   public ClassWrapper(String identifier, String name, String description, Package pakage)
   {
      this.identifier = identifier;
      this.name = name;
      this.description = description;
      this.pakage = pakage;
   }

   public String getIdentifier()
   {
      return identifier;
   }

   public String getFullName()
   {
      if (pakage == null)
      {
         return name;
      }
      else
      {
         return pakage.getFullName()+"."+name;
      }
   }

   public String getDescription()
   {
      return description;
   }
}