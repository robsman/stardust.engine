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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
class ProcessDefinition extends IdentifiableElement implements EventHandlerOwner
{
   private static final Logger trace = LogManager.getLogger(ProcessDefinition.class);
   
   private Model model;
   private Vector activities = new Vector();
   private Vector diagrams = new Vector();
   private Vector triggers = new Vector();
   private Vector eventHandlers = new Vector();
   private Vector transitions = new Vector();
   private Vector dataPaths = new Vector();

   public ProcessDefinition(String id, String name, String description, int elementOID, Model model)
   {
      super(id, name, description);

      this.model = model;
      model.register(this, elementOID);
   }

   public Activity createActivity(String id, String name, String description, int elementOID)
   {
      Activity result = new Activity(id, name, description, elementOID, model);
      activities.add(result);
      return result;
   }

   public Diagram createDiagram(String name, int elementOID)
   {
      Diagram result = new Diagram(name, elementOID, model);
      diagrams.add(result);
      return result;
   }

   public Trigger createTrigger(String type, String id, String name, int oid)
   {
      Trigger result = new Trigger(type, id, name , oid, model);
      triggers.add(result);
      return result;
   }

   public EventHandler createEventHandler(String id, String type, int elementOID)
   {
      EventHandler result = new EventHandler(id, type, elementOID, model);
      eventHandlers.add(result);
      return result;
   }

   public Transition createTransition(String id, String name, String description,
         String sourceID, String targetID, String condition, boolean forkOnTraversal, int elementOID)
   {
      Transition result = new Transition(id, name, description, sourceID, targetID,
            condition, forkOnTraversal, elementOID, model);
      transitions.add(result);
      return result;
   }

   public DataPath createDataPath(String id, String name, String pathExpr,
         String direction, boolean descriptor, int oid)
   {
      DataPath newPath = new DataPath(id, name, pathExpr, direction, descriptor, oid, model);
      
      DataPath path = findDataPath(id, direction);
      if (null != path)
      {
         if (CompareHelper.areEqual(path.getDataID(), newPath.getDataID())
               && CompareHelper.areEqual(path.getDataPath(), newPath.getDataPath()))
         {
            if (newPath.isDescriptor())
            {
               path.setDescriptor(true);
            }
            newPath = path;
         }
         else
         {
            trace.warn("Unable to merge conflicting data path and descriptor with ID '"
                  + id + "' for process definition '" + getId() + "'");
         }
      }

      if (newPath != path)
      {
         dataPaths.add(newPath);
      }
      
      return newPath;
   }

   public Activity findActivity(String activityID)
   {
      for (Iterator i = activities.iterator(); i.hasNext();)
      {
         Activity activity = (Activity) i.next();
         if (activity.getId().equals(activityID))
         {
            return activity;
         }
      }
      return null;
   }

   public Iterator getAllActivities()
   {
      return activities.iterator();
   }

   public Iterator getAllTransitions()
   {
      return transitions.iterator();
   }

   public Iterator getAllTriggers()
   {
      return triggers.iterator();
   }
   
   public DataPath findDataPath(String id, String direction)
   {
      DataPath result = null;
      for (Iterator i = dataPaths.iterator(); i.hasNext();)
      {
         DataPath path = (DataPath) i.next();
         if (path.getId().equals(id) && path.getDirection().equals(direction))
         {
            result = path;
            break;
         }
      }
      
      return result;
   }

   public Iterator getAllDataPaths()
   {
      return dataPaths.iterator();
   }

   public Iterator getAllDiagrams()
   {
      return diagrams.iterator();
   }

   public Iterator getAllEventHandlers()
   {
      return eventHandlers.iterator();
   }
}
