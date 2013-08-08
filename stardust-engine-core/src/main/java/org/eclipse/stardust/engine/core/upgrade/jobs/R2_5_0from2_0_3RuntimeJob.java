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

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.TableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * This job only applies to runtimes smaller than 2.1.0. If applied, it immediately
 * upgrades to a 2.1.0 runtime.
 *
 * @see #matches
 * @author jmahmood, kwinkler, rsauer, ubirkemeyer
 * @version $Revision$
 */
public class R2_5_0from2_0_3RuntimeJob extends OracleAwareRuntimeUpgradeJob
{

   private static final Logger trace = LogManager
         .getLogger(R2_5_0from2_0_3RuntimeJob.class);

   private static final TableInfo[] NEW_TABLE_LIST =
         new TableInfo[]
         {
            new TableInfo("message", "oid number, channel varchar2(32), " +
         "arrivalTime number, redeliveryTries number")
         };

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      System.out.println("Upgrading schema now (2.5.0 upgrade)...\n");
      System.out.println("Creating additional tables ...");

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
         DatabaseHelper.executeDdlStatement(item,
               "CREATE UNIQUE INDEX MESSAGE_IDX1 ON message (oid)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX MESSAGE_IDX2 ON message (channel)");
         //         DatabaseHelper.executeUpdate(item,
         //               "CREATE INDEX MESSAGE_IDX3 ON message (arrivalTime, redeliveryTries)");
      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   public Version getVersion()
   {
      return Version.createFixedVersion(2, 5, 0);
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
