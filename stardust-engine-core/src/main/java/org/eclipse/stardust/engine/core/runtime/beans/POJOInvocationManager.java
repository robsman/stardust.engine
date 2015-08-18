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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.*;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.ContainerConfigurationInterceptor;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOInvocationManager extends InvocationManager
{
   private static final long serialVersionUID = 1L;

   public POJOInvocationManager(Object service, String serviceName)
   {
      super(service, setupInterceptors(serviceName));
   }

   private static List setupInterceptors(String serviceName)
   {
      List interceptors = new ArrayList();

      interceptors.add(new DebugInterceptor());
      interceptors.add(new PropertyLayerProviderInterceptor());
      interceptors.add(new ContainerConfigurationInterceptor(serviceName, J2eeContainerType.POJO));
      interceptors.add(new POJOForkingInterceptor());
      interceptors.add(new POJOSessionInterceptor("AuditTrail"));
      interceptors.add(new LoginInterceptor());
      interceptors.add(new GuardingInterceptor(serviceName));
      interceptors.add(new RuntimeExtensionsInterceptor());
      interceptors.add(new POJOExceptionHandler());
      interceptors.add(new CallingInterceptor());

      return interceptors;
   }
}
