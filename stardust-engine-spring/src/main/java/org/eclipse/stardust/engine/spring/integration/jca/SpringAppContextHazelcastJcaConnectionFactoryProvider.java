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

/**
 * <p>
 * The class responsible for retrieving the <i>Hazelcast JCA Connection Factory</i>
 * from the <i>Spring Application Context</i>.
 * </p>
 *
 * <p>
 * This class assumes that there's a bean with id <code>localHazelcastConnectionFactory</code>
 * representing the <i>Hazelcast JCA Connection Factory</i> in the <i>Spring Application Context</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class SpringAppContextHazelcastJcaConnectionFactoryProvider implements HazelcastJcaConnectionFactoryProvider
{
   private static final String HZ_CF_BEAN_ID = "localHazelcastConnectionFactory";

   private static volatile ConnectionFactory connectionFactory;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider#connectionFactory()
    */
   @Override
   public ConnectionFactory connectionFactory()
   {
      if (connectionFactory == null)
      {
         synchronized (SpringAppContextHazelcastJcaConnectionFactoryProvider.class)
         {
            if (connectionFactory == null)
            {
               connectionFactory = getConnectionFactoryFromAppCtx();
            }
         }
      }
      return connectionFactory;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider#reset()
    */
   @Override
   public void reset()
   {
      synchronized (SpringAppContextHazelcastJcaConnectionFactoryProvider.class)
      {
         connectionFactory = null;
      }
   }

   private ConnectionFactory getConnectionFactoryFromAppCtx()
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
