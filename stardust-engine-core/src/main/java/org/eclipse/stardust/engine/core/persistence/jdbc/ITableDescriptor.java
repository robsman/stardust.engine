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

public interface ITableDescriptor
{

   public abstract String getTableName();
   
   public String getSchemaName();

   /**
    * Returns the table alias, if explicitly set. If no alias was set, <code>null</code>
    * will be returned.
    * 
    * @return the table alias, or <code>null</code>
    */
   public abstract String getTableAlias();

   public abstract FieldRef fieldRef(String fieldName);

   public abstract FieldRef fieldRef(String fieldName, boolean ignorePreparedStatements);
}