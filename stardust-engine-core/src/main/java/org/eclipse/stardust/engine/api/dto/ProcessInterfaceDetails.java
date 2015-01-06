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

import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.api.model.FormalParameter;
import org.eclipse.stardust.engine.api.model.IFormalParameter;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.ProcessInterface;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


public class ProcessInterfaceDetails
   implements ProcessInterface
{
   private static final long serialVersionUID = 1L;
   
   private QName processName;

   private List<FormalParameter> parameters;
   
   ProcessInterfaceDetails(IProcessDefinition process)
   {
      IProcessDefinition target = process;
      IReference ref = process.getExternalReference();
      if (ref != null)
      {
         IModel model = ref.getExternalPackage().getReferencedModel();
         target = model.findProcessDefinition(ref.getId());
      }
      processName = new QName(target.getModel().getId(), target.getId());
      parameters = DetailsFactory.<FormalParameter, FormalParameterDetails>createCollection(
            target.getFormalParameters(), IFormalParameter.class, FormalParameterDetails.class);
   }

   public QName getDeclaringProcessDefinitionId()
   {
      return processName;
   }

   public List<FormalParameter> getFormalParameters()
   {
      return parameters;
   }
}
