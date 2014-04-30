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
package org.eclipse.stardust.engine.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * Data holder to transfer the read-only process definition data to the client
 */
public class ProcessDefinitionDetails extends AuditTrailModelElementDetails
      implements ProcessDefinition
{
   private static final long serialVersionUID = 6250589260762155929L;

   private List<Trigger> triggers = new ArrayList<Trigger>();
   private final List<DataPath> dataPaths;
   private final List<Activity> activities;
   private final List<EventHandler> eventHandlers;
   private final ProcessDefinitionDetailsLevel detailsLevel;
   private String description;
   private int defaultPriority;
   private ProcessInterface declaredProcessInterface;
   private ProcessInterface implementedProcessInterface;

   private final static Logger log = LogManager.getLogger(ProcessDefinitionDetails.class);

   ProcessDefinitionDetails(IProcessDefinition processDefinition)
   {
      super(processDefinition);

      detailsLevel = ProcessDefinitionDetailsLevel.FULL;

      for (Iterator i = processDefinition.getAllTriggers(); i.hasNext();)
      {
         triggers.add(new TriggerDetails(this, (ITrigger) i.next()));
      }

      dataPaths = DetailsFactory.<DataPath, DataPathDetails> createCollection(
            processDefinition.getDataPaths(), IDataPath.class, DataPathDetails.class);

      activities = DetailsFactory.<Activity, ActivityDetails> createCollection(
            processDefinition.getActivities(), IActivity.class, ActivityDetails.class);

      Parameters parameters = Parameters.instance();
      if (parameters.getBoolean(
            KernelTweakingProperties.SORT_ACTIVITIES_IN_TRANSITION_ORDER, true))
      {
         Map<String, Activity> activityMap = CollectionUtils.newMap();
         Queue<IActivity> queue = new LinkedList<IActivity>();
         // put all activities in queue which has no incoming transitions
         ModelElementList activities = processDefinition.getActivities();
         int length = activities.size();
         for (int i = 0; i < length; ++i)
         {
            IActivity activity = (IActivity) activities.get(i);
            for (int adIndex = 0; adIndex < this.activities.size(); ++adIndex)
            {
               Activity ad = this.activities.get(adIndex);
               if (activity.getId().equals(ad.getId()))
               {
                  activityMap.put(activity.getId(), ad);
               }
            }
            if (activity.getInTransitions().isEmpty())
            {
               queue.offer(activity);
            }
         }
         List<Activity> sortedActivities = new LinkedList<Activity>();
         while ( !queue.isEmpty())
         {
            IActivity activity = queue.poll();
            sortedActivities.add(activityMap.get(activity.getId()));
            ModelElementList outTransistions = activity.getOutTransitions();
            for (int otIndex = 0; otIndex < outTransistions.size(); ++otIndex)
            {
               ITransition transition = (ITransition) outTransistions.get(otIndex);
               IActivity toActivity = transition.getToActivity();
               if ( !queue.contains(toActivity)
                     && !sortedActivities.contains(activityMap.get(toActivity.getId())))
               {
                  queue.offer(transition.getToActivity());
               }
            }
         }
         if (sortedActivities.size() != this.activities.size())
         {
            log.warn("Sorted activities for process "
                  + getName()
                  + " doesn't match with unsorted activity list. Use unsorted list instead.");
         }
         else
         {
            this.activities.clear();
            this.activities.addAll(sortedActivities);
         }

      }

      eventHandlers = DetailsFactory.<EventHandler, EventHandlerDetails> createCollection(
            processDefinition.getAllEventHandlers(), IEventHandler.class,
            EventHandlerDetails.class);

      description = processDefinition.getDescription();

      defaultPriority = processDefinition.getDefaultPriority();

      if (processDefinition.getDeclaresInterface())
      {
         declaredProcessInterface = DetailsFactory.create(processDefinition,
               IProcessDefinition.class, ProcessInterfaceDetails.class);
      }
      else
      {
         IReference ref = processDefinition.getExternalReference();
         if (ref != null)
         {
            implementedProcessInterface = DetailsFactory.create(processDefinition,
                  IProcessDefinition.class, ProcessInterfaceDetails.class);
         }
      }
   }

   /**
    * Clones the original ProcessDefinitionDetails to the extent defined by detailsLevel.
    * The template must always have a higher or equal details level than the targeted details level.
    *
    * @param template original ProcessDefinitionDetails with FULL details level.
    * @param detailsLevel target details level.
    */
   public ProcessDefinitionDetails(ProcessDefinitionDetails template,
         ProcessDefinitionDetailsLevel detailsLevel)
   {
      super(template);
      this.detailsLevel = detailsLevel != null
            ? detailsLevel
            : ProcessDefinitionDetailsLevel.FULL;
      this.description = template.description;
      this.defaultPriority = template.defaultPriority;

      if ( !ProcessDefinitionDetailsLevel.CORE.equals(this.detailsLevel))
      {
         this.dataPaths = template.dataPaths;
         this.eventHandlers = template.eventHandlers;
         this.triggers = template.triggers;
      }
      else
      {
         triggers = Collections.EMPTY_LIST;
         dataPaths = Collections.EMPTY_LIST;
         eventHandlers = Collections.EMPTY_LIST;
      }

      if (ProcessDefinitionDetailsLevel.FULL.equals(this.detailsLevel))
      {
         this.activities = template.activities;
      }
      else
      {
         activities = Collections.EMPTY_LIST;
      }

      this.declaredProcessInterface = template.declaredProcessInterface;
      this.implementedProcessInterface = template.implementedProcessInterface;
   }

   public List getAllEventHandlers()
   {
      return Collections.unmodifiableList(eventHandlers);
   }

   public EventHandler getEventHandler(String id)
   {
      return (EventHandler) ModelApiUtils.firstWithId(eventHandlers.iterator(), id);
   }

   public List getAllTriggers()
   {
      return Collections.unmodifiableList(triggers);
   }

   public List getAllDataPaths()
   {
      return Collections.unmodifiableList(dataPaths);
   }

   public DataPath getDataPath(String id)
   {
      return (DataPath) ModelApiUtils.firstWithId(dataPaths.iterator(), id);
   }

   public List getAllActivities()
   {
      return Collections.unmodifiableList(activities);
   }

   public Activity getActivity(String id)
   {
      return (Activity) ModelApiUtils.firstWithId(activities.iterator(), id);
   }

   public String getDescription()
   {
      return description;
   }

   public int getDefaultPriority()
   {
      return defaultPriority;
   }

   public ProcessDefinitionDetailsLevel getDetailsLevel()
   {
      return detailsLevel;
   }

   public ProcessInterface getDeclaredProcessInterface()
   {
      return declaredProcessInterface;
   }

   public ProcessInterface getImplementedProcessInterface()
   {
      return implementedProcessInterface;
   }

   public String toString()
   {
      return "ProcessDefinitionDetails: " + getName();
   }
}
