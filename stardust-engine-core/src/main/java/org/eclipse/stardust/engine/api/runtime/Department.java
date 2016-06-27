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

import java.io.ObjectStreamException;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;


/**
 * @author Florin.Herinean
 */
public interface Department extends DepartmentInfo
{
   /**
    * This constants must be used to represent the default (all null) department
    * when adding user grants.
    */
   public static final Department DEFAULT = new Department()
   {
      private static final long serialVersionUID = 1L;
      
      private Object readResolve()
         throws ObjectStreamException
      {
         return Department.DEFAULT;
      }
      
      public String getDescription()
      {
         return "Default department";
      }
      
      public Organization getOrganization()
      {
         return null;
      }
      
      public Department getParentDepartment()
      {
         return null;
      }
      
      public String getId()
      {
         return null;
      }
      
      public String getName()
      {
         return "Default";
      }
      
      public long getOID()
      {
         return 0;
      }
      
      public long getRuntimeOrganizationOID()
      {
         return 0;
      }
      
      public QualifiedModelParticipantInfo getScopedParticipant(ModelParticipant participant)
            throws InvalidArgumentException
      {
         return DepartmentInfoDetails.getParticipant(this, participant);
      }
   };

   /**
    * Returns the description of this department.
    * 
    * @return The description.
    */
   String getDescription();
   
   /**
    * Returns the parent department.
    * 
    * @return The parent department.
    */
   Department getParentDepartment();
   
   /**
    * Gets the organization bound to this department.
    * 
    * @return The organization.
    */
   Organization getOrganization();

   /**
    * Creates a client side model participant bound to this department.
    * 
    * @param participant The participant.
    * @return The bound participant info.
    * @throws InvalidArgumentException if the organization is not directly
    *         or indirectly part of the organization to which the department
    *         is assigned (invalid hierarchy).
    */
   QualifiedModelParticipantInfo getScopedParticipant(ModelParticipant participant)
      throws InvalidArgumentException;
}