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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

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
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetupDocumentBuilder;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
                  || (0 >= new Version(4, 0, 0).compareTo(auditTrailVersion)))
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
                        "There is no password set for the sysop-user.\n"
                              + "But as your schema seems to contain data sysop-user "
                              + "authorization is required.");
               }
               System.out.println("Allowing access to empty schema without sysop-user"
                     + " authorization.");
            }
            else if (!sysopPassword.equals(sysconPassword))
            {
               throw new PublicException(
                     "Please specify appropriate password for sysop-user.");
            }
         }
      }
      catch (SQLException e)
      {
         throw new PublicException("Failed during sysop-user authorization", e);
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
                  "Please specify appropriate password for sysop user "
                  + "before trying to change password");
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
            || RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION.equals(name))
      {
         throw new PublicException("Unable to set value of audit trail property '" + name
               + "'.");
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
            || RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION.equals(name))
      {
         throw new PublicException("Unable to delete audit trail property '" + name
               + "'.");
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
               + " will initialize it with it's default value.");
         new PropertyPersistor(name, defaultValue);
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
      alterAuditTrailCreateDataClusterTables(sysconPassword, configFileName, skipDdl,
            skipDml, spoolFile, DEFAULT_STATEMENT_DELIMITER);
   }

   public static void alterAuditTrailCreateDataClusterTables(String sysconPassword,
         String configFileName, boolean skipDdl, boolean skipDml, PrintStream spoolFile,
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

         PropertyPersistor prop = PropertyPersistor
               .findByName(RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);

         if (null != prop)
         {
            if ( !StringUtils.isEmpty(configFileName))
            {
               throw new PublicException(
                     "Cluster configuration already exists. Use option -dropDataClusters first.");
            }
         }
         else
         {
            if ( !StringUtils.isEmpty(configFileName))
            {
               DocumentBuilder domBuilder = new RuntimeSetupDocumentBuilder();
               File runtimeSetupFile = new File(configFileName);
               try
               {
                  Document setup = domBuilder.parse(new FileInputStream(runtimeSetupFile));
                  String xml = XmlUtils.toString(setup);
                  prop = new PropertyPersistor(
                        RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION, "dummy");
                  LargeStringHolder.setLargeString(prop.getOID(), PropertyPersistor.class,
                        xml);
                  session.save();
               }
               catch (SAXException x)
               {
                  throw new PublicException("Invalid runtime setup configuration file.", x);
               }
               catch (IOException x)
               {
                  throw new PublicException("Invalid runtime setup configuration file.", x);
               }
            }
            else
            {
               throw new PublicException(
                     "Cluster configuration does not exists. Provide valid configuration file.");
            }
         }

         DataCluster[] cluster;
         try
         {
            cluster = RuntimeSetup.instance().getDataClusterSetup();
         }
         catch (PublicException e)
         {
            LargeStringHolder.deleteAllForOID(prop.getOID(), PropertyPersistor.class);
            prop.delete();
            session.save();
            throw e;
         }

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         if ( !skipDdl)
         {
            if (null != spoolFile)
            {
               spoolFile
                     .println("/* DDL-statements for creation of cluster tables and indexes */");
            }

            for (int idx = 0; idx < cluster.length; ++idx)
            {
               ddlManager.createDataClusterTable(cluster[idx], session.getConnection(),
                     schemaName, spoolFile, statementDelimiter);
            }
         }

         if ( !skipDml)
         {
            if (null != spoolFile)
            {
               spoolFile.println();
               spoolFile
                     .println("/* DML-statements for synchronization of cluster tables */");
            }

            for (int idx = 0; idx < cluster.length; ++idx)
            {
               ddlManager.synchronizeDataCluster(cluster[idx], session.getConnection(),
                     schemaName, spoolFile, statementDelimiter);
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

   public static void alterAuditTrailVerifyDataClusterTables(String sysconPassword)
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

         DataCluster[] cluster = RuntimeSetup.instance().getDataClusterSetup();

         final String schemaName = Parameters.instance().getString(
               SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               Parameters.instance().getString(
                     SessionFactory.AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX));

         for (int idx = 0; idx < cluster.length; ++idx)
         {
            ddlManager.verifyClusterTable(cluster[idx], session.getConnection(), schemaName);
         }

         session.save();
      }
      finally
      {
         ParametersFacade.popLayer();
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
}
