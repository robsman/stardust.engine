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
package org.eclipse.stardust.engine.core.runtime.ejb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.*;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.CMTSessionInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.ContainerConfigurationInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.SessionBeanExceptionHandler;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.SessionBeanLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;

/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class SessionBeanInvocationManager extends InvocationManager implements Serializable
{
   private static final long serialVersionUID = 1L;

   public SessionBeanInvocationManager(SessionContext context,
         Ejb3ManagedService serviceBean, Object serviceInstance,
         String serviceName)
   {
      super(serviceInstance, setupInterceptors(context, serviceBean, serviceName));
   }

   private static List<MethodInterceptor> setupInterceptors(SessionContext context,
         Ejb3ManagedService serviceBean, String serviceName)
   {
      List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>();

      EjbTxPolicy ejbTxPolicy = new EjbTxPolicy(serviceName);

      interceptors.add(new DebugInterceptor());
      interceptors.add(new PropertyLayerProviderInterceptor(false));
      interceptors.add(new ContainerConfigurationInterceptor(serviceName,
            serviceBean == null ? new Ejb2ExecutorService() : serviceBean.getForkingService()));
      interceptors.add(new CMTSessionInterceptor(SessionProperties.DS_NAME_AUDIT_TRAIL,
            context, serviceBean, ejbTxPolicy));
      interceptors.add(new SessionBeanLoginInterceptor(context, serviceBean == null));
      interceptors.add(new GuardingInterceptor(serviceName));
      interceptors.add(new RuntimeExtensionsInterceptor());
      interceptors.add(new SessionBeanExceptionHandler(context, ejbTxPolicy));
      interceptors.add(new CallingInterceptor());

      return interceptors;
   }
}
