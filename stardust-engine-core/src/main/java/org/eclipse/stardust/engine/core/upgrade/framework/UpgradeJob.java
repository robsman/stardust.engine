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

import org.eclipse.stardust.common.config.Version;

/**
 * The base class for a concrete update job. This contains the logic to do the
 * update work for a <em>concrete</em> item type on a <em>concrete</em> version.
 *
 * @see Upgrader
 * @see ModelUpgradeJob
 * @see RuntimeUpgradeJob
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public abstract class UpgradeJob
{
   public abstract UpgradableItem run(UpgradableItem item, boolean recover);

   public abstract Version getVersion();

   public boolean matches(Version version)
   {
      if (getVersion().compareTo(version) > 0)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
}
