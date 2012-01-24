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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.persistence.FieldRef;


public class LockTableDescriptor implements ITableDescriptor
{
   private final TypeDescriptor type;
   
   private final String tableAlias;
   
   public LockTableDescriptor(TypeDescriptor type)
   {
      Assert.condition(type.isDistinctLockTableName(),
            "Type must support lock table option.");
      
      this.type = type;

      Assert.condition( !StringUtils.isEmpty(type.getTableAlias()),
            "Type must have a default alias defined.");
      
      this.tableAlias = type.getTableAlias() + "_lck";
   }

   public String getTableName()
   {
      return type.getLockTableName();
   }

   public String getTableAlias()
   {
      return tableAlias;
   }

   public FieldRef fieldRef(String fieldName)
   {
      return new FieldRef(this, fieldName);
   }

   public String getSchemaName()
   {
      return type.getSchemaName();
   }
}
