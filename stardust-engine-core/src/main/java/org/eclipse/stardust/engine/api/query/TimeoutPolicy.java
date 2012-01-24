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
package org.eclipse.stardust.engine.api.query;

/**
 * Supports specification of a runtime constraint on query execution. If query execution
 * exceeds the maximum time allowed, an {@link org.eclipse.stardust.common.error.ConcurrencyException} will
 * be reported.
 * <p>
 * Using timeouts might be useful to handle deadlock scenarios, i.e. to increase the
 * probability of queries being rolled back instead of concurrently performed
 * modifications.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TimeoutPolicy implements EvaluationPolicy
{
   private int timeout;

   /**
    * Initializes a new timeout policy.
    * 
    * @param timeout The number of seconds after which queries time out.
    */
   public TimeoutPolicy(int timeout)
   {
      this.timeout = timeout;
   }

   /**
    * Returns the number of seconds after which queries time out according to this policy.
    * 
    * @return The number of seconds.
    */
   public int getTimeout()
   {
      return timeout;
   }
}
