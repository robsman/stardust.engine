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

import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.Predicates.notEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryDescriptor.from;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.CriticalityEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListenerAction;



/**
 * This class implements a listener action to update the criticalities of activity
 * instances of a specific process instance triggered by a modified priority
 *
 * @author thomas.wolfram
 *
 */
public class UpdateCriticalityAction implements IPersistentListenerAction
{
   public static final String CRITICALITY_PREF_RECALC_ONPRIORITY = "Criticality.Recalc.OnPriorityChange ";

   private static final Logger trace = LogManager.getLogger(ProcessInstancePersistentListener.class);

   private static final String[] criticalityTriggers = {ProcessInstanceBean.FIELD__PRIORITY};

   public UpdateCriticalityAction()
   {

   }

   public void execute(Persistent persistent)
   {
      boolean recalcOnPriorityChange = true;

      if (retrievePreferences().containsKey(CRITICALITY_PREF_RECALC_ONPRIORITY))
      {
         recalcOnPriorityChange = (Boolean) retrievePreferences().get(
               CRITICALITY_PREF_RECALC_ONPRIORITY);
      }

      ProcessInstanceBean piBean = (ProcessInstanceBean) persistent;

      // Check for modifications to trigger criticality updates and if PI is not in state change;
      if (!hasStateChanged(piBean) && hasModifiedFields(piBean, criticalityTriggers)
            && recalcOnPriorityChange)
      {

         List ais = getAiUpdateListForProcessInstance(piBean.getOID());
         try
         {
            synchronizeCriticality(ais);
         }
         catch (Exception e)
         {
            long piOid = piBean.getOID();
            AuditTrailLogger.getInstance(LogCode.DAEMON)
                  .warn(MessageFormat.format(
                        "Failed to recalculate criticality for activities of process instance {0}.",
                        new Object[] {piOid}, e));
         }
      }

   }

   /**
    * Checks, if one or more fields of a give field array have been modified
    *
    * @return {@link Boolean}
    */
   private static boolean hasModifiedFields(IProcessInstance pi, String[] fieldRefs)
   {
      if (pi.getPersistenceController().getModifiedFields() != null)
      {
         for (int i = 0; i < fieldRefs.length; i++ )
         {
            if (pi.getPersistenceController().getModifiedFields().contains(fieldRefs[i]))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Checks if the state has been changed in a process instance
    * @param pi
    * @return {@link Boolean}
    */
   private static boolean hasStateChanged(IProcessInstance pi)
   {
      if (pi.getPersistenceController().getModifiedFields() != null)
      {
         if (pi.getPersistenceController()
               .getModifiedFields()
               .contains(ProcessInstanceBean.FIELD__STATE))
         {
            return true;
         }
      }
      return false;
   }

   /**
    *
    * @param piOid
    * @return
    */
   private List getAiUpdateListForProcessInstance(long piOid)
   {
      List updateList = CollectionUtils.newList();

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryDescriptor query = from(ActivityInstanceBean.class).select(
            ActivityInstanceBean.FR__OID, ActivityInstanceBean.FR__MODEL);
      query.getQueryExtension()
            .setWhere(
                  andTerm(
                        andTerm(
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.ABORTED),
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.ABORTING),
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.COMPLETED)),
                        isEqual(ActivityInstanceBean.FR__PROCESS_INSTANCE, piOid)))
            .addOrderBy(ActivityInstanceBean.FR__OID, true);

      ResultSet rs = session.executeQuery(query);

      try
      {
         int copiedRows = 0;
         while (rs.next())
         {
            long oid = rs.getLong(ActivityInstanceBean.FIELD__OID);
            updateList.add(oid);
            copiedRows++ ;
         }
      }
      catch (SQLException sqlex)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_COULD_NOT_RETRIEVE_ACTIVITY_INSTANCE_FOR_CRITICALITY_UPDATE
                     .raise());
      }

      return updateList;
   }

   /**
    *
    * @param updateList
    */
   private void synchronizeCriticality(List updateList)
   {

      try
      {
         Map criticalityMap = CollectionUtils.newMap();

         // Create update map
         for (Iterator i = updateList.iterator(); i.hasNext();)
         {
            long oid = (Long) i.next();
            ActivityInstanceBean.findByOID(oid).updateCriticality(
                  CriticalityEvaluator.recalculateCriticality(oid));
         }
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }
   }

   private Map retrievePreferences()
   {

      Preferences viewsCommonPreferences = PreferenceStorageFactory.getCurrent()
            .getPreferences(PreferenceScope.PARTITION,
                  PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
                  PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Map<String, Serializable> preferences = viewsCommonPreferences.getPreferences();

      return preferences;
   }

}
