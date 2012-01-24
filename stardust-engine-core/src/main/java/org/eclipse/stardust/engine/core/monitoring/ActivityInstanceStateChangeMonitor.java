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
package org.eclipse.stardust.engine.core.monitoring;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.CriticalityEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IActivityInstanceMonitor;



/**
 * 
 * @author thomas.wolfram
 * 
 */
public class ActivityInstanceStateChangeMonitor implements IActivityInstanceMonitor
{
   public static final String CRITICALITY_PREF_RECALC_ONSUSPEND = "Criticality.Recalc.OnSuspend";

   public static final String CRITICALITY_PREF_RECALC_ONCREATE = "Criticality.Recalc.OnCreate";

   private static final Logger trace = LogManager.getLogger(ActivityInstanceStateChangeMonitor.class);

   public ActivityInstanceStateChangeMonitor()
   {
   }

   public void activityInstanceStateChanged(IActivityInstance activity, int newState)
   {
      boolean recalcOnCreate = true;

      boolean recalcOnSuspend = true;

      if (retrievePreferences().containsKey(CRITICALITY_PREF_RECALC_ONCREATE))
      {
         recalcOnCreate = (Boolean) retrievePreferences().get(
               CRITICALITY_PREF_RECALC_ONCREATE);
      }

      if (retrievePreferences().containsKey(CRITICALITY_PREF_RECALC_ONSUSPEND))
      {
         recalcOnSuspend = (Boolean) retrievePreferences().get(
               CRITICALITY_PREF_RECALC_ONSUSPEND);
      }

      if ((activity.getState() == ActivityInstanceState.Application
            && newState == ActivityInstanceState.SUSPENDED && recalcOnSuspend)
            || (activity.getState() == ActivityInstanceState.Created
                  && newState == ActivityInstanceState.CREATED && recalcOnCreate))
      {
         try
         {
            ((ActivityInstanceBean) activity).updateCriticality(CriticalityEvaluator.recalculateCriticality(activity.getOID()));

            if (trace.isDebugEnabled())
            {
               trace.debug("Criticality for suspended activity instance <"
                     + activity.getOID() + "> has been calculated as <"
                     + activity.getOID() + ">.");
            }
         }
         catch (Exception e)
         {
            AuditTrailLogger.getInstance(LogCode.ENGINE)
                  .warn(MessageFormat.format(
                        "Failed to write criticality for activity instance {0}, no criticality has been set.",
                        new Object[] {activity.getOID()}, e));
         }
      }

   }

   private Map retrievePreferences()
   {

      Preferences criticalityPreferences = PreferenceStorageFactory.getCurrent()
            .getPreferences(PreferenceScope.PARTITION,
                  PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
                  PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Map<String, Serializable> preferences = criticalityPreferences.getPreferences();

      return preferences;
   }

}
