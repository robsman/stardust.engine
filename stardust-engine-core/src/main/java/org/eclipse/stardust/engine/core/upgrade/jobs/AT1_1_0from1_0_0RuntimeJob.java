/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.SQLException;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.*;

public class AT1_1_0from1_0_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final String UPL_TABLE_NAME = "user_participant";
   private static final String UPL_FIELD_ON_BEHALF_OF = "onBehalfOf";
   private static final String AIH_TABLE_NAME = "act_inst_history";
   private static final String AIH_FIELD_ON_BEHALF_OF_USER = "onBehalfOfUser";

   private static final Logger trace = LogManager.getLogger(AT1_1_0from1_0_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(1, 1, 0);

   protected AT1_1_0from1_0_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE, DBMSKey.MSSQL8,
            DBMSKey.MYSQL_SEQ});
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.alterTable(item, new AlterTableInfo(UPL_TABLE_NAME)
      {
         private final FieldInfo ON_BEHALF_OF = new FieldInfo(UPL_FIELD_ON_BEHALF_OF,
               Long.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { ON_BEHALF_OF };
         }

      }, this);

      try
      {
         setColumnDefaultValue(item, UPL_TABLE_NAME,
               UPL_FIELD_ON_BEHALF_OF, 0);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + UPL_TABLE_NAME
               + "." + UPL_FIELD_ON_BEHALF_OF + " to 0.");
      }

      DatabaseHelper.alterTable(item, new AlterTableInfo(AIH_TABLE_NAME)
      {
         private final FieldInfo ON_BEHALF_OF = new FieldInfo(AIH_FIELD_ON_BEHALF_OF_USER,
               Long.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { ON_BEHALF_OF };
         }

      }, this);

      try
      {
         setColumnDefaultValue(item, AIH_TABLE_NAME,
               AIH_FIELD_ON_BEHALF_OF_USER, 0);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + AIH_TABLE_NAME
               + "." + AIH_FIELD_ON_BEHALF_OF_USER + " to 0.");
      }
   }

   private void setColumnDefaultValue(RuntimeItem item, String tableName,
         String columnName, Object defaultValue) throws SQLException
   {
      tableName = DatabaseHelper.getQualifiedName(tableName);

      StringBuffer buffer = new StringBuffer(500);
      buffer.append("UPDATE ").append(tableName);
      buffer.append(" SET ").append(columnName).append(" = ").append(defaultValue);

      item.executeDdlStatement(buffer.toString(), false);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   protected void printUpgradeSchemaInfo()
   {
   }

   protected void printMigrateDataInfo()
   {
   }

   protected void printFinalizeSchemaInfo()
   {
   }

   public Version getVersion()
   {
      return VERSION;
   }
}