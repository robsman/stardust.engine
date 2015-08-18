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
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class POJOForkingServiceFactory implements ForkingServiceFactory
{
   private J2eeContainerType type;

   public POJOForkingServiceFactory(J2eeContainerType type)
   {
      this.type = type;
   }

   public ForkingService get()
   {
      return new POJOForkingService(type);
   }

   public IJobManager getJobManager()
   {
      return new ForkingServiceJobManager(get());
   }

   public void release(ForkingService service)
   {
      // @todo (france, ub):
   }

   public void release(IJobManager jobManager)
   {
      if (jobManager instanceof ForkingServiceJobManager)
      {
         release(((ForkingServiceJobManager) jobManager).getForkingService());
      }
   }

}
