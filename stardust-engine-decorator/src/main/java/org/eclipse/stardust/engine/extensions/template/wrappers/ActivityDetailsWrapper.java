package org.eclipse.stardust.engine.extensions.template.wrappers;

import org.eclipse.stardust.engine.api.dto.ActivityDetails;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IActivity;

public class ActivityDetailsWrapper extends ActivityDetails
{
   private Application application;

   public ActivityDetailsWrapper(IActivity activity)
   {
      super(activity);
   }

   public void setApplication(Application application)
   {
      this.application = application;
   }

   @Override
   public Application getApplication()
   {
      return this.application;
   }
}
