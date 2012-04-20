/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.test.impl.barrier.ActivityInstanceCreatedBarrier;
import org.eclipse.stardust.test.impl.barrier.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.impl.barrier.ProcessInstanceStateBarrier;

/**
 * <p>
 * This utility class allows for waiting for conditions such as
 * <ul>
 *   <li>a particular state of a process instance</li>
 *   <li>a particular state of an activity instance</li>
 *   <li>the creation of an activity instance</li>
 * </ul>
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class Barriers
{
   /**
    * <p>
    * Allows to wait for a process instance state change.
    * </p>
    * 
    * @param sf the service factory to use; must not be <code>null</code>
    * @param piOid the oid of the process instance to wait for
    * @param state the process instance state to wait for; must not be <code>null</code>
    * 
    * @throws IllegalStateException if the condition is still not met, but the retry count exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public static void awaitProcessInstanceState(final ServiceFactory sf, final long piOid, final ProcessInstanceState state) throws IllegalStateException, InterruptedException
   {
      new ProcessInstanceStateBarrier(sf, piOid, state).await();
   }

   /**
    * <p>
    * Allows to wait for an activity instance state change.
    * </p>
    * 
    * @param sf the service factory to use; must not be <code>null</code>
    * @param aiOid the oid of the activity instance to wait for
    * @param state the activity instance state to wait for; must not be <code>null</code>
    * 
    * @throws IllegalStateException if the condition is still not met, but the retry count exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public static void awaitActivityInstanceState(final ServiceFactory sf, final long aiOid, final ActivityInstanceState state) throws IllegalStateException, InterruptedException
   {
      new ActivityInstanceStateBarrier(sf, aiOid, state).await();
   }
   
   /**
    * <p>
    * Allows to wait for the creation of an activity instance, i.e. until
    * one activitiy instance for a particular process instance is in the 
    * state {@link ActivityInstanceState.Application}.
    * </p>
    * 
    * @param sf the service factory to use; must not be <code>null</code>
    * @param oid the oid of the process instance that comprises the activity instance to wait for
    * 
    * @throws IllegalStateException if the condition is still not met, but the retry count exceeded
    * @throws InterruptedException if any thread interrupted the current thread
    */
   public static void awaitActivityInstanceCreation(final ServiceFactory sf, final long piOid) throws IllegalStateException, InterruptedException
   {
      new ActivityInstanceCreatedBarrier(sf, piOid).await();
   }
}
