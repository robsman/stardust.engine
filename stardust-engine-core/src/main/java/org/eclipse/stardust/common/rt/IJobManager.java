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
package org.eclipse.stardust.common.rt;

import org.eclipse.stardust.common.ICallable;
import org.eclipse.stardust.common.error.PublicException;


/**
 * Abstraction of a job management facility supporting to execute synchronous jobs as well
 * as schedule or start asynchronous jobs.
 *
 * @author rsauer
 * @version $Revision$
 */
public interface IJobManager
{

   /**
    * Executes the action synchronous (in the calling thread) starting a new transaction.
    *
    * @param callable the Action to be executed.
    * @return the object resulted after the execution of the action.
    * @throws PublicException
    */
   Object performSynchronousJob(ICallable callable)
      throws PublicException;

   /**
    * Executes the action asynchronous (in a separate thread) starting a new transaction.
    *
    * The job will be started only if the current transaction will succeed.
    */
   void scheduleAsynchronousJobOnCommit(IJobDescriptor jobDescriptor);

   /**
    * Executes the action asynchronous (in a separate thread) starting a new transaction.
    * 
    * The job will be started regardless of the current transaction's outcome.
    */
   void startAsynchronousJob(IJobDescriptor jobDescriptor);

}
