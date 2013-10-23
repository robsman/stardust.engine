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
package org.eclipse.stardust.engine.core.extensions.triggers.manual;

import java.util.*;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;

public class ManualTriggerValidator implements TriggerValidator, TriggerValidatorEx
{
   public Collection validate(Map attributes, Iterator accessPoints)
   {
      throw new IllegalOperationException("This method is no longer supported.");
   }

   public List validate(ITrigger trigger)
   {
      List inconsistencies = new ArrayList();

      String typeId = null;
      PluggableType type = trigger.getType();
      if (type != null)
      {
         typeId = type.getId();
      }

      String participantId = (String) trigger.getAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);

      if (participantId == null)
      {
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_PARTICIPANT_FOR_TRIGGER.raise(typeId);
         inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      else
      {
         IModel model = (IModel) trigger.getModel();
      IModelParticipant starter = model.findParticipant(participantId);
         if (null == starter)
         {
            BpmValidationError error = BpmValidationError.TRIGG_INVALID_PARTICIPANT_FOR_TRIGGER.raise(
                  participantId, model.getElementOID());
            inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
         }
   }

      // todo: (fH) access points
      return inconsistencies;
   }
}
