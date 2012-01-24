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
package org.eclipse.stardust.engine.api.ejb2.beans.interceptors;

import java.util.Map;

import javax.ejb.EJBContext;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.ejb2.beans.EjbTxPolicy;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.EjbDocumentRepositoryService;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeActivityThreadContext;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.AuditTrailPropertiesInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CMTSessionInterceptor extends AuditTrailPropertiesInterceptor
      implements ITransactionStatus
{
   
   private static final long serialVersionUID = 1L;

   private static final String KEY_AUDIT_TRAIL_SESSION = SessionProperties.DS_NAME_AUDIT_TRAIL
         + SessionProperties.DS_SESSION_SUFFIX;
   
   private final EjbTxPolicy ejbTxPolicy;

   private final EJBContext context;
   
   private transient EjbDocumentRepositoryService dmsServiceProvider;
   
   public CMTSessionInterceptor(String sessionName, EJBContext context)
   {
      this(sessionName, context, null);
   }

   public CMTSessionInterceptor(String sessionName, EJBContext context,
         EjbTxPolicy ejbTxPolicy)
   {
      super(sessionName);

      this.ejbTxPolicy = ejbTxPolicy;
      this.context = context;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      Session session = SessionFactory.createContainerSession(sessionName);

      if (null == session)
      {
         throw new PublicException("Missing data source for '" + sessionName
               + "'");
      }
      
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      rtEnv.setActivityThreadContext(new RuntimeActivityThreadContext());
      rtEnv.setAuditTrailSession(session);

      // repository will be retrieved from bean local JNDI location java:comp/env/jcr/ContentRepository
      Object contentRepositoryRes = invocation.getParameters().get("jcr/ContentRepository");
      if (null != contentRepositoryRes)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Retrieved JCR repository from JNDI: " + contentRepositoryRes);
         }

         if (null == dmsServiceProvider)
         {
            this.dmsServiceProvider = new EjbDocumentRepositoryService();
         }
         dmsServiceProvider.setRepository((javax.jcr.Repository) contentRepositoryRes);
         
         // provide DMS service
         rtEnv.setDocumentRepositoryService(dmsServiceProvider);
      }
      
      rtEnv.setProperty("ActivityThread.Context", rtEnv.getActivityThreadContext());

      final String keySessionProperty = SessionProperties.DS_NAME_AUDIT_TRAIL.equals(sessionName)
            ? KEY_AUDIT_TRAIL_SESSION
            : sessionName + SessionProperties.DS_SESSION_SUFFIX;
      rtEnv.setProperty(keySessionProperty, session);

      Map auditTrailProperties = null;
      try
      {
         // provide TX status to callees
         TransactionUtils.registerTxStatus(rtEnv, this);
         
         auditTrailProperties = getAuditTrailProperties(invocation.getParameters());
         if (null != auditTrailProperties)
         {
            ParametersFacade.pushLayer(auditTrailProperties);
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
         if ((null == ejbTxPolicy) || ejbTxPolicy.mustRollback(invocation, e))
         {
            context.setRollbackOnly();
         }

         throw e;
      }
      finally
      {
         if (auditTrailProperties != null)
         {
            ParametersFacade.popLayer(invocation.getParameters());
         }

         rtEnv.setActivityThreadContext(null);
         rtEnv.setAuditTrailSession(null);
         rtEnv.setDocumentRepositoryService(null);
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
}
