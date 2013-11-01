/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;



/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class DbmsAwareRuntimeUpgradeJob extends RuntimeUpgradeJob
{
   // SQL constants
   protected static final String UPDATE = "UPDATE ";
   protected static final String SET = " SET ";
   protected static final String INSERT_INTO = "INSERT INTO ";
   protected static final String VALUES = " VALUES ";
   protected static final String SELECT = "SELECT ";
   protected static final String SELECT_DISTINCT = "SELECT DISTINCT ";
   protected static final String FROM = " FROM ";
   protected static final String INNER_JOIN = " INNER JOIN ";
   protected static final String ON = " ON ";
   protected static final String WHERE = " WHERE ";
   protected static final String EXISTS = " EXISTS ";
   protected static final String IS_NULL = " IS NULL";
   protected static final String PLACEHOLDER = "?";
   protected static final String EQUAL_PLACEHOLDER = " = " + PLACEHOLDER;
   protected static final String NOT_EQUAL_PLACEHOLDER = " != " + PLACEHOLDER;
   protected static final String EQUALS = " = ";
   protected static final String AND = " AND ";
   protected static final String OR = " OR ";
   protected static final String COMMA = ",";
   protected static final String DOT = ".";
   protected static final String SPACE = " ";
   protected static final String QUOTE = "'";
   protected static final Object ORDER_BY = " ORDER BY ";

   protected static class ConnectionWrapper implements DataSource
   {
      Connection connection;

      protected ConnectionWrapper(Connection connection)
      {
         this.connection = connection;
      }

      public Connection getConnection() throws SQLException
      {
         return connection;
      }

      public Connection getConnection(String username, String password)
            throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public int getLoginTimeout() throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public PrintWriter getLogWriter() throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public void setLoginTimeout(int seconds) throws SQLException
      {
         throw new UnsupportedOperationException();
      }

      public void setLogWriter(PrintWriter out) throws SQLException
      {
         throw new UnsupportedOperationException();
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

   private final DBMSKey[] supportedDbms;

   protected DbmsAwareRuntimeUpgradeJob(DBMSKey[] dbms)
   {
      this.supportedDbms = dbms;
   }

   protected Set<PartitionInfo> getPartitionsFromDb()
   {
      Set<PartitionInfo> partitions = CollectionUtils.newSet();

      PreparedStatement selectRowsStmt = null;
      try
      {
         String partitionTableName = DatabaseHelper
               .getQualifiedName(AuditTrailPartitionBean.TABLE_NAME);

         StringBuffer selectCmd = new StringBuffer() //
               .append(SELECT)
               .append(AuditTrailPartitionBean.FIELD__OID).append(", ") //
               .append(AuditTrailPartitionBean.FIELD__ID)
               .append(FROM).append(partitionTableName);

         Connection connection = item.getConnection();

         selectRowsStmt = connection.prepareStatement(selectCmd.toString());

         ResultSet pendingRows = null;
         try
         {
            pendingRows = selectRowsStmt.executeQuery();
            while (pendingRows.next())
            {
               final long partitionOid = pendingRows
                     .getLong(AuditTrailPartitionBean.FIELD__OID);
               final String partitionId = pendingRows
                     .getString(AuditTrailPartitionBean.FIELD__ID);
               partitions.add(new PartitionInfo(partitionOid, partitionId));
            }
         }
         finally
         {
            QueryUtils.closeResultSet(pendingRows);
         }
      }
      catch (SQLException e)
      {
         reportExeption(e, "Could not update double value.");
      }
      finally
      {
         QueryUtils.closeStatement(selectRowsStmt);
      }

      return partitions;
   }

   final protected static class PartitionInfo
   {
      private final long oid;

      private final String id;

      public PartitionInfo(long oid, String id)
      {
         super();
         this.oid = oid;
         this.id = id;
      }

      public long getOid()
      {
         return oid;
      }

      public String getId()
      {
         return id;
      }

      @Override
      public boolean equals(Object obj)
      {
         if ( !(obj instanceof PartitionInfo))
         {
            return false;
         }

         PartitionInfo thatPartInfo = (PartitionInfo) obj;
         return getOid() == thatPartInfo.getOid();
      }

      @Override
      public int hashCode()
      {
         // TODO Auto-generated method stub
         return super.hashCode();
      }
   }

   protected void assertCompatibility() throws UpgradeException
   {
      boolean isSupported = false;
      for (int i = 0; i < supportedDbms.length; i++)
      {
         isSupported |= supportedDbms[i].equals(item.getDbDescriptor().getDbmsKey());
      }

      if (!isSupported)
      {
         throw new UpgradeException("The runtime upgrade job for version "
               + getVersion()
               + " is only valid for the following DBMSs: "
               + StringUtils.join(new TransformingIterator(Arrays.asList(supportedDbms)
                     .iterator(), new Functor()
               {
                  public Object execute(Object source)
                  {
                     return ((DBMSKey) source).getName();
                  }
               })
               {
               }, ", ") + ".");
      }
   }

   abstract protected Logger getLogger();

   protected void reportExeption(SQLException sqle, String message)
   {
      final Logger logger = getLogger();
      if (logger != null)
      {
         SQLException ne = sqle;
         do
         {
            logger.error(message, ne);
         }
         while (null != (ne = ne.getNextException()));
      }

      try
      {
         item.rollback();
      }
      catch (SQLException e1)
      {
         warn("Failed rolling back transaction.", e1);
      }
      error("Failed migrating runtime item tables.", sqle);
   }

   protected Map getRtJobEngineProperties()
   {
      Map props = CollectionUtils.newHashMap();
      props.put("jdbc/" + SessionProperties.DS_NAME_AUDIT_TRAIL
            + SessionProperties.DS_DATA_SOURCE_SUFFIX,
            new ConnectionWrapper(item.getConnection()));
      props.put(Constants.FORCE_IMMEDIATE_INSERT_ON_SESSION, Boolean.TRUE);
      return props;
   }
}
