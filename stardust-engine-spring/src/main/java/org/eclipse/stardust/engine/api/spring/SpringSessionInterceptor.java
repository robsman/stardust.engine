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
package org.eclipse.stardust.engine.api.spring;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeActivityThreadContext;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.AuditTrailPropertiesInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SpringSessionInterceptor extends AuditTrailPropertiesInterceptor
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(SpringSessionInterceptor.class);

   private final AbstractSpringServiceBean serviceBean;

   public SpringSessionInterceptor(String sessionName,
         AbstractSpringServiceBean serviceBean)
   {
      super(sessionName);
      this.serviceBean = serviceBean;
   }

   public Object invoke(final MethodInvocation invocation) throws Throwable
   {
      if (invocation.getParameters().getBoolean("Carnot.Engine.Tuning.Spring.DeferJdbcConnectionRetrieval", false))
      {
         DeferredConnectionDataSourceAdapter deferredConnectionDs = new DeferredConnectionDataSourceAdapter(
               serviceBean.getDataSource());
         try
         {
            return doWithDataSource(invocation, deferredConnectionDs);
         }
         finally
         {
            deferredConnectionDs.maybeReleaseConnection();
         }
      }
      else
      {
         // eagerly retrieve a JDBC connection ...
         return new JdbcTemplate(serviceBean.getDataSource()).execute(new ConnectionCallback()
         {
            public Object doInConnection(Connection con) throws SQLException,
                  DataAccessException
            {
               // ... and hand it over for use by the engine
               return doWithDataSource(invocation, new EagerConnectionSourceAdapter(con));
            }
         });
      }
   }
   
   private Object doWithDataSource(final MethodInvocation invocation, DataSource dataSource)
         throws SQLException, Error
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      
      try
      {
         PropertyLayer props = ParametersFacade.pushLayer(Collections.EMPTY_MAP);
         
         props.setProperty("jdbc/" + sessionName + SessionFactory.DS_DATA_SOURCE_SUFFIX,
               dataSource);
         
         if (null != serviceBean.getJcaResourceProvider())
         {
            rtEnv.setJcaResourceProvider(serviceBean.getJcaResourceProvider());
         }
         
         if (null != serviceBean.getJmsResourceProvider())
         {
            rtEnv.setJmsResourceProvider(serviceBean.getJmsResourceProvider());
         }
         
         Session session = SessionFactory.createSession(sessionName);
         
         rtEnv.setAuditTrailSession(session);
         rtEnv.setActivityThreadContext(new RuntimeActivityThreadContext());
         
         props.setProperty(sessionName + SessionProperties.DS_SESSION_SUFFIX,
               rtEnv.getAuditTrailSession());
         
         if (null == session)
         {
            throw new PublicException("Missing data source for '" + sessionName + "'");
         }
         
         props.setProperty("ActivityThread.Context", rtEnv.getActivityThreadContext());
         
         Map auditTrailProperties = null;
         try
         {
            auditTrailProperties = getAuditTrailProperties(invocation.getParameters());
            if (auditTrailProperties != null)
            {
               ParametersFacade.pushLayer(auditTrailProperties);
            }
            
            try
            {
               session.postBindingInitialization();
               Object result = invocation.proceed();
               session.flush();
               
               return result;
            }
            catch (SQLException e)
            {
               serviceBean.getTransactionManager()
                     .getTransaction(null)
                     .setRollbackOnly();
               throw e;
            }
            catch (RuntimeException e)
            {
               serviceBean.getTransactionManager()
                     .getTransaction(null)
                     .setRollbackOnly();
               throw e;
            }
            catch (Error e)
            {
               serviceBean.getTransactionManager()
                     .getTransaction(null)
                     .setRollbackOnly();
               throw e;
            }
            catch (Throwable e)
            {
               serviceBean.getTransactionManager()
                     .getTransaction(null)
                     .setRollbackOnly();
               throw new PublicException("", e);
            }
            finally
            {
               try
               {
                  session.disconnect();
               }
               catch (SQLException e)
               {
                  serviceBean.getTransactionManager()
                        .getTransaction(null)
                        .setRollbackOnly();
                  trace.warn("Failed disconnecting session.", e);
                  throw e;
               }
            }
         }
         finally
         {
            if (auditTrailProperties != null)
            {
               ParametersFacade.popLayer();
            }
         }
      }
      finally
      {
         ParametersFacade.popLayer();
         
         rtEnv.setActivityThreadContext(null);
         rtEnv.setAuditTrailSession(null);
      }
   }

   private static final class EagerConnectionSourceAdapter implements DataSource
   {
      private final Connection connection;

      public EagerConnectionSourceAdapter(Connection connection)
      {
         this.connection = connection;
      }

      /**
       * @category Spring integration
       */
      public Connection getConnection() throws SQLException
      {
         // returned connection will ignore calls to close, as connection will be closed
         // by JdbcTemplate code
         return connection;
      }

      public Connection getConnection(String arg0, String arg1) throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public int getLoginTimeout() throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public PrintWriter getLogWriter() throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public void setLoginTimeout(int arg0) throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public void setLogWriter(PrintWriter arg0) throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public <T> T unwrap(Class<T> iface) throws SQLException
      {
         // TODO Auto-generated method stub
         return null;
      }

      public boolean isWrapperFor(Class< ? > iface) throws SQLException
      {
         // TODO Auto-generated method stub
         return false;
      }
   }

   private static final class DeferredConnectionDataSourceAdapter
         implements DataSource, InvocationHandler
   {
      private final DataSource dataSource;

      private Connection connection;

      private Connection wrappedConnection;

      public DeferredConnectionDataSourceAdapter(DataSource dataSource)
      {
         this.dataSource = dataSource;
      }
      
      public void maybeReleaseConnection()
      {
         if (null != connection)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Closing JDBC connection " + connection);
            }
            this.wrappedConnection = null;
            DataSourceUtils.releaseConnection(connection, dataSource);
            this.connection = null;
         }
      }

      /**
       * @category Spring integration
       */
      public Connection getConnection() throws SQLException
      {
         if (null == connection)
         {
            this.connection = DataSourceUtils.getConnection(dataSource);

            this.wrappedConnection = (Connection) Proxy.newProxyInstance(
                  getClass().getClassLoader(), new Class[] {Connection.class}, this);

            if (trace.isDebugEnabled())
            {
               trace.debug("Retrieved JDBC connection " + connection);
            }
         }

         return wrappedConnection;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         if ("close".equals(method.getName()))
         {
            // ignore close operation, will this will be performed later
            if (trace.isDebugEnabled())
            {
               trace.debug("Deferring close of JDBC connection " + connection);
            }
            return null;
         }
         else if ("equals".equals(method.getName()))
         {
            return ((proxy == args[0]) ? Boolean.TRUE : Boolean.FALSE);
         }
         else if ("hashCode".equals(method.getName()))
         {
            return System.identityHashCode(proxy);
         }
         else
         {
            try
            {
               return method.invoke(this.connection, args);
            }
            catch (InvocationTargetException ite)
            {
               throw ite.getTargetException();
            }
         }
      }

      public Connection getConnection(String arg0, String arg1) throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public int getLoginTimeout() throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public PrintWriter getLogWriter() throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public void setLoginTimeout(int arg0) throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public void setLogWriter(PrintWriter arg0) throws SQLException
      {
         throw new UnsupportedOperationException(
               "This Spring DataSource adapter is not intended to be used this way.");
      }

      public <T> T unwrap(Class<T> iface) throws SQLException
      {
         // TODO Auto-generated method stub
         return null;
      }

      public boolean isWrapperFor(Class< ? > iface) throws SQLException
      {
         // TODO Auto-generated method stub
         return false;
      }
   }
}
