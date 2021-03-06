/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
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
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgradeJob;


/**
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class ModelJobs
{
   private static LinkedList jobs = null;

   public static List<ModelUpgradeJob> getModelJobs()
   {
      jobs = new LinkedList<ModelUpgradeJob>();

      // Todo: refactor this out of Stardust code
      if (CurrentVersion.getProductName().matches(".*[Ee]clipse.*"))
      {
         jobs.add(new ATM3_1_0from1_0_0ModelJob());
      }
      else
      {
         jobs.add(new M9_0_0from7_0_0ModelJob());
         jobs.add(new M9_2_0from9_0_0ModelJob());
      }

      return jobs;
   }
}
