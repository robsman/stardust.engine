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
package org.eclipse.stardust.engine.spring.integration.jms.threading;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.spring.ISpringServiceBean;
import org.eclipse.stardust.engine.api.spring.SpringConfigurationInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * @author sauer
 * @version $Revision: $
 */
public class MessageHandlingInvocationManager extends InvocationManager
{

   private static final long serialVersionUID = 1L;

   public MessageHandlingInvocationManager(ISpringServiceBean serviceBean,
         String serviceName, Object serviceInstance, //
         int nRetries, int msPause, boolean rollbackOnError)
   {
      super(serviceInstance, setupInterceptors(serviceBean, serviceName, nRetries,
            msPause, rollbackOnError));
   }
   
   private static List setupInterceptors(ISpringServiceBean serviceBean,
         String serviceName, //
         int nRetries, int msPause, boolean rollbackOnError)
   {
      List interceptors = CollectionUtils.newArrayList(5);

      // apply multiple try before exception handling to avoid premature forced rollback
      interceptors.add(new PropertyLayerProviderInterceptor());
      interceptors.add(new SpringConfigurationInterceptor(serviceName, serviceBean));
      interceptors.add(new MessageHandlingExceptionInterceptor(rollbackOnError));
      interceptors.add(new MultipleTryInterceptor(nRetries, msPause));
      interceptors.add(new CallingInterceptor());
      
      return interceptors;
   }
}
