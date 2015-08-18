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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.TxRollbackPolicy;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.DebugInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.GuardingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.RuntimeExtensionsInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.CMTSessionInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.ContainerConfigurationInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.SessionBeanExceptionHandler;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.SessionBeanLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;

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

      TxRollbackPolicy txRollbackPolicy = new TxRollbackPolicy(serviceName)
      {
         private static final long serialVersionUID = 1L;

         @Override
         protected Mode determineRollbackOnErrorMode(MethodInvocation invocation)
         {
            // backwards compatibility: check EJB specific property first ...
            String rollbackOnErrorMode = invocation.getParameters().getString(
                  KernelTweakingProperties.EJB_ROLLBACK_ON_ERROR);
            if (isEmpty(rollbackOnErrorMode))
            {
               // .. but of not set, fall back to common property
               return super.determineRollbackOnErrorMode(invocation);
            }
            else
            {
               return fromConfig(rollbackOnErrorMode);
            }
         }
      };

      interceptors.add(new DebugInterceptor());
      interceptors.add(new PropertyLayerProviderInterceptor(false));
      interceptors.add(new ContainerConfigurationInterceptor(serviceName,
            serviceBean == null ? new Ejb2ExecutorService() : serviceBean.getForkingService()));
      interceptors.add(new CMTSessionInterceptor(SessionProperties.DS_NAME_AUDIT_TRAIL,
            context, serviceBean, txRollbackPolicy));
      interceptors.add(new SessionBeanLoginInterceptor(context, serviceBean == null));
      interceptors.add(new GuardingInterceptor(serviceName));
      interceptors.add(new RuntimeExtensionsInterceptor());
      interceptors.add(new SessionBeanExceptionHandler(context, txRollbackPolicy));
      interceptors.add(new CallingInterceptor());

      return interceptors;
   }
}
