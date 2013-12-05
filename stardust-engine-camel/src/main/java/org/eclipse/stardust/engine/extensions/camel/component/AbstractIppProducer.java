package org.eclipse.stardust.engine.extensions.camel.component;

import org.apache.camel.impl.DefaultProducer;

public abstract class AbstractIppProducer extends DefaultProducer
{

   private AbstractIppEndpoint endpoint;

   public AbstractIppProducer(AbstractIppEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   /**
    * Overriding isSingleton behavior to use the endpoint's setting, because if the
    * endpoint is FALSE, then so should the producer. Otherwise this leads to unexpected
    * behavior since the producer instance being reused always points to the same
    * Endpoint, essentially making this a Singleton again.
    */
   @Override
   public boolean isSingleton()
   {
      return this.endpoint.isSingleton();
   }
}
