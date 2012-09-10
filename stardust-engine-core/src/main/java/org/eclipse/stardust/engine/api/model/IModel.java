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
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.ColorKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.LineKey;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.RootElement;


/**
 * @author mgille
 */
public interface IModel extends SymbolTable, RootElement, ConfigurationVariableDefinitionProvider
{
   IQualityAssurance createQualityAssurance();
   
   IQualityAssurance getQualityAssurance();
   
   
   // @todo (france, ub): move to model node  -->

   // <---

   // @todo (france, ub): remove. has a dubios use case in hydra_test
   void addToProcessDefinitions(IProcessDefinition processDefinition);

   /**
    *
    */
   IModeler authentify(String id, String password);

   /**
    * Retrieves a vector with all inconsistencies of the model.
    */
   List checkConsistency();

   ITypeDeclaration createTypeDeclaration(String id, String name, String description,
         Map attributes, IXpdlType xpslType);

   /**
    *
    */
   IApplication createApplication(String id, String name, String description,
         int elementOID);

   /**
    *
    */
   IData createData(String id, IDataType type, String name, String description,
         boolean predefined, int elementOID, Map attributes);

   // @todo (france, ub): move to model node  -->
   /**
    *
    */
   Diagram createDiagram(String name);

   /**
    *
    */
   Diagram createDiagram(String name, int elementOID);

   // <---
   /**
    *
    */
   IModeler createModeler(String id, String name, String description,
         String password, int elementOID);

   /**
    *
    */
   IOrganization createOrganization(String id, String name, String description,
         int elementOID);

   /**
    *
    */
   IConditionalPerformer createConditionalPerformer(String id, String name,
         String description, IData data, int elementOID);

   /**
    *
    */
   IProcessDefinition createProcessDefinition(String id, String name, String description,
         boolean createDefaultDiagram, int elementOID);

   /**
    *
    */
   IProcessDefinition createProcessDefinition(String id, String name, String description);

   /**
    *
    */
   IRole createRole(String id, String name, String description, int elementOID);
   
   Scripting getScripting();
   
   void setScripting(Scripting scripting);

   List<IExternalPackage> getExternalPackages();

   IExternalPackage findExternalPackage(String id);

   ModelElementList/*<ITypeDeclaration>*/ getTypeDeclarations();

   ITypeDeclaration findTypeDeclaration(String id);

   /**
    *
    */
   IApplication findApplication(String id);

   /**
    *
    */
   IData findData(String id);

   /**
    *
    */
   Diagram findDiagram(String id);

   /**
    *
    */
   ILinkType findLinkType(String id);

   /**
    *
    */
   IModelParticipant findParticipant(String id);

   /**
    *
    */
   IProcessDefinition findProcessDefinition(String id);

   /**
    * @deprecated Use of {@link #getApplications()} allows for more efficient iteration.
    */
   Iterator getAllApplications();
   
   ModelElementList getApplications();

   /**
    *
    */
   int getApplicationsCount();

   /**
    * @deprecated Use of {@link #getData()} allows for more efficient iteration.
    */
   Iterator getAllData();
   
   ModelElementList<IData> getData();

   /**
    *
    */
   Iterator getAllDiagrams();

   /**
    *
    */
   int getDiagramsCount();

   /**
    * Returns all participants participating in workflow execution and modeling
    * that are organizations.
    *
    * @see #getAllParticipants
    */
   Iterator getAllOrganizations();

   /**
    * Returns all participants participating in workflow execution and modeling
    * that are roles.
    *
    * @see #getAllParticipants
    */
   Iterator getAllRoles();

   /**
    * @deprecated Use of {@link #getParticipants()} allows for more efficient iteration.
    */
   Iterator getAllParticipants();
   
   /**
    * Returns all participants participating in workflow execution and modeling.
    * Currently roles, organizations and modelers.
    *
    * @see #getAllParticipants
    */
   ModelElementList getParticipants();

   /**
    * Returns all participants participating in workflow execution.
    * Currently roles and organizations.
    *
    * @see #getAllOrganizations
    * @see #getAllParticipants
    */
   Iterator getAllWorkflowParticipants();

   /**
    * @deprecated Use of {@link #getProcessDefinitions()} allows for more efficient iteration.
    */
   Iterator getAllProcessDefinitions();
   
   ModelElementList<IProcessDefinition> getProcessDefinitions();

   /**
    *
    */
   int getProcessDefinitionsCount();

   /**
    *
    */
   Iterator getAllTopLevelParticipants();

   /**
    *
    */
   int getModelersCount();

   /**
    *
    */
   int getRolesCount();

   /**
    *
    */
   int getOrganizationsCount();

   /**
    *
    */
   int getConditionalPerformersCount();

   /**
    *
    */
   int getDataCount();

   /**
    * @return Unique ID for newly created applications.
    */
   String getDefaultApplicationId();

   /**
    * @return Unique ID for newly created data.
    */
   String getDefaultDataId();

   /**
    * @return Unique ID for newly created diagrams.
    */
   String getDefaultDiagramId();

   /**
    * @return Unique ID for newly created modelers.
    */
   String getDefaultModelerId();

   /**
    * @return Unique ID for newly created organizations.
    */
   String getDefaultOrganizationId();

   /**
    * @return Unique ID for newly created conditional resources.
    */
   String getDefaultConditionalPerformerId();

   /**
    * @return Unique ID for newly created process definitions.
    */
   String getDefaultProcessDefinitionId();

   /**
    * @return Unique ID for newly created roles.
    */
   String getDefaultRoleId();

   /**
    * @return Unique ID for newly created views.
    */
   String getDefaultViewId();

   /**
    *
    */
   String getDescription();

   /**
    *
    */
   void removeFromData(IData data);

   /**
    *
    */
   void removeFromDiagrams(Diagram diagram);

   /**
    *
    */
   void removeFromApplications(IApplication application);

   /**
    *
    */
   void removeFromParticipants(IModelParticipant participant);

   /**
    *
    */
   void removeFromProcessDefinitions(IProcessDefinition processDefinition);

   /**
    *
    */
   void setDescription(String description);

   /**
    *
    */
   void setId(String id);

   /**
    *
    */
   void setName(String name);

   /**
    * Retrieves all (predefined and user defined) link types for the model version
    */
   Iterator getAllLinkTypes();

   /**
    * Creates a user-defined link type for the model.
    */
   ILinkType createLinkType(String name
         , Class firstClass, Class secondClass
         , String firstRole, String secondRole
         , CardinalityKey firstCardinality, CardinalityKey secondCardinality
         , ArrowKey firstArrowType, ArrowKey secondArrowType
         , ColorKey lineColor
         , LineKey lineType
         , boolean showLinkTypeName
         , boolean showRoleNames
         , int elementOID);

   /**
    * Removes the link type <tt>linkType</tt>.
    * <p/>
    * Will throw an internal exception, if the link type is predefined.
    */
   void removeFromLinkTypes(ILinkType linkType);

   /**
    * Retrieves all link types whose first or second type is the class provided
    * by <tt>type</tt>.
    */
   Iterator getAllLinkTypesForType(Class type);

   IView createView(String name, String description, int elementOID);

   Iterator getAllViews();

   IApplicationType findApplicationType(String id);

   IApplicationType createApplicationType(String id, String name, boolean predefined,
         boolean synchronous, int elementOID);

   Iterator getAllApplicationTypes();

   void removeFromApplicationTypes(IApplicationType type);

   IDataType findDataType(String id);

   IDataType createDataType(String id, String name, boolean predefined,
         int elementOID);

   Iterator getAllDataTypes();

   void removeFromDataTypes(IDataType type);

   IApplicationContextType findApplicationContextType(String id);

   IApplicationContextType createApplicationContextType(String id, String name,
         boolean predefined, boolean hasMappingId, boolean hasApplicationPath,
         int elementOID);

   Iterator getAllApplicationContextTypes();

   void removeFromApplicationContextTypes(IApplicationContextType type);

   ITriggerType createTriggerType(String id, String name, boolean predefined,
         boolean pullTrigger, int elementOID);

   ITriggerType findTriggerType(String id);

   Iterator getAllTriggerTypes();

   Iterator getAllEventConditionTypes();

   IEventConditionType createEventConditionType(String id, String name,
         boolean predefined,
         EventType implementation, boolean processCondition, boolean activityCondition,
         int elementOID);

   IEventConditionType findEventConditionType(String id);

   IEventActionType findEventActionType(String id);

   Iterator getAllEventActionTypes();

   IEventActionType createEventActionType(String id, String name,
         boolean predefined, boolean processAction, boolean activityAction,
         int elementOID);

   Iterator getAllModelers();

   Iterator getAllConditionalPerformers();

   void setCarnotVersion(String version);

   Version getCarnotVersion();

   IProcessDefinition getImplementingProcess(QName processId);
   
   List<IProcessDefinition> getAllImplementingProcesses(QName processId);
   
   Set<QName> getImplementedInterfaces();
}
