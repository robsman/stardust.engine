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
public interface ActivityThreadContext
{
   boolean isStepMode();

   void suspendActivityThread(ActivityThread activityThread);

   void enteringTransition(TransitionTokenBean transitionToken);

   void completingTransition(TransitionTokenBean transitionToken);

   void enteringActivity(IActivityInstance activityInstance);

   void completingActivity(IActivityInstance activityInstance);

   void handleWorklistItem(IActivityInstance activityInstance);

   boolean allowsForceAssignmentToHuman();
   
   void addWorkflowEventListener(IWorkflowEventListener listener);

   void removeWorkflowEventListener(IWorkflowEventListener listener);
}
