package org.eclipse.stardust.engine.core.persistence.jdbc.extension;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;

public class SessionLifecycleUtils
{

   private static final String KEY_SESSION_LIFECYLCE_EXTENSION_MEDIATOR = SessionLifecycleExtensionMediator.class.getName()
         + ".RuntimeEnvironmentMonitorMediator";

   public static ISessionLifecycleExtension getSessionLifecycleExtension()
   {
      GlobalParameters globals = GlobalParameters.globals();

      ISessionLifecycleExtension mediator = (ISessionLifecycleExtension) globals.get(KEY_SESSION_LIFECYLCE_EXTENSION_MEDIATOR);

      if (null == mediator)
      {
         mediator = new SessionLifecycleExtensionMediator(
               ExtensionProviderUtils.getExtensionProviders(ISessionLifecycleExtension.class));
         globals.set(KEY_SESSION_LIFECYLCE_EXTENSION_MEDIATOR, mediator);
      }

      return mediator;      
   }

}
