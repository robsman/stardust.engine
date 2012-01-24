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

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public abstract class Upgrader
{
   private static final Logger trace = LogManager.getLogger(Upgrader.class);

   public static final String UPGRADE_DRYRUN = "ag.carnot.upgrade.dryrun";
   public static final String UPGRADE_STEP = "ag.carnot.upgrade.step";

   protected UpgradableItem item;

   public Upgrader(UpgradableItem item)
   {
      this.item = item;
   }

   public abstract List getUpgradeJobs();

   public UpgradableItem upgrade(boolean recover) throws UpgradeException
   {
      return upgradeToVersion(CurrentVersion.getVersion(), recover);
   }

   public UpgradableItem upgradeToVersion(Version version, boolean recover)
         throws UpgradeException
   {
      UpgradableItem item = this.item;
      try
      {
         setup();

         boolean upgradeStep = Parameters.instance().getBoolean(UPGRADE_STEP, false);
         final List jobs = getUpgradeJobs();
         for (Iterator i = jobs.iterator(); i.hasNext(); )
         {
            UpgradeJob job = (UpgradeJob) i.next();

            if (job.getVersion().compareTo(version) == 1)
            {
               break;
            }
            if (job.matches(item.getVersion()))
            {
               String message = "Running job '" + job.getVersion()
                     + "' against item '" + item.getDescription()
                     + "' with version '" + item.getVersion() + "'.";
               System.out.println(message);
               System.out.println("");

               trace.info(message);

               item = job.run(item, recover);

               String doneMessage = "Upgrade to version " + job.getVersion() + " done.";
               System.out.println(doneMessage);
               System.out.println("");
               trace.info(doneMessage);

               recover = false;
               
               if (upgradeStep)
               {
                  trace.info("Stopping upgrade after one step as requested");
                  break;
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }
      finally
      {
         shutdown();
      }
      return item;
   }

   protected void setup()
   {
   }

   protected void shutdown()
   {
   }
}
