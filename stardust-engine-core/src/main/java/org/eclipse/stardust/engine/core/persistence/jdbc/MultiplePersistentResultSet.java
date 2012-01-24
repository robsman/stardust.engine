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

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Column;


/**
 * Wrapper class allowing projection of columns with given table alias.
 *
 * @author sborn
 * @version $Revision$
 */
public class MultiplePersistentResultSet implements ResultSet, Serializable
{
   private static final Logger trace = LogManager.getLogger(MultiplePersistentResultSet.class);
   
   private final ResultSet baseResultSet;
   private final AliasProjectionResultSet primaryPersistentProjector;
   private List secondaryPersistentProjectors;
   private AliasProjectionResultSet workingResultSet;
   private Iterator secondaryIterator;
   private boolean forceLoading;
   
   public static ResultSet createPersistentProjector(Class type,
         ITableDescriptor tableDescr, ResultSet resultSet, Column[] completeFieldList)
   {
      return new MultiplePersistentResultSet(type, tableDescr, resultSet,
            completeFieldList, false);
   }

   public static ResultSet createPersistentProjector(Class type,
         ITableDescriptor tableDescr, ResultSet resultSet, Column[] completeFieldList,
         boolean forceLoading)
   {
      return new MultiplePersistentResultSet(type, tableDescr, resultSet,
            completeFieldList, forceLoading);
   }

   private MultiplePersistentResultSet(Class type, ITableDescriptor tableDescr,
         ResultSet resultSet, Column[] completeFieldList, boolean forceLoading)
   {
      baseResultSet = resultSet;
      
      primaryPersistentProjector = AliasProjectionResultSet.createAliasProjector(type,
            tableDescr, resultSet, completeFieldList);
      workingResultSet = primaryPersistentProjector;
      secondaryPersistentProjectors = new ArrayList();
      secondaryIterator = secondaryPersistentProjectors.iterator();
      this.forceLoading = forceLoading;
   }
   
   public MultiplePersistentResultSet add(AliasProjectionResultSet resultSet)
   {
      secondaryPersistentProjectors.add(resultSet);
      secondaryIterator = secondaryPersistentProjectors.iterator();
      
      return this;
   }
   
   public void activatePrimary()
   {
      workingResultSet = primaryPersistentProjector;
      secondaryIterator = secondaryPersistentProjectors.iterator();
   }
   
   public boolean hasNextSecondary()
   {
      return secondaryIterator.hasNext();
   }
   
   public void nextSecondary()
   {
      workingResultSet = (AliasProjectionResultSet) secondaryIterator.next();
   }
   
   public Class getPersistentType()
   {
      return workingResultSet.getPersistentType();
   }
   
   public boolean isForceLoadingEnabled()
   {
      return forceLoading;
   }

   public void close() throws SQLException
   {
      baseResultSet.close();
   }

   public void updateLong(int columnIndex, long x) throws SQLException
   {
      workingResultSet.updateLong(columnIndex, x);
   }

   public void updateLong(String columnName, long x) throws SQLException
   {
      workingResultSet.updateLong(columnName, x);
   }

   public boolean getBoolean(int columnIndex) throws SQLException
   {
      return workingResultSet.getBoolean(columnIndex);
   }

   public boolean getBoolean(String columnName) throws SQLException
   {
      return workingResultSet.getBoolean(columnName);
   }

   public boolean wasNull() throws SQLException
   {
      return workingResultSet.wasNull();
   }

   public int getConcurrency() throws SQLException
   {
      return workingResultSet.getConcurrency();
   }

   public int getRow() throws SQLException
   {
      return workingResultSet.getRow();
   }

   public int getFetchSize() throws SQLException
   {
      return workingResultSet.getFetchSize();
   }

   public Ref getRef(int i) throws SQLException
   {
      return workingResultSet.getRef(i);
   }

   public Ref getRef(String colName) throws SQLException
   {
      return workingResultSet.getRef(colName);
   }

   public java.sql.Date getDate(int columnIndex) throws SQLException
   {
      return workingResultSet.getDate(columnIndex);
   }

   public java.sql.Date getDate(String columnName) throws SQLException
   {
      return workingResultSet.getDate(columnName);
   }

   public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException
   {
      return workingResultSet.getDate(columnIndex, cal);
   }

   public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException
   {
      return workingResultSet.getDate(columnName, cal);
   }

   public Statement getStatement() throws SQLException
   {
      return workingResultSet.getStatement();
   }

   public boolean isFirst() throws SQLException
   {
      return workingResultSet.isFirst();
   }

   public boolean previous() throws SQLException
   {
      return workingResultSet.previous();
   }

   public void updateFloat(int columnIndex, float x) throws SQLException
   {
      workingResultSet.updateFloat(columnIndex, x);
   }

   public void updateFloat(String columnName, float x) throws SQLException
   {
      workingResultSet.updateFloat(columnName, x);
   }

   public void updateRow() throws SQLException
   {
      workingResultSet.updateRow();
   }

   public void updateBinaryStream(int columnIndex,
               java.io.InputStream x,
               int length) throws SQLException
   {
      workingResultSet.updateBinaryStream(columnIndex, x, length);
   }

   public void updateBinaryStream(String columnName,
               java.io.InputStream x,
               int length) throws SQLException
   {
      workingResultSet.updateBinaryStream(columnName, x, length);
   }

   public long getLong(int columnIndex) throws SQLException
   {
      return workingResultSet.getLong(columnIndex);
   }

   public long getLong(String columnName) throws SQLException
   {
      return workingResultSet.getLong(columnName);
   }

   public void updateByte(int columnIndex, byte x) throws SQLException
   {
      workingResultSet.updateByte(columnIndex, x);
   }

   public void updateByte(String columnName, byte x) throws SQLException
   {
      workingResultSet.updateByte(columnName, x);
   }

   public void cancelRowUpdates() throws SQLException
   {
      workingResultSet.cancelRowUpdates();
   }

   public java.io.Reader getCharacterStream(int columnIndex) throws SQLException
   {
      return workingResultSet.getCharacterStream(columnIndex);
   }

   public java.io.Reader getCharacterStream(String columnName) throws SQLException
   {
      return workingResultSet.getCharacterStream(columnName);
   }

   public boolean absolute( int row ) throws SQLException
   {
      return workingResultSet.absolute(row);
   }

   public boolean first() throws SQLException
   {
      return workingResultSet.first();
   }

   public void updateAsciiStream(int columnIndex,
              java.io.InputStream x,
              int length) throws SQLException
   {
      workingResultSet.updateAsciiStream(columnIndex, x, length);
   }

   public void updateAsciiStream(String columnName,
              java.io.InputStream x,
              int length) throws SQLException
   {
      workingResultSet.updateAsciiStream(columnName, x, length);
   }

   public void moveToInsertRow() throws SQLException
   {
      workingResultSet.moveToInsertRow();
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return workingResultSet.getWarnings();
   }

   public void updateDate(int columnIndex, Date x) throws SQLException
   {
      workingResultSet.updateDate(columnIndex, x);
   }

   public void updateDate(String columnName, Date x) throws SQLException
   {
      workingResultSet.updateDate(columnName, x);
   }

   public java.io.InputStream getBinaryStream(int columnIndex)
       throws SQLException
   {
      return workingResultSet.getBinaryStream(columnIndex);
   }

   public java.io.InputStream getBinaryStream(String columnName)
       throws SQLException
   {
      return workingResultSet.getBinaryStream(columnName);
   }

   public void updateBytes(int columnIndex, byte x[]) throws SQLException
   {
      workingResultSet.updateBytes(columnIndex, x);
   }

   public void updateBytes(String columnName, byte x[]) throws SQLException
   {
      workingResultSet.updateBytes(columnName, x);
   }

   public boolean last() throws SQLException
   {
      return workingResultSet.last();
   }

   public String getCursorName() throws SQLException
   {
      return workingResultSet.getCursorName();
   }

   public Time getTime(int columnIndex) throws SQLException
   {
      return workingResultSet.getTime(columnIndex);
   }

   public Time getTime(String columnName) throws SQLException
   {
      return workingResultSet.getTime(columnName);
   }

   public Time getTime(int columnIndex, Calendar cal) throws SQLException
   {
      return workingResultSet.getTime(columnIndex, cal);
   }

   public Time getTime(String columnName, Calendar cal) throws SQLException
   {
      return workingResultSet.getTime(columnName, cal);
   }

   public void updateBoolean(int columnIndex, boolean x) throws SQLException
   {
      workingResultSet.updateBoolean(columnIndex, x);
   }

   public void updateBoolean(String columnName, boolean x) throws SQLException
   {
      workingResultSet.updateBoolean(columnName, x);
   }

   public void beforeFirst() throws SQLException
   {
      workingResultSet.beforeFirst();
   }

   public Timestamp getTimestamp(int columnIndex) throws SQLException
   {
      return workingResultSet.getTimestamp(columnIndex);
   }

   public Timestamp getTimestamp(String columnName) throws SQLException
   {
      return workingResultSet.getTimestamp(columnName);
   }

   public Timestamp getTimestamp(int columnIndex, Calendar cal)
     throws SQLException
   {
      return workingResultSet.getTimestamp(columnIndex, cal);
   }

   public Timestamp getTimestamp(String columnName, Calendar cal)
     throws SQLException
   {
      return workingResultSet.getTimestamp(columnName, cal);
   }

   public int getFetchDirection() throws SQLException
   {
      return workingResultSet.getFetchDirection();
   }

   public void updateDouble(int columnIndex, double x) throws SQLException
   {
      workingResultSet.updateDouble(columnIndex, x);
   }

   public void updateDouble(String columnName, double x) throws SQLException
   {
      workingResultSet.updateDouble(columnName, x);
   }

   public boolean rowDeleted() throws SQLException
   {
      return workingResultSet.rowDeleted();
   }

   public void setFetchDirection(int direction) throws SQLException
   {
      workingResultSet.setFetchDirection(direction);
   }

   public void moveToCurrentRow() throws SQLException
   {
      workingResultSet.moveToCurrentRow();
   }

   public void insertRow() throws SQLException
   {
      workingResultSet.insertRow();
   }

   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
   {
      return workingResultSet.getBigDecimal(columnIndex, scale);
   }

   public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
   {
      return workingResultSet.getBigDecimal(columnName, scale);
   }

   public BigDecimal getBigDecimal(int columnIndex) throws SQLException
   {
      return workingResultSet.getBigDecimal(columnIndex);
   }

   public BigDecimal getBigDecimal(String columnName) throws SQLException
   {
      return workingResultSet.getBigDecimal(columnName);
   }

   public boolean isAfterLast() throws SQLException
   {
      return workingResultSet.isAfterLast();
   }

   public short getShort(int columnIndex) throws SQLException
   {
      return workingResultSet.getShort(columnIndex);
   }

   public short getShort(String columnName) throws SQLException
   {
      return workingResultSet.getShort(columnName);
   }

   public void updateInt(int columnIndex, int x) throws SQLException
   {
      workingResultSet.updateInt(columnIndex, x);
   }

   public void updateInt(String columnName, int x) throws SQLException
   {
      workingResultSet.updateInt(columnName, x);
   }

   public void clearWarnings() throws SQLException
   {
      workingResultSet.clearWarnings();
   }

   public void updateCharacterStream(int columnIndex,
                java.io.Reader x,
                int length) throws SQLException
   {
      workingResultSet.updateCharacterStream(columnIndex, x, length);
   }

   public void updateCharacterStream(String columnName,
                java.io.Reader reader,
                int length) throws SQLException
   {
      workingResultSet.updateCharacterStream(columnName, reader, length);
   }

   public Blob getBlob(int i) throws SQLException
   {
      return workingResultSet.getBlob(i);
   }

   public Blob getBlob(String colName) throws SQLException
   {
      return workingResultSet.getBlob(colName);
   }

   public void refreshRow() throws SQLException
   {
      workingResultSet.refreshRow();
   }

   public void updateTime(int columnIndex, Time x) throws SQLException
   {
      workingResultSet.updateTime(columnIndex, x);
   }

   public void updateTime(String columnName, Time x) throws SQLException
   {
      workingResultSet.updateTime(columnName, x);
   }

   public void updateObject(int columnIndex, Object x, int scale)
     throws SQLException
   {
      workingResultSet.updateObject(columnIndex, x, scale);
   }

   public void updateObject(int columnIndex, Object x) throws SQLException
   {
      workingResultSet.updateObject(columnIndex, x);
   }

   public void updateObject(String columnName, Object x, int scale)
     throws SQLException
   {
      workingResultSet.updateObject(columnName, x, scale);
   }

   public void updateObject(String columnName, Object x) throws SQLException
   {
      workingResultSet.updateObject(columnName, x);
   }

   public void updateNull(int columnIndex) throws SQLException
   {
      workingResultSet.updateNull(columnIndex);
   }

   public void updateNull(String columnName) throws SQLException
   {
      workingResultSet.updateNull(columnName);
   }

   public boolean rowInserted() throws SQLException
   {
      return workingResultSet.rowInserted();
   }

   public Clob getClob(int i) throws SQLException
   {
      return workingResultSet.getClob(i);
   }

   public Clob getClob(String colName) throws SQLException
   {
      return workingResultSet.getClob(colName);
   }

   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
   {
      workingResultSet.updateBigDecimal(columnIndex, x);
   }

   public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
   {
      workingResultSet.updateBigDecimal(columnName, x);
   }

   public int getType() throws SQLException
   {
      return workingResultSet.getType();
   }

   public boolean next() throws SQLException
   {
      return workingResultSet.next();
   }

   public int getInt(int columnIndex) throws SQLException
   {
      return workingResultSet.getInt(columnIndex);
   }

   public int getInt(String columnName) throws SQLException
   {
      return workingResultSet.getInt(columnName);
   }

   public ResultSetMetaData getMetaData() throws SQLException
   {
      return workingResultSet.getMetaData();
   }

   public boolean rowUpdated() throws SQLException
   {
      return workingResultSet.rowUpdated();
   }

   public Array getArray(int i) throws SQLException
   {
      return workingResultSet.getArray(i);
   }

   public Array getArray(String colName) throws SQLException
   {
      return workingResultSet.getArray(colName);
   }

   public byte getByte(int columnIndex) throws SQLException
   {
      return workingResultSet.getByte(columnIndex);
   }

   public byte getByte(String columnName) throws SQLException
   {
      return workingResultSet.getByte(columnName);
   }

   public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException
   {
      return workingResultSet.getAsciiStream(columnIndex);
   }

   public java.io.InputStream getAsciiStream(String columnName) throws SQLException
   {
      return workingResultSet.getAsciiStream(columnName);
   }

   public double getDouble(int columnIndex) throws SQLException
   {
      return workingResultSet.getDouble(columnIndex);
   }

   public double getDouble(String columnName) throws SQLException
   {
      return workingResultSet.getDouble(columnName);
   }

   public void updateTimestamp(int columnIndex, Timestamp x)
     throws SQLException
   {
      workingResultSet.updateTimestamp(columnIndex, x);
   }

   public void updateTimestamp(String columnName, Timestamp x)
     throws SQLException
   {
      workingResultSet.updateTimestamp(columnName, x);
   }

   public boolean isBeforeFirst() throws SQLException
   {
      return workingResultSet.isBeforeFirst();
   }

   public void deleteRow() throws SQLException
   {
      workingResultSet.deleteRow();
   }

   public void afterLast() throws SQLException
   {
      workingResultSet.afterLast();
   }

   public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException
   {
      return workingResultSet.getUnicodeStream(columnIndex);
   }

   public java.io.InputStream getUnicodeStream(String columnName) throws SQLException
   {
      return workingResultSet.getUnicodeStream(columnName);
   }

   public byte[] getBytes(int columnIndex) throws SQLException
   {
      return workingResultSet.getBytes(columnIndex);
   }

   public byte[] getBytes(String columnName) throws SQLException
   {
      return workingResultSet.getBytes(columnName);
   }

   public void updateString(int columnIndex, String x) throws SQLException
   {
      workingResultSet.updateString(columnIndex, x);
   }

   public void updateString(String columnName, String x) throws SQLException
   {
      workingResultSet.updateString(columnName, x);
   }

   public float getFloat(int columnIndex) throws SQLException
   {
      return workingResultSet.getFloat(columnIndex);
   }

   public float getFloat(String columnName) throws SQLException
   {
      return workingResultSet.getFloat(columnName);
   }

   public Object getObject(int columnIndex) throws SQLException
   {
      return workingResultSet.getObject(columnIndex);
   }

   public Object getObject(String columnName) throws SQLException
   {
      return workingResultSet.getObject(columnName);
   }

   public Object getObject(int i, java.util.Map map) throws SQLException
   {
      return workingResultSet.getObject(i, map);
   }

   public Object getObject(String colName, java.util.Map map) throws SQLException
   {
      return workingResultSet.getObject(colName, map);
   }

   public void setFetchSize(int rows) throws SQLException
   {
      workingResultSet.setFetchSize(rows);
   }

   public boolean isLast() throws SQLException
   {
      return workingResultSet.isLast();
   }

   public void updateShort(int columnIndex, short x) throws SQLException
   {
      workingResultSet.updateShort(columnIndex, x);
   }

   public void updateShort(String columnName, short x) throws SQLException
   {
      workingResultSet.updateShort(columnName, x);
   }

   public String getString(int columnIndex) throws SQLException
   {
      return workingResultSet.getString(columnIndex);
   }

   public String getString(String columnName) throws SQLException
   {
      return workingResultSet.getString(columnName);
   }

   public int findColumn(String columnName) throws SQLException
   {
      return workingResultSet.findColumn(columnName);
   }

   public boolean relative( int rows ) throws SQLException
   {
      return workingResultSet.relative(rows);
   }

   // jdk 1.4 methods
   // noops as long as we compile against jdk 1.3

   public URL getURL(int columnIndex) throws SQLException
   {
      return null;
      //return resultSet.getURL(columnIndex);
   }

   public URL getURL(String columnName) throws SQLException
   {
      return null;
      //return resultSet.getURL(columnName);
   }

   public void updateArray(int columnIndex, Array x) throws SQLException
   {
      //resultSet.updateArray(columnIndex, x);
   }

   public void updateArray(String columnName, Array x) throws SQLException
   {
      //resultSet.updateArray(columnName, x);
   }

   public void updateBlob(int columnIndex, Blob x) throws SQLException
   {
      //resultSet.updateBlob(columnIndex, x);
   }

   public void updateBlob(String columnName, Blob x) throws SQLException
   {
      //resultSet.updateBlob(columnName, x);
   }

   public void updateClob(int columnIndex, Clob x) throws SQLException
   {
      //resultSet.updateClob(columnIndex, x);
   }

   public void updateClob(String columnName, Clob x) throws SQLException
   {
      //resultSet.updateClob(columnName, x);
   }

   public void updateRef(int columnIndex, Ref x) throws SQLException
   {
      //resultSet.updateRef(columnIndex, x);
   }

   public void updateRef(String columnName, Ref x) throws SQLException
   {
      //resultSet.updateRef(columnName, x);
   }
}
