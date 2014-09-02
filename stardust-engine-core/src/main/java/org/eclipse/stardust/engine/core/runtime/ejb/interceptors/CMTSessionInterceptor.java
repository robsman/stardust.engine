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
package org.eclipse.stardust.engine.core.runtime.ejb.interceptors;

import java.util.Map;

import javax.ejb.SessionContext;
import javax.sql.DataSource;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeActivityThreadContext;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.Ejb3ManagedService;
import org.eclipse.stardust.engine.core.runtime.ejb.EjbTxPolicy;
import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;
import org.eclipse.stardust.engine.core.runtime.interceptor.AuditTrailPropertiesInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.interceptor.TransactionPolicyAdvisor;

/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class CMTSessionInterceptor extends AuditTrailPropertiesInterceptor
      implements ITransactionStatus, TransactionPolicyAdvisor
{
   private static final long serialVersionUID = 1L;

   private static final String KEY_AUDIT_TRAIL_SESSION = SessionProperties.DS_NAME_AUDIT_TRAIL
         + SessionProperties.DS_SESSION_SUFFIX;

   private final EjbTxPolicy ejbTxPolicy;

   private final SessionContext context;

   private final Ejb3ManagedService serviceBean;

   public CMTSessionInterceptor(String sessionName, SessionContext context)
   {
      this(sessionName, context, null, null);
   }

   public CMTSessionInterceptor(String sessionName, SessionContext context, EjbTxPolicy ejbTxPolicy)
   {
      this(sessionName, context, null, ejbTxPolicy);
   }

   public CMTSessionInterceptor(String sessionName, SessionContext context, ExecutorService serviceBean)
   {
      this(sessionName, context, serviceBean, null);
   }

   public CMTSessionInterceptor(String sessionName, SessionContext context,
         Ejb3ManagedService serviceBean, EjbTxPolicy ejbTxPolicy)
   {
      super(sessionName);

      this.ejbTxPolicy = ejbTxPolicy;
      this.context = context;
      this.serviceBean = serviceBean;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      Session session;
      if (serviceBean != null)
      {
         DataSource ds = serviceBean.getDataSource();
         session = SessionFactory.createSession(sessionName, ds);

         Object contentRepositoryRes = serviceBean.getRepository();
         rtEnv.setProperty(
            Parameters.instance().getString("Jcr.ContentRepository", "jcr/ContentRepository"),
            contentRepositoryRes);
      }
      else
      {
         session = SessionFactory.createContainerSession(sessionName);
      }
      if (null == session)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_MISSING_DATA_SOURCE.raise(sessionName));
      }
      rtEnv.setAuditTrailSession(session);
      final String keySessionProperty = SessionProperties.DS_NAME_AUDIT_TRAIL.equals(sessionName)
            ? KEY_AUDIT_TRAIL_SESSION
            : sessionName + SessionProperties.DS_SESSION_SUFFIX;
      rtEnv.setProperty(keySessionProperty, session);

      rtEnv.setActivityThreadContext(new RuntimeActivityThreadContext());
      rtEnv.setProperty("ActivityThread.Context", rtEnv.getActivityThreadContext());

      // repository will be retrieved from bean local JNDI location
      // java:comp/env/jcr/ContentRepository

      boolean pushedLayer = false;
      try
      {
         // provide TX status to callees
         TransactionUtils.registerTxStatus(rtEnv, this);

         @SuppressWarnings("unchecked")
         Map<String, ?> auditTrailProperties = getAuditTrailProperties(invocation.getParameters());
         if (auditTrailProperties != null)
         {
            ParametersFacade.pushLayer(auditTrailProperties);
            pushedLayer  = true;
         }

         session.postBindingInitialization();

         Object result = invocation.proceed();

         session.flush();
         session.disconnect();

         return result;
      }
      catch (Throwable e)
      {
         session.disconnect();
         if (mustRollback(invocation, e))
         {
            context.setRollbackOnly();
         }

         throw e;
      }
      finally
      {
         if (pushedLayer)
         {
            ParametersFacade.popLayer(invocation.getParameters());
         }

         rtEnv.setActivityThreadContext(null);
         rtEnv.setAuditTrailSession(null);
      }
   }

   public boolean isRollbackOnly()
   {
      return (null != context) ? context.getRollbackOnly() : false;
   }

   public void setRollbackOnly()
   {
      if (null != context)
      {
         context.setRollbackOnly();
      }
   }

   public Object getTransaction()
   {
      // Transaction object is unknown for EJB scenario
      return null;
   }

   @Override
   public boolean mustRollback(MethodInvocation invocation, Throwable e)
   {
      return (null == ejbTxPolicy) || ejbTxPolicy.mustRollback(invocation, e);
   }
}
