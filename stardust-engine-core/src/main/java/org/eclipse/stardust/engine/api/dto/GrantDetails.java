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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;


/**
 * Default implementation of a Grant.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class GrantDetails implements Grant
{
   private static final long serialVersionUID = 2L;
   
   private String id;
   private String namespace;
   private String qualifiedId;
   private String name;
   private boolean isOrganization;
   private Department department;

   // TODO: (fh) remove this constructor because it adds a dependency on the engine internals
   public GrantDetails(IModelParticipant participant, Department department)
   {
      id = participant.getId();
      namespace = PredefinedConstants.ADMINISTRATOR_ROLE.equals(id) ? null : participant.getModel().getId();
      qualifiedId = PredefinedConstants.ADMINISTRATOR_ROLE.equals(id) ? id : '{' + namespace + '}' + id;
      name = participant.getName();
      isOrganization = participant instanceof IOrganization;
      this.department = department;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public String getNamespace()
   {
      return namespace;
   }

   /**
    * {@inheritDoc}
    */
   public String getQualifiedId()
   {
      return qualifiedId;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isOrganization()
   {
      return isOrganization;
   }

   /**
    * {@inheritDoc}
    */
   public Department getDepartment()
   {
      return department;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      if (!(other instanceof Grant))
      {
         return false;
      }
      return CompareHelper.areEqual(qualifiedId, ((Grant) other).getQualifiedId());
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return "Grant: " + qualifiedId;
   }
}