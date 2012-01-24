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
package org.eclipse.stardust.engine.extensions.jms.app;

import java.util.Date;

import org.eclipse.stardust.common.config.TimestampProvider;


public class RecordedTimestampProvider implements TimestampProvider
{
   public static final String PROP_EVENT_TIME = "eventTime";
   
   private long timestamp;
   
   public RecordedTimestampProvider(long timestamp)
   {
      this.timestamp = timestamp;
   }

   public synchronized Date getTimestamp()
   {
      return new Date(timestamp++);
   }
}
