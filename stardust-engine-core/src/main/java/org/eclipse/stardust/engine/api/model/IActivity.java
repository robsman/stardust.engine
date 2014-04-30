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
package org.eclipse.stardust.engine.api.model;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 *
 */
public interface IActivity extends IViewable, EventHandlerOwner
{
   void setQualityAssuranceEnabled();

   boolean isQualityAssuranceEnabled();

   void setQualityAssuranceCodes(Set<IQualityAssuranceCode> qualityAssuranceCodes);

   Set<IQualityAssuranceCode> getQualityAssuranceCodes();

   void setQualityAssurancePerformer(IModelParticipant participant);

   IModelParticipant getQualityAssurancePerformer();

   void setQualityAssuranceFormula(String formula);

   String getQualityAssuranceFormula();

   void setQualityAssuranceProbability(int probability);

   int getQualityAssuranceProbability();

   void addToDataMappings(IDataMapping dataMapping);

   ImplementationType getImplementationType();

   //void setLoopType(LoopType type);

   LoopType getLoopType();

   String getLoopCondition();

   //void setLoopCondition(String loopCondition);

   void setImplementationType(ImplementationType type);

   JoinSplitType getJoinType();

   void setJoinType(JoinSplitType type);

   JoinSplitType getSplitType();

   void setSplitType(JoinSplitType type);

   boolean getAllowsAbortByPerformer();

   void setAllowsAbortByPerformer(boolean allows);


   // @todo (france, ub):  what is this?

   /**
    * @return The process definition, the activity belongs to.
    */
   IProcessDefinition getProcessDefinition();

   /**
    * @return The (sub)process definition, the activity uses
    *         for implementation.
    */
   IProcessDefinition getImplementationProcessDefinition();

   /**
    * Sets the process definition which is used by the implementation of this
    * activity. This method throws an InternalException if the implementation
    * type of the activity is not ImplementationTypeKey.SUBPROCESS.
    *
    * @param processDefinition The (sub)process definition, the activity uses
    *                          for implementation.
    */
   void setImplementationProcessDefinition(IProcessDefinition processDefinition);

   /**
    *
    */
   SubProcessModeKey getSubProcessMode();

   /**
    *
    */
   void setSubProcessMode(SubProcessModeKey mode);

   /**
    * @deprecated Use of {@link #getInTransitions()} allows for more efficient iteration.
    */
   Iterator getAllInTransitions();

   ModelElementList getInTransitions();

   /**
    * @deprecated Use of {@link #getOutTransitions()} allows for more efficient iteration.
    */
   Iterator getAllOutTransitions();

   ModelElementList getOutTransitions();

   ITransition getExceptionTransition(String eventHandlerId);

   boolean hasExceptionTransitions();

   /**
    *
    */
   IModelParticipant getPerformer();

   /**
    *
    */
   void setPerformer(IModelParticipant performer);

   /**
    *
    */
   IApplication getApplication();

   /**
    *
    */
   void setApplication(IApplication application);

   /**
    *
    */
   IDataMapping createDataMapping(String id, String name, IData data, Direction direction,
         String applicationAccessPointId, int elementOID);

   /**
    *
    */
   IDataMapping createDataMapping(String id, String name, IData data, Direction direction);

   void removeFromDataMappings(IDataMapping dataMapping);

   void removeAllDataMappings();

   /**
    * @deprecated Use of {@link #getDataMappings()} allows for more efficient iteration.
    */
   Iterator getAllDataMappings();

   ModelElementList<IDataMapping> getDataMappings();

   /**
    * @deprecated Use of {@link #getInDataMappings()} allows for more efficient iteration.
    */
   Iterator getAllInDataMappings();

   ModelElementList<IDataMapping> getInDataMappings();

   /**
    * @deprecated Use of {@link #getOutDataMappings()} allows for more efficient iteration.
    */
   Iterator getAllOutDataMappings();

   ModelElementList getOutDataMappings();

   IDataMapping findDataMappingById(String id, Direction direction, String context);

   /**
    * Retrieves a data mapping for the data <code>data</code>.
    */
   Iterator findDataMappings(IData data, Direction direction);

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the
    * activity.
    */
   void checkConsistency(List inconsistencies);

   /**
    * Checks, wether this activity may be suspended to a worklist.
    */
   boolean isInteractive();

   Set getApplicationOutDataMappingAccessPoints();

   /**
    * Searches for access point with specific context and id.
    * @param context
    * @param id
    * @return
    */
   AccessPoint getAccessPoint(String context, String id);

   /**
    * Searches for access point with specific context, id and direction. Access point
    * direction matches if (1) the parameter direction null
    * (2) direction is exactly the same or (3) direction of access point is IN_OUT.
    * @param context
    * @param id
    * @param direction
    * @return
    */
   AccessPoint getAccessPoint(String context, String id, Direction direction);

   Iterator findExceptionHandlers(IData data);

   Iterator getAllContexts();

   IApplicationContext getContext(String id);

   boolean isHibernateOnCreation();

   void setHibernateOnCreation(boolean hibernate);

   Iterator getAllEventHandlers(String type);

   boolean hasEventHandlers(String type);

   IReference getExternalReference();

   ILoopCharacteristics getLoopCharacteristics();

   void setLoopCharacteristics(ILoopCharacteristics loopCharacteristics);
}