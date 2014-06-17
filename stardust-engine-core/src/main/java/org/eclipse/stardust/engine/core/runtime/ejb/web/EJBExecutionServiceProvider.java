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
package org.eclipse.stardust.engine.core.runtime.ejb.web;

import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet.ExecutionServiceProvider;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.ejb.Ejb2ExecutorService;
import org.eclipse.stardust.engine.core.runtime.ejb.RemoteSessionForkingServiceFactory;

public class EJBExecutionServiceProvider implements ExecutionServiceProvider
{
   public static final String EJB_CONTEXT = "ejb";

   public ForkingService getExecutionService(String clientContext)
   {
      ForkingService forkingService = null;
      if (EJB_CONTEXT.equals(clientContext))
      {
         ForkingServiceFactory factory = new RemoteSessionForkingServiceFactory(new Ejb2ExecutorService());
         forkingService = factory.get();
      }
      return forkingService;
   }
}
