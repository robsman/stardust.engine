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
package org.eclipse.stardust.engine.core.upgrade;

import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;

import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgrader;

/**
 * Test cases for testing the correct processing of the job list.
 *
 * @author kberberich, ubirkemeyer
 * @version $Revision: 7281 $
 */
public class JobListTest
{
   String model = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                + "<model xmlns=\"http://www.carnot.ag/workflowmodel/3.1\" "
                + "id=\"ACME_Workflow_Model\" name=\"ACME Workflow Model\" "
                + "carnotVersion=\"2.0.0\" vendor=\"SunGard CSA LLC, IPP\"/>";

   /**
    * Tests that upgrade process is stopped if a maximum version is provided.
    */
   @Test
   public void testUpgradeToVersion()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job19 = new DumbModelUpgradeJob(Version.createFixedVersion(1,9,0));
      DumbModelUpgradeJob job21 = new DumbModelUpgradeJob(Version.createFixedVersion(2,1,0));
      DumbModelUpgradeJob job22 = new DumbModelUpgradeJob(Version.createFixedVersion(2,2,0));
      DumbModelUpgradeJob job27 = new DumbModelUpgradeJob(Version.createFixedVersion(2,7,0));
      jobs.add(job19);
      jobs.add(job21);
      jobs.add(job22);
      jobs.add(job27);
      ModelItem item = new ModelItem(null, Long.MIN_VALUE, model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      upgrader.upgradeToVersion(Version.createFixedVersion(2,6,9), false);
      assertEquals(job22.getVersion(), item.getVersion());
      assertTrue(!job19.wasVisited());
      assertTrue(job21.wasVisited());
      assertTrue(job22.wasVisited());
      assertTrue(!job27.wasVisited());
   }

   /**
    * Tests whether a job with the current version is included in the upgrade.
    */
   @Test
   @Ignore("not Stardust ready")
   public void testUpgradeToCurrentVersion()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job19 = new DumbModelUpgradeJob(Version.createFixedVersion(1,9,0));
      DumbModelUpgradeJob job21 = new DumbModelUpgradeJob(Version.createFixedVersion(2,1,0));
      DumbModelUpgradeJob job22 = new DumbModelUpgradeJob(Version.createFixedVersion(2,2,0));
      DumbModelUpgradeJob current = new DumbModelUpgradeJob(CurrentVersion.getVersion());
      jobs.add(job19);
      jobs.add(job21);
      jobs.add(job22);
      jobs.add(current);
      ModelItem item = new ModelItem(null, Long.MIN_VALUE, model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgrade(false);
      assertEquals(current.getVersion(), newModel.getVersion());
      assertTrue(!job19.wasVisited());
      assertTrue(job21.wasVisited());
      assertTrue(job22.wasVisited());
      assertTrue(current.wasVisited());
   }

   /**
    * Tests an existing future version in the job list which should not be visited.
    */
   @Test
   @Ignore("not Stardust ready")
   public void testUpgradeToFutureVersion()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job19 = new DumbModelUpgradeJob(Version.createFixedVersion(1,9,0));
      DumbModelUpgradeJob job21 = new DumbModelUpgradeJob(Version.createFixedVersion(2,1,0));
      DumbModelUpgradeJob job22 = new DumbModelUpgradeJob(Version.createFixedVersion(2,2,0));
      DumbModelUpgradeJob current = new DumbModelUpgradeJob(CurrentVersion.getVersion());
      DumbModelUpgradeJob future = new DumbModelUpgradeJob(Version.createFixedVersion(10000, 0, 0));
      jobs.add(job19);
      jobs.add(job21);
      jobs.add(job22);
      jobs.add(current);
      ModelItem item = new ModelItem(null, Long.MIN_VALUE, model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgrade(false);
      assertEquals(current.getVersion(), newModel.getVersion());
      assertTrue(!job19.wasVisited());
      assertTrue(job21.wasVisited());
      assertTrue(job22.wasVisited());
      assertTrue(current.wasVisited());
      assertTrue(!future.wasVisited());
   }

   /**
    * Tests recovery mode. Only the first visited job should be in recovery mode.
    */
   @Test
   @Ignore("not Stardust ready")
   public void testRecovery()
   {
      List jobs = new LinkedList();
      DumbModelUpgradeJob job19 = new DumbModelUpgradeJob(Version.createFixedVersion(1,9,0));
      DumbModelUpgradeJob job21 = new DumbModelUpgradeJob(Version.createFixedVersion(2,1,0));
      DumbModelUpgradeJob job22 = new DumbModelUpgradeJob(Version.createFixedVersion(2,2,0));
      DumbModelUpgradeJob current = new DumbModelUpgradeJob(CurrentVersion.getVersion());
      jobs.add(job19);
      jobs.add(job21);
      jobs.add(job22);
      jobs.add(current);
      ModelItem item = new ModelItem(null, Long.MIN_VALUE, model);
      ModelUpgrader upgrader = new ModelUpgrader(item, jobs);
      ModelItem newModel = (ModelItem) upgrader.upgrade(true);
      assertEquals(current.getVersion(), newModel.getVersion());
      assertTrue(!job19.wasVisited());
      assertTrue(job21.wasVisited());
      assertTrue(job22.wasVisited());
      assertTrue(current.wasVisited());
      assertTrue(job21.wasRecovered());
      assertTrue(!job22.wasRecovered());
      assertTrue(!current.wasRecovered());
   }
}
