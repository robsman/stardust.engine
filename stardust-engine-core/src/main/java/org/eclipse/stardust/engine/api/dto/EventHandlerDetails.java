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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventHandlerDetails extends AuditTrailModelElementDetails
      implements EventHandler
{
   private static final long serialVersionUID = 1793784254864830555L;
   private Map typeAttributes = new HashMap();
   private List actions;
   private List bindActions;
   private List unbindActions;

   private final String activityId;
   private final String processDefinitionId;
   
   public EventHandlerDetails(IEventHandler handler)
   {
      super(handler);

      typeAttributes.putAll(handler.getType().getAllAttributes());

      initUnspecialized(handler);
      
      ModelElement parent = handler.getParent();
      if (parent instanceof IProcessDefinition)
      {
         this.processDefinitionId = ((IProcessDefinition) parent).getId();
         this.activityId = null;
      }
      else if (parent instanceof IActivity)
      {
         this.processDefinitionId = ((IActivity) parent).getProcessDefinition().getId();
         this.activityId = ((IActivity) parent).getId();
      }
      else
      {
         this.processDefinitionId = null;
         this.activityId = null;
      }
   }

   public Map getAllTypeAttributes()
   {
      return Collections.unmodifiableMap(typeAttributes);
   }

   public Object getTypeAttribute(String name)
   {
      return typeAttributes.get(name);
   }

   public List getAllEventActions()
   {
      return Collections.unmodifiableList(actions);
   }

   public EventAction getEventAction(String id)
   {
      return (EventAction) ModelApiUtils.firstWithId(actions.iterator(), id);
   }

   public List getAllBindActions()
   {
      return Collections.unmodifiableList(bindActions);
   }

   public EventAction getBindAction(String id)
   {
      return (EventAction) ModelApiUtils.firstWithId(bindActions.iterator(), id);
   }

   public List getAllUnbindActions()
   {
      return Collections.unmodifiableList(unbindActions);
   }

   public EventAction getUnbindAction(String id)
   {
      return (EventAction) ModelApiUtils.firstWithId(unbindActions.iterator(), id);
   }

   public String getActivityId()
   {
      return activityId;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   private void initUnspecialized(IEventHandler handler)
   {
      actions = DetailsFactory.createCollection(handler.getAllEventActions(),
            IAction.class, EventActionDetails.class);
      bindActions = DetailsFactory.createCollection(handler.getAllBindActions(),
            IAction.class, EventActionDetails.class);
      unbindActions = DetailsFactory.createCollection(handler.getAllUnbindActions(),
            IAction.class, EventActionDetails.class);
   }
}
