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
public abstract class AlterTableInfo extends AbstractTableInfo
{
   public static final FieldInfo[] NO_FIELDS = new FieldInfo[0];
   public static final IndexInfo[] NO_INDEXES = new IndexInfo[0];

   public FieldInfo[] getAddedFields()
   {
      return NO_FIELDS;
   }
   
   public FieldInfo[] getDroppedFields()
   {
      return NO_FIELDS;
   }

   public FieldInfo[] getModifiedFields()
   {
      return NO_FIELDS;
   }

   public IndexInfo[] getAlteredIndexes()
   {
      return NO_INDEXES;
   }

   public IndexInfo[] getDroppedIndexes()
   {
      return NO_INDEXES;
   }

   public IndexInfo[] getAddedIndexes()
   {
      return NO_INDEXES;
   }
   
   public AlterTableInfo(String tableName)
   {
      super(tableName);
      // TODO Auto-generated constructor stub
   }
   
   public AlterTableInfo(String tableName, boolean tryDrop)
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
   
   /**
    * Here DML statements should be executed which perform necessary changes on columns 
    * before indexes are created on them. Manipulating values which are indexed can take 
    * much longer then it would without any index.  
    *  
    * This will be executed for altered indexes after all indexes got dropped and before they are recreated,
    * and before added indexes get created. If this method has been executed once for 
    * altered indexes it will not be executed again before added indexes. 
    * 
    * @param item the runtime item
    * @throws SQLException
    */
   public void executeDmlBeforeIndexCreation(RuntimeItem item) throws SQLException
   {
   }
}
