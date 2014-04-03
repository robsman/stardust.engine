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
package org.eclipse.stardust.engine.core.extensions.triggers.timer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;


public class TimerTriggerValidator implements TriggerValidator, TriggerValidatorEx
{
   public Collection validate(Map attributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }

   public List validate(ITrigger trigger)
   {
      List inconsistencies = CollectionUtils.newList();

      if (trigger.getAttribute(PredefinedConstants.TIMER_TRIGGER_START_TIMESTAMP_ATT) == null)
      {
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_START_TIME_FOR_TRIGGER.raise();
         inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
      }

      // todo: (fh) access points

      return inconsistencies;
   }
}
