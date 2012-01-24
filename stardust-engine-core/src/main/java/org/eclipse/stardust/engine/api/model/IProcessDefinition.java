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

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;


/**
 *
 */
public interface IProcessDefinition
      extends IViewable, IdentifiableElement, EventHandlerOwner
{
   /**
    * Add the activity to this process definition
    */
   void addToActivities(IActivity activity);

   /**
    * Add the transition to this process definition
    */
   void addToTransitions(ITransition transition);

   /**
    * Retrieves a vector with all inconsistencies of the model.
    */
   List checkConsistency();

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the process definition.
    */
   void checkConsistency(List<Inconsistency> inconsistencies);

   IActivity createActivity(String id, String name, String description,
         int elementOID);

   Diagram createDiagram(String name);

   Diagram createDiagram(String name, int elementOID);

   ITrigger createTrigger(String id, String name, ITriggerType type, int elementOID);

   ITransition createTransition(String id, String name, String description,
         IActivity previous, IActivity next);

   ITransition createTransition(String id, String name, String description,
         IActivity previous, IActivity next, int elementOID);

   IActivity findActivity(String id);

   IDataPath findDescriptor(String id);

   ITransition findTransition(String id);

   ITrigger findTrigger(String id);

   /**
    * @deprecated Use of {@link #getActivities()} allows for more efficient iteration.
    */
   Iterator getAllActivities();

   ModelElementList<IActivity> getActivities();

   Iterator getAllDiagrams();

   /**
    * Retrieves all instances of this process.
    */
   Iterator getAllInstances();

   /**
    * @deprecated Use of {@link #getTransitions()} allows for more efficient iteration.
    */
   Iterator getAllTransitions();
   
   ModelElementList getTransitions();

   /**
    * @deprecated Use of {@link #getTriggers()} allows for more efficient iteration.
    */
   Iterator getAllTriggers();

   ModelElementList getTriggers();

   Iterator getAllDescriptors();
   
   IActivity getRootActivity();

   /**
    * Checks wether the model is consistent.
    */
   boolean isConsistent();

   void removeFromActivities(IActivity activity);

   void removeFromDiagrams(Diagram diagram);

   void removeFromTransitions(ITransition transition);

   void removeFromTriggers(ITrigger trigger);

   void removeFromDataPaths(IDataPath dataPath);

   IDataPath createDataPath(String id, String name, IData data, String path,
         Direction direction, int elementOID);

   /**
    * @deprecated Use of {@link #getDataPaths()} allows for more efficient iteration.
    */
   Iterator getAllDataPaths();

   ModelElementList getDataPaths();

   IDataPath findDataPath(String id, Direction direction);

   void addToDataPaths(IDataPath dataPath);

   String getDefaultActivityId();

   String getDefaultTransitionId();

   Iterator getAllOutDataPaths();

   Iterator getAllInDataPaths();

   void addToTriggers(ITrigger trigger);

   /**
    * @deprecated Use of {@link #getEventHandlers()} allows for more efficient iteration.
    */
   Iterator getAllEventHandlers(String type);

   boolean hasEventHandlers(String type);
   
   int getDefaultPriority();
   
   void setDefaultPriority(int priority);
   
   List<IFormalParameter> getFormalParameters();

   IFormalParameter findFormalParameter(String id);

   boolean getDeclaresInterface();

   IData getMappedData(String parameterId);

   String getMappedDataId(String parameterId);
   
   IReference getExternalReference();
}
