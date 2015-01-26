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

import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantType;


public class ConditionalPerformerDetails extends ModelParticipantDetails
      implements ConditionalPerformer
{
   private static final long serialVersionUID = 8298195206220070313L;
   private final ParticipantType type;
   
   public ConditionalPerformerDetails(IConditionalPerformer source)
   {
      super(source);

      this.type = source.getPerformerKind();
   }
   
   public ParticipantType getPerformerKind()
   {
      return type;
   }

   public Participant getResolvedPerformer()
   {
      // by default don't resolve
      return null;
   }

   public List<Organization> getAllSuperOrganizations()
   {
      return Collections.emptyList();
   }

   public boolean definesDepartmentScope()
   {
      // conditional performers never supports departments
      return false;
   }

   public boolean isDepartmentScoped()
   {
      // conditional performers are never scoped
      return false;
   }
}
