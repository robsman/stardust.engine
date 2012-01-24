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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeActivityThreadContext;
import org.eclipse.stardust.engine.core.runtime.interceptor.AuditTrailPropertiesInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOSessionInterceptor extends AuditTrailPropertiesInterceptor
      implements ITransactionStatus
{
   private static final Logger trace = LogManager.getLogger(POJOSessionInterceptor.class);
   
   public static final String RESOURCES = "managed.resources";

   public POJOSessionInterceptor(String sessionName)
   {
      super(sessionName);
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {

      Session session = SessionFactory.createSession(sessionName);
      
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      rtEnv.setAuditTrailSession(session);
      rtEnv.setActivityThreadContext(new RuntimeActivityThreadContext());
      
      Map locals = CollectionUtils.newMap();
      locals.put(sessionName + SessionProperties.DS_SESSION_SUFFIX, rtEnv.getAuditTrailSession());
      locals.put("ActivityThread.Context", rtEnv.getActivityThreadContext());

      List managedResources = new ArrayList(2);
      locals.put(RESOURCES, managedResources);

      List ds = invocation.getParameters().getStrings("Orchestrated.DataSources");
      for (Iterator i = ds.iterator(); i.hasNext();)
      {
         String dsName = (String) i.next();
         DataSource managedDs = SessionFactory.obtainDataSource(dsName);
         managedResources.add(new ManagedDataSource(managedDs));
      }

      Map auditTrailProperties = null;
      try
      {
         PropertyLayer localProps = ParametersFacade.pushLayer(
               invocation.getParameters(), locals);
         
         TransactionUtils.registerTxStatus(localProps, this);

         auditTrailProperties = getAuditTrailProperties(invocation.getParameters());
         if (auditTrailProperties != null)
         {
            ParametersFacade.pushLayer(auditTrailProperties);
         }

         session.postBindingInitialization();
         Object result = invocation.proceed();
         session.save();

         for (Iterator i = managedResources.iterator(); i.hasNext();)
         {
            ManagedResource res = (ManagedResource) i.next();
            res.commit();
         }

         return result;
      }
      catch (Throwable e)
      {
         session.rollback();

         for (Iterator i = managedResources.iterator(); i.hasNext();)
         {
            ManagedResource res = (ManagedResource) i.next();
            res.rollback();
         }

         throw e;
      }
      finally
      {
         if (auditTrailProperties != null)
         {
            ParametersFacade.popLayer(invocation.getParameters());
         }
         ParametersFacade.popLayer(invocation.getParameters());
         
         rtEnv.setActivityThreadContext(null);
         rtEnv.setAuditTrailSession(null);
      }
   }

   public boolean isRollbackOnly()
   {
      // TODO implement (probably based on connection status)
      
      return false;
   }

   public void setRollbackOnly()
   {
      // TODO implement (probably based on connection status)
      trace.warn("Forced TX rollbacks are not supported in POJO deployments.");
   }

   public Object getTransaction()
   {
      // Transaction object is unknown for POJO scenario
      return null;
   }
   
}
