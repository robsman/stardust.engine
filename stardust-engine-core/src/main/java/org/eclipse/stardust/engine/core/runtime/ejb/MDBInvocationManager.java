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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.MessageDrivenContext;

import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.ContainerConfigurationInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.MDBExceptionHandler;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;

/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class MDBInvocationManager extends InvocationManager
{
   private static final long serialVersionUID = 1L;

   public MDBInvocationManager(String serviceName, Object serviceInstance,
         MessageDrivenContext context, int nRetries, int msPause, boolean rollbackOnError)
   {
      super(serviceInstance, setupInterceptors(serviceName, serviceInstance, context,
            nRetries, msPause, rollbackOnError));
   }

   private static List<MethodInterceptor> setupInterceptors(String serviceName, Object serviceBean,
         MessageDrivenContext context, int nRetries, int msPause, boolean rollbackOnError)
   {
      List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>();

      // apply multiple try before exception handling to avoid premature forced rollback
      interceptors.add(new PropertyLayerProviderInterceptor());
      interceptors.add(new ContainerConfigurationInterceptor(serviceName,
            serviceBean instanceof Ejb3ManagedService ? ((Ejb3ManagedService) serviceBean).getForkingService() : null));
      interceptors.add(new MDBExceptionHandler(context, rollbackOnError));
      interceptors.add(new MultipleTryInterceptor(nRetries, msPause));
      interceptors.add(new CallingInterceptor());

      return interceptors;
   }
}
