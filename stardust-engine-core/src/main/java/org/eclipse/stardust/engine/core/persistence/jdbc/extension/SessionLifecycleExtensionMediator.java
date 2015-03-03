package org.eclipse.stardust.engine.core.persistence.jdbc.extension;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;

public class SessionLifecycleExtensionMediator implements ISessionLifecycleExtension
{

   private static final String FAILED_BROADCASTING_SESSION_LIFECYCLE_EXTENSION_EVENT = "Failed broadcasting session lifecycle extension event.";

   private static final Logger trace = LogManager.getLogger(SessionLifecycleExtensionMediator.class);

   private final List<ISessionLifecycleExtension> monitors;
   
   public SessionLifecycleExtensionMediator(List<ISessionLifecycleExtension> monitors)
   {
      this.monitors = monitors;
   }   
   
   @Override
   public void beforeSave(Session session)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         ISessionLifecycleExtension lifecycleExtension = monitors.get(i);
         try
         {
            lifecycleExtension.beforeSave(session);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_SESSION_LIFECYCLE_EXTENSION_EVENT, e);
         }
      }
   }

   @Override
   public void afterSave(Session session)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         ISessionLifecycleExtension lifecycleExtension = monitors.get(i);
         try
         {
            lifecycleExtension.afterSave(session);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_SESSION_LIFECYCLE_EXTENSION_EVENT, e);
         }
      }
      
   }
   
   
      
  

}
