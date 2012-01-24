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

import java.util.Date;

import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.core.persistence.Persistent;


/**
 * Supports activity instance state change logging for process warehouse
 * data retrieval.
 */
public interface IActivityInstanceHistory extends Persistent
{
   /*
    * Retrieves the process instance for which the log has been
    * written.
    */
   public IProcessInstance getProcessInstance();

   /*
    * Retrieves the activity instance for which the log has been
    * written.
    */
   public IActivityInstance getActivityInstance();

   /*
    * Retrieves the timestamp of the log.
    */
   public Date getFrom();

   /*
    * Retrieves the timestamp of the log.
    */
   public Date getUntil();

   /*
    * Retrieves the participant of the activity instance log context.
    */
   public IParticipant getPerformer();

   /*
    * Retrieves the user of the activity instance log context.
    */
   public IUser getUser();
}
