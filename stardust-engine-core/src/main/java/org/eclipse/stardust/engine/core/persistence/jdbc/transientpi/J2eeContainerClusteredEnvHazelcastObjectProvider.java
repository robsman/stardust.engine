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
package org.eclipse.stardust.engine.core.persistence.jdbc.transientpi;

import static org.eclipse.stardust.engine.core.cache.hazelcast.HazelcastCacheFactory.HAZELCAST_CF_DEFAULT_JNDI_NAME;
import static org.eclipse.stardust.engine.core.cache.hazelcast.HazelcastCacheFactory.PRP_HAZELCAST_CF_JNDI_NAME;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class J2eeContainerClusteredEnvHazelcastObjectProvider extends ClusteredEnvHazelcastObjectProvider
{
   @Override
   protected ConnectionFactory connectionFactory()
   {
      return ConnectionFactoryHolder.connectionFactory;
   }
   
   /**
    * this class' only purpose is to ensure both safe publication and lazy initialization
    * (see 'lazy initialization class holder' idiom)
    */
   private static final class ConnectionFactoryHolder
   {
      public static final ConnectionFactory connectionFactory = getConnectionFactoryFromJndi();
      
      private static ConnectionFactory getConnectionFactoryFromJndi()
      {
         try
         {
            final InitialContext ctx = new InitialContext();
            final Parameters params = Parameters.instance();
            final String hzCfJndiName = params.getString(PRP_HAZELCAST_CF_JNDI_NAME, HAZELCAST_CF_DEFAULT_JNDI_NAME);
            return (ConnectionFactory) ctx.lookup(hzCfJndiName);
         }
         catch (final NamingException e)
         {
            throw new PublicException("Failed retrieving the Hazelcast Connection Factory from JNDI.", e);
         }
      }      
   }
}
