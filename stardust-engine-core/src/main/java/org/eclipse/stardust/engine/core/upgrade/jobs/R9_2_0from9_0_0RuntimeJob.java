/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *    antje.fuhrmann (SunGard CSA LLC)
 *    stephan.born (SunGard CSA LLC) - now based on SD upgrade job
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class R9_2_0from9_0_0RuntimeJob extends AT4_0_0from3_1_0RuntimeJob
{
   private static final Logger trace = LogManager
         .getLogger(R9_2_0from9_0_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(9, 2, 0);

   R9_2_0from9_0_0RuntimeJob()
   {
      super();
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }

   @Override
   public Version getVersion()
   {
      return VERSION;
   }
}
