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
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.CreateTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R4_0_0from3_6_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R4_0_0from3_6_0RuntimeJob.class);
   
   private static final Version VERSION = Version.createFixedVersion(4, 0, 0);
   
   private static final String FIELD_NAME_OID = "oid";
   private static final String FIELD_NAME_ID = "id";
   private static final String FIELD_NAME_NAME = "name";
   private static final String FIELD_NAME_DESCRIPTION = "description";
   private static final String FIELD_NAME_MODEL = "model";
   private static final String FIELD_NAME_PARTITION = "partition";
   private static final String FIELD_NAME_REALM = "realm";
   private static final String FIELD_NAME_DOMAIN = "domain";
   
   private static final FieldInfo FIELD_PARTITION = new FieldInfo(FIELD_NAME_PARTITION, Long.TYPE);
   private static final FieldInfo FIELD_REALM = new FieldInfo(FIELD_NAME_REALM, Long.TYPE);
   
   private static final String TABLE_NAME_DAEMON_LOG = "daemon_log";
   private static final String FIELD__DL_CODE = "code";
   private static final String FIELD__DL_TYPE = "type";
   
   private static final String TABLE_NAME_EVENT_BINDING = "event_binding";
   private static final String FIELD__EB_OBJECT_OID = "objectOID";
   private static final String FIELD__EB_TYPE = "type";
   private static final String FIELD__EB_MODEL = FIELD_NAME_MODEL;
   private static final String FIELD__EB_HANDLER_OID = "handlerOID";
   private static final String FIELD__EB_TARGET_STAMP = "targetStamp";
   
   private static final String TABLE_NAME_LOG_ENTRY = "log_entry";
   
   private static final String TABLE_NAME_PROPERTY = "property";
   
   private static final String TABLE_NAME_USER_GROUP = "usergroup";
   private static final String FIELD__UG_ID = FIELD_NAME_ID;
   
   private static final String TABLE_NAME_WORKFLOW_USER = "workflowuser";
   private static final String FIELD__USR_ACCOUNT = "account";
   
   private static final String TABLE_NAME_DOMAIN = "domain";
   private static final String FIELD__DOM_OID = FIELD_NAME_OID;
   private static final String FIELD__DOM_ID = FIELD_NAME_ID;
   private static final String FIELD__DOM_DESCRIPTION = FIELD_NAME_DESCRIPTION;
   private static final String FIELD__DOM_SUPER_DOMAIN = "superDomain";
   
   private static final String TABLE_NAME_DOMAIN_HIERARCHY = "domain_hierarchy";
   private static final String FIELD__DH_OID = FIELD_NAME_OID;
   private static final String FIELD__DH_SUPER_DOMAIN = "superDomain";
   private static final String FIELD__DH_SUB_DOMAIN = "subDomain";
   
   private static final String TABLE_NAME_WFUSER_DOMAIN = "wfuser_domain";
   private static final String FIELD__UD_OID = FIELD_NAME_OID;
   private static final String FIELD__UD_VALID_FROM = "validFrom";
   private static final String FIELD__UD_VALID_TO = "validTo";
   private static final String FIELD__UD_DOMAIN = FIELD_NAME_DOMAIN;
   private static final String FIELD__UD_USER = "wfUser";
   
   private static final String TABLE_NAME_WFUSER_REALM = "wfuser_realm";
   private static final String FIELD__RLM_OID = FIELD_NAME_OID;
   private static final String FIELD__RLM_ID = FIELD_NAME_ID;
   private static final String FIELD__RLM_NAME = FIELD_NAME_NAME;
   private static final String FIELD__RLM_DESCRIPTION = FIELD_NAME_DESCRIPTION;
   
   private static final String TABLE_NAME_WORKITEM = "workitem";
   private static final String FIELD__WI_AI = "activityInstance";
   private static final String FIELD__WI_PI = "processInstance";
   private static final String FIELD__WI_SCOPE_PI = "scopeProcessInstance";
   private static final String FIELD__WI_ROOT_PI = "rootProcessInstance";
   private static final String FIELD__WI_MODEL = FIELD_NAME_MODEL;
   private static final String FIELD__WI_ACTIVITY = "activity";
   private static final String FIELD__WI_STATE = "state";
   private static final String FIELD__WI_STARTED = "startTime";
   private static final String FIELD__WI_LAST_MODIFIED = "lastModificationTime";
   private static final String FIELD__WI_DOMAIN = FIELD_NAME_DOMAIN;
   private static final String FIELD__WI_PERFORMER_KIND = "performerKind";
   private static final String FIELD__WI_PERFORMER = "performer";
   
   private static final String TABLE_NAME_MODEL = "model";
   
   private static final String TABLE_NAME_PARTITION = "partition";
   private static final String FIELD__PRT_OID = FIELD_NAME_OID;
   private static final String FIELD__PRT_ID = FIELD_NAME_ID;
   private static final String FIELD__PRT_DESCRIPTION = FIELD_NAME_DESCRIPTION;

   private static final String TABLE_NAME_ACT_INST = "activity_instance";
   private static final String FIELD__AI_OID = FIELD_NAME_OID;
   private static final String FIELD__AI_PROC_INST = "processInstance";
   private static final String FIELD__AI_MODEL = FIELD_NAME_MODEL;
   private static final String FIELD__AI_ACTIVITY = "activity";
   private static final String FIELD__AI_STATE = "state";
   private static final String FIELD__AI_STARTED = "startTime";
   private static final String FIELD__AI_LAST_MODIFIED = "lastModificationTime";
   private static final String FIELD__AI_CURRENT_PERFORMER = "currentPerformer";
   private static final String FIELD__AI_CURRENT_USER_PERFORMER = "currentUserPerformer";
   
   private static final String TABLE_NAME_PROC_INST_SCOPE = "procinst_scope";
   private static final String FIELD__PIS_PROC_INST = "processInstance";
   private static final String FIELD__PIS_SCOPE_PROC_INST = "scopeProcessInstance";
   private static final String FIELD__PIS_ROOT_PROC_INST = "rootProcessInstance";
   
   private int batchSize = 500;

   R4_0_0from3_6_0RuntimeJob()
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
      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_DAEMON_LOG)
      {
         private final FieldInfo CODE = new FieldInfo(FIELD__DL_CODE, Integer.TYPE);
         private final FieldInfo TYPE = new FieldInfo(FIELD__DL_TYPE, String.class);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PARTITION};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {new IndexInfo(TABLE_NAME_DAEMON_LOG + "_idx2",
                  new FieldInfo[] {CODE, FIELD_PARTITION, TYPE})};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_EVENT_BINDING)
      {
         private final FieldInfo OBJECT_OID = new FieldInfo(FIELD__EB_OBJECT_OID, Long.TYPE);
         private final FieldInfo TYPE = new FieldInfo(FIELD__EB_TYPE, Integer.TYPE);
         private final FieldInfo HANDLER_OID = new FieldInfo(FIELD__EB_HANDLER_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD__EB_MODEL, Long.TYPE);
         private final FieldInfo TARGET_STAMP = new FieldInfo(FIELD__EB_TARGET_STAMP, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PARTITION};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo(TABLE_NAME_EVENT_BINDING + "_idx2", new FieldInfo[] {
                        OBJECT_OID, TYPE, HANDLER_OID, MODEL, FIELD_PARTITION}),
                  new IndexInfo(TABLE_NAME_EVENT_BINDING + "_idx3", new FieldInfo[] {
                        TARGET_STAMP, FIELD_PARTITION})};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_LOG_ENTRY)
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PARTITION};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_PROPERTY)
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PARTITION};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_USER_GROUP)
      {
         private final FieldInfo ID = new FieldInfo(FIELD__UG_ID, String.class);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PARTITION};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {new IndexInfo(TABLE_NAME_USER_GROUP + "_idx2", true,
                  new FieldInfo[] {ID, FIELD_PARTITION})};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_WORKFLOW_USER)
      {
         private final FieldInfo ACCOUNT = new FieldInfo(FIELD__USR_ACCOUNT, String.class);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_REALM};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {new IndexInfo(TABLE_NAME_WORKFLOW_USER + "_idx2",
                  true, new FieldInfo[] {ACCOUNT, FIELD_REALM})};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_DOMAIN)
      {
         private final FieldInfo OID = new FieldInfo(FIELD__DOM_OID, Long.TYPE, 0, true);
         private final FieldInfo ID = new FieldInfo(FIELD__DOM_ID, String.class, 50);
         private final FieldInfo DESCRIPTION = new FieldInfo(FIELD__DOM_DESCRIPTION, String.class, 4000);
         private final FieldInfo SUPER_DOMAIN = new FieldInfo(FIELD__DOM_SUPER_DOMAIN, Long.TYPE);
         
         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_DOMAIN + "_idx1",
               true, new FieldInfo[] {OID});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_DOMAIN + "_idx2",
               true, new FieldInfo[] {ID, FIELD_PARTITION});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, ID, DESCRIPTION, FIELD_PARTITION, SUPER_DOMAIN};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : TABLE_NAME_DOMAIN + "_seq";
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_DOMAIN_HIERARCHY)
      {
         private final FieldInfo OID = new FieldInfo(FIELD__DH_OID, Long.TYPE, 0, true);
         private final FieldInfo SUPER_DOMAIN = new FieldInfo(FIELD__DH_SUPER_DOMAIN, Long.TYPE);
         private final FieldInfo SUB_DOMAIN = new FieldInfo(FIELD__DH_SUB_DOMAIN, Long.TYPE);
         
         private final IndexInfo PK_IDX = new IndexInfo("domain_hier_idx1",
               true, new FieldInfo[] {OID});
         private final IndexInfo HIERARCHY_IDX = new IndexInfo("domain_hier_idx2",
               true, new FieldInfo[] {SUPER_DOMAIN, SUB_DOMAIN});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, SUPER_DOMAIN, SUB_DOMAIN};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, HIERARCHY_IDX};
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : TABLE_NAME_DOMAIN_HIERARCHY + "_seq";
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_WFUSER_DOMAIN)
      {
         private final FieldInfo OID = new FieldInfo(FIELD__UD_OID, Long.TYPE, 0, true);
         private final FieldInfo VALID_FROM = new FieldInfo(FIELD__UD_VALID_FROM, Long.TYPE);
         private final FieldInfo VALID_TO = new FieldInfo(FIELD__UD_VALID_TO, Long.TYPE);
         private final FieldInfo DOMAIN = new FieldInfo(FIELD__UD_DOMAIN, Long.TYPE);
         private final FieldInfo USER = new FieldInfo(FIELD__UD_USER, Long.TYPE);
         
         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_WFUSER_DOMAIN + "_idx1",
               true, new FieldInfo[] {OID});
         private final IndexInfo USR_IDX = new IndexInfo(TABLE_NAME_WFUSER_DOMAIN + "_idx2",
               true, new FieldInfo[] {DOMAIN, USER});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, VALID_FROM, VALID_TO, DOMAIN, USER};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, USR_IDX};
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : TABLE_NAME_WFUSER_DOMAIN + "_seq";
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_WFUSER_REALM)
      {
         private final FieldInfo OID = new FieldInfo(FIELD__RLM_OID, Long.TYPE, 0, true);
         private final FieldInfo ID = new FieldInfo(FIELD__RLM_ID, String.class, 50);
         private final FieldInfo NAME = new FieldInfo(FIELD__RLM_NAME, String.class, 100);
         private final FieldInfo DESCRIPTION = new FieldInfo(FIELD__RLM_DESCRIPTION, String.class, 4000);
         
         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_WFUSER_REALM + "_idx1",
               true, new FieldInfo[] {OID});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_WFUSER_REALM + "_idx2",
               true, new FieldInfo[] {ID, FIELD_PARTITION});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, ID, NAME, DESCRIPTION, FIELD_PARTITION};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : TABLE_NAME_WFUSER_REALM + "_seq";
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_WORKITEM)
      {
         private final FieldInfo AI = new FieldInfo(FIELD__WI_AI, Long.TYPE);
         private final FieldInfo PI = new FieldInfo(FIELD__WI_PI, Long.TYPE);
         private final FieldInfo SCOPE_PI = new FieldInfo(FIELD__WI_SCOPE_PI, Long.TYPE);
         private final FieldInfo ROOT_PI = new FieldInfo(FIELD__WI_ROOT_PI, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD__WI_MODEL, Long.TYPE);
         private final FieldInfo ACTIVITY = new FieldInfo(FIELD__WI_ACTIVITY, Long.TYPE);
         private final FieldInfo STATE = new FieldInfo(FIELD__WI_STATE, Integer.TYPE);
         private final FieldInfo STARTED = new FieldInfo(FIELD__WI_STARTED, Long.TYPE);
         private final FieldInfo LAST_MODIFIED = new FieldInfo(FIELD__WI_LAST_MODIFIED, Long.TYPE);
         private final FieldInfo DOMAIN = new FieldInfo(FIELD__WI_DOMAIN, Long.TYPE);
         private final FieldInfo PERFORMER_KIND = new FieldInfo(FIELD__WI_PERFORMER_KIND, Integer.TYPE);
         private final FieldInfo PERFORMER = new FieldInfo(FIELD__WI_PERFORMER, Long.TYPE);
         
         private final IndexInfo AI_IDX = new IndexInfo(TABLE_NAME_WORKITEM + "_idx1",
               true, new FieldInfo[] {AI});
         private final IndexInfo PERFORMER_IDX = new IndexInfo(TABLE_NAME_WORKITEM + "_idx2",
               new FieldInfo[] {PERFORMER, PERFORMER_KIND, STATE});
         private final IndexInfo STATE_IDX = new IndexInfo(TABLE_NAME_WORKITEM + "_idx3",
               new FieldInfo[] {STATE});
         private final IndexInfo PI_IDX = new IndexInfo(TABLE_NAME_WORKITEM + "_idx4",
               new FieldInfo[] {PI});
         private final IndexInfo SCOPE_PI_IDX = new IndexInfo(TABLE_NAME_WORKITEM + "_idx5",
               new FieldInfo[] {SCOPE_PI});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {
                  AI, PI, SCOPE_PI, ROOT_PI, MODEL, ACTIVITY, STATE, STARTED,
                  LAST_MODIFIED, DOMAIN, PERFORMER_KIND, PERFORMER};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {AI_IDX, PERFORMER_IDX, STATE_IDX, PI_IDX, SCOPE_PI_IDX};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_MODEL)
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[]{FIELD_PARTITION};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_PARTITION)
      {
         private final FieldInfo OID = new FieldInfo(FIELD__PRT_OID, Long.TYPE, 0, true);
         private final FieldInfo ID = new FieldInfo(FIELD__PRT_ID, String.class, 50);
         private final FieldInfo DESCRIPTION = new FieldInfo(FIELD__PRT_DESCRIPTION, String.class, 4000);
         
         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_PARTITION + "_idx1",
               true, new FieldInfo[] {OID});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_PARTITION + "_idx2",
               true, new FieldInfo[] {ID});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, ID, DESCRIPTION};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }

         public String getSequenceName()
         {
            return item.isArchiveAuditTrail() ? null : TABLE_NAME_PARTITION + "_seq";
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         // perform data migration in well defined TXs
         item.getConnection().setAutoCommit(false);
         
         info("Populating partition, domain and realm tables ...");
         
         populatePartitionTable();
         
         populateDomainTables();
         
         long realmOid = populateRealmTable();
         
         item.commit();
         
         info("Enabling partitioning of existing tables ...");
         
         populateKafkaColumn(TABLE_NAME_DAEMON_LOG, FIELD_NAME_OID,
               FIELD_NAME_PARTITION, 1);
      
         populateKafkaColumn(TABLE_NAME_EVENT_BINDING, FIELD_NAME_OID,
               FIELD_NAME_PARTITION, 1);
      
         populateKafkaColumn(TABLE_NAME_LOG_ENTRY, FIELD_NAME_OID,
               FIELD_NAME_PARTITION, 1);
      
         populateKafkaColumn(TABLE_NAME_PROPERTY, FIELD_NAME_OID,
               FIELD_NAME_PARTITION, -1);
      
         populateKafkaColumn(TABLE_NAME_USER_GROUP, FIELD_NAME_OID,
               FIELD_NAME_PARTITION, 1);
      
         populateKafkaColumn(TABLE_NAME_WORKFLOW_USER, FIELD_NAME_OID,
                  FIELD_NAME_REALM, realmOid);
         
         populateKafkaColumn(TABLE_NAME_MODEL, FIELD_NAME_OID,
               FIELD_NAME_PARTITION, 1);
      
         info("Populating work item table ...");
         
         populateWorkitemTable();
      }
      catch (SQLException sqle)
      {
         SQLException ne = sqle;
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
         error("Failed migrating runtime item tables.", sqle);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   private void populatePartitionTable() throws SQLException
   {
      PreparedStatement insertPartitionStmt = null;
      PreparedStatement selectPartitionStmt = null;

      try
      {
         StringBuffer insertCmd = new StringBuffer();
         
         if (item.isArchiveAuditTrail())
         {
            insertCmd.append("INSERT INTO ").append(TABLE_NAME_PARTITION)
                  .append("(" + FIELD__PRT_OID + ", " + FIELD__PRT_ID + ", " + FIELD__PRT_DESCRIPTION + ")")
                  .append(" VALUES (1, ?, ?)");
         }
         else if (item.getDbDescriptor().supportsSequences())
         {
            insertCmd.append("INSERT INTO ").append(TABLE_NAME_PARTITION)
                  .append("(" + FIELD__PRT_OID + ", " + FIELD__PRT_ID + ", " + FIELD__PRT_DESCRIPTION + ")")
                  .append(" VALUES (" + item.getDbDescriptor().getNextValForSeqString(null, TABLE_NAME_PARTITION + "_seq") + ", ?, ?)");
         }
         else
         {
            insertCmd.append("INSERT INTO ").append(TABLE_NAME_PARTITION)
                  .append("(" + FIELD__PRT_ID + ", " + FIELD__PRT_DESCRIPTION + ")")
                  .append(" VALUES (?, ?)");
         }
         
         StringBuffer selectCmd = new StringBuffer()
               .append("SELECT p." + FIELD__PRT_OID + ", p." + FIELD__PRT_ID)
               .append("  FROM " + TABLE_NAME_PARTITION + " p")
               .append(" WHERE p.").append(FIELD__PRT_ID).append(" ='default'");

         Connection connection = item.getConnection();

         insertPartitionStmt = connection.prepareStatement(insertCmd.toString());
         selectPartitionStmt = connection.prepareStatement(selectCmd.toString());

         boolean hasPartition = false;
         
         ResultSet partition = selectPartitionStmt.executeQuery();
         try
         {
            if (partition.next())
            {
               // partition is already existing
               long oid = partition.getLong(1);
               
               if (1 == oid)
               {
                  hasPartition = true;
               }
               else
               {
                  error("Partition OID after upgrade should be 1 but was " + oid + ".",
                        null);
               }
            }
         }
         finally
         {
            QueryUtils.closeResultSet(partition);
         }

         if ( !hasPartition)
         {
            insertPartitionStmt.setString(1, "default");
            insertPartitionStmt.setNull(2, java.sql.Types.VARCHAR);
            insertPartitionStmt.execute();
            
            partition = selectPartitionStmt.executeQuery();
            try
            {
               if (partition.next())
               {
                  long oid = partition.getLong(1);
                  
                  if (1 != oid)
                  {
                     error(
                           "Partition OID after upgrade should be 1 but was " + oid + ".",
                           null);
                  }
                  else
                  {
                     item.commit();

                     info("Partition table upgraded.");
                  }
               }
               else
               {
                  error("No partition can be found after upgrade.", null);
               }
            }
            finally
            {
               QueryUtils.closeResultSet(partition);
            }
         }
      }
      finally
      {
         QueryUtils.closeStatement(insertPartitionStmt);
         QueryUtils.closeStatement(selectPartitionStmt);
      }
   }

   private long populateDomainTables() throws SQLException
   {
      Long domainOid = null;
      
      PreparedStatement insertDomainStmt = null;
      PreparedStatement selectDomainStmt = null;

      PreparedStatement insertDomainHierStmt = null;

      try
      {
         StringBuffer insertDomainCmd = new StringBuffer();
         
         if (item.isArchiveAuditTrail())
         {
            insertDomainCmd.append("INSERT INTO ").append(TABLE_NAME_DOMAIN)
                  .append("(" + FIELD__DOM_OID + ", " + FIELD__DOM_ID + ", " + FIELD__DOM_DESCRIPTION + ", " + FIELD_NAME_PARTITION + ", " + FIELD__DOM_SUPER_DOMAIN + ")")
                  .append(" VALUES (1, ?, ?, 1, ?)");
         }
         else if (item.getDbDescriptor().supportsSequences())
         {
            insertDomainCmd.append("INSERT INTO ").append(TABLE_NAME_DOMAIN)
                  .append("(" + FIELD__DOM_OID + ", " + FIELD__DOM_ID + ", " + FIELD__DOM_DESCRIPTION + ", " + FIELD_NAME_PARTITION + ", " + FIELD__DOM_SUPER_DOMAIN + ")")
                  .append(" VALUES (" + item.getDbDescriptor().getNextValForSeqString(null, TABLE_NAME_DOMAIN + "_seq") + ", ?, ?, 1, ?)");
         }
         else
         {
            insertDomainCmd.append("INSERT INTO ").append(TABLE_NAME_DOMAIN)
                  .append("(" + FIELD__DOM_ID + ", " + FIELD__DOM_DESCRIPTION + ", " + FIELD_NAME_PARTITION + ", " + FIELD__DOM_SUPER_DOMAIN + ")")
                  .append(" VALUES (?, ?, 1, ?)");
         }
         
         StringBuffer selectDomainCmd = new StringBuffer()
               .append("SELECT d." + FIELD__DOM_OID + ", d." + FIELD__DOM_ID)
               .append("  FROM " + TABLE_NAME_DOMAIN + " d")
               .append(" WHERE d.").append(FIELD__DOM_ID).append(" ='default'");

         StringBuffer insertDomainHierCmd = new StringBuffer();
         
         if (item.isArchiveAuditTrail())
         {
            insertDomainHierCmd.append("INSERT INTO ").append(TABLE_NAME_DOMAIN_HIERARCHY)
                  .append("(" + FIELD__DH_OID + ", " + FIELD__DH_SUPER_DOMAIN + ", " + FIELD__DH_SUB_DOMAIN + ")")
                  .append(" VALUES (1, ?, ?)");
         }
         else if (item.getDbDescriptor().supportsSequences())
         {
            insertDomainHierCmd.append("INSERT INTO ").append(TABLE_NAME_DOMAIN_HIERARCHY)
                  .append("(" + FIELD__DH_OID + ", " + FIELD__DH_SUPER_DOMAIN + ", " + FIELD__DH_SUB_DOMAIN + ")")
                  .append(" VALUES (" + item.getDbDescriptor().getNextValForSeqString(null, TABLE_NAME_DOMAIN_HIERARCHY + "_seq") + ", ?, ?)");
         }
         else
         {
            insertDomainHierCmd.append("INSERT INTO ").append(TABLE_NAME_DOMAIN_HIERARCHY)
                  .append("(" + FIELD__DH_SUPER_DOMAIN + ", " + FIELD__DH_SUB_DOMAIN + ")")
                  .append(" VALUES (?, ?)");
         }
         
         Connection connection = item.getConnection();

         insertDomainStmt = connection.prepareStatement(insertDomainCmd.toString());
         selectDomainStmt = connection.prepareStatement(selectDomainCmd.toString());
         insertDomainHierStmt = connection.prepareStatement(insertDomainHierCmd.toString());
         
         ResultSet domain = selectDomainStmt.executeQuery();
         try
         {
            if (domain.next())
            {
               domainOid = new Long(domain.getLong(1));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(domain);
         }
         
         if (null == domainOid)
         {
            insertDomainStmt.setString(1, "default");
            insertDomainStmt.setNull(2, java.sql.Types.VARCHAR);
            insertDomainStmt.setNull(3, java.sql.Types.BIGINT);
            insertDomainStmt.execute();
            
            domain = selectDomainStmt.executeQuery();
            try
            {
               if (domain.next())
               {
                  domainOid = new Long(domain.getLong(1));
                  
                  insertDomainHierStmt.setLong(1, domainOid.longValue());
                  insertDomainHierStmt.setLong(2, domainOid.longValue());
                  insertDomainHierStmt.execute();
               }
               else
               {
                  // TODO
                  connection.rollback();
                  
                  error("No domain can be found after upgrade.", null);
               }
            }
            finally
            {
               QueryUtils.closeResultSet(domain);
            }
            
            info("Domain table upgraded.");
         }
      }
      finally
      {
         QueryUtils.closeStatement(insertDomainStmt);
         QueryUtils.closeStatement(selectDomainStmt);
         QueryUtils.closeStatement(insertDomainHierStmt);
      }
      
      return domainOid.longValue();
   }

   private long populateRealmTable() throws SQLException
   {
      Long realmOid = null;
      
      PreparedStatement insertRealmStmt = null;
      PreparedStatement selectRealmStmt = null;

      try
      {
         StringBuffer insertCmd = new StringBuffer();
         
         if (item.isArchiveAuditTrail())
         {
            insertCmd.append("INSERT INTO ").append(TABLE_NAME_WFUSER_REALM)
                  .append("(" + FIELD__RLM_OID + ", " + FIELD__RLM_ID + ", " + FIELD__RLM_NAME + ", " + FIELD__RLM_DESCRIPTION + ", " + FIELD_NAME_PARTITION + ")")
                  .append(" VALUES (1, ?, ?, ?, ?)");
         }
         else if (item.getDbDescriptor().supportsSequences())
         {
            insertCmd.append("INSERT INTO ").append(TABLE_NAME_WFUSER_REALM)
                  .append("(" + FIELD__RLM_OID + ", " + FIELD__RLM_ID + ", " + FIELD__RLM_NAME + ", " + FIELD__RLM_DESCRIPTION + ", " + FIELD_NAME_PARTITION + ")")
                  .append(" VALUES (" + item.getDbDescriptor().getNextValForSeqString(null, TABLE_NAME_WFUSER_REALM + "_seq") + ", ?, ?, ?, ?)");
         }
         else
         {
            insertCmd.append("INSERT INTO ").append(TABLE_NAME_WFUSER_REALM)
                  .append("(" + FIELD__RLM_ID + ", " + FIELD__RLM_NAME + ", " + FIELD__RLM_DESCRIPTION + ", " + FIELD_NAME_PARTITION + ")")
                  .append(" VALUES (?, ?, ?, ?)");
         }
         
         StringBuffer selectCmd = new StringBuffer()
               .append("SELECT r." + FIELD__RLM_OID + ", r." + FIELD__RLM_ID)
               .append("  FROM " + TABLE_NAME_WFUSER_REALM + " r")
               .append(" WHERE r.").append(FIELD__RLM_ID).append(" ='carnot'");

         Connection connection = item.getConnection();

         insertRealmStmt = connection.prepareStatement(insertCmd.toString());
         selectRealmStmt = connection.prepareStatement(selectCmd.toString());
         
         ResultSet realm = selectRealmStmt.executeQuery();
         try
         {
            if (realm.next())
            {
               realmOid = new Long(realm.getLong(1));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(realm);
         }

         if (null == realmOid)
         {
            insertRealmStmt.setString(1, "carnot");
            insertRealmStmt.setString(2, "CARNOT");
            insertRealmStmt.setNull(3, java.sql.Types.VARCHAR);
            insertRealmStmt.setLong(4, 1);
            insertRealmStmt.execute();
            
            realm = selectRealmStmt.executeQuery();
            try
            {
               if (realm.next())
               {
                  realmOid = new Long(realm.getLong(1));
               }
               else
               {
                  // TODO
                  connection.rollback();
                  
                  error("No realm can be found after upgrade.", null);
               }
            }
            finally
            {
               QueryUtils.closeResultSet(realm);
            }

            info("Realm table upgraded.");
         }
      }
      finally
      {
         QueryUtils.closeStatement(insertRealmStmt);
         QueryUtils.closeStatement(selectRealmStmt);
      }
      
      return realmOid.longValue();
   }

   private void populateKafkaColumn(String tableName, String oidColumn,
         String kafkaColumn, long realmOid) throws SQLException
   {
      PreparedStatement selectRowsStmt = null;
      PreparedStatement updateRowsStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer()
               .append("SELECT r." + oidColumn)
               .append("  FROM " + tableName + " r")
               .append(" WHERE r.").append(kafkaColumn).append(" IS NULL");

         StringBuffer updateCmd = new StringBuffer()
               .append("UPDATE ").append(tableName)
               .append(" SET " + kafkaColumn + " = ?")
               .append(" WHERE " + oidColumn + " = ?");


         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());
         updateRowsStmt = connection.prepareStatement(updateCmd.toString());

         int rowCounter = 0;

         ResultSet pendingRows = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               pendingRows = selectRowsStmt.executeQuery();
               while (pendingRows.next())
               {
                  eating = true;
                  try
                  {
                     final long rowOid = pendingRows.getLong(1);

                     updateRowsStmt.setLong(1, realmOid);
                     updateRowsStmt.setLong(2, rowOid);
                     updateRowsStmt.addBatch();

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

               updateRowsStmt.executeBatch();
               connection.commit();

               info(MessageFormat.format("Committing updates of field {1} on table {0}"
                     + " after {2} rows.", new Object[] {
                     tableName, kafkaColumn, new Integer(rowCounter)}));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }

         info(tableName + " table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.closeStatement(updateRowsStmt);
      }
   }

   private void populateWorkitemTable() throws SQLException
   {
      PreparedStatement selectRowsStmt = null;
      PreparedStatement insertRowsStmt = null;
      try
      {
         StringBuffer selectCmd = new StringBuffer()
               .append("SELECT ai." + FIELD__AI_OID + ", ai." + FIELD__AI_PROC_INST
                     + ", pis." + FIELD__PIS_SCOPE_PROC_INST + ", pis." + FIELD__PIS_ROOT_PROC_INST
                     + ", ai." + FIELD__AI_MODEL + ", ai." + FIELD__AI_ACTIVITY + ", ai." + FIELD__AI_STATE
                     + ", ai." + FIELD__AI_STARTED + ", ai." + FIELD__AI_LAST_MODIFIED
                     + ", ai." + FIELD__AI_CURRENT_PERFORMER + ", ai." + FIELD__AI_CURRENT_USER_PERFORMER)
               .append("  FROM " + TABLE_NAME_ACT_INST + " ai, ").append(TABLE_NAME_PROC_INST_SCOPE + " pis")
               .append(" WHERE ai.").append(FIELD__AI_STATE).append(" IN (").append(ActivityInstanceState.SUSPENDED).append(", ").append(ActivityInstanceState.APPLICATION).append(")")
               .append("   AND (ai.").append(FIELD__AI_CURRENT_PERFORMER).append(" > 0").append(" OR ai.").append(FIELD__AI_CURRENT_USER_PERFORMER).append(" <> 0)")
               .append("   AND ai.").append(FIELD__AI_OID).append(" NOT IN (")
               .append("      SELECT wi.").append(FIELD__WI_AI)
               .append("        FROM ").append(TABLE_NAME_WORKITEM).append(" wi")
               .append("      )")
               .append("   AND ai." + FIELD__AI_PROC_INST + " = pis." + FIELD__PIS_PROC_INST);

         StringBuffer insertCmd = new StringBuffer()
            .append("INSERT INTO ").append(TABLE_NAME_WORKITEM)
            .append("(" + FIELD__WI_AI + ", " + FIELD__WI_PI + ", " + FIELD__WI_SCOPE_PI + ", " + FIELD__WI_ROOT_PI
                  + ", " + FIELD__WI_MODEL + ", " + FIELD__WI_ACTIVITY + ", " + FIELD__WI_STATE
                  + ", " + FIELD__WI_STARTED + ", " + FIELD__WI_LAST_MODIFIED + ", " + FIELD__WI_DOMAIN
                  + ", " + FIELD__WI_PERFORMER_KIND + ", " + FIELD__WI_PERFORMER + ")")
            .append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");


         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());
         insertRowsStmt = connection.prepareStatement(insertCmd.toString());

         int rowCounter = 0;

         ResultSet pendingRows = null;
         try
         {
            boolean eating = true;

            while (eating)
            {
               eating = false;
               final long rowCounterBackup = rowCounter;

               pendingRows = selectRowsStmt.executeQuery();
               while (pendingRows.next())
               {
                  eating = true;
                  try
                  {
                     final long aiOid = pendingRows.getLong(1);
                     final long piOid = pendingRows.getLong(2);
                     final long scopePiOid = pendingRows.getLong(3);
                     final long rootPiOid = pendingRows.getLong(4);
                     final long modelOid = pendingRows.getLong(5);
                     final long activityRtOid = pendingRows.getLong(6);
                     final int state = pendingRows.getInt(7);
                     final long started = pendingRows.getLong(8);
                     final long lastModified = pendingRows.getLong(9);
                     final long currentPerformer = pendingRows.getLong(10);
                     final long currentUserPerformer = pendingRows.getLong(11);
                     
                     final int performerKind;
                     final long performer;
                     if (0 < currentUserPerformer)
                     {
                        performerKind = PerformerType.USER;
                        performer = currentUserPerformer;
                     }
                     else if (0 < currentPerformer)
                     {
                        performerKind = PerformerType.MODEL_PARTICIPANT;
                        performer = currentPerformer;
                     }
                     else if (0 > currentPerformer)
                     {
                        performerKind = PerformerType.USER_GROUP;
                        performer = -currentPerformer;
                     }
                     else
                     {
                        performerKind = PerformerType.NONE;
                        performer = 0;
                     }

                     insertRowsStmt.setLong(1, aiOid);
                     insertRowsStmt.setLong(2, piOid);
                     insertRowsStmt.setLong(3, scopePiOid);
                     insertRowsStmt.setLong(4, rootPiOid);
                     insertRowsStmt.setLong(5, modelOid);
                     insertRowsStmt.setLong(6, activityRtOid);
                     insertRowsStmt.setInt(7, state);
                     insertRowsStmt.setLong(8, started);
                     insertRowsStmt.setLong(9, lastModified);
                     insertRowsStmt.setLong(10, 0);
                     insertRowsStmt.setInt(11, performerKind);
                     insertRowsStmt.setLong(12, performer);
                     
                     insertRowsStmt.addBatch();

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

               insertRowsStmt.executeBatch();
               connection.commit();

               info(MessageFormat.format("Committing insert into table {0}"
                     + "after {1} rows.", new Object[] {
                     TABLE_NAME_WORKITEM, new Integer(rowCounter)}));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }

         info("Work item table upgraded.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.closeStatement(insertRowsStmt);
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
