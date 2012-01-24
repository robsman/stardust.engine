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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.eclipse.stardust.engine.api.model.AccessPointOwner;
import org.eclipse.stardust.engine.api.model.CardinalityKey;
import org.eclipse.stardust.engine.api.model.EventActionContext;
import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.EventType;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.IApplicationContextType;
import org.eclipse.stardust.engine.api.model.IApplicationType;
import org.eclipse.stardust.engine.api.model.IBindAction;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IEventAction;
import org.eclipse.stardust.engine.api.model.IEventActionType;
import org.eclipse.stardust.engine.api.model.IEventConditionType;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IExternalPackage;
import org.eclipse.stardust.engine.api.model.IFormalParameter;
import org.eclipse.stardust.engine.api.model.ILinkType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IModeler;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IParameterMapping;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IQualityAssurance;
import org.eclipse.stardust.engine.api.model.IQualityAssuranceCode;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.ITriggerType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IUnbindAction;
import org.eclipse.stardust.engine.api.model.IView;
import org.eclipse.stardust.engine.api.model.IViewable;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.JoinSplitType;
import org.eclipse.stardust.engine.api.model.LoopType;
import org.eclipse.stardust.engine.api.model.ModelParsingException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Scripting;
import org.eclipse.stardust.engine.api.model.SubProcessModeKey;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.UnresolvedExternalReference;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.ColorKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.GroupSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.LineKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.PathConnection;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.gui.ActivitySymbol;
import org.eclipse.stardust.engine.core.model.gui.AnnotationSymbol;
import org.eclipse.stardust.engine.core.model.gui.ApplicationSymbol;
import org.eclipse.stardust.engine.core.model.gui.ConditionalPerformerSymbol;
import org.eclipse.stardust.engine.core.model.gui.DataMappingConnection;
import org.eclipse.stardust.engine.core.model.gui.DataSymbol;
import org.eclipse.stardust.engine.core.model.gui.ModelerSymbol;
import org.eclipse.stardust.engine.core.model.gui.OrganizationSymbol;
import org.eclipse.stardust.engine.core.model.gui.ProcessDefinitionSymbol;
import org.eclipse.stardust.engine.core.model.gui.RoleSymbol;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.runtime.beans.ModelRefBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class ElementFactoryImpl implements ElementFactory
{
   private static final Logger trace = LogManager.getLogger(ElementFactoryImpl.class);
   private int elementOID;
   private String id;
   private String name;
   private String description;
   private boolean predefined;

   final private NodeReader reader;
   final private IConfigurationVariablesProvider confVariablesProvider;

   public ElementFactoryImpl(IConfigurationVariablesProvider confVariablesProvider)
   {
      reader = new NodeReader(confVariablesProvider);
      this.confVariablesProvider = confVariablesProvider;
   }

   public IActivity createActivity(Node node, IProcessDefinition process, IModel model, Map subprocessList)
   {
      IActivity activity = process.createActivity(id, name, description, elementOID);
      
      activity.setAllowsAbortByPerformer(reader.getBooleanAttribute(ALLOWS_ABORT_BY_PERFORMER_ATT, false));
      activity.setHibernateOnCreation(reader.getBooleanAttribute(HIBERNATE_ON_CREATION, false));

      String applicationID = reader.getRawAttribute(APPLICATION_REF_ATT);
      if (applicationID != null)
      {
         IApplication application = model.findApplication(applicationID);
         if (application == null)
         {
            warn(ConversionWarning.COSMETIC,
                  "Could not find associated application for activity " + elementOID, null, process);
         }
         else
         {
            activity.setApplication(application);
         }
      }
      String value = reader.getRawAttribute(IMPLEMENTATION_ATT);
      if (value != null)
      {
         activity.setImplementationType((ImplementationType) StringKey.getKey(ImplementationType.class, value));
      }
      value = reader.getRawAttribute(JOIN_ATT);
      if (value != null)
      {
         activity.setJoinType(JoinSplitType.getKey(value));
      }
      value = reader.getRawAttribute(SPLIT);
      if (value != null)
      {
         activity.setSplitType(JoinSplitType.getKey(value));
      }
      activity.setLoopCondition(reader.getAttribute(LOOP_CONDITION_ATT));
      value = reader.getRawAttribute(LOOP_TYPE);
      if (value != null)
      {
         activity.setLoopType(LoopType.getKey(value));
      }

      String performerID = reader.getRawAttribute(PERFORMER);
      if (performerID != null)
      {
         IModelParticipant participant = model.findParticipant(performerID);
         if (participant == null)
         {
            if (!activity.isInteractive()
                  && !ImplementationType.Route.equals(activity.getImplementationType()))
            {
               warn(ConversionWarning.COSMETIC,
                     "Could not find associated performer for activity " + elementOID,
                     null, process);
            }
         }
         else
         {
            activity.setPerformer(participant);
         }
      }

      String qualityAssurancePerformerID = reader.getRawAttribute(QUALITY_ASSURANCE_PERFORMER);
      if (qualityAssurancePerformerID != null)
      {
         IModelParticipant participant = model.findParticipant(qualityAssurancePerformerID);
         if (participant != null)
         {
            activity.setQualityAssurancePerformer(participant);
         }
      }
      
      String processID = reader.getRawAttribute(PROCESS_ID_REF);
      if (processID != null)
      {
         // hint: The subprocess-superprocess-relation will be created later
         //       when all process definition exist.
         //       This prevent problems with references to processes that
         //       (yet) doesn't exist.
         subprocessList.put(activity, processID);
      }
      activity.setSubProcessMode(SubProcessModeKey.getKey(reader
            .getRawAttribute(SUB_PROCESS_MODE)));
      return activity;
   }

   public IApplication createApplication(Node node, IModel model)
   {
      IApplication application = model.createApplication(id, name, description, elementOID);

      boolean interactive = reader.getBooleanAttribute(INTERACTIVE_ATT, false);
      application.setInteractive(interactive);

      if (!interactive)
      {
         String type = reader.getRawAttribute(TYPE_ATT);
         if (type != null)
         {
            application.setApplicationType(model.findApplicationType(type));
         }
      }
      return application;
   }

   public IApplicationContext createApplicationContext(Node node, IApplication application)
   {
      return application.createContext(reader.getAttribute(TYPE_ATT), elementOID);
   }

   public AccessPoint createAccessPoint(Node node, AccessPointOwner holder)
   {
      String dataTypeId = reader.getRawAttribute(TYPE_ATT);
      Direction direction = Direction.getKey(reader.getRawAttribute(DIRECTION_ATT));
      IDataType type = null;
      if (dataTypeId != null)
      {
         type = ((IModel) ((ModelElement) holder).getModel()).findDataType(dataTypeId);
         if (type == null)
         {
            warn(ConversionWarning.ERROR, "Unknown data type '" + type + "'.", null, (IdentifiableElement) holder);
         }
      }
      return holder.createAccessPoint(id, name, direction, type, elementOID);
   }

   public IApplicationType createApplicationType(Node node, IModel model)
   {
      return model.createApplicationType(id, name, predefined,
            reader.getBooleanAttribute(SYNCHRONOUS_ATT, true), elementOID);
   }

   public IDataType createDataType(Node node, IModel model)
   {
      return model.createDataType(id, name, predefined, elementOID);
   }

   public IApplicationContextType createApplicationContextType(Node node, IModel model)
   {
      return model.createApplicationContextType(id, name, predefined,
            reader.getBooleanAttribute(HAS_MAPPING_ID_ATT, true),
            reader.getBooleanAttribute(HAS_APPLICATION_PATH_ATT, false), elementOID);
   }

   public ITriggerType createTriggerType(Node node, IModel model)
   {
      return model.createTriggerType(id, name, predefined,
            reader.getBooleanAttribute(PULL_TRIGGER_ATT, false), elementOID);
   }

   public IEventConditionType createEventConditionType(Node node, IModel model)
   {
      return model.createEventConditionType(id, name, predefined,
            EventType.getKey(reader.getRawAttribute(IMPLEMENTATION_ATT)),
            reader.getBooleanAttribute(PROCESS_CONDITION_ATT, false),
            reader.getBooleanAttribute(ACTIVITY_CONDITION_ATT, false), elementOID);
   }

   public IEventHandler createEventHandler(Node childNode, EventHandlerOwner owner)
   {
      IEventHandler handler = owner.createEventHandler(id, name, description,
            ((IModel) owner.getModel()).findEventConditionType(reader.getRawAttribute(TYPE_ATT)),
            elementOID);
      handler.setAutoBind(reader.getBooleanAttribute(AUTO_BIND_ATT, false));
      handler.setUnbindOnMatch(reader.getBooleanAttribute(UNBIND_ON_MATCH_ATT, false));
      handler.setLogHandler(reader.getBooleanAttribute(LOG_HANDLER_ATT, false));
      handler.setConsumeOnMatch(reader.getBooleanAttribute(CONSUME_ON_MATCH_ATT, false));
      return handler;
   }

   public IEventActionType createEventActionType(Node node, IModel model)
   {
      IEventActionType result = model.createEventActionType(id, name, predefined,
            reader.getBooleanAttribute(PROCESS_ACTION_ATT, false),
            reader.getBooleanAttribute(ACTIVITY_ACTION_ATT, false), elementOID);
      for (Iterator i = reader.getRawListAttribute(SUPPORTED_CONDITION_TYPES_ATT).iterator();i.hasNext();)
      {
         String cid = (String) i.next();
         IEventConditionType conditionType = model.findEventConditionType(cid);
         if (conditionType == null)
         {
            warn(ConversionWarning.MEDIUM, "Unknown condition type '" + cid + "'.", null, result);
         }
         else
         {
            result.addSupportedConditionType(conditionType);
         }
      }
      for (Iterator i = reader.getRawListAttribute(UNSUPPORTED_CONTEXTS_ATT).iterator();i.hasNext();)
      {
         String cid = (String) i.next();
         EventActionContext context = EventActionContext.getKey(cid);
         if (context == null)
         {
            warn(ConversionWarning.MEDIUM, "Unknown event action context '" + cid + "'.", null, result);
         }
         else
         {
            result.addUnsupportedContext(context);
         }
      }
      return result;
   }

   public IEventAction createEventAction(Node node, IEventHandler handler)
   {
      return handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            reader.getRawAttribute(TYPE_ATT)),
            elementOID);
   }

   public IBindAction createBindAction(Node node, IEventHandler handler)
   {
      return handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            reader.getRawAttribute(TYPE_ATT)),
            elementOID);
   }

   public IUnbindAction createUnbindAction(Node node, IEventHandler handler)
   {
      return handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            reader.getRawAttribute(TYPE_ATT)),
            elementOID);
   }

   public ITypeDeclaration createTypeDeclaration(Node node, IModel model,
         IXpdlType xpdlType)
   {
      // need to fetch again the id/name because xpdl uses different case in attribute names.
      this.id = reader.getAttribute(XPDL_ID_ATT);
      this.name = reader.getAttribute(XPDL_NAME_ATT);
      // TODO: check if description is correctly processed.
      ITypeDeclaration typeDeclaration = model.createTypeDeclaration(id, name,
            description, Collections.EMPTY_MAP, xpdlType);
      if (node instanceof Element)
      {
         NodeList nlAttributes = ((Element)node).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTENDED_ATTRIBUTES);
         if (nlAttributes.getLength() == 0)
         {
            nlAttributes = ((Element)node).getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTENDED_ATTRIBUTES);
         }
         for (int i = 0; i < nlAttributes.getLength(); i++)
         {
            NodeList nlAttribute = ((Element)nlAttributes.item(i)).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTENDED_ATTRIBUTE);
            if (nlAttribute.getLength() == 0)
            {
               nlAttribute = ((Element)nlAttributes.item(i)).getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTENDED_ATTRIBUTE);
            }
            for (int j = 0; j < nlAttribute.getLength(); j++)
            {
               Element attributeElement = (Element) nlAttribute.item(j);
               String name = attributeElement.getAttribute(XPDL_EXTENDED_ATTRIBUTE_NAME);
               String value = attributeElement.getAttribute(XPDL_EXTENDED_ATTRIBUTE_VALUE);
               if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(value))
               {
                  typeDeclaration.setAttribute(name.trim().intern(), value.trim().intern());
               }
            }
         }
      }
      xpdlType.setParent(typeDeclaration);
      return typeDeclaration;
   }

   public IData createData(Node node, final IModel model)
   {
      String dataType = reader.getRawAttribute(TYPE_ATT);

      Map attributes = new HashMap();
      boolean isPredefined = predefined;

      IDataType type = model.findDataType(dataType);
      IData data = model.createData(id, type, name,
            description, isPredefined, elementOID, attributes);
      
      NodeList children = ((Element) node).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTERNAL_REFERENCE);
      for (int i = 0; i < children.getLength(); i++)
      {
         NodeReader reader = new NodeReader(children.item(i), confVariablesProvider);
         final String pkg = reader.getAttribute("location");
         final String ref = reader.getAttribute("xref");
         ReferenceBean reference = new ReferenceBean();
         reference.setId(ref);
         reference.setExternalPackage(model.findExternalPackage(pkg));
         ((DataBean) data).setExternalReference(reference);
      }

      if (null == type)
      {
         warn(ConversionWarning.ERROR, "Unknown data type '" + dataType + "'.", null, data);
      }

      return data;
   }

   public Diagram createDiagram(Node node, IProcessDefinition process)
   {
      return process.createDiagram(name, elementOID);
   }

   public Diagram createDiagram(Node node, IModel model)
   {
      return model.createDiagram(name, elementOID);
   }

   public IModeler createModeler(Node node, IModel model)
   {
      return model.createModeler(id, name, description,
            reader.getAttribute(PASSWORD), elementOID);
   }

   public ILinkType createLinkType(Node node, IModel model)
   {
      String sourceClassName = reader.getRawAttribute(SOURCE_CLASS);
      String sourceCardinality = reader.getRawAttribute(SOURCE_CARDINALITY);
      String sourceRoleName = reader.getAttribute(SOURCE_ROLE);
      ArrowKey sourceSymbol = new ArrowKey(reader.getRawAttribute(SOURCE_SYMBOL));
      String targetClassName = reader.getRawAttribute(TARGET_CLASS);
      String targetCardinality = reader.getRawAttribute(TARGET_CARDINALITY);
      String targetRoleName = reader.getAttribute(TARGET_ROLE);
      ArrowKey targetSymbol = new ArrowKey(reader.getRawAttribute(TARGET_SYMBOL));
      ColorKey lineColor = new ColorKey(reader.getRawAttribute(LINE_COLOR_ATT));
      LineKey lineType = new LineKey(reader.getRawAttribute(LINE_TYPE_ATT));
      boolean showLinkTypeName = reader.getBooleanAttribute(SHOW_LINKTYPE_NAME, false);

      boolean showRoleNames = reader.getBooleanAttribute(SHOW_ROLE_NAMES, false);
      try
      {
         return model.createLinkType(name,
               StringUtils.isEmpty(sourceClassName) ? null : Reflect.getClassFromClassName(sourceClassName),
               StringUtils.isEmpty(targetClassName) ? null : Reflect.getClassFromClassName(targetClassName),
               sourceRoleName, targetRoleName,
               // TODO: make it enumeration!
               new CardinalityKey(sourceCardinality), new CardinalityKey(targetCardinality),
               sourceSymbol, targetSymbol,
               lineColor, lineType, showLinkTypeName, showRoleNames, elementOID);
      }
      catch (InternalException e)
      {
         // @todo (france, ub):
         trace.warn("", e);
         return null;
      }
   }

   public ITrigger createTrigger(Node node, IProcessDefinition process)
   {
      ITrigger trigger = process.createTrigger(id, name,
            ((IModel)process.getModel()).findTriggerType(reader.getRawAttribute(TYPE_ATT)),
            elementOID);
      trigger.setDescription(description);
      return trigger;
   }

   public IParameterMapping createParameterMapping(Node mappingNode, ITrigger trigger)
   {
      String dataId = reader.getRawAttribute(DATA_REF_ATT);
      IData data = ((IModel)trigger.getModel()).findData(dataId);
      
      String dataPath = reader.getAttribute(DATA_PATH_ATT);
      String parameterId = reader.getAttribute(PARAMETER);
      String parameterPath = reader.getAttribute(PARAMETER_PATH);
      
      return trigger.createParameterMapping(data, dataPath, parameterId, parameterPath, elementOID);
   }

   public IModelParticipant createConditionalPerformer(Node node, IModel model)
   {
      String dataId = reader.getRawAttribute(DATA_REF_ATT);
      IData data = model.findData(dataId);

      String dataPath = reader.getAttribute(DATA_PATH_ATT);
      boolean isUser = reader.getBooleanAttribute(IS_USER_ATT, false);

      IConditionalPerformer performer =  model.createConditionalPerformer(id, name,
            description, data, elementOID);
      performer.setDereferencePath(dataPath);
      performer.setUser(isUser);

      return performer;
   }

   public IOrganization createOrganization(Node node, IModel model)
   {
      return model.createOrganization(id, name, description, elementOID);
   }

   public IProcessDefinition createProcess(Node node, IModel model)
   {
      ProcessDefinitionBean pd = (ProcessDefinitionBean) model.createProcessDefinition(id, name, description, false, elementOID);
      pd.setDefaultPriority(reader.getIntegerAttribute(DEFAULT_PRIORITY_ATT, 0));
      return pd;
   }

   public IRole createRole(Node node, IModel model)
   {
      IRole role = model.createRole(id, name, description, elementOID);

      role.setCardinality(reader.getIntegerAttribute(CARDINALITY_ATT, Unknown.INT));

      return role;
   }

   public ITransition createTransition(Node node, IProcessDefinition process)
   {
      ITransition transition = process.createTransition(id, name, description,
            process.findActivity(reader.getRawAttribute(FROM_ATT)),
            process.findActivity(reader.getRawAttribute(TO)),
            elementOID);

      String condition = reader.getAttribute(CONDITION);

      if (condition != null && !CONDITION_VALUE.equals(condition))
      {
         transition.setCondition(condition);
      }
      else
      {
         condition = reader.getChildValue(EXPRESSION);
         if (null != condition)
         {
            transition.setCondition(condition);
         }
         else
         {
            NodeList expressions = ((Element) node).getElementsByTagName(EXPRESSION);
            if (0 < expressions.getLength())
            {
               condition = XmlUtils.toString(expressions.item(0));
               transition.setCondition(condition.intern());
            }
         }
      }
      
      transition.setForkOnTraversal(reader.getBooleanAttribute(FORK_ON_TRAVERSAL_ATT, false));
      return transition;
   }

   public IDataMapping createDataMapping(Node node, IActivity activity)
   {
      String id = reader.getAttribute(ID_ATT);
      String name = reader.getAttribute(NAME_ATT);
      String activityID = reader.getRawAttribute(ACTIVITY_REF_ATT);
      String dataID = reader.getRawAttribute(DATA_REF_ATT);
      String directionName = reader.getRawAttribute(DIRECTION_ATT);
      String dataPath = reader.getAttribute(DATA_PATH_ATT);
      String applicationPath = reader.getAttribute(APPLICATION_PATH_ATT);
      String applicationAccessPointId = reader.getAttribute(APPLICATION_ACCESS_POINT_ATT);
      String context = reader.getAttribute(CONTEXT_ATT);

      IData data = ((IModel) activity.getModel()).findData(dataID);

      if (data == null)
      {
         warn(ConversionWarning.MEDIUM,
               "Data '" + dataID + "' for data mapping not found.", null, activity);
         return null;
      }

      //avoid duplicate mappings (at least check)
      Direction direction = (Direction) StringKey.getKey(Direction.class, directionName);
      if (activity.findDataMappingById(id, direction, context) != null)
      {
         warn(ConversionWarning.MEDIUM,
               "DataMapping for activity '" + activityID + "' and data '" + dataID
               + "' is not unique: id = '" + id
               + "', direction = " + directionName
               + "', context = " + context, null, activity);
      }

      IDataMapping dataMapping = activity.createDataMapping(id, name, data,
            direction, applicationAccessPointId, elementOID);

      dataMapping.setDataPath(dataPath);
      dataMapping.setActivityPath(applicationPath);
      if (context == null || context.length() == 0)
      {
         if (activity.isInteractive())
         {
            context = PredefinedConstants.DEFAULT_CONTEXT;
         }
         else
         {
            context = PredefinedConstants.ENGINE_CONTEXT;
         }
      }
      dataMapping.setContext(context);

      return dataMapping;
   }

   public IDataPath createDataPath(Node node, IProcessDefinition processDefinition)
   {
      String dataId = reader.getRawAttribute(DATA_REF_ATT);

      IDataPath result = processDefinition.createDataPath(id, name,
            ((IModel)processDefinition.getModel()).findData(dataId),
            reader.getAttribute(DATA_PATH_ATT),
            (Direction) StringKey.getKey(Direction.class, reader.getRawAttribute(DIRECTION_ATT)),
            elementOID);
      result.setDescriptor(reader.getBooleanAttribute(DESCRIPTOR_ATT, false));
      result.setKeyDescriptor(reader.getBooleanAttribute(KEY_ATT, false));
      return result;
   }

   public IViewable attachViewable(Node node, IModel model, IView view)
   {
      int oid = reader.getIntegerAttribute(VIEWABLE_ATT, 0);
      IViewable viewable = (IViewable) model.lookupElement(oid);

      if (viewable == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Could not find associated viewable for view " + elementOID, null, view);
         return null;
      }

      view.addToViewables(viewable);

      return viewable;
   }

   public IView createView(Node node, IModel model, IView parentView)
   {
      if (parentView != null)
      {
         return parentView.createView(name, description, elementOID);
      }
      else
      {
         return model.createView(name, description, elementOID);
      }
   }

   public ActivitySymbol createActivitySymbol(Node node, IProcessDefinition process, Diagram diagram)
   {
      String activityId = reader.getRawAttribute(USEROBJECT);

      IActivity activity = process.findActivity(activityId);

      if (activity == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Couldn't find the associated activity for activity symbol " + elementOID , null, null);
         return null;
      }

      ActivitySymbol symbol = new ActivitySymbol(activity);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));
      diagram.addToNodes(symbol, elementOID);

      return symbol;
   }

   public AnnotationSymbol createAnnotationSymbol(Node node, Diagram diagram)
   {
      AnnotationSymbol symbol = new AnnotationSymbol(reader.getChildValue(TEXT), null);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));
      diagram.addToNodes(symbol, elementOID);

      return symbol;
   }

   public ApplicationSymbol createApplicationSymbol(Node node, IModel model, Diagram diagram)
   {
      String referenceID = reader.getRawAttribute(USEROBJECT);

      IApplication application = model.findApplication(referenceID);

      if (application == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Couldn't find the associated application " + referenceID + " for "
               + "application symbol " + elementOID, null, null);
         return null;
      }

      ApplicationSymbol symbol = new ApplicationSymbol(application);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));
      diagram.addToNodes(symbol, elementOID);

      return symbol;
   }

   public DataMappingConnection createDataMappingConnection(Node node,
         Diagram diagram, IProcessDefinition processDefinition)
   {
      int dataSymbolID = reader.getIntegerAttribute(DATA_SYMBOL_ID, 0);
      int activitySymbolID = reader.getIntegerAttribute(ACTIVITY_SYMBOL_REF, 0);

      DataSymbol dataSymbol = (DataSymbol) diagram.getModel().lookupElement(dataSymbolID);
      if (dataSymbol == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Data symbol with id '" + dataSymbolID +
               "'for data mapping connection " + elementOID + " is null.", null, diagram);
         return null;
      }
      ActivitySymbol activitySymbol = (ActivitySymbol) diagram.getModel().lookupElement(activitySymbolID);
      if (activitySymbol == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Activity symbol with id '" + activitySymbolID +
               "'for data mapping connection "+ elementOID + " is null.", null, diagram);
         return null;
      }

      IActivity activity = activitySymbol.getActivity();
      if (activity == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Activity for activity symbol with id '" + activitySymbolID +
               "'for data mapping connection "+ elementOID +" is null.", null, diagram);
         return null;
      }

      IData data = dataSymbol.getData();
      if (data == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Data for data symbol with id '" + dataSymbolID +
               "'for data mapping connection is null.", null, diagram);
      }

      Direction direction = DataMappingConnection.getConnectionDirection(activity,
            data);
      if (direction != null)
      {
         DataMappingConnection connection = null;
         try
         {
            connection = new DataMappingConnection(data, activity);
            diagram.addToConnections(connection, elementOID);
            connection.setFirstSymbol(dataSymbol);
            connection.setSecondSymbol(activitySymbol, false);
            connection.updateDirection(direction);

            return connection;
         }
         catch (PublicException ex)
         {
            if (connection != null)
            {
               diagram.removeFromConnections(connection);
            }
            throw ex;
         }
      }

      return null;
   }

   public DataSymbol createDataSymbol(Node node, IModel model, Diagram diagram)
   {
      String dataId = reader.getRawAttribute(USEROBJECT);
      IData data = model.findData(dataId);

      if (data == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Could not find associated data for data symbol " + elementOID, null, diagram);
         return null;
      }

      DataSymbol symbol = new DataSymbol(data);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));
      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public GroupSymbol createGroupSymbol(Node node,
         Diagram diagram, IModel model, IProcessDefinition process, Collection _childList)
   {

      GroupSymbol symbol = GroupSymbol.createGroupSymbol(diagram, _childList);

      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public ModelerSymbol createModelerSymbol(Node node, IModel model, Diagram diagram)
   {
      String modelerId = reader.getRawAttribute(USEROBJECT);

      IModelParticipant modeler = model.findParticipant(modelerId);

      if (modeler == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Could not find associated modeler for modeler symbol " + elementOID, null, diagram);
         return null;
      }

      ModelerSymbol symbol = new ModelerSymbol((IModeler) modeler);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));
      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public OrganizationSymbol createOrganizationSymbol(Node node, IModel model, Diagram diagram)
   {
      String referenceID = reader.getRawAttribute(USEROBJECT);
      IModelParticipant organization = model.findParticipant(referenceID);

      if (organization == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Could not find associated organization for organization symbol " + elementOID, null, diagram);
         return null;
      }

      OrganizationSymbol symbol = new OrganizationSymbol((IOrganization) organization);

      try
      {
         int x = reader.getIntegerAttribute(X, 0);
         int y = reader.getIntegerAttribute(Y, 0);
         symbol.setX(x);
         symbol.setY(y);
      }
      catch (Exception ex)
      {
         warn(ConversionWarning.COSMETIC,
               "Can't read the position of symbol " + symbol, ex, null);
      }

      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public ConnectionSymbol attachConnection(Node node, ConnectionSymbol connection, Diagram diagram,
         String firstSymbolName, String secondSymbolName)
   {
      int firstID = reader.getIntegerAttribute(firstSymbolName, 0);
      int secondID = reader.getIntegerAttribute(secondSymbolName, 0);

      Symbol firstSymbol = (Symbol) diagram.getModel().lookupElement(firstID);
      Symbol secondSymbol = (Symbol) diagram.getModel().lookupElement(secondID);

      if (firstSymbol == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Connection '" + connection.getClass().getName() + "' with OID "
               + elementOID + " is missing first symbol. First id = " + firstSymbolName
               + ", second id " + secondSymbolName, null, diagram);
         return null;
      }
      if (secondSymbol == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Connection with OID " + elementOID + " is missing second symbol.", null, diagram);
         return null;
      }

      try
      {
         diagram.addToConnections(connection, elementOID);

         connection.setFirstSymbol(firstSymbol);
         connection.setSecondSymbol(secondSymbol, false);
      }
      catch (Exception e)
      {
         warn(ConversionWarning.MEDIUM, "Invalid connection with OID " + elementOID
               + " .", e, diagram);

         diagram.removeFromConnections(connection);
         return null;
      }

      if (connection instanceof PathConnection)
      {
         String pointsString = reader.getRawAttribute(POINTS);

         if (pointsString != null && pointsString.length() != 0)
         {
            StringTokenizer stringTokenizer = new StringTokenizer(pointsString, ",");
            List points = new ArrayList();

            while (stringTokenizer.hasMoreTokens())
            {
               String token = stringTokenizer.nextToken();

               points.add(new Integer(token.trim()));
            }

            ((PathConnection) connection).getPath().setPoints(points);
         }
      }

      return connection;
   }

   public ProcessDefinitionSymbol createProcessSymbol(Node node, IModel model, Diagram diagram)
   {
      String processID = reader.getRawAttribute(USEROBJECT);

      IProcessDefinition process = model.findProcessDefinition(processID);

      if (process == null)
      {
         warn(ConversionWarning.COSMETIC,
               "Could not find associated process for process symbol " + elementOID, null, diagram);
         return null;
      }

      ProcessDefinitionSymbol symbol = new ProcessDefinitionSymbol(process);

      try
      {
         symbol.setX(reader.getIntegerAttribute(X, 0));
         symbol.setY(reader.getIntegerAttribute(Y, 0));
      }
      catch (Exception ex)
      {
         warn(ConversionWarning.COSMETIC,
               "Can't read the position of symbol " + symbol, ex, null);
      }

      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public ConditionalPerformerSymbol createConditionalPerformerSymbol(Node node, IModel model, Diagram diagram)
   {
      String referenceID = reader.getRawAttribute(USEROBJECT);

      IModelParticipant participant = model.findParticipant(referenceID);

      if (participant == null || !(participant instanceof IConditionalPerformer))
      {
         warn(ConversionWarning.COSMETIC,
               "Couldn't find the associated performer " + referenceID + " for symbol " + elementOID, null, null);
         return null;
      }

      ConditionalPerformerSymbol symbol =
            new ConditionalPerformerSymbol((IConditionalPerformer) participant);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));
      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public RoleSymbol createRoleSymbol(Node node, IModel model, Diagram diagram)
   {
      String roleId = reader.getRawAttribute(USEROBJECT);

      IModelParticipant role = model.findParticipant(roleId);
      if (role == null || !(role instanceof IRole))
      {
         warn(ConversionWarning.COSMETIC,
               "Couldn't find the associated role " + roleId + " for symbol " + elementOID, null, null);
         return null;
      }

      RoleSymbol symbol = new RoleSymbol((IRole) role);

      symbol.setX(reader.getIntegerAttribute(X, 0));
      symbol.setY(reader.getIntegerAttribute(Y, 0));

      diagram.addToNodes(symbol, elementOID);
      return symbol;
   }

   public IModelParticipant attachParticipant(Node organizationNode, Node subNode, IModel model)
   {
      IOrganization organization = (IOrganization) model.findParticipant(reader.getRawAttribute(ID_ATT));
      reader.setNode(subNode);
      IModelParticipant associated = model.findParticipant(reader.getRawAttribute(PARTICIPANT_ATT));
      organization.addToParticipants(associated);
      return associated;
   }

   public IModelParticipant attachTeamLead(Node organizationNode, IModel model)
   {
      IOrganization organization = (IOrganization) model.findParticipant(reader.getRawAttribute(ID_ATT));
      
      IRole teamLead = null;
      String teamLeadID = reader.getRawAttribute(TEAM_LEAD);
      if (!StringUtils.isEmpty(teamLeadID))
      {
         IModelParticipant associated = model.findParticipant(teamLeadID);
         if (associated instanceof IRole)
         {
            teamLead = (IRole) associated;
            organization.setTeamLead(teamLead);
         }
      }
      return teamLead;
   }

   public IModel createModel(Element node)
   {
      ModelBean model = new ModelBean(id, name, description);

      String versionString = node.getAttribute(CARNOT_VERSION_ATT);
      Version version = null;
      try
      {
         version = new Version(versionString);
         model.setCarnotVersion(versionString);
      }
      catch (Exception e)
      {
         throw new ModelParsingException(
               BpmRuntimeError.MDL_UNKNOWN_IPP_VERSION.raise(versionString, id));
      }
      if (CompareHelper.compare(version, new Version(3, 0, 0)) < 0)
      {
         throw new ModelParsingException(
               BpmRuntimeError.MDL_UNSUPPORTED_IPP_VERSION.raise(version, id));
      }
      
      NodeList scriptElements = node.getElementsByTagNameNS(NS_XPDL_2_1, XPDL_SCRIPT);
      if (scriptElements.getLength() == 0)
      {
         scriptElements = node.getElementsByTagNameNS(NS_XPDL_1_0, XPDL_SCRIPT);
      }
      Node scriptElement = scriptElements.item(0);
      if (null != scriptElement)
      {
         NodeReader reader = new NodeReader(scriptElement, confVariablesProvider);
         model.setScripting(new Scripting(
               reader.getAttribute(XPDL_SCRIPT_TYPE_ATT),
               reader.getAttribute(XPDL_SCRIPT_VERSION_ATT),
               reader.getAttribute(XPDL_SCRIPT_GRAMMAR_ATT)
         ));
      }
      else
      {
         // defaults to carnotEL
         model.setScripting(new Scripting("text/carnotEL", null, null));
      }

      model.setModelOID(reader.getIntegerAttribute(MODEL_OID, 0));

      int maxOID = getMaxId(node, null, OID_ATT, "*");

      trace.debug("Max OID used in model: " + maxOID);

      ((ModelBean) model).setCurrentElementOID(maxOID + 1);
      
      return model;
   }

   private int getMaxId(Element node, String prefix, String attributeName,
         String elementFilter)
   {
      int max = 0;
      NodeList elements = node.getElementsByTagName(elementFilter);
      for (int i = 0, nElements = elements.getLength(); i < nElements; i++)
      {
         Element element = (Element) elements.item(i);
         if (element.hasAttribute(attributeName))
         {
            if (prefix != null)
            {
               String att = element.getAttribute(attributeName);
               if (att.startsWith(prefix))
               {
                  try
                  {
                     max = Math.max(max,
                           (int) Long.parseLong(element.getAttribute(attributeName).substring(prefix.length())));
                  }
                  catch (NumberFormatException e)
                  {
                     trace.debug("Rejected: " + element.getAttribute(attributeName));
                  }
               }
            }
            else
            {
               try
               {
                  max = Math.max(max,
                        (int) Long.parseLong(element.getAttribute(attributeName)));
               }
               catch (NumberFormatException e)
               {
                  trace.debug("Rejected: " + element.getAttribute(attributeName));
               }
            }
         }
      }
      return max;
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

   public void setNode(Node node)
   {
      reader.setNode(node);

      this.elementOID = reader.getIntegerAttribute(OID_ATT, 0);
      this.id = reader.getAttribute(ID_ATT, false);
      this.name = reader.getAttribute(NAME_ATT, false);
      this.description = reader.getChildValue(DESCRIPTION, false);
      this.predefined = reader.getBooleanAttribute(PREDEFINED_ATT, false);
   }

   public IExternalPackage createExternalPackage(Node node, final IModel model)
   {
      final String id = reader.getAttribute(XPDL_ID_ATT);
      final String name = reader.getAttribute(XPDL_NAME_ATT);
      final String href = reader.getAttribute(XPDL_HREF_ATT);
      final Map<String, String> attributes = getExtendedAttributes(node);
      IExternalPackage externalPackage = new IExternalPackage() {
         private IModel[] referencedModel;
         public String getId() {return id;}
         public String getName() {return name;}
         public String getHref() {return href;}
         public String getExtendedAttribute(String name)
         {
            return attributes.get(name);
         }
         public IModel getModel() {return model;}
         public IModel getReferencedModel() throws UnresolvedExternalReference
         {
            if (referencedModel == null)
            {
               IModel resolvedModel = ModelRefBean.resolveModel(this);
               trace.info(resolvedModel == null
                     ? "Reference '" + href + "' could not be resolved for model '" + model.getId()
                           + "' [" + model.getModelOID()+ "]."
                     : "Reference '" + href + "' was resolved for model '" + model.getId()
                           + "' to model with oid: " + resolvedModel.getModelOID());
               referencedModel = new IModel[] {resolvedModel};
            }
            return referencedModel[0];
         }
      };
      ((ModelBean) model).addToExternalPackages(externalPackage);
      return externalPackage;
   }
   
   private Map<String, String> getExtendedAttributes(Node node)
   {
      Map<String, String> attributes = CollectionUtils.newMap();
      if (node instanceof Element)
      {
         NodeList nlAttributes = ((Element)node).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTENDED_ATTRIBUTES);
         if (nlAttributes.getLength() == 0)
         {
            nlAttributes = ((Element)node).getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTENDED_ATTRIBUTES);
         }
         if (nlAttributes.getLength() == 1)
         {
            NodeList nlAttribute = ((Element)nlAttributes.item(0)).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTENDED_ATTRIBUTE);
            if (nlAttribute.getLength() == 0)
            {
               nlAttribute = ((Element)node).getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTENDED_ATTRIBUTE);
            }
            for (int i = 0, nNlAttribute = nlAttribute.getLength(); i < nNlAttribute; i++)
            {
               Element attributeElement = (Element)nlAttribute.item(i);
               attributes.put(
                     attributeElement.getAttribute(XPDL_EXTENDED_ATTRIBUTE_NAME),
                     attributeElement.getAttribute(XPDL_EXTENDED_ATTRIBUTE_VALUE));
            }
         }
      }
      return attributes;
   }

   public IReference createExternalReference(Node childNode, IModel model)
   {
      final String pkg = reader.getAttribute("PackageRef");
      final String ref = reader.getAttribute("ref");
      ReferenceBean reference = new ReferenceBean();
      reference.setId(ref);
      reference.setExternalPackage(model.findExternalPackage(pkg));
      return reference;
   }

   public IFormalParameter createFormalParameters(Node item, final IProcessDefinition process)
   {
      final String id = reader.getAttribute("Id");
      final String name = reader.getAttribute("Name");
      String mode = reader.getAttribute("Mode");
      final Direction direction = mode == null ? Direction.IN : Direction.getKey(mode);
      IFormalParameter parameter = new IFormalParameter()
      {
         public String getId() {return id;}
         public String getName() {return name;}
         public Direction getDirection() {return direction;}
         public IData getData() {return process.getMappedData(id);}
      };
      ((ProcessDefinitionBean) process).addToFormalParameters(parameter);
      return null;
   }

   public IQualityAssurance createQualityAssurance(Node node, IModel model)
   {
      return model.createQualityAssurance();
   }

   public IQualityAssuranceCode createQualityAssuranceCode(Node node, IQualityAssurance qualityAssurance)
   {
      String code = reader.getAttribute("code");
      String value = reader.getAttribute("value");
      
      return qualityAssurance.createQualityAssuranceCode(code, value);
   }

   public IQualityAssuranceCode createQualityAssuranceCode(Node node, IActivity activity, IModel model)
   {      
      Node firstChild = node.getFirstChild();
      String textContent = firstChild.getTextContent();
      int index = textContent.indexOf("#");
      String code = textContent.substring(index + 1);
      
      IQualityAssurance qualityAssurance = model.getQualityAssurance();
      return qualityAssurance.findQualityAssuranceCode(code);      
   }

   public void createQualityAssuranceAttributes(Node node, IActivity activity, IModel model)
   {
      String name = reader.getAttribute(NAME_ATT);      
      if(name.equals(PredefinedConstants.ACTIVITY_IS_QUALITY_ASSURANCE_ATT))
      {
         boolean isQualityAssuranceEnabled = reader.getBooleanAttribute(VALUE, false);
         if(isQualityAssuranceEnabled)
         {
            activity.setQualityAssuranceEnabled();
         }         
      }
      else if(name.equals(PredefinedConstants.QUALITY_ASSURANCE_PROBABILITY_ATT))
      {
         int intValue = 0;         
         
         try
         {
            intValue = Integer.parseInt(reader.getChildValue(VALUE));
            activity.setQualityAssuranceProbability(intValue);                     
         }
         catch (NumberFormatException e)
         {
         }
      }
      else if(name.equals(PredefinedConstants.QUALITY_ASSURANCE_FORMULA_ATT))
      {
         activity.setQualityAssuranceFormula(reader.getChildValue(VALUE));          
      }
   }
}