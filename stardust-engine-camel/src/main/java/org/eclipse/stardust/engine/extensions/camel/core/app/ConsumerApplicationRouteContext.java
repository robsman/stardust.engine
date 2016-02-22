package org.eclipse.stardust.engine.extensions.camel.core.app;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.core.ConsumerRouteContext;

public class ConsumerApplicationRouteContext extends ConsumerRouteContext
{
   public ConsumerApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   /**
    * Returns the provided camel route.
    */
   @Override
   public String getUserProvidedRouteConfiguration()
   {
      return Util.getConsumerRouteConfiguration(application);
   }
}
