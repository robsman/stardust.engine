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
package org.eclipse.stardust.common.utils.ejb;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Encapsulates the most important use case regarding session bean and
 * entity bean handling.
 * <p>
 * Fakes home lookup, bean creation etc. for local objects with assumptions
 * on the compliance to the following conventions:
 */
public class EJBUtils
{
   private static final Logger trace = LogManager.getLogger(EJBUtils.class);
   
   public static final String SERVER_VENDOR_PROPERTY = EjbProperties.SERVER_VENDOR_PROPERTY;
   public static final String BORLAND = "BORLAND";
   
   public static final String INITIAL_CONTEXT_FACTORY = EjbProperties.INITIAL_CONTEXT_FACTORY;
   public static final String JNDI_URL = EjbProperties.JNDI_URL;
   public static final String USER_NAME = EjbProperties.USER_NAME;
   public static final String USER_PASS = EjbProperties.USER_PASS;
   public static final String PKG_PREFIXES = EjbProperties.PKG_PREFIXES;
   public static final String CONTAINER_TYPE = EjbProperties.CONTAINER_TYPE;

   // legacy support for pre 3.6.0 agent configurations 
   public static final String AGENT_LEGACY_JNDI_URL = "JNDI.ServerURL";
   public static final String AGENT_LEGACY_USER_NAME = "JNDI.UserName";
   public static final String AGENT_LEGACY_USER_PASS = "JNDI.UserPassword";

   /**
    * Returns the initial context either on server or on client side
    * @param isLocal true, if running in fake mode, false if using real ejb mode
    * @param isLivingInContainer true, if context is provided by the container, false,
    *                          if the context is retrieved from the client
    * @return the initial context
    */
   public static Context getInitialContext(boolean isLocal, boolean isLivingInContainer)
   {
      return getInitialContext(isLocal, isLivingInContainer, false);
   }
   
   /**
    * Returns the initial context either on server or on client side
    * @param isLocal true, if running in fake mode, false if using real ejb mode
    * @param isLivingInContainer true, if context is provided by the container, false,
    *                          if the context is retrieved from the client
    * @param jndiPropertyCompatibility true, if alternative jndi property names shall be
    *                                  used when new property names do not find anything. 
    * @return the initial context
    */
   public static Context getInitialContext(boolean isLocal, boolean isLivingInContainer,
         boolean jndiPropertyCompatibility)
   {
      if (isLocal)
      {
         trace.debug("Getting local 'fake' context.");
         return new InitialContext();
      }

      if (isLivingInContainer)
      {
         trace.debug("Getting EJB server context.");
         try
         {
            return new javax.naming.InitialContext();
         }
         catch (NamingException e)
         {
            throw new InternalException("Cannot bind JNDI context within container.", e);
         }
      }

      trace.debug("Getting EJB client context.");

      Properties properties = new Properties();

      String factoryName = Parameters.instance().getString(INITIAL_CONTEXT_FACTORY);

      if (factoryName == null)
      {
         throw new PublicException(
               "You have not supplied the correct JNDIInitialContext parameter in properties");
      }

      trace.debug("Initial context factory is " + factoryName);

      properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryName);

      if (jndiPropertyCompatibility)
      {
         setProperty(JNDI_URL, AGENT_LEGACY_JNDI_URL, Context.PROVIDER_URL, properties);
         setProperty(USER_NAME, AGENT_LEGACY_USER_NAME, Context.SECURITY_PRINCIPAL, properties);
         setProperty(USER_PASS, AGENT_LEGACY_USER_PASS, Context.SECURITY_CREDENTIALS, properties);
      }
      else
      {
         setProperty(JNDI_URL, Context.PROVIDER_URL, properties);
         setProperty(USER_NAME, Context.SECURITY_PRINCIPAL, properties);
         setProperty(USER_PASS, Context.SECURITY_CREDENTIALS, properties);
      }
      
      setProperty(PKG_PREFIXES, Context.URL_PKG_PREFIXES, properties);

      try
      {
         return new javax.naming.InitialContext(properties);
      }
      catch (Exception e)
      {
         throw new InternalException("Failed to connect to server '" + 
            properties.getProperty(JNDI_URL) + "'.", e);
      }
   }
   
   private static void setProperty(String sourceName, String targetName,
         Properties properties)
   {
      setProperty(sourceName, null, targetName, properties);
   }

   private static void setProperty(String sourceName, String alternativeSourceName,
         String targetName, Properties properties)
   {
      String value = Parameters.instance().getString(sourceName);
      if ( !StringUtils.isEmpty(value))
      {
         properties.setProperty(targetName, value);
      }
      else if (null != alternativeSourceName)
      {
         setProperty(alternativeSourceName, targetName, properties);
      }
   }

   /**
    * Returns the home object retrieved from the initial context
    * @param jndiPath   the JNDI path to the object
    * @param interfaceClass  the interface of the remote object
    * @return the object
    */
   public static Object getJndiObject(String jndiPath, Class interfaceClass)
   {
      return getHomeObject(jndiPath, interfaceClass, false);
   }

   /**
    * Returns the home object retrieved from the initial context
    * @param jndiPath   the JNDI path to the bean
    * @param homeInterfaceClass  the home interface of the bean
    * @param isLocal    true, if running in fake mode, false, if running in real
    *    ejb mode
    * @return the home object of the bean
    */
   public static Object getHomeObject(String jndiPath, Class homeInterfaceClass,
      boolean isLocal)
   {
      Context context = getInitialContext(isLocal, isLivingInContainer());

      try
      {
         Object homeObj = context.lookup(jndiPath);

         if (!isLocal)
         {
            return PortableRemoteObject.narrow(homeObj, homeInterfaceClass);
         }
         else
         {
            return homeObj;
         }

      }
      catch (Exception x)
      {
         throw new PublicException(
               "Cannot lookup object via JNDI path '" + jndiPath + "'.", x);
      }
   }

   public static boolean isLivingInContainer()
   {
      J2eeContainerType type = (J2eeContainerType) Parameters.instance().get(
            CONTAINER_TYPE);
      return ((type == J2eeContainerType.EJB) || (type == J2eeContainerType.WEB));
   }

   private EJBUtils()
   {
   }

}