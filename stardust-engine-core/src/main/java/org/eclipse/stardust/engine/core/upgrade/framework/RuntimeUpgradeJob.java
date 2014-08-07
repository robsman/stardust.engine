/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.stardust.common.config.*;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;


/**
 * @author kberberich
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class RuntimeUpgradeJob extends UpgradeJob implements UpgradeObserver
{
   private static final Logger trace = LogManager.getLogger(RuntimeUpgradeJob.class);

   private static final Version VERSION_3 = Version.createFixedVersion(3, 0, 0);

   protected RuntimeItem item;

   public static final String UPGRADE_LEVEL0 = "0";
   public static final String UPGRADE_LEVEL1 = "1";
   public static final String UPGRADE_LEVEL2 = "2";
   public static final String UPGRADE_LEVEL3 = "3";
   public static final String UPGRADE_LEVEL4 = "4";
   public static final String UPGRADE_LEVEL = "upgrade.level";
   private static final int ATOM_SIZE = 1000;
   private static final String MODEL_DATA_TYPE = "model";
   private int warn;

   public UpgradableItem run(UpgradableItem item, boolean recover) throws UpgradeException
   {
      try
      {
         ParametersFacade.pushGlobals();
         GlobalParameters.globals().set(ModelManager.class.getSimpleName() + ".CHECK_AUDITTRAIL_VERSION", Boolean.FALSE);

         final boolean dryRun = Parameters.instance().getBoolean(Upgrader.UPGRADE_DRYRUN,
               false);
         final boolean dataUpgrade = Parameters.instance().getBoolean(
               RuntimeUpgrader.UPGRADE_DATA, false);
         final boolean schemaUpgrade = Parameters.instance().getBoolean(
               RuntimeUpgrader.UPGRADE_SCHEMA, false);
         final boolean verbose = Parameters.instance().getBoolean(
               RuntimeUpgrader.UPGRADE_VERBOSE, false);

         this.item = (RuntimeItem) item;

         assertCompatibility();

         int recoveryLevel = checkForRecovery(recover);
         switch (recoveryLevel)
         {
            case 0:
               if (schemaUpgrade || (!dryRun && !dataUpgrade))
               {
                  info("Upgrading schema...");

                  this.item.spoolSqlComment(this.getVersion() + " schema upgrade DDL");
                  if (verbose)
                  {
                    printUpgradeSchemaInfo();
                  }
                  upgradeSchema(recover);
                  setUpgradeState(UPGRADE_LEVEL1);
                  info("...Schema upgrade done.");
               }
               else
               {
                  info("Skipping schema upgrade DDL as requested.");
                  if (verbose)
                  {
                    printUpgradeSchemaInfo();
                  }
               }
               // falling through to level 1
            case 1:
               if (!dryRun && !schemaUpgrade)
               {
                  info("Migrating data...");
                  if (verbose)
                  {
                    printMigrateDataInfo();
                  }
                  migrateData(recover);
                  setUpgradeState(UPGRADE_LEVEL2);
                  info("...Data Migration done.");
               }
               else
               {
                  info("Skipping data migration as requested.");
                  if (verbose)
                  {
                    printMigrateDataInfo();
                  }
               }
               // falling through to level 2
            case 2:
               if (!dryRun && !schemaUpgrade)
               {
                  info("Upgrading Model...");
                  upgradeModel(recover);
                  setUpgradeState(UPGRADE_LEVEL3);
                  info("...Model migration done.");
               }
               else
               {
                  info("Skipping model migration as requested.");
               }
               // falling through to level 3
            case 3:
               // upgrades to before 3.0.0 did not use schema finalization
               if (0 <= getVersion().compareTo(VERSION_3))
               {
                  if (schemaUpgrade || (!dryRun && !dataUpgrade))
                  {
                     info("Finalizing schema...");

                     this.item.spoolSqlComment(this.getVersion() + " schema finalization DDL");
                     if(verbose)
                     {
                        printFinalizeSchemaInfo();
                     }
                     finalizeSchema(recover);
                     setUpgradeState(UPGRADE_LEVEL4);
                     info("...Schema finalization done.");
                  }
                  else
                  {
                     info("Skipping schema finalization DDL as requested.");
                     if(verbose)
                     {
                        printFinalizeSchemaInfo();
                     }
                  }
               }
               // falling through to level 4
            case 4:
               info("Upgrade to version " + getVersion()
                  + " done, upgrading runtime version stamp...");
               upgradeRuntimeVersion();
               finalizeUpgradeState();
               setProductName();
               info("...Version stamp updated.");
         }
         if (warn > 0)
         {
            info("!!There where " + warn + " warnings or errors. Check your log file.");
         }
      }
      finally
      {
         ParametersFacade.popGlobals();
      }

      return item;
   }

   /**
    * Asserts if the upgrade job is valid for the current runtime item.
    */
   protected abstract void assertCompatibility() throws UpgradeException;

   protected abstract void upgradeSchema(boolean recover) throws UpgradeException;

   protected abstract void migrateData(boolean recover) throws UpgradeException;

   protected abstract void finalizeSchema(boolean recover) throws UpgradeException;

   /**
    * This method prints detailed information that describes what is done by this upgrade schema task.
    */
   protected abstract void printUpgradeSchemaInfo();

   /**
    * This method prints detailed information that describes what is done by this migrate data task.
    */
   protected abstract void printMigrateDataInfo();

   /**
    * This method prints detailed information that describes what is done by this finalize schema task.
    */
   protected abstract void printFinalizeSchemaInfo();

   protected void upgradeModel(boolean recover) throws UpgradeException
   {
      ModelItem model = null;
      // TODO table model will be modified for 3.0 runtimes
      Iterator oidItr = (0 <= VERSION_3.compareTo(getVersion()))
            ? getPre30ModelOIDs()
            : getPost30ModelOIDs();
      while (oidItr.hasNext())
      {
         long oid = ((Long) oidItr.next()).longValue();
         model = retrieveModelFromAuditTrail(oid);
         if (model != null)
         {
            ModelUpgrader modelUpgrader = new ModelUpgrader(model);
            ModelItem newModel = (ModelItem) modelUpgrader.upgradeToVersion(getVersion(),
                  recover);

            if (newModel != null && !model.getModel().equals(newModel.getModel()))
            {
               dumpModel(oid, newModel);
            }
         }
      }
   }

   protected ModelItem retrieveModelFromAuditTrail(long oid)
   {
      Connection connection = item.getConnection();
      StringBuffer buffer = new StringBuffer();
      ResultSet rset = null;
      boolean useEndMarker = item.getDbDescriptor().isTrimmingTrailingBlanks();
      try
      {
         String query = "SELECT data"
               + "  FROM " + DatabaseHelper.getQualifiedName("STRING_DATA")
               + " WHERE data_type='model'"
               + "   AND objectid = " + oid
               + " ORDER BY oid";

         Statement statement = connection.createStatement();
         rset = statement.executeQuery(query);
         while (rset.next())
         {
            String data = rset.getString("data");
            if (useEndMarker)
            {
               data = data.substring(0, data.length() - 1);
            }
            buffer.append(data);
         }
      }
      catch (SQLException e)
      {
         throw new UpgradeException("Error while accessing model with oid " + oid + " : "
               + e.getMessage());
      }
      finally
      {
         QueryUtils.closeResultSet(rset);
      }
      if (buffer.length() == 0)
      {
         return null;
      }
      return new ModelItem(buffer.toString());
   }

   protected void dumpModel(long oid, ModelItem model) throws UpgradeException
   {
      if (Parameters.instance().getBoolean(Upgrader.UPGRADE_DRYRUN, false))
      {
         throw new UpgradeException("Unable to write model to readonly runtime");
      }

      boolean supportsIdentityColumns = item.getDbDescriptor().supportsIdentityColumns();
      Connection connection = item.getConnection();
      try
      {
         String stringDataTableName = DatabaseHelper.getQualifiedName("STRING_DATA");

         Statement deleteStmt = connection.createStatement();
         deleteStmt.executeUpdate("UPDATE "+stringDataTableName+" SET objectid=" + (-1 * oid)
               + " where objectid=" + oid);
         String insertStmt;
         if (supportsIdentityColumns)
         {
            insertStmt = "INSERT INTO "+stringDataTableName+" (objectid, data_type, data)"
                  + " VALUES (?, ?, ?)";
         }
         else
         {
            insertStmt = "INSERT INTO "+stringDataTableName+" (oid, objectid, data_type, data)"
                  + " VALUES (?, ?, ?, ?)";
         }

         PreparedStatement lshStmt = connection.prepareStatement(insertStmt.toString());
         writeToStringDataTable(lshStmt, oid, MODEL_DATA_TYPE, model.getModel());
         lshStmt.executeBatch();
         connection.commit();
      }
      catch (SQLException x)
      {
         String message = x.getMessage();
         trace.warn("Failed dumping model", x);
         try
         {
            connection.rollback();
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new UpgradeException(e.getMessage());
         }
         trace.warn("Failed dumping model.", x);
         for (x = x.getNextException(); null != x; )
         {
            trace.warn("Failed dumping model.", x);
         }
         throw new UpgradeException(message);
      }
   }

   protected void upgradeRuntimeVersion() throws UpgradeException
   {
      item.setVersion(getVersion());
   }

   protected void setProductName() throws UpgradeException
   {
      try
      {
         if(item.hasProperty(Constants.PRODUCT_NAME))
         {
            item.updateProperty(Constants.PRODUCT_NAME, CurrentVersion.getProductName());
         }
         else
         {
            item.createProperty(Constants.PRODUCT_NAME, CurrentVersion.getProductName());
         }
      }
      catch (SQLException e)
      {
         throw new UpgradeException("Failed to write upgrade state to database : "
               + e.getMessage());
      }
   }

   private void setUpgradeState(String level) throws UpgradeException
   {
      try
      {
         item.updateProperty(UPGRADE_LEVEL, level);
      }
      catch (SQLException e)
      {
         throw new UpgradeException("Failed to write upgrade state to database : "
               + e.getMessage());
      }
   }

   private void finalizeUpgradeState()
   {
      try
      {
         item.deleteProperty(UPGRADE_LEVEL);
      }
      catch (SQLException e)
      {
         throw new UpgradeException(
               "Unable to remove upgrade level information from database : "
               + e.getMessage());
      }
   }

   private String getRecoveryState()
   {
      String recoveryState = null;
      try
      {
         recoveryState = item.getProperty(UPGRADE_LEVEL);
      }
      catch (SQLException e)
      {
         throw new UpgradeException("Unable to access database for recovery state : "
               + e.getMessage());
      }

      return recoveryState;
   }

   private void initializeUpgradeLevel()
   {
      try
      {
         item.createProperty(UPGRADE_LEVEL, UPGRADE_LEVEL0);
      }
      catch (SQLException e)
      {
         throw new UpgradeException(
               "Unbable to initialize upgrade level in database : " + e.getMessage());
      }
   }

   private int checkForRecovery(boolean recover)
   {
      String upgradeError = getRecoveryState();
      if (upgradeError == null)
      {
         initializeUpgradeLevel();
         return 0;
      }
      if (!recover)
      {
         throw new UpgradeException("Found unsuccessful upgrade. Recovery is required.");
      }
      try
      {
         return Integer.parseInt(upgradeError);
      }
      catch (NumberFormatException e)
      {
         throw new UpgradeException(
               "Upgrade level from database is not a number (" + upgradeError + ")");
      }
   }

   protected void rollback()
   {
      try
      {
         item.rollback();
      }
      catch (SQLException e)
      {
         warn("Failed undoing changes to runtime item.", e);
      }
   }

   protected void writeToStringDataTable(PreparedStatement lshStmt,
         long objectOid, String tableName, String value) throws SQLException
   {
      if (Parameters.instance().getBoolean(Upgrader.UPGRADE_DRYRUN, false))
      {
         throw new UpgradeException("Unable to write model to readonly runtime");
      }

      final int nSlices = ((value.length() / ATOM_SIZE)) + 1;

      boolean supportsSequences = item.getDbDescriptor().supportsSequences();
      // obtain loopCount OID values and sort them in increasing order
      SortedSet oids = new TreeSet();
      if (supportsSequences)
      {
         while (oids.size() < nSlices)
         {
            oids.add(new Long(item.getSequenceValue("STRING_DATA_SEQ", "STRING_DATA",
                  "oid")));
         }
      }
      Iterator oidItr = oids.iterator();

      boolean useEndMarker = item.getDbDescriptor().isTrimmingTrailingBlanks();
      for (int i = 0; i < nSlices; i++)
      {
         String part = null;

         if (i == (nSlices - 1))
         {
            part = value.substring(ATOM_SIZE * i);
         }
         else
         {
            part = value.substring(ATOM_SIZE * i, ATOM_SIZE * (i + 1));
         }

         if (useEndMarker)
         {
            part = part + LargeStringHolder.END_MARKER;
         }

         int colIdx = 1;
         if (supportsSequences)
         {
            lshStmt.setLong(colIdx++, ((Long) oidItr.next()).longValue());
         }
         lshStmt.setLong(colIdx++, objectOid);
         lshStmt.setString(colIdx++, tableName);
         lshStmt.setString(colIdx++, part);
         lshStmt.addBatch();
      }
   }

   protected Iterator getPre30ModelOIDs()
   {
      List oids = new ArrayList();
      try
      {
         Statement stmt = null;
         try
         {
            stmt = item.getConnection().createStatement();
            ResultSet rset = null;
            try
            {
               String tableName = DatabaseHelper.getQualifiedName("model");
               rset = stmt.executeQuery("SELECT modelOID FROM "+tableName);
               while (rset.next())
               {
                  oids.add(new Long(rset.getLong(1)));
               }
            }
            finally
            {
               QueryUtils.closeResultSet(rset);
            }
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
      catch (SQLException e)
      {
         trace.warn("Searching for model versions failed.");
      }

      return oids.iterator();
   }

   protected Iterator getPost30ModelOIDs()
   {
      List oids = new ArrayList();
      try
      {
         Statement stmt = null;
         try
         {
            stmt = item.getConnection().createStatement();
            ResultSet rset = null;
            try
            {
               String tableName = DatabaseHelper.getQualifiedName("model");
               rset = stmt.executeQuery("SELECT oid FROM "+tableName);
               while (rset.next())
               {
                  oids.add(new Long(rset.getLong(1)));
               }
            }
            finally
            {
               QueryUtils.closeResultSet(rset);
            }
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
      catch (SQLException e)
      {
         trace.warn("Searching for model versions failed.");
      }

      return oids.iterator();
   }

   public void info(String message)
   {
      System.out.println(message);
      trace.info(message);
   }

   public void warn(String message, Throwable e)
   {
      warn++;
      System.out.println("Warning: " + message);
      if (e != null)
      {
         e.printStackTrace(System.out);
         trace.warn(message, e);
      }
      else
      {
         trace.warn(message);
      }
   }

   public void fatal(String message, Throwable e)
   {
      warn++;
      System.out.println("Fatal Error: " + message);
      if (e != null)
      {
         e.printStackTrace(System.out);
         trace.fatal(message, e);
      }
      else
      {
         trace.fatal(message);
      }
      throw new UpgradeException(message);
   }

   public void error(String message, Throwable e)
   {
      warn++;
      System.out.println("Error: " + message);
      if (e != null)
      {
         e.printStackTrace(System.out);
         trace.error(message, e);
      }
      else
      {
         trace.error(message);
      }
      if (!"true".equalsIgnoreCase(System.getProperty("carnot.upgrade.ignoreerrors")))
      {
         throw new UpgradeException(message);
      }
      else
      {
         System.out.println("...continueing anyway.");
         trace.info("...continueing anyway.");
      }
   }

}
