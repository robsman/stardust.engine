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
package org.eclipse.stardust.engine.core.extensions.conditions.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventConditionValidator;


public class ExpressionConditionValidator implements EventConditionValidator
{
   public Collection validate(EventHandlerOwner context, Map attributes)
   {
      ArrayList list = new ArrayList();
      Object o = attributes.get(PredefinedConstants.WORKFLOW_EXPRESSION_ATT);
      if (!(o instanceof String) || StringUtils.isEmpty((String) o))
      {
         list.add(new Inconsistency("ExpressionCondition has no condition specified", Inconsistency.WARNING));
      }
      return list;
   }
}
