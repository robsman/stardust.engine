package org.eclipse.stardust.engine.extensions.camel.core;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SCRIPTING_OVERLAY;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.core.app.GenericApplicationRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.app.scripting.ScriptingApplicationRouteContext;

public class ProducerRouteContextFactory
{

   public static ProducerRouteContext getContext(IApplication application,
         CamelContext context, String partition)
   {
      if(Util.getOverlayType(application).equalsIgnoreCase(SCRIPTING_OVERLAY))
         return new ScriptingApplicationRouteContext(application, partition, context.getName());
      
      return new GenericApplicationRouteContext(application, partition, context.getName());
   }
}
