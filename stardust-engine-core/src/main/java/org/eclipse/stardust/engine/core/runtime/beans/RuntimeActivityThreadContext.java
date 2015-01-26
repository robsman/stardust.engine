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

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RuntimeActivityThreadContext extends ActivityThreadContextAdapter
{
   public boolean isStepMode()
   {
      return false;
   }

   public void suspendActivityThread(ActivityThread activityThread)
   {
      // nothing to do
   }

   public void enteringTransition(TransitionTokenBean transitionToken)
   {
      // todo/laokoon
   }

   public void completingTransition(TransitionTokenBean transitionToken)
   {
      // todo/laokoon
   }

   public void enteringActivity(IActivityInstance activityInstance)
   {
      // todo/laokoon
      fireActivityStarted(activityInstance);
   }

   public void completingActivity(IActivityInstance activityInstance)
   {
      // todo/laokoon
      fireActivityCompleted(activityInstance);
   }

   public void handleWorklistItem(IActivityInstance activityInstance)
   {
      // todo/laokoon
   }

   public boolean allowsForceAssignmentToHuman()
   {
      return true;
   }
}
