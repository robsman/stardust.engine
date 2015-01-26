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
package org.eclipse.stardust.engine.core.persistence;

import java.util.List;

import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


public class InsertDescriptor implements ITableDescriptor
{
   private String schemaName;
   private Class type;
   private final TypeDescriptor tdType;
   
   private List values;
   private QueryDescriptor fullselect;
   
   public static InsertDescriptor into(Class type)
   {
      return into(null, type);
   }
   
   public static InsertDescriptor into(String schemaName, Class type)
   {
      InsertDescriptor result = new InsertDescriptor(type);

      result.schemaName = schemaName;

      return result;
   }
   
   private InsertDescriptor(Class type)
   {
      this.type = type;
      this.tdType = TypeDescriptor.get(type);
   }
   
   public InsertDescriptor fullselect(QueryDescriptor query)
   {
      setFullselect(query);
      
      return this;
   }
   
   public String getTableName()
   {
      return tdType.getTableName();
   }

   public String getTableAlias()
   {
      return null;
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
      return type;
   }

   public QueryDescriptor getFullselect()
   {
      return fullselect;
   }

   public void setFullselect(QueryDescriptor fullselect)
   {
      this.fullselect = fullselect;
   }

   public List getValues()
   {
      return values;
   }

   public String getSchemaName()
   {
      return schemaName;
   }
}
