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
package org.eclipse.stardust.engine.api.model;

import java.util.Date;

/**
 * A client side view of a workflow participant defined at runtime.
 * A participant is a workflow element which performs manual or interactive activities.
 * 
 * @author sborn
 * @version $Revision$
 */
public interface DynamicParticipant extends Participant, DynamicParticipantInfo
{
   /**
    * Retrieves the date from on which this participant is valid.
    *
    * @return The validity start date, or null if unlimited.
    */
   Date getValidFrom();

   /**
    * Retrieves the date until this participant is valid.
    *
    * @return The validity end date, or null if unlimited.
    */
   Date getValidTo();

   /**
    * Provides the ID of the partition the dynamic participant is associated with.
    * 
    * @return The partition ID.
    */
   String getPartitionId();

   /**
    * Provides the OID of the partition the dynamic participant is associated with.
    * 
    * @return The partition OID.
    */
   short getPartitionOID();
}
