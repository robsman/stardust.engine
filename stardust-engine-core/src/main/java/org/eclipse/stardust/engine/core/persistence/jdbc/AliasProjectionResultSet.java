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
import java.sql.*;
import java.sql.Date;
import java.util.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Functions.BoundFunction;


/**
 * Wrapper class allowing projection of columns with given table alias.
 *
 * @author sborn
 * @version $Revision$
 */
public class AliasProjectionResultSet implements ResultSet, Serializable
{
   private static final long serialVersionUID = 2L;

   private static final Logger trace = LogManager.getLogger(AliasProjectionResultSet.class);
   
   private final Class type;
   private final ITableDescriptor tableDescr;
   private final ResultSet resultSet;
   private final List<Integer> fieldIndexList; 
   private final Map<String, Integer> fieldNameIndexMap;
   
   private final List<Pair<ValueType, Object>> fieldIndexValueList;
   
   private boolean useCustomWasNull = false;
   private boolean wasCustomNull = false;
   
   private int lookupIndexbyIndex(int columnIndex)
   {
      int idx = -columnIndex;
      
      try
      {
         idx = fieldIndexList.get(columnIndex - 1);
      }
      catch(IndexOutOfBoundsException e)
      {
         trace.warn("Index is not mapped. Access proceeds with negative index value.", e);
      }

      return idx;
   }

   private int lookupIndexbyName(String fieldName)
   {
      Assert.isNotEmpty(fieldName, "For index lookup a nonempty field name has to be provided.");
      
      int idx = -1;
      
      Integer lutValue = fieldNameIndexMap.get(fieldName);
      if (null != lutValue)
      {
         idx = lutValue.intValue();
      }
      else
      {
         trace.warn("Field name is not mapped. Access proceeds with -1.");
      }
         
      return idx;
   }

   public static AliasProjectionResultSet createAliasProjector(Class type,
         ITableDescriptor tdType, ResultSet resultSet, Column[] completeFieldList)
   {
      return new AliasProjectionResultSet(type, tdType, resultSet, completeFieldList,
            null);
   }

   public static AliasProjectionResultSet createAliasProjector(Class type,
         ITableDescriptor tdType, ResultSet resultSet, Column[] completeFieldList,
         List<Pair<ValueType, Object>> fieldIndexValueList)
   {
      return new AliasProjectionResultSet(type, tdType, resultSet, completeFieldList, fieldIndexValueList);
   }

   private AliasProjectionResultSet(Class type, ITableDescriptor tdType,
         ResultSet resultSet, Column[] completeFieldList, List<Pair<ValueType, Object>> fieldIndexValueList)
   {
      this.type = type;
      this.tableDescr = tdType;
      this.resultSet = resultSet;
      this.fieldIndexList = new ArrayList();
      this.fieldNameIndexMap = CollectionUtils.newHashMap();
      
      this.fieldIndexValueList = fieldIndexValueList;
      
      // build lookup tables
      int realIndex = 1;
      for (int i = 0; i < completeFieldList.length; ++i)
      {
         if (completeFieldList[i] instanceof FieldRef)
         {
            FieldRef field = (FieldRef) completeFieldList[i];
            if ( !StringUtils.isEmpty(field.getType().getTableAlias())
                  && tdType.getTableAlias().equals(field.getType().getTableAlias())
                  || StringUtils.isEmpty(tdType.getTableAlias())
                  && tdType.getTableName().equals(field.getType().getTableName()))
            {
               fieldIndexList.add(new Integer(realIndex));
               fieldNameIndexMap.put(field.fieldName, new Integer(realIndex));
            }
            ++realIndex;
         }
         else if (completeFieldList[i] instanceof BoundFunction)
         {
            BoundFunction fuction = (BoundFunction) completeFieldList[i];
            if ( !StringUtils.isEmpty(tdType.getTableAlias())
                  && fuction.getSelector().startsWith(tdType.getTableAlias() + "."))
            {
               fieldIndexList.add(new Integer(realIndex));
               fieldNameIndexMap.put(fuction.getSelector(), new Integer(realIndex));
            }
            ++realIndex;
         }
         else
         {
            throw new InternalException(
                  "Eager fetch of other than regular fields is not supported.");
         }
      }
   }
   
   public Class getPersistentType()
   {
      return type;
   }

   public void close() throws SQLException
   {
      resultSet.close();
   }

   public void updateLong(int columnIndex, long x) throws SQLException
   {
      resultSet.updateLong(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateLong(String columnName, long x) throws SQLException
   {
      resultSet.updateLong(lookupIndexbyName(columnName), x);
   }

   public boolean getBoolean(int columnIndex) throws SQLException
   {
      return resultSet.getBoolean(lookupIndexbyIndex(columnIndex));
   }

   public boolean getBoolean(String columnName) throws SQLException
   {
      return resultSet.getBoolean(lookupIndexbyName(columnName));
   }

   public boolean wasNull() throws SQLException
   {
      return useCustomWasNull ? wasCustomNull : resultSet.wasNull();
   }

   public int getConcurrency() throws SQLException
   {
      return resultSet.getConcurrency();
   }

   public int getRow() throws SQLException
   {
      return resultSet.getRow();
   }

   public int getFetchSize() throws SQLException
   {
      return resultSet.getFetchSize();
   }

   public Ref getRef(int i) throws SQLException
   {
      return resultSet.getRef(lookupIndexbyIndex(i));
   }

   public Ref getRef(String colName) throws SQLException
   {
      return resultSet.getRef(lookupIndexbyName(colName));
   }

   public java.sql.Date getDate(int columnIndex) throws SQLException
   {
      return resultSet.getDate(lookupIndexbyIndex(columnIndex));
   }

   public java.sql.Date getDate(String columnName) throws SQLException
   {
      return resultSet.getDate(lookupIndexbyName(columnName));
   }

   public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException
   {
      return resultSet.getDate(lookupIndexbyIndex(columnIndex), cal);
   }

   public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException
   {
      return resultSet.getDate(lookupIndexbyName(columnName), cal);
   }

   public Statement getStatement() throws SQLException
   {
      return resultSet.getStatement();
   }

   public boolean isFirst() throws SQLException
   {
      return resultSet.isFirst();
   }

   public boolean previous() throws SQLException
   {
      return resultSet.previous();
   }

   public void updateFloat(int columnIndex, float x) throws SQLException
   {
      resultSet.updateFloat(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateFloat(String columnName, float x) throws SQLException
   {
      resultSet.updateFloat(lookupIndexbyName(columnName), x);
   }

   public void updateRow() throws SQLException
   {
      resultSet.updateRow();
   }

   public void updateBinaryStream(int columnIndex,
               java.io.InputStream x,
               int length) throws SQLException
   {
      resultSet.updateBinaryStream(lookupIndexbyIndex(columnIndex), x, length);
   }

   public void updateBinaryStream(String columnName,
               java.io.InputStream x,
               int length) throws SQLException
   {
      resultSet.updateBinaryStream(lookupIndexbyName(columnName), x, length);
   }

   public long getLong(int columnIndex) throws SQLException
   {
      return resultSet.getLong(lookupIndexbyIndex(columnIndex));
   }

   public long getLong(String columnName) throws SQLException
   {
      return resultSet.getLong(lookupIndexbyName(columnName));
   }

   public void updateByte(int columnIndex, byte x) throws SQLException
   {
      resultSet.updateByte(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateByte(String columnName, byte x) throws SQLException
   {
      resultSet.updateByte(lookupIndexbyName(columnName), x);
   }

   public void cancelRowUpdates() throws SQLException
   {
      resultSet.cancelRowUpdates();
   }

   public java.io.Reader getCharacterStream(int columnIndex) throws SQLException
   {
      return resultSet.getCharacterStream(lookupIndexbyIndex(columnIndex));
   }

   public java.io.Reader getCharacterStream(String columnName) throws SQLException
   {
      return resultSet.getCharacterStream(lookupIndexbyName(columnName));
   }

   public boolean absolute( int row ) throws SQLException
   {
      return resultSet.absolute(row);
   }

   public boolean first() throws SQLException
   {
      return resultSet.first();
   }

   public void updateAsciiStream(int columnIndex,
              java.io.InputStream x,
              int length) throws SQLException
   {
      resultSet.updateAsciiStream(lookupIndexbyIndex(columnIndex), x, length);
   }

   public void updateAsciiStream(String columnName,
              java.io.InputStream x,
              int length) throws SQLException
   {
      resultSet.updateAsciiStream(lookupIndexbyName(columnName), x, length);
   }

   public void moveToInsertRow() throws SQLException
   {
      resultSet.moveToInsertRow();
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return resultSet.getWarnings();
   }

   public void updateDate(int columnIndex, Date x) throws SQLException
   {
      resultSet.updateDate(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateDate(String columnName, Date x) throws SQLException
   {
      resultSet.updateDate(lookupIndexbyName(columnName), x);
   }

   public java.io.InputStream getBinaryStream(int columnIndex)
       throws SQLException
   {
      return resultSet.getBinaryStream(lookupIndexbyIndex(columnIndex));
   }

   public java.io.InputStream getBinaryStream(String columnName)
       throws SQLException
   {
      return resultSet.getBinaryStream(lookupIndexbyName(columnName));
   }

   public void updateBytes(int columnIndex, byte x[]) throws SQLException
   {
      resultSet.updateBytes(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateBytes(String columnName, byte x[]) throws SQLException
   {
      resultSet.updateBytes(lookupIndexbyName(columnName), x);
   }

   public boolean last() throws SQLException
   {
      return resultSet.last();
   }

   public String getCursorName() throws SQLException
   {
      return resultSet.getCursorName();
   }

   public Time getTime(int columnIndex) throws SQLException
   {
      return resultSet.getTime(lookupIndexbyIndex(columnIndex));
   }

   public Time getTime(String columnName) throws SQLException
   {
      return resultSet.getTime(lookupIndexbyName(columnName));
   }

   public Time getTime(int columnIndex, Calendar cal) throws SQLException
   {
      return resultSet.getTime(lookupIndexbyIndex(columnIndex), cal);
   }

   public Time getTime(String columnName, Calendar cal) throws SQLException
   {
      return resultSet.getTime(lookupIndexbyName(columnName), cal);
   }

   public void updateBoolean(int columnIndex, boolean x) throws SQLException
   {
      resultSet.updateBoolean(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateBoolean(String columnName, boolean x) throws SQLException
   {
      resultSet.updateBoolean(lookupIndexbyName(columnName), x);
   }

   public void beforeFirst() throws SQLException
   {
      resultSet.beforeFirst();
   }

   public Timestamp getTimestamp(int columnIndex) throws SQLException
   {
      return resultSet.getTimestamp(lookupIndexbyIndex(columnIndex));
   }

   public Timestamp getTimestamp(String columnName) throws SQLException
   {
      return resultSet.getTimestamp(lookupIndexbyName(columnName));
   }

   public Timestamp getTimestamp(int columnIndex, Calendar cal)
     throws SQLException
   {
      return resultSet.getTimestamp(lookupIndexbyIndex(columnIndex), cal);
   }

   public Timestamp getTimestamp(String columnName, Calendar cal)
     throws SQLException
   {
      return resultSet.getTimestamp(lookupIndexbyName(columnName), cal);
   }

   public int getFetchDirection() throws SQLException
   {
      return resultSet.getFetchDirection();
   }

   public void updateDouble(int columnIndex, double x) throws SQLException
   {
      resultSet.updateDouble(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateDouble(String columnName, double x) throws SQLException
   {
      resultSet.updateDouble(lookupIndexbyName(columnName), x);
   }

   public boolean rowDeleted() throws SQLException
   {
      return resultSet.rowDeleted();
   }

   public void setFetchDirection(int direction) throws SQLException
   {
      resultSet.setFetchDirection(direction);
   }

   public void moveToCurrentRow() throws SQLException
   {
      resultSet.moveToCurrentRow();
   }

   public void insertRow() throws SQLException
   {
      resultSet.insertRow();
   }

   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
   {
      return resultSet.getBigDecimal(lookupIndexbyIndex(columnIndex), scale);
   }

   public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
   {
      return resultSet.getBigDecimal(lookupIndexbyName(columnName), scale);
   }

   public BigDecimal getBigDecimal(int columnIndex) throws SQLException
   {
      return resultSet.getBigDecimal(lookupIndexbyIndex(columnIndex));
   }

   public BigDecimal getBigDecimal(String columnName) throws SQLException
   {
      return resultSet.getBigDecimal(lookupIndexbyName(columnName));
   }

   public boolean isAfterLast() throws SQLException
   {
      return resultSet.isAfterLast();
   }

   public short getShort(int columnIndex) throws SQLException
   {
      return resultSet.getShort(lookupIndexbyIndex(columnIndex));
   }

   public short getShort(String columnName) throws SQLException
   {
      return resultSet.getShort(lookupIndexbyName(columnName));
   }

   public void updateInt(int columnIndex, int x) throws SQLException
   {
      resultSet.updateInt(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateInt(String columnName, int x) throws SQLException
   {
      resultSet.updateInt(lookupIndexbyName(columnName), x);
   }

   public void clearWarnings() throws SQLException
   {
      resultSet.clearWarnings();
   }

   public void updateCharacterStream(int columnIndex,
                java.io.Reader x,
                int length) throws SQLException
   {
      resultSet.updateCharacterStream(lookupIndexbyIndex(columnIndex), x, length);
   }

   public void updateCharacterStream(String columnName,
                java.io.Reader reader,
                int length) throws SQLException
   {
      resultSet.updateCharacterStream(lookupIndexbyName(columnName), reader, length);
   }

   public Blob getBlob(int i) throws SQLException
   {
      return resultSet.getBlob(lookupIndexbyIndex(i));
   }

   public Blob getBlob(String colName) throws SQLException
   {
      return resultSet.getBlob(lookupIndexbyName(colName));
   }

   public void refreshRow() throws SQLException
   {
      resultSet.refreshRow();
   }

   public void updateTime(int columnIndex, Time x) throws SQLException
   {
      resultSet.updateTime(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateTime(String columnName, Time x) throws SQLException
   {
      resultSet.updateTime(lookupIndexbyName(columnName), x);
   }

   public void updateObject(int columnIndex, Object x, int scale)
     throws SQLException
   {
      resultSet.updateObject(lookupIndexbyIndex(columnIndex), x, scale);
   }

   public void updateObject(int columnIndex, Object x) throws SQLException
   {
      resultSet.updateObject(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateObject(String columnName, Object x, int scale)
     throws SQLException
   {
      resultSet.updateObject(lookupIndexbyName(columnName), x, scale);
   }

   public void updateObject(String columnName, Object x) throws SQLException
   {
      resultSet.updateObject(lookupIndexbyName(columnName), x);
   }

   public void updateNull(int columnIndex) throws SQLException
   {
      resultSet.updateNull(lookupIndexbyIndex(columnIndex));
   }

   public void updateNull(String columnName) throws SQLException
   {
      resultSet.updateNull(lookupIndexbyName(columnName));
   }

   public boolean rowInserted() throws SQLException
   {
      return resultSet.rowInserted();
   }

   public Clob getClob(int i) throws SQLException
   {
      return resultSet.getClob(lookupIndexbyIndex(i));
   }

   public Clob getClob(String colName) throws SQLException
   {
      return resultSet.getClob(lookupIndexbyName(colName));
   }

   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
   {
      resultSet.updateBigDecimal(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
   {
      resultSet.updateBigDecimal(lookupIndexbyName(columnName), x);
   }

   public int getType() throws SQLException
   {
      return resultSet.getType();
   }

   public boolean next() throws SQLException
   {
      return resultSet.next();
   }

   public int getInt(int columnIndex) throws SQLException
   {
      return resultSet.getInt(lookupIndexbyIndex(columnIndex));
   }

   public int getInt(String columnName) throws SQLException
   {
      return resultSet.getInt(lookupIndexbyName(columnName));
   }

   public ResultSetMetaData getMetaData() throws SQLException
   {
      return resultSet.getMetaData();
   }

   public boolean rowUpdated() throws SQLException
   {
      return resultSet.rowUpdated();
   }

   public Array getArray(int i) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getArray(lookupIndexbyIndex(i));
   }

   public Array getArray(String colName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getArray(lookupIndexbyName(colName));
   }

   public byte getByte(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getByte(lookupIndexbyIndex(columnIndex));
   }

   public byte getByte(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getByte(lookupIndexbyName(columnName));
   }

   public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getAsciiStream(lookupIndexbyIndex(columnIndex));
   }

   public java.io.InputStream getAsciiStream(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getAsciiStream(lookupIndexbyName(columnName));
   }

   public double getDouble(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getDouble(lookupIndexbyIndex(columnIndex));
   }

   public double getDouble(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getDouble(lookupIndexbyName(columnName));
   }

   public void updateTimestamp(int columnIndex, Timestamp x)
     throws SQLException
   {
      resultSet.updateTimestamp(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateTimestamp(String columnName, Timestamp x)
     throws SQLException
   {
      resultSet.updateTimestamp(lookupIndexbyName(columnName), x);
   }

   public boolean isBeforeFirst() throws SQLException
   {
      return resultSet.isBeforeFirst();
   }

   public void deleteRow() throws SQLException
   {
      resultSet.deleteRow();
   }

   public void afterLast() throws SQLException
   {
      resultSet.afterLast();
   }

   public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getUnicodeStream(lookupIndexbyIndex(columnIndex));
   }

   public java.io.InputStream getUnicodeStream(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getUnicodeStream(lookupIndexbyName(columnName));
   }

   public byte[] getBytes(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getBytes(lookupIndexbyIndex(columnIndex));
   }

   public byte[] getBytes(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getBytes(lookupIndexbyName(columnName));
   }

   public void updateString(int columnIndex, String x) throws SQLException
   {
      resultSet.updateString(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateString(String columnName, String x) throws SQLException
   {
      resultSet.updateString(lookupIndexbyName(columnName), x);
   }

   public float getFloat(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getFloat(lookupIndexbyIndex(columnIndex));
   }

   public float getFloat(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getFloat(lookupIndexbyName(columnName));
   }

   public Object getObject(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      if ( fieldIndexValueList != null && !fieldIndexValueList.isEmpty())
      {
         Pair<ValueType, Object> valueDscr = fieldIndexValueList.get(columnIndex-1);
         Object value = valueDscr.getSecond();
         switch (valueDscr.getFirst())
         {
            case LOCAL_RS_INDEX:
               return resultSet
                     .getObject(lookupIndexbyIndex((Integer) value));
            case GLOBAL_RS_INDEX:
               return resultSet.getObject((Integer) value);
            case OBJECT:
               useCustomWasNull = true;
               wasCustomNull = value == null;
               return value;
            default:
               throw new InternalException("ValueType not supported: " + valueDscr.getFirst());
         }
      }
      else
      {
         return resultSet.getObject(lookupIndexbyIndex(columnIndex));
      }
   }

   public Object getObject(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getObject(lookupIndexbyName(columnName));
   }

   public Object getObject(int i, java.util.Map map) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getObject(lookupIndexbyIndex(i), map);
   }

   public Object getObject(String colName, java.util.Map map) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getObject(lookupIndexbyName(colName), map);
   }

   public void setFetchSize(int rows) throws SQLException
   {
      resultSet.setFetchSize(rows);
   }

   public boolean isLast() throws SQLException
   {
      return resultSet.isLast();
   }

   public void updateShort(int columnIndex, short x) throws SQLException
   {
      resultSet.updateShort(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateShort(String columnName, short x) throws SQLException
   {
      resultSet.updateShort(lookupIndexbyName(columnName), x);
   }

   public String getString(int columnIndex) throws SQLException
   {
      useCustomWasNull = false;
      
      if ( fieldIndexValueList != null && !fieldIndexValueList.isEmpty())
      {
         Pair<ValueType, Object> valueDscr = fieldIndexValueList.get(columnIndex-1);
         Object value = valueDscr.getSecond();
         switch (valueDscr.getFirst())
         {
            case LOCAL_RS_INDEX:
               return resultSet.getString(lookupIndexbyIndex(columnIndex));
            case GLOBAL_RS_INDEX:
               return resultSet.getString((Integer) value);
            case OBJECT:
               useCustomWasNull = true;
               wasCustomNull = value == null;
               return (String) value;
            default:
               throw new InternalException("ValueType not supported: " + valueDscr.getFirst());
         }
      }
      else
      {
         return resultSet.getString(lookupIndexbyIndex(columnIndex));
      }
   }

   public String getString(String columnName) throws SQLException
   {
      useCustomWasNull = false;
      
      return resultSet.getString(lookupIndexbyName(columnName));
   }

   public int findColumn(String columnName) throws SQLException
   {
      int idx = lookupIndexbyName(columnName);
      
      if (0 > idx)
      {
         trace.warn("Column " + columnName + " with alias " + tableDescr.getTableAlias()
               + " does not exist. Forcing Exception.");
         resultSet.findColumn(columnName + "__FORCE_EXCEPTION");
      }
      
      return idx;
   }

   public boolean relative( int rows ) throws SQLException
   {
      return resultSet.relative(rows);
   }

   // jdk 1.4 methods
   // noops as long as we compile against jdk 1.3

   public URL getURL(int columnIndex) throws SQLException
   {
      return null;
      //return resultSet.getURL(lookupIndexbyIndex(columnIndex));
   }

   public URL getURL(String columnName) throws SQLException
   {
      return null;
      //return resultSet.getURL(lookupIndexbyName(columnName));
   }

   public void updateArray(int columnIndex, Array x) throws SQLException
   {
      //resultSet.updateArray(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateArray(String columnName, Array x) throws SQLException
   {
      //resultSet.updateArray(lookupIndexbyName(columnName), x);
   }

   public void updateBlob(int columnIndex, Blob x) throws SQLException
   {
      //resultSet.updateBlob(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateBlob(String columnName, Blob x) throws SQLException
   {
      //resultSet.updateBlob(lookupIndexbyName(columnName), x);
   }

   public void updateClob(int columnIndex, Clob x) throws SQLException
   {
      //resultSet.updateClob(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateClob(String columnName, Clob x) throws SQLException
   {
      //resultSet.updateClob(lookupIndexbyName(columnName), x);
   }

   public void updateRef(int columnIndex, Ref x) throws SQLException
   {
      //resultSet.updateRef(lookupIndexbyIndex(columnIndex), x);
   }

   public void updateRef(String columnName, Ref x) throws SQLException
   {
      //resultSet.updateRef(lookupIndexbyName(columnName), x);
   }
   
   public static enum ValueType
   {
      LOCAL_RS_INDEX,
      GLOBAL_RS_INDEX,
      OBJECT
   }
}
