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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import org.eclipse.stardust.engine.core.persistence.FieldRef;

public abstract class TableDescriptor implements ITableDescriptor
{
   private String schemaName;
   
   protected TableDescriptor(String schemaName)
   {
      this.schemaName = schemaName;
   }
   
   public FieldRef fieldRef(String fieldName)
   {
      return new FieldRef(this, fieldName);
   }

   public String getSchemaName()
   {
      return schemaName;
   }
}