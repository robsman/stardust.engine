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
package org.eclipse.stardust.engine.core.extensions.actions.trigger;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventActionValidator;


public class TriggerActionValidator implements EventActionValidator
{
   public Collection validate(Map attributes)
   {
      ArrayList list = new ArrayList();
      Object processId = attributes.get(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);
      if (!(processId instanceof String) || StringUtils.isEmpty((String) processId))
      {
         BpmValidationError error = BpmValidationError.ACT_NO_PROCESS_SELECTED.raise();
         list.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      return list;
   }
}
