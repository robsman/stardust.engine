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
package org.eclipse.stardust.engine.api.ejb2.beans;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.ejb.SessionBeanInvocationManager;

/**
 * @author ubirkemeyer
 * @version $Revision: 28033 $
 */
public abstract class AbstractEjbServiceImpl implements SessionBean
{
   private static final Logger trace = LogManager.getLogger(AbstractEjbServiceImpl.class);

   private static final long serialVersionUID = 1L;

   protected SessionContext sessionContext;

   private InvocationManager invocationManager;

   private String serviceTypeName;
   protected Object service;

   public void init(Class serviceType, Class serviceImplType)
   {
      init(serviceType, serviceImplType, false);
   }

   public void init(Class serviceType, Class serviceImplType, boolean stateless)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("ejbCreate: " + this);
      }

      this.serviceTypeName = serviceType.getName();

      prepareInvocationManager(serviceImplType);
      setupServiceProxy();
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      this.sessionContext = ctx;
   }

   public void ejbRemove() throws EJBException
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("ejbRemove: " + this);
      }

      service = null;
      invocationManager = null;
   }

   public void ejbActivate() throws EJBException
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("ejbActivate: " + this);
      }

      // restoring invocation proxy on activation
      setupServiceProxy();
      // restoring the invocationManager on activation may require a restore of stateful
      // authentication information, too
   }

   public void ejbPassivate() throws EJBException
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("ejbPassivate: " + this);
      }

      // remove invocation proxy to prevent passivation problems
      service = null;
   }

   private void prepareInvocationManager(Class serviceImplType)
   {
      Object serviceInstance = null;
      try
      {
         serviceInstance = serviceImplType.newInstance();
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
      invocationManager = new SessionBeanInvocationManager(sessionContext,
            null, serviceInstance, serviceTypeName);
   }

   private void setupServiceProxy()
   {
      ClassLoader classLoader = getClass().getClassLoader();
      try
      {
         Class serviceType = classLoader.loadClass(serviceTypeName);
         this.service = Proxy.newProxyInstance(classLoader, new Class[] {
               serviceType, ManagedService.class}, invocationManager);
      }
      catch (ClassNotFoundException e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_FAILED_LOADING_SERVICE_INTERFACE_CLASS.raise(), e);
      }
   }
}
