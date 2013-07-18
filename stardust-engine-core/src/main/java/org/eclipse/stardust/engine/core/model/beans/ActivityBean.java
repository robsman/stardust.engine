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

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.*;
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
   
   public static final String IMPLEMENTATION_TYPE_ATT = "Implementation Type";
   private ImplementationType implementationType;

   public static final String LOOP_TYPE_ATT = "Loop Type";
   private LoopType loopType = LoopType.None;

   public static final String LOOP_CONDITION_ATT = "Loop Condition";
   private String loopCondition;

   public static final String JOIN_TYPE_ATT = "Join Type";
   private JoinSplitType joinType = JoinSplitType.None;

   public static final String SPLIT_TYPE_ATT = "Split Type";
   private JoinSplitType splitType = JoinSplitType.None;

   public static final String ALLOWS_ABORT_BY_PERFORMER_ATT = "Allows abort by Performer";
   private boolean allowsAbortByPerformer;

   public static final String HIBERNATE_ON_CREATION_ATT = "Hibernate On Creation";
   private boolean hibernateOnCreation;

   private IProcessDefinition implementationProcessDefinition = null;

   public static final String SUBPROCESS_MODE_ATT = "Subprocess Execution Mode";
   
   private SubProcessModeKey subProcessMode;

   private IModelParticipant performer = null;

   private IApplication application = null;

   private List<ITransition> inTransitions = null;

   private List<ITransition> outTransitions = null;
   
   private List<ITransition> exceptionTransitions = null;

   private List dataMappings = null;
   
   private ModelElementListAdapter inDataMappings = new ModelElementListAdapter(CollectionUtils.newList());

   private ModelElementListAdapter outDataMappings = new ModelElementListAdapter(CollectionUtils.newList());

   private Link eventHandlers = new Link(this, "Event Handlers");
   
   private IReference externalReference = null;

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

   public void setLoopType(LoopType type)
   {
      markModified();

      loopType = type;
   }

   public LoopType getLoopType()
   {
      return loopType;
   }

   public String getLoopCondition()
   {
      return loopCondition;
   }

   public void setLoopCondition(String loopCondition)
   {
      markModified();

      this.loopCondition = loopCondition;
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
            IProcessDefinition referenceProcess = externalModel.findProcessDefinition(externalReference.getId());
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
            return otherModel.findApplication(externalReference.getId());
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

   public ModelElementList getInDataMappings()
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
            inconsistencies.add(new Inconsistency("Duplicate ID for activity '" +
                  getName() + "'.", this, Inconsistency.ERROR));
         }
         
         // check id to fit in maximum length
         if (getId().length() > AuditTrailActivityBean.getMaxIdLength())
         {
            inconsistencies.add(new Inconsistency("ID for activity '" + getName()
                  + "' exceeds maximum length of "
                  + AuditTrailActivityBean.getMaxIdLength() + " characters.",
                  this, Inconsistency.ERROR));
         }
      }

      // Rule: Each activity must be assigned a performer.
      if (isInteractive())
      {
         IModelParticipant performer = getPerformer();
         
         if (performer == null)
         {
            inconsistencies.add(new Inconsistency("No performer set for manual or interactive application activity '" + getId()
                  + "' of process '" + getProcessDefinition().getName() + "'.",
                  this,
                  Inconsistency.ERROR));
         }
         else if (((IModel) getModel()).findParticipant(getPerformer().getId()) == null)
         {
            inconsistencies.add(new Inconsistency("The associated performer '" + getPerformer().getId()
                  + "' set for manual or interactive application activity '"
                  + getId() + "' doesn't exist in the model.",
                  this, Inconsistency.ERROR));
         }
         
         if(isQualityAssuranceEnabled())
         {
            if(performer != null && performer instanceof IConditionalPerformer)
            {
               inconsistencies.add(new Inconsistency("Performer should not be a conditional performer for quality assurance activity '" + getId()
                     + "' of process '" + getProcessDefinition().getName() + "'.",
                     this,
                     Inconsistency.ERROR));               
            }
            IModelParticipant qualityAssurancePerformer = getQualityAssurancePerformer();
            if(qualityAssurancePerformer == null)
            {
               inconsistencies.add(new Inconsistency("No quality assurance performer set for quality assurance activity '" + getId()
                     + "' of process '" + getProcessDefinition().getName() + "'.",
                     this,
                     Inconsistency.ERROR));               
            }
            else if(qualityAssurancePerformer instanceof IConditionalPerformer)
            {
               inconsistencies.add(new Inconsistency("Quality assurance performer should not be a conditional performer for quality assurance activity '" + getId()
                     + "' of process '" + getProcessDefinition().getName() + "'.",
                     this,
                     Inconsistency.ERROR));                              
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
               inconsistencies.add(new Inconsistency("No implementation process set for subprocess activity '" +
                     getId() + "'.",
                     this, Inconsistency.ERROR));
            }
         }
         catch (UnresolvedExternalReference ex)
         {
            // ignore
         }
         if (getSubProcessMode() == null)
         {
            inconsistencies.add(new Inconsistency(
                  "Value 'subProcessMode' is not set for activity '" + getId()
                        + "'. This will be evaluated as 'Sync Shared'.", this,
                  Inconsistency.WARNING));
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
               inconsistencies.add(new Inconsistency("No application set for application activity '" +
                     getId() + "'.",
                     this, Inconsistency.ERROR));
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
                     
                     if(!valid)
                     {
                        inconsistencies.add(new Inconsistency("No access point for application '" +
                              app.getId() + "'.",
                              this, Inconsistency.ERROR));                        
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

      // check concistencies for DataMappings
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
      }

      // check consistencies for EventHandlers
      for (Iterator iterator = getAllEventHandlers(); iterator.hasNext();)
      {
         IEventHandler eventHandler = (IEventHandler) iterator.next();
         eventHandler.checkConsistency(inconsistencies);
         
         checkBoundaryEventConsistency(eventHandler, inconsistencies);
      }

      // @todo laokoon (ub): temporarily disabled, leads to stack overflow in cycles

      // Rule: Activity network may not form a XOR-AND block

      /*
      Activity _andJoinActivity;

      if (getSplitType().getValue() == JoinSplitTypeKey.XOR
            && (_andJoinActivity = checkXORANDBlock(this, this)) != null)
      {
         inconsistencies.add(new Inconsistency("XOR split and AND join block exists between activity '" +
               getId() + "' and '" + _andJoinActivity.getId() + "'.",
               this, Inconsistency.ERROR, getProcessDefinition().getName()));
      }
      */
   }

   /**
    * if it's a boundary event, there MUST be a corresponding exception flow transition
    */
   private void checkBoundaryEventConsistency(final IEventHandler eventHandler, final List<Inconsistency> inconsitencies)
   {
      if (eventHandler.getAttribute(EventHandlerBean.BOUNDARY_EVENT_TYPE_KEY) != null)
      {
         final ITransition exceptionTransition = getExceptionTransition(eventHandler.getId());
         if (exceptionTransition == null)
         {
            inconsitencies.add(new Inconsistency("No exception flow transition for event handler with ID '" + eventHandler.getId() + "'.", eventHandler, Inconsistency.ERROR));
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

   public ModelElementList getEventHandlers()
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