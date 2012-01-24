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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * Provides extended information in result of non-trivial activity completion. Currently
 * it allows to activate and retrieve a synchronous interactive successor activity, which
 * may be executed by he user.
 * 
 * @author rsauer
 * @version $Revision$
 */
public interface ActivityCompletionLog extends Serializable
{
   /**
    * Obtain the activity just completed.
    * 
    * @return The completed activity.
    */
   ActivityInstance getCompletedActivity();

   /**
    * Obtain any synchronous interactive successor activity, if existent. This activity
    * must be activateable by the calling user.
    * 
    * @return The next interactive activity, if existent.
    * 
    * @see WorkflowService#FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE
    * @see WorkflowService#activateNextActivityInstance(long)
    */
   ActivityInstance getNextForUser();
}
