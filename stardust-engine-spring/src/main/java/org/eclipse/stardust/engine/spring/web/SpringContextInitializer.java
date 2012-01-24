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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SpringContextInitializer implements ServletContextListener
{
   private static final Logger trace = LogManager.getLogger(SpringContextInitializer.class);
   
   public static final String SPRING_CONTEXT_PARAM = "carnot.SPRING_CONTEXT";

   public void contextInitialized(ServletContextEvent e)
   {
      trace
            .warn("The listener-class entry org.eclipse.stardust.engine.spring.web.SpringContextInitializer can be removed from web.xml as it is no longer used.");
//      WebApplicationContext webCtxt = WebApplicationContextUtils.getWebApplicationContext(e.getServletContext());
//      if (webCtxt instanceof ConfigurableApplicationContext)
//      {
//         trace.info("Connection CARNOT to default Spring Web application context.");
//         
//         SpringUtils.setApplicationContext((ConfigurableApplicationContext) webCtxt);
//
//         return;
//      }
//      
//      // else bootstrap our own
//      
//      trace.info("Initializing custom application context.");
//
//      String contextFileParam = e.getServletContext().getInitParameter(
//            SPRING_CONTEXT_PARAM);
//
//      AbstractRefreshableWebApplicationContext ctxt = new XmlWebApplicationContext();
//      ctxt.setServletContext(e.getServletContext());
//
//      if (StringUtils.isEmpty(contextFileParam))
//      {
//         contextFileParam = "/WEB-INF/carnot-spring-context.xml";
//      }
//
//      ctxt.setConfigLocations(new String[] {contextFileParam});
//      try
//      {
//         ctxt.refresh();
//      }
//      catch (BeanDefinitionStoreException bdse)
//      {
//         // try with fixed context path
//         if ( !contextFileParam.startsWith("/WEB-INF/"))
//         {
//            StringBuffer buffer = new StringBuffer();
//            buffer.append("/WEB-INF");
//            if ( !contextFileParam.startsWith("/"))
//            {
//               buffer.append("/");
//            }
//            buffer.append(contextFileParam);
//            
//            ctxt.setConfigLocations(new String[] {buffer.toString()});
//            ctxt.refresh();
//         }
//         else
//         {
//            throw bdse;
//         }
//      }
//      
//      SpringUtils.setApplicationContext(ctxt);
   }

   public void contextDestroyed(ServletContextEvent e)
   {
//      SpringUtils.setApplicationContext(null);
   }
}
