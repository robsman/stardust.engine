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
package org.eclipse.stardust.engine.core.model.convert.income;

import org.eclipse.stardust.engine.api.model.IProcessDefinition;

public abstract class IncomeElement
{
   public final static String _DEFAULT_DESCRIPTION = "N/A";
   
   private String id;
   private String name;
   private String description;
   
   public IncomeElement(String id, String name, String description)
   {
      this.id = id;
      this.name = name;
      this.description = description;
   }
   
   public String getName()
   {
      return this.name;
   }

   public String getId()
   {
      return this.id;
   }
   
   public String getDescription()
   {
      return this.description;
   }
   
   public abstract Object create(IProcessDefinition processDefinition);
   
}
