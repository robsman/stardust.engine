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

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;

/**
 * @author sauer
 * @version $Revision$
 */
public class ApplicationInvocationContext
{
   private final ActivityInstance activityInstance;
   
   public ApplicationInvocationContext(ActivityInstance ai)
   {
      this.activityInstance = ai;
   }

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }
   
}
