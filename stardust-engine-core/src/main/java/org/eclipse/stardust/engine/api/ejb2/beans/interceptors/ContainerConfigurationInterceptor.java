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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.eclipse.stardust.common.config.ContextCache;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.api.ejb2.beans.RemoteSessionForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.POJOForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ContainerConfigurationInterceptor implements MethodInterceptor
{
   public static final Logger trace = LogManager.getLogger(ContainerConfigurationInterceptor.class);
   
   private static final RemoteSessionForkingServiceFactory EJB_FORKING_SERVICE_FACTORY = new RemoteSessionForkingServiceFactory();

   private final String serviceName;
   private final J2eeContainerType type;

   public ContainerConfigurationInterceptor(String serviceName, J2eeContainerType type)
   {
      this.serviceName = serviceName;
      this.type = type;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      
      rtEnv.setProperty(EngineProperties.FORKING_SERVICE_HOME,
            type == J2eeContainerType.EJB
                  ? (ForkingServiceFactory) EJB_FORKING_SERVICE_FACTORY
                  : new POJOForkingServiceFactory(type));
      rtEnv.setProperty("Engine.ContainerType", type);

      try
      {
         if (type != J2eeContainerType.POJO)
         {
            ContextCache cachedContext = ParametersFacade.getCachedContext(
                  invocation.getParameters(), serviceName);
            
            if (null != cachedContext)
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
