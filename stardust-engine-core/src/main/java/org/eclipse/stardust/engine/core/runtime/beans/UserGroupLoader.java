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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Loader;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class UserGroupLoader implements Loader
{
   public void load(Persistent persistent)
   {
      Parameters params = Parameters.instance();
      if ( !params.getBoolean(SynchronizationService.PRP_DISABLE_SYNCHRONIZATION, false)
            && params.getBoolean(SecurityProperties.AUTHORIZATION_SYNC_LOAD_PROPERTY,
                  true))
      {
         SynchronizationService.synchronize((IUserGroup) persistent);
      }
   }
}
