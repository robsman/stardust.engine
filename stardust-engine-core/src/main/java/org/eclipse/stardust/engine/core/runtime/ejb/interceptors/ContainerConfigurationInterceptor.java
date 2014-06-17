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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.eclipse.stardust.common.config.ContextCache;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.POJOForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;
import org.eclipse.stardust.engine.core.runtime.ejb.RemoteSessionForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class ContainerConfigurationInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   private static final RemoteSessionForkingServiceFactory EJB2_FORKING_SERVICE_FACTORY = new RemoteSessionForkingServiceFactory(null);

   private ForkingServiceFactory forkingServiceFactory;

   private String serviceName;
   private J2eeContainerType type;

   public ContainerConfigurationInterceptor(String serviceName, ExecutorService serviceBean)
   {
      this(serviceName, J2eeContainerType.EJB, serviceBean);
   }

   public ContainerConfigurationInterceptor(String serviceName, J2eeContainerType type)
   {
      this(serviceName, type, null);
   }

   public ContainerConfigurationInterceptor(String serviceName, J2eeContainerType type, ExecutorService serviceBean)
   {
      this.serviceName = serviceName;
      this.type = type;
      forkingServiceFactory = type == J2eeContainerType.EJB
            ? serviceBean == null ? EJB2_FORKING_SERVICE_FACTORY : new RemoteSessionForkingServiceFactory(serviceBean)
            : new POJOForkingServiceFactory(type);
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      rtEnv.setProperty(EngineProperties.FORKING_SERVICE_HOME, forkingServiceFactory);
      rtEnv.setProperty("Engine.ContainerType", type);

      try
      {
         if (type != J2eeContainerType.POJO)
         {
            ContextCache cachedContext = ParametersFacade.getCachedContext(invocation.getParameters(), serviceName);
            if (cachedContext != null)
            {
               ParametersFacade.pushContext(invocation.getParameters(), cachedContext);
            }
            else
            {
               InitialContext ic = new InitialContext();
               Context environment = (Context) ic.lookup("java:comp/env");
               ParametersFacade.pushContext(invocation.getParameters(), environment, serviceName);
            }
         }
         return invocation.proceed();
      }
      finally
      {
         if (type != J2eeContainerType.POJO)
         {
            ParametersFacade.popContext(invocation.getParameters());
         }
      }
   }
}
