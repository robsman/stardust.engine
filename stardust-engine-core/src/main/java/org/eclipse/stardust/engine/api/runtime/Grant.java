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

import org.eclipse.stardust.engine.api.model.Organization;


/**
 * The <code>Grant</code> class represents a permission granted to a user to perform as
 * a specific model participant (role or organization) of a model.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Grant extends Serializable
{
   /**
    * Gets the ID of the participant.
    *
    * @return the ID of the participant.
    */
   String getId();
   
   /**
    * Gets the qualified ID of the participant
    * 
    * @return the qualified id in the form "{<namespace>}<id>"
    */
   String getQualifiedId();

   /**
    * Gets the participant namespace.
    *
    * @return the ID of the model.
    */
   String getNamespace();
   
   /**
    * Gets the name of the participant.
    *
    * @return the name of the participant.
    */
   String getName();

   /**
    * Gets whether this participant is a role or an organization.
    *
    * @return true if the participant is an {@link Organization}.
    */
   boolean isOrganization();
   
   /**
    * Gets the department with which this grant is associated.
    * 
    * @return the associated {@link Department} or null if the grant has no department association.
    */
   Department getDepartment();
}
