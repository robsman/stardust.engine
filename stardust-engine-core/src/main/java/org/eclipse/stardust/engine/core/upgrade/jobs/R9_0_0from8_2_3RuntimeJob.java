/*******************************************************************************
 * Copyright (c) 2015, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class R9_0_0from8_2_3RuntimeJob extends AT3_1_0from3_0_2RuntimeJob
{
   private static final Logger trace = LogManager.getLogger(R9_0_0from8_2_3RuntimeJob.class);

   static final Version VERSION = Version.createFixedVersion(9, 0, 0);

   protected R9_0_0from8_2_3RuntimeJob()
   {
      super();
   }

   @Override
   public Version getVersion()
   {
      return VERSION;
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
