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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.upgrade.jobs.RuntimeJobs;


/**
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class RuntimeUpgrader extends Upgrader
{
   private static final Logger trace = LogManager.getLogger(RuntimeUpgrader.class);

   public static final String UPGRADE_BATCH_SIZE = "carnot.upgrade.batchsize";

   public static final String UPGRADE_SCHEMA = "ag.carnot.upgrade.runtime.schema";
   public static final String UPGRADE_DATA = "ag.carnot.upgrade.runtime.data";
   public static final String UPGRADE_VERBOSE = "ag.carnot.upgrade.verbose";

   private String password;
   private List jobs;
   private String id = Long.toString(Calendar.getInstance().getTime().getTime());
   private boolean ignoreLock = false;
   
   private final boolean readonly;

   /**
    * Constructor which bootstraps the RuntimeUpgrader with the list of jobs
    * kept in <code>org.eclipse.stardust.engine.core.upgrade.jobs.RuntimeJobs</code>
    *
    * @param ignoreLock forces a run even if an upgrade lock is set
    */
   public RuntimeUpgrader(RuntimeItem item, String password, boolean ignoreLock)
   {
      this(item, password, RuntimeJobs.getRuntimeJobs(), ignoreLock);
   }

   /**
    * Constructor mainly for testing purposes. A list of jobs to execute is
    * provided.
    */
   public RuntimeUpgrader(RuntimeItem item, String password, List jobs)
   {
      this(item, password, jobs, false);
   }

   /**
    * Constructor which bootstraps the RuntimeUpgrader with an explicit list of jobs.
    *
    * @param ignoreLock forces a run even if an upgrade lock is set
    */
   public RuntimeUpgrader(RuntimeItem item, String password, List jobs, boolean ignoreLock)
   {
      super(item);
      this.password = password;
      this.jobs = jobs;
      this.ignoreLock = ignoreLock;
      
      this.readonly = Parameters.instance().getBoolean(UPGRADE_DRYRUN, false);
   }

   public RuntimeItem getItem()
   {
      return (RuntimeItem) super.item;
   }

   protected void setup()
   {
      try
      {
         boolean locked = getItem().hasProperty(Constants.UPGRADE_LOCK);

         if (locked)
         {
            if (ignoreLock)
            {
               getItem().deleteProperty(Constants.UPGRADE_LOCK);
            }
            else
            {
               throw new UpgradeException("Audit trail is already locked for upgrade.");
            }
         }

         getItem().createProperty(Constants.UPGRADE_LOCK, id);

         String version = getItem().getProperty(Constants.CARNOT_VERSION);

         String sysopPassword = getItem().getProperty(Constants.SYSOP_PASSWORD);

         if (version == null)
         {
            if (sysopPassword != null)
            {
               throw new UpgradeException("Can't find a version.");
            }

            if ((null == password) || !Constants.DEFAULT_PASSWORD.equals(password))
            {
               throw new UpgradeException("Invalid password.");
            }

            getItem().createProperty(Constants.SYSOP_PASSWORD,
                  Constants.DEFAULT_PASSWORD);

            getItem().createProperty(Constants.CARNOT_VERSION,
                  Version.createFixedVersion(1, 3, 0).toString());
         }
         else
         {
            if (sysopPassword == null)
            {
               throw new UpgradeException("Can't find password.");
            }

            if (password == null || !password.equals(sysopPassword))
            {
               throw new UpgradeException("Invalid password.");
            }
         }
      }
      catch (SQLException e)
      {
         trace.warn("Failed setting up runtime upgrade", e);
         throw new UpgradeException("Database access problem while setup of "
               + "RuntimeUpgrader : " + e.getMessage());
      }
   }

   protected void shutdown()
   {
      deleteUpgradeLock(false);
   }

   private void deleteUpgradeLock(boolean force)
   {
      try
      {
         String upgradeLock = getItem().getProperty(Constants.UPGRADE_LOCK);

         if (upgradeLock != null)
         {
            if (force || upgradeLock.equals(id))
            {
               getItem().deleteProperty(Constants.UPGRADE_LOCK);
            }
         }
      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new UpgradeException("SQL error: "+ e.getMessage());
      }
   }

   public List getUpgradeJobs()
   {
      return jobs;
   }
}
