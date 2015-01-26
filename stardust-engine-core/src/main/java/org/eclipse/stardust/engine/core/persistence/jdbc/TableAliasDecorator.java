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

import java.util.List;

import org.eclipse.stardust.engine.core.persistence.FieldRef;


public class TableAliasDecorator implements ITypeDescriptor
{
   private final String alias;

   private final ITypeDescriptor delegate;

   public TableAliasDecorator(String alias, ITypeDescriptor delegate)
   {
      this.alias = alias;
      this.delegate = delegate;
   }

   public String getTableAlias()
   {
      return alias;
   }

   public String getTableName()
   {
      return delegate.getTableName();
   }

   public FieldRef fieldRef(String fieldName)
   {
      return new FieldRef(this, fieldName);
   }
   
   public FieldRef fieldRef(String fieldName, boolean ignorePreparedStatements)
   {
      return new FieldRef(this, fieldName, ignorePreparedStatements);
   }

   public Class getType()
   {
      return delegate.getType();
   }

   public List getPersistentFields()
   {
      return delegate.getPersistentFields();
   }

   public List getLinks()
   {
      return delegate.getLinks();
   }

   public List getPersistentVectors()
   {
      return delegate.getPersistentVectors();
   }

   public String getEncryptKey()
   {
      return delegate.getEncryptKey();
   }

   public String getDecryptKey()
   {
      return delegate.getDecryptKey();
   }

   public String getSchemaName()
   {
      return delegate.getSchemaName();
   }
}
