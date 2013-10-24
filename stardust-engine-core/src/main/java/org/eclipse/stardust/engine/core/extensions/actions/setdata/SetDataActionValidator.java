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
package org.eclipse.stardust.engine.core.extensions.actions.setdata;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventActionValidator;


public class SetDataActionValidator implements EventActionValidator
{
   public Collection validate(Map attributes)
   {
      ArrayList list = new ArrayList();
      if (StringUtils.isEmpty((String) attributes.get(
            PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT)))
      {
         BpmValidationError error = BpmValidationError.ACTN_NO_DATA_DEFINED.raise();
         list.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      if (StringUtils.isEmpty((String) attributes.get(
            PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT)))
      {
         BpmValidationError error = BpmValidationError.ACTN_NO_ACCESS_POINT_DEFINED.raise();
         list.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      return list;
   }
}
