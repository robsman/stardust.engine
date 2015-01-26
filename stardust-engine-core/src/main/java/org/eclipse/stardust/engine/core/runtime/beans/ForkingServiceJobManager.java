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

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.ICallable;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.IJobDescriptor;
import org.eclipse.stardust.common.rt.IJobManager;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ForkingServiceJobManager implements IJobManager
{
   private final ForkingService forkingService;

   public ForkingServiceJobManager(ForkingService forkingService)
   {
      this.forkingService = forkingService;
   }

   public ForkingService getForkingService()
   {
      return forkingService;
   }

   public Object performSynchronousJob(final ICallable callable) throws PublicException
   {
      return forkingService.isolate(new Action()
      {
         public Object execute()
         {
            try
            {
               return callable.call();
            }
            catch (RuntimeException re)
            {
               throw re;
            }
            catch (Exception e)
            {
               throw new PublicException(e);
            }
         }
      });
   }

   public void scheduleAsynchronousJobOnCommit(IJobDescriptor jobDescriptor)
   {
      forkingService.fork(jobDescriptor.getCarrier(), true);
   }

   public void startAsynchronousJob(IJobDescriptor jobDescriptor)
   {
      forkingService.fork(jobDescriptor.getCarrier(), false);
   }
   
}
