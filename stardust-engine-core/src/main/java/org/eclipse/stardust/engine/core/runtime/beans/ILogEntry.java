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

import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;


/**
 * An log entry is the persistent, transactional representation of
 * something which happened during workflow execution.
 * <p>
 * It is thought to log exceptions, important events.
 * <p>
 * The log entry might be bound to a specific process instance or even
 * a specific activity instance.
 */
public interface ILogEntry extends IdentifiablePersistent
{
   /*
    * Returns the timestamp of the log entry creation.
    */
   Date getTimeStamp();
   /*
    * Returns the (human readable) subject of the log entry.
    */
   String getSubject();

   int getType();

   int getCode();

   /*
    * The process instance for which the log entry is created
    * if there is one.
    */
   long getProcessInstanceOID();
   /*
    * The process instance for which the log entry is created
    * if there is one.
    */
   long getActivityInstanceOID();

   long getUserOID();
   
   short getPartitionOid();
}
