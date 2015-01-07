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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Date;

/**
 * The LogEntry class provides information about the various messages the Carnot engine is
 * logging into AuditTrail.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface LogEntry extends Serializable
{
   /**
    * Gets the global identifier of the LogEntry.
    *
    * @return the OID of this LogEntry.
    */
   long getOID();

   /**
    * Gets the time when the log was created.
    *
    * @return the creation time.
    */
   Date getTimeStamp();

   /**
    * Gets the message contained by this log entry.
    *
    * @return the log message.
    */
   String getSubject();

   /**
    * Gets the name of the activity instance or process instance that were active when the
    * log was created or "Global" if there was no activity instances or process instances.
    *
    * @return the name of the activity instance or process instance.
    */
   String getContext();

   /**
    * Gets the OID of the activity instance that was active when the log entry was created.
    *
    * @return the activity instance OID or 0 if the log was not created during an
    *         activity execution.
    */
   long getActivityInstanceOID();

   /**
    * Gets the OID of the process instance that was active when the log entry was created.
    *
    * @return the process instance OID or 0 if the log was not created during an
    *         process execution.
    */
   long getProcessInstanceOID();

   /**
    * Gets the OID of the user that was performing an activity when the log entry was created.
    *
    * @return the user OID or 0 if the log was not created during an user activity.
    */
   long getUserOID();

   /**
    * Gets the context code of the log entry.
    *
    * @return the context code.
    */
   LogCode getCode();

   /**
    * Gets the type of the log entry.
    *
    * @return the log entry type.
    */
   LogType getType();

   /**
    * Gets the <code>User</code> object of the user that was performing an activity when the log entry was created.
    *
    * @return the <code>User</code> object or null if the log was not created during an user activity.
    */
   User getUser();
}
