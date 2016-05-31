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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * <code>LocalJDBCConnection</code> delegates all functionality to the underlying java.sql.Connection, except Close
 * which is intercepted to return the connection to the pool
 */

class LocalJDBCConnection implements Connection
{
   Connection connection;
   JDBCConnectionPool jdbcConnectionPool;


   LocalJDBCConnection(Connection connection, JDBCConnectionPool jdbcConnectionPool)
   {
      this.connection = connection;
      this.jdbcConnectionPool = jdbcConnectionPool;
   }

   Connection getInnerConnection()
   {
      return connection;
   }


   /**
    * OVERRIDE
    * @throws SQLException
    */
   public void close() throws SQLException
   {
      jdbcConnectionPool.returnConnection(this);
   }

   public Statement createStatement() throws SQLException
   {
      return connection.createStatement();
   }

   public PreparedStatement prepareStatement(String sql)
   throws SQLException
   {
      return connection.prepareStatement(sql);
   }

   public CallableStatement prepareCall(String sql) throws SQLException
   {
      return connection.prepareCall(sql);
   }

   public String nativeSQL(String sql) throws SQLException
   {
      return connection.nativeSQL(sql);
   }

   public void setAutoCommit(boolean autoCommit) throws SQLException
   {
      connection.setAutoCommit(autoCommit);
   }

   public boolean getAutoCommit() throws SQLException
   {
      return connection.getAutoCommit();
   }

   public void commit() throws SQLException
   {
      connection.commit();
   }

   public void rollback() throws SQLException
   {
      connection.rollback();
   }


   public boolean isClosed() throws SQLException
   {
      return connection.isClosed();
   }

   public DatabaseMetaData getMetaData() throws SQLException
   {
      return connection.getMetaData();
   }

   public void setReadOnly(boolean readOnly) throws SQLException
   {
      connection.setReadOnly(readOnly);
   }

   public boolean isReadOnly() throws SQLException
   {
      return connection.isReadOnly();
   }

   public void setCatalog(String catalog) throws SQLException
   {
      connection.setCatalog(catalog);
   }

   public String getCatalog() throws SQLException
   {
      return connection.getCatalog();
   }

   public void setTransactionIsolation(int level) throws SQLException
   {
      connection.setTransactionIsolation(level);
   }

   public int getTransactionIsolation() throws SQLException
   {
      return connection.getTransactionIsolation();
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return connection.getWarnings();
   }

   public void clearWarnings() throws SQLException
   {
      connection.clearWarnings();
   }

   public Statement createStatement(int resultSetType, int resultSetConcurrency)
   throws SQLException
   {
      return connection.createStatement(resultSetType, resultSetConcurrency);
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType,
                      int resultSetConcurrency)
   throws SQLException
   {
      return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   public CallableStatement prepareCall(String sql, int resultSetType,
                 int resultSetConcurrency) throws SQLException
   {
      return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
   }

   public java.util.Map getTypeMap() throws SQLException
   {
      return connection.getTypeMap();
   }

   public void setTypeMap(java.util.Map map) throws SQLException
   {
      connection.setTypeMap(map);
   }



   // implementation of Java 1.4 methods

   public void setHoldability(int holdability) throws SQLException
   {
   }

   public int getHoldability() throws SQLException
   {
      return 0;
   }

   public Savepoint setSavepoint() throws SQLException
   {
      return null;
   }

   public Savepoint setSavepoint(String name) throws SQLException
   {
      return null;
   }

   public void rollback(Savepoint savepoint) throws SQLException
   {
   }

   public void releaseSavepoint(Savepoint savepoint) throws SQLException
   {
   }

   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
   {
      return null;
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
   {
      return null;
   }

   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
   {
      return null;
   }

   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
   {
      return null;
   }

   public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException
   {
      return null;
   }

   public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException
   {
      return null;
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

   @Override
   public Array createArrayOf(String arg0, Object[] arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Blob createBlob() throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Clob createClob() throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public NClob createNClob() throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public SQLXML createSQLXML() throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Struct createStruct(String arg0, Object[] arg1) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Properties getClientInfo() throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getClientInfo(String arg0) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isValid(int arg0) throws SQLException
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void setClientInfo(Properties arg0) throws SQLClientInfoException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setSchema(String schema) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public String getSchema() throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void abort(Executor executor) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public int getNetworkTimeout() throws SQLException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /*
   class Savepoint
   {

   }
   */


}
