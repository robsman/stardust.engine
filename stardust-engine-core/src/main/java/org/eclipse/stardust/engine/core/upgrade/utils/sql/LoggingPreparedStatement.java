package org.eclipse.stardust.engine.core.upgrade.utils.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class LoggingPreparedStatement implements PreparedStatement
{
   private static final Logger trace = LogManager.getLogger(LoggingPreparedStatement.class);
   private PreparedStatement delegate;

   private Map<Integer, Object> parameterMap = new HashMap<Integer, Object>();
   private String preparedSql;


   public LoggingPreparedStatement(PreparedStatement delegate, String preparedSql)
   {
      this.delegate = delegate;
      this.preparedSql = preparedSql;
   }

   private void logParameter(Integer parameterIndex, Object value)
   {
      parameterMap.put(parameterIndex, value);
   }

   private void logSql(String sql)
   {
      if(trace.isDebugEnabled())
      {
         trace.debug("executing SQL query: '" + sql + "'");
         int paramsSize = parameterMap.size();

         if(paramsSize > 0)
         {
            ArrayList<Integer> sortedKeys = new ArrayList(parameterMap.keySet());
            Collections.sort(sortedKeys);
            trace.debug("parameter values(in numeric order, ascending): ");

            for(Integer key: sortedKeys)
            {
               Object paramValue = parameterMap.get(key);
               StringBuffer paramLog = new StringBuffer();
               paramLog.append(key);
               paramLog.append(": ");
               paramLog.append(paramValue);
               trace.debug(paramLog);
            }
         }
         else
         {
            trace.debug(" no parameters set");
         }
      }
   }

   public ResultSet executeQuery(String sql) throws SQLException
   {
      logSql(sql);
      return delegate.executeQuery(sql);
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      return delegate.unwrap(iface);
   }

   public ResultSet executeQuery() throws SQLException
   {
      logSql(preparedSql);
      return delegate.executeQuery();
   }

   public int executeUpdate(String sql) throws SQLException
   {
      logSql(sql);
      return delegate.executeUpdate(sql);
   }

   public boolean isWrapperFor(Class< ? > iface) throws SQLException
   {
      return delegate.isWrapperFor(iface);
   }

   public int executeUpdate() throws SQLException
   {
      logSql(preparedSql);
      return delegate.executeUpdate();
   }

   public void close() throws SQLException
   {
      delegate.close();
   }

   public void setNull(int parameterIndex, int sqlType) throws SQLException
   {
      logParameter(parameterIndex, null);
      delegate.setNull(parameterIndex, sqlType);
   }

   public int getMaxFieldSize() throws SQLException
   {
      return delegate.getMaxFieldSize();
   }

   public void setBoolean(int parameterIndex, boolean x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBoolean(parameterIndex, x);
   }

   public void setMaxFieldSize(int max) throws SQLException
   {
      delegate.setMaxFieldSize(max);
   }

   public void setByte(int parameterIndex, byte x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setByte(parameterIndex, x);
   }

   public void setShort(int parameterIndex, short x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setShort(parameterIndex, x);
   }

   public int getMaxRows() throws SQLException
   {
      return delegate.getMaxRows();
   }

   public void setInt(int parameterIndex, int x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setInt(parameterIndex, x);
   }

   public void setMaxRows(int max) throws SQLException
   {
      delegate.setMaxRows(max);
   }

   public void setLong(int parameterIndex, long x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setLong(parameterIndex, x);
   }

   public void setEscapeProcessing(boolean enable) throws SQLException
   {
      delegate.setEscapeProcessing(enable);
   }

   public void setFloat(int parameterIndex, float x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setFloat(parameterIndex, x);
   }

   public int getQueryTimeout() throws SQLException
   {
      return delegate.getQueryTimeout();
   }

   public void setDouble(int parameterIndex, double x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setDouble(parameterIndex, x);
   }

   public void setQueryTimeout(int seconds) throws SQLException
   {
      delegate.setQueryTimeout(seconds);
   }

   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBigDecimal(parameterIndex, x);
   }

   public void cancel() throws SQLException
   {
      delegate.cancel();
   }

   public void setString(int parameterIndex, String x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setString(parameterIndex, x);
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return delegate.getWarnings();
   }

   public void setBytes(int parameterIndex, byte[] x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBytes(parameterIndex, x);
   }

   public void clearWarnings() throws SQLException
   {
      delegate.clearWarnings();
   }

   public void setDate(int parameterIndex, java.sql.Date x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setDate(parameterIndex, x);
   }

   public void setCursorName(String name) throws SQLException
   {
      delegate.setCursorName(name);
   }

   public void setTime(int parameterIndex, Time x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setTime(parameterIndex, x);
   }

   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setTimestamp(parameterIndex, x);
   }

   public boolean execute(String sql) throws SQLException
   {
      logSql(sql);
      return delegate.execute(sql);
   }

   public void setAsciiStream(int parameterIndex, InputStream x, int length)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setAsciiStream(parameterIndex, x, length);
   }

   public ResultSet getResultSet() throws SQLException
   {
      return delegate.getResultSet();
   }

   @SuppressWarnings("deprecation")
   public void setUnicodeStream(int parameterIndex, InputStream x, int length)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setUnicodeStream(parameterIndex, x, length);
   }

   public int getUpdateCount() throws SQLException
   {
      return delegate.getUpdateCount();
   }

   public boolean getMoreResults() throws SQLException
   {
      return delegate.getMoreResults();
   }

   public void setBinaryStream(int parameterIndex, InputStream x, int length)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBinaryStream(parameterIndex, x, length);
   }

   public void setFetchDirection(int direction) throws SQLException
   {
      delegate.setFetchDirection(direction);
   }

   public void clearParameters() throws SQLException
   {
      delegate.clearParameters();
   }

   public int getFetchDirection() throws SQLException
   {
      return delegate.getFetchDirection();
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setObject(parameterIndex, x, targetSqlType);
   }

   public void setFetchSize(int rows) throws SQLException
   {
      delegate.setFetchSize(rows);
   }

   public int getFetchSize() throws SQLException
   {
      return delegate.getFetchSize();
   }

   public void setObject(int parameterIndex, Object x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setObject(parameterIndex, x);
   }

   public int getResultSetConcurrency() throws SQLException
   {
      return delegate.getResultSetConcurrency();
   }

   public int getResultSetType() throws SQLException
   {
      return delegate.getResultSetType();
   }

   public void addBatch(String sql) throws SQLException
   {
      delegate.addBatch(sql);
   }

   public void clearBatch() throws SQLException
   {
      delegate.clearBatch();
   }

   public boolean execute() throws SQLException
   {
      logSql(preparedSql);
      return delegate.execute();
   }

   public int[] executeBatch() throws SQLException
   {
      logSql(preparedSql);
      return delegate.executeBatch();
   }

   public void addBatch() throws SQLException
   {
      delegate.addBatch();
   }

   public void setCharacterStream(int parameterIndex, Reader reader, int length)
         throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setCharacterStream(parameterIndex, reader, length);
   }

   public void setRef(int parameterIndex, Ref x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setRef(parameterIndex, x);
   }

   public Connection getConnection() throws SQLException
   {
      return delegate.getConnection();
   }

   public void setBlob(int parameterIndex, Blob x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBlob(parameterIndex, x);
   }

   public void setClob(int parameterIndex, Clob x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setClob(parameterIndex, x);
   }

   public boolean getMoreResults(int current) throws SQLException
   {
      return delegate.getMoreResults(current);
   }

   public void setArray(int parameterIndex, Array x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setArray(parameterIndex, x);
   }

   public ResultSetMetaData getMetaData() throws SQLException
   {
      return delegate.getMetaData();
   }

   public ResultSet getGeneratedKeys() throws SQLException
   {
      return delegate.getGeneratedKeys();
   }

   public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setDate(parameterIndex, x, cal);
   }

   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
   {
      logSql(sql);
      return delegate.executeUpdate(sql, autoGeneratedKeys);
   }

   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setTime(parameterIndex, x, cal);
   }

   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
   {
      logSql(sql);
      return delegate.executeUpdate(sql, columnIndexes);
   }

   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setTimestamp(parameterIndex, x, cal);
   }

   public void setNull(int parameterIndex, int sqlType, String typeName)
         throws SQLException
   {
      logParameter(parameterIndex, null);
      delegate.setNull(parameterIndex, sqlType, typeName);
   }

   public int executeUpdate(String sql, String[] columnNames) throws SQLException
   {
      logSql(sql);
      return delegate.executeUpdate(sql, columnNames);
   }

   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
   {
      logSql(sql);
      return delegate.execute(sql, autoGeneratedKeys);
   }

   public void setURL(int parameterIndex, URL x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setURL(parameterIndex, x);
   }

   public ParameterMetaData getParameterMetaData() throws SQLException
   {
      return delegate.getParameterMetaData();
   }

   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      delegate.setRowId(parameterIndex, x);
   }

   public boolean execute(String sql, int[] columnIndexes) throws SQLException
   {
      logSql(sql);
      return delegate.execute(sql, columnIndexes);
   }

   public void setNString(int parameterIndex, String value) throws SQLException
   {
      logParameter(parameterIndex, value);
      delegate.setNString(parameterIndex, value);
   }

   public void setNCharacterStream(int parameterIndex, Reader value, long length)
         throws SQLException
   {
      logParameter(parameterIndex, value);
      delegate.setNCharacterStream(parameterIndex, value, length);
   }

   public boolean execute(String sql, String[] columnNames) throws SQLException
   {
      logSql(sql);
      return delegate.execute(sql, columnNames);
   }

   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      logParameter(parameterIndex, value);
      delegate.setNClob(parameterIndex, value);
   }

   public void setClob(int parameterIndex, Reader reader, long length)
         throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setClob(parameterIndex, reader, length);
   }

   public int getResultSetHoldability() throws SQLException
   {
      return delegate.getResultSetHoldability();
   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length)
         throws SQLException
   {
      logParameter(parameterIndex, inputStream);
      delegate.setBlob(parameterIndex, inputStream, length);
   }

   public boolean isClosed() throws SQLException
   {
      return delegate.isClosed();
   }

   public void setPoolable(boolean poolable) throws SQLException
   {
      delegate.setPoolable(poolable);
   }

   public void setNClob(int parameterIndex, Reader reader, long length)
         throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setNClob(parameterIndex, reader, length);
   }

   public boolean isPoolable() throws SQLException
   {
      return delegate.isPoolable();
   }

   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
   {
      logParameter(parameterIndex, xmlObject);
      delegate.setSQLXML(parameterIndex, xmlObject);
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType,
         int scaleOrLength) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setAsciiStream(parameterIndex, x, length);
   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length)
         throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBinaryStream(parameterIndex, x, length);
   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length)
         throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setCharacterStream(parameterIndex, reader, length);
   }

   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setAsciiStream(parameterIndex, x);
   }

   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
   {
      logParameter(parameterIndex, x);
      delegate.setBinaryStream(parameterIndex, x);
   }

   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setCharacterStream(parameterIndex, reader);
   }

   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
   {
      logParameter(parameterIndex, value);
      delegate.setNCharacterStream(parameterIndex, value);
   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setClob(parameterIndex, reader);
   }

   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
   {
      logParameter(parameterIndex, inputStream);
      delegate.setBlob(parameterIndex, inputStream);
   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      logParameter(parameterIndex, reader);
      delegate.setNClob(parameterIndex, reader);
   }

   @Override
   public void closeOnCompletion() throws SQLException
   {
      delegate.closeOnCompletion();
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException
   {
      return delegate.isCloseOnCompletion();
   }
}
