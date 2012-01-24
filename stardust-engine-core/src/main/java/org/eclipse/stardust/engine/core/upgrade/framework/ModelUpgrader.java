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

import org.eclipse.stardust.engine.core.upgrade.jobs.ModelJobs;


/**
 * The upgrader to upgrade a model.
 *
 * @see Upgrader
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class ModelUpgrader extends Upgrader
{
   private List jobs;

   public ModelUpgrader(ModelItem item)
   {
      super(item);
      jobs = ModelJobs.getModelJobs();
   }

   public ModelUpgrader(ModelItem item, List jobs)
   {
      super(item);
      this.jobs = jobs;
   }

   public List getUpgradeJobs()
   {
      return jobs;
   }
}
