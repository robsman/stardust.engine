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

import java.io.Serializable;

/**
 * A client side view of a workflow participant with base information only.
 * A participant is a workflow element which performs manual or interactive activities.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface ParticipantInfo extends Serializable
{
   /**
    * Returns the qualified ID of this participant, uniquely identifying the participant within its
    * domain.
    *
    * @return The qualified ID of this participant.
    */
   String getQualifiedId();

   /**
    * Returns the ID of this participant, uniquely identifying the participant within its
    * domain.
    *
    * @return The ID of this participant.
    */
   String getId();

   /**
    * Returns the name of this participant.
    *
    * @return The name of this participant.
    */
   String getName();
}