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
package org.eclipse.stardust.engine.core.runtime.ejb3.web;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet.ExecutionServiceProvider;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.ejb.ForkingService;
import org.eclipse.stardust.engine.core.runtime.ejb.RemoteSessionForkingServiceFactory;

public class EJBExecutionServiceProvider implements ExecutionServiceProvider
{
   public static final String EJB_CONTEXT = "ejb";

   private Logger trace = LogManager.getLogger(this.getClass());

   public org.eclipse.stardust.engine.core.runtime.beans.ForkingService getExecutionService(String clientContext)
   {
      org.eclipse.stardust.engine.core.runtime.beans.ForkingService forkingService = null;
      if (EJB_CONTEXT.equals(clientContext))
      {
         try
         {
            ForkingService fs = (ForkingService) new InitialContext()
                  .lookup("java:app/carnot-ejb3/ForkingServiceImpl!"
                        + ForkingService.class.getName());
            ForkingServiceFactory factory = new RemoteSessionForkingServiceFactory(fs);
            forkingService = factory.get();
         }
         catch (NamingException e)
         {
            trace.error(e);
         }
      }
      return forkingService;
   }
}
