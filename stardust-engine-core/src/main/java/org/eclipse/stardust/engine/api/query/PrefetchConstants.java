/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

/**
 * Constants for query prefetch caches.
 *
 * @author Roland.Stamm
 */
public class PrefetchConstants
{
   public static final String HIST_STATE_PERFORMED_ON_BEHALF_OF_USER_CACHE = "Engine.Prefetch.HistoricalState.PerformedOnBehalfOfUser.Cache";

   public static final String HIST_STATE_AIH_CACHE = "Engine.Prefetch.HistoricalState.ActivityInstanceHistoryBean.Cache";

   public static final String NOTES_PI_CACHE = "Engine.Prefetch.Notes.ProcessInstanceAttributes.Cache";

   private PrefetchConstants()
   {
   }


}
