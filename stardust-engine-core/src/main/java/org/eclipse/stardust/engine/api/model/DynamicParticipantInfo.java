/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.model;

/**
 * A client side view of a workflow participant defined at runtime with base information only.
 * A participant is a workflow element which performs manual or interactive activities.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface DynamicParticipantInfo extends ParticipantInfo
{
   /**
    * Returns the OID of this participant.
    *
    * @return The OID of this participant.
    */
   long getOID();
}
