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
package org.eclipse.stardust.engine.core.runtime.logging;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


public class RuntimeLog
{
   private static final String CAT_ROOT = getRootCategory();
   
   public static final Logger CONFIGURATION = LogManager.getLogger(CAT_ROOT + ".Configuration");
   
   public static final Logger TX_MGMT = LogManager.getLogger(CAT_ROOT + ".Tx");
   
   public static final Logger SQL = LogManager.getLogger(CAT_ROOT + ".SQL");
   
   public static final Logger SECURITY = LogManager.getLogger(CAT_ROOT + ".Security");
   
   public static final Logger EJB = LogManager.getLogger(CAT_ROOT + ".EJB");
   
   public static final Logger WF_EVENT = LogManager.getLogger(CAT_ROOT + ".WfEvent");
   
   public static final Logger PERFORMANCE = LogManager.getLogger(CAT_ROOT + ".Performance");

   public static final Logger DAEMON = LogManager.getLogger(CAT_ROOT + ".Daemon");
   
   public static final Logger API = LogManager.getLogger(CAT_ROOT + ".API");

   public static final Logger SPI = LogManager.getLogger(CAT_ROOT + ".SPI");   
   
   private static final String getRootCategory()
   {
      String fqName = Reflect.getHumanReadableClassName(RuntimeLog.class, true);

      final int lastDot = fqName.lastIndexOf('.');
      
      return (-1 != lastDot) ? fqName.substring(0, lastDot) : fqName;
   }
}