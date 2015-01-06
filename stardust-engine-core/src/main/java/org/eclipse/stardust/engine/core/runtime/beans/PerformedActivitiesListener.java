/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public final class PerformedActivitiesListener implements IWorkflowEventListener
{
   private static final Logger trace = LogManager.getLogger(PerformedActivitiesListener.class);
   
   private List<IActivityInstance> activities;
   
   public List<IActivityInstance> getActivities()
   {
      return (null != activities)
            ? Collections.unmodifiableList(activities)
            : Collections.<IActivityInstance>emptyList();
   }

   public void startedActivityInstance(IActivityInstance activityInstance)
   {
      if (null == activities)
      {
         activities = new ArrayList<IActivityInstance>();
      }
      
      if ( !activities.contains(activityInstance))
      {
         activities.add(activityInstance);
      }
      else
      {
         trace.debug("Activity started twice: " + activityInstance + ".");
      }
   }

   public void completedActivityInstance(IActivityInstance activityInstance)
   {
      if (null == activities)
      {
         activities = new ArrayList<IActivityInstance>();
      }
      
      if ( !activities.contains(activityInstance)
            || ( !activities.isEmpty() && activities.get(activities.size() - 1)
                  .equals(activityInstance)))
      {
         activities.add(activityInstance);
      }
      else
      {
         trace.debug("Activity completed in unexpected order: " + activityInstance + ".");
      }
   }
}