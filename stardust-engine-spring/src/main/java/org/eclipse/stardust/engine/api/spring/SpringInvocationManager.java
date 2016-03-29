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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.config.PropertyProvider;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.TxRollbackPolicy;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.*;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SpringInvocationManager extends InvocationManager implements Serializable
{
   private static final long serialVersionUID = 1L;

   public SpringInvocationManager(AbstractSpringServiceBean serviceBean,
         Object serviceInstance, String serviceName)
   {
      super(serviceInstance, setupInterceptors(serviceBean, serviceName));
   }

   private static List setupInterceptors(final AbstractSpringServiceBean serviceBean,
         String serviceName)
   {
      List interceptors = new ArrayList(10);

      TxRollbackPolicy txRollbackPolicy = new TxRollbackPolicy(serviceName);

      interceptors.add(new SpringTxInterceptor(serviceBean, txRollbackPolicy));
      interceptors.add(new DebugInterceptor());
      interceptors.add(new PropertyLayerProviderInterceptor(new PropertyProvider()
      {
         public Map getProperties()
         {
            return serviceBean.getCarnotProperties();
         }
         
         public String getPropertyDisplayValue(String key)
         {
            return getProperties().get(key).toString();
         }
      }));
      interceptors.add(new SpringConfigurationInterceptor(serviceName, serviceBean));
      interceptors.add(new SpringSessionInterceptor(SessionFactory.AUDIT_TRAIL,
            serviceBean));

      if ( ForkingService.class.getName().equals(serviceName))
      {
         interceptors.add(new NonInteractiveSecurityContextInterceptor());
      }
      else
      {
         interceptors.add(new SpringBeanLoginInterceptor(serviceBean));
         interceptors.add(new GuardingInterceptor(serviceName));
      }

      interceptors.add(new RuntimeExtensionsInterceptor());
      interceptors.add(new POJOExceptionHandler());
      interceptors.add(new CallingInterceptor());

      return interceptors;
   }
}
