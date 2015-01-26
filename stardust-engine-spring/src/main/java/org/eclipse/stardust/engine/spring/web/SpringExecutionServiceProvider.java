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
package org.eclipse.stardust.engine.spring.web;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.spring.SpringConstants;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet.ExecutionServiceProvider;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.springframework.context.ApplicationContext;



public class SpringExecutionServiceProvider implements ExecutionServiceProvider
{
   public static final String SPRING_CONTEXT = "spring";
   
   public ForkingService getExecutionService(String clientContext)
   {
      ForkingService forkingService = null;
      
      if (StringUtils.isEmpty(clientContext) || SPRING_CONTEXT.equals(clientContext))
      {
         ApplicationContext appContext = SpringUtils.getWebApplicationContext();
         if (appContext == null)
         {
            // should never happen
            appContext = SpringUtils.getApplicationContext();
         }
         forkingService = (ForkingService) appContext.getBean(
               SpringConstants.BEAN_ID_FORKING_SERVICE, ForkingService.class);
      }
      return forkingService;
   }
}
