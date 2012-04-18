/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

/**
 * <p>
 * This utility class allows for creating departments for testing purposes.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DepartmentHome
{
   /**
    * <p>
    * Creates a new department for the specified organization with the given department ID and parent.
    * </p>
    * 
    * @param deptId the ID the department's <i>id</i>, <i>name</i>, and <i>description</i> will be initialized with
    * @param orgId the ID of the organization the department should be created for
    * @param parent the parent department, if any
    * @param sf a service factory needed for creating the user
    * @return the created department
    */
   public static Department create(final String deptId, final String orgId, final DepartmentInfo parent, final ServiceFactory sf)
   {
      if (deptId == null)
      {
         throw new NullPointerException("Department ID must not be null.");
      }
      if (deptId.isEmpty())
      {
         throw new IllegalArgumentException("Department ID must not be empty.");
      }
      if (orgId == null)
      {
         throw new NullPointerException("Organization ID must not be null.");
      }
      if (orgId.isEmpty())
      {
         throw new IllegalArgumentException("Organization ID must not be empty.");
      }
      /* parent may be null */
      if (sf == null)
      {
         throw new NullPointerException("Service Factory must not be null.");
      }
      
      final Participant participant = sf.getQueryService().getParticipant(orgId);
      if ( !(participant instanceof Organization))
      {
         throw new IllegalArgumentException("'" + orgId + "' is not an organization.");
      }
      final Organization org = (Organization) participant;
      return sf.getAdministrationService().createDepartment(deptId, deptId, deptId, parent, org);
   }
   
   private DepartmentHome()
   {
      /* utility class; do not allow the creation of an instance */
   }
}
