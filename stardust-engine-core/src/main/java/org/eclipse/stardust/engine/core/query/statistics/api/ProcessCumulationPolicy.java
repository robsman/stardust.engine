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
package org.eclipse.stardust.engine.core.query.statistics.api;

import org.eclipse.stardust.engine.api.query.EvaluationPolicy;

/**
 * Policy to retrieve root/scope process for cumulation.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class ProcessCumulationPolicy implements EvaluationPolicy
{
   static final long serialVersionUID = -4483029632890408112L;
   
   public static final ProcessCumulationPolicy WITH_ROOT_PI = new ProcessCumulationPolicy(true, false);

   public static final ProcessCumulationPolicy WITH_SCOPE_PI = new ProcessCumulationPolicy(false, true);

   public static final ProcessCumulationPolicy WITH_PI = new ProcessCumulationPolicy(false, false);

   private final boolean cumulateWithRootPi;
   private final boolean cumulateWithScopePi;

   private ProcessCumulationPolicy(boolean cumulateWithRootPi, boolean cumulateWithScopePi)
   {
      this.cumulateWithRootPi = cumulateWithRootPi;
      this.cumulateWithScopePi = cumulateWithScopePi;
   }

   public boolean cumulateWithRootProcess()
   {
      return cumulateWithRootPi;
   }
   
   public boolean cumulateWithScopeProcess()
   {
      return cumulateWithScopePi;
   }
   
   public boolean cumulateWithProcess()
   {
      return !cumulateWithRootProcess() && !cumulateWithScopeProcess();
   }
}
