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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailEventHandlerBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventConditionValidator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventHandlerBean extends IdentifiableElementBean implements IEventHandler
{
   private static final Logger trace = LogManager.getLogger(EventHandlerBean.class);
   
   public static final String BOUNDARY_EVENT_TYPE_KEY = "carnot:engine:event:boundaryEventType";
   public static final String BOUNDARY_EVENT_TYPE_INTERRUPTING_VALUE = "Interrupting";
   public static final String BOUNDARY_EVENT_TYPE_NON_INTERRUPTING_VALUE = "Non-interrupting";
   
   static final String AUTO_BIND_ATT = "Automatic Binding At Runtime";
   private boolean autoBind;

   static final String UNBIND_ON_MATCH_ATT = "Unbind On Match";
   private boolean unbindOnMatch;

   static final String LOG_HANDLER_ATT = "Create Log Entry";
   private boolean logHandler;

   static final String CONSUME_ON_MATCH_ATT = "Consume On Match";
   private boolean consumeOnMatch;

   private Link eventActions = new Link(this, "Event Actions");
   private Link bindActions = new Link(this, "Bind Actions");
   private Link unbindActions = new Link(this, "Unbind Actions");

   private Link persistentAccessPoints = new Link(this, "Access Points");
   private transient AccessPointJanitor accessPoints;

   private IEventConditionType conditionType = null;

   public EventHandlerBean()
   {
   }

   public EventHandlerBean(String id, String name, String description)
   {
      super(id, name);
      setDescription(description);
   }

   public void setConditionType(IEventConditionType type)
   {
      this.conditionType = type;
   }

   public PluggableType getType()
   {
      return conditionType;
   }

   public Iterator getAllEventActions()
   {
      return eventActions.iterator();
   }

   public Iterator getAllBindActions()
   {
      return bindActions.iterator();
   }

   public Iterator getAllUnbindActions()
   {
      return unbindActions.iterator();
   }

   public IEventAction createEventAction(String id, String name, IEventActionType type,
         int elementOID)
   {
      markModified();
      IEventAction result = new EventActionBean(id, name);
      addToEventActions(result);
      result.register(elementOID);
      result.setActionType(type);
      return result;
   }

   public IBindAction createBindAction(String id, String name, IEventActionType type,
         int elementOID)
   {
      markModified();
      IBindAction result = new BindActionBean(id, name);
      addToBindActions(result);
      result.register(elementOID);
      result.setActionType(type);
      return result;
   }

   public IUnbindAction createUnbindAction(String id, String name, IEventActionType type,
         int elementOID)
   {
      markModified();
      IUnbindAction result = new UnbindActionBean(id, name);
      addToUnbindActions(result);
      result.register(elementOID);
      result.setActionType(type);
      return result;
   }

   public boolean isAutoBind()
   {
      return autoBind;
   }

   public boolean isUnbindOnMatch()
   {
      return unbindOnMatch;
   }

   public void setAutoBind(boolean autoBind)
   {
      this.autoBind = autoBind;
   }

   public void setUnbindOnMatch(boolean disableOnFire)
   {
      this.unbindOnMatch = disableOnFire;
   }

   public boolean isLogHandler()
   {
      return logHandler;
   }

   public void setLogHandler(boolean logHandler)
   {
      this.logHandler = logHandler;
   }

   public boolean isConsumeOnMatch()
   {
      return consumeOnMatch;
   }

   public void setConsumeOnMatch(boolean swallow)
   {
      markModified();
      this.consumeOnMatch = swallow;
   }

   public boolean hasBindActions()
   {
      return bindActions.size() > 0;
   }

   public boolean hasUnbindActions()
   {
      return unbindActions.size() > 0;
   }

   public void removeFromEventActions(IEventAction action)
   {
      eventActions.remove(action);
   }

   public void removeFromBindActions(IBindAction action)
   {
      bindActions.remove(action);
   }

   public void removeFromUnbindActions(IUnbindAction action)
   {
      unbindActions.remove(action);
   }

   public void addToEventActions(IEventAction action)
   {
      eventActions.add(action);
   }

   public void addToBindActions(IBindAction action)
   {
      bindActions.add(action);
   }

   public void addToUnbindActions(IUnbindAction action)
   {
      unbindActions.add(action);
   }

   public void checkConsistency(List inconsistencies)
   {
      super.checkConsistency(inconsistencies);
      checkId(inconsistencies);
      
      if (getId() != null)
      {
         IEventHandler eh = ((EventHandlerOwner) getParent()).findHandlerById(getId());
         if (eh != null && eh != this)
         {
            inconsistencies.add(new Inconsistency("Duplicate ID for event handler '" +
                  getName() + "'.", this, Inconsistency.ERROR));
         }
         
         // check id to fit in maximum length
         if (getId().length() > AuditTrailEventHandlerBean.getMaxIdLength())
         {
            inconsistencies.add(new Inconsistency("ID for event handler '" + getName()
                  + "' exceeds maximum length of "
                  + AuditTrailEventHandlerBean.getMaxIdLength() + " characters.",
                  this, Inconsistency.ERROR));
         }
      }

      IEventConditionType type = (IEventConditionType) getType();
      if (type == null)
      {
         inconsistencies.add(new Inconsistency("EventHandler does not have a condition type",
               this, Inconsistency.ERROR));
      }
      else
      {
         EventConditionValidator validator = (EventConditionValidator) ValidatorUtils.getValidator(type, this, inconsistencies);
         if (null != validator)
         {
            Collection coll = validator.validate((EventHandlerOwner) getParent(), getAllAttributes());
            for (Iterator i = coll.iterator(); i.hasNext();)
            {
               Inconsistency x = (Inconsistency) i.next();
               inconsistencies.add(new Inconsistency(x.getMessage(), this, x.getSeverity()));
            }
         }
      }

      checkActionsConsistency(inconsistencies, eventActions);
      checkActionsConsistency(inconsistencies, bindActions);
      checkActionsConsistency(inconsistencies, unbindActions);
   }

   private void checkActionsConsistency(List inconsistencies, Link actions)
   {
      for (Iterator i = actions.iterator(); i.hasNext();)
      {
         IAction eventAction = (IAction) i.next();
         eventAction.checkConsistency(inconsistencies);

         // check id to be unique
         if (eventAction.getId() != null)
         {
            IAction a = (IAction) actions.findById(eventAction.getId());
            if (a != null && a != eventAction)
            {
               inconsistencies.add(new Inconsistency("Duplicate ID for event action '" +
                     eventAction.getName() + "'.", eventAction,
                     Inconsistency.ERROR));
            }
         }
      }
   }

   private AccessPointJanitor getAccessPointLink()
   {
      if (accessPoints == null)
      {
         accessPoints = new AccessPointJanitor(persistentAccessPoints);
      }
      return accessPoints;
   }

   public AccessPoint findAccessPoint(String id)
   {
      return findAccessPoint(id, null);
   }

   public AccessPoint findAccessPoint(String id, Direction direction)
   {
      return getAccessPointLink().findAccessPoint(id, direction);
   }

   public AccessPoint createAccessPoint(String id, String name, Direction direction,
         IDataType type, int elementOID)
   {
      IAccessPoint result = new AccessPointBean(id, name, direction);
      addToPersistentAccessPoints(result);
      result.register(elementOID);
      result.setDataType(type);
      return result;
   }

   public Iterator getAllAccessPoints()
   {
      Iterator accessPoints;
      
      IEventConditionType handlerType = (IEventConditionType) getType();
      Object apProvider = handlerType
            .getAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT);
      if (null != apProvider)
      {
         if (apProvider instanceof String)
         {
            try
            {
               AccessPointProvider provider = (AccessPointProvider) Reflect
                     .createInstance((String) apProvider);
               accessPoints = provider.createIntrinsicAccessPoints(getAllAttributes(),
                     handlerType.getAllAttributes());
            }
            catch (Exception e)
            {
               trace.warn("Invalid intrinsic access point provider configuration '"
                     + apProvider + "'", e);
               accessPoints = Collections.EMPTY_LIST.iterator();
            }
         }
         else
         {
            trace.warn("Invalid intrinsic access point provider configuration '"
                  + apProvider + "'");
            accessPoints = Collections.EMPTY_LIST.iterator();
         }
      }
      else
      {
         accessPoints = getAccessPointLink().iterator();
      }

      return accessPoints;
   }

   public void addToPersistentAccessPoints(IAccessPoint accessPoint)
   {
      persistentAccessPoints.add(accessPoint);
   }

   public void removeFromPersistentAccessPoints(IAccessPoint accessPoint)
   {
      persistentAccessPoints.remove(accessPoint);
   }

   public void removeFromAccessPoints(AccessPoint accessPoint)
   {
      getAccessPointLink().remove(accessPoint);
   }

   public Iterator getAllInAccessPoints()
   {
      return getAccessPointLink().getAllInAccessPoints();
   }

   public Iterator getAllOutAccessPoints()
   {
      return getAccessPointLink().getAllOutAccessPoints();
   }

   public String getProviderClass()
   {
      return getType().getStringAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT);
   }

   public void addIntrinsicAccessPoint(AccessPoint ap)
   {
      getAccessPointLink().addIntrinsicAccessPoint(ap);
   }

   public Iterator getAllPersistentAccessPoints()
   {
      return persistentAccessPoints.iterator();
   }

   public void markModified()
   {
      super.markModified();
      getAccessPointLink().setDirty();
   }

   public String toString()
   {
      return "Event Handler: " + getName();
   }
}
