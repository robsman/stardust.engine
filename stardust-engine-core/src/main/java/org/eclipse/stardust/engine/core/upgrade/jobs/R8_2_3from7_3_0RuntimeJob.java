/*******************************************************************************
 * Copyright (c) 2015, 2016 SunGard CSA LLC and others.
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

public class R8_2_3from7_3_0RuntimeJob extends AT3_0_2from2_0_0RuntimeJob
{
   private static final Logger trace = LogManager
         .getLogger(R8_2_3from7_3_0RuntimeJob.class);

   private static final Version VERSION = Version.createFixedVersion(8, 2, 3);

   R8_2_3from7_3_0RuntimeJob()
   {
      super();
   }

   @Override
   protected Logger getLogger()
   {
      return trace;
   }

   @Override
   public boolean isMandatory()
   {
      return false;
   }

   @Override
   public Version getVersion()
   {
      return VERSION;
   }

}
