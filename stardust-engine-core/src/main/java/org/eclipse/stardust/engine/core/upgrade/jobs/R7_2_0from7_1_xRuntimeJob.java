/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class R7_2_0from7_1_xRuntimeJob extends AT1_1_0from1_0_0RuntimeJob
{
   private static final Logger trace = LogManager.getLogger(R7_2_0from7_1_xRuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(7, 2, 0);

   protected R7_2_0from7_1_xRuntimeJob()
   {
      super();
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }

   public Version getVersion()
   {
      return VERSION;
   }
}