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
package org.eclipse.stardust.engine.core.extensions.conditions.exception;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ExceptionConditionAccessPointProvider implements AccessPointProvider
{
   public Iterator createIntrinsicAccessPoints(Map attributes, Map typeAttributes)
   {
      String exception = (String) attributes.get(PredefinedConstants.EXCEPTION_CLASS_ATT);
      if (!StringUtils.isEmpty(exception))
      {
         JavaAccessPoint ap = new JavaAccessPoint(PredefinedConstants.EXCEPTION_ATT,
               "Exception", Direction.OUT);
         ap.setAttribute(PredefinedConstants.CLASS_NAME_ATT, exception);
         ap.setAttribute(PredefinedConstants.EVENT_ACCESS_POINT, Boolean.TRUE);
         return new OneElementIterator(ap);
      }
      return Collections.EMPTY_LIST.iterator();
   }

}
