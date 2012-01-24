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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.*;
import java.util.Date;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.SynonymTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.TableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * This job only applies to runtimes smaller than 2.0.0. If applied, it immediately
 * upgrades to a 2.0.2 runtime.
 *
 * @see #matches
 * @author jmahmood, kwinkler, rsauer, ubirkemeyer
 * @version $Revision$
 */
public class R2_0_2fromPre2_0_0RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager
         .getLogger(R2_0_2fromPre2_0_0RuntimeJob.class);

   public static final int string_value_COLUMN_LENGTH = 128;

   private static final SynonymTableInfo[] TABLE_SYNONYM_LIST =
         new SynonymTableInfo[]
         {
            new SynonymTableInfo(
                  "activity_instance_log", "activity_inst_log"),
            new SynonymTableInfo(
                  "domain_organisation", "dom_organisation"),
            new SynonymTableInfo(
                  "process_recovery_log", "proc_recovery_log"),
            new SynonymTableInfo(
                  "subdomain_superdomain", "subdom_superdom"),
            new SynonymTableInfo(
                  "transition_instance", "transition_inst")
         };

   private static final TableInfo[] NEW_TABLE_LIST =
         new TableInfo[]
         {
            new TableInfo(
                  "string_data",
                  "oid number, objectid number, data_type varchar2(32), "
         + "data varchar2(4000)", true, false),
            new TableInfo(
                  "term_emu_clearing",
                  "oid number, processinstance number,"
         + "initiator number, error varchar2(4000), "
         + "username varchar2(300)", true, true)
         };

   private final String BATCH_SIZE_PROPERTY = "ag.carnot.upgrade.batch_size";
   private final int DEFAULT_BATCH_SIZE = 100;

   public R2_0_2fromPre2_0_0RuntimeJob()
   {
   }

   /**
    *
    */
   public Version getVersion()
   {
      return new Version(2, 0, 2);
   }

   /**
    *
    */
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      /**
       * 1) Create synonyms if necessary
       * 2) create Table stringdata
       */
      System.out.println("Upgrading schema now (2.0.2 upgrade)...\n");

      System.out.println("Creating synonyms ...");

      for (int i = 0; i < TABLE_SYNONYM_LIST.length; i++)
      {
         try
         {
            TABLE_SYNONYM_LIST[i].create(item);
         }
         catch (SQLException se)
         {
            trace.error("", se);

            if (se.getErrorCode() != 955)
            {
               throw new UpgradeException(
                     "Error creating synonym '"
                     + TABLE_SYNONYM_LIST[i].getSynonymName() + "' "
                     + se.getMessage());
            }
         }
      }

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

      addWorkflowUserColumns();

      try
      {
         System.out.println("Creating additional columns ...");
         trace.info("Creating columns for table 'data_value'");
         DatabaseHelper.executeDdlStatement(
               item, "ALTER TABLE DATA_VALUE ADD (" +
               "STRING_VALUE VARCHAR2(" + string_value_COLUMN_LENGTH + ")," +
               "NUMBER_VALUE NUMBER," +
               "TYPE_KEY INTEGER )");
         DatabaseHelper.executeDdlStatement(item,
               "DROP INDEX data_values_index2");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX data_values_index2 on data_value(processinstance)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX data_values_index3 on data_value(type_key)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX data_values_index4 on data_value(number_value)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX data_values_index5 on data_value(string_value)");

         trace.info("Creating columns for table 'user_property'");
         DatabaseHelper.executeDdlStatement(
               item, "ALTER TABLE USER_PROPERTY ADD (" +
               "TYPE_KEY INTEGER," +
               "NUMBER_VALUE NUMBER," +
               "STRING_VALUE VARCHAR2(" + string_value_COLUMN_LENGTH + "))");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX user_property_idx2 on user_property(type_key)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX user_property_idx3 on user_property(number_value)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX user_property_idx4 on user_property(string_value)");

         trace.info("Creating columns for table 'domain_property'");
         DatabaseHelper.executeDdlStatement(
               item, "ALTER TABLE DOMAIN_PROPERTY ADD (" +
               "TYPE_KEY INTEGER," +
               "NUMBER_VALUE NUMBER," +
               "STRING_VALUE VARCHAR2(" + string_value_COLUMN_LENGTH + "))");

         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX dom_property_idx2 on domain_property(type_key)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX dom_property_idx3 on domain_property(number_value)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX dom_property_idx4 on domain_property(string_value)");

         trace.info("Updating indexes.");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX act_PK_UNIQUE ON activity (ID)");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX activity_instances_index1 RENAME TO activity_inst_idx1");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX activity_instances_index2 RENAME TO activity_inst_idx2");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX activity_instances_index3 RENAME TO activity_inst_idx3");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX activity_instances_index4 RENAME TO activity_inst_idx4");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX activity_inst_idx5 ON activity_instance (processinstance)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX act_inst_log_idx1" +
               " ON activity_instance_log (activityinstance)");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX leaf_activities_index RENAME TO leaf_actv_idx");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX proc_def_PK_UNIQUE ON process_definition (ID)");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX process_instances_index1 RENAME TO proc_inst_idx1");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX proc_inst_idx2" +
               " ON process_instance (state, terminationtime, processdefinition)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX proc_inst_idx3" +
               " ON process_instance (state, starttime, terminationtime)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX proc_inst_idx4" +
               " ON process_instance (startingactivityinstance)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX str_dt_i1 ON string_data (objectid, data_type)");
         DatabaseHelper.executeDdlStatement(item,
               "CREATE INDEX trans_PK_UNIQUE ON transition (ID)");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX transition_instances_index1 RENAME TO trans_inst_idx1");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX user_organisation_index1 RENAME TO user_org_idx1");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX user_organisation_index2 RENAME TO user_org_idx2");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX user_property_index RENAME TO user_property_idx");
         DatabaseHelper.executeDdlStatement(item,
               "ALTER INDEX workflowuser_index1 RENAME TO workflowuser_idx1");
      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }
   }

   /**
    *
    */
   protected void migrateData(boolean recover) throws UpgradeException
   {
      /**
       * 1) move model
       * 2) move data (DataValue Table)
       * finally) delete columns
       */
      System.out.println("Migrating data now (2.0.2 upgrade)...");

      try
      {
         System.out.println("Migrating RAW columns...");

         migrateSpecialUserProperties();

         migrateRawColumns("data_value", "value");
         migrateRawColumns("user_property", "value");
         migrateRawColumns("domain_property", "value");

         System.out.println("Migrating CLOB columns...");

         migrateClobColumns("model", "modeloid", "xmlstring");
         migrateClobColumns("document", "oid", "content");

         System.out.println("Dropping superfluous table columns");

         dropSuperfluousColumns();

      }
      catch (SQLException se)
      {
         trace.error("", se);

         throw new UpgradeException(
               "Database access failed : " + se.getMessage());
      }
      catch (IOException ie)
      {
         trace.error("", ie);

         throw new UpgradeException(
               "Unexpected io error occurred while upgrading : "
               + ie.getMessage());
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   protected void upgradeModel(boolean recover) throws UpgradeException
   {
   }

   /**
    *
    */
   private void dropSuperfluousColumns() throws SQLException
   {
      try
      {
         trace.info("Dropping column 'value' of table 'data_value'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table data_value drop (value)");

         trace.info("Dropping column 'value' of table 'user_property'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table user_property drop (value)");

         trace.info("Dropping column 'value' of table 'domain_property'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table domain_property drop (value)");

         trace.info("Dropping column 'xmlstring' of table 'model'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table model drop (xmlstring)");

         trace.info("Dropping column 'content' of table 'document'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table document drop (content)");
      }
      catch (SQLException se)
      {
         if (se.getErrorCode() != DatabaseHelper.ORACLE_ERROR_INVALID_COLUMN)
         {
            throw se;
         }
      }
   }

   /**
    *
    */
   private void addWorkflowUserColumns()
   {
      try
      {
         trace.info(
               "Adding column 'failedlogincount' of table 'workflowuser'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table workflowuser add (failedlogincount number)");

         trace.info(
               "Adding column 'lastlogintime' of table 'workflowuser'");
         DatabaseHelper.executeDdlStatement(
               item, "alter table workflowuser add (lastlogintime number)");
      }
      catch (SQLException se)
      {
         if (se.getErrorCode() != DatabaseHelper.ORACLE_ERROR_COLUMN_EXISTS)
         {
            trace.warn("", se);
            throw new UpgradeException(se.getMessage());
         }
      }
   }

   /**
    *
    */
   private void migrateClobColumns(
         String tableName, String pkColumnName, String clobColumnName)
   {
      ResultSet resultSet = null;

      trace.info(
            "Migrating Clob column '" + clobColumnName + "' of table '" + tableName + "'");

      Connection connection = item.getConnection();
      try
      {
         resultSet = DatabaseHelper.executeQuery(
               item,
               "select " + pkColumnName + ", " + clobColumnName + " from " + tableName);

         while (resultSet.next())
         {
            long _oid = resultSet.getLong(pkColumnName);
            Clob _clob = resultSet.getClob(clobColumnName);

            InputStream _is = _clob.getAsciiStream();
            byte[] _buffer = new byte[(int) _clob.length()];

            _is.read(_buffer);

            String value = new String(_buffer);

            PreparedStatement lshStmt = connection.prepareStatement(
                  "insert into STRING_DATA VALUES (string_data_seq.nextval, ?, ?, ?)");
            writeToStringDataTable(lshStmt, _oid, tableName, value);

            lshStmt.executeBatch();
            connection.commit();
         }
      }
      catch (Throwable x)
      {
         trace.warn("", x);
         try
         {
            connection.rollback();
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new UpgradeException(e.getMessage());
         }
         throw new UpgradeException(x.getMessage());
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   /**
    *
    */
   private void migrateRawColumns(String tableName, String rawColumnName)
         throws SQLException
   {
      int batchSize = Parameters.instance().getInteger(
            BATCH_SIZE_PROPERTY, DEFAULT_BATCH_SIZE);
      trace.info("Using " + BATCH_SIZE_PROPERTY + " value of " + batchSize);

      ResultSet resultSet = null;

      trace.info(
            "Migrating RAW column '" + rawColumnName + "' of table '" + tableName + "'");

      Connection connection = item.getConnection();

      try
      {
         PreparedStatement readStatement = connection.prepareStatement(
               "select oid, " + rawColumnName + " from " + tableName + " " +
               "where type_key is null and rownum <= " + batchSize);

         PreparedStatement statement = connection.prepareStatement(
               "update " + tableName +
               " set type_key=?, number_value=?, string_value=? where oid = ?");
         PreparedStatement lshStatement = connection.prepareStatement(
               "insert into STRING_DATA VALUES (string_data_seq.nextval, ?, ?, ?)");

         int batchCounter = 0;
         do
         {
            resultSet = readStatement.executeQuery();
            batchCounter = 0;
            while (resultSet.next())
            {
               batchCounter++;

               long oid = resultSet.getLong("oid");

               writeBigData(statement, lshStatement, tableName, oid,
                     resultSet.getBytes(rawColumnName));

            }

            statement.executeBatch();
            lshStatement.executeBatch();
            connection.commit();
            trace.info("Committed " + batchCounter + " migrated records in table '" +
                  tableName + "'.");
         }
         while (batchCounter > 0);

         trace.info("Migration of table '" + tableName + "' done.");
      }
      catch (Throwable x)
      {
         trace.warn("", x);
         connection.rollback();
         throw new UpgradeException(x.getMessage());
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   public void writeBigData(PreparedStatement statement, PreparedStatement lshStmt,
         String tableName, long oid, byte[] object)
         throws SQLException
   {
      statement.setLong(4, oid);

      if (object == null || object.length == 0)
      {
         statement.setInt(1, BigData.NULL);
         statement.setNull(2, Types.INTEGER);
         statement.setNull(3, Types.VARCHAR);
         statement.addBatch();
         return;
      }

      Serializable value = null;
      try
      {
         value = Serialization.deserializeObject(object);
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }

      if (value == null)
      {
         // deserialized null

         statement.setInt(1, BigData.NULL);
         statement.setNull(2, Types.INTEGER);
         statement.setNull(3, Types.VARCHAR);
         statement.addBatch();
         return;
      }

      Class type = value.getClass();
      if (type == Short.class)
      {
         statement.setInt(1, BigData.SHORT);
         statement.setLong(2, ((Short) value).longValue());
         statement.setNull(3, Types.VARCHAR);
      }
      else if (type == Integer.class)
      {
         statement.setInt(1, BigData.INTEGER);
         statement.setLong(2, ((Integer) value).longValue());
         statement.setNull(3, Types.VARCHAR);
      }
      else if (type == Long.class)
      {
         statement.setInt(1, BigData.LONG);
         statement.setLong(2, ((Long) value).longValue());
         statement.setNull(3, Types.VARCHAR);
      }
      else if (type == Byte.class)
      {
         statement.setInt(1, BigData.BYTE);
         statement.setLong(2, ((Byte) value).longValue());
         statement.setNull(3, Types.VARCHAR);
      }
      else if (type == Boolean.class)
      {
         statement.setInt(1, BigData.BOOLEAN);
         statement.setLong(2, ((Boolean) value).booleanValue() ? 1 : 0);
         statement.setNull(3, Types.VARCHAR);
      }
      else if (type == Date.class)
      {
         statement.setInt(1, BigData.DATE);
         statement.setLong(2, ((Date) value).getTime());
         statement.setNull(3, Types.VARCHAR);
      }
      else if (type == Float.class)
      {
         statement.setInt(1, BigData.FLOAT);
         statement.setNull(2, Types.INTEGER);
         statement.setString(3, value.toString());
      }
      else if (type == Double.class)
      {
         statement.setInt(1, BigData.DOUBLE);
         statement.setNull(2, Types.INTEGER);
         statement.setString(3, value.toString());
      }
      else if (type == Character.class)
      {
         statement.setInt(1, BigData.CHAR);
         statement.setNull(2, Types.INTEGER);
         statement.setString(3, value.toString());
      }
      else if (type == Money.class)
      {
         // @todo 2.0.2: check toString implementation of Money
         statement.setInt(1, BigData.MONEY);
         statement.setNull(2, Types.INTEGER);
         statement.setString(3, value.toString());
      }
      else if (type == String.class)
      {
         writeStringValue(statement, lshStmt, oid, tableName,
               (String) value, BigData.STRING, BigData.BIG_STRING);
      }
      else
      {
         String stringifiedValue = null;
         try
         {
            stringifiedValue = new String(Base64.encode(
                  Serialization.serializeObject(value)));
         }
         catch (IOException e)
         {
            throw new InternalException("Cannot serialize value for data.");
         }
         writeStringValue(statement, lshStmt, oid, tableName,
               stringifiedValue, BigData.SERIALIZABLE, BigData.BIG_SERIALIZABLE);
      }
      statement.addBatch();
   }

   private void writeStringValue(PreparedStatement stmt, PreparedStatement lshStmt,
         long oid, String tableName,
         String value, int smalltype, int bigtype) throws SQLException
   {
      if (value.length() <= string_value_COLUMN_LENGTH)
      {
         stmt.setInt(1, smalltype);
         stmt.setNull(2, Types.INTEGER);
         stmt.setString(3, value);
      }
      else
      {
         stmt.setInt(1, bigtype);
         stmt.setNull(2, Types.INTEGER);
         stmt.setString(3, value.substring(0, string_value_COLUMN_LENGTH));

         // write slices of big data

         writeToStringDataTable(lshStmt, oid, tableName, value);

      }
   }


   /**
    *
    */
   private void migrateSpecialUserProperties()
         throws SQLException, IOException
   {
      ResultSet resultSet = null;
      long _oid = 0;

      try
      {
         trace.info(
               "Migrating special user properties (RAW) column of table 'workflowuser'");

         resultSet = DatabaseHelper.executeQuery(
               item, "select oid, name, value from user_property");

         while (resultSet.next())
         {
            String _propertyName = resultSet.getString(2);
            try
            {
               if ("FailedLoginRetriesCount".equals(_propertyName))
               {
                  Integer i = (Integer) objectFromByteArray(
                        resultSet.getBytes(3));

                  _oid = resultSet.getLong(1);

                  DatabaseHelper.executeUpdate(
                        item,
                        "update workflowuser set failedlogincount = "
                        + i.intValue() + " where oid = " + _oid);

                  DatabaseHelper.executeUpdate(
                        item,
                        "delete from user_property where oid = "
                        + _oid + " and name = '" + _propertyName + "'");
               }
            }
            catch (ClassCastException cce)
            {
            }
            catch (ClassNotFoundException cnfe)
            {
            }

            try
            {
               if ("LastLoginTimestamp".equals(_propertyName))
               {
                  Long l = (Long) objectFromByteArray(resultSet.getBytes(3));

                  _oid = resultSet.getLong(1);

                  DatabaseHelper.executeUpdate(
                        item,
                        "update workflowuser set lastlogintime = "
                        + l.longValue() + " where oid = " + _oid);

                  DatabaseHelper.executeUpdate(
                        item,
                        "delete from user_property where oid = "
                        + _oid + " and name = '" + _propertyName + "'");
               }
            }
            catch (ClassCastException cce)
            {
            }
            catch (ClassNotFoundException cnfe)
            {
            }
         }
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   /**
    * Gets the corresponding object from a byte array.
    * @param array the array of bytes
    * @return the corresponding object
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private Object objectFromByteArray(byte[] array)
         throws IOException, ClassNotFoundException
   {
      ByteArrayInputStream _istream = new ByteArrayInputStream(array);
      ObjectInputStream _p = new ObjectInputStream(_istream);

      Object _object = _p.readObject();

      _istream.close();

      return _object;
   }

   /**
    * Match only if we have a runtime < 2.0.0
    * @param version
    * @return
    */
   public boolean matches(Version version)
   {
      return version.compareTo(new Version(2, 0, 0)) < 0;
   }
}
