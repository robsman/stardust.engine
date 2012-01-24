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

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
class Activity extends IdentifiableElement implements EventHandlerOwner
{
   private boolean allowsAbortByPerformer;
   private String applicationId;
   private String implementationType;
   private String joinType;
   private String splitType;
   private String performerID;
   private String loopCondition;
   private String loopType;
   private String subProcessID;
   private String subProcessMode;
   private Vector eventHandlers = new Vector();
   private Vector dataMappings = new Vector();
   private Model model;

   public Activity(String id, String name, String description, int elementOID, Model model)
   {
      super(id, name, description);
      this.model = model;
      model.register(this, elementOID);
   }

   public void setAllowsAbortByPerformer(boolean b)
   {
      allowsAbortByPerformer = b;
   }

   public void setApplicationId(String id)
   {
      applicationId = id;
   }

   public void setImplementationType(String value)
   {
      implementationType = value;
   }

   public void setJoinType(String value)
   {
      joinType = value;
   }

   public void setSplitType(String value)
   {
      splitType = value;
   }

   public void setPerformerID(String performerID)
   {
      this.performerID = performerID;
   }

   public void setLoopCondition(String condition)
   {
      this.loopCondition = condition;
   }

   public void setLoopType(String value)
   {
      this.loopType = value;
   }

   public void setSubProcessID(String processID)
   {
      this.subProcessID = processID;
   }

   public void setSubProcessMode(String processMode)
   {
      this.subProcessMode = processMode;
   }

   public EventHandler createEventHandler(String id, String conditionType, int elementOID)
   {
      EventHandler result = new EventHandler(id, conditionType, elementOID, model);
      eventHandlers.add(result);
      return result;
   }

   public DataMapping createDataMapping(String id, String dataID, String direction, String context, String applicationAccessPointId, String dataPath, String applicationPath, int oid)
   {
      DataMapping result = new DataMapping(id, dataID, direction, context, applicationAccessPointId, dataPath, applicationPath, oid, model);
      dataMappings.add(result);
      return result;
   }

   public boolean isAllowsAbortByPerformer()
   {
      return allowsAbortByPerformer;
   }

   public String getApplicationId()
   {
      return applicationId;
   }

   public String getImplementationType()
   {
      return implementationType;
   }

   public String getJoinType()
   {
      return joinType;
   }

   public String getLoopCondition()
   {
      return loopCondition;
   }

   public String getLoopType()
   {
      return loopType;
   }

   public String getPerformerID()
   {
      return performerID;
   }

   public String getSplitType()
   {
      return splitType;
   }

   public String getSubProcessID()
   {
      return subProcessID;
   }

   public String getSubProcessMode()
   {
      return subProcessMode;
   }

   public Iterator getAllEventHandlers()
   {
      return eventHandlers.iterator();
   }

   public Iterator getAllDataMappings()
   {
      return dataMappings.iterator();
   }
}

