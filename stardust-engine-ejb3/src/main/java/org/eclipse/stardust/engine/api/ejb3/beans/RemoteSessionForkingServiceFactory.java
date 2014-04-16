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
package org.eclipse.stardust.engine.api.ejb3.beans;

import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceJobManager;


/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */
public class RemoteSessionForkingServiceFactory implements ForkingServiceFactory
{
	
	private final org.eclipse.stardust.engine.api.ejb3.ForkingService service;
	
   public RemoteSessionForkingServiceFactory(org.eclipse.stardust.engine.api.ejb3.ForkingService service) {
	this.service = service;
}
	
   public ForkingService get()
   {
      return new EJBForkingService(this.service);
   }

   public IJobManager getJobManager()
   {
      return new ForkingServiceJobManager(get());
   }

   public void release(ForkingService service)
   {

   }

   public void release(IJobManager jobManager)
   {
      if (jobManager instanceof ForkingServiceJobManager)
      {
         release(((ForkingServiceJobManager) jobManager).getForkingService());
      }
   }

}
