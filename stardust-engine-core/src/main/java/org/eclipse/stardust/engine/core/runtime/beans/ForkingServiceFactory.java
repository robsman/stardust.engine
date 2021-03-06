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

import org.eclipse.stardust.common.rt.IJobManager;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ForkingServiceFactory
{
   ForkingService get();

   IJobManager getJobManager();
   
   /**
    * Should not throw an exception
    * @param service
    */
   void release(ForkingService service);

   /**
    * Should not throw an exception
    * @param jobManager
    */
   void release(IJobManager jobManager);
}
