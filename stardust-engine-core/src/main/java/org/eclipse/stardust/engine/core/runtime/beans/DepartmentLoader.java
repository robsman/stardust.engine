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
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService.PRP_DISABLE_SYNCHRONIZATION;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_LOAD_PROPERTY;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Loader;


public class DepartmentLoader implements Loader
{
   public void load(final Persistent persistent)
   {
      final Parameters params = Parameters.instance();

      final boolean isSyncDisabled = params.getBoolean(PRP_DISABLE_SYNCHRONIZATION, false);
      final boolean syncOnLoad = params.getBoolean(AUTHORIZATION_SYNC_LOAD_PROPERTY, true);
      if ( !isSyncDisabled && syncOnLoad)
      {
         SynchronizationService.synchronize((IDepartment) persistent);
      }
   }
}
