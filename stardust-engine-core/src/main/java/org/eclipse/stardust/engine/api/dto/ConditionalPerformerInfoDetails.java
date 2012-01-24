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

import org.eclipse.stardust.engine.api.model.QualifiedConditionalPerformerInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;


public class ConditionalPerformerInfoDetails extends ModelParticipantInfoDetails
      implements QualifiedConditionalPerformerInfo
{
   private static final long serialVersionUID = 1L;

   public ConditionalPerformerInfoDetails(long runtimeElementOID, String id, String name,
         DepartmentInfo department)
   {
      super(runtimeElementOID, id, name, false, false, department);
   }
}
