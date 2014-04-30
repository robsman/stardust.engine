/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl.monitoring;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sql.DataSource;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;

/**
 * <p>
 * This class is used to monitor database operations.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DatabaseOperationMonitoring
{
   private static final String TABLE_NAME_ANNOTATION = "TABLE_NAME";

   private static final String[] DB_OPS_TO_MONITOR = { "INSERT", "UPDATE", "DELETE", "SELECT" };

   private static final String TRIGGER_NAME_FRAGMENT = "_monitoring_trigger_";

   private final Queue<TableOperation> tableOps = new ConcurrentLinkedQueue<TableOperation>();

   public static DatabaseOperationMonitoring instance()
   {
      return DatabaseOperationMonitoringHolder.INSTANCE;
   }

   public void createMonitoringTriggers(final DataSource ds) throws SQLException
   {
      for (final Class<?> clazz : Constants.PERSISTENT_RUNTIME_CLASSES)
      {
         final String tableName = (String) Reflect.getStaticFieldValue(clazz, TABLE_NAME_ANNOTATION);
         createMonitoringTriggerFor(tableName, ds);
      }

      for (final Class<?> clazz : Constants.PERSISTENT_MODELING_CLASSES)
      {
         final String tableName = (String) Reflect.getStaticFieldValue(clazz, TABLE_NAME_ANNOTATION);
         createMonitoringTriggerFor(tableName, ds);
      }
   }

   public void dropMonitoringTriggers(final DataSource ds) throws SQLException
   {
      for (final Class<?> clazz : Constants.PERSISTENT_RUNTIME_CLASSES)
      {
         final String tableName = (String) Reflect.getStaticFieldValue(clazz, TABLE_NAME_ANNOTATION);
         dropMonitoringTriggerFor(tableName, ds);
      }

      for (final Class<?> clazz : Constants.PERSISTENT_MODELING_CLASSES)
      {
         final String tableName = (String) Reflect.getStaticFieldValue(clazz, TABLE_NAME_ANNOTATION);
         dropMonitoringTriggerFor(tableName, ds);
      }

      tableOps.clear();
   }

   public void assertExactly(final TableOperation[] ... expectedTableOps)
   {
      final List<TableOperation> actualOps = new ArrayList<TableOperation>(tableOps);

      for (final TableOperation[] tos : expectedTableOps)
      {
         for (final TableOperation to : tos)
         {
            final boolean removed = actualOps.remove(to);
            if ( !removed)
            {
               fail("Expected table operation '" + to + "' did not occur.");
            }
         }
      }

      assertThat("Unexpected table operations found.", actualOps, empty());
   }

   /* package-private */ void addTableOperation(final TableOperation tableOp)
   {
      if (tableOp == null)
      {
         throw new NullPointerException("Table operation must not be null.");
      }

      tableOps.add(tableOp);
   }

   private void createMonitoringTriggerFor(final String tableName, final DataSource ds) throws SQLException
   {
      doWithConnection(new StatementAction()
      {
         @Override
         public void run(final Statement stmt) throws SQLException
         {
            for (final String dbOp : DB_OPS_TO_MONITOR)
            {
               stmt.addBatch("CREATE TRIGGER " + buildTriggerName(dbOp, tableName) + " BEFORE " + dbOp + " ON " + tableName
                           + " CALL \"org.eclipse.stardust.test.impl.monitoring.MonitoringTrigger\"");
            }
            stmt.executeBatch();
         }
      }, ds);
   }

   private void dropMonitoringTriggerFor(final String tableName, final DataSource ds) throws SQLException
   {
      doWithConnection(new StatementAction()
      {
         @Override
         public void run(final Statement stmt) throws SQLException
         {
            for (final String dbOp : DB_OPS_TO_MONITOR)
            {
               stmt.addBatch("DROP TRIGGER " + buildTriggerName(dbOp, tableName));
            }
            stmt.executeBatch();
         }
      }, ds);
   }

   private String buildTriggerName(final String dbOp, final String tableName)
   {
      return dbOp.toLowerCase() + TRIGGER_NAME_FRAGMENT + tableName;
   }

   private void doWithConnection(final StatementAction action, final DataSource ds) throws SQLException
   {
      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         action.run(stmt);
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
   }

   private DatabaseOperationMonitoring()
   {
      /* nothing to do */
   }

   private static interface StatementAction
   {
      void run(final Statement stmt) throws SQLException;
   }

   /**
    * <p>
    * This class' only purpose is to ensure both safe publication and lazy initialization
    * (see 'lazy initialization class holder' idiom).
    * </p>
    */
   private static final class DatabaseOperationMonitoringHolder
   {
      private static final DatabaseOperationMonitoring INSTANCE = new DatabaseOperationMonitoring();
   }
}
