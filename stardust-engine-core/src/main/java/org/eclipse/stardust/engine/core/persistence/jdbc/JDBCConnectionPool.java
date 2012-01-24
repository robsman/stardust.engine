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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Manages connections for one JDBC driver, one JDBC source and one (technical)
 * user.
 */
public class JDBCConnectionPool
{
   static final Logger trace = LogManager.getLogger(JDBCConnectionPool.class);

   /**
    * Stores all connection lists of all connections pools for releasing on
    * shutdown.
    */
   private static Vector poolList = new Vector();

   private final String databaseURL;
   private final String user;
   private final String password;

   private Driver driver;
   
   /**
    * Maximum number of (open) connections which can be retrieved from the datasource.
    */
   private int maxConnections;
   /**
    * Maximum number of connections held open.
    */
   private int returnConnections;
   /**
    * Connection pool.
    */
   protected Stack pool;
   /**
    * All open connections.
    */
   protected Vector connections;

   private ReleaseConnectionsHook shutdownHook;

   /**
    *
    */
   private synchronized static void registerForRelease(JDBCConnectionPool pool)
   {
      poolList.add(pool);
   }

   /**
    *
    */
   public synchronized static void releaseConnectionPools()
   {
      for (Iterator i = poolList.iterator(); i.hasNext();)
      {
         ((JDBCConnectionPool) i.next()).shutDown();
      }
   }

   public JDBCConnectionPool(String driverClazz, String databaseURL, String user,
         String password, int startTotal, int maxConnections, int returnConnections)
   {
      this(databaseURL, user, password, maxConnections, returnConnections);
      
      if (trace.isDebugEnabled())
      {
         trace.debug("Trying to register the driver to the DriverManager (class="
               + driverClazz + ").");
      }
      try
      {
         DriverManager.registerDriver(loadJdbcDriver(driverClazz));
      }
      catch (Throwable x)
      {
         throw new PublicException("Failed to load JDBC driver '" + driverClazz + "'", x);
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("Succesfully registered driver.");
      }

      initPool(startTotal);
   }


   public JDBCConnectionPool(Driver driver, String databaseURL, String user,
         String password, int startTotal, int maxConnections, int returnConnections)
   {
      this(databaseURL, user, password, maxConnections, returnConnections);

      this.driver = driver;

      initPool(startTotal);
   }

   protected JDBCConnectionPool(String databaseURL, String user, String password,
         int maxConnections, int returnConnections)
   {
      if (maxConnections < returnConnections)
      {
         throw new IllegalArgumentException(
               "Maximum number of connections must not be less than average.");
      }
      
      this.user = user;
      this.password = password;
      this.databaseURL = databaseURL;

      this.pool = new Stack();
      this.connections = new Vector();

      this.returnConnections = returnConnections;
      this.maxConnections = maxConnections;
   }

   private void initPool(int startTotal)
   {
      if (returnConnections < startTotal)
      {
         throw new IllegalArgumentException(
               "Initial number of connections must not be greater than average.");
      }

      if (System.getProperty("java.version").startsWith("1.3")
            || System.getProperty("java.version").startsWith("1.4")
            || System.getProperty("java.version").startsWith("1.5")
            || System.getProperty("java.version").startsWith("1.6"))
      {
         this.shutdownHook = new ReleaseConnectionsHook(this);
         Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
      else
      {
         registerForRelease(this);
      }

      for (int i = 0; i < startTotal; i++)
      {
         pool.push(newConnection());
      }
   }

   /**
    * Returns a connection connected with the provided parameters. This connection
    */
   public synchronized java.sql.Connection getConnection()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("*** Entering getConnection() - Pool: " + pool.size() + " Total: "
               + getTotalConnections());
      }

      java.sql.Connection connection;

      if (pool.empty())
      {
         if (getTotalConnections() == getMaxConnections())
         {
            throw new PublicException("Maximum number of connections " + getMaxConnections()
                  + " in connection pool exceeded.");
         }

         connection = newConnection();
      }
      else
      {
         connection = (java.sql.Connection) pool.pop();

         try
         {
            if (connection.isClosed())
            {
               // We have lost this connection, decrement count

               if (trace.isDebugEnabled())
               {
                  trace.debug("Pooled JDBC connection closed. Retrieving other.");
               }

               connections.remove(connection);

               // Try recursively to find an open connection

               connection = getConnection();
            }
         }
         catch (SQLException x)
         {
            throw new InternalException("Unexpected exception in check for closed connections: " + x);
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("*** Leaving getConnection() - Pool: " + pool.size() + " Total: "
               + getTotalConnections());
      }

      return connection;
   }

   private java.sql.Connection newConnection()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Creating new JDBC connection using URL '" + databaseURL
               + "', user is '" + user + "'.");
      }
      try
      {
         Connection jdbcConnection;
         if ((null != driver) && driver.acceptsURL(databaseURL))
         {
            // if a suitable driver instance was provided, skip use of DriverManager to
            // prevent class loading issues in an OSGI environment
            
            // wrap user/password in properties instance (see implementation of
            // DriverManager.getConnection(...))
            Properties props = new Properties();
            if (null != user)
            {
               props.put("user", user);
            }
            if (null != password)
            {
               props.put("password", password);
            }
            
            jdbcConnection = driver.connect(databaseURL, props);
         }
         else
         {
            jdbcConnection = DriverManager.getConnection(databaseURL, user, password);
         }         

         LocalJDBCConnection connection = new LocalJDBCConnection(jdbcConnection, this);

         connection.setAutoCommit(false);

         connections.add(connection);

         if (trace.isDebugEnabled())
         {
            trace.debug("Added the new connection to the pool.");
         }

         return connection;
      }
      catch (SQLException x)
      {
         trace.info("url: " + databaseURL);
         trace.info("user: " + user);
         throw new InternalException(
               "Failed to retrieve a new JDBC connection from the JDBC driver: ",  x);
      }
   }

   public synchronized void returnConnection(LocalJDBCConnection connection)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("*** Entering returnConnection() - Pool: " + pool.size()
               + " Total: " + getTotalConnections());
      }

      // Abort transaction, because otherwise in a pathologic scenario, the transaction might be committed by another session

      try
      {
         connection.rollback();
      }
      catch (SQLException x)
      {
         // Ignore
      }

      if (pool.contains(connection))
      {
         throw new InternalException("Connection " + connection
               + " returned twice to connection pool.");
      }

      if (pool.size() > returnConnections)
      {
         trace.debug("Closing returned connection.");

         try
         {
            connections.remove(connection);
            connection.getInnerConnection().close();
         }
         catch (SQLException x)
         {
            trace.debug("Cannot close connection: " + x);
         }
      }
      else
      {
         trace.debug("Returned connection pushed on pool.");

         pool.push(connection);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("*** Leaving returnConnection() - Pool: " + pool.size() + " Total: "
               + getTotalConnections());
      }
   }

   public synchronized int getTotalConnections()
   {
      return connections.size();
   }

   public synchronized int getMaxConnections()
   {
      return this.maxConnections;
   }

   public void shutDown()
   {
      shutDown(true);
   }
   
   private synchronized void shutDown(boolean removeReleaseHook)
   {
      if (removeReleaseHook && (null != shutdownHook))
      {
         Runtime.getRuntime().removeShutdownHook(shutdownHook);
         this.shutdownHook = null;
      }
      
      if (pool.size() < connections.size())
      {
         trace.warn("Releasing pooled connections while "
               + (connections.size() - pool.size()) + " connections are still in use.");
      }
      
      for (Iterator i = connections.iterator(); i.hasNext(); )
      {
         LocalJDBCConnection connection = (LocalJDBCConnection) i.next();

         try
         {
            if (!connection.isClosed())
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Closing JDBC connection " + connection);
               }

               connection.rollback();
               connection.getInnerConnection().close();
            }
         }
         catch (java.sql.SQLException x)
         {
         }
      }
      pool.clear();
      connections.clear();
   }

   private static Driver loadJdbcDriver(String driverClazz)
   {
      if (StringUtils.isEmpty(driverClazz))
      {
         throw new IllegalArgumentException("Driver class name must not be empty.");
      }

      if (JDBCConnectionPool.trace.isDebugEnabled())
      {
         JDBCConnectionPool.trace.debug("Trying to load JDBC driver '" + driverClazz
               + "'.");
      }
      try
      {
         Driver driver = (Driver) Class.forName(driverClazz).newInstance();
         if (JDBCConnectionPool.trace.isDebugEnabled())
         {
            JDBCConnectionPool.trace.debug("Succesfully loaded JDBC driver '"
                  + driverClazz + "'.");
         }

         return driver;
      }
      catch (Throwable x)
      {
         throw new PublicException("Failed to load JDBC driver '" + driverClazz + "'", x);
      }
   }
   
   /**
    * Shutdown hook for cleaning up all JDBC connectiones maintained by this
    * pool.
    */
   static public class ReleaseConnectionsHook extends Thread
   {
      private final JDBCConnectionPool pool;

      public ReleaseConnectionsHook(JDBCConnectionPool pool)
      {
         this.pool = pool;
      }

      public void run()
      {
         pool.shutDown(false);
      }
   }
}





