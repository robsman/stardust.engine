/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.spring.integration.jca;

import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringAppCtxHazelcastJcaConnectionFactoryProvider implements HazelcastJcaConnectionFactoryProvider
{
   private static final String HZ_CF_BEAN_ID = "localHazelcastConnectionFactory";
   
   @Override
   public ConnectionFactory connectionFactory()
   {
      return ConnectionFactoryHolder.connectionFactory;
   }
   
   /**
    * this class' only purpose is to ensure both safe publication and lazy initialization
    * (see 'lazy initialization class holder' idiom)
    */
   private static final class ConnectionFactoryHolder
   {
      public static final ConnectionFactory connectionFactory = getConnectionFactoryFromAppCtx();
      
      private static ConnectionFactory getConnectionFactoryFromAppCtx()
      {
         try
         {
            ApplicationContext appCtx = SpringUtils.getWebApplicationContext();
            if (appCtx == null)
            {
               /* we're not in a web environment */
               appCtx = SpringUtils.getApplicationContext();
            }
            return appCtx.getBean(HZ_CF_BEAN_ID, ConnectionFactory.class);
         }
         catch (final BeansException e)
         {
            throw new PublicException("Failed retrieving the Hazelcast Connection Factory from Spring's application context.", e);
         }
      }
   }
}
