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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.util.List;

import org.eclipse.stardust.engine.core.upgrade.jobs.RepositoryJobs;


/**
 * The upgrader to upgrade a model.
 *
 * @see Upgrader
 * @author fherinean
 * @version $Revision$
 */
public class RepositoryUpgrader extends Upgrader
{
   private List jobs;

   public RepositoryUpgrader(RepositoryItem item)
   {
      super(item);
      jobs = RepositoryJobs.getRepositoryJobs();
   }

   public RepositoryUpgrader(RepositoryItem item, List jobs)
   {
      super(item);
      this.jobs = jobs;
   }

   public List getUpgradeJobs()
   {
      return jobs;
   }
}
