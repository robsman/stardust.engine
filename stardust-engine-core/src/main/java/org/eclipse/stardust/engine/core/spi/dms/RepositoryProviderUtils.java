/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.PropertyLayer;

public class RepositoryProviderUtils
{
   public final static String DMS_ADMIN_SESSION = RepositoryProviderUtils.class.getName() + ".AdminSessionFlag";
   
   public RepositoryProviderUtils()
   {
      // utility class
   }
   
   public static boolean isAdminSessionFlagEnabled()
   {
      return Parameters.instance().getBoolean(DMS_ADMIN_SESSION, false);
   }
   
   public static void setAdminSessionFlag(boolean enabled, PropertyLayer layer)
   {
      layer.setProperty(DMS_ADMIN_SESSION, enabled);
   }


}
