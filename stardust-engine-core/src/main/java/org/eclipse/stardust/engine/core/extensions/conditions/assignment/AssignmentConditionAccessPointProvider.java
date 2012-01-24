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
package org.eclipse.stardust.engine.core.extensions.conditions.assignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AssignmentConditionAccessPointProvider implements AccessPointProvider
{
   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      List result = new ArrayList(2);

      JavaAccessPoint previousUserAp = new JavaAccessPoint(PredefinedConstants.SOURCE_USER_ATT,
            "Previous user", Direction.OUT);
      previousUserAp.setAttribute(PredefinedConstants.CLASS_NAME_ATT, Long.class.getName());
      previousUserAp.setAttribute(PredefinedConstants.EVENT_ACCESS_POINT, Boolean.TRUE);
      result.add(previousUserAp);
      
      JavaAccessPoint newUserAp = new JavaAccessPoint(PredefinedConstants.TARGET_USER_ATT,
            "New user", Direction.OUT);
      newUserAp.setAttribute(PredefinedConstants.CLASS_NAME_ATT, Long.class.getName());
      newUserAp.setAttribute(PredefinedConstants.EVENT_ACCESS_POINT, Boolean.TRUE);
      result.add(newUserAp);
      
      return result.iterator();
   }
}
