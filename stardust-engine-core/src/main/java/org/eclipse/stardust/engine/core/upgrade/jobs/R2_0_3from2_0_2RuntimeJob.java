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

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


public class R2_0_3from2_0_2RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager
         .getLogger(R2_0_3from2_0_2RuntimeJob.class);

   public Version getVersion()
   {
      return Version.createFixedVersion(2, 0, 3);
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      System.out.println("Upgrading schema now (2.0.3 upgrade)...\n");

      System.out.println("Creating additional indexes ...");

      trace.info("Creating 'oid' indexes");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX ACT_PK_UNIQUE");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX ACTIVITY_IDX1 on activity(oid)");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE INDEX ACTIVITY_IDX2 on activity(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX ACTIVITY_INST_IDX1");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX ACTIVITY_INST_IDX1 on activity_instance(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE INDEX ACTIVITY_INST_IDX6 on activity_instance("
            + "starttime, activity, state)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX ACT_INST_LOG_IDX2 on activity_inst_log(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX DAEMON_LOG_IDX1 on daemon_log(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX DATA_VALUES_INDEX1");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX DATA_VALUES_INDEX1 on data_value(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX DOCUMENT_IDX1 on document(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX DOM_DOM_IDX1 on domain_domain(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX DOM_PROPERTY_IDX1 on domain_property(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX DOM_ORG_IDX1 on dom_organisation(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "ALTER INDEX LEAF_ACTV_IDX RENAME TO LEAF_ACTV_IDX1");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX LEAF_ACTV_IDX2 on leaf_activity(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX LOG_ENTRY_IDX1 on log_entry(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX MAIL_IDX1 on mail(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX MODEL_IDX1 on model(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX NOTF_LOG_IDX1 on notification_log(oid)");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE INDEX NOTF_LOG_IDX2 on notification_log("
            + "runtimeobject, notification, type)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX OIS_SESSION_IDX1 on ois_session_proxy(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX PROC_DEF_PK_UNIQUE");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX PROC_DEF_IDX1 on process_definition(oid)");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE INDEX PROC_DEF_IDX2 on process_definition(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX PROC_INST_IDX1");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX PROC_INST_IDX1 on process_instance(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX PROC_RECOV_IDX1 on proc_recovery_log(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX PROPERTY_IDX1 on property(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX RECOV_LOG_IDX1 on recovery_log(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX STR_DT_I2 on string_data(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX SUBDOM_SUPDOM_IDX1 on subdom_superdom(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX TRM_EMU_CLR_IDX1 on term_emu_clearing(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX TIMER_LOG_IDX1 on timer_log(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX TRANS_PK_UNIQUE");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX TRANS_IDX1 on transition(oid)");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE INDEX TRANS_IDX2 on transition(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX TRANS_INST_IDX2 on transition_inst(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX USER_ORG_IDX3 on user_organisation(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX USER_PROPERTY_IDX5 on user_property(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX USER_ROLE_INDEX3 on user_role(id)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "DROP INDEX WORKFLOWUSER_IDX1");
      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX WORKFLOWUSER_IDX1 on workflowuser(oid)");

      DatabaseHelper.tryExecuteDdlStatement(item,
            "CREATE UNIQUE INDEX WRKFL_DOM_IDX1 on worlflowdomain(oid)");
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
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
}
