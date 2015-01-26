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
import java.util.ArrayList;
import java.util.List;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AlterTableInfo extends AbstractTableInfo
{
   public static final FieldInfo[] NO_FIELDS = new FieldInfo[0];
   public static final IndexInfo[] NO_INDEXES = new IndexInfo[0];
   
   private List<FieldInfo> addedFields = new ArrayList<FieldInfo>();
   private List<FieldInfo> droppedFields = new ArrayList<FieldInfo>();
   private List<FieldInfo> modifiedFields = new ArrayList<FieldInfo>();
   
   private List<IndexInfo> addedIndexes = new ArrayList<IndexInfo>();
   private List<IndexInfo> droppedIndexes = new ArrayList<IndexInfo>();
   private List<IndexInfo> alteredIndexes = new ArrayList<IndexInfo>();

   public FieldInfo[] getAddedFields()
   {
      return addedFields.toArray(new FieldInfo[addedFields.size()]);
   }
   
   public FieldInfo[] getDroppedFields()
   {
      return droppedFields.toArray(new FieldInfo[droppedFields.size()]);
   }

   public FieldInfo[] getModifiedFields()
   {
      return modifiedFields.toArray(new FieldInfo[modifiedFields.size()]);
   }

   public IndexInfo[] getAlteredIndexes()
   {
      return alteredIndexes.toArray(new IndexInfo[alteredIndexes.size()]);
   }

   public IndexInfo[] getDroppedIndexes()
   {
      return droppedIndexes.toArray(new IndexInfo[droppedIndexes.size()]);
   }

   public IndexInfo[] getAddedIndexes()
   {
      return addedIndexes.toArray(new IndexInfo[addedIndexes.size()]);
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

   @Override
   public void addField(FieldInfo info)
   {
      super.addField(info);
      addedFields.add(info);
   }

   @Override
   public void removeField(FieldInfo info)
   {
      super.removeField(info);
      droppedFields.add(info);
   }

   @Override
   public void addIndex(IndexInfo info)
   {
      super.addIndex(info);
      addedIndexes.add(info);
   }

   @Override
   public void removeIndex(IndexInfo info)
   {
      super.removeIndex(info);
      droppedIndexes.add(info);
   }
}
