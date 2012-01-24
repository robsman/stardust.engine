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
public class ParameterWrapper
{
   private String name;
   private ClassWrapper classWrapper;
   private String identifier;
   private String description;

   public ParameterWrapper(String identifier, String name, String description,
                           ClassWrapper classWrapper)
   {
      this.identifier = identifier;
      this.name = name;
      this.classWrapper = classWrapper;
      this.description = description;
   }

   public String getName()
   {
      return name;
   }

   public ClassWrapper getClassWrapper()
   {
      return classWrapper;
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
