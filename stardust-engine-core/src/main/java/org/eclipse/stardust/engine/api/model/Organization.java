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

/**
 * A client view of a workflow organizational unit.
 * An organization is a logical grouping of workflow participants.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Organization extends ModelParticipant, QualifiedOrganizationInfo
{
   /**
    * Gets all the organizations that are part of this one.
    *
    * @return a List of Organization objects.
    */
   List getAllSubOrganizations();

   /**
    * Gets all the roles that are part of this organization.
    *
    * @return a List of {@link Role} objects.
    */
   List getAllSubRoles();

   /**
    * Gets all the participants that are part of this organization.
    *
    * @return the cumulated list of roles and organizations that are part of this organization.
    */
   List getAllSubParticipants();
   
   /**
    * The team lead role for this organization.
    *
    * @return The team lead role, if available.
    */
   Role getTeamLead();
}
