/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.runtime.audittrail.management.AuditTrailManagementUtils.deleteAllContentFromPartition;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils.InitOptions;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.*;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataClusterSynchronizationInfo;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.IClusterChangeObserver;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper.AlterMode;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper.ColumnNameModificationMode;

/**
 * SchemaHelper will be used to generate DDL and copy to a specified file.
 * The DDL operations can be executed on any supported DBMS for Carnot schema
 * creation.
 *
 * @author jmahmood
 * @version $Revision$
 */
public class SchemaHelper
{
   private static final Logger trace = LogManager.getLogger(SchemaHelper.class);

   public static final String DEFAULT_STATEMENT_DELIMITER = null;

   protected static final String SELECT = "SELECT ";
   protected static final String FROM = " FROM ";
   protected static final String COMMA = ",";

   public static final Collection getPersistentClasses(DBDescriptor schemaManager)
   {
      LinkedList result = new LinkedList();
      for (int n = 0; n < Constants.PERSISTENT_RUNTIME_CLASSES.length; ++n)
      {
         result.add(Constants.PERSISTENT_RUNTIME_CLASSES[n]);
      }

      for (int n = 0; n < Constants.PERSISTENT_MODELING_CLASSES.length; ++n)
      {
         result.add(Constants.PERSISTENT_MODELING_CLASSES[n]);
      }

      for (Iterator i = schemaManager.getPersistentTypes(); i.hasNext();)
      {
         result.add(i.next());

      }
      return result;
   }

   public static final void generateCreateSchemaDDL(String fileName, String schemaName,
         String statementDelimiter)
   {
      File file = new File(fileName);

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL, null);

      DBDescriptor dbDescriptor = session.getDBDescriptor();
      DDLManager ddlManager = new DDLManager(dbDescriptor);

      ddlManager.dumpCreateSchemaDDLToFile(file, session, schemaName,
            getPersistentClasses(dbDescriptor), statementDelimiter);
   }

   public static final void generateCreateArchiveSchemaDDL(String fileName,
         String schemaName)
   {
      File file = new File(fileName);

      final DBDescriptor schemaManager = DBDescriptor.create(SessionFactory.AUDIT_TRAIL);
      DDLManager ddlManager = new DDLManager(schemaManager);

      List persistentClasses = new LinkedList();
      for (int n = 0; n < Constants.PERSISTENT_RUNTIME_CLASSES.length; ++n)
      {
         persistentClasses.add(Constants.PERSISTENT_RUNTIME_CLASSES[n]);
      }

      for (int n = 0; n < Constants.PERSISTENT_MODELING_CLASSES.length; ++n)
      {
         persistentClasses.add(Constants.PERSISTENT_MODELING_CLASSES[n]);
      }

      ddlManager.dumpCreateArchiveSchemaDDLToFile(file, schemaName, persistentClasses);
   }

   /**
    *
    */
   public static final void generateDropSchemaDDL(String fileName, String schemaName,
         String statementDelimiter)
   {
      File file = new File(fileName);

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL, null);

      DBDescriptor dbDescriptor = session.getDBDescriptor();
      DDLManager ddlManager = new DDLManager(dbDescriptor);

      ddlManager.dumpDropSchemaDDLToFile(file, session, schemaName,
            getPersistentClasses(dbDescriptor), statementDelimiter);
   }

   /**
   *
   */
   public static final void createSchema() throws SQLException
   {
      createSchema(SessionFactory.createSession(SessionFactory.AUDIT_TRAIL),
            DEFAULT_STATEMENT_DELIMITER);
   }

   public static final void runSql(String fileName, boolean verbose) throws SQLException, IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Connection conn = null;
      try
      {
         conn = session.getConnection();
         Statement stmt = conn.createStatement();
         String command = null;
         while ((command = reader.readLine()) != null)
         {
            command = command.trim();
            if (command.length() > 0)
            {
               if (verbose)
               {
                  System.out.println(command);
               }
               if (command.endsWith(";"))
               {
                  command = command.substring(0, command.length() - 1);
               }
               if ("COMMIT".equalsIgnoreCase(command))
               {
                  conn.commit();
               }
               else
               {

                  boolean isResultSet = stmt.execute(command);
                  if (verbose)
                  {
                     while (true)
                     {
                        if (isResultSet)
                        {
                           // show result set
                           ResultSet rs = stmt.getResultSet();
                           ResultSetMetaData md = rs.getMetaData();
                           int cc = md.getColumnCount();
                           for (int i = 1; i <= cc; i++)
                           {
                              if (i > 1)
                              {
                                 System.out.print(" | ");
                              }
                              System.out.print(md.getColumnName(i));
                           }
                           System.out.println();
                           System.out.println("=====================================");
                           while (rs.next())
                           {
                              for (int i = 1; i <= cc; i++)
                              {
                                 if (i > 1)
                                 {
                                    System.out.print(" | ");
                                 }
                                 System.out.print(rs.getObject(i));
                              }
                              System.out.println();
                           }
                           System.out.println("-------------------------------------");
                        }
                        else
                        {
                           // show update count.
                           int count = stmt.getUpdateCount();
                           if (count == -1)
                           {
                              break;
                           }
                           if (count > 0)
                           {
                              System.out.println(count + " rows updated.");
                           }
                        }
                        isResultSet = stmt.getMoreResults();
                     }
                  }
               }
            }
         }
         //conn.commit();
      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
         reader.close();
      }
   }

   /**
   *
   */
   public static final void createSchema(String statementDelimiter) throws SQLException
   {
      createSchema(SessionFactory.createSession(SessionFactory.AUDIT_TRAIL),
            statementDelimiter);
   }

   public static final void createSchema(Session session) throws SQLException
   {
      createSchema(session, DEFAULT_STATEMENT_DELIMITER);
   }
   /**
    *
    */
   public static final void createSchema(Session session, String statementDelimiter)
         throws SQLException
   {
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         Collection classes = getPersistentClasses(dbDescriptor);

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         ddlManager.createGlobalSequenceIfNecessary(schemaName, session.getConnection());
         ddlManager.createSequenceStoredProcedureIfNecessary(schemaName, session.getConnection());

         for (Iterator i = classes.iterator(); i.hasNext();)
         {
            Class clazz = (Class) i.next();
            ddlManager.createTableForClass(schemaName, clazz, session.getConnection());

            if (session.isUsingLockTables()
                  && TypeDescriptor.get(clazz).isDistinctLockTableName())
            {
               ddlManager.createLockTableForClass(schemaName, clazz,
                     session.getConnection(), null, statementDelimiter);
            }
         }

         new PropertyPersistor(Constants.SYSOP_PASSWORD, Constants.DEFAULT_PASSWORD);
         new PropertyPersistor(Constants.CARNOT_VERSION, CurrentVersion.getVersionName());
         new PropertyPersistor(Constants.PRODUCT_NAME, CurrentVersion.PRODUCT_NAME);

         AuditTrailPartitionBean defaultPartitionBean = new AuditTrailPartitionBean(PredefinedConstants.DEFAULT_PARTITION_ID);
         Parameters.instance().set(SecurityProperties.CURRENT_PARTITION, defaultPartitionBean);

         new UserDomainBean(defaultPartitionBean.getId(), defaultPartitionBean, null);

         UserRealmBean carnotRealm = new UserRealmBean(
               PredefinedConstants.DEFAULT_REALM_ID,
               PredefinedConstants.DEFAULT_REALM_NAME, defaultPartitionBean);

         IUser motu = new UserBean(PredefinedConstants.MOTU,
               PredefinedConstants.MOTU_FIRST_NAME, PredefinedConstants.MOTU_LAST_NAME,
               carnotRealm);
         motu.setPassword(motu.getId());

         for (PredefinedProcessInstanceLinkTypes type : PredefinedProcessInstanceLinkTypes.values())
         {
            new ProcessInstanceLinkTypeBean(type.getId(), type.getDescription());
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   /**
    * Validates the existence of the CARNOT base properties in the current
    * schema. If one of them is missing it gets created with a default value.
    *
    * @see org.eclipse.stardust.engine.core.runtime.beans.Constants#CARNOT_VERSION
    * @see org.eclipse.stardust.engine.core.runtime.beans.Constants#SYSOP_PASSWORD
    */
   public static final void validateBaseProperties()
   {
      validateBaseProperty(Constants.SYSOP_PASSWORD, Constants.DEFAULT_PASSWORD);
      validateBaseProperty(Constants.CARNOT_VERSION, CurrentVersion.getVersionName());
      validateBaseProperty(Constants.PRODUCT_NAME, CurrentVersion.PRODUCT_NAME);
   }

   public static final void dropSchema(String sysconPassword)
   {
      dropSchema(SessionFactory.createSession(SessionFactory.AUDIT_TRAIL),
            sysconPassword, null);
   }

   public static final void dropSchema(String sysconPassword, String statementDelimiter)
   {
      dropSchema(SessionFactory.createSession(SessionFactory.AUDIT_TRAIL),
            sysconPassword, statementDelimiter);
   }

   public static final void dropSchema(Session session, String sysconPassword)
   {
      dropSchema(session, sysconPassword, DEFAULT_STATEMENT_DELIMITER);
   }

   public static final void dropSchema(Session session, String sysconPassword,
         String statementDelimiter)
   {
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         try
         {
            final String schemaName = Parameters.instance().getString(
                  SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
                  Parameters.instance().getString(
                        SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

            DBDescriptor dbDescriptor = session.getDBDescriptor();
            Collection classes = getPersistentClasses(dbDescriptor);

            DDLManager ddlManager = new DDLManager(dbDescriptor);
            Connection connection = session.getConnection();

            ddlManager.dropGlobalSequenceIfAny(schemaName, connection);
            ddlManager.dropSequenceStoredProcedureIfAny(schemaName, connection);

            String tableDecorator = "";
            for (Iterator i = classes.iterator(); i.hasNext();)
            {
               Class clazz = (Class) i.next();

               try
               {
                  tableDecorator = "";
                  ddlManager.dropTableForClass(schemaName, clazz, connection);

                  if (TypeDescriptor.get(clazz).isDistinctLockTableName())
                  {
                     tableDecorator = "lock ";

                     if (session.isUsingLockTables())
                     {
                        ddlManager.dropLockTableForClass(schemaName, clazz, connection, null, statementDelimiter);
                     }
                     else
                     {
                        Statement stmt = null;
                        try
                        {
                           stmt = connection.createStatement();
                           String possibleLockTable = DDLManager.getQualifiedName(schemaName, TypeDescriptor
                                 .getLockTableName(clazz));
                           if (ddlManager.containsTable(schemaName, possibleLockTable, connection))
                           {
                              String message = "Lock table "
                                    + possibleLockTable
                                    + " for class '" + clazz
                                    + "' exists but it should not.";
                              trace.info(message);
                           }
                           else
                           {
                              String doesnotExistMessage = new StringBuffer(50).append(
                                    "Lock table ").append(possibleLockTable)
                                    .append(" for class '").append(clazz).append(
                                          "' doesn't exist.").toString();

                              trace.info(doesnotExistMessage);
                           }
                        }
                        catch(SQLException e)
                        {
                           trace.error("SQLException encountered while trying to detect the presence of a lock table.", e);
                        }
                        finally
                        {
                           QueryUtils.closeStatement(stmt);
                        }
                     }

                  }
               }
               catch (Exception x)
               {
                  String message = "Couldn't drop " + tableDecorator + "table for class '"
                        + clazz + "'." + " Reason: " + x.getMessage();
                  System.out.println(message);
                  trace.warn(message, x);
               }
            }
            connection.commit();
         }
         catch (Exception ex)
         {
            session.rollback();
            throw new InternalException(ex);
         }
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void verifySysopPassword(Session session, String sysconPassword)
         throws PublicException
   {
      DBDescriptor dbDescriptor = session.getDBDescriptor();
      DDLManager ddlManager = new DDLManager(dbDescriptor);

      try
      {
         if (ddlManager.containsTable(session.getSchemaName(),
               PropertyPersistor.TABLE_NAME, session.getConnection()))
         {
            Version auditTrailVersion = null;

            QueryDescriptor queryVersion = QueryDescriptor //
                  .from(session.getSchemaName(), PropertyPersistor.class) //
                  .select(PropertyPersistor.FIELD__VALUE) //
                  .where(Predicates //
                        .isEqual(PropertyPersistor.FR__NAME, Constants.CARNOT_VERSION));
            ResultSet rsCarnotVersion = session.executeQuery(queryVersion);
            try
            {
               if (rsCarnotVersion.next())
               {
                  auditTrailVersion = new Version(rsCarnotVersion.getString(1));
               }
            }
            finally
            {
               QueryUtils.closeResultSet(rsCarnotVersion);
            }

            String sysopPassword = null;

            if ((null == auditTrailVersion)
                  || (0 >= Version.createFixedVersion(4, 0, 0).compareTo(auditTrailVersion)))
            {
               PropertyPersistor persistor = PropertyPersistor
                     .findByName(Constants.SYSOP_PASSWORD);
               sysopPassword = (null != persistor) ? persistor.getValue() : null;
            }
            else
            {
               // retrieve sysop password in a 3.x compatible way
               QueryDescriptor query = QueryDescriptor //
                     .from(session.getSchemaName(), PropertyPersistor.class) //
                     .select(PropertyPersistor.FIELD__VALUE) //
                     .where(Predicates //
                           .isEqual(PropertyPersistor.FR__NAME, Constants.SYSOP_PASSWORD));

               ResultSet rsSysopPassword = session.executeQuery(query);
               try
               {
                  if (rsSysopPassword.next())
                  {
                     sysopPassword = rsSysopPassword.getString(1);
                  }
               }
               finally
               {
                  QueryUtils.closeResultSet(rsSysopPassword);
               }
            }


            if (null == sysopPassword)
            {
               boolean isSchemaEmpty = true;

               Collection classes = getPersistentClasses(dbDescriptor);
               Iterator classWalker = classes.iterator();
               while (isSchemaEmpty && classWalker.hasNext())
               {
                  isSchemaEmpty &= (0 == SessionFactory.getSession(
                        SessionFactory.AUDIT_TRAIL).getCount((Class) classWalker.next()));
               }
               if ( !isSchemaEmpty)
               {
                  throw new PublicException(
                        BpmRuntimeError.ATDB_NO_PASSWORD_SET_FOR_SYSOP_THOUGH_USER_AUTHORIZATION_IS_REQUIRED
                              .raise());
               }
               System.out.println("Allowing access to empty schema without sysop-user"
                     + " authorization.");
            }
            else if (!sysopPassword.equals(sysconPassword))
            {
               throw new PublicException(
                     BpmRuntimeError.ATDB_PLEASE_SPECIFY_APPROPRIATE_PASSWORD_FOR_SYSOP_USER
                           .raise());
            }
         }
      }
      catch (SQLException e)
      {
         throw new PublicException(
               BpmRuntimeError.ATDB_FAILED_DURING_SYSOP_USER_AUTHORIZATION.raise(), e);
      }
   }

   /**
    *
    */
   public static final void changeSysOpPassword(String oldPassword, String newPassword)
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         PropertyPersistor sysop =
               PropertyPersistor.findByName(Constants.SYSOP_PASSWORD);

         if ((sysop == null) || (newPassword == null)
               || (!sysop.getValue().equals(oldPassword)))
         {
            session.rollback();
            throw new PublicException(
                  BpmRuntimeError.ATDB_PLEASE_SPECIFY_APPROPRIATE_PASSWORD_FOR_SYSOP_USER_BEFORE_TRYING_TO_CHANGE_PASSWORD
                        .raise());
         }

         sysop.setValue(newPassword);
         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static final Map listAuditTrailProperties()
   {
      Parameters params = Parameters.instance();

      Map result = new TreeMap();

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(params, locals);

         for (Iterator i = PropertyPersistor.findAll(null); i.hasNext(); )
         {
            PropertyPersistor prop = (PropertyPersistor) i.next();
            if ( !Constants.SYSOP_PASSWORD.equals(prop.getName()))
            {
               result.put(prop.getName(), prop.getValue());
            }
         }
         session.rollback();
      }
      finally
      {
         ParametersFacade.popLayer(params);
      }

      return result;
   }

   public static final String getAuditTrailProperty(String name)
   {
      PropertyPersistor prop = null;

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         if (!Constants.SYSOP_PASSWORD.equals(name))
         {
            prop = PropertyPersistor.findByName(name);
         }
         session.rollback();
      }
      finally
      {
         ParametersFacade.popLayer();
      }

      return (null != prop) ? prop.getValue() : null;
   }

   public static final void setAuditTrailProperty(String name, String value)
   {
      if (Constants.SYSOP_PASSWORD.equals(name) || Constants.CARNOT_VERSION.equals(name)
            || Constants.PRODUCT_NAME.equals(name)
            || RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION.equals(name))
      {
         throw new PublicException(
               BpmRuntimeError.ATDB_UNABLE_TO_SET_VALUE_OF_AUDIT_TRAIL_PROPERTY
                     .raise(name));
      }

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         PropertyPersistor prop = PropertyPersistor.findByName(name);

         if (null != prop)
         {
            prop.setValue(value);
         }
         else
         {
            prop = new PropertyPersistor(name, value);
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static final String deleteAuditTrailProperty(String name)
   {
      if (Constants.SYSOP_PASSWORD.equals(name) || Constants.CARNOT_VERSION.equals(name)
            || Constants.PRODUCT_NAME.equals(name)
            || RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION.equals(name))
      {
         throw new PublicException(
               BpmRuntimeError.ATDB_UNABLE_TO_DELETE_VALUE_OF_AUDIT_TRAIL_PROPERTY
                     .raise(name));
      }

      String result = null;

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         PropertyPersistor prop = PropertyPersistor.findByName(name);
         if (null != prop)
         {
            result = prop.getValue();
            prop.delete();
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }

      return result;
   }

   /**
    * Validates the existence of the given CARNOT base property in the current
    * schema. If it is missing it gets created with a default value.
    *
    * @param name The name of the property to validate.
    * @param defaultValue The default value the property gets initialized to if
    *       it is missing.
    */
   private static final void validateBaseProperty(String name, String defaultValue)
   {
      if (null == PropertyPersistor.findByName(name))
      {
         trace.info("CARNOT base property '" + name + "' not set,"
               + " will initialize it with its default value.");
         new PropertyPersistor(name, defaultValue);
      }
   }

   public static void alterAuditTrailCreateSequenceTable(String sysconPassword,
         boolean skipDdl, boolean skipDml, PrintStream spoolFile) throws SQLException
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX,
            session);
      try
      {
         ParametersFacade.pushLayer(locals);
         verifySysopPassword(session, sysconPassword);
         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);
         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         if (!skipDdl)
         {
            if (null != spoolFile)
            {
               spoolFile
                     .println("/* DDL-statements for creation of 'sequence' table and 'next_sequence_value_for' function */");
            }
            ddlManager
                  .createSequenceTable(schemaName, session.getConnection(), spoolFile);
         }
         if (!skipDml)
         {
            if (null != spoolFile)
            {
               spoolFile
                     .println("/* DML-statements for synchronization of 'sequence' table */");
            }

            ddlManager.synchronizeSequenceTable(schemaName, session.getConnection(),
                  spoolFile);
         }

         if (null != spoolFile)
         {
            spoolFile.println();
            spoolFile.println("commit;");
         }
         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailDropSequenceTable(String sysconPassword,
         PrintStream spoolFile) throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX,
            session);
      try
      {
         ParametersFacade.pushLayer(locals);
         verifySysopPassword(session, sysconPassword);
         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);
         Connection connection = session.getConnection();

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         if (null != spoolFile)
         {
            spoolFile
                  .println("/* DDL-statements for dropping 'sequence' table and 'next_sequence_value_for' function */");
         }
         ddlManager.dropSequenceTable(schemaName, connection, spoolFile);

         if (null != spoolFile)
         {
            spoolFile.println();
            spoolFile.println("commit;");
         }
         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailVerifySequenceTable(String sysconPassword)
         throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX,
            session);
      try
      {
         ParametersFacade.pushLayer(locals);
         verifySysopPassword(session, sysconPassword);

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         ddlManager.verifySequenceTable(session.getConnection(), schemaName);
         ddlManager.verifySequenceFunction(session.getConnection(), schemaName);
         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailCreateLockingTables(String sysconPassword)
         throws SQLException
   {
      alterAuditTrailCreateLockingTables(sysconPassword, false, false, null,
            DEFAULT_STATEMENT_DELIMITER);
   }

   public static void alterAuditTrailCreateLockingTables(String sysconPassword,
         String statementDelimiter) throws SQLException
   {
      alterAuditTrailCreateLockingTables(sysconPassword, false, false, null,
            statementDelimiter);
   }

   public static void alterAuditTrailCreateLockingTables(String sysconPassword,
         boolean skipDdl, boolean skipDml, PrintStream spoolFile,
         String statementDelimiter) throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         Collection classes = getPersistentClasses(dbDescriptor);

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         if ( !skipDdl)
         {
            if (null != spoolFile)
            {
               spoolFile
                     .println("/* DDL-statements for creation of locking tables and indexes */");
            }

            for (Iterator i = classes.iterator(); i.hasNext();)
            {
               Class clazz = (Class) i.next();
               TypeDescriptor typeDescr = TypeDescriptor.get(clazz);

               if (typeDescr.isDistinctLockTableName())
               {
                  ddlManager.createLockTableForClass(schemaName, clazz,
                        session.getConnection(), spoolFile, statementDelimiter);
               }
            }
         }

         if ( !skipDml)
         {
            if (null != spoolFile)
            {
               spoolFile
                     .println("/* DML-statements for synchronization of locking tables */");
            }

            for (Iterator i = classes.iterator(); i.hasNext();)
            {
               Class clazz = (Class) i.next();
               TypeDescriptor typeDescr = TypeDescriptor.get(clazz);

               if (typeDescr.isDistinctLockTableName())
               {
                  ddlManager.synchronizeLockTableForClass(clazz, session.getConnection(),
                        schemaName, spoolFile, statementDelimiter);
               }
            }
         }

         if (null != spoolFile)
         {
            spoolFile.println();
            spoolFile.println("commit;");
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailVerifyLockingTables(String sysconPassword)
         throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         Collection classes = getPersistentClasses(dbDescriptor);

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         for (Iterator i = classes.iterator(); i.hasNext();)
         {
            Class clazz = (Class) i.next();
            TypeDescriptor typeDescr = TypeDescriptor.get(clazz);

            if (typeDescr.isDistinctLockTableName())
            {
               ddlManager.verifyLockTableForClass(clazz, session.getConnection(), schemaName);
            }
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailDropLockingTables(String sysconPassword)
   {
      alterAuditTrailDropLockingTables(sysconPassword, null, DEFAULT_STATEMENT_DELIMITER);
   }

   public static void alterAuditTrailDropLockingTables(String sysconPassword,
         String statementDelimiter)
   {
      alterAuditTrailDropLockingTables(sysconPassword, null, statementDelimiter);
   }

   public static void alterAuditTrailDropLockingTables(String sysconPassword,
         PrintStream spoolFile, String statementDelimiter)
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         try
         {
            DBDescriptor dbDescriptor = session.getDBDescriptor();
            Collection classes = getPersistentClasses(dbDescriptor);

            DDLManager ddlManager = new DDLManager(dbDescriptor);
            Connection connection = session.getConnection();

            final String schemaName = Parameters.instance().getString(
                  SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
                  Parameters.instance().getString(
                        SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

            if (null != spoolFile)
            {
               spoolFile.println("/* DDL-statements for dropping locking tables and indexes */");
            }

            for (Iterator i = classes.iterator(); i.hasNext();)
            {
               Class clazz = (Class) i.next();

               try
               {
                  TypeDescriptor typeDescr = TypeDescriptor.get(clazz);

                  if (typeDescr.isDistinctLockTableName())
                  {
                     ddlManager.dropLockTableForClass(schemaName, clazz, connection,
                           spoolFile, statementDelimiter);
                  }
               }
               catch (Exception x)
               {
                  String message = "Couldn't drop lock table for class '" + clazz + "'."
                        + " Reason: " + x.getMessage();
                  System.out.println(message);
                  trace.warn(message, x);
               }
            }

            if (null != spoolFile)
            {
               spoolFile.println();
               spoolFile.println("commit;");
            }

            session.save();
         }
         catch (Exception ex)
         {
            session.rollback();
            throw new InternalException(ex);
         }
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailCreateDataClusterTables(String sysconPassword,
         String configFileName, boolean skipDdl, boolean skipDml, PrintStream spoolFile)
         throws SQLException
   {
      alterAuditTrailDataClusterTables(sysconPassword, configFileName, false, skipDdl,
            skipDml, spoolFile, DEFAULT_STATEMENT_DELIMITER);
   }

   private static TransientRuntimeSetup getTransientRuntimeSetup(String configFileName)
   {
      DocumentBuilder domBuilder = new RuntimeSetupDocumentBuilder();
      File runtimeSetupFile = new File(configFileName);
      try
      {
         Document setup = domBuilder.parse(new FileInputStream(runtimeSetupFile));
         String xml = XmlUtils.toString(setup);
         return new TransientRuntimeSetup(xml);
      }
      catch (SAXException x)
      {
         throw new PublicException(
               BpmRuntimeError.ATDB_INVALID_RUNTIME_SETUP_CONFIGURATION_FILE.raise(), x);
      }
      catch (IOException x)
      {
         throw new PublicException(
               BpmRuntimeError.ATDB_INVALID_RUNTIME_SETUP_CONFIGURATION_FILE.raise(), x);
      }
   }



   private static void applyClusterChanges(final Session session, IClusterChangeObserver clusterChanges, TransientRuntimeSetup newSetup, PrintStream sqlRecorder)
   {
      //when doing changes to the cluster table(columns), don't do anything strange
      //and take the column names as they are defined in the XML
      DatabaseHelper.columnNameModificationMode = ColumnNameModificationMode.NONE;

      RuntimeItem runtimeItem = createRuntimeItem(session);

      UpgradeObserver observer = new ErrorAwareObserver();
      runtimeItem.setSqlSpoolDevice(sqlRecorder);

      final String schemaName = Parameters.instance().getString(
            SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
            Parameters.instance().getString(
                  SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

      try
      {
         if (null != sqlRecorder)
         {
            sqlRecorder.println("/* DDL-statements for cluster tables */");
         }

         //process all drop table change
         Collection<DropTableInfo> dropChanges = clusterChanges.getDropInfos();
         for(DropTableInfo dropInfo: dropChanges)
         {
            DatabaseHelper.dropTable(runtimeItem, dropInfo, observer);
         }

         //process all create table change
         Collection<CreateTableInfo> createChanges = clusterChanges.getCreateInfos();
         for(CreateTableInfo createInfo: createChanges)
         {
            DatabaseHelper.createTable(runtimeItem, createInfo, observer);
         }

         //do the renaming - renaming means here:
         // 1) created the new column (already included in the {@link AlterTableInfo#getAddedFields()})
         Collection<AlterTableInfo> alterChanges = clusterChanges.getAlterInfos();
         for(AlterTableInfo alterInfo: alterChanges)
         {
            DatabaseHelper.alterTable(runtimeItem, alterInfo, observer, AlterMode.ADDED_COLUMNS_ONLY);
         }

         // 2) save it values from the old column to the new column
         DataClusterSynchronizationInfo syncInfo
            = clusterChanges.getDataClusterSynchronizationInfo();
         Map<String, Map<FieldInfo, FieldInfo>> columnRenames = syncInfo.getColumnRenames();
         for(String clusterTableName: columnRenames.keySet())
         {
            StringBuilder builder = new StringBuilder();
            builder.append("UPDATE ");
            builder.append(schemaName);
            builder.append(".");
            builder.append(clusterTableName);
            builder.append(" SET ");

            Map<FieldInfo, FieldInfo> columnMapping = columnRenames.get(clusterTableName);
            Iterator<FieldInfo> columnIterator
               = columnMapping.keySet().iterator();
            while(columnIterator.hasNext())
            {
               FieldInfo oldColumn = columnIterator.next();
               FieldInfo newColumn = columnMapping.get(oldColumn);

               builder.append(newColumn.getName());
               builder.append(" = ");
               builder.append(oldColumn.getName());
               if(columnIterator.hasNext())
               {
                  builder.append(", ");
               }
            }

            //retrieve values form old column and insert into new column, after that commit to allow
            //structural changes to the table
            DDLManager.executeOrSpoolStatement(builder.toString(), session.getConnection(), sqlRecorder);
            session.save();
         }

         // 3) drop the old column(already included in {@link AlterTableInfo#getDroppedFields()})
         for(AlterTableInfo alterInfo: alterChanges)
         {
            DatabaseHelper.alterTable(runtimeItem, alterInfo, observer, AlterMode.ADDED_COLUMNS_IGNORED);
         }

         //delete old setup
         DataClusterHelper.deleteDataClusterSetup();

         //insert new setup
         PropertyPersistor archiveProp = PropertyPersistor
               .findByName(Constants.CARNOT_ARCHIVE_AUDITTRAIL);
         boolean isArchiveAuditTrail = archiveProp != null ? Boolean
               .parseBoolean(archiveProp.getValue()) : false;
         if (isArchiveAuditTrail)
         {
            session.setUsingDataClusterOnArchiveAuditTrail(true);
         }
         PropertyPersistor newSetupPersistor = new PropertyPersistor(
               RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION, "dummy");
         LargeStringHolder.setLargeString(newSetupPersistor.getOID(), PropertyPersistor.class,
               newSetup.getXml());
         session.save();
         if (isArchiveAuditTrail)
         {
            session.setUsingDataClusterOnArchiveAuditTrail(false);
         }

         //force loading of new setup - to verify its working without problems
         Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, null);
         RuntimeSetup.instance().getDataClusterSetup();
      }
      catch (SQLException e)
      {
         handClusterUpgradeException(session, e, true);
      }
      catch (PublicException e)
      {
         handClusterUpgradeException(session, e, false);
         throw e;
      }
      finally
      {
         //clear old setup from memory
         Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, null);
      }
   }

   private static RuntimeItem createRuntimeItem(final Session session)
   {
      // Creating a RuntimeItem instance which is being used for schema modifications
      // using the upgrade framework.
      // NOTE: This item needs to share the same JDBC connection as the outer Session.
      //       Thus it is being initialized with DbmsKey-ID only.
      RuntimeItem runtimeItem = new RuntimeItem(
            session.getDBDescriptor().getDbmsKey().getId(),
            null, null, null, null)
      {
         /*
          * Return the same JDBC connection object as being used in Session. This prevents
          * locks which would occur if TX A (from RuntimeItem) modifies an object, e.g.
          * table, and TX B (from Session) tries to select / delete / update it.
          */
         @Override
         public Connection getConnection()
         {
            try
            {
               return session.getConnection();
            }
            catch (Exception e)
            {
               trace.warn("Failed obtaining JDBC connection", e);
               throw new UpgradeException(e.getMessage());
            }
         }
      };

      return runtimeItem;
   }

   private static void handClusterUpgradeException(Session session, Exception e, boolean propagate)
   {
      trace.error("Error during manipulating datacluster, removing invalid cluster setup: ", e);
      //try to clear invalid setup from database
      DataClusterHelper.deleteDataClusterSetup();

      if(propagate)
      {
         throw new PublicException(e);
      }
   }


   public static void alterAuditTrailDataClusterTables(String sysconPassword,
         String configFileName, boolean upgrade, boolean skipDdl, boolean skipDml, PrintStream spoolFile,
         String statementDelimiter) throws SQLException
   {
      try
      {
         IClusterChangeObserver changeObserver = null;
         boolean firstPartition = true;

         Set<Pair<Long, String>> partitionInfos = fetchListOfPartitionInfo();
         for (Pair<Long, String> partitionInfo : partitionInfos)
         {
            String partitionId = partitionInfo.getSecond();
            Utils.initCarnotEngine(partitionId, InitOptions.NO_FLUSH_BEFORE_INIT);

            try
            {
               Session consoleSession = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
               if (firstPartition)
               {
                  // only for 1st partition the structure cluster for tables need to be updated as these are used for _all_ partitions
                  verifySysopPassword(consoleSession, sysconPassword);

                  if ( !StringUtils.isEmpty(configFileName))
                  {
                     DataCluster[] oldSetup = RuntimeSetup.instance().getDataClusterSetup();
                     if(oldSetup != null && oldSetup.length > 0 && !upgrade)
                     {
                        throw new PublicException(
                              BpmRuntimeError.ATDB_CLUSTER_CONFIGURATION_ALREADY_EXIST_USE_OPTION_DROP_OR_UPDATEDATACLUSTERS_FIRST
                              .raise());
                     }

                     TransientRuntimeSetup transientSetup = getTransientRuntimeSetup(configFileName);
                     DataCluster[] newSetup = transientSetup.getDataClusterSetup();

                     DataClusterSetupAnalyzer analyzer = new DataClusterSetupAnalyzer();
                     changeObserver = analyzer.analyzeChanges(oldSetup, newSetup);

                     if (!skipDdl)
                     {
                        applyClusterChanges(consoleSession, changeObserver, transientSetup, spoolFile);
                     }
                  }
                  else
                  {
                     throw new PublicException(
                           BpmRuntimeError.ATDB_CLUSTER_CONFIGURATION_DOES_NOT_EXIST_PROVIDE_VALID_CONFIGURATION_FILE
                           .raise());
                  }
               }

               DBDescriptor dbDescriptor = consoleSession.getDBDescriptor();
               DDLManager ddlManager = new DDLManager(dbDescriptor);
               final String schemaName = Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
                     Parameters.instance().getString(
                           SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));
               if ( !skipDml)
               {
                  if (null != spoolFile)
                  {
                     spoolFile.println();
                     spoolFile.println(
                           "/* DML-statements for synchronization of cluster tables */");
                  }

                  ddlManager.synchronizeDataCluster(true, changeObserver.getDataClusterSynchronizationInfo(), consoleSession.getConnection(), schemaName, spoolFile, statementDelimiter, null);
               }

               if (null != spoolFile)
               {
                  spoolFile.println();
                  spoolFile.println("commit;");
               }

               consoleSession.save();

               // first partition done
               firstPartition = false;
            }
            finally
            {
               ParametersFacade.popLayer();
            }
         }
      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new PublicException(
               BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e.getMessage()));
      }
      finally
      {

      }
   }

   public static void alterAuditTrailVerifyDataClusterTables(String sysconPassword, PrintStream consoleLog)
         throws SQLException
   {
      try
      {
         Set<Pair<Long, String>> partitionInfos = fetchListOfPartitionInfo();
         for (Pair<Long, String> partitionInfo : partitionInfos)
         {
            String partitionId = partitionInfo.getSecond();
            Utils.initCarnotEngine(partitionId, InitOptions.NO_FLUSH_BEFORE_INIT);

            try
            {
               Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
               verifySysopPassword(session, sysconPassword);

               DBDescriptor dbDescriptor = session.getDBDescriptor();
               DDLManager ddlManager = new DDLManager(dbDescriptor);

               DataCluster[] cluster = RuntimeSetup.instance().getDataClusterSetup();

               final String schemaName = Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
                     Parameters.instance().getString(
                           SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

               for (int idx = 0; idx < cluster.length; ++idx)
               {
                  ddlManager.verifyClusterTable(cluster[idx], session.getConnection(), schemaName, consoleLog);
               }

               session.save();
            }
            finally
            {
               ParametersFacade.popLayer();
            }
         }
      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new PublicException(
               BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e.getMessage()));
      }
      finally
      {

      }
   }

   public static void alterAuditTrailSynchronizeDataClusterTables(String sysconPassword,
         PrintStream consoleLog, PrintStream spoolFile, String statementDelimiter)
         throws SQLException
   {
      Set<Pair<Long, String>> partitionInfos = fetchListOfPartitionInfo();
      for (Pair<Long, String> partitionInfo : partitionInfos)
      {
         String partitionId = partitionInfo.getSecond();
         Utils.initCarnotEngine(partitionId, InitOptions.NO_FLUSH_BEFORE_INIT);

         try
         {
            Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

            verifySysopPassword(session, sysconPassword);

            DBDescriptor dbDescriptor = session.getDBDescriptor();
            DDLManager ddlManager = new DDLManager(dbDescriptor);

            DataCluster[] cluster = RuntimeSetup.instance().getDataClusterSetup();

            final String schemaName = Parameters.instance().getString(
                  SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
                  Parameters.instance().getString(
                        SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

            for (DataCluster dataCluster : cluster)
            {
               ddlManager.synchronizeDataCluster(true,
                     DataClusterHelper.getDataClusterSynchronizationInfo(dataCluster, null),
                     session.getConnection(), schemaName, spoolFile, statementDelimiter, consoleLog);
               session.save();
            }
         }
         finally
         {
         }
      }
   }

   public static void alterAuditTrailDropDataClusterTables(String sysconPassword,
         PrintStream spoolFile)
   {
      alterAuditTrailDropDataClusterTables(sysconPassword, spoolFile,
            DEFAULT_STATEMENT_DELIMITER);
   }

   public static void alterAuditTrailDropDataClusterTables(String sysconPassword,
         PrintStream spoolFile, String statementDelimiter)
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         try
         {
            DBDescriptor dbDescriptor = session.getDBDescriptor();
            DDLManager ddlManager = new DDLManager(dbDescriptor);

            DataCluster[] cluster = RuntimeSetup.instance().getDataClusterSetup();

            final String schemaName = Parameters.instance().getString(
                  SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
                  Parameters.instance().getString(
                        SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

            if (null != spoolFile)
            {
               spoolFile.println("/* DDL-statements for dropping cluster tables and indexes */");
            }

            for (int idx = 0; idx < cluster.length; ++idx)
            {
               try
               {
                  ddlManager.dropClusterTable(cluster[idx], session.getConnection(),
                        schemaName, spoolFile, statementDelimiter);
               }
               catch (Exception x)
               {
                  String message = "Couldn't drop cluster table '"
                        + cluster[idx].getTableName() + "'." + " Reason: " + x.getMessage();
                  System.out.println(message);
                  trace.warn(message, x);
               }
            }

            if (null != spoolFile)
            {
               spoolFile.println();
               spoolFile.println("commit;");
            }

            PropertyPersistor prop = PropertyPersistor
                  .findByName(RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);
            if (null != prop)
            {
               LargeStringHolder.deleteAllForOID(prop.getOID(), PropertyPersistor.class);
               prop.delete();
            }

            session.save();
         }
         catch (Exception ex)
         {
            session.rollback();
            throw new InternalException(ex);
         }
      }
      finally
      {
         ParametersFacade.popLayer();
         Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, null);
      }
   }

   public static void alterAuditTrailCreatePartition(String sysconPassword, String partitionId,
         PrintStream spoolFile, String statementDelimiter) throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         if (null != spoolFile)
         {
            spoolFile
                  .println("/* DML-statements for partition creation */");
         }

         ddlManager.createAuditTrailPartition(partitionId, session.getConnection(),
               schemaName, spoolFile, statementDelimiter);

         if (null != spoolFile)
         {
            spoolFile.println();
            spoolFile.println("commit;");
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static void alterAuditTrailDropPartition(String partitionId, String password)
   {
      IAuditTrailPartition partition = AuditTrailPartitionBean.findById(partitionId);
      Session session = (Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      verifySysopPassword(session, password);

      deleteAllContentFromPartition(partition.getOID(), session);

      partition.delete();

      session.save(true);
   }

   public static void alterAuditTrailListPartitions(String sysconPassword)
         throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Map locals = new HashMap();
      locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);

      try
      {
         ParametersFacade.pushLayer(locals);

         verifySysopPassword(session, sysconPassword);

         DBDescriptor dbDescriptor = session.getDBDescriptor();
         DDLManager ddlManager = new DDLManager(dbDescriptor);

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         ddlManager.listAuditTrailPartitions(session.getConnection(), schemaName);
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   /**
    * TODO: refactor as this code is being duplicated / adapted from class R6_0_0from5_2_0RuntimeJob
    * @return
    * @throws SQLException
    */
   private static Set<Pair<Long, String>> fetchListOfPartitionInfo() throws SQLException
   {
      DBDescriptor dbDescriptor = DBDescriptor.create(SessionProperties.DS_NAME_AUDIT_TRAIL);
      Set<Pair<Long, String>> partitionInfo = CollectionUtils.newSet();
      PreparedStatement selectRowsStmt = null;
      Session session = null;
      try
      {
         // @formatter:off
         StringBuffer selectCmd = new StringBuffer()
               .append(SELECT).append(AuditTrailPartitionBean.FIELD__OID)
               .append(COMMA).append(AuditTrailPartitionBean.FIELD__ID)
               .append(FROM).append(DatabaseHelper.getQualifiedName(dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)));
         // @formatter:on

         session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
         Connection connection = session.getConnection();
         selectRowsStmt = DatabaseHelper.prepareLoggingStatement(connection, selectCmd.toString());

         ResultSet pendingRows = null;
         try
         {
            pendingRows = selectRowsStmt.executeQuery();
            while (pendingRows.next())
            {
               Long oid = pendingRows.getLong(AuditTrailPartitionBean.FIELD__OID);
               String id = pendingRows.getString(AuditTrailPartitionBean.FIELD__ID);
               partitionInfo.add(new Pair(oid, id));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
         QueryUtils.disconnectSession(session);
      }

      return partitionInfo;
   }


   private static class ErrorAwareObserver implements UpgradeObserver
   {
      @Override
      public void warn(String warning, Throwable reason)
      {
         trace.warn(warning, reason);
         if(reason != null)
         {
            throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(warning),
                  reason);
         }
      }
   }
}
