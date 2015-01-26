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

/**
 * A client view of a workflow conditional performer.
 * Conditional performers allows to use late bound participant associations, i.e. deriving
 * the concrete participant from process state.
 * 
 * @author rsauer
 * @version $Revision$
 */
public interface ConditionalPerformer extends ModelParticipant, QualifiedConditionalPerformerInfo
{
   /**
    * Retrieves the kind of participant this conditional performer is supposed to resolve
    * to at runtime.
    * 
    * @return The runtime-type of this conditional performer.
    */
   ParticipantType getPerformerKind();

   /**
    * Retrieves the performer this conditional performer resolves to at runtime. This
    * resolve operation can only be performed if this conditional performer was obtained
    * in the context of a process or activity instance.
    * 
    * @return The resolved participant. <code>null</code> if the participant could not
    *         be resolved.
    */
   Participant getResolvedPerformer();
}
