package org.eclipse.stardust.engine.extensions.template.wrappers;

import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;

public class ActivityInstanceWrapper extends ActivityInstanceDetails
{
   private IActivityInstance ai;

   private IActivity activity;

   private ActivityDetailsWrapper aiWrapper;

   public ActivityInstanceWrapper(IActivityInstance activityInstance)
   {
      super(activityInstance);
      ai = activityInstance;
      this.activity = ai.getActivity();
      aiWrapper = new ActivityDetailsWrapper(this.activity);
   }

   @Override
   public ActivityDetailsWrapper getActivity()
   {
      return aiWrapper;
   }

}
