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

public class SchemaDecorator implements ITableDescriptor
{
   private final String schema;

   private final ITableDescriptor delegate;

   public SchemaDecorator(String schema, ITableDescriptor delegate)
   {
      this.schema = schema;
      this.delegate = delegate;
   }

   public String getSchemaName()
   {
      return schema;
   }

   public String getTableName()
   {
      return delegate.getTableName();
   }

   public String getTableAlias()
   {
      return delegate.getTableAlias();
   }

   public FieldRef fieldRef(String fieldName)
   {
      return new FieldRef(this, fieldName);
   }
}
