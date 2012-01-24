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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.SQLException;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class CreateTableInfo extends AbstractTableInfo
{
   public abstract FieldInfo[] getFields();
   
   public abstract IndexInfo[] getIndexes();
   
   public abstract String getSequenceName();
   
   public CreateTableInfo(String tableName)
   {
      super(tableName);
      // TODO Auto-generated constructor stub
   }
   
   public CreateTableInfo(String tableName, boolean tryDrop)
   {
      super(tableName, tryDrop);
      // TODO Auto-generated constructor stub
   }

   public void doCreate(RuntimeItem item) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }

   public void drop(RuntimeItem item) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }
}
