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
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author born
 * @version $Revision$
 */
public class R4_7_0from4_6_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Version VERSION = Version.createFixedVersion(4, 7, 0);

   // UserSession table.
   private static final String AI_TABLE_NAME = "activity_instance";
   private static final String AI_FIELD_CURRENT_USER_PERFORMER = "currentUserPerformer";
   private static final String AI_FIELD_CURRENT_PERFORMER = "currentPerformer";
   private static final String AI_FIELD_ACTIVITY = "activity";
   private static final String AI_FIELD_PROCESS_INSTANCE = "processInstance";
   
   R4_7_0from4_6_0RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE});
   }

   public Version getVersion()
   {
      return VERSION;
   }
   
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.alterTable(item, new AlterTableInfo(AI_TABLE_NAME)
      {
         private final FieldInfo CURRENT_USER_PERFORMER = new FieldInfo(
               AI_FIELD_CURRENT_USER_PERFORMER, Long.TYPE, 0, false);
         private final FieldInfo CURRENT_PERFORMER = new FieldInfo(
               AI_FIELD_CURRENT_PERFORMER, Long.TYPE, 0, false);
         private final FieldInfo ACTIVITY = new FieldInfo(AI_FIELD_ACTIVITY, Long.TYPE,
               0, false);
         private final FieldInfo PROCESS_INSTANCE = new FieldInfo(
               AI_FIELD_PROCESS_INSTANCE, Long.TYPE, 0, false);

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {// 
                  new IndexInfo("activity_inst_idx3", false, new FieldInfo[] {
                        CURRENT_USER_PERFORMER, CURRENT_PERFORMER }) };
         }
         
         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {// 
                  new IndexInfo("activity_inst_idx8", false, new FieldInfo[] { ACTIVITY,
                        PROCESS_INSTANCE }) };
         }
      }, this);
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
