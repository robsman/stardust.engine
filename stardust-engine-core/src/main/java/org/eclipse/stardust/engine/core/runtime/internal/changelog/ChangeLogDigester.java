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
package org.eclipse.stardust.engine.core.runtime.internal.changelog;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.FactoryFinder;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.spi.IChangeLogDigestionStrategy;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.spi.IChangeLogDigestionStrategyFactory;

/**
 * @author rsauer
 * @version $Revision$
 */
public class ChangeLogDigester
{

   public static final String PRP_DIGESTER_INSTANCE = ChangeLogDigester.class.getName()
         + ".INSTANCE";

   public static final String PRP_AIH_PREFIX = "Carnot.AuditTrail.ActivityInstanceHistory";

   public static final String PRP_AIH_ENABLED = PRP_AIH_PREFIX + ".Enabled";

   public static final String PRP_AIH_IMPL_TYPE_FILTER = PRP_AIH_PREFIX
         + ".ImplementationTypeFilter";

   public static final String AIH_FILTER_INTERACTIVE_AND_APPLICATION = StringUtils.join(
         ImplementationType.Manual.getId(), ImplementationType.Application.getId(), ",");

   public static final String PRP_STATE_FILTER = PRP_AIH_PREFIX + ".StateFilter";
   
   private final boolean aiHistoryEnabled;

   private final Set aiImplementationTypeFilter;
   
   private final boolean aiImplementationTypeFilterInteractiveApplications;
   
   private final IChangeLogDigestionStrategy digestionStrategy;

   public static ChangeLogDigester instance()
   {
      GlobalParameters globals = GlobalParameters.globals();

      ChangeLogDigester manager = (ChangeLogDigester) globals.get(PRP_DIGESTER_INSTANCE);

      if (null == manager)
      {
         synchronized (globals)
         {
            // double checked locking is fine here as we read from a map, not from a field
            manager = (ChangeLogDigester) globals.get(PRP_DIGESTER_INSTANCE);

            if (null == manager)
            {
               manager = new ChangeLogDigester();
               globals.set(PRP_DIGESTER_INSTANCE, manager);
            }
         }
      }

      return manager;
   }

   public static long getExpirationTime(long lastModificationTime)
   {
      // TODO retrieve from parameters
      final int automaticSessionTimeout = 2 * 60 * 60;

      final Calendar expirationTimeCalculator = Calendar.getInstance();

      expirationTimeCalculator.setTimeInMillis(lastModificationTime);
      expirationTimeCalculator.add(Calendar.SECOND, automaticSessionTimeout);
      return expirationTimeCalculator.getTimeInMillis();
   }

   public ChangeLogDigester()
   {
      final Parameters params = Parameters.instance();

      this.aiHistoryEnabled = params.getBoolean(PRP_AIH_ENABLED, true);

      final String aihImplTypeFilterParam = params.getString(PRP_AIH_IMPL_TYPE_FILTER,
            AIH_FILTER_INTERACTIVE_AND_APPLICATION);

      Set aiImplTypeFilter = CollectionUtils.newSet();
      if (("InteractiveActivities").equals(aihImplTypeFilterParam))
      {
         aiImplTypeFilter.add(ImplementationType.Manual);
         aiImplTypeFilter.add(ImplementationType.Application);
         this.aiImplementationTypeFilterInteractiveApplications = true;
      }
      else
      {
         this.aiImplementationTypeFilterInteractiveApplications = false;
         for (Iterator i = StringUtils.split(aihImplTypeFilterParam, ","); i.hasNext();)
         {
            String typeId = (String) i.next();
            
            ImplementationType type = ImplementationType.get(typeId);
            if (null != type)
            {
               aiImplTypeFilter.add(type);
            }
         }
      }

      this.aiImplementationTypeFilter = Collections.unmodifiableSet(aiImplTypeFilter);

      IChangeLogDigestionStrategyFactory digesterFactory = (IChangeLogDigestionStrategyFactory) FactoryFinder.findFactory(
            IChangeLogDigestionStrategyFactory.class,
            DefaultChangeLogDigestionStrategy.Factory.class, null);
      this.digestionStrategy = digesterFactory.createDigester();
   }

   public boolean isAiHistoryEnabled()
   {
      return aiHistoryEnabled;
   }

   public Set getAiImplementationTypeFilter()
   {
      return aiImplementationTypeFilter;
   }

   public boolean isAiChangeLogEnabled(IActivityInstance activityInstance)
   {
      ImplementationType implementationType = activityInstance.getActivity()
            .getImplementationType();

      boolean result;
      
      if ( !isAiHistoryEnabled()
            || !aiImplementationTypeFilter.contains(implementationType))
      {
         // logging is disabled at all
         result = false;
      }
      else if ((ImplementationType.Application == implementationType)
            && aiImplementationTypeFilterInteractiveApplications
            && !activityInstance.getActivity().isInteractive())
      {
         // logging is disabled for non-interactive applications
         result = false;
      }
      else
      {
         result = true;
      }
      
      return result;
   }

   public List/*<HistoricState>*/ digestChangeLog(IActivityInstance activityInstance,
         List/*<HistoricState>*/ changeLog)
   {
      List result;

      if (isAiChangeLogEnabled(activityInstance))
      {
         if (null != digestionStrategy)
         {
            result = digestionStrategy.digestChangeLog(activityInstance, changeLog);
         }
         else
         {
            result = changeLog;
         }
      }
      else
      {
         result = Collections.EMPTY_LIST;
      }
      
      return result;
   }

   public static class HistoricState
   {
      private boolean newRecord = true;
      
      private final Date from;
      
      public Date until;
      
      private final ActivityInstanceState state;
   
      private final IParticipant performer;
      private final IDepartment department;
   
      public HistoricState(Date from, ActivityInstanceState state,
            IParticipant performer, IDepartment department)
      {
         this.from = from;
   
         this.state = state;
         this.performer = performer;
         this.department = department;
      }
   
      public HistoricState(Date from, Date until, ActivityInstanceState state,
            IParticipant performer, IDepartment department)
      {
         this.from = from;
         this.until = until;
   
         this.state = state;
         this.performer = performer;
         this.department = department;
      }
   
      public boolean isNewRecord()
      {
         return newRecord;
      }
   
      public boolean isUpdatedRecord()
      {
         return !isNewRecord();
      }
   
      public void setUpdatedRecord()
      {
         this.newRecord = false;
      }
   
      public Date getFrom()
      {
         return from;
      }
   
      public Date getUntil()
      {
         return until;
      }
   
      public void setUntil(Date until)
      {
         this.until = until;
      }
   
      public ActivityInstanceState getState()
      {
         return state;
      }
   
      public IParticipant getPerformer()
      {
         return performer;
      }
      
      public IDepartment getDepartment()
      {
         return department;
      }
   }   
}
