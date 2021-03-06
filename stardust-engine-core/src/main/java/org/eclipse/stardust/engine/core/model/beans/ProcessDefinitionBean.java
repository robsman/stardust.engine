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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.collections.IteratorUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.api.model.IEventConditionType;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IFormalParameter;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.ITriggerType;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.JoinSplitType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.compatibility.diagram.DefaultDiagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.model.utils.Connections;
import org.eclipse.stardust.engine.core.model.utils.ExclusionComputer;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailProcessDefinitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.DeploymentUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;

/**
 * @author pielmann
 */
public class ProcessDefinitionBean extends IdentifiableElementBean
      implements IProcessDefinition
{

   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ProcessDefinitionBean.class);

   private static final String ACTIVITY_STRING = "Activity";

   private static final String TRANSITION_STRING = "Transition";

   private List<IFormalParameter> formalParameters = CollectionUtils.newList();

   private Link activities = new Link(this, "Activities");

   private Link triggers = new Link(this, "Triggers");

   private Link diagrams = new Link(this, "Diagrams");

   private Link dataPaths = new Link(this, "Data Paths");

   private Link eventHandlers = new Link(this, "Event Handlers");

   private Connections transitions = new Connections(this, "Transitions",
         "outTransitions", "inTransitions");

   private Connections exceptionTransitions = new Connections(this,
         "Exception Transitions", "exceptionTransitions", "inTransitions");

   private int defaultActivityId = -1;

   private int defaultTransitionId = -1;

   static final String DEFAULT_PRIORITY_ATT = "defaultPriority";

   private int defaultPriority;

   private IReference externalReference;

   private boolean declaresInterface;

   private Map<String, String> formalParameterMappings;

   private transient IApplicationContext engineContext = new MyEngineContext();

   private transient IApplicationContext defaultContext = new MyDefaultContext();

   ProcessDefinitionBean()
   {
   }

   public ProcessDefinitionBean(String id, String name, String description)
   {
      super(id, name);
      setDescription(description);
   }

   public void addToActivities(IActivity activity)
   {
      markModified();
      activities.add(activity);
      if (0 <= defaultActivityId)
      {
         // avoid parsing the activity IDs at runtime as this is pretty CPU intensive
         defaultActivityId = nextID(ACTIVITY_STRING, defaultActivityId, activity.getId());
      }
   }

   public void addToTransitions(ITransition transition, String condition)
   {
      markModified();

      final Iterator iter1 = transitions.iterator();
      checkForDuplicateTransition(iter1, transition);

      final Iterator iter2 = exceptionTransitions.iterator();
      checkForDuplicateTransition(iter2, transition);

      if (condition != null
            && TransitionBean.ON_BOUNDARY_EVENT_CONDITION.matcher(condition).matches())
      {
         exceptionTransitions.add(transition);
      }
      else
      {
         transitions.add(transition);
      }

      if (0 <= defaultTransitionId)
      {
         // avoid parsing the transition IDs at runtime as this is pretty CPU intensive
         defaultTransitionId = nextID(TRANSITION_STRING, defaultTransitionId,
               transition.getId());
      }
   }

   private void checkForDuplicateTransition(final Iterator<ITransition> iter,
         final ITransition transition)
   {
      while (iter.hasNext())
      {
         ITransition t = iter.next();
         if (CompareHelper.areEqual(t.getFromActivity(), transition.getFromActivity())
               && CompareHelper.areEqual(t.getToActivity(), transition.getToActivity()))
         {
            trace.warn("Duplicate transition: " + this + " - " + transition
                  + " has the same source/target with " + t);
         }
      }
   }

   public void addToTriggers(ITrigger trigger)
   {
      markModified();
      triggers.add(trigger);
   }

   public Iterator getAllEventHandlers(final String type)
   {
      return new FilteringIterator(getAllEventHandlers(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return type.equals(((IEventHandler) o).getType().getId());
         }
      });
   }

   public boolean hasEventHandlers(String type)
   {
      for (int i = 0; i < getEventHandlers().size(); ++i)
      {
         IEventHandler handler = (IEventHandler) getEventHandlers().get(i);

         if (type.equals(handler.getType().getId()))
         {
            return true;
         }
      }

      return false;
   }

   public List checkConsistency()
   {
      List inconsistencies = CollectionUtils.newList();

      checkConsistency(inconsistencies);

      return inconsistencies;
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the
    * process definition.
    */
   public void checkConsistency(List<Inconsistency> inconsistencies)
   {
      try
      {
         super.checkConsistency(inconsistencies);
         checkId(inconsistencies);

         if (getId() != null)
         {
            // check for unique Id
            IProcessDefinition pd = ((IModel) getModel()).findProcessDefinition(getId());
            if (pd != null && pd != this)
            {
               BpmValidationError error = BpmValidationError.PD_DUPLICATE_ID.raise(getName());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }

            // check id to fit in maximum length
            if (getId().length() > AuditTrailProcessDefinitionBean.getMaxIdLength())
            {
               BpmValidationError error = BpmValidationError.PD_ID_EXCEEDS_LENGTH.raise(
                     getName(), AuditTrailProcessDefinitionBean.getMaxIdLength());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
         }

         boolean isRevalidation = Parameters.instance().getBoolean(
               ModelElementBean.PRP_REVALIDATE_ELEMENTS, false);
         // Check Implementation of Interface only if it is not a revalidation
         if ( !isRevalidation)
         {
            checkImplementation(inconsistencies);
            if (declaresInterface)
            {
               boolean externalProcessInvocation = isExternalProcessInvocation();
               for (IFormalParameter formalParameter : formalParameters)
               {
                  IData data = formalParameter.getData();
                  if (data == null)
                  {
                     BpmValidationError error = BpmValidationError.PD_FORMAL_PARAMETER_NO_DATA_SET.raise(formalParameter.getId());
                     inconsistencies.add(new Inconsistency(error, this,
                           Inconsistency.ERROR));
                  }
                  // For external process invocations check restricted types, only
                  // primitive and structured data are supported.
                  else if (externalProcessInvocation
                        && !StructuredTypeRtUtils.isStructuredType(data)
                        && !PredefinedConstants.PRIMITIVE_DATA.equals(data.getType()
                              .getId()))
                  {
                     BpmValidationError error = BpmValidationError.PD_FORMAL_PARAMETER_INCOMPATIBLE_DATA_FOR_EXTERNAL_INVOCATION.raise(formalParameter.getId());
                     inconsistencies.add(new Inconsistency(error, this,
                           Inconsistency.WARNING));
                  }
               }
            }
         }

         // check Rules for Activities
         for (IActivity activity : getActivities())
         {
            activity.checkConsistency(inconsistencies);
         }

         // check Rules for Triggers
         for (ITrigger trigger : getTriggers())
         {
            trigger.checkConsistency(inconsistencies);
         }

         // check consistencies for EventHandlers
         for (Iterator iterator = getAllEventHandlers(); iterator.hasNext();)
         {
            IEventHandler eventHandler = (IEventHandler) iterator.next();
            eventHandler.checkConsistency(inconsistencies);
         }

         // check consistencies for DataPaths
         for (IDataPath dataPath : getDataPaths())
         {
            dataPath.checkConsistency(inconsistencies);
         }

         // check Rules for Transitions and verify duplicates
         List v = CollectionUtils.newList();
         for (ITransition transition : getTransitions())
         {
            transition.checkConsistency(inconsistencies);
            for (int i = 0; i < v.size(); i++ )
            {
               ITransition t = (ITransition) v.get(i);
               if (CompareHelper.areEqual(t.getFromActivity(),
                     transition.getFromActivity())
                     && CompareHelper.areEqual(t.getToActivity(),
                           transition.getToActivity()))
               {
                  BpmValidationError error = BpmValidationError.PD_DUPLICATE_TRANSITION_SAME_SOURCE_OR_TARGET.raise(
                        transition, t);
                  inconsistencies.add(new Inconsistency(error, transition,
                        Inconsistency.ERROR));
               }
            }
            v.add(transition);
         }

         // Rule 1: Process definitions should have precisely one root activity

         IActivity startActivity = null;
         String otherStartActivities = null;

         for (IActivity activity : getActivities())
         {
            if (activity.getInTransitions().isEmpty())
            {
               if (startActivity == null)
               {
                  startActivity = activity;
               }
               else
               {
                  if (otherStartActivities == null)
                  {
                     otherStartActivities = "'" + startActivity.getId() + "', '"
                           + activity.getId() + "'";
                  }
                  else
                  {
                     otherStartActivities += ", '" + activity.getId() + "'";
                  }
               }
            }
         }

         if (startActivity == null)
         {
            BpmValidationError error = BpmValidationError.PD_NO_START_ACTIVITY.raise();
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         if (otherStartActivities != null)
         {
            BpmValidationError error = BpmValidationError.PD_MULTIPLE_START_ACTIVYTIES.raise(
                  otherStartActivities, getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         // Rule 2: Process definitions need to have either a trigger or should be used as
         // a subprocess activity
         // or is started by a notification (associated to an activity or a process)

         // This was removed for 3.0.0 because with the new event handlers this rule is
         // not "computable" anymore. Functionality was questionable anyway.

         // Rule 3: Process definitions must have at least one activities

         if (getActivities().isEmpty())
         {
            BpmValidationError error = BpmValidationError.PD_NO_ACTIVITIES_DEFINED.raise(getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         checkForDeadlocks(inconsistencies);
      }
      catch (Exception e)
      {
         throw new InternalException("Process definition '" + getId()
               + "' cannot be checked.", e);
      }
   }

   private boolean isExternalProcessInvocation()
   {
      String externalInvocationAttribute = (String) getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE);
      return externalInvocationAttribute == null
            ? false
            : PredefinedConstants.PROCESSINTERFACE_INVOCATION_SOAP.equals(externalInvocationAttribute)
                  || PredefinedConstants.PROCESSINTERFACE_INVOCATION_REST.equals(externalInvocationAttribute)
                  || PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH.equals(externalInvocationAttribute);
   }

   private void checkForDeadlocks(List<Inconsistency> inconsistencies)
   {
      ExclusionComputer<IActivity, ITransition> computer = new ExclusionComputer<IActivity, ITransition>()
      {
         protected IActivity getFrom(ITransition transition)
         {
            return transition.getFromActivity();
         }

         protected IActivity getTo(ITransition transition)
         {
            return transition.getToActivity();
         }

         protected Iterable<ITransition> getIn(IActivity activity)
         {
            return activity.getInTransitions();
         }

         protected boolean isInclusiveJoin(IActivity activity)
         {
            return activity.getJoinType() == JoinSplitType.And
                  || activity.getJoinType() == JoinSplitType.Or;
         }
      };
      for (IActivity activity : getActivities())
      {
         IActivity blockingActivity = computer.getBlockingActivity(activity);
         // we want to show the deadlock only once.
         if (blockingActivity != null
               && activity.getId().compareTo(blockingActivity.getId()) < 0)
         {
            BpmValidationError error = BpmValidationError.PD_POTENTIAL_DEADLOCKS.raise(
                  activity.getName(), blockingActivity.getName(), getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }
   }

   private void checkImplementation(List<Inconsistency> inconsistencies)
   {
      if (externalReference != null)
      {
         QName qname = new QName(externalReference.getExternalPackage().getHref(),
               externalReference.getId());
         IModel refModel = externalReference.getExternalPackage().getReferencedModel();
         if (refModel != null)
         {
            String uuid = getStringAttribute("carnot:connection:uuid");
            IProcessDefinition refProcess = refModel.findProcessDefinition(StringUtils.isEmpty(uuid)
                  ? externalReference.getId()
                  : externalReference.getId() + "?uuid=" + uuid);
            if (refProcess == null)
            {
               BpmValidationError error = BpmValidationError.PD_PROCESS_INTERFACE_NOT_RESOLVED.raise(qname);
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            else
            {
               DeploymentUtils.checkCompatibleInterface(inconsistencies, refProcess,
                     this, true);
            }
         }
      }
   }

   public IActivity createActivity(String id, String name, String description,
         int elementOID)
   {
      if (findActivity(id) != null)
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ACTIVITY_WITH_ID_ALREADY_EXISTS.raise(id));
      }

      markModified();

      ActivityBean activity = new ActivityBean(id, name, description);

      addToActivities(activity);
      activity.register(elementOID);
      activity.setImplementationType(ImplementationType.Route);

      return activity;
   }

   /**
    *
    */
   public Diagram createDiagram(String name)
   {
      return createDiagram(name, 0);
   }

   public Diagram createDiagram(String name, int elementOID)
   {
      markModified();

      DefaultDiagram diagram = new DefaultDiagram(name);
      addToDiagrams(diagram);
      diagram.register(elementOID);

      return diagram;
   }

   public ITrigger createTrigger(String id, String name, ITriggerType type, int elementOID)
   {
      TriggerBean trigger = new TriggerBean(id, name);
      trigger.setType(type);

      addToTriggers(trigger);
      trigger.register(elementOID);

      return trigger;
   }

   public ITransition createTransition(String id, String name, String description,
         IActivity fromActivity, IActivity toActivity)
   {
      return createTransition(id, name, description, fromActivity, toActivity, 0, null);
   }

   public ITransition createTransition(String id, String name, String description,
         IActivity fromActivity, IActivity toActivity, int elementOID, String condition)
   {
      if (findTransition(id) != null)
      {
         throw new PublicException(
               BpmRuntimeError.MDL_TRANSITION_WITH_ID_ALREADY_EXISTS.raise(id));
      }

      if (PredefinedConstants.RELOCATION_TRANSITION_ID.equals(id))
      {
         if (fromActivity != null || toActivity != null)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_RELOCATION_TRANSITION_MUST_NOT_HAVE_ANY_SOURCE_OR_TARGET_ACTIVITY_ATTACHED.raise(getId()));
         }
      }
      else
      {
         if (fromActivity.getProcessDefinition() != this)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_FROM_ACTIVITY_DOES_NOT_BELONG_TO.raise(this,
                        fromActivity.getId(), fromActivity));
         }

         if (toActivity.getProcessDefinition() != this)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_TO_ACTIVITY_DOES_NOT_BELONG_TO.raise(this, fromActivity.getId(),
                        fromActivity));
         }

         if (toActivity.getJoinType() == JoinSplitType.None
               && !toActivity.getInTransitions().isEmpty())
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_MULTIPLE_INCOMING_TRANSITIONS_ARE_ONLY_ALLOWED_FOR_AND_OR_XOR_ACTIVITY_JOINS.raise(
                        elementOID, toActivity.getId(), toActivity.getElementOID()));
         }

         if (fromActivity.getSplitType() == JoinSplitType.None
               && !fromActivity.getOutTransitions().isEmpty()
               && !TransitionBean.ON_BOUNDARY_EVENT_CONDITION.matcher(condition)
                     .matches())
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_MULTIPLE_OUTGOING_TRANSITIONS_ARE_ONLY_ALLOWED_FOR_AND_OR_XOR_ACTIVITY_SPLITS.raise(
                        elementOID, toActivity.getId(), toActivity.getElementOID()));
         }
      }

      markModified();

      TransitionBean transition = new TransitionBean(id, name, description, fromActivity,
            toActivity);

      addToTransitions(transition, condition);
      transition.register(elementOID);

      return transition;
   }

   public IActivity findActivity(String id)
   {
      return (IActivity) activities.findById(id);
   }

   public IDataPath findDescriptor(String id)
   {
      for (Iterator i = dataPaths.iterator(); i.hasNext();)
      {
         IDataPath d = (IDataPath) i.next();
         if (d.isDescriptor() && CompareHelper.areEqual(d.getId(), id))
         {
            return d;
         }
      }
      return null;
   }

   public ITransition findTransition(String id)
   {
      ITransition result = (ITransition) transitions.findById(id);
      if (result == null)
      {
         result = (ITransition) exceptionTransitions.findById(id);
      }
      return result;
   }

   public ITrigger findTrigger(String id)
   {
      return (ITrigger) triggers.findById(id);
   }

   /**
    * @deprecated Use of {@link #getActivities()} allows for more efficient iteration.
    */
   public Iterator getAllActivities()
   {
      return activities.iterator();
   }

   public ModelElementList<IActivity> getActivities()
   {
      return activities;
   }

   public Iterator getAllDiagrams()
   {
      return diagrams.iterator();
   }

   /**
    * Retrieves all instances of this process.
    */
   public Iterator getAllInstances()
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getVector(ProcessInstanceBean.class)
            .iterator();
   }

   /**
    * @deprecated Use of {@link #getTransitions()} allows for more efficient iteration.
    */
   public Iterator getAllTransitions()
   {
      return IteratorUtils.chainedIterator(transitions.iterator(),
            exceptionTransitions.iterator());
   }

   public ModelElementList<ITransition> getTransitions()
   {
      return new ModelElementList<ITransition>()
      {
         @Override
         public ITransition get(final int index)
         {
            if (index < transitions.size())
            {
               return (ITransition) transitions.get(index);
            }

            return (ITransition) exceptionTransitions.get(index - transitions.size());
         }

         @Override
         public boolean isEmpty()
         {
            return transitions.isEmpty() && exceptionTransitions.isEmpty();
         }

         @Override
         public Iterator<ITransition> iterator()
         {
            return IteratorUtils.chainedIterator(transitions.iterator(),
                  exceptionTransitions.iterator());
         }

         @Override
         public int size()
         {
            return transitions.size() + exceptionTransitions.size();
         }
      };
   }

   public Iterator getAllTriggers()
   {
      return triggers.iterator();
   }

   public ModelElementList<ITrigger> getTriggers()
   {
      return triggers;
   }

   public Iterator getAllDescriptors()
   {
      return new FilteringIterator(dataPaths.iterator(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((IDataPath) o).isDescriptor();
         }

      });
   }

   public void removeFromDataPaths(IDataPath dataPath)
   {
      markModified();

      dataPaths.remove(dataPath);
   }

   public IDataPath createDataPath(String id, String name, IData data, String path,
         Direction direction, int elementOID)
   {
      markModified();

      DataPathBean dataPath = new DataPathBean(id, name, data, path, direction);

      addToDataPaths(dataPath);
      dataPath.register(elementOID);

      return dataPath;
   }

   public Iterator getAllDataPaths()
   {
      return dataPaths.iterator();
   }

   public ModelElementList<IDataPath> getDataPaths()
   {
      return dataPaths;
   }

   public IDataPath findDataPath(String id, Direction direction)
   {
      for (Iterator i = dataPaths.iterator(); i.hasNext();)
      {
         IDataPath d = (IDataPath) i.next();
         if (direction == d.getDirection() && CompareHelper.areEqual(d.getId(), id))
         {
            return d;
         }
      }
      return null;
   }

   public void addToDataPaths(IDataPath path)
   {
      markModified();
      dataPaths.add(path);
   }

   public String getDefaultActivityId()
   {
      if ( -1 == defaultActivityId)
      {
         this.defaultActivityId = 1;
         for (int i = 0; i < getActivities().size(); i++ )
         {
            IActivity activity = (IActivity) getActivities().get(i);
            this.defaultActivityId = nextID(ACTIVITY_STRING, defaultActivityId,
                  activity.getId());
         }
      }

      return ACTIVITY_STRING + defaultActivityId;
   }

   public String getDefaultTransitionId()
   {
      if ( -1 == defaultTransitionId)
      {
         this.defaultTransitionId = 1;
         for (int i = 0; i < getTransitions().size(); i++ )
         {
            ITransition transition = (ITransition) getTransitions().get(i);
            this.defaultTransitionId = nextID(TRANSITION_STRING, defaultTransitionId,
                  transition.getId());
         }
      }

      return TRANSITION_STRING + defaultTransitionId;
   }

   public Iterator getAllInDataPaths()
   {
      return new FilteringIterator(getAllDataPaths(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((IDataPath) o).getDirection().equals(Direction.IN);
         }

      });
   }

   public Iterator getAllEventHandlers()
   {
      return eventHandlers.iterator();
   }

   public ModelElementList<IEventHandler> getEventHandlers()
   {
      return eventHandlers;
   }

   public void removeFromEventHandlers(IEventHandler handler)
   {
      eventHandlers.remove(handler);
   }

   public void addToEventHandlers(IEventHandler handler)
   {
      eventHandlers.add(handler);
   }

   public IEventHandler createEventHandler(String id, String name, String description,
         IEventConditionType type, int elementOID)
   {
      IEventHandler result = new EventHandlerBean(id, name, description);
      eventHandlers.add(result);
      result.register(elementOID);
      result.setConditionType(type);

      return result;
   }

   public IEventHandler findHandlerById(String id)
   {
      return (IEventHandler) eventHandlers.findById(id);
   }

   public Iterator getAllOutDataPaths()
   {
      return new FilteringIterator(getAllDataPaths(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((IDataPath) o).getDirection().equals(Direction.OUT);
         }

      });
   }

   public IActivity getRootActivity()
   {
      for (int i = 0; i < getActivities().size(); ++i)
      {
         IActivity activity = (IActivity) getActivities().get(i);

         if (activity.getInTransitions().isEmpty())
         {
            return activity;
         }
      }

      throw new InternalException("No root activity available.");
   }

   /**
    * Checks if the process definition is consistent.
    */
   public boolean isConsistent()
   {
      Iterator iterator = checkConsistency().iterator();

      while (iterator.hasNext())
      {
         if (((Inconsistency) iterator.next()).getSeverity() == Inconsistency.ERROR)
         {
            return false;
         }
      }

      return true;
   }

   /**
    *
    */
   public void removeFromActivities(IActivity activity)
   {
      markModified();
      activities.remove(activity);
   }

   public void addToDiagrams(Diagram diagram)
   {
      markModified();
      this.diagrams.add(diagram);
   }

   /**
    *
    */
   public void removeFromDiagrams(Diagram diagram)
   {
      markModified();
      diagrams.remove(diagram);
   }

   /**
    *
    */
   public void removeFromTransitions(ITransition transition)
   {
      markModified();

      if (transitions.contains(transition))
      {
         transitions.remove(transition);
      }
      else
      {
         exceptionTransitions.remove(transition);
      }
   }

   /**
    *
    */
   public void removeFromTriggers(ITrigger trigger)
   {
      markModified();

      triggers.remove(trigger);
   }

   public String toString()
   {
      return "Process Definition: " + getName();
   }

   public int getDefaultPriority()
   {
      return defaultPriority;
   }

   public void setDefaultPriority(int priority)
   {
      defaultPriority = priority;
   }

   public void addToFormalParameters(IFormalParameter parameter)
   {
      formalParameters.add(parameter);
   }

   public IFormalParameter findFormalParameter(String id)
   {
      return ModelUtils.findById(formalParameters, id);
   }

   public List<IFormalParameter> getFormalParameters()
   {
      return Collections.unmodifiableList(formalParameters);
   }

   public void setDeclaresInterface(boolean declaresInterface)
   {
      this.declaresInterface = declaresInterface;
   }

   public boolean getDeclaresInterface()
   {
      return declaresInterface;
   }

   public void setFormalParameterMappings(Map<String, String> formalParameterMappings)
   {
      this.formalParameterMappings = formalParameterMappings;
   }

   public IData getMappedData(String parameterId)
   {
      String id = formalParameterMappings.get(parameterId);

      return StringUtils.isEmpty(id) ? null : ((IModel) getModel()).findData(id);
   }

   public String getMappedDataId(String parameterId)
   {
      return formalParameterMappings.get(parameterId);
   }

   public IReference getExternalReference()
   {
      return externalReference;
   }

   public void setExternalReference(IReference externalReference)
   {
      this.externalReference = externalReference;
   }
   

   public AccessPoint getAccessPoint(String contextId, String id)
   {
      return getAccessPoint(contextId, id, null);
   }

   public AccessPoint getAccessPoint(String contextId, String id, Direction direction)
   {
      IApplicationContext context = getContext(contextId);
      if (context != null)
      {
         return context.findAccessPoint(id, direction);
      }
      return null;
   }   
   
   
   public IApplicationContext getContext(String contextId)
   {
      if (PredefinedConstants.ENGINE_CONTEXT.equals(contextId))
      {
         return engineContext;
      }

      if (PredefinedConstants.DEFAULT_CONTEXT.equals(contextId))
      {
         return defaultContext;
      }

      return null;
   }   

   private class MyEngineContext extends ApplicationContextBean
   {
      private static final long serialVersionUID = 1L;

      private transient Map accessPoints = new HashMap();

      public MyEngineContext()
      {
         accessPoints.put(PredefinedConstants.PROCESS_INSTANCE_ACCESSPOINT,
               JavaDataTypeUtils.createIntrinsicAccessPoint(this,
                     PredefinedConstants.PROCESS_INSTANCE_ACCESSPOINT,
                     PredefinedConstants.PROCESS_INSTANCE_ACCESSPOINT,
                     "org.eclipse.stardust.engine.api.runtime.ProcessInstance",
                     Direction.OUT, false, null));
      }

      public RootElement getModel()
      {
         return ProcessDefinitionBean.this.getModel();
      }

      public AccessPoint findAccessPoint(String id)
      {
         return (AccessPoint) accessPoints.get(id);
      }

      public AccessPoint findAccessPoint(String id, Direction direction)
      {
         return (AccessPoint) accessPoints.get(id);
      }

      public Iterator getAllAccessPoints()
      {
         return accessPoints.values().iterator();
      }

      public synchronized Iterator getAllInAccessPoints()
      {
         return new FilteringIterator(getAllAccessPoints(), new Predicate()
         {
            public boolean accept(Object point)
            {
               AccessPoint candidate = (AccessPoint) point;
               return candidate.getDirection() == Direction.IN
                     || candidate.getDirection() == Direction.IN_OUT;
            }
         });
      }

      public synchronized Iterator getAllOutAccessPoints()
      {
         return new FilteringIterator(getAllAccessPoints(), new Predicate()
         {
            public boolean accept(Object point)
            {
               AccessPoint candidate = (AccessPoint) point;
               return candidate.getDirection() == Direction.OUT
                     || candidate.getDirection() == Direction.IN_OUT;
            }
         });
      }
   }

   private class MyDefaultContext extends ApplicationContextBean
   {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private MyDefaultContext()
      {
         super(PredefinedConstants.DEFAULT_CONTEXT, true);
      }

      public RootElement getModel()
      {
         return ProcessDefinitionBean.this.getModel();
      }
   }
}
