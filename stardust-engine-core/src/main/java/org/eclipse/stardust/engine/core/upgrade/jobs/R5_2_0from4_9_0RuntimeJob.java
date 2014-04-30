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
import java.sql.SQLException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.UpdateColumnInfo;


/**
 * @author born
 * @version $Revision: $
 */
public class R5_2_0from4_9_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R5_2_0from4_9_0RuntimeJob.class);

   private int batchSize = 500;
   private static final Version VERSION = Version.createFixedVersion(5, 2, 0);

   private static final String FIELD__OID = "oid";
   private static final String D_TABLE_NAME = "department";
   private static final String D_FIELD__ID = "id";
   private static final String D_FIELD__NAME = "name";
   private static final String D_FIELD__PARTITION = "partition";
   private static final String D_FIELD__PARENTDEPARTMENT = "parentDepartment";
   private static final String D_FIELD__DESCRIPTION = "description";
   private static final String D_FIELD__ORGANIZATION = "organization";
   private static final String D_PK_SEQUENCE = "department_seq";
   private static final String D_IDX1 = "department_idx1";
   private static final String D_IDX2 = "department_idx2";

   private static final String DH_TABLE_NAME = "department_hierarchy";
   private static final String DH_FIELD__SUPERDEPARTMENT = "superDepartment";
   private static final String DH_FIELD__SUBDEPARTMENT = "subDepartment";
   private static final String DH_IDX1 = "department_hier_idx1";
   private static final String DH_IDX2 = "department_hier_idx2";

   private static final String AI_TABLE_NAME = "activity_instance";
   private static final String AI_FIELD__CURRENT_PERFORMER = "currentPerformer";
   private static final String AI_FIELD__CURRENT_USER_PERFORMER = "currentUserPerformer";
   private static final String AI_FIELD__CURRENT_DEPARTMENT = "currentDepartment";
   private static final String AI_IDX2 = "activity_inst_idx2";
   private static final String AI_IDX3 = "activity_inst_idx3";

   private static final String WI_TABLE_NAME = "workitem";
   private static final String WI_FIELD_ACTIVITYINSTANCE = "activityInstance";
   private static final String WI_FIELD__PERFORMER_KIND = "performerKind";
   private static final String WI_FIELD__PERFORMER = "performer";
   private static final String WI_FIELD__DEPARTMENT = "department";
   private static final String WI_FIELD__STATE = "state";
   private static final String WI_IDX2 = "workitem_idx2";

   private static final String AIH_TABLE_NAME = "act_inst_history";
   private static final String AIH_FIELD__PROCESSINSTANCE = "processInstance";
   private static final String AIH_FIELD__DEPARTMENT = "department";
   private static final String AIH_FIELD__ON_BEHALF_OF_DEPARTMENT = "onBehalfOfDepartment";

   private static final String UP_TABLE_NAME = "user_participant";
   private static final String UP_FIELD__MODEL = "model";
   private static final String UP_FIELD__PARTICIPANT = "participant";
   private static final String UP_FIELD__DEPARTMENT = "department";
   private static final String UP_IDX2 = "user_particip_idx2";

   private static final String WU_TABLE_NAME = "workflowuser";
   private static final String WU_EXTENDED_STATE = "extendedState";

   R5_2_0from4_9_0RuntimeJob()
   {
      super(new DBMSKey[] { DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE });
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
      final FieldInfo genericOidColumn = new FieldInfo(FIELD__OID, Long.TYPE, 0, true);
      DatabaseHelper.createTable(item, new CreateTableInfo(D_TABLE_NAME)
      {
         private final FieldInfo ID = new FieldInfo(D_FIELD__ID, String.class, 50);
         private final FieldInfo NAME = new FieldInfo(D_FIELD__NAME, String.class, 150);
         private final FieldInfo PARTITION = new FieldInfo(D_FIELD__PARTITION, Long.TYPE);
         private final FieldInfo PARENTDEPARTMENT = new FieldInfo(D_FIELD__PARENTDEPARTMENT, Long.TYPE);
         private final FieldInfo DESCRIPTION = new FieldInfo(D_FIELD__DESCRIPTION, String.class, 4000);
         private final FieldInfo ORGANIZATION = new FieldInfo(D_FIELD__ORGANIZATION, Long.TYPE);

         private final IndexInfo IDX1 = new IndexInfo(D_IDX1,
               true, new FieldInfo[] { genericOidColumn });

         private final IndexInfo IDX2 = new IndexInfo(D_IDX2,
               true, new FieldInfo[] { ID, ORGANIZATION, PARENTDEPARTMENT });

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] { genericOidColumn, ID, NAME, PARTITION, PARENTDEPARTMENT,
                  DESCRIPTION, ORGANIZATION };
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] { IDX1, IDX2 };
         }

         @Override
         public String getSequenceName()
         {
            return D_PK_SEQUENCE;
         }

      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(DH_TABLE_NAME)
      {
         private final FieldInfo SUPERDEPARTMENT = new FieldInfo(DH_FIELD__SUPERDEPARTMENT, Long.TYPE);
         private final FieldInfo SUBDEPARTMENT = new FieldInfo(DH_FIELD__SUBDEPARTMENT, Long.TYPE);

         private final IndexInfo IDX1 = new IndexInfo(DH_IDX1,
               true, new FieldInfo[] { SUPERDEPARTMENT, SUBDEPARTMENT});

         private final IndexInfo IDX2 = new IndexInfo(DH_IDX2,
               true, new FieldInfo[] { SUBDEPARTMENT, SUPERDEPARTMENT });

         @Override
         public FieldInfo[] getFields()
         {
            return new FieldInfo[] { SUPERDEPARTMENT, SUBDEPARTMENT };
         }

         @Override
         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] { IDX1, IDX2 };
         }

         @Override
         public String getSequenceName()
         {
            return null;
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(AI_TABLE_NAME)
      {
         private final FieldInfo CURRENT_USER_PERFORMER = new FieldInfo(AI_FIELD__CURRENT_USER_PERFORMER, Long.TYPE);
         private final FieldInfo CURRENT_PERFORMER = new FieldInfo(AI_FIELD__CURRENT_PERFORMER, Long.TYPE);
         private final FieldInfo CURRENT_DEPARTMENT = new FieldInfo(AI_FIELD__CURRENT_DEPARTMENT, Long.TYPE);

         private final IndexInfo IDX2 = new IndexInfo(AI_IDX2, false,
               new FieldInfo[] { CURRENT_PERFORMER, CURRENT_DEPARTMENT });

         private final IndexInfo IDX3 = new IndexInfo(AI_IDX3, false,
               new FieldInfo[] { CURRENT_USER_PERFORMER, CURRENT_PERFORMER,
                     CURRENT_DEPARTMENT });

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { CURRENT_DEPARTMENT };
         }

         @Override
         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] { IDX2, IDX3 };
         }

         @Override
         public void executeDmlBeforeIndexCreation(RuntimeItem item) throws SQLException
         {
            Connection connection = item.getConnection();
            connection.setAutoCommit(false);
            setColumnsToZero(item, getTableName(), genericOidColumn, CURRENT_DEPARTMENT);
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(WI_TABLE_NAME)
      {
         private final FieldInfo PERFORMER_KIND = new FieldInfo(WI_FIELD__PERFORMER_KIND, Long.TYPE);
         private final FieldInfo PERFORMER = new FieldInfo(WI_FIELD__PERFORMER, Long.TYPE);
         private final FieldInfo DEPARTMENT = new FieldInfo(WI_FIELD__DEPARTMENT, Long.TYPE);
         private final FieldInfo STATE = new FieldInfo(WI_FIELD__STATE, Integer.TYPE);
         //primary key for the workitem table
         private final FieldInfo ACTIVITY_INSTANCE = new FieldInfo(
               WI_FIELD_ACTIVITYINSTANCE, Long.TYPE, true);

         private final IndexInfo IDX2 = new IndexInfo(WI_IDX2, false,
               new FieldInfo[] { PERFORMER, DEPARTMENT, PERFORMER_KIND, STATE });


         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { DEPARTMENT };
         }

         @Override
         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] { IDX2 };
         }

         @Override
         public void executeDmlBeforeIndexCreation(RuntimeItem item) throws SQLException
         {
            Connection connection = item.getConnection();
            connection.setAutoCommit(false);
            setColumnsToZero(item, getTableName(), ACTIVITY_INSTANCE, DEPARTMENT);
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(AIH_TABLE_NAME)
      {
         private final FieldInfo DEPARTMENT = new FieldInfo(AIH_FIELD__DEPARTMENT, Long.TYPE);
         private final FieldInfo ON_BEHALF_OF_DEPARTMENT = new FieldInfo(AIH_FIELD__ON_BEHALF_OF_DEPARTMENT, Long.TYPE);
         //primary key for act_inst_history table
         private final FieldInfo PROCESS_INSTANCE = new FieldInfo(AIH_FIELD__PROCESSINSTANCE, Long.TYPE, true);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { DEPARTMENT, ON_BEHALF_OF_DEPARTMENT };
         }

         @Override
         public void executeDmlBeforeIndexCreation(RuntimeItem item)
               throws SQLException
         {
            Connection connection = item.getConnection();
            connection.setAutoCommit(false);
            setColumnsToZero(item, getTableName(), PROCESS_INSTANCE, DEPARTMENT, ON_BEHALF_OF_DEPARTMENT);
         }
      }, this);


      DatabaseHelper.alterTable(item, new AlterTableInfo(UP_TABLE_NAME)
      {
         private final FieldInfo MODEL = new FieldInfo(UP_FIELD__MODEL, Long.TYPE);
         private final FieldInfo PARTICIPANT = new FieldInfo(UP_FIELD__PARTICIPANT, Long.TYPE);
         private final FieldInfo DEPARTMENT = new FieldInfo(UP_FIELD__DEPARTMENT, Long.TYPE);

         private final IndexInfo IDX2 = new IndexInfo(UP_IDX2, false,
               new FieldInfo[] { PARTICIPANT, DEPARTMENT, MODEL });


         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { DEPARTMENT };
         }

         @Override
         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] { IDX2 };
         }

         @Override
         public void executeDmlBeforeIndexCreation(RuntimeItem item) throws SQLException
         {
            Connection connection = item.getConnection();
            connection.setAutoCommit(false);

            setColumnsToZero(item, getTableName(), genericOidColumn, DEPARTMENT);
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(WU_TABLE_NAME)
      {
         private final FieldInfo EXTENDED_STATE = new FieldInfo(WU_EXTENDED_STATE,
               Integer.TYPE);

         @Override
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] { EXTENDED_STATE };
         }

      }, this);


      final FieldInfo extendedState = new FieldInfo(WU_EXTENDED_STATE, Long.TYPE);
      try
      {
         setColumnsToZero(item, WU_TABLE_NAME, genericOidColumn, extendedState);
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update new column " + WU_TABLE_NAME + "."
               + WU_EXTENDED_STATE + "to 0.");
      }
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   protected void setColumnsToZero(RuntimeItem item, String tableName, FieldInfo oidColumn, FieldInfo...columnsToUpdate)
         throws SQLException
   {
      UpdateColumnInfo[] updateInfos = new UpdateColumnInfo[columnsToUpdate.length];
      for(int i=0; i<columnsToUpdate.length; i++)
      {
         FieldInfo columnToUpdate = columnsToUpdate[i];
         updateInfos[i] = new UpdateColumnInfo(columnToUpdate, 0);
      }

      //check javadoc for further information on this method
      DatabaseHelper.setColumnValuesInBatch(item, tableName, oidColumn, batchSize,
            updateInfos);
   }




   @Override
   protected void printUpgradeSchemaInfo()
   {
      info("A new table 'department' with the columns " +
      		"'oid', 'id', 'name', 'partition', 'parentDepartment', 'description', 'organization' " +
            		"and indexes 'department_idx1' and 'department_idx2' will be created.");
      info("A new table 'department_hierarchy' with the columns 'superDepartment', 'subDepartment' " +
      		"and indexes 'department_hier_idx1' and 'department_hier_idx2' will be created.");
      info("The new columns 'currentUserPerformer', 'currentPerformer' and 'currentDepartment' " +
      		"will be created in table 'activity_instance' " +
      		"and indexes 'activity_inst_idx2' and 'activity_inst_idx3' will be modified.");
      info("The new columns 'performerKind', 'performer', 'department' and 'state' will be created in table 'workitem' " +
      		"and index 'workitem_idx2' will be modified.");
      info("The new columns 'department' and 'onBehalfOfDepartment' will be created in table 'act_inst_history'.");
      info("The new columns 'model', 'participant' and 'department' will be created in table 'user_participant' " +
      		"and index 'user_particip_idx2' will be modified.");
      info("The new column 'extendedState' will be created in table 'workflowuser'.");
   }

   @Override
   protected void printMigrateDataInfo()
   {
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
