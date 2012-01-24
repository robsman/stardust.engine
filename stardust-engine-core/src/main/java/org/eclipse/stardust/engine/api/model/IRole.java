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

/**
 * A role represents a workflow participant, different users can act as.
 */
public interface IRole extends IModelParticipant
{
   /**
    * Gets the cardinality of this role.
    */
   int getCardinality();

   /**
    * Sets the cardinality of this role.
    */
   void setCardinality(int cardinality);
   
   /**
    * Retrieves the organizations this role serves as team lead.
    */
   Iterator getAllTeams();
   
   /**
    * Retrieves the organizations this role works for.
    */
   Iterator getAllClientOrganizations();

   void addToTeams(IOrganization org);
   
}
