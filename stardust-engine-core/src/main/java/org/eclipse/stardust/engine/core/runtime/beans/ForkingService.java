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
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.IActionCarrier;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ForkingService
{
   /**
    * Executes the action synchronous (in the calling thread) starting a new transaction.
    *
    * @param action the Action to be executed.
    * @return the object resulted after the execution of the action.
    * @throws PublicException
    */
   Object isolate(Action action)
      throws PublicException;

   /**
    * Executes the action asynchronous (in a separate thread) starting a new transaction.
    *
    * @param action the Action to be executed.
    * @param transacted if true, the action will be executed only if the current
    *    transaction is successfull. If false, a new transaction will be started
    *    regardless of the current transaction state.
    */
   void fork(IActionCarrier action, boolean transacted);

}
