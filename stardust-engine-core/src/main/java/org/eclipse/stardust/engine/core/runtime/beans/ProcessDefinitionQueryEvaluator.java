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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public class ProcessDefinitionQueryEvaluator extends ModelAwareQueryPredicate<IProcessDefinition>
{
   public ProcessDefinitionQueryEvaluator(ProcessDefinitionQuery query)
   {
      super(query);
   }

   public Object getValue(IProcessDefinition process, String attribute, Object expected)
   {
      if (ProcessDefinitionQuery.TRIGGER_TYPE.getAttributeName().equals(attribute))
      {
         if (expected == null)
         {
            return "";
         }
         for (ITrigger trigger : (ModelElementList<ITrigger>) process.getTriggers())
         {
            PluggableType type = trigger.getType();
            if (type != null)
            {
               String id = type.getId();
               if (expected.equals(id))
               {
                  String participantId = (String) trigger.getAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
                  IModelParticipant participant = ((IModel) process.getModel()).findParticipant(participantId);
                  if (participant != null
                        && participant.isAuthorized(SecurityProperties.getUser()))
                  {
                     return id;
                  }
               }
            }
         }
         return null;
      }
      else if (ProcessDefinitionQuery.INVOCATION_TYPE.getAttributeName().equals(attribute))
      {
         if (process.getDeclaresInterface())
         {
               return process.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE);

         }
         return null;
      }
      else
      {
         return super.getValue(process, attribute, expected);
      }
   }
}
