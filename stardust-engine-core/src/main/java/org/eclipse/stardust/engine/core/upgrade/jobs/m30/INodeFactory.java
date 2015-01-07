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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import org.w3c.dom.Node;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface INodeFactory
{
   Node createDiagramElement(Diagram diagram);

   Node createGroupSymbolElement(GroupSymbol symbol);

   Node createSubProcessOfConnectionElement(Connection connection);

   Node createParameterMappingElement(ParameterMapping mapping);

   Node createActivityElement(Activity activity);

   Node createApplicationElement(Application application);

   Node createAccessPointElement(AccessPoint accessPoint);

   Node createTriggerElement(Trigger trigger);

   Node createDataElement(Data data);

   Node createActivitySymbolElement(ActivitySymbol symbol);

   Node createAnnotationSymbolElement(AnnotationSymbol symbol);

   Node createApplicationSymbolElement(ApplicationSymbol symbol);

   Node createDataMappingConnectionElement(DataMappingConnection connection);

   Node createDataSymbolElement(DataSymbol symbol);

   Node createExecutedByConnectionElement(Connection connection);

   Node createGenericLinkConnectionElement(GenericLinkConnection connection);

   Node createModelerSymbolElement(ModelerSymbol symbol);

   Node createConditionalPerformerSymbolElement(ConditionalPerformerSymbol symbol);

   Node createOrganizationSymbolElement(OrganizationSymbol symbol);

   Node createPartOfConnectionElement(Connection connection);

   Node createPerformsConnectionElement(Connection connection);

   Node createProcessDefinitionSymbolElement(ProcessDefinitionSymbol symbol);

   Node createRefersToConnectionElement(Connection connection);

   Node createRoleSymbolElement(RoleSymbol symbol);

   Node createTransitionConnectionElement(TransitionConnection connection);

   Node createWorksForConnectionElement(Connection connection);

   Node createModelerElement(Modeler modeler);

   Node createLinkTypeElement(LinkType linkType);

   Node createConditionalPerformerElement(ConditionalPerformer performer);

   Node createModelElement(Model model);

   Node createApplicationTypeElement(ApplicationType type);

   Node createDataTypeElement(DataType type);

   Node createOrganizationElement(Organization organization);

   Node createProcessDefinitionElement(ProcessDefinition process);

   Node createRoleElement(Role role);

   Node createTransitionElement(Transition transition);

   Node createDataPathElement(DataPath dataPath);

   Node createDataMappingElement(DataMapping dataMapping);

   Node createViewElement(View view);

   Node attachViewableElement(ModelElement viewable);

   Node createApplicationContextElement(ApplicationContext context);

   Node attachAssociatedParticipantElement(String participant);

   Node createApplicationContextTypeElement(ApplicationContextType type);

   Node createTriggerTypeElement(TriggerType type);

   Node createEventConditionTypeElement(EventConditionType type);

   Node createEventActionTypeElement(EventActionType type);

   Node createEventHandlerElement(EventHandler eventHandler);

   Node createEventActionElement(EventAction action);

}
