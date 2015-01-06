/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
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
import java.text.MessageFormat;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.CreateTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R3_6_0from3_2_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R3_6_0from3_2_0RuntimeJob.class);
   
   private static final Version VERSION = Version.createFixedVersion(3, 6, 0);
   
   private static final String TABLE_NAME_PROC_INST = "process_instance";
   private static final String FIELD__PI_OID = "oid";
   private static final String FIELD__PI_STATE = "state";
   private static final String FIELD__PI_ROOT_PROC_INST = "rootProcessInstance";
   private static final String FIELD__PI_SCOPE_PROC_INST = "scopeProcessInstance";
   private static final String FIELD__PI_STARTING_ACT_INST = "startingActivityInstance";
   
   private static final String TABLE_NAME_ACT_INST = "activity_instance";
   private static final String FIELD__AI_OID = "oid";
   private static final String FIELD__AI_PROC_INST = "processInstance";
   
   private static final String TABLE_NAME_PROC_INST_SCOPE = "procinst_scope";
   private static final String FIELD__PIS_PROC_INST = "processInstance";
   private static final String FIELD__PIS_SCOPE_PROC_INST = "scopeProcessInstance";
   private static final String FIELD__PIS_ROOT_PROC_INST = "rootProcessInstance";
   
   private static final String TABLE_NAME_PROC_INST_HIER = "procinst_hierarchy";
   private static final String FIELD__PIH_PROC_INST = "processInstance";
   private static final String FIELD__PIH_SUB_PROC_INST = "subProcessInstance";
   
   private int batchSize = 500;

   R3_6_0from3_2_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY});

      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }
   }

   public Version getVersion()
   {
      return VERSION;
   }
   
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_PROC_INST)
      {
         private final FieldInfo OID = new FieldInfo(FIELD__PI_OID, Long.TYPE);
         private final FieldInfo STATE = new FieldInfo(FIELD__PI_STATE, Integer.TYPE);
         private final FieldInfo SCOPE_PROC_INST = new FieldInfo(FIELD__PI_SCOPE_PROC_INST, Long.TYPE);
         
         private final IndexInfo PI_IDX6 = new IndexInfo("proc_inst_idx6",
               true, new FieldInfo[] {SCOPE_PROC_INST, STATE, OID});

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{SCOPE_PROC_INST};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {PI_IDX6};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_PROC_INST_SCOPE)
      {
         private final FieldInfo PROC_INST = new FieldInfo(FIELD__PIS_PROC_INST, Long.TYPE);
         private final FieldInfo SCOPE_PROC_INST = new FieldInfo(FIELD__PIS_SCOPE_PROC_INST, Long.TYPE);
         private final FieldInfo ROOT_PROC_INST = new FieldInfo(FIELD__PIS_ROOT_PROC_INST, Long.TYPE);
         
         private final IndexInfo PI_IDX = new IndexInfo(TABLE_NAME_PROC_INST_SCOPE + "_i1",
               true, new FieldInfo[] {PROC_INST, SCOPE_PROC_INST});
         private final IndexInfo SCOPE_PI_IDX = new IndexInfo(TABLE_NAME_PROC_INST_SCOPE + "_i2",
               true, new FieldInfo[] {SCOPE_PROC_INST, PROC_INST});
         private final IndexInfo ROOT_PI_IDX = new IndexInfo(TABLE_NAME_PROC_INST_SCOPE + "_i3",
               new FieldInfo[] {ROOT_PROC_INST});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{PROC_INST, SCOPE_PROC_INST, ROOT_PROC_INST};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PI_IDX, SCOPE_PI_IDX, ROOT_PI_IDX};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_PROC_INST_HIER)
      {
         private final FieldInfo PROC_INST = new FieldInfo(FIELD__PIH_PROC_INST, Long.TYPE);
         private final FieldInfo SUB_PROC_INST = new FieldInfo(FIELD__PIH_SUB_PROC_INST, Long.TYPE);
         
         private final IndexInfo PI_IDX = new IndexInfo("procinst_hier_idx1",
               true, new FieldInfo[] {PROC_INST, SUB_PROC_INST});
         private final IndexInfo SUB_PI_IDX = new IndexInfo("procinst_hier_idx2",
               true, new FieldInfo[] {SUB_PROC_INST, PROC_INST});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{PROC_INST, SUB_PROC_INST};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PI_IDX, SUB_PI_IDX};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         // perform data migration in well defined TXs
         item.getConnection().setAutoCommit(false);

         info("Populating process instance scope table ...");

         // for all PIs: insert record (oid, rootPiOid, rootPiOid) into procinst_scope table
         populateScopeTable();

         info("Populating process instance hierarchy table ...");

         // for all root PIs: insert record (oid, oid) into procinst_hierarchy table
         populateHierarchyTableWithRootPIs();

         // for all non-root PIs: insert set of records (oid, parent-oid) into procinst_hierarchy table
         populateHierarchyTableWithNonrootPIs();
      }
      catch (SQLException e)
      {
         SQLException ne = e;
         do
         {
            trace.error("Failed migrating runtime item tables (nested exception).", ne);
         }
         while (null != (ne = ne.getNextException()));

         try
         {
            item.rollback();
         }
         catch (SQLException e1)
         {
            warn("Failed rolling back transaction.", e1);
         }
         error("Failed migrating runtime item tables.", e);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   private void populateScopeTable() throws SQLException
   {
      PreparedStatement selectProcessesStmt = null;
      PreparedStatement insertProcessScopesStmt = null;
      PreparedStatement updateProcessScopesStmt = null;
      try
      {
         StringBuffer selectCmd;
         if (item.getDbDescriptor().useAnsiJoins())
         {
            selectCmd = new StringBuffer()
                  .append("SELECT pi." + FIELD__PI_OID + ", pi." + FIELD__PI_ROOT_PROC_INST)
                  .append("  FROM " + TABLE_NAME_PROC_INST + " pi")
                  .append(" LEFT OUTER JOIN " + TABLE_NAME_PROC_INST_SCOPE + " pis")
                  .append("   ON (pi." + FIELD__PI_OID + " = pis." + FIELD__PIS_PROC_INST + ")")
                  .append(" WHERE pis.").append(FIELD__PIS_PROC_INST).append(" IS NULL");
         }
         else
         {
            selectCmd = new StringBuffer()
                  .append("SELECT pi." + FIELD__PI_OID + ", pi." + FIELD__PI_ROOT_PROC_INST)
                  .append("  FROM " + TABLE_NAME_PROC_INST + " pi")
                  .append(" WHERE pi.").append(FIELD__PI_OID).append(" NOT IN (")
                  .append("      SELECT pis.").append(FIELD__PIS_PROC_INST)
                  .append("        FROM ").append(TABLE_NAME_PROC_INST_SCOPE).append(" pis")
                  .append("      )");
         }

         StringBuffer insertCmd = new StringBuffer()
               .append("INSERT INTO ").append(TABLE_NAME_PROC_INST_SCOPE)
               .append("(" + FIELD__PIS_PROC_INST + ", " + FIELD__PIS_SCOPE_PROC_INST + ", " + FIELD__PIS_ROOT_PROC_INST + ")")
               .append(" VALUES (?, ?, ?)");

         StringBuffer updateCmd = new StringBuffer()
               .append("UPDATE ").append(TABLE_NAME_PROC_INST)
               .append(" SET " + FIELD__PI_SCOPE_PROC_INST + " = ?")
               .append(" WHERE oid = ?");


         Connection connection = item.getConnection();

         selectProcessesStmt = connection.prepareStatement(selectCmd.toString());
         insertProcessScopesStmt = connection.prepareStatement(insertCmd.toString());
         updateProcessScopesStmt = connection.prepareStatement(updateCmd.toString());

         int rowCounter = 0;

         ResultSet processes = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               processes = selectProcessesStmt.executeQuery();
               while (processes.next())
               {
                  eating = true;
                  try
                  {
                     final long processOid = processes.getLong(1);
                     final long rootProcessOid = processes.getLong(2);

                     insertProcessScopesStmt.setLong(1, processOid);
                     insertProcessScopesStmt.setLong(2, rootProcessOid);
                     insertProcessScopesStmt.setLong(3, rootProcessOid);
                     insertProcessScopesStmt.addBatch();

                     updateProcessScopesStmt.setLong(1, rootProcessOid);
                     updateProcessScopesStmt.setLong(2, processOid);
                     updateProcessScopesStmt.addBatch();

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

               if ( !eating)
               {
                  break;
               }

               insertProcessScopesStmt.executeBatch();
               updateProcessScopesStmt.executeBatch();
               connection.commit();

               info(MessageFormat.format(
                           "Committing inserts into table {0} and updates on table {1}" +
                           "after {2} process instances.",
                           new Object[] { TABLE_NAME_PROC_INST_SCOPE, TABLE_NAME_PROC_INST,
                                 new Integer(rowCounter) }));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(processes);
         }

         info("Process instance scopes table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectProcessesStmt);
         QueryUtils.closeStatement(insertProcessScopesStmt);
      }
   }

   private void populateHierarchyTableWithRootPIs() throws SQLException
   {
      PreparedStatement selectRootProcessesStmt = null;
      PreparedStatement insertRootProcessStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer();
         if (item.getDbDescriptor().useAnsiJoins())
         {
            selectCmd = new StringBuffer()
                  .append("SELECT pi." + FIELD__PI_OID)
                  .append("  FROM " + TABLE_NAME_PROC_INST + " pi")
                  .append(" LEFT OUTER JOIN " + TABLE_NAME_PROC_INST_HIER + " pih")
                  .append("   ON (pi." + FIELD__PI_OID + " = pih." + FIELD__PIH_PROC_INST + ")")
                  .append(" WHERE pi." + FIELD__PI_OID + " = pi." + FIELD__PI_ROOT_PROC_INST)
                  .append("   AND pih.").append(FIELD__PIS_PROC_INST).append(" IS NULL");
         }
         else
         {
            selectCmd
                  .append("SELECT pi." + FIELD__PI_OID)
                  .append("  FROM " + TABLE_NAME_PROC_INST + " pi")
                  .append(" WHERE pi." + FIELD__PI_OID + " = pi." + FIELD__PI_ROOT_PROC_INST)
                  .append("   AND pi." + FIELD__PI_OID + " NOT IN (")
                  .append("      SELECT pih.").append(FIELD__PIH_PROC_INST)
                  .append("        FROM ").append(TABLE_NAME_PROC_INST_HIER).append(" pih")
                  .append("      )");
         }

         StringBuffer insertCmd = new StringBuffer();
         insertCmd
               .append("INSERT INTO ").append(TABLE_NAME_PROC_INST_HIER)
               .append("(" + FIELD__PIH_PROC_INST + ", " + FIELD__PIH_SUB_PROC_INST + ")")
               .append(" VALUES (?, ?)");


         Connection connection = item.getConnection();

         selectRootProcessesStmt = connection.prepareStatement(selectCmd.toString());
         insertRootProcessStmt = connection.prepareStatement(insertCmd.toString());

         int rowCounter = 0;

         ResultSet processes = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               processes = selectRootProcessesStmt.executeQuery();
               while (processes.next())
               {
                  eating = true;
                  try
                  {
                     final long processOid = processes.getLong(1);

                     insertRootProcessStmt.setLong(1, processOid);
                     insertRootProcessStmt.setLong(2, processOid);
                     insertRootProcessStmt.addBatch();

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

               if ( !eating)
               {
                  break;
               }

               insertRootProcessStmt.executeBatch();
               connection.commit();

               info("Committing inserts into table '" + TABLE_NAME_PROC_INST_HIER
                     + "' after " + rowCounter + " process instances.");
            }
         }
         finally
         {
            QueryUtils.closeResultSet(processes);
         }

         info("Process instance hierrchy table upgraded with root process instances.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRootProcessesStmt);
         QueryUtils.closeStatement(insertRootProcessStmt);
      }
   }
   
   private void populateHierarchyTableWithNonrootPIs() throws SQLException
   {
      PreparedStatement selectProcessesStmt = null;
      PreparedStatement insertSelfRefStmt = null;
      PreparedStatement insertSuperRefStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer();
         selectCmd
               .append("SELECT pi." + FIELD__PI_OID + ", ai." + FIELD__AI_PROC_INST)
               .append("  FROM " + TABLE_NAME_PROC_INST + " pi, " + TABLE_NAME_ACT_INST + " ai")
               .append(" WHERE pi." + FIELD__PI_OID + " != pi." + FIELD__PI_ROOT_PROC_INST)
               .append("   AND pi." + FIELD__PI_STARTING_ACT_INST + " = ai." + FIELD__AI_OID)
               .append("   AND pi." + FIELD__PI_OID + " NOT IN (")
               .append("      SELECT ").append(FIELD__PIH_PROC_INST)
               .append("        FROM ").append(TABLE_NAME_PROC_INST_HIER)
               .append("      )")
               .append("   AND ai." + FIELD__AI_PROC_INST + " IN (")
               .append("      SELECT ").append(FIELD__PIH_SUB_PROC_INST)
               .append("        FROM ").append(TABLE_NAME_PROC_INST_HIER)
               .append("      )");

         StringBuffer insertSelfRefCmd = new StringBuffer();
         insertSelfRefCmd
               .append("INSERT INTO ").append(TABLE_NAME_PROC_INST_HIER)
               .append("(" + FIELD__PIH_PROC_INST + ", " + FIELD__PIH_SUB_PROC_INST + ") ")
               .append("VALUES (?, ?)");

         StringBuffer insertSuperRefCmd = new StringBuffer();
         insertSuperRefCmd
               .append("INSERT INTO ").append(TABLE_NAME_PROC_INST_HIER)
               .append("(" + FIELD__PIH_PROC_INST + ", " + FIELD__PIH_SUB_PROC_INST + ") ")
               .append("SELECT ppi." + FIELD__PIH_PROC_INST + ", pih." + FIELD__PIH_SUB_PROC_INST)
               .append("  FROM " + TABLE_NAME_PROC_INST_HIER + " ppi, " + TABLE_NAME_PROC_INST_HIER + " pih")
               .append(" WHERE ppi." + FIELD__PIH_SUB_PROC_INST + " = ?")
               .append("   AND pih." + FIELD__PIH_PROC_INST + " = ? AND pih." + FIELD__PIH_SUB_PROC_INST + " = ?");


         Connection connection = item.getConnection();

         selectProcessesStmt = connection.prepareStatement(selectCmd.toString());
         insertSelfRefStmt = connection.prepareStatement(insertSelfRefCmd.toString());
         insertSuperRefStmt = connection.prepareStatement(insertSuperRefCmd.toString());

         int rowCounter = 0;

         ResultSet processes = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               processes = selectProcessesStmt.executeQuery();
               while (processes.next())
               {
                  eating = true;
                  try
                  {
                     final long processOid = processes.getLong(1);
                     final long superProcessOid = processes.getLong(2);

                     insertSelfRefStmt.setLong(1, processOid);
                     insertSelfRefStmt.setLong(2, processOid);
                     insertSelfRefStmt.addBatch();

                     insertSuperRefStmt.setLong(1, superProcessOid);
                     insertSuperRefStmt.setLong(2, processOid);
                     insertSuperRefStmt.setLong(3, processOid);
                     insertSuperRefStmt.addBatch();

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

               if ( !eating)
               {
                  break;
               }

               insertSelfRefStmt.executeBatch();
               insertSuperRefStmt.executeBatch();
               connection.commit();

               info("Committing inserts into table '" + TABLE_NAME_PROC_INST_HIER
                     + "' after " + rowCounter + " process instances.");
            }
         }
         finally
         {
            QueryUtils.closeResultSet(processes);
         }

         info("Process instance hierrchy table upgraded with non-root process instances.");
      }
      finally
      {
         QueryUtils.closeStatement(selectProcessesStmt);
         QueryUtils.closeStatement(insertSelfRefStmt);
         QueryUtils.closeStatement(insertSuperRefStmt);
      }
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void printMigrateDataInfo()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
