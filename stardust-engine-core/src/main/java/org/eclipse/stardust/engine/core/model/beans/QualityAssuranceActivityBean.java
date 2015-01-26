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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.Hook;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * Decorator class for {@link ActivityBean} used in quality control activity instances
 * @author holger.prause
 * @version $Revision: $
 */
public class QualityAssuranceActivityBean implements IActivity
{
   /**
    *
    */
   private static final long serialVersionUID = -1996372708676893771L;
   private final IActivity delegate;

   public QualityAssuranceActivityBean(IActivity delegate)
   {
      this.delegate = delegate;

   }

   public <T> T getRuntimeAttribute(String name)
   {
      return (T) delegate.getRuntimeAttribute(name);
   }

   public Object setRuntimeAttribute(String name, Object value)
   {
      return delegate.setRuntimeAttribute(name, value);
   }

   public String getName()
   {
      return delegate.getName();
   }

   public String getId()
   {
      return delegate.getId();
   }

   public void markModified()
   {
      delegate.markModified();
   }

   public Map<String, Object> getAllAttributes()
   {
      return delegate.getAllAttributes();
   }

   public void setId(String id)
   {
      delegate.setId(id);
   }

   public void setName(String name)
   {
      delegate.setName(name);
   }

   public <V> void setAllAttributes(Map<String, V> attributes)
   {
      delegate.setAllAttributes(attributes);
   }

   public String getDescription()
   {
      return delegate.getDescription();
   }

   @Deprecated
   public Iterator getAllEventHandlers()
   {
      return delegate.getAllEventHandlers();
   }

   public void setDescription(String description)
   {
      delegate.setDescription(description);
   }

   public void setQualityAssuranceEnabled()
   {
      delegate.setQualityAssuranceEnabled();
   }

   public void delete()
   {
      delegate.delete();
   }

   public void setQualityAssuranceCodes(Set<IQualityAssuranceCode> qualityAssuranceCodes)
   {
      delegate.setQualityAssuranceCodes(qualityAssuranceCodes);
   }

   public RootElement getModel()
   {
      return delegate.getModel();
   }

   public void addReference(Hook reference)
   {
      delegate.addReference(reference);
   }

   public ModelElementList<IEventHandler> getEventHandlers()
   {
      return delegate.getEventHandlers();
   }

   public void setParent(ModelElement parent)
   {
      delegate.setParent(parent);
   }

   public void setAttribute(String name, Object value)
   {
      delegate.setAttribute(name, value);
   }

   public void setQualityAssurancePerformer(IModelParticipant participant)
   {
      delegate.setQualityAssurancePerformer(participant);
   }

   public void removeFromEventHandlers(IEventHandler handler)
   {
      delegate.removeFromEventHandlers(handler);
   }

   public void removeReference(Hook reference)
   {
      delegate.removeReference(reference);
   }

   public void removeAllAttributes()
   {
      delegate.removeAllAttributes();
   }

   public ModelElement getParent()
   {
      return delegate.getParent();
   }

   public void setQualityAssuranceFormula(String formula)
   {
      delegate.setQualityAssuranceFormula(formula);
   }

   public void addToEventHandlers(IEventHandler handler)
   {
      delegate.addToEventHandlers(handler);
   }

   public void removeAttribute(String name)
   {
      delegate.removeAttribute(name);
   }

   public int getElementOID()
   {
      return delegate.getElementOID();
   }

   public boolean getBooleanAttribute(String name)
   {
      return delegate.getBooleanAttribute(name);
   }

   public IEventHandler createEventHandler(String id, String name, String description,
         IEventConditionType type, int elementOID)
   {
      return delegate.createEventHandler(id, name, description, type, elementOID);
   }

   public void register(int oid)
   {
      delegate.register(oid);
   }

   public void setQualityAssuranceProbability(int probability)
   {
      delegate.setQualityAssuranceProbability(probability);
   }

   public long getOID()
   {
      return delegate.getOID();
   }

   public long getLongAttribute(String name)
   {
      return delegate.getLongAttribute(name);
   }

   public boolean isQualityAssuranceEnabled()
   {
      return delegate.isQualityAssuranceEnabled();
   }

   public int getIntegerAttribute(String name)
   {
      return delegate.getIntegerAttribute(name);
   }

   public Set<IQualityAssuranceCode> getQualityAssuranceCodes()
   {
      return delegate.getQualityAssuranceCodes();
   }

   public void setElementOID(int elementOID)
   {
      delegate.setElementOID(elementOID);
   }

   public IEventHandler findHandlerById(String id)
   {
      return delegate.findHandlerById(id);
   }

   public float getFloatAttribute(String name)
   {
      return delegate.getFloatAttribute(name);
   }

   public boolean isTransient()
   {
      return delegate.isTransient();
   }

   public String getStringAttribute(String name)
   {
      return delegate.getStringAttribute(name);
   }

   public IModelParticipant getQualityAssurancePerformer()
   {
      return delegate.getQualityAssurancePerformer();
   }

   public boolean isPredefined()
   {
      return delegate.isPredefined();
   }

   public void setPredefined(boolean predefined)
   {
      delegate.setPredefined(predefined);
   }

   public String getQualityAssuranceFormula()
   {
      return delegate.getQualityAssuranceFormula();
   }

   public String getUniqueId()
   {
      return delegate.getUniqueId();
   }

   public int getQualityAssuranceProbability()
   {
      return delegate.getQualityAssuranceProbability();
   }

   public void addToDataMappings(IDataMapping dataMapping)
   {
      delegate.addToDataMappings(dataMapping);
   }

   public ImplementationType getImplementationType()
   {
      return delegate.getImplementationType();
   }

   /*public void setLoopType(LoopType type)
   {
      delegate.setLoopType(type);
   }*/

   public LoopType getLoopType()
   {
      return delegate.getLoopType();
   }

   public String getLoopCondition()
   {
      return delegate.getLoopCondition();
   }

   /*public void setLoopCondition(String loopCondition)
   {
      delegate.setLoopCondition(loopCondition);
   }*/

   public void setImplementationType(ImplementationType type)
   {
      delegate.setImplementationType(type);
   }

   public JoinSplitType getJoinType()
   {
      return delegate.getJoinType();
   }

   public void setJoinType(JoinSplitType type)
   {
      delegate.setJoinType(type);
   }

   public JoinSplitType getSplitType()
   {
      return delegate.getSplitType();
   }

   public void setSplitType(JoinSplitType type)
   {
      delegate.setSplitType(type);
   }

   public boolean getAllowsAbortByPerformer()
   {
      return delegate.getAllowsAbortByPerformer();
   }

   public void setAllowsAbortByPerformer(boolean allows)
   {
      delegate.setAllowsAbortByPerformer(allows);
   }

   public IProcessDefinition getProcessDefinition()
   {
      return delegate.getProcessDefinition();
   }

   public IProcessDefinition getImplementationProcessDefinition()
   {
      return delegate.getImplementationProcessDefinition();
   }

   public void setImplementationProcessDefinition(IProcessDefinition processDefinition)
   {
      delegate.setImplementationProcessDefinition(processDefinition);
   }

   public SubProcessModeKey getSubProcessMode()
   {
      return delegate.getSubProcessMode();
   }

   public void setSubProcessMode(SubProcessModeKey mode)
   {
      delegate.setSubProcessMode(mode);
   }

   @Deprecated
   public Iterator getAllInTransitions()
   {
      return delegate.getAllInTransitions();
   }

   public ModelElementList getInTransitions()
   {
      return delegate.getInTransitions();
   }

   @Deprecated
   public Iterator getAllOutTransitions()
   {
      return delegate.getAllOutTransitions();
   }

   public ModelElementList getOutTransitions()
   {
      return delegate.getOutTransitions();
   }

   public ITransition getExceptionTransition(String eventHandlerId)
   {
      return delegate.getExceptionTransition(eventHandlerId);
   }

   public boolean hasExceptionTransitions()
   {
      return delegate.hasExceptionTransitions();
   }

   public IModelParticipant getPerformer()
   {
      return delegate.getQualityAssurancePerformer();
   }

   public void setPerformer(IModelParticipant performer)
   {
      delegate.setPerformer(performer);
   }

   public IApplication getApplication()
   {
      return delegate.getApplication();
   }

   public void setApplication(IApplication application)
   {
      delegate.setApplication(application);
   }

   public IDataMapping createDataMapping(String id, String name, IData data,
         Direction direction, String applicationAccessPointId, int elementOID)
   {
      return delegate.createDataMapping(id, name, data, direction,
            applicationAccessPointId, elementOID);
   }

   public IDataMapping createDataMapping(String id, String name, IData data,
         Direction direction)
   {
      return delegate.createDataMapping(id, name, data, direction);
   }

   public void removeFromDataMappings(IDataMapping dataMapping)
   {
      delegate.removeFromDataMappings(dataMapping);
   }

   public void removeAllDataMappings()
   {
      delegate.removeAllDataMappings();
   }

   @Deprecated
   public Iterator getAllDataMappings()
   {
      return delegate.getAllDataMappings();
   }

   public ModelElementList<IDataMapping> getDataMappings()
   {
      return delegate.getDataMappings();
   }

   @Deprecated
   public Iterator getAllInDataMappings()
   {
      return delegate.getAllInDataMappings();
   }

   public ModelElementList getInDataMappings()
   {
      return delegate.getInDataMappings();
   }

   @Deprecated
   public Iterator getAllOutDataMappings()
   {
      return delegate.getAllOutDataMappings();
   }

   public ModelElementList getOutDataMappings()
   {
      return delegate.getOutDataMappings();
   }

   public IDataMapping findDataMappingById(String id, Direction direction, String context)
   {
      return delegate.findDataMappingById(id, direction, context);
   }

   public Iterator findDataMappings(IData data, Direction direction)
   {
      return delegate.findDataMappings(data, direction);
   }

   public void checkConsistency(List inconsistencies)
   {
      delegate.checkConsistency(inconsistencies);
   }

   public boolean isInteractive()
   {
      return delegate.isInteractive();
   }

   public Set getApplicationOutDataMappingAccessPoints()
   {
      return delegate.getApplicationOutDataMappingAccessPoints();
   }

   public AccessPoint getAccessPoint(String context, String id)
   {
      return delegate.getAccessPoint(context, id);
   }

   public AccessPoint getAccessPoint(String context, String id, Direction direction)
   {
      return delegate.getAccessPoint(context, id, direction);
   }

   public Iterator findExceptionHandlers(IData data)
   {
      return delegate.findExceptionHandlers(data);
   }

   public Iterator getAllContexts()
   {
      return delegate.getAllContexts();
   }

   public IApplicationContext getContext(String id)
   {
      return delegate.getContext(id);
   }

   public boolean isHibernateOnCreation()
   {
      return delegate.isHibernateOnCreation();
   }

   public void setHibernateOnCreation(boolean hibernate)
   {
      delegate.setHibernateOnCreation(hibernate);
   }

   public Iterator getAllEventHandlers(String type)
   {
      return delegate.getAllEventHandlers(type);
   }

   public boolean hasEventHandlers(String type)
   {
      return delegate.hasEventHandlers(type);
   }

   public IReference getExternalReference()
   {
      return delegate.getExternalReference();
   }

   @Override
   public <T> T getAttribute(String name)
   {
      return delegate.getAttribute(name);
   }

   @Override
   public ILoopCharacteristics getLoopCharacteristics()
   {
      return delegate.getLoopCharacteristics();
   }

   @Override
   public void setLoopCharacteristics(ILoopCharacteristics loopCharacteristics)
   {
      delegate.setLoopCharacteristics(loopCharacteristics);
   }
}
