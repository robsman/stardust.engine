/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.stardust.common.config.CurrentVersion;

/**
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class RuntimeJobs
{
   private static List jobs;

   public static List getRuntimeJobs()
   {
      if (jobs == null)
      {
         jobs = new LinkedList();

         // Todo: refactor this out of Stardust code
         if (CurrentVersion.getProductName().matches(".*[Ee]clipse.*"))
         {
            jobs.add(new AT1_1_0from1_0_0RuntimeJob());
            jobs.add(new AT2_0_0from1_1_0RuntimeJob());
         }
         else
         {
            jobs.add(new R5_2_0from4_9_0RuntimeJob());
            jobs.add(new R6_0_0from5_2_0RuntimeJob());
            jobs.add(new R7_0_0from6_x_xRuntimeJob());
            jobs.add(new R7_1_0from7_0_xRuntimeJob());
            jobs.add(new R7_1_4from7_1_0RuntimeJob());
            jobs.add(new R7_2_0from7_1_xRuntimeJob());
            jobs.add(new R7_3_0from7_2_0RuntimeJob());
         }
      }

      return jobs;
   }
}
