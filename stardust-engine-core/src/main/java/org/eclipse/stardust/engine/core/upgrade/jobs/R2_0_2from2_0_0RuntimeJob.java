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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Key;
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
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class R2_0_2from2_0_0RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager
         .getLogger(R2_0_2from2_0_0RuntimeJob.class);

   public static final int string_value_COLUMN_LENGTH = 128;

   private int ATOM_SIZE = 1000;
   private HashMap dataMap = new HashMap();

   private final String BATCH_SIZE_PROPERTY = "ag.carnot.upgrade.batch_size";
   private final int DEFAULT_BATCH_SIZE = 100;

   /**
    *
    */
   public Version getVersion()
   {
      return Version.createFixedVersion(2, 0, 2);
   }

   /**
    *
    */
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
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

      bootstrapModel();

      try
      {
         System.out.println("Migrating large data...");

         migrateRawColumns("data_value");
         migrateRawColumns("user_property");
         migrateRawColumns("domain_property");
      }
      catch (SQLException se)
      {
         trace.error("", se);

         throw new UpgradeException(
               "Database access failed : " + se.getMessage());
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   protected void upgradeModel(boolean recover) throws UpgradeException
   {

   }

   private void bootstrapModel()
   {
      Connection connection = item.getConnection();
      ResultSet resultSet = null;
      try
      {
         Statement statement = connection.createStatement();
         resultSet = statement.executeQuery(
               "select oid, objectid, data from string_data " +
               "where data_type='model' order by objectid, oid");

         long previousOid = 0;
         long oid = 0;
         StringBuffer dataBuffer = new StringBuffer();
         while (resultSet.next())
         {
            oid = resultSet.getLong(2);
            if (previousOid == 0)
            {
               previousOid = oid;
            }
            if (oid != previousOid)
            {
               inspectModel(dataBuffer.toString());
               previousOid = oid;
               dataBuffer = new StringBuffer();
            }
            dataBuffer.append(resultSet.getString(3));
         }
         if (oid != 0)
         {
            inspectModel(dataBuffer.toString());
         }

      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   private void inspectModel(String model)
   {
      try
      {
         final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

         builderFactory.setValidating(false);

         DocumentBuilder builder = builderFactory.newDocumentBuilder();
         builder.setEntityResolver(new MyEntityResolver(new DefaultHandler()));
         builder.setErrorHandler(new DefaultHandler());

         InputSource source = new InputSource(new StringReader(model));
         source.setSystemId("poodle");
         Document document = builder.parse(source);
         NodeList dataList = document.getElementsByTagName("DATA");
         for (int i = 0; i < dataList.getLength(); i++)
         {
            try
            {
               Element dataNode = (Element) dataList.item(i);
               String oid = dataNode.getAttribute("oid");
               TypeKey type = new TypeKey(dataNode.getAttribute("type"));
               dataMap.put(oid, type);
            }
            catch (Exception e)
            {
            }
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }

   }

   /**
    *
    */
   private void migrateRawColumns(String tableName)
         throws SQLException
   {
      int batchSize = Parameters.instance().getInteger(
            BATCH_SIZE_PROPERTY, DEFAULT_BATCH_SIZE);
      trace.info("Using " + BATCH_SIZE_PROPERTY + " value of " + batchSize);

      ResultSet resultSet = null;

      trace.info("Migrating table '" + tableName + "'");

      Connection connection = item.getConnection();

      try
      {
         PreparedStatement updateStatement = connection.prepareStatement(
               "update " + tableName +
               " set type_key=?, number_value=?, string_value=? where oid = ?");

         PreparedStatement lshDeleteStatement = connection.prepareStatement(
               "delete from string_data " +
               "where objectid=? and data_type='" + tableName + "'");

         if (tableName.equals("data_value"))
         {
            PreparedStatement statement = connection.prepareStatement(
                  "SELECT data_value.oid, data_value.data " +
                  "FROM data_value, string_data " +
                  "WHERE data_value.oid = string_data.objectid(+) " +
                  "   AND string_data.data_type IS NULL " +
                  "   AND type_key IS NULL " +
                  "   AND rownum <= " + batchSize);

            ResultSet result = null;
            try
            {
               int batchCounter;
               do
               {
                  batchCounter = 0;
                  result = statement.executeQuery();
                  while (result.next())
                  {
                     writeSmallData(updateStatement, lshDeleteStatement, "data_value",
                           result.getLong(1), null, result.getString(2));
                     batchCounter++;
                  }
                  updateStatement.executeBatch();
                  connection.commit();
                  trace.info("Committed " + batchCounter + " migrated records " +
                        "in table 'data_value'.");
               }
               while (batchCounter != 0);
            }
            finally
            {
               result.close();
            }
         }

         PreparedStatement lshReadStatement = null;
         if (tableName.equals("data_value"))
         {
            lshReadStatement = connection.prepareStatement(
                  "select string_data.oid, data_value.oid, string_data.data, data_value.data " +
                  "from string_data, data_value " +
                  "where data_type='data_value' " +
                  "and string_data.objectid = data_value.oid " +
                  "and string_data.objectid <=? " +
                  "order by data_value.oid, string_data.oid");
         }
         else
         {
            lshReadStatement = connection.prepareStatement(
                  "select oid, objectid, data from string_data " +
                  "where data_type = '" + tableName + "' " +
                  "and string_data.objectid <=? " +
                  "order by objectid, oid");
         }

         Statement maxOidStatement = connection.createStatement();
         ResultSet maxOidResult = maxOidStatement.executeQuery(
               "select max (objectid) from string_data " +
               "where data_type='" + tableName + "'");

         maxOidResult.next();
         long maxOid = maxOidResult.getLong(1);

         long counter = 0;
         while (counter * batchSize <= maxOid)
         {
            counter++;
            lshReadStatement.setLong(1, counter * batchSize);

            resultSet = lshReadStatement.executeQuery();

            int batchCounter = 0;
            long previousOid = 0;
            long oid = 0;
            StringBuffer dataBuffer = new StringBuffer();
            String dataOid = null;
            while (resultSet.next())
            {
               oid = resultSet.getLong(2);
               if (previousOid == 0)
               {
                  previousOid = oid;
                  if (tableName.equals("data_value"))
                  {
                     dataOid = resultSet.getString(4);
                  }
               }
               if (oid != previousOid)
               {
                  writeSmallData(updateStatement, lshDeleteStatement, tableName,
                        previousOid, dataBuffer, dataOid);

                  previousOid = oid;
                  if (tableName.equals("data_value"))
                  {
                     dataOid = resultSet.getString(4);
                  }
                  dataBuffer = new StringBuffer();
                  batchCounter++;
               }
               dataBuffer.append(resultSet.getString(3));

            }
            if (oid != 0)
            {
               writeSmallData(updateStatement, lshDeleteStatement, tableName,
                     oid, dataBuffer, dataOid);
               batchCounter++;
            }
            if (batchCounter != 0)
            {
               lshDeleteStatement.executeBatch();
               updateStatement.executeBatch();
               connection.commit();
               trace.info("Committed " + batchCounter + " migrated records with large " +
                     "data in table '" + tableName + "'.");
            }
         }
         trace.info("Migration of table '" + tableName + "'done.");
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

   public Object newValueInstance(int type)
   {
      switch (type)
      {
         case TypeKey.BOOLEAN:
            {
               return new Boolean(false);
            }
         case TypeKey.CHAR:
            {
               return new Character((char) 0);
            }
         case TypeKey.BYTE:
            {
               return new Byte((byte) 0);
            }
         case TypeKey.SHORT:
            {
               return new Short((short) 0);
            }
         case TypeKey.INTEGER:
            {
               return new Integer(0);
            }
         case TypeKey.LONG:
            {
               return new Long(0);
            }
         case TypeKey.FLOAT:
            {
               return new Float(0.0);
            }
         case TypeKey.DOUBLE:
            {
               return new Double(0.0);
            }
         case TypeKey.STRING:
            {
               return new String();
            }
         case TypeKey.CALENDAR:
            {
               return java.util.Calendar.getInstance();
            }
         case TypeKey.MONEY:
            {
               return new org.eclipse.stardust.common.Money();
            }
         default:
            {
               return null;
            }
      }
   }

   /**
    *
    * @param dataOid This is a secret parameter. If not null it is used for type lookup
    * to replace null values in 2.0.0 by 'default values' in 2.0.2
    */
   public void writeSmallData(PreparedStatement statement,
         PreparedStatement lshDeleteStatement, String tableName,
         long oid, StringBuffer rawData, String dataOid)
         throws SQLException
   {
      byte[] object = null;
      if (rawData != null)
      {
         object = Base64.decode(rawData.toString().getBytes());
      }

      statement.setLong(4, oid);

      Object value = null;
      if (object == null || object.length == 0)
      {
         value = null;
      }
      else
      {
         try
         {
            value = Serialization.deserializeObject(object);
         }
         catch (Exception e)
         {
            trace.warn("", e);
            throw new UpgradeException(e.getMessage());
         }
      }

      if (dataOid != null && value == null)
      {
         TypeKey type = (TypeKey) dataMap.get(dataOid);
         value = newValueInstance(type.getValue());
      }

      if (value == null)
      {
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
         writeStringValue(statement, (String) value, BigData.STRING, BigData.BIG_STRING);
      }
      else
      {
         String stringifiedValue = null;
         try
         {
            stringifiedValue = new String(Base64.encode(
                  Serialization.serializeObject((Serializable) value)));
         }
         catch (IOException e)
         {
            throw new InternalException("Cannot serialize value for data.");
         }
         writeStringValue(statement, stringifiedValue,
               BigData.SERIALIZABLE, BigData.BIG_SERIALIZABLE);
      }
      statement.addBatch();

      if (object != null && object.length <= string_value_COLUMN_LENGTH)
      {
         lshDeleteStatement.setLong(1, oid);
         lshDeleteStatement.addBatch();
      }
   }

   private void writeStringValue(PreparedStatement stmt,
         String value, int smalltype, int bigtype)
         throws SQLException
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
      }
   }

   public static void closeStatement(Statement statement)
   {
      if (statement != null)
      {
         try
         {
            statement.close();
         }
         catch (SQLException e)
         {
            trace.warn("Error closing JDBC statement: " + e.getMessage());
         }
      }
   }

   /**
    *  A wrapper around the EntityResolver of the parser to find the
    * <code>WorkflowModel.dtd</code> by means of the carnot parameter
    * <code>Model.Dtd</code>.
    */
   static class MyEntityResolver implements EntityResolver
   {
      private EntityResolver parent;

      MyEntityResolver(EntityResolver parent_)
      {
         parent = parent_;
      }

      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
      {
         trace.debug("publicId: " + publicId);
         trace.debug("systemId: " + systemId);
         if (!systemId.endsWith("WorkflowModel.dtd"))
         {
            return parent.resolveEntity(publicId, systemId);
         }
         String dtdLocation = Parameters.instance().getString(
               "Model.Dtd",
               "." + File.separator + "WorkflowModel.dtd");
         trace.debug("dtd location: " + dtdLocation);
         return new InputSource(new FileInputStream(dtdLocation));
      }
   }

   private static class TypeKey extends Key
   {
      public static final int BOOLEAN = 0;
      public static final int CHAR = 1;
      public static final int BYTE = 2;
      public static final int SHORT = 3;
      public static final int INTEGER = 4;
      public static final int LONG = 5;
      public static final int FLOAT = 6;
      public static final int DOUBLE = 7;
      public static final int STRING = 8;
      public static final int CALENDAR = 9;
      public static final int MONEY = 10;
      public static final int SERIALIZABLE = 11;
      public static final int TIMESTAMP = 12;

      static String[] keyList = {"boolean"
                                 , "char"
                                 , "byte"
                                 , "short"
                                 , "int"
                                 , "long"
                                 , "float"
                                 , "double"
                                 , "String"
                                 , "Calendar"
                                 , "Money"
                                 , "Serializable"
                                 , "Timestamp"
      };

      public TypeKey(int value)
      {
         super(value);
      }

      public TypeKey(String keyRepresentation)
      {
         if (keyRepresentation.equalsIgnoreCase("Date"))
         {
            value = CALENDAR;
         }
         else
         {
            value = getValue(keyRepresentation, getKeyList());
         }
      }

      public static String[] getKeyList()
      {
         return keyList;
      }

      public String getString()
      {
         if (value < 0)
         {
            return UNKNOWN_STRING;
         }

         return keyList[value];
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
}
