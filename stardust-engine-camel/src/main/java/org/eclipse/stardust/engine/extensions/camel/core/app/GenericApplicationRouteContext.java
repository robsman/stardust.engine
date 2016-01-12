package org.eclipse.stardust.engine.extensions.camel.core.app;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;

public class GenericApplicationRouteContext extends ProducerRouteContext
{

   public GenericApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   @Override
   public String getUserProvidedRouteConfiguration()
   {
      return Util.getProducerRouteConfiguration(application);
   }

}
