/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * This class provides methods to obtain the Spring Application Context, to cache a Spring
 * Application Context, to clear the cache and to obtain the Spring Web Application
 * Context.
 */
public class SpringUtils
{
   private static final Logger trace = LogManager.getLogger(SpringUtils.class);

   // TODO revert to Spring singleton
   private static ApplicationContext singleton;

   /**
    * Returns the cached Spring Application Context for retrieving bean instances specified in
    * <code>carnot-spring-context.xml</code>. If no cached application context is
    * available a new one is bootstrapped.
    * 
    * @return <code>ApplicationContext</code> containing Spring Beans
    */
   public static ApplicationContext getApplicationContext()
   {
      ApplicationContext result = (ApplicationContext) Parameters.instance().get(
            SpringConstants.PRP_CACHED_APPLICATION_CONTEXT);

      if (null == result)
      {
         result = initApplicationContext();
      }

      return result;
   }

   /**
    * Sets the specified Spring Application Context that has to be cached.
    * 
    * @param ctxt
    *           The Spring Application Context to be cached.
    */
   public static synchronized void setApplicationContext(ConfigurableApplicationContext ctxt)
   {
      Parameters parameters = Parameters.instance();

      parameters.set(SpringConstants.PRP_CACHED_APPLICATION_CONTEXT, ctxt);
/*
      parameters.set(SpringConstants.PRP_CACHED_APPLICATION_CONTEXT_DISPOSER,
            new ApplicationContextDisposer(ctxt, parameters.getLong(
                  SpringConstants.PRP_CACHED_APPLICATION_DISPOSE_DELAY, 60 * 1000)));
*/
   }

   private static synchronized ApplicationContext initApplicationContext()
   {
      if (null == singleton)
      {
         Parameters parameters = Parameters.instance();
         String contextFile = parameters.getString(
               SpringConstants.PRP_APPLICATION_CONTEXT_FILE, "carnot-spring-context.xml");
         String[] contextFiles = contextFile.split(",");
         String contextClass = parameters.getString(
               SpringConstants.PRP_APPLICATION_CONTEXT_CLASS,
               ClassPathXmlApplicationContext.class.getName());
         singleton = (ApplicationContext) Reflect.createInstance(contextClass,
               new Class[] { String[].class },
               new Object[] { contextFiles });

         trace.warn("Bootstrapped new Spring application context " + singleton);
      }

      setApplicationContext((ConfigurableApplicationContext) singleton);

      return singleton;
   }

   /**
    * Removes the stored Spring Application Context from cache.
    */
   public static synchronized void reset()
   {
      Parameters.instance().set(SpringConstants.PRP_CACHED_APPLICATION_CONTEXT, null);
      singleton = null;
   }

   /**
    * Finds the root web application context for this web application for the servlet
    * context or for the current thread.
    * 
    * @return The root web <code>ApplicationContext</code>, or null if none found.
    */
   public static ApplicationContext getWebApplicationContext()
   {
      try
      {
         RequestAttributes currentRequestAttributes = RequestContextHolder.currentRequestAttributes();
         if (currentRequestAttributes instanceof ServletRequestAttributes)
         {
            HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes).getRequest();
            HttpSession session = request.getSession(false);
            if (session != null)
            {
               return WebApplicationContextUtils
                  .getWebApplicationContext(session.getServletContext());
            }
         }
         return ContextLoader.getCurrentWebApplicationContext();
      }
      catch (IllegalStateException e)
      {
         // seems that we're not in a web environment
      }
      return null;
   }
/*
   private static class ApplicationContextDisposer extends TimerTask
         implements Parameters.IDisposable
   {
      private final long delay;

      private final ConfigurableApplicationContext target;

      public ApplicationContextDisposer(ConfigurableApplicationContext target, long delay)
      {
         this.target = target;
         this.delay = delay;
      }

      public void dispose()
      {
         // schedule app context close after a short delay to enable clients to be
         // completed properly
         new Timer(false).schedule(this, delay);
      }

      public void run()
      {
         trace.warn("Closing Spring application context " + target);

         target.close();
      }
   }
*/
}
