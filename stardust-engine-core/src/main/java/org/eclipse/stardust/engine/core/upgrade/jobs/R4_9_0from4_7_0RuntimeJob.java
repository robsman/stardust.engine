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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.FieldDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author born
 * @version $Revision: $
 */
public class R4_9_0from4_7_0RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R4_9_0from4_7_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(4, 9, 0);

   private static final String LOG_ENTRY_TABLE_NAME = "log_entry";
   private static final String LOG_ENTRY_AI_COLUMN = "activityInstance";
   private static final String LOG_ENTRY_PI_COLUMN = "processInstance";

   R4_9_0from4_7_0RuntimeJob()
   {
      super(new DBMSKey[] { DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY, DBMSKey.POSTGRESQL, DBMSKey.SYBASE });
   }

   public Version getVersion()
   {
      return VERSION;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DBMSKey targetDBMS = item.getDbDescriptor().getDbmsKey();
      if (DBMSKey.ORACLE.equals(targetDBMS) || DBMSKey.ORACLE9i.equals(targetDBMS))
      {
         DDLManager ddlManager = new DDLManager(item.getDbDescriptor());
         for (Iterator iter = SchemaHelper.getPersistentClasses(item.getDbDescriptor())
               .iterator(); iter.hasNext();)
         {
            Class type = (Class) iter.next();
            final TypeDescriptor typeDescriptor = TypeDescriptor.get(type);
            try
            {
               if (ddlManager.containsTable(typeDescriptor.getSchemaName(),
                     typeDescriptor.getTableName(), item.getConnection()))
               {
                  final List modifyColumnCandidates = CollectionUtils.newArrayList();

                  List persistentFields = typeDescriptor.getPersistentFields();
                  for (Iterator fieldIter = persistentFields.iterator(); fieldIter
                        .hasNext();)
                  {
                     FieldDescriptor fieldDscr = (FieldDescriptor) fieldIter.next();
                     Class fieldType = fieldDscr.getField().getType();
                     int length = fieldDscr.getLength();
                     if (String.class.isAssignableFrom(fieldType)
                           && Integer.MAX_VALUE != length)
                     {
                        modifyColumnCandidates.add(new FieldInfo(fieldDscr.getField()
                              .getName(), String.class, length));
                     }
                  }

                  if (!modifyColumnCandidates.isEmpty())
                  {
                     DatabaseHelper.alterTable(item,
                           new AlterTableInfo(typeDescriptor.getTableName())
                           {
                              public FieldInfo[] getModifiedFields()
                              {
                                 return (FieldInfo[]) modifyColumnCandidates
                                       .toArray(new FieldInfo[modifyColumnCandidates
                                             .size()]);
                              }

                           }, this);
                  }
               }
            }
            catch (SQLException e)
            {
               error("Failed determining the tables to modify.", e);
            }
         }
      }
      
      DatabaseHelper.alterTable(item, new AlterTableInfo(LOG_ENTRY_TABLE_NAME)
      {
         private final FieldInfo ACTIVITY_INSTANCE = new FieldInfo(LOG_ENTRY_AI_COLUMN,
               Long.TYPE, 0, false);

         private final FieldInfo PROCESS_INSTANCE = new FieldInfo(LOG_ENTRY_PI_COLUMN,
               Long.TYPE, 0, false);

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {//
                  new IndexInfo("log_entry_idx2", false,
                        new FieldInfo[] {ACTIVITY_INSTANCE}),
                  new IndexInfo("log_entry_idx3", false,
                        new FieldInfo[] {PROCESS_INSTANCE})};
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

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
