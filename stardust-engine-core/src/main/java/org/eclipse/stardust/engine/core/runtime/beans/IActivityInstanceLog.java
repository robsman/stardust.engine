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

import java.util.Calendar;

import org.eclipse.stardust.engine.core.persistence.Persistent;


/**
 * Supports activity instance state change logging for process warehouse
 * data retrieval.
 */
public interface IActivityInstanceLog extends Persistent
{
   public static final int CREATION = 0;
   public static final int ACTIVATION = 1;
   public static final int SUSPEND = 2;
   public static final int COMPLETION = 3;
   public static final int INTERRUPTION = 4;
   public static final int ABORTION = 5;
   /*
    * Retrieves the activity instances for which the log has been
    * written.
    */
   public IActivityInstance getActivityInstance();
   /*
    * Retrieves the timestamp of the log.
    */
   public Calendar getTimeStamp();
   /*
    * Retrieves the participant of the activity instance log context.
    */
   public long getParticipant();
   /*
    * Retrieves the user of the activity instance log context.
    */
   public IUser getUser();
}
