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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Hashtable;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;


/**
 * An upgradable item representing the CARNOT runtime.
 *
 * @see UpgradableItem
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class RuntimeItem implements UpgradableItem
{
   private static final Logger trace = LogManager.getLogger(RuntimeItem.class);

   public static final String TABLE_PROPERTY = "property";
   public static final String TABLE_PROPERTY_SEQ = "PROPERTY_SEQ";
   public static final String FIELD_PROPERTY__OID = "oid";
   public static final String FIELD_PROPERTY__NAME = "name";
   public static final String FIELD_PROPERTY__VALUE = "value";
   public static final String FIELD_PROPERTY__LOCALE = "locale";

   public static final String PROPERTY_LOCALE_DEFAULT = "DEFAULT";

   public static final String PROP_PRODUCTION_AT_SCHEMA = "carnot.auditTrail.upgrade.productionSchema";

   private final String driverName;
   private final String connectURL;
   private final String user;
   private final String password;
   private final DBDescriptor dbDescriptor;

   private Connection connection;

   private Version version;

   private final boolean dryRun;

   private PrintStream sqlSpoolDevice;

   private boolean archiveFlagTested = false;
   private boolean archive = false;
   private Map sequenceMap = new Hashtable();


   public RuntimeItem()
   {
      this.dbDescriptor = null;
      this.driverName = null;
      this.connectURL = null;
      this.user = null;
      this.password = null;
      this.dryRun = false;
   }

   /**
    * Constructs the item with connection information to the audit trail.
    * The connection information should have all necessary rights to perform
    * the upgrade jobs, e.g. schema manipulation.
    */
   public RuntimeItem(String databaseType, String driverName, String connectURL,
         String user, String password)
   {
      this.dbDescriptor = DBDescriptor.create("AuditTrail", databaseType);
      this.driverName = driverName;
      this.connectURL = connectURL;
      this.user = user;
      this.password = password;

      this.dryRun = Parameters.instance().getBoolean(Upgrader.UPGRADE_DRYRUN, false);

      if (dryRun)
      {
         trace.info("Operating against runtime in readonly mode");
      }
   }

   public void initSqlSpoolDevice(File spoolFile) throws FileNotFoundException
   {
      this.sqlSpoolDevice = new PrintStream(new FileOutputStream(spoolFile));
   }

   public void setSqlSpoolDevice(PrintStream sqlSpoolDevice)
   {
      this.sqlSpoolDevice = sqlSpoolDevice;
   }

   public DBDescriptor getDbDescriptor()
   {
      return dbDescriptor;
   }

   public void spoolSqlComment(String comment)
   {
      if (null != sqlSpoolDevice)
      {
         sqlSpoolDevice.println();
         sqlSpoolDevice.print("// ");
         sqlSpoolDevice.print(comment);
         sqlSpoolDevice.println();
         sqlSpoolDevice.println();
      }
   }

   public void executeDdlStatement(String sql, boolean commit) throws SQLException
   {
      Statement statement = null;
      if (null != sqlSpoolDevice)
      {
         sqlSpoolDevice.print(sql);
         sqlSpoolDevice.println(";");
      }
      else if (dryRun)
      {
         trace.debug("Skipping DDL execution against readonly runtime item as requested");
      }
      else
      {
         try
         {
            trace.debug("executing SQL command: '" + sql + "'");
            statement = getConnection().createStatement();
            statement.executeUpdate(sql);
         }
         finally
         {
            QueryUtils.closeStatement(statement);
         }
      }
   }

   public Connection getConnection()
   {
      if (connection == null)
      {
         try
         {
            trace.info("Connecting database '" + connectURL + "' as user '" + user + "'");

            Reflect.getClassFromClassName(driverName);
            connection = DriverManager.getConnection(connectURL, user, password);

            if (connection.getAutoCommit())
            {
               trace.info("Disabling Auto-Commit mode for JDBC connection.");
               connection.setAutoCommit(false);
            }
         }
         catch (Exception e)
         {
            trace.warn("Failed obtaining JDBC connection", e);
            throw new UpgradeException(e.getMessage());
         }
      }

      return connection;
   }

   public Version getVersion()
   {
      if (version == null)
      {
         String versionString;

         try
         {
            versionString = getProperty(Constants.CARNOT_VERSION);

            if (null == versionString)
            {
               throw new UpgradeException("No runtime version detected");
            }
         }
         catch (SQLException e)
         {
            trace.warn("Failed reading runtime version", e);
            throw new UpgradeException("SQL error: " + e.getMessage());
         }

         version = new Version(versionString);
      }

      return version;
   }

   public void setVersion(Version version)
   {
      this.version = version;

      try
      {
         updateProperty(Constants.CARNOT_VERSION, version.toString());
      }
      catch (SQLException e)
      {
         trace.warn("Failed writing runtime version", e);
         throw new UpgradeException("SQL error: " + e.getMessage());
      }
   }

   public boolean hasProperty(String name) throws SQLException
   {
      boolean result;

      Statement statement = null;
      ResultSet rs = null;
      try
      {
         statement = getConnection().createStatement();

         rs = statement.executeQuery("SELECT " + FIELD_PROPERTY__VALUE
               + " FROM " + DatabaseHelper.getQualifiedName(TABLE_PROPERTY)
               + " WHERE " + FIELD_PROPERTY__NAME + "=" + stringLiteral(name));

         result = rs.next();
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(statement, rs);
      }

      return result;
   }

   public String getProperty(String name) throws SQLException
   {
      String value = null;

      Statement statement = null;
      ResultSet rs = null;
      try
      {
         statement = getConnection().createStatement();

         rs = statement.executeQuery("SELECT " + FIELD_PROPERTY__VALUE
               + " FROM " + DatabaseHelper.getQualifiedName(TABLE_PROPERTY)
               + " WHERE " + FIELD_PROPERTY__NAME + "=" + stringLiteral(name));

         if (rs.next())
         {
            value = rs.getString(1);
         }
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(statement, rs);
      }

      return value;
   }

   public void createProperty(String name, String value) throws SQLException
   {
      createProperty(name, value, true);
   }

   public void createProperty(String name, String value, boolean doCommit) throws SQLException
   {
      if (dryRun)
      {
         trace.debug("Skipping creation of property " + stringLiteral(name)
               + " with initial value " + stringLiteral(value)
               + " for readonly runtime item.");
      }
      else
      {
         Statement statement = null;
         try
         {
            StringBuilder sqlBuilder = new StringBuilder();
            statement = getConnection().createStatement();

            if (isArchiveAuditTrail() || getDbDescriptor().supportsSequences())
            {
               sqlBuilder.append("INSERT INTO ").append(DatabaseHelper.getQualifiedName(TABLE_PROPERTY));
               sqlBuilder.append(" (");
               sqlBuilder.append(FIELD_PROPERTY__OID).append(", ");
               sqlBuilder.append(FIELD_PROPERTY__NAME).append(", ");
               sqlBuilder.append(FIELD_PROPERTY__VALUE).append(", ");
               sqlBuilder.append(FIELD_PROPERTY__LOCALE);
               sqlBuilder.append(")");
               sqlBuilder.append(" VALUES (");
               sqlBuilder.append(getSequenceValue(
                     TABLE_PROPERTY_SEQ, TABLE_PROPERTY,  FIELD_PROPERTY__OID)).append(", ");
               sqlBuilder.append(stringLiteral(name)).append(", ");
               sqlBuilder.append(stringLiteral(value)).append(", ");
               sqlBuilder.append(stringLiteral(PROPERTY_LOCALE_DEFAULT));
               sqlBuilder.append(")");

            }
            else if (getDbDescriptor().supportsIdentityColumns())
            {
               statement.executeUpdate("INSERT INTO " + DatabaseHelper.getQualifiedName(TABLE_PROPERTY) + " ("
                     + FIELD_PROPERTY__NAME + ", "
                     + FIELD_PROPERTY__VALUE + ", "
                     + FIELD_PROPERTY__LOCALE
                     + ")"
                     + " VALUES ("
                     + stringLiteral(name) + ", "
                     + stringLiteral(value) + ", "
                     + stringLiteral(PROPERTY_LOCALE_DEFAULT)
                     + ")");
            }
            else
            {
               throw new UpgradeException("Unsupported DBMS kind, don't know how to "
                     + "create new properties.");
            }

            if (doCommit)
            {
               getConnection().commit();
            }
         }
         finally
         {
            QueryUtils.closeStatement(statement);
         }
      }
   }

   public void updateProperty(String name, String value) throws SQLException
   {
      if (dryRun)
      {
         trace.debug("Skipping update of property " + stringLiteral(name)
               + " to new value " + stringLiteral(value) + " for readonly runtime item.");
      }
      else
      {
         Statement statement = null;
         try
         {
            statement = getConnection().createStatement();
            statement.executeUpdate("UPDATE " + DatabaseHelper.getQualifiedName(TABLE_PROPERTY)
                  + "   SET " + FIELD_PROPERTY__VALUE + "=" + stringLiteral(value)
                  + " WHERE " + FIELD_PROPERTY__NAME + "=" + stringLiteral(name));

            getConnection().commit();
         }
         finally
         {
            QueryUtils.closeStatement(statement);
         }
      }
   }

   public void deleteProperty(String name) throws SQLException
   {
      deleteProperty(name, true);
   }

   public void deleteProperty(String name, boolean commit) throws SQLException
   {
      if (dryRun)
      {
         trace.debug("Skipping deletion of property " + stringLiteral(name)
               + " for readonly runtime item.");
      }
      else
      {
         Statement statement = null;
         try
         {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("DELETE FROM ");
            sqlBuilder.append(DatabaseHelper.getQualifiedName(TABLE_PROPERTY));
            sqlBuilder.append(" WHERE ");
            sqlBuilder.append(FIELD_PROPERTY__NAME);
            sqlBuilder.append("=");
            sqlBuilder.append(stringLiteral(name));
            if (null != sqlSpoolDevice)
            {
               sqlSpoolDevice.print(sqlBuilder.toString());
               sqlSpoolDevice.println(";");
               if(commit)
               {
                  sqlSpoolDevice.println("commit;");
               }
            }

            statement = getConnection().createStatement();
            statement.executeUpdate(sqlBuilder.toString());

            getConnection().commit();
         }
         finally
         {
            QueryUtils.closeStatement(statement);
         }
      }
   }

   public String getDescription()
   {
      return "Runtime Environment";
   }

   public String getDriverName()
   {
      return driverName;
   }

   public String getConnectURL()
   {
      return connectURL;
   }

   public String getUser()
   {
      return user;
   }

   public String getPassword()
   {
      return password;
   }

   public void commit() throws SQLException
   {
      trace.debug("Commiting transaction on current connection");
      getConnection().commit();
   }

   public void rollback() throws SQLException
   {
      trace.debug("Aborting transaction on current connection, changes rollbacked");
      getConnection().rollback();
   }

   private static String stringLiteral(String value)
   {
      return "'" + value + "'";
   }

   public String getSequenceValue(String sequenceName, String tableName, String fieldName) throws SQLException
   {
      if (!isArchiveAuditTrail())
      {
         Statement stmt = null;
         ResultSet rs = null;
         try
         {
            stmt = getConnection().createStatement();
            rs = stmt.executeQuery(getDbDescriptor().getCreatePKStatement(
                  DatabaseHelper.getSchemaName(), sequenceName));
            rs.next();
            return Long.toString(rs.getLong(1));
         }
         finally
         {
            QueryUtils.closeStatementAndResultSet(stmt, rs);
         }
      }
      Sequence sequence = (Sequence) sequenceMap.get(sequenceName);
      if (sequence == null)
      {
         long value = 0;
         Statement stmt = null;
         try
         {
            stmt = getConnection().createStatement();
            ResultSet rset = null;
            try
            {
               rset = stmt.executeQuery("SELECT MIN(" + fieldName + ") FROM " + DatabaseHelper.getQualifiedName(tableName));
               if (rset.next())
               {
                  value = rset.getLong(1);
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
         sequence = new Sequence(value);
         sequenceMap.put(sequenceName, sequence);
         System.out.println("Created memory sequence '" + sequenceName + "' initialized with: " + sequence);
      }
      return Long.toString(sequence.get());
   }

   public boolean isArchiveAuditTrail()
   {
      if (!archiveFlagTested)
      try
      {
         archive = hasProperty("carnot.audittrail.archive")
               && "true".equalsIgnoreCase(getProperty("carnot.audittrail.archive"));
         archiveFlagTested = true;
      }
      catch (SQLException e)
      {
         final String message = "Unable to determine if it's an archive audit trail.";

         try
         {
            rollback();
         }
         catch (SQLException e1)
         {
            System.out.println("Warning: " + message);
            if (e1 != null)
            {
               e1.printStackTrace(System.out);
               trace.warn(message, e1);
            }
         }
         throw new UpgradeException(message);
      }
      return archive;
   }

   public String getProductionAtSchemaName()
   {
      String productionSchema = System.getProperty(RuntimeItem.PROP_PRODUCTION_AT_SCHEMA);
      if (StringUtils.isEmpty(productionSchema))
      {
         throw new UpgradeException("Upgrade of an archive audit trail requires "
               + "access to the associated production audit trail. Please "
               + "configure the " + RuntimeItem.PROP_PRODUCTION_AT_SCHEMA
               + " system property.");
      }
      return productionSchema;
   }

   public static final class Sequence
   {
      long value;

      public Sequence(long value)
      {
         this.value = value < 0 ? value - 1: -1;
      }

      public long get()
      {
         return value--;
      }

      public String toString()
      {
         return Long.toString(value);
      }
   }
}
