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

import java.util.Date;


public final class RealtimeTimestampProvider implements TimestampProvider
{
   public static final RealtimeTimestampProvider INSTANCE = new RealtimeTimestampProvider();

   public Date getTimestamp()
   {
      return new Date(getTimestampValue());
   }

   public long getTimestampValue()
   {
      return System.currentTimeMillis();
   }
}
