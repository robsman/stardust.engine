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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class OracleAwareRuntimeUpgradeJob extends RuntimeUpgradeJob
{
   protected void assertCompatibility() throws UpgradeException
   {
      if (!(item.getDbDescriptor().getDbmsKey().equals(DBMSKey.ORACLE)
            || item.getDbDescriptor().getDbmsKey().equals(DBMSKey.ORACLE9i)))
      {
         throw new UpgradeException("The upgrade job for version " + getVersion()
               + " is only valid for " + DBMSKey.ORACLE.getName() + " or "
               + DBMSKey.ORACLE9i + " databases and thus will not work with the current "
               + item.getDbDescriptor().getDbmsKey().getName() + " database.");
      }
   }
}
