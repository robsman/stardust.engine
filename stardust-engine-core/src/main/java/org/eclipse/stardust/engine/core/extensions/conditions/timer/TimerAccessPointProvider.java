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
package org.eclipse.stardust.engine.core.extensions.conditions.timer;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TimerAccessPointProvider implements AccessPointProvider
{
   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      JavaAccessPoint ap = new JavaAccessPoint(PredefinedConstants.TARGET_TIMESTAMP_ATT,
            "Time stamp", Direction.OUT);
      ap.setAttribute(PredefinedConstants.CLASS_NAME_ATT, Long.class.getName());
      ap.setAttribute(PredefinedConstants.EVENT_ACCESS_POINT, Boolean.TRUE);

      return new OneElementIterator(ap);
   }
}
