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

import java.io.File;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.model.removethis.ModelProperties;


/**
 * Wraps an CARNOT model repository as an upgradable item.
 *
 * @author fherinean
 * @version $Revision$
 */
public class RepositoryItem implements UpgradableItem
{
   public static final Logger trace = LogManager.getLogger(RepositoryItem.class);

   private Version version;
   private File boot;

   public RepositoryItem()
   {
      String repositoryPath = Parameters.instance().getString(
            ModelProperties.REPOSITORY_PATH, ".");
      boot = new File(repositoryPath, "repository.boot");
      if (boot.exists())
      {
         version = Version.createFixedVersion(3, 0, 0);
      }
      else
      {
         boot = new File(repositoryPath, "carnot.boot");
         if (boot.exists())
         {
            version = Version.createFixedVersion(2, 0, 0);
         }
         else
         {
            trace.debug("Unknown repository version.");
            version = Version.createFixedVersion(0, 0, 0);
         }
      }
   }

   public Version getVersion()
   {
      return version;
   }

   public void setVersion(Version version)
   {
      this.version = version;
   }

   public File getBoot()
   {
      return boot;
   }

   public void setBoot(File boot)
   {
      this.boot = boot;
   }

   public String getDescription()
   {
      return "Repository at " + boot;
   }
}
