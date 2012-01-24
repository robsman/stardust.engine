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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.eclipse.stardust.engine.api.model.EventActionContext;
import org.eclipse.stardust.engine.api.model.IEventActionType;
import org.eclipse.stardust.engine.api.model.IEventConditionType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.MultiRef;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventActionTypeBean extends IdentifiableElementBean implements IEventActionType
{
   static final String PROCESS_ACTION_ATT="Process Action";
   private boolean processAction;

   static final String ACTIVITY_ACTION_ATT="Activity Action";
   private boolean activityAction;

   private MultiRef supportedConditionTypes = new MultiRef(this, "supportedConditionTypes");

   private Set unsupportedContexts;

   public EventActionTypeBean()
   {
   }

   public EventActionTypeBean(String id, String name, boolean predefined, boolean processAction, boolean activityAction)
   {
      super(id, name);
      setPredefined(predefined);
      this.processAction = processAction;
      this.activityAction = activityAction;
   }

   public boolean isActivityAction()
   {
      return activityAction;
   }

   public boolean isProcessAction()
   {
      return processAction;
   }

   public void setProcessAction(boolean b)
   {
      processAction = b;
   }

   public void setActivityAction(boolean b)
   {
      activityAction = b;
   }

   public Iterator getSupportedConditionTypes()
   {
      return supportedConditionTypes.iterator();
   }

   public void addSupportedConditionType(IEventConditionType type)
   {
      supportedConditionTypes.add(type);
   }

   public boolean supports(IEventConditionType type)
   {
      return supportedConditionTypes.contains(type);
   }

   public void removeAllSupportedConditionTypes()
   {
      supportedConditionTypes.clear();
   }

   public Iterator getUnsupportedContexts()
   {
      return unsupportedContexts == null ?
            Collections.EMPTY_SET.iterator() : unsupportedContexts.iterator();
   }

   public void addUnsupportedContext(EventActionContext type)
   {
      if (unsupportedContexts == null)
      {
         unsupportedContexts = new HashSet();
      }
      unsupportedContexts.add(type);
   }

   public boolean supports(EventActionContext type)
   {
      return unsupportedContexts == null || !unsupportedContexts.contains(type);
   }

   public void removeAllUnsupportedContexts()
   {
      if (unsupportedContexts != null)
      {
         unsupportedContexts.clear();
         unsupportedContexts = null;
      }
   }

   public String toString()
   {
      return "Event Action Type: " + getName();
   }
}
