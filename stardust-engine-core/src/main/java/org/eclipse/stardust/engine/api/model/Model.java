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
import java.util.Set;

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;

/**
 * A client side view of a workflow model.
 * Contains information about the model as well as sub elements such as processes, roles,
 * organisations etc.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Model extends ModelElement
{
   Set<QualityAssuranceCode> getAllQualityAssuranceCodes();

   /**
    * Gets all participants defined in this model.
    *
    * @return a List of {@link Participant} objects.
    */
   List getAllParticipants();

   /**
    * Gets the specified participant.
    *
    * @param id the ID of the participant.
    *
    * @return the participand having the provided id or null if no participant was found.
    */
   Participant getParticipant(String id);

   /**
    * Gets all the organizations defined in this model.
    *
    * @return a List of {@link Organization} objects.
    */
   List getAllOrganizations();

   /**
    * Gets the specified organization.
    *
    * @param id the ID of the organization.
    *
    * @return the organization having the provided id or null if no organization was found.
    */
   Organization getOrganization(String id);

   /**
    * Gets all the roles defined in this model.
    *
    * @return a List of {@link Role} objects.
    */
   List getAllRoles();

   /**
    * Gets the specified role.
    *
    * @param id the ID of the role.
    *
    * @return the role having the provided id or null if no role was found.
    */
   Role getRole(String id);

   /**
    * Gets all the top level organizations defined in this model. A top level organization
    * is an organization which is not a part of any other organization.
    *
    * @return a List of {@link Organization} objects.
    */
   List getAllTopLevelOrganizations();

   /**
    * Gets all the top level roles defined in this model. A top level role is a role
    * which is not part of any organization.
    *
    * @return a List of {@link Role} objects.
    */
   List getAllTopLevelRoles();

   /**
    * Gets all the process definitions contained in this model.
    *
    * @return a List of {@link ProcessDefinition} objects.
    */
   List getAllProcessDefinitions();

   /**
    * Gets the specified process definition.
    *
    * @param id the ID of the process definition.
    *
    * @return the process definition having the provided id or null if no process
    *         definition was found.
    */
   ProcessDefinition getProcessDefinition(String id);

   /**
    * Gets all the data definitions contained in this model.
    *
    * @return a List of {@link Data} objects.
    */
   List getAllData();

   /**
    * Gets the specified data definition.
    *
    * @param id the ID of the data definition
    *
    * @return the data definition having the provided id or null if no data
    *         definition was found.
    */
   Data getData(String id);

   /**
    * Gets all the type declarations contained in this model.
    *
    * @return a List of {@link TypeDeclaration} objects.
    */
   List<TypeDeclaration> getAllTypeDeclarations();

   /**
    * Gets the specified type declaration.
    *
    * @param id the ID of the type declaration
    *
    * @return the type declaration having the provided id or null if no type
    *         declaration was found.
    */
   TypeDeclaration getTypeDeclaration(String id);


   /**
    * Gets the specified type declaration.
    *
    * @param documentType the documentType of a Document.
    *
    * @return the type declaration matching the provided documentType or null if no type
    *         declaration was found.
    */
   TypeDeclaration getTypeDeclaration(DocumentType documentType);

   /**
    * Gets the referenced external package ids.
    *
    * @return a collection of ids.
    */
   Set<String> getExternalPackages();

   /**
    * Gets the resolved model oid corresponding to the external package reference.
    *
    * @param  externalPackageId the id of the external package reference.
    * @return the oid of the resolved model or null if the id does not correspond
    *         to an external package reference.
    */
   Long getResolvedModelOid(String externalPackageId);

   // @todo (france, ub):
   /*List getAllEventActionTypes();
   EventActionType getEventActionType(String id);
   List getAllEventConditionTypes();
   EventConditionType getEventConditionType(String id);
   List getAllApplicationTypes();
   ApplicationType getApplicationType();
   List getAllApplicationTypes();
   ApplicationType getApplicationType();
   */

}
