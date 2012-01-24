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

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.ejb.EjbProperties;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SpringConfigurationInterceptor implements MethodInterceptor
{

   private static final long serialVersionUID = 1L;

   public static final Logger trace = LogManager.getLogger(SpringConfigurationInterceptor.class);

   private final String serviceName;

   private final ISpringServiceBean serviceBean;

   private final J2eeContainerType type;

   public SpringConfigurationInterceptor(String serviceName,
         ISpringServiceBean serviceBean)
   {
      this(serviceName, serviceBean, J2eeContainerType.POJO);
   }

   public SpringConfigurationInterceptor(String serviceName,
         ISpringServiceBean serviceBean, J2eeContainerType type)
   {
      this.serviceName = serviceName;
      this.serviceBean = serviceBean;
      this.type = type;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      ForkingServiceFactory fsFactory = serviceBean.getForkingServiceFactory();
      if (null == fsFactory)
      {
         fsFactory = new ForkingServiceSpringBeanFactory(type,
               serviceBean.getBeanFactory());
      }

      Map locals = new HashMap();

      locals.put(SpringConstants.PRP_APPLICATION_CONTEXT,
            serviceBean.getApplicationContext());
      locals.put(SpringConstants.PRP_TX_MANAGER, serviceBean.getTransactionManager());

      locals.put(EngineProperties.FORKING_SERVICE_HOME, fsFactory);

      locals.put(EjbProperties.CONTAINER_TYPE, type);

      try
      {
         ParametersFacade.pushLayer(invocation.getParameters(), locals);

         // TODO what is the equivalent in spring?
         Context environment = null;
         if (type != J2eeContainerType.POJO)
         {
            InitialContext ic = new InitialContext();
            environment = (Context) ic.lookup("java:comp/env");
         }

         try
         {
            if (null != environment)
            {
               ParametersFacade.pushContext(invocation.getParameters(), environment, serviceName);
            }

            // TODO pass application context
            
            return invocation.proceed();
         }
         finally
         {
            if (null != environment)
            {
               ParametersFacade.popContext(invocation.getParameters());
            }
         }
      }
      finally
      {
         ParametersFacade.popLayer(invocation.getParameters());
      }
   }
}
