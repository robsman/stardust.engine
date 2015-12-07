package org.eclipse.stardust.engine.extensions.camel.core.app;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.app.templating.ApplicationWrapper;
import org.eclipse.stardust.engine.extensions.camel.core.app.templating.TemplatingRouteBuilder;

public class TemplatingApplicationRouteContext extends ProducerRouteContext
{
   public static final Logger logger = LogManager
         .getLogger(TemplatingApplicationRouteContext.class);

   public TemplatingApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   @Override
   protected String generateRoute(IApplication application)
   {
      return TemplatingRouteBuilder.generateRoute(new ApplicationWrapper(application));
   }
}
