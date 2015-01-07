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

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradableItem;


/**
 * Test Job which only updates the model version.
 * It remembers whether it was visited or recovered for later testing.
 *
 * @author ubirkemeyer
 * @version $Revision: 7281 $
 */
public class DumbModelUpgradeJob extends ModelUpgradeJob
{
   private Version version;
   private boolean visited = false;
   private boolean recover = false;

   public DumbModelUpgradeJob(Version version)
   {
      this.version = version;
   }

   public UpgradableItem run(UpgradableItem item, boolean recover)
   {
      item.setVersion(version);
      visited = true;
      this.recover = recover;
      return item;
   }

   public Version getVersion()
   {
      return version;
   }

   public boolean wasVisited()
   {
      return visited;
   }

   public boolean wasRecovered()
   {
      return recover;
   }
}
