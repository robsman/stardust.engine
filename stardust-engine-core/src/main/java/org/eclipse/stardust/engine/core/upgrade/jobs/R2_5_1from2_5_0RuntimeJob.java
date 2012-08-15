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

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.upgrade.framework.TableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class R2_5_1from2_5_0RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   public static final Logger trace = LogManager.getLogger(R2_5_1from2_5_0RuntimeJob.class);

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      System.out.println("Creating additional tables ...");

      TableInfo dataTable = new TableInfo("data", "OID NUMBER, ID varchar2(50), " +
            "MODEL NUMBER, NAME varchar2(100), DESCRIPTION varchar2(4000)", false, false);
      try
      {
         dataTable.create(item);
      }
      catch (SQLException e)
      {
         trace.error("", e);

         if (e.getErrorCode() != 955)
         {
            throw new UpgradeException("Error creating table 'data' :" + e.getMessage());
         }
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
      return new Version(2, 5, 1);
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
