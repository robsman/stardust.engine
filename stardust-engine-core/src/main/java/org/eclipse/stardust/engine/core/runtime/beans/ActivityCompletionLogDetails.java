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

import org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;

public class ActivityCompletionLogDetails implements ActivityCompletionLog
{
   private final ActivityInstance completedActivity;

   private final ActivityInstance nextForUser;

   public ActivityCompletionLogDetails(ActivityInstance completedActivity,
         ActivityInstance nextForUser)
   {
      this.completedActivity = completedActivity;
      this.nextForUser = nextForUser;
   }

   public ActivityInstance getCompletedActivity()
   {
      return completedActivity;
   }

   public ActivityInstance getNextForUser()
   {
      return nextForUser;
   }
}
