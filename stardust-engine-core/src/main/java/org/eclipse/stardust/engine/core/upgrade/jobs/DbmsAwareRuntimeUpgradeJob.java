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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class DbmsAwareRuntimeUpgradeJob extends RuntimeUpgradeJob
{
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

   protected void assertCompatibility() throws UpgradeException
   {
      boolean isSupported = false;
      for (int i = 0; i < supportedDbms.length; i++ )
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
