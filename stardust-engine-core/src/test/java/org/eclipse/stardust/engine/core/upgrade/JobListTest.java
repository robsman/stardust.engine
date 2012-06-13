/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade;

import java.util.List;
import java.util.LinkedList;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgrader;

import junit.framework.TestCase;


/**
 * Test cases for testing the correct processing of the job list.
 *
 * @author kberberich, ubirkemeyer
 * @version $Revision: 7281 $
 */
public class JobListTest extends TestCase
{
   String model = "<MODEL carnot_xml_version=\"1.0.0\" id=\"ACME_Workflow_Model\"/>";
;
   public JobListTest(String name)
   {
      super(name);
   }

   /**
    * Tests that upgrade process is stopped if a maximum version is provided.
    */
   public void testUpgradeToVersion()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job09 = new DumbModelUpgradeJob(new Version(0,9,0));
      DumbModelUpgradeJob job11 = new DumbModelUpgradeJob(new Version(1,1,0));
      DumbModelUpgradeJob job12 = new DumbModelUpgradeJob(new Version(1,2,0));
      DumbModelUpgradeJob job17 = new DumbModelUpgradeJob(new Version(1,7,0));
      jobs.add(job09);
      jobs.add(job11);
      jobs.add(job12);
      jobs.add(job17);
      ModelItem item = new ModelItem(model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgradeToVersion(new Version(1,6,9), false);
      assertEquals(job12.getVersion(), newModel.getVersion());
      assertTrue(!job09.wasVisited());
      assertTrue(job11.wasVisited());
      assertTrue(job12.wasVisited());
      assertTrue(!job17.wasVisited());
   }

   /**
    * Tests whether a job with the current version is included in the upgrade.
    */
   public void testUpgradeToCurrentVersion()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job09 = new DumbModelUpgradeJob(new Version(0,9,0));
      DumbModelUpgradeJob job11 = new DumbModelUpgradeJob(new Version(1,1,0));
      DumbModelUpgradeJob job12 = new DumbModelUpgradeJob(new Version(1,2,0));
      DumbModelUpgradeJob current = new DumbModelUpgradeJob(CurrentVersion.getVersion());
      jobs.add(job09);
      jobs.add(job11);
      jobs.add(job12);
      jobs.add(current);
      ModelItem item = new ModelItem(model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgrade(false);
      assertEquals(current.getVersion(), newModel.getVersion());
      assertTrue(!job09.wasVisited());
      assertTrue(job11.wasVisited());
      assertTrue(job12.wasVisited());
      assertTrue(current.wasVisited());
   }

   /**
    * Tests an existing future version in the job list which should not be visited.
    */
   public void testUpgradeToFutureVersion()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job09 = new DumbModelUpgradeJob(new Version(0,9,0));
      DumbModelUpgradeJob job11 = new DumbModelUpgradeJob(new Version(1,1,0));
      DumbModelUpgradeJob job12 = new DumbModelUpgradeJob(new Version(1,2,0));
      DumbModelUpgradeJob current = new DumbModelUpgradeJob(CurrentVersion.getVersion());
      DumbModelUpgradeJob future = new DumbModelUpgradeJob(new Version(10000, 0, 0));
      jobs.add(job09);
      jobs.add(job11);
      jobs.add(job12);
      jobs.add(current);
      ModelItem item = new ModelItem(model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgrade(false);
      assertEquals(current.getVersion(), newModel.getVersion());
      assertTrue(!job09.wasVisited());
      assertTrue(job11.wasVisited());
      assertTrue(job12.wasVisited());
      assertTrue(current.wasVisited());
      assertTrue(!future.wasVisited());
   }

   /**
    * Tests recovery mode. Only the first visited job should be in recovery mode.
    */
   public void testRecovery()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job09 = new DumbModelUpgradeJob(new Version(0,9,0));
      DumbModelUpgradeJob job11 = new DumbModelUpgradeJob(new Version(1,1,0));
      DumbModelUpgradeJob job12 = new DumbModelUpgradeJob(new Version(1,2,0));
      DumbModelUpgradeJob current = new DumbModelUpgradeJob(CurrentVersion.getVersion());
      jobs.add(job09);
      jobs.add(job11);
      jobs.add(job12);
      jobs.add(current);
      ModelItem item = new ModelItem(model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgrade(true);
      assertEquals(current.getVersion(), newModel.getVersion());
      assertTrue(!job09.wasVisited());
      assertTrue(job11.wasVisited());
      assertTrue(job12.wasVisited());
      assertTrue(current.wasVisited());
      assertTrue(job11.wasRecovered());
      assertTrue(!job12.wasRecovered());
      assertTrue(!current.wasRecovered());
   }
}
