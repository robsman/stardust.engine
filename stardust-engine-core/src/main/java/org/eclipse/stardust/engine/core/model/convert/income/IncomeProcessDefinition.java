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
package org.eclipse.stardust.engine.core.model.convert.income;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;


public class IncomeProcessDefinition extends IncomeElement
{
   public static final Logger log = Logger.getLogger(IncomeProcessDefinition.class);

   public static final String LOAD_QUERY = "select d.dia_id, dt.diat_short_name, dt.diat_description from inc4_diagrams d, inc4_diagram_texts dt where dia_pro_id = ? and dia_type = 0 and dt.diat_dia_id = d.dia_id";

   public final static String ID_FIELD = "DIA_ID";

   public final static String NAME_FIELD = "DIAT_SHORT_NAME";

   public final static String DESCRIPTION_FIELD = "DIAT_DESCRIPTION";

   private Set activities;

   private Set objectstores;

   public IncomeProcessDefinition(String id, String name, String description)
   {
      super(id, name, description);
      this.activities = new HashSet();
      this.objectstores = new HashSet();
   }

   public Object create(IProcessDefinition processDefinition)
   {

      this.findAndPrepareANDSplitsWithFollowingORSplit();
      this.findAndPrepareANDJoinWithPreviousORJoin();

      IncomeActivity processStartActivity = this.findAndPrepareStartActivity();

      this.traverse(processStartActivity, null, processDefinition);

      return processStartActivity;

   }

   private void traverse(IncomeActivity activity, IncomeActivity previousActivity,
         IProcessDefinition processDefinition)
   {
      if (!activity.getOutConnections().isEmpty())
      {
         for (Iterator activityConnectionIterator = activity.getOutConnections()
               .iterator(); activityConnectionIterator.hasNext();)
         {
            IncomeConnection activityConnection = (IncomeConnection) activityConnectionIterator
                  .next();

            IncomeActivity currentActivity = activityConnection.getActivity();

            for (Iterator objectstoreConnectionIterator = activityConnection
                  .getObjectstore().getOutConnections().iterator(); objectstoreConnectionIterator
                  .hasNext();)
            {
               IncomeConnection objectstoreConnection = (IncomeConnection) objectstoreConnectionIterator
                     .next();

               IncomeActivity nextActivity = objectstoreConnection.getActivity();

               IActivity carnotActivity = processDefinition
                     .findActivity(activityConnection.getActivity().getId());

               if (carnotActivity == null)
               {
                  carnotActivity = (IActivity) currentActivity.create(processDefinition);
               }

               this
                     .createTransition(previousActivity, currentActivity,
                           processDefinition);

               this.traverse(nextActivity, currentActivity, processDefinition);
            }
         }
      }
      else
      {
         IActivity carnotActivity = processDefinition.findActivity(activity.getId());

         if (carnotActivity == null)
         {
            carnotActivity = (IActivity) activity.create(processDefinition);
         }

         this.createTransition(previousActivity, activity, processDefinition);
      }
   }

   public Set getActivities()
   {
      return activities;
   }

   public void addActivity(IncomeActivity activity)
   {
      this.activities.add(activity);
   }

   public Set getObjectstores()
   {
      return objectstores;
   }

   public void addObjectstore(IncomeObjectstore objectstore)
   {
      this.objectstores.add(objectstore);
   }

   /**
    * Method finds and prepare AND splits with following or splits. In this case the
    * objectstore of the income model must be modelled as furhter activity. This is done
    * with the income onject model. After preparing the income object model will be
    * converted into carnot model elements.
    */
   private void findAndPrepareANDSplitsWithFollowingORSplit()
   {
      List connections = new ArrayList();

      for (Iterator activityIterator = this.activities.iterator(); activityIterator
            .hasNext();)
      {
         IncomeActivity activity = (IncomeActivity) activityIterator.next();
         if (activity.getOutConnections().size() > 1) // AND Split
         {
            for (Iterator activityConnectionIterator = activity.getOutConnections()
                  .iterator(); activityConnectionIterator.hasNext();)
            {
               IncomeConnection activityConnection = (IncomeConnection) activityConnectionIterator
                     .next();
               IncomeObjectstore objectstore = activityConnection.getObjectstore();

               if (objectstore.getOutConnections().size() > 1) // OR Split
               {
                  connections.add(activityConnection);
               }
            }
         }
      }

      for (int i = 0; i < connections.size(); i++)
      {
         IncomeConnection connection = (IncomeConnection) connections.get(i);
         IncomeConnection outConnection = new IncomeConnection("CIN", "CIN",
               IncomeConnection._DEFAULT_DESCRIPTION,
               IncomeConnection.OUT_CONNECTION_TYPE);
         IncomeConnection inConnection = new IncomeConnection("COUT", "COUT",
               IncomeConnection._DEFAULT_DESCRIPTION, IncomeConnection.IN_CONNECTION_TYPE);
         IncomeObjectstore newObjectstore = new IncomeObjectstore(connection
               .getActivity().getId(), connection.getActivity().getId(),
               IncomeObjectstore._DEFAULT_DESCRIPTION);

         IncomeActivity newActivity = new IncomeActivity(connection.getActivity().getId()
               + connection.getObjectstore().getId(),
               IncomeActivity._ROUTE_ACTIVITY_NAME, IncomeActivity._DEFAULT_DESCRIPTION);

         outConnection.setActivity(connection.getActivity());
         outConnection.setObjectstore(newObjectstore);

         inConnection.setActivity(newActivity);
         inConnection.setObjectstore(newObjectstore);

         connection.setActivity(newActivity);

         this.activities.add(newActivity);
      }

   }

   /**
    * Method finds and prepare AND joins with previous OR joins. In this case the
    * objectstore of the income model must be modelled as furhter activity. This is done
    * with the income onject model. After preparing the income object model will be
    * converted into carnot model elements.
    */
   private void findAndPrepareANDJoinWithPreviousORJoin()
   {
      List connections = new ArrayList();

      for (Iterator activityIterator = this.activities.iterator(); activityIterator
            .hasNext();)
      {
         IncomeActivity activity = (IncomeActivity) activityIterator.next();
         if (activity.getInConnections().size() > 1) // AND Join
         {
            for (Iterator activityConnectionIterator = activity.getInConnections()
                  .iterator(); activityConnectionIterator.hasNext();)
            {
               IncomeConnection activityConnection = (IncomeConnection) activityConnectionIterator
                     .next();
               IncomeObjectstore objectstore = activityConnection.getObjectstore();

               if (objectstore.getInConnections().size() > 1) // OR Join
               {
                  connections.add(activityConnection);
               }
            }
         }
      }

      for (int i = 0; i < connections.size(); i++)
      {
         IncomeConnection connection = (IncomeConnection) connections.get(i);
         IncomeConnection outConnection = new IncomeConnection("CIN", "CIN",
               IncomeConnection._DEFAULT_DESCRIPTION,
               IncomeConnection.OUT_CONNECTION_TYPE);
         IncomeConnection inConnection = new IncomeConnection("COUT", "COUT",
               IncomeConnection._DEFAULT_DESCRIPTION, IncomeConnection.IN_CONNECTION_TYPE);
         IncomeObjectstore newObjectstore = new IncomeObjectstore(connection
               .getActivity().getId(), connection.getActivity().getId(),
               IncomeObjectstore._DEFAULT_DESCRIPTION);

         IncomeActivity newActivity = new IncomeActivity(connection.getActivity().getId()
               + connection.getObjectstore().getId(),
               IncomeActivity._ROUTE_ACTIVITY_NAME, IncomeActivity._DEFAULT_DESCRIPTION);

         outConnection.setActivity(newActivity);
         outConnection.setObjectstore(newObjectstore);

         inConnection.setActivity(connection.getActivity());
         inConnection.setObjectstore(newObjectstore);

         connection.setActivity(newActivity);

         this.activities.add(newActivity);
      }

   }

private IncomeActivity findAndPrepareStartActivity()
   {
      IncomeActivity processStartActivity = null;

      ArrayList startActivities = new ArrayList();
      ArrayList startObjectstores = new ArrayList();

      for (Iterator activityIterator = this.activities.iterator(); activityIterator
            .hasNext();)
      {
         IncomeActivity activity = (IncomeActivity) activityIterator.next();
         if (activity.isProcessStartActivity())
         {
            startActivities.add(activity);
         }
      }

      for (Iterator objectstoreIterator = this.objectstores.iterator(); objectstoreIterator
            .hasNext();)
      {
         IncomeObjectstore objectstore = (IncomeObjectstore) objectstoreIterator.next();
         if (objectstore.isProcessStartActivity())
         {
            startObjectstores.add(objectstore);
         }
      }

      if (startActivities.isEmpty() && startObjectstores.isEmpty())
      {
         log.error("No process start activity found.");
         throw new RuntimeException("No process start activity found.");
      }

      if (!startActivities.isEmpty() && !startObjectstores.isEmpty())
      {
         log.error("More than one process start activity found.");
         throw new RuntimeException("More than one process start activity found.");
      }

      if (startActivities.isEmpty() && !startObjectstores.isEmpty())
      {
         processStartActivity = new IncomeActivity(IncomeActivity._START_ACTIVITY_ID,
               IncomeActivity._ROUTE_ACTIVITY_NAME, IncomeActivity._DEFAULT_DESCRIPTION);

         for (int i = 0; i < startObjectstores.size(); i++)
         {
            IncomeObjectstore objectstore = (IncomeObjectstore) startObjectstores.get(i);
            IncomeConnection outConnection = new IncomeConnection("COUT" + i, "COUT" + i,
                  IncomeConnection._DEFAULT_DESCRIPTION,
                  IncomeConnection.OUT_CONNECTION_TYPE);

            outConnection.setActivity(processStartActivity);
            outConnection.setObjectstore(objectstore);

            this.activities.add(processStartActivity);
         }
      }
      else if (startActivities.size() == 1)
      {
         processStartActivity = (IncomeActivity) startActivities.iterator().next();
      }
      else
      {
         processStartActivity = new IncomeActivity(IncomeActivity._START_ACTIVITY_ID,
               IncomeActivity._ROUTE_ACTIVITY_NAME, IncomeActivity._DEFAULT_DESCRIPTION);

         for (int i = 0; i < startActivities.size(); i++)
         {
            IncomeActivity activity = (IncomeActivity) startActivities.get(i);
            IncomeObjectstore objectstore = new IncomeObjectstore("OS" + i, "OS" + i,
                  IncomeObjectstore._DEFAULT_DESCRIPTION);
            IncomeConnection inConnection = new IncomeConnection("CIN" + i, "CIN" + i,
                  IncomeObjectstore._DEFAULT_DESCRIPTION,
                  IncomeConnection.IN_CONNECTION_TYPE);
            IncomeConnection outConnection = new IncomeConnection("COUT" + i, "COUT" + i,
                  IncomeObjectstore._DEFAULT_DESCRIPTION,
                  IncomeConnection.OUT_CONNECTION_TYPE);

            outConnection.setActivity(processStartActivity);
            outConnection.setObjectstore(objectstore);

            inConnection.setActivity(activity);
            inConnection.setObjectstore(objectstore);

            this.objectstores.add(objectstore);
         }
         this.activities.add(processStartActivity);
      }
      return processStartActivity;
   }   private void createTransition(IncomeActivity previousActivity,
         IncomeActivity currentActivity, IProcessDefinition processDefinition)
   {

      if (previousActivity != null
            && currentActivity != null
            && processDefinition.findTransition(previousActivity.getId()
                  + currentActivity.getId()) == null)
      {
         IActivity carnotPreviousActivity = processDefinition
               .findActivity(previousActivity.getId());

         IActivity carnotCurrentActivity = processDefinition.findActivity(currentActivity
               .getId());

         String transitionId = previousActivity.getId() + currentActivity.getId();

         if (log.isDebugEnabled())
         {
            log.debug("Creating transtition between " + carnotPreviousActivity.getId()
                  + " and " + carnotCurrentActivity.getId() + ".");
         }

         if (processDefinition.findTransition(transitionId) != null)
         {
            if (log.isDebugEnabled())
            {
               log.debug("There is already a transition with ID '"
                     + carnotPreviousActivity.getId() + carnotCurrentActivity.getId()
                     + "'.");
            }
         }
         else
         {
            ITransition transition = processDefinition.createTransition(transitionId,
                  transitionId, IncomeProcessDefinition._DEFAULT_DESCRIPTION,
                  carnotPreviousActivity, carnotCurrentActivity);

            for (Iterator activityIterator = currentActivity.getInConnections()
                  .iterator(); activityIterator.hasNext();)
            {
               IncomeConnection connection = (IncomeConnection) activityIterator.next();
               if (connection.getCondition() != null
                     && connection.getObjectstore().hasLeftConnectionWithActivity(
                           previousActivity.getId()))
               {
                  transition.setCondition(connection.getCondition());
               }
            }
         }
      }
   }

}
