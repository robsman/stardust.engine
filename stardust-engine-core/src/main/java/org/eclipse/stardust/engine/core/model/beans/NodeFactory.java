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

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.GroupSymbol;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.w3c.dom.Node;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface NodeFactory extends XMLConstants
{
   Node createDiagramElement(Diagram diagram);

   Node createGroupSymbolElement(GroupSymbol symbol);

   Node createSubProcessOfConnectionElement(SubProcessOfConnection connection);

   Node createParameterMappingElement(IParameterMapping mapping);

   Node createActivityElement(IActivity activity);

   Node createApplicationElement(IApplication application);

   Node createAccessPointElement(AccessPoint accessPoint);

   Node createTriggerElement(ITrigger trigger);

   Node createDataElement(IData data);

   Node createActivitySymbolElement(ActivitySymbol symbol);

   Node createAnnotationSymbolElement(AnnotationSymbol symbol);

   Node createApplicationSymbolElement(ApplicationSymbol symbol);

   Node createDataMappingConnectionElement(DataMappingConnection connection);

   Node createDataSymbolElement(DataSymbol symbol);

   Node createExecutedByConnectionElement(ExecutedByConnection connection);

   Node createGenericLinkConnectionElement(GenericLinkConnection connection);

   Node createModelerSymbolElement(ModelerSymbol symbol);

   Node createConditionalPerformerSymbolElement(ConditionalPerformerSymbol symbol);

   Node createOrganizationSymbolElement(OrganizationSymbol symbol);

   Node createPartOfConnectionElement(PartOfConnection connection);

   Node createPerformConnectionElement(PerformsConnection connection);

   Node createProcessDefinitionSymbolElement(ProcessDefinitionSymbol symbol);

   Node createRefersToConnectionElement(RefersToConnection connection);

   Node createRoleSymbolElement(RoleSymbol symbol);

   Node createTransitionConnectionElement(TransitionConnection connection);

   Node createWorksForConnectionElement(WorksForConnection connection);

   Node createModelerElement(IModeler modeler);

   Node createLinkTypeElement(ILinkType linkType);

   Node createConditionalPerformerElement(IConditionalPerformer performer);

   Node createModelElement(IModel model);

   Node createApplicationTypeElement(IApplicationType type);

   Node createDataTypeElement(IDataType type);

   Node createOrganizationElement(IOrganization organization);

   Node createProcessDefinitionElement(IProcessDefinition process);

   Node createRoleElement(IRole role);

   Node createTransitionElement(ITransition transition);

   Node createDataPathElement(IDataPath dataPath);

   Node createDataMappingElement(IDataMapping dataMapping);

   Node createViewElement(IView view);

   Node attachViewableElement(IViewable viewable);

   Node createApplicationContextElement(IApplicationContext context);

   Node attachParticipantElement(IModelParticipant participant);

   Node createApplicationContextTypeElement(IApplicationContextType type);

   Node createTriggerTypeElement(ITriggerType type);

   Node createEventHandlerElement(IEventHandler handler);

   Node createEventConditionTypeElement(IEventConditionType type);

   Node createEventActionTypeElement(IEventActionType type);

   Node createEventActionElement(IEventAction action);

   Node createUnbindActionElement(IUnbindAction action);

   Node createBindActionElement(IBindAction action);

   Node createScripting(Scripting scripting);
}
