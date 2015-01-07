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

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class NodeFactory implements XMLConstants, INodeFactory
      // @todo (france, ub): switch this last minute to " implements NewConstants"
      // replace XMLConstants in the whole package!!!
{
   public static final Logger trace = LogManager.getLogger(NodeFactory.class);

   private Document document;

   public NodeFactory(Document document)
   {
      this.document = document;
   }

   public Node createDiagramElement(Diagram diagram)
   {
      Element node = document.createElement(DIAGRAM);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(NAME_ATT, diagram.getName());

      return node;
   }

   public Node createGroupSymbolElement(GroupSymbol symbol)
   {
      return document.createElement(GROUP_SYMBOL);
   }

   public Node createSubProcessOfConnectionElement(Connection connection)
   {
      Element node = document.createElement(SUBPROCESS_OF_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SUB_PROCESS_SYMBOL_ID,
            connection.getFirstOID());
      writer.writeAttribute(PROCESS_SYMBOL_ID,
            connection.getSecondOID());

      return node;
   }

   public Node createParameterMappingElement(ParameterMapping mapping)
   {
      Element node = document.createElement(PARAMETER_MAPPING);

      if (mapping.getDataId() == null)
      {
         warn("The target data is null", null, mapping);
      }
      if ((mapping.getParameterId() == null))
      {
         warn("The source parameter is null", null, mapping);
      }

      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(DATA_REF_ATT, mapping.getDataId());
      writer.writeAttribute(PARAMETER, mapping.getParameterId());
      String path = mapping.getParameterPath();
      writer.writeAttribute(PARAMETER_PATH, (null != path) ? path.toString() : "");

      return node;
   }

   public Node createActivityElement(Activity activity)
   {
      Element node = document.createElement(ACTIVITY);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(IMPLEMENTATION_ATT, activity.getImplementationType());

      if (!StringUtils.isEmpty(activity.getApplicationId()))
      {
         writer.writeAttribute(APPLICATION_REF_ATT, activity.getApplicationId());
      }

      writer.writeAttribute(HIBERNATE_ON_CREATION, false);
      writer.writeAttribute(JOIN_ATT, activity.getJoinType());
      writer.writeAttribute(SPLIT, activity.getSplitType());
      writer.writeAttribute(LOOP_CONDITION_ATT, activity.getLoopCondition());
      writer.writeAttribute(LOOP_TYPE, activity.getLoopType());
      writer.writeAttribute(ALLOWS_ABORT_BY_PERFORMER_ATT,
            activity.isAllowsAbortByPerformer());
      if (!StringUtils.isEmpty(activity.getPerformerID()))
      {
         writer.writeAttribute(PERFORMER, activity.getPerformerID());
      }

      if (!StringUtils.isEmpty(activity.getSubProcessID()))
      {
         writer.writeAttribute(PROCESS_ID_REF, activity.getSubProcessID());
      }
      if (!StringUtils.isEmpty(activity.getSubProcessMode()))
      {
         writer.writeAttribute(SUB_PROCESS_MODE, activity.getSubProcessMode());
      }

      return node;
   }

   public Node createApplicationElement(Application application)
   {
      Element applicationElement = document.createElement(APPLICATION);
      NodeWriter writer = new NodeWriter(applicationElement);

      if (application.isInteractive())
      {
         writer.writeAttribute(INTERACTIVE_ATT, "true");
      }
      else
      {
         writer.writeAttribute(TYPE_ATT, application.getApplicationTypeId());
      }

      return applicationElement;
   }

   public Node createAccessPointElement(AccessPoint accessPoint)
   {
      Element node = document.createElement(ACCESS_POINT);
      NodeWriter writer = new NodeWriter(node);

      if (accessPoint.getType() != null)
      {
         writer.writeAttribute(TYPE_ATT, accessPoint.getType());
      }
      else
      {
         warn("Type attribute is missing.", null, accessPoint);
      }
      writer.writeAttribute(DIRECTION_ATT, accessPoint.getDirection());
      // @todo (france, ub): how is default value working with 3.0???

      // @todo (france, ub): browsable:


      return node;
   }

   public Node createTriggerElement(Trigger trigger)
   {
      Element triggerElement = document.createElement(TRIGGER);
      NodeWriter writer = new NodeWriter(triggerElement);

      writer.writeAttribute(TYPE_ATT, trigger.getType());

      return triggerElement;
   }

   public Node createDataElement(Data data)
   {
      Element node = document.createElement(DATA);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(TYPE_ATT, data.getType());

      return node;
   }

   public Node createActivitySymbolElement(ActivitySymbol symbol)
   {
      Element node = document.createElement(ACTIVITY_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getActivityId());

      return node;
   }

   public Node createAnnotationSymbolElement(AnnotationSymbol symbol)
   {
      Element node = document.createElement(ANNOTATION_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());

      writer.appendChildElement(TEXT, symbol.getText());

      return node;
   }

   public Node createApplicationSymbolElement(ApplicationSymbol symbol)
   {
      Element node = document.createElement(APPLICATION_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getApplication());

      return node;
   }

   public Node createDataMappingConnectionElement(DataMappingConnection connection)
   {
      Element node = document.createElement(DATA_MAPPING_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(DATA_SYMBOL_ID,
            connection.getFirstOID());
      writer.writeAttribute(ACTIVITY_SYMBOL_REF,
            connection.getSecondOID());

      return node;
   }

   public Node createDataSymbolElement(DataSymbol symbol)
   {
      Element node = document.createElement(DATA_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getDataId());

      return node;
   }

   public Node createExecutedByConnectionElement(Connection connection)
   {
      // @todo (france, ub): !!
      Element node = document.createElement(EXECUTED_BY_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(APPLICATION_SYMBOL_ID, connection.getFirstOID());
      writer.writeAttribute(ACTIVITY_SYMBOL_REF, connection.getSecondOID());

      return node;
   }

   public Node createGenericLinkConnectionElement(GenericLinkConnection connection)
   {
      Element node = document.createElement(GENERIC_LINK_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SOURCE_SYMBOL_ID, connection.getFirstOID());
      writer.writeAttribute(TARGET_SYMBOL_ID, connection.getSecondOID());

      writer.writeAttribute(LINK_TYPE_REF, connection.getLinkTypeId());

      return node;
   }

   public Node createModelerSymbolElement(ModelerSymbol symbol)
   {
      Element node = document.createElement(MODELER_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());

      writer.writeAttribute(USEROBJECT, symbol.getModeler());

      return node;
   }

   public Node createConditionalPerformerSymbolElement(ConditionalPerformerSymbol symbol)
   {
      Element node = document.createElement(CONDITIONAL_PERFORMER_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getPerformer());

      return node;
   }

   public Node createOrganizationSymbolElement(OrganizationSymbol symbol)
   {
      Element node = document.createElement(ORGANIZATION_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getOrganization());
      return node;
   }

   public Node createPartOfConnectionElement(Connection connection)
   {
      Element node = document.createElement(PART_OF_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SUB_ORGANIZATION_SYMBOL_ID, connection.getFirstOID());
      writer.writeAttribute(ORGANIZATION_SYMBOL_ID, connection.getSecondOID());

      return node;
   }

   public Node createPerformsConnectionElement(Connection connection)
   {
      Element node = document.createElement(PERFORMS_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(PARTICIPANT_SYMBOL_ID, connection.getFirstOID());
      writer.writeAttribute(ACTIVITY_SYMBOL_REF, connection.getSecondOID());

      return node;
   }

   public Node createProcessDefinitionSymbolElement(ProcessDefinitionSymbol symbol)
   {
      Element node = document.createElement(PROCESS_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getProcess());

      return node;
   }

   public Node createRefersToConnectionElement(Connection connection)
   {
      Element node = document.createElement(REFERS_TO_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(FROM_ATT, connection.getFirstOID());
      writer.writeAttribute(TO, connection.getSecondOID());

      return node;
   }

   public Node createRoleSymbolElement(RoleSymbol symbol)
   {
      Element node = document.createElement(ROLE_SYMBOL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(X, symbol.getX());
      writer.writeAttribute(Y, symbol.getY());
      writer.writeAttribute(USEROBJECT, symbol.getRole());

      return node;
   }

   public Node createTransitionConnectionElement(TransitionConnection connection)
   {
      Element node = document.createElement(TRANSITION_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SOURCE_ACTIVITY_SYMBOL_ID, connection.getFirstOID());
      writer.writeAttribute(TARGET_ACTIVITY_SYMBOL_ID, connection.getSecondOID());
      writer.writeAttribute(TRANSITION_REF, connection.getTransition());

      List points = connection.getPoints();

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

   public Node createWorksForConnectionElement(Connection connection)
   {
      Element node = document.createElement(WORKS_FOR_CONNECTION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(PARTICIPANT_SYMBOL_ID, connection.getFirstOID());
      writer.writeAttribute(ORGANIZATION_SYMBOL_ID, connection.getSecondOID());

      return node;
   }

   public Node createModelerElement(Modeler modeler)
   {
      Element node = document.createElement(MODELER);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(PASSWORD, modeler.getPassword());

      return node;
   }

   public Node createLinkTypeElement(LinkType linkType)
   {
      Element node = document.createElement(LINK_TYPE);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(SOURCE_CLASS, linkType.getSourceClassName());
      writer.writeAttribute(SOURCE_CARDINALITY, linkType.getSourceCardinality());
      writer.writeAttribute(SOURCE_ROLE, linkType.getSourceRoleName());
      writer.writeAttribute(SOURCE_SYMBOL, linkType.getSourceSymol());

      writer.writeAttribute(TARGET_CLASS, linkType.getTargetClassName());
      writer.writeAttribute(TARGET_CARDINALITY, linkType.getTargetCardinality());
      writer.writeAttribute(TARGET_ROLE, linkType.getTargetRoleName());
      writer.writeAttribute(TARGET_SYMBOL, linkType.getTargetSymbol());

      writer.writeAttribute(LINE_COLOR_ATT, linkType.getLineColor());
      writer.writeAttribute(LINE_TYPE_ATT, linkType.getLineType());
      writer.writeAttribute(SHOW_LINKTYPE_NAME, linkType.isShowLinkTypeName());
      writer.writeAttribute(SHOW_ROLE_NAMES, linkType.isShowRoleNames());

      return node;
   }

   public Node createConditionalPerformerElement(ConditionalPerformer performer)
   {
      Element node = document.createElement(CONDITIONAL_PERFORMER);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(DATA_REF_ATT, performer.getDataId());
      writer.writeAttribute(DATA_PATH_ATT, "");
      writer.writeAttribute(IS_USER_ATT, performer.isUser());

      return node;
   }

   public Node createModelElement(Model model)
   {
      Element node = document.createElement(MODEL);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(VENDOR, VENDOR_NAME);
      writer.writeAttribute(CREATED, new Date());
      writer.writeAttribute(AUTHOR_ATT, System.getProperty(USER_NAME, "unknown"));
      writer.writeAttribute(CARNOT_VERSION_ATT, CurrentVersion.getVersionName());
      writer.writeAttribute(MODEL_OID, model.getModelOid());

      return node;
   }

   public Node createApplicationTypeElement(ApplicationType type)
   {
      Element node = document.createElement(APPLICATION_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(SYNCHRONOUS_ATT, type.isSynchronous());
      return node;
   }

   public Node createDataTypeElement(DataType type)
   {
      return document.createElement(DATA_TYPE);
   }

   public Node createOrganizationElement(Organization organization)
   {
      return document.createElement(ORGANIZATION);
   }

   public Node createProcessDefinitionElement(ProcessDefinition process)
   {
      return document.createElement(PROCESS);
   }

   public Node createRoleElement(Role role)
   {
      Element node = document.createElement(ROLE);
      NodeWriter writer = new NodeWriter(node);

      if (role.getCardinality() != Unknown.INT)
      {
         writer.writeAttribute(CARDINALITY_ATT, role.getCardinality());
      }
      return node;
   }

   public Node createTransitionElement(Transition transition)
   {
      Element node = document.createElement(TRANSITION);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(FROM_ATT, transition.getSourceId());
      writer.writeAttribute(TO, transition.getTargetID());
      writer.writeAttribute(CONDITION, transition.getCondition());
      writer.writeAttribute(FORK_ON_TRAVERSAL_ATT, transition.isForkOnTraversal());

      return node;
   }

   public Node createDataPathElement(DataPath dataPath)
   {
      Element node = document.createElement(DATA_PATH);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(DATA_REF_ATT, dataPath.getDataID());
      writer.writeAttribute(DATA_PATH_ATT, dataPath.getDataPath());
      writer.writeAttribute(DIRECTION_ATT, dataPath.getDirection());
      writer.writeAttribute(DESCRIPTOR_ATT, dataPath.isDescriptor());

      return node;
   }

   public Node createDataMappingElement(DataMapping dataMapping)
   {
      Element node = document.createElement(DATA_MAPPING);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(ID_ATT, dataMapping.getId());
      writer.writeAttribute(DIRECTION_ATT, dataMapping.getDirection());
      writer.writeAttribute(DATA_REF_ATT, dataMapping.getDataID());
      writer.writeAttribute(CONTEXT_ATT, dataMapping.getContext());
      writer.writeAttribute(APPLICATION_ACCESS_POINT_ATT,
            dataMapping.getApplicationAccessPointId());
      writer.writeAttribute(DATA_PATH_ATT, dataMapping.getDataPath());
      writer.writeAttribute(APPLICATION_PATH_ATT, dataMapping.getApplicationPath());

      return node;
   }

   public Node createViewElement(View view)
   {
      Element node = document.createElement(VIEW);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(NAME_ATT, view.getName());

      return node;
   }

   public Node attachViewableElement(ModelElement viewable)
   {
      Element node = document.createElement(VIEWABLE);
      NodeWriter writer = new NodeWriter(node);

      writer.writeAttribute(VIEWABLE_ATT, viewable.getElementOID());
      return node;
   }

   private void warn(String message, Exception exception, ModelElement scope)
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

   public Node createApplicationContextElement(ApplicationContext context)
   {
      Element contextElement = document.createElement(CONTEXT);
      contextElement.setAttribute(TYPE_ATT, context.getId());
      return contextElement;
   }

   public Node attachAssociatedParticipantElement(String participant)
   {
      Element result = document.createElement(PARTICIPANT);
      result.setAttribute(PARTICIPANT_ATT, participant);
      return result;
   }

   public Node createApplicationContextTypeElement(ApplicationContextType type)
   {
      Element node = document.createElement(APPLICATION_CONTEXT_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(HAS_MAPPING_ID_ATT, type.hasMappingId());
      writer.writeAttribute(HAS_APPLICATION_PATH_ATT, type.hasApplicationPath());
      return node;
   }

   public Node createTriggerTypeElement(TriggerType type)
   {
      Element node = document.createElement(TRIGGER_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(PULL_TRIGGER_ATT, type.isPullTrigger());
      return node;
   }

   public Node createEventConditionTypeElement(EventConditionType type)
   {
      Element node = document.createElement(EVENT_CONDITION_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(IMPLEMENTATION_ATT, type.getImplementation());
      writer.writeAttribute(PROCESS_CONDITION_ATT, type.isProcessCondition());
      writer.writeAttribute(ACTIVITY_CONDITION_ATT, type.isActivityCondition());
      return node;
   }

   public Node createEventActionTypeElement(EventActionType type)
   {
      Element node = document.createElement(EVENT_ACTION_TYPE);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(ACTIVITY_ACTION_ATT, type.isActivityAction());
      writer.writeAttribute(PROCESS_ACTION_ATT, type.isProcessAction());
      writer.writeAttribute(SUPPORTED_CONDITION_TYPES_ATT, type.getAllSupportedConditionTypes());
      return node;
   }

   public Node createEventHandlerElement(EventHandler eventHandler)
   {
      Element node = document.createElement(EVENT_HANDLER);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(TYPE_ATT, eventHandler.getType());
      writer.writeAttribute(AUTO_BIND_ATT, eventHandler.isAutoBind());
      writer.writeAttribute(CONSUME_ON_MATCH_ATT, eventHandler.isConsumeOnMatch());
      writer.writeAttribute(UNBIND_ON_MATCH_ATT, eventHandler.isUnbindOnMatch());
      writer.writeAttribute(LOG_HANDLER_ATT, eventHandler.isLogHandler());
      return node;
   }

   public Node createEventActionElement(EventAction action)
   {
      Element node = document.createElement(EVENT_ACTION);
      NodeWriter writer = new NodeWriter(node);
      writer.writeAttribute(TYPE_ATT, action.getType());
      return node;
   }
}
