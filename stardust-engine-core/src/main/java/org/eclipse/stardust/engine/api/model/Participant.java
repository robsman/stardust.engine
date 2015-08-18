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

import java.util.List;
import java.util.Map;

/**
 * A client side view of a workflow participant.
 * A participant is a workflow element which performs manual or interactive activities.
 * 
 * <p>
 * Starting with the introduction of user groups, workflow participants are not
 * necessarily model elements anymore. Due to this fact the generalization association
 * between {@link ModelElement} and {@link Participant} is deprecated. Participants which
 * are defined at modelling time are now implementations of {@link ModelParticipant}
 * instead.
 * </p>
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Participant extends ModelElement, ParticipantInfo
{
   /**
    * Returns all custom attributes of this participant.
    * 
    * @return A Map with name-value pairs containing all custom attributes defined for
    *         this participant.
    */
   Map getAllAttributes();

   /**
    * Returns a specific custom attribute of this participant.
    * 
    * @param name The name of the attribute to be returned.
    * 
    * @return The attribute value.
    */
   Object getAttribute(String name);
   
   /**
    * @deprecated The inheritance association between {@link ModelElement} and
    *             {@link Participant} is deprecated. Participants which are defined in a
    *             workflow model are now implementations of {@link ModelParticipant}.
    *             Invoking this method on a non-model participant will result in a value
    *             of <code>0</code>.
    *             
    *  @see ModelParticipant#getModelOID()
    */
   short getPartitionOID();

   /**
    * @deprecated The inheritance association between {@link ModelElement} and
    *             {@link Participant} is deprecated. Participants which are defined in a
    *             workflow model are now implementations of {@link ModelParticipant}.
    *             Invoking this method on a non-model participant will result in a value
    *             of <code>null</code>.
    *             
    *  @see ModelParticipant#getModelOID()
    */
   String getPartitionId();

   /**
    * @deprecated The inheritance association between {@link ModelElement} and
    *             {@link Participant} is deprecated. Participants which are defined in a
    *             workflow model are now implementations of {@link ModelParticipant}.
    *             Invoking this method on a non-model participant will result in a value
    *             of <code>0</code>.
    *             
    *  @see ModelParticipant#getModelOID()
    */
   int getModelOID();

   /**
    * @deprecated The inheritance association between {@link ModelElement} and
    *             {@link Participant} is deprecated. Participants which are defined in a
    *             workflow model are now implementations of {@link ModelParticipant}.
    *             Invoking this method on a non-model participant will result in a value
    *             of <code>0</code>.
    *             
    *  @see ModelParticipant#getElementOID()
    */
   int getElementOID();

   /**
    * @deprecated The inheritance association between {@link ModelElement} and
    *             {@link Participant} is deprecated. Participants which are defined in a
    *             workflow model are now implementations of {@link ModelParticipant}.
    *             Invoking this method on a non-model participant will result in a value
    *             of {@link java.util.Collections#EMPTY_LIST}.
    *             
    *  @see ModelParticipant#getAllSuperOrganizations()
    */
   List getAllSuperOrganizations();

   /**
    * @deprecated The inheritance association between {@link ModelElement} and
    *             {@link Participant} is deprecated. Participants which are defined in a
    *             workflow model are now implementations of {@link ModelParticipant}.
    *             Invoking this method on a non-model participant will result in a value
    *             of <code>null</code>.
    *             
    *  @see ModelParticipant#getNamespace()
    */
   String getNamespace();
}