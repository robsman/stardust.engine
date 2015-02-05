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
package org.eclipse.stardust.engine.core.persistence.jca;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider;

/**
 * <p>
 * The class responsible for retrieving the <i>Hazelcast JCA Connection Factory</i>
 * from the <i>JNDI</i>.
 * </p>
 *
 * <p>
 * If not specified otherwise (see property <code>Infinity.Engine.Caching.Hazelcast.ConnectionFactoryJndiName</code>)
 * it assumes that it's bound to <code>java:/HazelcastCF</code>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class JndiHazelcastJcaConnectionFactoryProvider implements HazelcastJcaConnectionFactoryProvider
{
   private static final String PRP_HAZELCAST_CF_JNDI_NAME = "Infinity.Engine.Caching.Hazelcast.ConnectionFactoryJndiName";

   private static final String HAZELCAST_CF_DEFAULT_JNDI_NAME = "HazelcastCF";

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider#connectionFactory()
    */
   @Override
   public ConnectionFactory connectionFactory()
   {
      try
      {
         return ConnectionFactoryHolder.connectionFactory;
      }
      catch (final ExceptionInInitializerError e)
      {
         throw new PublicException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider#reset()
    */
   @Override
   public void reset()
   {
      // TODO implement re-initialization from JNDI
   }

   /**
    * <p>
    * This class' only purpose is to ensure both safe publication and lazy initialization
    * (see 'lazy initialization class holder' idiom).
    * </p>
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
            throw new PublicException(
                  BpmRuntimeError.HZLC_FAILED_RETRIEVING_HAZLECAST_CONNECTION_FACTORY_FROM_JNDI
                        .raise());
         }
      }
   }
}
