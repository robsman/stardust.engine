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

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.GroupSymbol;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class NodeFactoryImpl implements NodeFactory
{
   private static final Logger trace = LogManager.getLogger(NodeFactoryImpl.class);

   private Document document;

   public NodeFactoryImpl(Document document)
   {
      this.document = document;
   }

   public Node createDiagramElement(Diagram diagram)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DIAGRAM);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(NAME_ATT, diagram.getName());

      return node;
   }

   public Node createGroupSymbolElement(GroupSymbol symbol)
   {
      return document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, GROUP_SYMBOL);
   }

   public Node createSubProcessOfConnectionElement(SubProcessOfConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, SUBPROCESS_OF_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SUB_PROCESS_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(PROCESS_SYMBOL_ID,
            connection.getSecondSymbol().getElementOID());

      return node;
   }

   public Node createParameterMappingElement(IParameterMapping mapping)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, PARAMETER_MAPPING);

      if (mapping.getData() == null)
      {
         warn(ConversionWarning.MEDIUM, "The target data is null", null, mapping);
      }
      if ((mapping.getParameterId() == null))
      {
         warn(ConversionWarning.MEDIUM, "The source parameter is null", null, mapping);
      }

      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(DATA_REF_ATT, mapping.getData().getId());
      writer.writeAttribute(PARAMETER, mapping.getParameterId());
      writer.writeAttribute(PARAMETER_PATH, mapping.getParameterPath());

      return node;
   }

   public Node createActivityElement(IActivity activity)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ACTIVITY);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(IMPLEMENTATION_ATT, activity.getImplementationType());

      writer.writeReference(APPLICATION_REF_ATT, activity.getApplication());

      writer.writeAttribute(JOIN_ATT, activity.getJoinType());
      writer.writeAttribute(SPLIT, activity.getSplitType());
      writer.writeAttribute(LOOP_CONDITION_ATT, activity.getLoopCondition());
      writer.writeAttribute(LOOP_TYPE, activity.getLoopType());
      writer.writeAttribute(ALLOWS_ABORT_BY_PERFORMER_ATT,
            activity.getAllowsAbortByPerformer());
      writer.writeAttribute(HIBERNATE_ON_CREATION, activity.isHibernateOnCreation());
      writer.writeReference(PERFORMER, activity.getPerformer());

      writer.writeReference(PROCESS_ID_REF,
            activity.getImplementationProcessDefinition());

      if (ImplementationType.SubProcess.equals(activity.getImplementationType()))
      {
         writer.writeAttribute(SUB_PROCESS_MODE,
               activity.getSubProcessMode().getId());
      }

      return node;
   }

   public Node createApplicationElement(IApplication application)
   {
      Element applicationElement = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, APPLICATION);
      NodeWriter writer = new NodeWriter(applicationElement);

      if (application.isInteractive())
      {
         writer.writeAttribute(INTERACTIVE_ATT, "true");
      }
      else
      {
         writer.writeReference(TYPE_ATT, (ModelElement) application.getType());
      }

      return applicationElement;
   }

   public Node createAccessPointElement(AccessPoint accessPoint)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ACCESS_POINT);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(TYPE_ATT, (IDataType) accessPoint.getType());
      writer.writeAttribute(DIRECTION_ATT, accessPoint.getDirection());

      return node;
   }

   public Node createTriggerElement(ITrigger trigger)
   {
      Element triggerElement = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, TRIGGER);
      NodeWriter writer = new NodeWriter(triggerElement);

      writer.writeAttribute(TYPE_ATT, trigger.getType().getId());
      writer.writeAttribute(NAME_ATT, trigger.getName());

      return triggerElement;
   }

   public Node createDataElement(IData data)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DATA);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(TYPE_ATT, data.getType().getId());

      return node;
   }

   public Node createActivitySymbolElement(ActivitySymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ACTIVITY_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      if (symbol.getActivity() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getActivity().getId());
      }

      return node;
   }

   public Node createAnnotationSymbolElement(AnnotationSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ANNOTATION_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());

      writer.appendChildElement(TEXT, symbol.getText());

      return node;
   }

   public Node createApplicationSymbolElement(ApplicationSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, APPLICATION_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      if (symbol.getApplication() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getApplication().getId());
      }

      return node;
   }

   public Node createDataMappingConnectionElement(DataMappingConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DATA_MAPPING_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      if (connection.getFirstSymbol() instanceof DataSymbol)
      {
         writer.writeAttribute(DATA_SYMBOL_ID,
               connection.getFirstSymbol().getElementOID());
         writer.writeAttribute(ACTIVITY_SYMBOL_REF,
               connection.getSecondSymbol().getElementOID());
      }
      else
      {
         writer.writeAttribute(DATA_SYMBOL_ID,
               connection.getSecondSymbol().getElementOID());
         writer.writeAttribute(ACTIVITY_SYMBOL_REF,
               connection.getFirstSymbol().getElementOID());
      }

      return node;
   }

   public Node createDataSymbolElement(DataSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DATA_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());

      if (symbol.getData() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getData().getId());
      }

      return node;
   }

   public Node createExecutedByConnectionElement(ExecutedByConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, EXECUTED_BY_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(APPLICATION_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(ACTIVITY_SYMBOL_REF,
            connection.getSecondSymbol().getElementOID());

      return node;
   }

   public Node createGenericLinkConnectionElement(GenericLinkConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, GENERIC_LINK_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SOURCE_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(TARGET_SYMBOL_ID,
            connection.getSecondSymbol().getElementOID());

      if (connection.getLinkType() != null)
      {
         writer.writeAttribute(LINK_TYPE_REF, connection.getLinkType().getName());
      }

      return node;
   }

   public Node createModelerSymbolElement(ModelerSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, MODELER_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());

      if (symbol.getModeler() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getModeler().getId());
      }

      return node;
   }

   public Node createConditionalPerformerSymbolElement(ConditionalPerformerSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, CONDITIONAL_PERFORMER_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      if (symbol.getPerformer() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getPerformer().getId());
      }

      return node;
   }

   public Node createOrganizationSymbolElement(OrganizationSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ORGANIZATION_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      if (symbol.getOrganization() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getOrganization().getId());
      }
      return node;
   }

   public Node createPartOfConnectionElement(PartOfConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, PART_OF_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SUB_ORGANIZATION_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(ORGANIZATION_SYMBOL_ID,
            connection.getSecondSymbol().getElementOID());

      return node;
   }

   public Node createPerformConnectionElement(PerformsConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, PERFORMS_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(PARTICIPANT_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(ACTIVITY_SYMBOL_REF,
            connection.getSecondSymbol().getElementOID());

      return node;
   }

   public Node createProcessDefinitionSymbolElement(ProcessDefinitionSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, PROCESS_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      if (symbol.getProcessDefinition() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getProcessDefinition().getId());
      }

      return node;
   }

   public Node createRefersToConnectionElement(RefersToConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, REFERS_TO_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(FROM_ATT, connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(TO, connection.getSecondSymbol().getElementOID());

      return node;
   }

   public Node createRoleSymbolElement(RoleSymbol symbol)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ROLE_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      if (symbol.getRole() != null)
      {
         writer.writeAttribute(USEROBJECT, symbol.getRole().getId());
      }

      return node;
   }

   public Node createTransitionConnectionElement(TransitionConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, TRANSITION_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SOURCE_ACTIVITY_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(TARGET_ACTIVITY_SYMBOL_ID,
            connection.getSecondSymbol().getElementOID());
      writer.writeAttribute(TRANSITION_REF, connection.getTransition().getId());

      List points = connection.getPath().getPoints();

      StringBuffer buffer = new StringBuffer();

      for (int n = 0; n < points.size(); ++n)
      {
         if (n != 0)
         {
            buffer.append(", ");
         }

         buffer.append(((Integer) points.get(n)).intValue());
      }

      writer.writeAttribute(POINTS, buffer.toString());

      return node;
   }

   public Node createWorksForConnectionElement(WorksForConnection connection)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, WORKS_FOR_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(PARTICIPANT_SYMBOL_ID,
            connection.getFirstSymbol().getElementOID());
      writer.writeAttribute(ORGANIZATION_SYMBOL_ID,
            connection.getSecondSymbol().getElementOID());

      return node;
   }

   public Node createModelerElement(IModeler modeler)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, MODELER);
      NodeWriter writer = new NodeWriter(node);

      // @todo (france, ub): scramble password
      writer.writeAttribute(PASSWORD, modeler.getPassword());

      // @todo ... write eMail (from dialog) -> missing attribut in modeler

      return node;
   }

   public Node createLinkTypeElement(ILinkType linkType)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, LINK_TYPE);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SOURCE_CLASS, linkType.getFirstClass().getName());
      writer.writeAttribute(SOURCE_CARDINALITY, linkType.getFirstCardinality());
      writer.writeAttribute(SOURCE_ROLE, linkType.getFirstRole());
      writer.writeAttribute(SOURCE_SYMBOL, linkType.getFirstArrowType());

      writer.writeAttribute(TARGET_CLASS, linkType.getSecondClass().getName());
      writer.writeAttribute(TARGET_CARDINALITY, linkType.getSecondCardinality());
      writer.writeAttribute(TARGET_ROLE, linkType.getSecondRole());
      writer.writeAttribute(TARGET_SYMBOL, linkType.getSecondArrowType());

      writer.writeAttribute(LINE_COLOR_ATT, linkType.getLineColor());
      writer.writeAttribute(LINE_TYPE_ATT, linkType.getLineType());
      writer.writeAttribute(SHOW_LINKTYPE_NAME, linkType.getShowLinkTypeName());
      writer.writeAttribute(SHOW_ROLE_NAMES, linkType.getShowRoleNames());

      return node;
   }

   public Node createConditionalPerformerElement(IConditionalPerformer performer)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, CONDITIONAL_PERFORMER);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(DATA_REF_ATT, performer.getData());
      writer.writeAttribute(DATA_PATH_ATT, performer.getDereferencePath());
      writer.writeAttribute(IS_USER_ATT, performer.isUser());

      return node;
   }

   public Node createModelElement(IModel model)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, MODEL);
      node.setAttribute(XMLNS_ATTR, NS_CARNOT_WORKFLOWMODEL_31);
      node.setAttribute(XMLNS_XPDL_ATTR, NS_XPDL_2_1);

      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(MODEL_OID, model.getModelOID());
      writer.writeAttribute(VENDOR, VENDOR_NAME);
      writer.writeAttribute(CREATED, TimestampProviderUtils.getTimeStamp());

      try
      {
         writer.writeAttribute(AUTHOR_ATT, System.getProperty(USER_NAME, "unknown"));
      }
      catch (Exception e)
      {
      }

      writer.writeAttribute(CARNOT_VERSION_ATT, CurrentVersion.getVersionName());
      
      return node;
   }

   public Node createApplicationTypeElement(IApplicationType type)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, APPLICATION_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(SYNCHRONOUS_ATT, type.isSynchronous());
      return node;
   }

   public Node createDataTypeElement(IDataType type)
   {
      return document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DATA_TYPE);
   }

   public Node createOrganizationElement(IOrganization organization)
   {
      return document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ORGANIZATION);
   }

   public Node createProcessDefinitionElement(IProcessDefinition process)
   {
      return document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, PROCESS);
   }

   public Node createRoleElement(IRole role)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, ROLE);
      NodeWriter writer = new NodeWriter(node);

      if (role.getCardinality() != Unknown.INT)
      {
         writer.writeAttribute(CARDINALITY_ATT, role.getCardinality());
      }

      // Write all the other stuff
      // @todo (?) ... cardinality (from diagram) -> missing attribute in role

      return node;
   }

   public Node createTransitionElement(ITransition transition)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, TRANSITION);
      NodeWriter writer = new NodeWriter(node);

      IActivity fromActivity = transition.getFromActivity();
      IActivity toActivity = transition.getToActivity();
      if (fromActivity == null)
      {
         warn(ConversionWarning.MEDIUM, "Transition has no target.",
               null, transition);
      }
      else
      {
         writer.writeAttribute(FROM_ATT, fromActivity.getId());
      }
      if (toActivity == null)
      {
         warn(ConversionWarning.MEDIUM, "Transition has no source.", null, transition);
      }
      else
      {
         writer.writeAttribute(TO, toActivity.getId());
      }
      writer.writeAttribute(FORK_ON_TRAVERSAL_ATT, transition.getForkOnTraversal());

      String condition = transition.getCondition();
      if ("OTHERWISE".equals(condition))
      {
         writer.writeAttribute(CONDITION, condition);
      }
      else
      {
         writer.writeAttribute(CONDITION, "CONDITION");
         if ("TRUE".equals(condition))
         {
            writer.appendChildElement(EXPRESSION, "true");            
         }
         if ("FALSE".equals(condition))
         {            
            writer.appendChildElement(EXPRESSION, "false");
         }
         else
         {
            writer.appendChildElement(EXPRESSION, condition);            
         }
      }

      return node;
   }

   public Node createDataPathElement(IDataPath dataPath)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DATA_PATH);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(DATA_REF_ATT, dataPath.getData());
      writer.writeAttribute(DATA_PATH_ATT, dataPath.getAccessPath());
      writer.writeAttribute(DIRECTION_ATT, dataPath.getDirection());
      writer.writeAttribute(DESCRIPTOR_ATT, dataPath.isDescriptor());

      return node;
   }

   public Node createDataMappingElement(IDataMapping dataMapping)
   {
      // @todo (france, ub): make an automatic and an interactive case
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, DATA_MAPPING);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(ID_ATT, dataMapping.getId());
      writer.writeAttribute(DIRECTION_ATT, dataMapping.getDirection());
      writer.writeAttribute(DATA_REF_ATT, dataMapping.getData().getId());
      writer.writeAttribute(CONTEXT_ATT, dataMapping.getContext());
      writer.writeAttribute(APPLICATION_ACCESS_POINT_ATT,
            dataMapping.getActivityAccessPointId());
      writer.writeAttribute(DATA_PATH_ATT, dataMapping.getDataPath());
      writer.writeAttribute(APPLICATION_PATH_ATT, dataMapping.getActivityPath());

      return node;
   }

   public Node createViewElement(IView view)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, VIEW);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(NAME_ATT, view.getName());

      return node;
   }

   public Node attachViewableElement(IViewable viewable)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, VIEWABLE);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(VIEWABLE_ATT, viewable.getElementOID());

      return node;
   }

   private void warn(int severity, String message, Exception exception, ModelElement scope)
   {
      if (exception != null)
      {
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message, exception);
      }
      else
      {
         trace.debug("", new Exception());
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message);
      }
   }

   public Node createApplicationContextElement(IApplicationContext context)
   {
      Element contextElement = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, CONTEXT);
      NodeWriter writer = new NodeWriter(contextElement);
      writer.writeReference(TYPE_ATT, (ModelElement) context.getType());
      return contextElement;
   }

   public Node attachParticipantElement(IModelParticipant participant)
   {
      Element result = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, PARTICIPANT);
      result.setAttribute(PARTICIPANT_ATT, participant.getId());
      return result;
   }

   public Node createApplicationContextTypeElement(IApplicationContextType type)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, APPLICATION_CONTEXT_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(HAS_MAPPING_ID_ATT, type.hasMappingId());
      writer.writeAttribute(HAS_APPLICATION_PATH_ATT, type.hasApplicationPath());
      return node;
   }

   public Node createTriggerTypeElement(ITriggerType type)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, TRIGGER_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(PULL_TRIGGER_ATT, type.isPullTrigger());
      return node;
   }

   public Node createEventHandlerElement(IEventHandler handler)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, EVENT_HANDLER);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(TYPE_ATT, (ModelElement) handler.getType());
      writer.writeAttribute(AUTO_BIND_ATT, handler.isAutoBind());
      writer.writeAttribute(UNBIND_ON_MATCH_ATT, handler.isUnbindOnMatch());
      writer.writeAttribute(LOG_HANDLER_ATT, handler.isLogHandler());
      writer.writeAttribute(CONSUME_ON_MATCH_ATT, handler.isConsumeOnMatch());

      return node;
   }

   public Node createEventConditionTypeElement(IEventConditionType type)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, EVENT_CONDITION_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(IMPLEMENTATION_ATT, type.getImplementation());
      writer.writeAttribute(PROCESS_CONDITION_ATT, type.hasProcessInstanceScope());
      writer.writeAttribute(ACTIVITY_CONDITION_ATT, type.hasActivityInstanceScope());
      return node;
   }

   public Node createEventActionTypeElement(IEventActionType type)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, EVENT_ACTION_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(ACTIVITY_ACTION_ATT, type.isActivityAction());
      writer.writeAttribute(PROCESS_ACTION_ATT, type.isProcessAction());
      writer.writeAttribute(SUPPORTED_CONDITION_TYPES_ATT, type.getSupportedConditionTypes());
      writer.writeAttribute(UNSUPPORTED_CONTEXTS_ATT, type.getUnsupportedContexts());
      return node;
   }

   public Node createEventActionElement(IEventAction action)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, EVENT_ACTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(TYPE_ATT, (IEventActionType) action.getType());

      return node;
   }

   public Node createBindActionElement(IBindAction action)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, BIND_ACTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(TYPE_ATT, (IEventActionType) action.getType());

      return node;
   }

   public Node createUnbindActionElement(IUnbindAction action)
   {
      Element node = document.createElementNS(NS_CARNOT_WORKFLOWMODEL_31, UNBIND_ACTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeReference(TYPE_ATT, (IEventActionType) action.getType());

      return node;
   }

   public Node createScripting(Scripting scripting)
   {
      if (scripting != null)
      {
         Element node = document.createElementNS(NS_XPDL_2_1, XPDL + ":" + XPDL_SCRIPT);
         NodeWriter writer = new NodeWriter(node);
         writer.writeAttribute(XPDL_SCRIPT_TYPE_ATT, scripting.getType());
         writer.writeAttribute(XPDL_SCRIPT_VERSION_ATT, scripting.getVersion());
         writer.writeAttribute(XPDL_SCRIPT_GRAMMAR_ATT, scripting.getGrammar());
         return node;
      }
      return null;
   }
}