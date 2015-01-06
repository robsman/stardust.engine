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
package org.eclipse.stardust.engine.core.integration.calendar;

import java.util.Date;

/**
 * @author gille
 * @author rsauer
 * @version $Revision$
 */
public interface IWorktimeCalendar
{
   /**
    * Computes the worktime in milliseconds the user with the ID <code>performerID<code>
    * is or was available within the timeframe between <code>tStart</code and
    * <code>tEnd</code> to perform any activity.
    * 
    * @param tStart the starting timestamp of the interval to be considered
    * @param tEnd the end timestamp of the interval to be considered
    * @param performerId the ID of the resource to be considered
    * 
    * @return The worktime between <code>tStart</code> and <code>tEnd</code> in milliseconds.
    */
   long calculateWorktime(Date tStart, Date tEnd, String performerId);
}
