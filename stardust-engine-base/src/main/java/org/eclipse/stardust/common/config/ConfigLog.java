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
package org.eclipse.stardust.common.config;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


public class ConfigLog
{
   private static final String CAT_ROOT = getRootCategory() + ".log";

   public static final Logger CONFIG_LOG = LogManager.getLogger(CAT_ROOT + ".config");

   public static final Logger EXTENSIONS_LOG = LogManager.getLogger(CAT_ROOT + ".extensions");

   private static final String getRootCategory()
   {
      String fqName = Reflect.getHumanReadableClassName(ConfigLog.class, true);

      final int lastDot = fqName.lastIndexOf('.');

      return (-1 != lastDot) ? fqName.substring(0, lastDot) : fqName;
   }
}
