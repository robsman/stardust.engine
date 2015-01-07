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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.runtime.UnresolvedExternalReference;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelElementListAdapter;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailActivityBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelRefBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.VfsOperationAccessPointProvider;

/**
 * @author mgille
 */
public class ActivityBean extends IdentifiableElementBean implements IActivity
{
   private static final long serialVersionUID = 1L;

   private static final String TAG_INTERMEDIATE_EVENT_HOST = "stardust:bpmnIntermediateEventHost";

   public static final String IMPLEMENTATION_TYPE_ATT = "Implementation Type";
   private ImplementationType implementationType;

   public static final String JOIN_TYPE_ATT = "Join Type";
   private JoinSplitType joinType = JoinSplitType.None;

   public static final String SPLIT_TYPE_ATT = "Split Type";
   private JoinSplitType splitType = JoinSplitType.None;

   public static final String ALLOWS_ABORT_BY_PERFORMER_ATT = "Allows abort by Performer";
   private boolean allowsAbortByPerformer;

   public static final String HIBERNATE_ON_CREATION_ATT = "Hibernate On Creation";
   private boolean hibernateOnCreation;

   private IProcessDefinition implementationProcessDefinition;

   public static final String SUBPROCESS_MODE_ATT = "Subprocess Execution Mode";

   private SubProcessModeKey subProcessMode;

   private IModelParticipant performer;

   private IApplication application;

   private List<ITransition> inTransitions;

   private List<ITransition> outTransitions;

   private List<ITransition> exceptionTransitions;

   private List dataMappings;

   private ModelElementListAdapter inDataMappings = new ModelElementListAdapter(CollectionUtils.newList());

   private ModelElementListAdapter outDataMappings = new ModelElementListAdapter(CollectionUtils.newList());

   private Link eventHandlers = new Link(this, "Event Handlers");

   private IReference externalReference;

   private ILoopCharacteristics loopCharacteristics;

   private transient IApplicationContext interfaceContext = new MyInterfaceContext();
   private transient IApplicationContext engineContext = new MyEngineContext();
   private transient IApplicationContext noninteractiveAppContext = new MyApplicationContext();
   private transient IApplicationContext defaultContext = new MyDefaultContext();

   private Set<IQualityAssuranceCode> qualityAssuranceCodes;
   private boolean qualityAssuranceEnabled = false;
   private IModelParticipant qualityAssuranceParticipant = null;
   private String qualityAssuranceFormula;
   private int qualityAssuranceProbability;

   /**
    * @param activity
    */
   public static boolean getCopyAllDataAttribute(IActivity activity)
   {
      if (activity.getAllAttributes().containsKey(
            PredefinedConstants.SUBPROCESS_ACTIVITY_COPY_ALL_DATA_ATT))
      {
         return activity
               .getBooleanAttribute(PredefinedConstants.SUBPROCESS_ACTIVITY_COPY_ALL_DATA_ATT);
      }
      else
      {
         // return default values in case this attribute is not set (legacy models)
         SubProcessModeKey mode = activity.getSubProcessMode();
         if (SubProcessModeKey.ASYNC_SEPARATE == mode)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
   }

   ActivityBean()
   {
   }

   public ActivityBean(String id, String name, String description)
   {
      super(id, name);
      setDescription(description);
   }

   public void addToDataMappings(IDataMapping dataMapping)
   {
      if (dataMappings == null)
      {
         dataMappings = CollectionUtils.newList();
      }
      dataMappings.add(dataMapping);

      if (Direction.IN == dataMapping.getDirection() || Direction.IN_OUT == dataMapping.getDirection())
      {
         inDataMappings.getDelegate().add(dataMapping);
      }
      if (Direction.OUT == dataMapping.getDirection() || Direction.IN_OUT == dataMapping.getDirection())
      {
         outDataMappings.getDelegate().add(dataMapping);
      }
   }

   public ImplementationType getImplementationType()
   {
      return implementationType;
   }

   public void setImplementationType(ImplementationType type)
   {
      implementationType = type;
   }

   public ILoopCharacteristics getLoopCharacteristics()
   {
      return loopCharacteristics;
   }

   @Override
   public void setLoopCharacteristics(ILoopCharacteristics loopCharacteristics)
   {
      this.loopCharacteristics = loopCharacteristics;
   }

   public LoopType getLoopType()
   {
      return loopCharacteristics instanceof IStandardLoopCharacteristics
            ? ((IStandardLoopCharacteristics) loopCharacteristics).testBefore()
                  ? LoopType.While
                  : LoopType.Repeat
            : LoopType.None;
   }

   public String getLoopCondition()
   {
      return loopCharacteristics instanceof IStandardLoopCharacteristics
            ? ((IStandardLoopCharacteristics) loopCharacteristics).getLoopCondition()
            : null;
   }

   public JoinSplitType getJoinType()
   {
      return joinType;
   }

   public void setJoinType(JoinSplitType type)
   {
      markModified();
      joinType = type;
   }

   public JoinSplitType getSplitType()
   {
      return splitType;
   }

   public void setSplitType(JoinSplitType type)
   {
      markModified();
      splitType = type;
   }

   public boolean getAllowsAbortByPerformer()
   {
      return allowsAbortByPerformer;
   }

   public void setAllowsAbortByPerformer(boolean allowsAbortByPerformer)
   {
      markModified();
      this.allowsAbortByPerformer = allowsAbortByPerformer;
   }

   /**
    * @return The process definition, the activity belongs to.
    */
   public IProcessDefinition getProcessDefinition()
   {
      return (IProcessDefinition) parent;
   }

   /**
    * @return The (sub)process definition, the activity uses
    *         for implementation.
    */
   public IProcessDefinition getImplementationProcessDefinition()
   {
      if (implementationType != ImplementationType.SubProcess)
      {
         return null;
      }

      if (externalReference != null)
      {
         IExternalPackage externalPackage = externalReference.getExternalPackage();
         IModel externalModel = externalPackage.getReferencedModel();
         if (externalModel != null)
         {
            String uuid = getStringAttribute("carnot:connection:uuid");
            IProcessDefinition referenceProcess = externalModel.findProcessDefinition(StringUtils.isEmpty(uuid)
                  ? externalReference.getId() : externalReference.getId() + "?uuid=" + uuid);
            if (referenceProcess != null)
            {
               return getImplementation(referenceProcess);
            }
         }
      }

      if (implementationProcessDefinition != null && implementationProcessDefinition.getDeclaresInterface())
      {
         return getImplementation(implementationProcessDefinition);
      }

      return implementationProcessDefinition;
   }

   private IProcessDefinition getImplementation(IProcessDefinition referenceProcess)
   {
      IData dataObject = null;
      String dataPath = null;
      if (hasRuntimeBinding())
      {
         String dataId = getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
         dataObject = ((IModel) getModel()).findData(dataId);
         if (dataObject == null)
         {
            throw new InternalException("No data '" + dataId
                  + "' available for process implementation retrieval.");
         }
         dataPath = getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);
      }
      return ModelRefBean.getPrimaryImplementation(referenceProcess, dataObject, dataPath);
   }

   private boolean hasRuntimeBinding()
   {
      return getBooleanAttribute(PredefinedConstants.BINDING_ATT);
   }

   /**
    * Sets the process definition which is used by the implementation of this
    * activity. This method throws an InternalException if the implementation
    * type of the activity is not ImplementationTypeKey.SUBPROCESS.
    *
    * @param processDefinition The (sub)process definition, the activity uses
    *                          for implementation.
    */
   public void setImplementationProcessDefinition(IProcessDefinition processDefinition)
   {
      implementationProcessDefinition = processDefinition;
   }

   public SubProcessModeKey getSubProcessMode()
   {
      // TODO: set default value Sync Shared
      return subProcessMode == null ? SubProcessModeKey.SYNC_SHARED : subProcessMode;
   }

   public void setSubProcessMode(SubProcessModeKey mode)
   {
      if (!CompareHelper.areEqual(mode, subProcessMode))
      {
         markModified();
         subProcessMode = mode;
      }
   }

   public Iterator getAllInTransitions()
   {
      if (inTransitions == null)
      {
         return Collections.emptyList().iterator();
      }
      return inTransitions.iterator();
   }

   public ModelElementList getInTransitions()
   {
      return ModelUtils.getModelElementList(inTransitions);
   }

   public Iterator getAllOutTransitions()
   {
      if (outTransitions == null)
      {
         return Collections.emptyList().iterator();
      }
      return outTransitions.iterator();
   }

   public ModelElementList getOutTransitions()
   {
      return ModelUtils.getModelElementList(outTransitions);
   }

   public ITransition getExceptionTransition(final String eventHandlerId)
   {
      if (exceptionTransitions == null)
      {
         return null;
      }

      final String condition = TransitionBean.ON_BOUNDARY_EVENT_PREDICATE + "(" + eventHandlerId + ")";
      for (final ITransition t : exceptionTransitions)
      {
         if (condition.equals(t.getCondition()))
         {
            return t;
         }
      }
      return null;
   }

   public boolean hasExceptionTransitions()
   {
      return exceptionTransitions != null && !exceptionTransitions.isEmpty();
   }

   public IModelParticipant getPerformer()
   {
      return performer;
   }

   public void setPerformer(IModelParticipant performer)
   {
      this.performer = performer;
   }

   public IApplication getApplication()
   {
      if (implementationType != ImplementationType.Application)
      {
         return null;
      }

      if (externalReference != null)
      {
         IExternalPackage pkg = externalReference.getExternalPackage();
         IModel otherModel = pkg.getReferencedModel();
         if (otherModel != null)
         {
            String uuid = getStringAttribute("carnot:connection:uuid");
            return otherModel.findApplication(StringUtils.isEmpty(uuid)
                  ? externalReference.getId() : externalReference.getId() + "?uuid=" + uuid);
         }
      }
      return application;
   }

   public void setApplication(IApplication app)
   {
      application = app;
   }

   public IDataMapping createDataMapping(String id, String name, IData data, Direction direction,
         String applicationAccessPointId, int elementOID)
   {
      DataMappingBean dataMapping = new DataMappingBean(id, name, data, this, direction,
            applicationAccessPointId);
      dataMapping.setParent(this);
      addToDataMappings(dataMapping);
      dataMapping.register(elementOID);

      return dataMapping;
   }

   public IDataMapping createDataMapping(String id, String name, IData data, Direction direction)
   {
      return createDataMapping(id, name, data, direction, null, 0);
   }

   public void removeFromDataMappings(IDataMapping dataMapping)
   {
      markModified();
      // @todo/hiob (ub)???

      if (Direction.IN == dataMapping.getDirection() || Direction.IN_OUT == dataMapping.getDirection())
      {
         inDataMappings.getDelegate().remove(dataMapping);
      }
      if (Direction.OUT == dataMapping.getDirection() || Direction.IN_OUT == dataMapping.getDirection())
      {
         outDataMappings.getDelegate().remove(dataMapping);
      }

      if (dataMappings != null)
      {
         dataMappings.remove(dataMapping);
      }
      dataMapping.markModified();
   }

   public void removeAllDataMappings()
   {
      inDataMappings.getDelegate().clear();
      outDataMappings.getDelegate().clear();

      if (dataMappings != null)
      {
         dataMappings.clear();
      }
   }

   public Iterator getAllDataMappings()
   {
      if (dataMappings == null)
      {
         return Collections.emptyList().iterator();
      }
      return dataMappings.iterator();
   }

   public ModelElementList<IDataMapping> getDataMappings()
   {
      return ModelUtils.getModelElementList(dataMappings);
   }

   public Iterator getAllInDataMappings()
   {
      return new FilteringIterator(getAllDataMappings(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((IDataMapping) o).getDirection().equals(Direction.IN);
         }
      });

   }

   public ModelElementList<IDataMapping> getInDataMappings()
   {
      return inDataMappings;
   }

   public Iterator getAllOutDataMappings()
   {
      return new FilteringIterator(getAllDataMappings(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return ((IDataMapping) o).getDirection().equals(Direction.OUT);
         }
      });

   }

   public ModelElementList getOutDataMappings()
   {
      return outDataMappings;
   }

   public IDataMapping findDataMappingById(String id, Direction direction, String context)
   {
      for (int i = 0; i < getDataMappings().size(); ++i)
      {
         IDataMapping dataMapping = (IDataMapping) getDataMappings().get(i);

         if (id.equals(dataMapping.getId()) && dataMapping.getDirection().equals(direction)
               && CompareHelper.areEqual(dataMapping.getContext(), context))
         {
            return dataMapping;
         }
      }

      return null;
   }

   /**
    * Retrieves all data mappings for the data <code>data</code>.
    */
   public Iterator findDataMappings(IData data, Direction direction)
   {
      final String dataId = data.getId();
      Iterator candidates = null;
      if (direction == null)
      {
         candidates = getAllDataMappings();
      }
      else if (direction.equals(Direction.IN))
      {
         candidates = getAllInDataMappings();
      }
      else if (direction.equals(Direction.OUT))
      {
         candidates = getAllOutDataMappings();
      }
      return new FilteringIterator(candidates, new Predicate()
      {
         public boolean accept(Object mapping)
         {
            return ((IDataMapping) mapping).getData() != null
                  && ((IDataMapping) mapping).getData().getId().equals(dataId);
         }
      });
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies
    * of the activity.
    */
   public void checkConsistency(List inconsistencies)
   {
      super.checkConsistency(inconsistencies);
      checkId(inconsistencies);

      if (getId() != null)
      {
         // check for unique Id
         IActivity a = getProcessDefinition().findActivity(getId());
         if (a != null && a != this)
         {
            BpmValidationError error = BpmValidationError.ACTY_DUPLICATE_ID.raise(getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         // check id to fit in maximum length
         if (getId().length() > AuditTrailActivityBean.getMaxIdLength())
         {
            BpmValidationError error = BpmValidationError.ACTY_ID_EXCEEDS_MAXIMUM_LENGTH.raise(
                  getName(), AuditTrailActivityBean.getMaxIdLength());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
      }

      // Rule: Each activity must be assigned a performer.
      if (isInteractive())
      {
         IModelParticipant performer = getPerformer();

         if (performer == null)
         {
            BpmValidationError error = BpmValidationError.ACTY_NO_PERFORMER.raise(
                  getId(), getProcessDefinition().getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
         else if (((IModel) getModel()).findParticipant(getPerformer().getId()) == null)
         {
            BpmValidationError error = BpmValidationError.ACTY_PERFORMER_DOES_NOT_EXIST.raise(
                  getPerformer().getId(), getId());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         if (isQualityAssuranceEnabled())
         {
            if (performer != null && performer instanceof IConditionalPerformer)
            {
               BpmValidationError error = BpmValidationError.ACTY_PERFORMER_SHOULD_NOT_BE_CONDITIONAL_PERFORMER.raise(
                     getId(), getProcessDefinition().getName());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            IModelParticipant qualityAssurancePerformer = getQualityAssurancePerformer();
            if (qualityAssurancePerformer == null)
            {
               BpmValidationError error = BpmValidationError.ACTY_NO_QA_PERFORMER_SET.raise(
                     getId(), getProcessDefinition().getName());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            else if (qualityAssurancePerformer instanceof IConditionalPerformer)
            {
               BpmValidationError error = BpmValidationError.ACTY_QA_PERFORMER_SHOULD_NOT_BE_CONDITIONAL_PERFORMER.raise(
                     getId(), getProcessDefinition().getName());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
         }
      }

      // Rule: If the activity represent an associated subprocess this process must be exist
      if (getImplementationType().equals(ImplementationType.SubProcess))
      {
         try
         {
            IProcessDefinition ipd = getImplementationProcessDefinition();
            if (ipd == null)
            {
               BpmValidationError error = BpmValidationError.ACTY_NO_IMPLEMENTATION_PROCESS_SET_FOR_SUBPROCESS_ACTIVITY.raise(getId());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
         }
         catch (UnresolvedExternalReference ex)
         {
            // ignore
         }
         SubProcessModeKey mode = getSubProcessMode();
         if (mode == null)
         {
            BpmValidationError error = BpmValidationError.ACTY_SUBPROCESSMODE_NOT_SET.raise(getId());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }

         if (loopCharacteristics instanceof IMultiInstanceLoopCharacteristics)
         {
            if (mode == SubProcessModeKey.SYNC_SHARED &&
                  !((IMultiInstanceLoopCharacteristics) loopCharacteristics).isSequential())
            {
               BpmValidationError error = BpmValidationError.ACTY_INCOMPATIBLE_SUBPROCESSMODE.raise(getId());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
            }
         }
      }

      // Rule: if implementation type is application, an application must be set
      if (getImplementationType().equals(ImplementationType.Application))
      {
         try
         {
            IApplication app = getApplication();
            if (app == null)
            {
               BpmValidationError error = BpmValidationError.ACTY_NO_APPLICATION_SET_FOR_APPLICATION_ACTIVITY.raise(getId());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            else
            {
               ApplicationTypeBean type = (ApplicationTypeBean) app.getType();
               // change this
               if(type != null && type.getId().equals("dmsOperation"))
               {
                  boolean folder = (Boolean) app.getBooleanAttribute(DmsConstants.PRP_RUNTIME_DEFINED_TARGET_FOLDER);
                  String operation = app.getStringAttribute(DmsConstants.PRP_OPERATION_NAME);

                  if(folder && operation != null && operation.equals("addDocument"))
                  {
                     boolean valid = false;
                     for (Iterator iterator = getAllInDataMappings(); iterator.hasNext();)
                     {
                        IDataMapping dataMapping = (IDataMapping) iterator.next();
                        IData data = dataMapping.getData();
                        if(data != null)
                        {
                           DataTypeBean dataType = (DataTypeBean) data.getType();
                           if(dataType.getId().equals(DmsConstants.DATA_TYPE_DMS_FOLDER))
                           {
                              AccessPoint activityAccessPoint = dataMapping.getActivityAccessPoint();
                              if(activityAccessPoint != null && activityAccessPoint.getId().equals(VfsOperationAccessPointProvider.AP_ID_TARGET_FOLDER))
                              {
                                 valid = true;
                                 break;
                              }
                           }
                        }
                     }

                     if ( !valid)
                     {
                        BpmValidationError error = BpmValidationError.ACTY_NO_ACCESS_POINT_FOR_APPLICATION.raise(getId());
                        inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                     }
                  }
               }
            }
         }
         catch (UnresolvedExternalReference ex)
         {
            // ignore
         }
      }

      String inputId = null;
      if (loopCharacteristics instanceof IMultiInstanceLoopCharacteristics)
      {
         inputId = ((IMultiInstanceLoopCharacteristics) loopCharacteristics).getInputParameterId();
         if (inputId == null)
         {
            BpmValidationError error = BpmValidationError.ACTY_NO_LOOP_INPUT_DATA.raise(getId());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }

      boolean hasLoopInput = false;
      // check consistencies for DataMappings
      for (Iterator iterator = getAllDataMappings(); iterator.hasNext();)
      {
         IDataMapping dataMapping = (IDataMapping) iterator.next();
         try
         {
            dataMapping.checkConsistency(inconsistencies);
         }
         catch (UnresolvedExternalReference ex)
         {
            // ignore
         }
         if (inputId != null && dataMapping.getDirection() == Direction.IN
               && inputId.equals(dataMapping.getContext() + ":" + dataMapping.getActivityAccessPointId()))
         {
            hasLoopInput = true;
         }
      }
      if (inputId != null && !hasLoopInput)
      {
         BpmValidationError error = BpmValidationError.ACTY_NO_LOOP_INPUT_DATA.raise(getId());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }

      // check consistencies for EventHandlers
      for (Iterator iterator = getAllEventHandlers(); iterator.hasNext();)
      {
         IEventHandler eventHandler = (IEventHandler) iterator.next();
         eventHandler.checkConsistency(inconsistencies);

         checkBoundaryEventConsistency(eventHandler, inconsistencies);
      }
      checkBoundaryEventsConsistency(eventHandlers, inconsistencies);
      checkIntermediateEventConsistency(inconsistencies);
   }

   /**
    * if it's a boundary event, there SHOULD be a corresponding exception flow transition
    */
   private void checkBoundaryEventConsistency(final IEventHandler eventHandler,
         final List<Inconsistency> inconsistencies)
   {
      if (eventHandler.getAttribute(EventHandlerBean.BOUNDARY_EVENT_TYPE_KEY) != null)
      {
         final ITransition exceptionTransition = getExceptionTransition(eventHandler.getId());
         if (exceptionTransition == null)
         {
            BpmValidationError error = BpmValidationError.ACTY_NO_EXCEPTION_FLOW_TRANSITION_FOR_EVENT_HANDLER.raise(eventHandler.getId());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }
   }

   /**
    * if there are multiple error boundary events, the exception hierarchies SHOULD be disjunct
    */
   private void checkBoundaryEventsConsistency(final Link eventHandlers, final List<Inconsistency> inconsistencies)
   {
      for (int i=0; i<eventHandlers.size(); i++)
      {
         final IEventHandler x = (IEventHandler) eventHandlers.get(i);
         if ( !isErrorBoundaryEvent(x))
         {
            continue;
         }

         for (int j=i+1; j<eventHandlers.size(); j++)
         {
            final IEventHandler y = (IEventHandler) eventHandlers.get(j);
            if ( !isErrorBoundaryEvent(y))
            {
               continue;
            }

            if ( !exceptionHierarchiesAreDisjunct(x, y))
            {
               BpmValidationError error = BpmValidationError.ACTY_BOUNDARY_EVENTS_WITH_UNDISJUNCT_TYPE_HIERARCHIES.raise(
                     x.getId(), y.getId());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
            }
         }
      }
   }

   private boolean isErrorBoundaryEvent(final IEventHandler eventHandler)
   {
      if (eventHandler.getAttribute(EventHandlerBean.BOUNDARY_EVENT_TYPE_KEY) == null)
      {
         return false;
      }

      if ( !PredefinedConstants.EXCEPTION_CONDITION.equals(eventHandler.getType().getId()))
      {
         return false;
      }

      return true;
   }

   private boolean exceptionHierarchiesAreDisjunct(final IEventHandler x, final IEventHandler y)
   {
      final String xExceptionName = (String) x.getAttribute(PredefinedConstants.EXCEPTION_CLASS_ATT);
      final String yExceptionName = (String) y.getAttribute(PredefinedConstants.EXCEPTION_CLASS_ATT);

      final Class<?> xException = Reflect.getClassFromClassName(xExceptionName);
      final Class<?> yException = Reflect.getClassFromClassName(yExceptionName);

      if (xException.isAssignableFrom(yException) || yException.isAssignableFrom(xException))
      {
         return false;
      }

      return true;
   }

   /**
    * if it's an intermediate event, it SHOULD have one inbound and one outbound sequence flow
    */
   private void checkIntermediateEventConsistency(final List<Inconsistency> inconsistencies)
   {
      final Boolean isIntermediateEvent = (Boolean) getAttribute(TAG_INTERMEDIATE_EVENT_HOST);
      if (isIntermediateEvent != null && isIntermediateEvent.booleanValue())
      {
         if (getInTransitions().size() != 1 || getOutTransitions().size() != 1)
         {
            BpmValidationError error = BpmValidationError.ACTY_INTERMEDIATE_EVENTS_MUST_HAVE_ONE_IN_AND_OUTBOUND_SEQUENCE_FLOW.raise();
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }
   }

   /**
    * Checks, wether there is a path from <tt>startActivity</tt> to an activity
    * with AND join and another path back to <tt>startActivity</tt>.
    * <p/>
    * The critical activity is returned.
    */
/*   private IActivity checkXORANDBlock(IActivity startActivity,
         IActivity currentActivity)
   {
      Iterator _transitions = currentActivity.getAllOutTransitions();
      ITransition _inTransition;

      while (_transitions.hasNext())
      {
         _inTransition = (ITransition) _transitions.next();
         currentActivity = _inTransition.getToActivity();

         if (currentActivity.getJoinType() == JoinSplitType.And
               && checkBackXORANDBlock(startActivity, currentActivity, _inTransition))
         {
            return currentActivity;
         }

         return checkXORANDBlock(startActivity, currentActivity);
      }

      return null;
   }*/

   /**
    *
    */
/*   public boolean checkBackXORANDBlock(IActivity startActivity,
         IActivity currentActivity,
         ITransition inTransition)
   {
      Iterator _transitions = currentActivity.getAllInTransitions();
      ITransition _transition;

      while (_transitions.hasNext())
      {
         _transition = (ITransition) _transitions.next();

         if (inTransition != null &&
               _transition.equals(inTransition))
         {
            // All subsequent steps will not require skipping the in transition

            inTransition = null;

            continue;
         }

         currentActivity = _transition.getFromActivity();
         //if a corresponding AND split is found on backward traversal for
         //the AND join activity.
         if (currentActivity.getSplitType() == JoinSplitType.And)
         {
            return false;
         }
         if (currentActivity.equals(startActivity))
         {
            return true;
         }

         return checkBackXORANDBlock(startActivity, currentActivity,
               inTransition);
      }

      return false;
   }*/

   public boolean isInteractive()
   {
      if (getImplementationType().equals(ImplementationType.Manual))
      {
         return true;
      }
      else if (getImplementationType().equals(ImplementationType.Application))
      {
         try
         {
            IApplication app = getApplication();
            if (app != null)
            {
               return app.isInteractive();
            }
         }
         catch (UnresolvedExternalReference ex)
         {
            // ignore
         }
      }
      return false;
   }

   public String toString()
   {
      return "Activity: " + getName();
   }

   public Set getApplicationOutDataMappingAccessPoints()
   {
      IApplication app = getApplication();
      if (app != null && !app.isInteractive())
      {
         Set result = new TreeSet();
         for (int i = 0; i < getOutDataMappings().size(); ++i)
         {
            IDataMapping mapping = (IDataMapping) getOutDataMappings().get(i);
            if (PredefinedConstants.APPLICATION_CONTEXT.equals(mapping.getContext()))
            {
               result.add(mapping.getActivityAccessPointId());
            }
         }
         return result;
      }
      else
      {
         return Collections.EMPTY_SET;
      }
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

   public Iterator findExceptionHandlers(final IData data)
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public Iterator getAllContexts()
   {
      IApplication app = getApplication();
      if (isInteractive())
      {
         Iterator intrinsic = new SplicingIterator(new OneElementIterator(engineContext),
               new OneElementIterator(defaultContext));
         if (app != null)
         {
            return new SplicingIterator(intrinsic, app.getAllContexts());
         }
         else
         {
            return intrinsic;
         }
      }
      else
      {
         if (app != null)
         {
            return new SplicingIterator(new OneElementIterator(engineContext),
                  new OneElementIterator(noninteractiveAppContext));
         }
         else
         {
            return new OneElementIterator(engineContext);
         }
      }
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

      if (PredefinedConstants.APPLICATION_CONTEXT.equals(contextId))
      {
         return noninteractiveAppContext;
      }

      if (PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(contextId))
      {
         return interfaceContext;
      }

      IApplication app = getApplication();
      if (app != null && isInteractive())
      {
         return app.findContext(contextId);
      }

      return null;
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

   public IEventHandler createEventHandler(String id, String name, String description, IEventConditionType type, int elementOID)
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

   public boolean isHibernateOnCreation()
   {
      return hibernateOnCreation;
   }

   public void setHibernateOnCreation(boolean hibernateOnCreation)
   {
      this.hibernateOnCreation = hibernateOnCreation;
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
         if (handler.getType().getId().equals(type))
         {
            return true;
         }
      }
      return false;
   }

   public IReference getExternalReference()
   {
      return externalReference;
   }

   public void setExternalReference(IReference externalReference)
   {
      this.externalReference = externalReference;
      this.application = null;
      this.implementationProcessDefinition = null;
   }

   public class MyInterfaceContext extends ApplicationContextBean
   {
      private static final long serialVersionUID = 1L;

      public MyInterfaceContext()
      {
         super(PredefinedConstants.PROCESSINTERFACE_CONTEXT, true);
      }

      public AccessPoint findAccessPoint(String id)
      {
         return findAccessPoint(id, null);
      }

      public AccessPoint findAccessPoint(String id, Direction direction)
      {
         IProcessDefinition pd = getImplementationProcessDefinition();
         if (pd == null)
         {
            return null;
         }
         IFormalParameter param = pd.findFormalParameter(id);
         if (param == null)
         {
            return null;
         }
         if (direction != null && !param.getDirection().isCompatibleWith(direction))
         {
            return null;
         }
         return param.getData();
      }

      public Iterator getAllAccessPoints()
      {
         return getAccessPoints(null);
      }

      public Iterator getAllInAccessPoints()
      {
         return getAccessPoints(Direction.IN);
      }

      public Iterator getAllOutAccessPoints()
      {
         return getAccessPoints(Direction.OUT);
      }

      public Iterator getAccessPoints(final Direction filter)
      {
         IProcessDefinition pd = getImplementationProcessDefinition();
         return pd == null
            ? Collections.emptyList().iterator()
            : new TransformingIterator<IFormalParameter, AccessPoint>(
               pd.getFormalParameters().iterator(),
               new Functor<IFormalParameter, AccessPoint>()
               {
                  public AccessPoint execute(IFormalParameter param)
                  {
                     return param.getData();
                  }
               },
               filter == null ? null : new Predicate<IFormalParameter>()
               {
                  public boolean accept(IFormalParameter o)
                  {
                     return filter.isCompatibleWith(o.getDirection());
                  }
               }
         );
      }

      public RootElement getModel()
      {
         return ActivityBean.this.getModel();
      }
   }

   public class MyApplicationContext extends ApplicationContextBean
   {
      private static final long serialVersionUID = 1L;

      private final List<AccessPoint> EMPTY = Collections.emptyList();

      public MyApplicationContext()
      {
         super(PredefinedConstants.APPLICATION_CONTEXT, true);
      }

      public AccessPoint findAccessPoint(String id)
      {
         IApplication app = getApplication();
         return app == null ? null : app.findAccessPoint(id);
      }

      public AccessPoint findAccessPoint(String id, Direction direction)
      {
         IApplication app = getApplication();
         return app == null ? null : app.findAccessPoint(id, direction);
      }

      public Iterator getAllAccessPoints()
      {
         IApplication app = getApplication();
         return app == null ? EMPTY.iterator() : app.getAllAccessPoints();
      }

      public Iterator getAllInAccessPoints()
      {
         IApplication app = getApplication();
         return app == null ? EMPTY.iterator() : app.getAllInAccessPoints();
      }

      public Iterator getAllOutAccessPoints()
      {
         IApplication app = getApplication();
         return app == null ? EMPTY.iterator() : app.getAllOutAccessPoints();
      }

      public String getProviderClass()
      {
         IApplication app = getApplication();
         return app == null ? null : app.getProviderClass();
      }

      public RootElement getModel()
      {
         return ActivityBean.this.getModel();
      }
   }

   private class MyEngineContext extends ApplicationContextBean
   {
      private static final long serialVersionUID = 1L;

      private transient Map accessPoints = new HashMap();

      public MyEngineContext()
      {
         super(PredefinedConstants.ENGINE_CONTEXT, true);
         accessPoints.put(PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT,
               JavaDataTypeUtils.createIntrinsicAccessPoint(this,
            PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT,
                     PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT,
            "org.eclipse.stardust.engine.api.runtime.ActivityInstance", Direction.OUT, false, null));
      }

      public RootElement getModel()
      {
         return ActivityBean.this.getModel();
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
         return ActivityBean.this.getModel();
      }
   }

   public void setQualityAssuranceEnabled()
   {
      qualityAssuranceEnabled = true;
   }

   public boolean isQualityAssuranceEnabled()
   {
      return qualityAssuranceEnabled;
   }

   public void setQualityAssuranceCodes(Set<IQualityAssuranceCode> qualityCodes)
   {
      this.qualityAssuranceCodes = qualityCodes;
   }

   public Set<IQualityAssuranceCode> getQualityAssuranceCodes()
   {
      return qualityAssuranceCodes;
   }

   public void setQualityAssurancePerformer(IModelParticipant participant)
   {
      qualityAssuranceParticipant = participant;
   }

   public IModelParticipant getQualityAssurancePerformer()
   {
      return qualityAssuranceParticipant;
   }

   public void setQualityAssuranceFormula(String formula)
   {
      qualityAssuranceFormula = formula;
   }

   public String getQualityAssuranceFormula()
   {
      return qualityAssuranceFormula;
   }

   public void setQualityAssuranceProbability(int probability)
   {
      qualityAssuranceProbability = probability;
   }

   public int getQualityAssuranceProbability()
   {
      return qualityAssuranceProbability;
   }
}