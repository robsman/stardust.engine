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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IWorkItem;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface IActivityExecutionStrategy
{
   
   void startActivityInstance(IActivityInstance activityInstance) throws IllegalStateChangeException;

   void updateWorkItem(IActivityInstance activityInstance);
   
   void updateWorkItem(IActivityInstance activityInstance, IWorkItem workItem);
   
   void completeActivityInstance(IActivityInstance activityInstance) throws IllegalStateChangeException;
   
   interface Factory
   {
      IActivityExecutionStrategy getExecutionStrategy(IActivity activity);
   }

}
