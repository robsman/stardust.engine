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
import java.util.Map;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.GroupSymbol;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ElementFactory extends XMLConstants
{
   void createQualityAssuranceAttributes(Node node, IActivity activity, IModel model);

   IQualityAssurance createQualityAssurance(Node node, IModel model);

   IQualityAssuranceCode createQualityAssuranceCode(Node node, IQualityAssurance qualityAssurance);

   IQualityAssuranceCode createQualityAssuranceCode(Node node, IActivity activity, IModel model);

   IActivity createActivity(Element node, IProcessDefinition process, IModel model, Map subprocessList);

   IApplication createApplication(Node node, IModel model);

   IApplicationContext createApplicationContext(Node node, IApplication application);

   AccessPoint createAccessPoint(Node node, AccessPointOwner holder);

   ITypeDeclaration createTypeDeclaration(Node node, IModel model, IXpdlType xpdlType);

   IApplicationType createApplicationType(Node node, IModel model);

   IData createData(Node node, IModel model);

   Diagram createDiagram(Node node, IProcessDefinition process);

   Diagram createDiagram(Node node, IModel model);

   IModeler createModeler(Node node, IModel model);

   ILinkType createLinkType(Node node, IModel model);

   ITrigger createTrigger(Node node, IProcessDefinition process);

   IParameterMapping createParameterMapping(Node mappingNode, ITrigger trigger);

   IModelParticipant createConditionalPerformer(Node node, IModel model);

   IOrganization createOrganization(Node node, IModel model);

   IProcessDefinition createProcess(Node node, IModel model);

   IRole createRole(Node node, IModel model);

   ITransition createTransition(Node node, IProcessDefinition process);

   IDataMapping createDataMapping(Node node, IActivity activity);

   IDataPath createDataPath(Node node, IProcessDefinition processDefinition);

   IViewable attachViewable(Node node, IModel model, IView view);

   IView createView(Node node, IModel model, IView parentView);

   ActivitySymbol createActivitySymbol(Node node, IProcessDefinition process, Diagram diagram);

   AnnotationSymbol createAnnotationSymbol(Node node, Diagram diagram);

   ApplicationSymbol createApplicationSymbol(Node node, IModel model, Diagram diagram);

   DataMappingConnection createDataMappingConnection(Node node,
         Diagram diagram, IProcessDefinition processDefinition);

   DataSymbol createDataSymbol(Node node, IModel model, Diagram diagram);

   GroupSymbol createGroupSymbol(Node node,
         Diagram diagram, IModel model, IProcessDefinition process, Collection children);

   ModelerSymbol createModelerSymbol(Node node, IModel model, Diagram diagram);

   OrganizationSymbol createOrganizationSymbol(Node node, IModel model, Diagram diagram);

   ConnectionSymbol attachConnection(Node node, ConnectionSymbol connection, Diagram diagram,
         String firstSymbolName, String secondSymbolName);

   ProcessDefinitionSymbol createProcessSymbol(Node node, IModel model, Diagram diagram);

   ConditionalPerformerSymbol createConditionalPerformerSymbol(Node node, IModel model, Diagram diagram);

   RoleSymbol createRoleSymbol(Node node, IModel model, Diagram diagram);

   IModel createModel(Element node);

   IModelParticipant attachParticipant(Node organizationNode, Node subNode, IModel model);

   IModelParticipant attachTeamLead(Node organizationNode, IModel model);

   IDataType createDataType(Node node, IModel model);

   IApplicationContextType createApplicationContextType(Node node, IModel model);

   ITriggerType createTriggerType(Node node, IModel model);

   IEventConditionType createEventConditionType(Node node, IModel model);

   IEventHandler createEventHandler(Node childNode, EventHandlerOwner owner);

   IEventActionType createEventActionType(Node node, IModel model);

   IEventAction createEventAction(Node node, IEventHandler handler);

   IBindAction createBindAction(Node node, IEventHandler handler);

   IUnbindAction createUnbindAction(Node node, IEventHandler handler);

   IExternalPackage createExternalPackage(Node externalPackage, IModel model);

   IReference createExternalReference(Node childNode, IModel model);

   IFormalParameter createFormalParameters(Node item, IProcessDefinition process);
}