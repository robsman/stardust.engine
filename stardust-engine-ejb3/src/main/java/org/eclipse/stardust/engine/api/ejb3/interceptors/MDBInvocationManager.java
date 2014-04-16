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
package org.eclipse.stardust.engine.api.ejb3.interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.MessageDrivenContext;

import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.api.ejb3.beans.Ejb3Service;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class MDBInvocationManager extends InvocationManager
{
   private static final long serialVersionUID = 1L;

   public MDBInvocationManager(String serviceName, Ejb3Service serviceInstance,
         MessageDrivenContext context, int nRetries, int msPause, boolean rollbackOnError)
   {
      super(serviceInstance, setupInterceptors(serviceName, serviceInstance, context, nRetries, msPause,
            rollbackOnError));
   }
   
   private static List setupInterceptors(String serviceName, Ejb3Service serviceBean,
         MessageDrivenContext context, int nRetries, int msPause, boolean rollbackOnError)
   {
      List interceptors = new ArrayList();

      // apply multiple try before exception handling to avoid premature forced rollback
      interceptors.add(new PropertyLayerProviderInterceptor());
		interceptors.add(new ContainerConfigurationInterceptor(serviceName,
				J2eeContainerType.EJB, serviceBean.getForkingService()));
      interceptors.add(new MDBExceptionHandler(context, rollbackOnError));
      interceptors.add(new MultipleTryInterceptor(nRetries, msPause));
      interceptors.add(new CallingInterceptor());
      
      return interceptors;
   }
}
