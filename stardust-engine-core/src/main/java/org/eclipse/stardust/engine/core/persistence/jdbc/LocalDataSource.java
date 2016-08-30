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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters.IDisposable;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class LocalDataSource  implements DataSource, IDisposable
{
   private static final Logger trace = LogManager.getLogger(LocalDataSource.class);

   /**
    * the datasource maintains a set of <code>LocalJDBCConnection</code> objects via a JDBCConnectionPool.
    * the pool is handled as a singleton, the DataSource acts as factory.
    */
   private JDBCConnectionPool jdbcConnectionPool = null;

   private final String driverClazz;
   private final Driver driver;
   private final String url;
   private final String user;
   private final String password;
   private final Boolean autoCommit;

   /**
    * binds the local/pseudo datasource to its "enviroment" to support different databases.
    * @param driverClass
    * @param url
    * @param user
    * @param password
    */
   public LocalDataSource(String driverClazz, String url, String user, String password, Boolean autoCommit)
   {
      this.driverClazz = driverClazz;
      this.driver = null;
      this.url = url;
      this.user = user;
      this.password = password;
      this.autoCommit = autoCommit;
   }

   /**
    * binds the local/pseudo datasource to its "enviroment" to support different databases.
    * @param driverClass
    * @param url
    * @param user
    * @param password
    */
   public LocalDataSource(String driverClazz, String url, String user, String password)
   {
      this.driverClazz = driverClazz;
      this.driver = null;
      this.url = url;
      this.user = user;
      this.password = password;
      this.autoCommit = false;
   }

   /**
    * binds the local/pseudo datasource to its "enviroment" to support different databases.
    * @param driver
    * @param url
    * @param user
    * @param password
    */
   public LocalDataSource(Driver driver, String url, String user, String password)
   {
      this.driverClazz = driver.getClass().getName();
      this.driver = driver;
      this.url = url;
      this.user = user;
      this.password = password;
      this.autoCommit = false;
   }

   /**
    * <p>Attempts to establish a connection with the data source that
    * this <code>DataSource</code> object represents.
    *
    * @return  a connection to the data source implementing <code>LocalJDBCConnection</code>
    * which returns to the pool when it gets closed
    * @exception SQLException if a database access error occurs
    */
   public Connection getConnection() throws SQLException
   {
      trace.debug("Getting a connection from the pool...");

      if ((null != driver) && StringUtils.isEmpty(driverClazz))
      {
         throw new IllegalArgumentException("No JDBC driver was specified.");
      }
      if (StringUtils.isEmpty(url))
      {
         throw new IllegalArgumentException("JDBC URL must not be empty.");
      }
      if (null == user)
      {
         throw new IllegalArgumentException("JDBC user must not be null.");
      }
      if (null == password)
      {
         throw new IllegalArgumentException("JDBC Password must not be null.");
      }

      if(jdbcConnectionPool == null)
      {
         trace.debug("Inititalizing the pool...");

         if (null != driver)
         {
            this.jdbcConnectionPool = new JDBCConnectionPool(driver, //
                  url, //
                  user, password, //
                  1, 1000, 10, autoCommit);
            jdbcConnectionPool.setAutoCommit(autoCommit);
         }
         else
         {
            this.jdbcConnectionPool = new JDBCConnectionPool(driverClazz, //
                  url, //
                  user, password, //
                  1, 1000, 10, autoCommit);
         }
      }
      return jdbcConnectionPool.getConnection();
   }

   /**
    * not implemented
    *
    */
   public Connection getConnection(String username, String password) throws SQLException
   {
      return null;
   }

   /**
    * not implemented
    */
   public PrintWriter getLogWriter() throws SQLException
   {
      return null;
   }

   /**
    * not implemented
    */
   public void setLogWriter(PrintWriter out) throws SQLException
   {
   }

   /**
    * not implemented
    */
   public void setLoginTimeout(int seconds) throws SQLException
   {
   }

   /**
    * not implemented
    */
   public int getLoginTimeout() throws SQLException
   {
      return 0;
   }

   public void resetConnectionPool()
   {
      if (null != jdbcConnectionPool)
      {
         jdbcConnectionPool.shutDown();
         this.jdbcConnectionPool = null;
      }
   }

   public void dispose()
   {
      resetConnectionPool();
   }

   @Override
   public boolean isWrapperFor(Class< ? > iface) throws SQLException
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

}
