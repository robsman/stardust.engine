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

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;


/**
 * @author mgille
 */
public interface IModelParticipant extends IParticipant, IViewable, IdentifiableElement
{
   IOrganization findOrganization(String id);

   void setDescription(String description);

   Iterator getAllOrganizations();

   /**
    * Returns the qualified ID of this participant
    * 
    * @return The qualified ID of this participant.
    */
   String getQualifiedId();   
   
   /**
    * Retrieves an iterator over all top-level organizations, the user is directly or indirectly
    * participating in. A top-level organization is an organization without a super organization.
    */
   Iterator getAllTopLevelOrganizations();

   /**
    * This method is used to determine, whether the passed participant is allowed to act
    * as myself.
    */
   boolean isAuthorized(IModelParticipant participant);

   /**
    * This method is used to determine, whether the passed user is allowed to act
    * as myself.
    */
   boolean isAuthorized(IUser user);
   
   /**
    * This method is used to determine, whether the passed user group is allowed to act
    * as myself.
    */
   boolean isAuthorized(IUserGroup userGroup);

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the participant.
    */
   void checkConsistency(List inconsistencies);

   Iterator getAllParticipants();

   int getCardinality();

   void addToOrganizations(IOrganization org);
}