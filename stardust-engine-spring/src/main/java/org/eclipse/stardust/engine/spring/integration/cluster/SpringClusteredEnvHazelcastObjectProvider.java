package org.eclipse.stardust.engine.spring.integration.cluster;

import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusteredEnvHazelcastObjectProvider;
import org.springframework.beans.BeansException;

/**
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class SpringClusteredEnvHazelcastObjectProvider extends ClusteredEnvHazelcastObjectProvider
{
   private static final String HZ_CF_BEAN_ID = "xaHazelcastConnectionFactory";
   
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
      public static final ConnectionFactory connectionFactory = getConnectionFactoryFromAppCtx();
      
      private static ConnectionFactory getConnectionFactoryFromAppCtx()
      {
         try
         {
            return SpringUtils.getApplicationContext().getBean(HZ_CF_BEAN_ID, ConnectionFactory.class);
         }
         catch (final BeansException e)
         {
            throw new PublicException("Failed retrieving the Hazelcast Connection Factory from Spring's application context.", e);
         }
      }
   }   
}
