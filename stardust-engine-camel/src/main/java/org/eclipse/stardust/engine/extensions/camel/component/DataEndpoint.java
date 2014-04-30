package org.eclipse.stardust.engine.extensions.camel.component;

import org.apache.camel.Consumer;

import org.apache.camel.Processor;
import org.apache.camel.Producer;

public class DataEndpoint extends AbstractIppEndpoint
{
   public DataEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }

   @Override
   public Producer createProducer() throws Exception
   {
      return new DataEndpointProducer(this);
   }

   @Override
   public Consumer createConsumer(Processor processor) throws Exception
   {
      throw new UnsupportedOperationException("This endpoint cannot be used as a consumer:" + getEndpointUri());
   }

   @Override
   public boolean isSingleton()
   {
      return true;
   }

   /**
    * return true because the endpoint allows additional unknown options to be passed to
    * it
    */
   public boolean isLenientProperties()
   {
      return true;
   }
}
