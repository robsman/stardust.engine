/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.archiver;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.Driver;
import org.springframework.core.io.ClassPathResource;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session.RuntimeDmlManagerProvider;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;

public class ArchiveTestUtils
{
   private static final String ARCHIVE_DATACLUSTER_SETUP = "test.archive.datacluster.setup";

   static final String SRC_SCHEMA = "PUBLIC";

   static final String ARC_SCHEMA = "ARCHIVE";

   public static Connection createArchiveAuditTrail() throws SQLException,
         ClassNotFoundException, IOException
   {
      return createArchiveAuditTrailWithDataCluster(null);
   }

   public static Connection createArchiveAuditTrailWithDataCluster(String clusterConfig)
         throws SQLException, ClassNotFoundException, IOException
   {
      Connection connection = createAuditTrail();
      if (clusterConfig != null)
      {
         createDCTables(clusterConfig);
      }
      resetProperties();
      return connection;
   }

   private static Connection createAuditTrail() throws SQLException,
         ClassNotFoundException
   {
      IAuditTrailPartition partition = (IAuditTrailPartition) Parameters.instance().get(
            SecurityProperties.CURRENT_PARTITION);
      Object partitionOid = Parameters.instance().get(
            SecurityProperties.CURRENT_PARTITION_OID);
      Connection connection;
      Class.forName(Driver.class.getName());
      Session session = setArchiveAudittrailProperties();
      SchemaHelper.createSchema(session);
      String updatePartitionOidStmt = "UPDATE "
            + DDLManager.getQualifiedName(ARC_SCHEMA, "PARTITION")
            + " SET oid=1 WHERE id='default'";
      connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(updatePartitionOidStmt, connection, null);
      connection.commit();
      Parameters.instance().set(SecurityProperties.CURRENT_PARTITION, partition);
      if (partitionOid != null)
      {
         Parameters.instance().set(SecurityProperties.CURRENT_PARTITION_OID,
               (Short) partitionOid);
      }

      return connection;
   }

   public static void dropArchiveAuditTrail(Connection connection) throws SQLException
   {
      Statement stmt = connection.createStatement();
      String deleteString = "DROP SCHEMA " + ARC_SCHEMA;
      stmt.executeUpdate(deleteString);
   }

   private static void createDCTables(String clusterConfig) throws IOException,
         SQLException
   {
      Parameters.instance().setBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, true);
      RuntimeSetup setup = RuntimeSetup.instance();
      Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, null);
      ClassPathResource resource = new ClassPathResource(clusterConfig);
      File configFile;
      configFile = resource.getFile();
      SchemaHelper.alterAuditTrailCreateDataClusterTables("sysop",
            configFile.getAbsolutePath(), false, true, null);
      RuntimeSetup arcDCSetup = RuntimeSetup.instance();
      Parameters.instance().set(ARCHIVE_DATACLUSTER_SETUP, arcDCSetup);
      Parameters.instance().setBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false);
      Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, setup);
   }

   public static void dropDCTables()
   {
      setArchiveAudittrailProperties();
      RuntimeSetup arcDCSetup = (RuntimeSetup) Parameters.instance().get(
            ARCHIVE_DATACLUSTER_SETUP);
      RuntimeSetup setup = (RuntimeSetup) Parameters.instance().get(
            RuntimeSetup.RUNTIME_SETUP_PROPERTY);
      Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, arcDCSetup);
      SchemaHelper.alterAuditTrailDropDataClusterTables("sysop", null);
      Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, setup);
      resetProperties();
   }

   private static DataSource obtainDataSource(String name)
   {
      Parameters params = Parameters.instance();
      DataSource result = null;
      String driver = "org.h2.Driver";
      String url = "jdbc:h2:tcp://localhost:9822/mem:stardust;MODE=ORACLE;MVCC=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS ARCHIVE";
      String user = "SA";
      String password = "";
      params.set("jdbc/" + name + SessionProperties.DS_DATA_SOURCE_SUFFIX, result);
      return new LocalDataSource(driver, url, user, password);
   }

   public static Session setArchiveAudittrailProperties()
   {
      Parameters.instance().set(
            SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX, ARC_SCHEMA);
      GlobalParameters.globals().set(
            RuntimeDmlManagerProvider.class.getName() + ".GlobalCache."
                  + SessionProperties.DS_NAME_AUDIT_TRAIL, null);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL,
            obtainDataSource(SessionFactory.AUDIT_TRAIL));
      Parameters.instance().set(
            SessionFactory.DS_NAME_AUDIT_TRAIL + SessionFactory.DS_SESSION_SUFFIX,
            session);
      return session;
   }

   public static void resetProperties()
   {
      Parameters.instance().set(
            SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX, SRC_SCHEMA);
      GlobalParameters.globals().set(
            RuntimeDmlManagerProvider.class.getName() + ".GlobalCache."
                  + SessionProperties.DS_NAME_AUDIT_TRAIL, null);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Parameters.instance().set(
            SessionFactory.DS_NAME_AUDIT_TRAIL + SessionFactory.DS_SESSION_SUFFIX,
            session);
   }

}