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

import org.eclipse.stardust.engine.api.model.EventType;
import org.eclipse.stardust.engine.api.model.IEventConditionType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventConditionTypeBean extends IdentifiableElementBean
      implements IEventConditionType
{
   private static final String PROCESS_INSTANCE_SCOPE_ATT = "Process instance scope";
   private boolean processInstanceScope;

   private static final String ACTIVITY_INSTANCE_SCOPE_ATT = "Activity instance scope";
   private boolean activityInstanceScope;

   private static final String AUTO_BINDING_ATT = "Auto binding";
   private boolean autoBinding;

   private static final String DISABLE_ON_MATCH_ATT = "Disable on match";
   private boolean disableOnMatch;

   private static final String IMPLEMENTATION_ATT = "Implementation";
   private EventType implementation;

   public EventConditionTypeBean()
   {
   }

   public EventConditionTypeBean(String id, String name, boolean predefined,
         EventType implementation,
         boolean processCondition, boolean activityCondition)
   {
      super(id, name);
      setPredefined(predefined);
      this.processInstanceScope = processCondition;
      this.activityInstanceScope = activityCondition;
      this.implementation = implementation;
   }

   public boolean hasProcessInstanceScope()
   {
      return processInstanceScope;
   }

   public boolean hasActivityInstanceScope()
   {
      return activityInstanceScope;
   }

   public boolean isAutoBinding()
   {
      return autoBinding;
   }

   public boolean isDisableOnMatch()
   {
      return disableOnMatch;
   }

   public void setActivityInstanceScope(boolean activityInstanceScope)
   {
      this.activityInstanceScope = activityInstanceScope;
   }

   public void setProcessInstanceScope(boolean processInstanceScope)
   {
      this.processInstanceScope = processInstanceScope;
   }

   public void setDisableOnMatch(boolean disableOnMatch)
   {
      this.disableOnMatch = disableOnMatch;
   }

   public void setAutoBinding(boolean autoBinding)
   {
      this.autoBinding = autoBinding;
   }

   public EventType getImplementation()
   {
      return implementation;
   }

   public String toString()
   {
      return "Event Condition Type: " + getName();
   }
}
