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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.SQLException;
import java.util.Date;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.TableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class R2_6from2_5_1RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   public static final Logger trace =
         LogManager.getLogger(R2_6from2_5_1RuntimeJob.class);

   private static final TableInfo[] NEW_TABLE_LIST =
         new TableInfo[]
         {
            new TableInfo("TRANS_TOKEN", "oid number, transition number, "
         + "processInstance number, source number, target number, "
         + "isConsumed number"),
            new TableInfo("TRANS_INST", "oid number, processInstance number, "
         + "transition number, source number, target number")
         };

   private static final TableInfo[] DROP_TABLE_LIST =
         new TableInfo[]
         {
            new TableInfo("RECOVERY_LOG", null),
            new TableInfo("PROC_RECOVERY_LOG", null, "PROCESS_RECOVERY_LOG_SEQ", false)
         };

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      System.out.println("Creating additional tables ...");

      try
      {
         DatabaseHelper.executeDdlStatement(item, "DROP INDEX trans_inst_idx1");
      }
      catch (SQLException e)
      {
         String message = "Couldn't drop index TRANS_INST_IDX1 on table TRANSITION_INST. "
               + "Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }
      try
      {
         DatabaseHelper.executeDdlStatement(item, "DROP INDEX trans_inst_idx2");
      }
      catch (SQLException e)
      {
         String message = "Couldn't drop index TRANS_INST_IDX2 on table TRANSITION_INST. "
               + "Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      for (int j = 0; j < NEW_TABLE_LIST.length; j++)
      {
         try
         {
            NEW_TABLE_LIST[j].create(item);
         }
         catch (SQLException se)
         {
            trace.error("", se);

            if (se.getErrorCode() != 955)
            {
               throw new UpgradeException("Error creating table '"
                     + NEW_TABLE_LIST[j].getTableName() + "' :" + se.getMessage());
            }
         }
      }

      trace.info("Updating indexes.");

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX TRANS_TOKEN_IDX1 ON "
               + "TRANS_TOKEN(processInstance)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index TRANS_TOKEN_IDX1 on attribute "
               + "processInstance of table TRANS_TOKEN. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE UNIQUE INDEX TRANS_TOKEN_IDX2 ON "
               + "TRANS_TOKEN(oid)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index TRANS_TOKEN_IDX2 on attribute "
               + "oid of table TRANS_TOKEN. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX TRANS_INST_IDX1 ON "
               + "TRANS_INST(processInstance)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index TRANS_INST_IDX1 on attribute "
               + "processInstance of table TRANS_INST. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE UNIQUE INDEX TRANS_INST_IDX2 ON "
               + "TRANS_INST(oid)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index TRANS_INST_IDX2 on attribute "
               + "oid of table TRANS_INST. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "ALTER TABLE PROCESS_INSTANCE "
               + "ADD (TOKENCOUNT NUMBER)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't alter table PROCESS_INSTANCE. Message: "
               + e.getMessage();
         System.out.println(message);
         trace.warn(message, e);
      }
      try
      {
         DatabaseHelper.executeDdlStatement(item, "ALTER TABLE LOG_ENTRY "
               + "ADD(WORKFLOWUSER NUMBER)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't alter table LOG_ENTRY. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      System.out.println("Dropping obsolete tables ...");

      for (int i = 0; i < DROP_TABLE_LIST.length; i++)
      {
         try
         {
            DROP_TABLE_LIST[i].drop(item);
         }
         catch (SQLException e)
         {
            String message = "Couldn't drop table " + DROP_TABLE_LIST[i].getTableName()
                  + ". Message: " + e.getMessage();
            System.out.println(message);
            trace.warn(message, e);
         }
      }
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      // @todo laokoon (ub):
      // migrate running process instances.

      // @todo normalize timestamp entries
      // @todo (ub) e.g.
      normalizeTimestampColumn("workflowuser", "validfrom");
      normalizeTimestampColumn("workflowuser", "validto");
      normalizeTimestampColumn("process_instance", "terminationtime");

      try
      {
         DatabaseHelper.executeUpdate(item, "UPDATE ACTIVITY_INSTANCE SET STATE=6, "
               + "CURRENTUSERPERFORMER=0, CURRENTPERFORMER=0, "
               + "LASTMODIFICATIONTIME=" + new Date().getTime() + " "
               + "WHERE STATE NOT IN (2, 6)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't abort active activity instances. Message: "
               + e.getMessage();
         System.out.println(message);
         trace.warn(message, e);
      }
      try
      {
         DatabaseHelper.executeUpdate(item, "UPDATE PROCESS_INSTANCE SET STATE=1, "
               + "TERMINATIONTIME=" + new Date().getTime() + " "
               + "WHERE STATE NOT IN (1, 2)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't abort active process instances. Message: "
               + e.getMessage();
         System.out.println(message);
         trace.warn(message, e);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   private void normalizeTimestampColumn(String table, String field)
   {
      try
      {
         DatabaseHelper.executeUpdate(item, "UPDATE " + table + " set " + field
               + "= 0 where " + field + " is null");
         DatabaseHelper.executeUpdate(item, "UPDATE " + table + " set " + field
               + "= 0 where " + field + " < 0");

         item.commit();
      }
      catch (SQLException e)
      {
         trace.warn("", e);
      }
   }

   public Version getVersion()
   {
      return new Version(2, 6, 0);
   }
}
