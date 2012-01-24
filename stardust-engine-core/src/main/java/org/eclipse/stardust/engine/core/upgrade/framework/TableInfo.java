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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * This class is used in the upgrade package as holder for information about a
 * database table.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class TableInfo extends AbstractTableInfo
{
   private static final Logger trace = LogManager.getLogger(TableInfo.class);

   /**
    * The table definition string
    */
   private String tableDefinition = null;

   /**
    * If <code>true</code> a sequence will be managed with this table
    */
   private boolean hasSequence = false;

   /**
    * The table's sequence name.
    */
   private String sequenceName = null;

   /**
    *
    */
   public TableInfo(String tableName, String tableDefinition)
   {
      this(tableName, tableDefinition, true, false);
   } 

   /**
    *
    */
   public TableInfo(String tableName, String tableDefinition, boolean hasSequence,
         boolean tryDrop)
   {
      super(tableName, tryDrop);

      this.tableDefinition = tableDefinition;
      this.hasSequence = hasSequence;
   }

   /**
    *
    */
   public TableInfo(String tableName, String tableDefinition, String sequenceName,
         boolean tryDrop)
   {
      super(tableName, tryDrop);

      this.tableDefinition = tableDefinition;
      this.hasSequence = true;
      this.sequenceName = sequenceName;
   }

   /**
    *
    */
   public String getTableDefinition()
   {
      return tableDefinition;
   }

   /**
    *
    */
   public boolean hasSequence()
   {
      return hasSequence;
   }

   /**
    *
    */
   public String getSequenceName()
   {
      if (null != sequenceName)
      {
         return sequenceName;
      }
      else
      {
         return getTableName() + "_SEQ";
      }
   }

   /**
    *
    */
   public void doCreate(RuntimeItem item) throws SQLException
   {
      trace.info("Creating table '" + getTableName() + "'");
      DatabaseHelper.createTable(item, this);
      
      if (hasSequence())
      {
         trace.info("Creating sequence '" + getSequenceName() + "'");
         DatabaseHelper.createSequence(item, this);
      }
   }

   /**
    *
    */
   public void drop(RuntimeItem item) throws SQLException
   {
      trace.info("Dropping table '" + getTableName() + "'");
      DatabaseHelper.dropTable(item, this);

      if (hasSequence())
      {
         trace.info("Dropping sequence '" + getSequenceName() + "'");
         DatabaseHelper.dropSequence(item, this);
      }
   }
}
