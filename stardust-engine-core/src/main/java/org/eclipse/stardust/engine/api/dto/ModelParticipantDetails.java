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

import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


/**
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class ModelParticipantDetails extends AuditTrailModelElementDetails
      implements ModelParticipant
{
   private String description;
   private boolean isDepartmentScoped;

   protected ModelParticipantDetails(IModelParticipant participant)
   {
      super(participant);
      description = participant.getDescription();
      isDepartmentScoped = DepartmentUtils.getFirstScopedOrganization(participant) != null;
   }

   public String getDescription()
   {
      return description;
   }

   public DepartmentInfo getDepartment()
   {
      // by default not associated with a department
      return null;
   }

   public boolean definesDepartmentScope()
   {
      Boolean value = (Boolean) getAttribute(PredefinedConstants.BINDING_ATT);
      return value != null && value.booleanValue();
   }

   public boolean isDepartmentScoped()
   {
      return isDepartmentScoped;
   }
}
