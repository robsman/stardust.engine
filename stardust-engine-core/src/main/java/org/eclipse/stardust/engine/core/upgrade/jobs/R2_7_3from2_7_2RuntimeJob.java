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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R2_7_3from2_7_2RuntimeJob extends OracleDB2AwareRuntimeUpgradeJob
{
   public static final Version VERSION = new Version(2, 7, 3);

   private int batchSize = 500;

   public R2_7_3from2_7_2RuntimeJob()
   {
      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      try
      {
         DatabaseHelper.executeDdlStatement(item, "ALTER TABLE PROCESS_INSTANCE ADD "
               + "rootProcessInstance "
               + item.getDbDescriptor().getSQLType(Long.TYPE, 0));
      }
      catch (SQLException e)
      {
         String message = "Couldn't add column rootProcessInstance to table "
               + "PROCESS_INSTANCE.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX PROC_INST_IDX5 "
               + "ON PROCESS_INSTANCE (rootProcessInstance, state, oid)");
         item.getConnection().commit();
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index INDEX PROC_INST_IDX5 for table "
               + "PROCESS_INSTANCE.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.tryExecuteDdlStatement(item, "DROP INDEX DATA_VALUES_INDEX6");
         DatabaseHelper.executeDdlStatement(item, "CREATE UNIQUE INDEX DATA_VALUES_INDEX6 "
               + "ON DATA_VALUE (data, processInstance)");
         item.getConnection().commit();
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index DATA_VALUES_INDEX6 for table "
               + "DATA_VALUE.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.tryExecuteDdlStatement(item, "DROP INDEX ACTIVITY_IDX2");
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX ACTIVITY_IDX2 "
               + "ON ACTIVITY (id, oid)");
         item.getConnection().commit();
      }
      catch (SQLException e)
      {
         String message = "Couldn't modify index ACTIVITY_IDX2 for table ACTIVITY.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.tryExecuteDdlStatement(item, "DROP INDEX DATA_IDX2");
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX DATA_IDX2 "
               + "ON DATA (id, oid)");
         item.getConnection().commit();
      }
      catch (SQLException e)
      {
         String message = "Couldn't modify index DATA_IDX2 for table DATA.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.tryExecuteDdlStatement(item, "DROP INDEX PROC_DEF_IDX2");
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX PROC_DEF_IDX2 "
               + "ON PROCESS_DEFINITION (id, oid)");
         item.getConnection().commit();
      }
      catch (SQLException e)
      {
         String message = "Couldn't modify index PROC_DEF_IDX2 for table PROCESS_DEFINITION.";
         warn(message, e);
      }
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      info("Populating rootProcessInstance column ...");

      try
      {
         upgradeRootProcesses();

         upgradeSubprocesses();
      }
      catch (SQLException e)
      {
         error("", e);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   private void upgradeRootProcesses() throws SQLException
   {
      PreparedStatement selectRootProcessesStmt = null;
      PreparedStatement updateRootProcessesStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer();
         selectCmd
               .append("SELECT PI.oid")
               .append("  FROM PROCESS_INSTANCE PI")
               .append(" WHERE PI.rootProcessInstance IS NULL")
               .append("   AND PI.startingActivityInstance IS NULL")
               .append(" ORDER BY PI.oid");

         StringBuffer updateCmd = new StringBuffer();
         updateCmd
               .append("UPDATE PROCESS_INSTANCE PI")
               .append("   SET PI.rootProcessInstance = ?")
               .append(" WHERE PI.oid = ?");


         Connection connection = item.getConnection();

         selectRootProcessesStmt = connection.prepareStatement(selectCmd.toString());
         updateRootProcessesStmt = connection.prepareStatement(updateCmd.toString());

         int rowCounter = 0;

         ResultSet rootProcesses = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               rootProcesses = selectRootProcessesStmt.executeQuery();
               while (rootProcesses.next())
               {
                  eating = true;
                  try
                  {
                     long processOID = rootProcesses.getLong(1);

                     updateRootProcessesStmt.setLong(1, processOID);
                     updateRootProcessesStmt.setLong(2, processOID);
                     updateRootProcessesStmt.addBatch();

                     ++rowCounter;
                  }
                  catch (SQLException e)
                  {
                     warn(e.getMessage(), null);
                  }

                  if (batchSize <= (rowCounter - rowCounterBackup))
                  {
                     break;
                  }
               }

               if (!eating)
               {
                  break;
               }

               updateRootProcessesStmt.executeBatch();
               connection.commit();

               info("Commit point for table 'PROCESS_INSTANCE' after " + rowCounter
                     + " root process instances.");
            }
         }
         finally
         {
            QueryUtils.closeResultSet(rootProcesses);
         }

         info("Root process instances upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRootProcessesStmt);
         QueryUtils.closeStatement(updateRootProcessesStmt);
      }
   }

   private void upgradeSubprocesses() throws SQLException
   {
      PreparedStatement selectSubProcessesStmt = null;
      PreparedStatement updateSubProcessesStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer();
         selectCmd
               .append("SELECT PI.oid, PARENT_PI.rootProcessInstance")
               .append("  FROM PROCESS_INSTANCE PI,")
               .append("       ACTIVITY_INSTANCE AI,")
               .append("       PROCESS_INSTANCE PARENT_PI")
               .append(" WHERE PI.startingActivityInstance = AI.oid")
               .append("   AND AI.processInstance =  PARENT_PI.oid")
               .append("   AND PI.rootProcessInstance IS NULL")
               .append("   AND PARENT_PI.rootProcessInstance IS NOT NULL")
               .append(" ORDER BY PI.oid");

         StringBuffer updateCmd = new StringBuffer();
         updateCmd
               .append("UPDATE PROCESS_INSTANCE PI")
               .append("   SET PI.rootProcessInstance = ?")
               .append(" WHERE PI.oid = ?");


         Connection connection = item.getConnection();

         selectSubProcessesStmt = connection.prepareStatement(selectCmd.toString());
         updateSubProcessesStmt = connection.prepareStatement(updateCmd.toString());

         int rowCounter = 0;

         ResultSet subProcesses = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               subProcesses = selectSubProcessesStmt.executeQuery();
               while (subProcesses.next())
               {
                  eating = true;
                  try
                  {
                     long processOID = subProcesses.getLong(1);
                     long rootProcessOID = subProcesses.getLong(2);

                     updateSubProcessesStmt.setLong(1, rootProcessOID);
                     updateSubProcessesStmt.setLong(2, processOID);
                     updateSubProcessesStmt.addBatch();

                     ++rowCounter;
                  }
                  catch (SQLException e)
                  {
                     warn(e.getMessage(), null);
                  }

                  if (batchSize <= (rowCounter - rowCounterBackup))
                  {
                     break;
                  }
               }

               if (!eating)
               {
                  break;
               }

               updateSubProcessesStmt.executeBatch();
               connection.commit();

               info("Commit point for table 'PROCESS_INSTANCE' after " + rowCounter
                     + " subprocess instances.");
            }
         }
         finally
         {
            QueryUtils.closeResultSet(subProcesses);
         }

         info("Subprocess instances upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectSubProcessesStmt);
         QueryUtils.closeStatement(updateSubProcessesStmt);
      }
   }

   public Version getVersion()
   {
      return VERSION;
   }
}
