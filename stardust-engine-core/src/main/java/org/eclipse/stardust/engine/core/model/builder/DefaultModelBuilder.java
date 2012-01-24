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
package org.eclipse.stardust.engine.core.model.builder;

import java.util.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Mail;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.compatibility.el.DataTypeResolver;
import org.eclipse.stardust.engine.core.compatibility.el.JsConverter;
import org.eclipse.stardust.engine.core.extensions.actions.delegate.TargetWorklist;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.gui.AccessPointTemplate;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.extensions.jms.app.JMSDirection;
import org.eclipse.stardust.engine.extensions.jms.app.MessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.MessageProvider;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.mail.action.sendmail.ReceiverType;
import org.eclipse.stardust.engine.extensions.mail.trigger.MailProtocol;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultModelBuilder implements ModelBuilder
{
   private static final Logger trace = LogManager.getLogger(DefaultModelBuilder.class);

   private static final String MAIL_PARAMETER = "mail";

   private int handlerId;
   private int dataMappings;
   private int actionId;
   private int bindActionId;
   private int unbindActionId;
   private int triggerId;

   public static DefaultModelBuilder create()
   {
      return new DefaultModelBuilder();
   }

   protected DefaultModelBuilder()
   {
   }

   public void createPredefinedConstants(IModel model)
   {
      trace.debug("Creating default model elements.");
      
      model.setScripting(new Scripting(Scripting.ECMA_SCRIPT, null, null));

      model.createDiagram("Default Diagram");

      createPredefinedDataTypes(model);

      createPredefinedApplicationContextTypes(model);
      
      createPredefinedApplicationTypes(model);

      createPredefinedTriggerTypes(model);

      createPredefinedEventConditionTypes(model);

      createPredefinedEventActionTypes(model);

      createPredefinedData(model);

      createPredefinedModelers(model);

      createPredefinedRoles(model);
   }

   public static void createPredefinedDataTypes(IModel model)
   {
      if (null == model.findDataType(PredefinedConstants.PRIMITIVE_DATA))
      {
         IDataType primitive = model.createDataType(PredefinedConstants.PRIMITIVE_DATA,
               "Primitive", true, 0);
         primitive.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.PRIMITIVE_PANEL_CLASS);
         primitive.setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT,
               PredefinedConstants.PRIMITIVE_EVALUATOR_CLASS);
         primitive.setAttribute(PredefinedConstants.ACCESSPATH_EDITOR_ATT,
               PredefinedConstants.PRIMITIVE_ACCESSPATH_EDITOR_CLASS);
         primitive.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.PRIMITIVE_VALIDATOR_CLASS);
         primitive.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.PRIMITIVE_ICON_LOCATION);
      }

      if (null == model.findDataType(PredefinedConstants.SERIALIZABLE_DATA))
      {
         IDataType serializable = model.createDataType(
               PredefinedConstants.SERIALIZABLE_DATA, "Serializable", true, 0);
         serializable.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.SERIALIZABLE_PANEL_CLASS);
         serializable.setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT,
               PredefinedConstants.SERIALIZABLE_EVALUATOR_CLASS);
         serializable.setAttribute(PredefinedConstants.ACCESSPATH_EDITOR_ATT,
               PredefinedConstants.SERIALIZABLE_ACCESSPATH_EDITOR_CLASS);
         serializable.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.SERIALIZABLE_VALIDATOR_CLASS);
         serializable.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.SERIALIZABLE_ICON_LOCATION);
      }

      if (null == model.findDataType(PredefinedConstants.ENTITY_BEAN_DATA))
      {
         IDataType entity = model.createDataType(PredefinedConstants.ENTITY_BEAN_DATA,
               "Entity Bean", true, 0);
         entity.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.ENTITYBEAN_PANEL_CLASS);
         entity.setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT,
               PredefinedConstants.ENTITYBEAN_EVALUATOR_CLASS);
         entity.setAttribute(PredefinedConstants.ACCESSPATH_EDITOR_ATT,
               PredefinedConstants.ENTITYBEAN_ACCESSPATH_EDITOR_CLASS);
         entity.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.ENTITYBEAN_VALIDATOR_CLASS);
         entity.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.ENTITYBEAN_ICON_LOCATION);
      }

      if (null == model.findDataType(PredefinedConstants.PLAIN_XML_DATA))
      {
         IDataType xml = model.createDataType(PredefinedConstants.PLAIN_XML_DATA,
               "XML Document", true, 0);
         xml.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.PLAINXML_PANEL_CLASS);
         xml.setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT,
               PredefinedConstants.PLAINXML_EVALUATOR_CLASS);
         xml.setAttribute(PredefinedConstants.ACCESSPATH_EDITOR_ATT,
               PredefinedConstants.PLAINXML_ACCESSPATH_EDITOR_CLASS);
         xml.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.PLAINXML_VALIDATOR_CLASS);
         xml.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.PLAINXML_ICON_LOCATION);
      }
   }

   public static void createPredefinedApplicationContextTypes(IModel model)
   {
      if (null == model.findApplicationContextType(PredefinedConstants.DEFAULT_CONTEXT))
      {
         model.createApplicationContextType(PredefinedConstants.DEFAULT_CONTEXT,
               "Default Context", true, true, false, 0);
      }

      // @todo (france, ub): where is this used?
      if (null == model.findApplicationContextType(PredefinedConstants.ENGINE_CONTEXT))
      {
         model.createApplicationContextType(PredefinedConstants.ENGINE_CONTEXT,
               "Engine Context", true, false, true, 0);
      }

      // @todo (france, ub): where is this used?
      if (null == model.findApplicationContextType(PredefinedConstants.APPLICATION_CONTEXT))
      {
         model.createApplicationContextType(PredefinedConstants.APPLICATION_CONTEXT,
               "Noninteractive Application Context", true, false, true, 0);
      }

      if (null == model.findApplicationContextType(PredefinedConstants.JFC_CONTEXT))
      {
         IApplicationContextType jfcType = model.createApplicationContextType(
               PredefinedConstants.JFC_CONTEXT, "JFC Application", true, false, true, 0);
         jfcType.setAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
               PredefinedConstants.JFC_CONTEXT_ACCESSPOINT_PROVIDER_CLASS);
         jfcType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.JFC_CONTEXT_VALIDATOR_CLASS);
         jfcType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.JFC_CONTEXT_PANEL_CLASS);
         jfcType.setAttribute(PredefinedConstants.JFC_CONTEXT_INSTANCE_CLASS_ATT,
               PredefinedConstants.JFC_CONTEXT_INSTANCE_CLASS);
         jfcType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.JFC_CONTEXT_ICON_LOCATION);
      }

      if (null == model.findApplicationContextType(PredefinedConstants.JSP_CONTEXT))
      {
         IApplicationContextType jspType = model.createApplicationContextType(
               PredefinedConstants.JSP_CONTEXT, "JSP Application", true, true, false, 0);
         jspType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.JSP_CONTEXT_VALIDATOR_CLASS);
         jspType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.JSP_CONTEXT_PANEL_CLASS);
         jspType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.JSP_CONTEXT_ICON_LOCATION);
      }
   }

   public static void createPredefinedApplicationTypes(IModel model)
   {
      if (null == model.findApplicationType(PredefinedConstants.SESSIONBEAN_APPLICATION))
      {
         IApplicationType sbType = model.createApplicationType(
               PredefinedConstants.SESSIONBEAN_APPLICATION, "Session Bean Application",
               true, true, 0);
         sbType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.SESSIONBEAN_VALIDATOR_CLASS);
         sbType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.SESSIONBEAN_PANEL_CLASS);
         sbType.setAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
               PredefinedConstants.SESSIONBEAN_ACCESSPOINT_PROVIDER_CLASS);
         sbType.setAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT,
               PredefinedConstants.SESSIONBEAN_INSTANCE_CLASS);
         sbType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.SESSIONBEAN_ICON_LOCATION);
      }

      if (null == model.findApplicationType(PredefinedConstants.PLAINJAVA_APPLICATION))
      {
         IApplicationType javaType = model.createApplicationType(
               PredefinedConstants.PLAINJAVA_APPLICATION, "Plain Java Application", true,
               true, 0);
         javaType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.PLAINJAVA_VALIDATOR_CLASS);
         javaType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.PLAINJAVA_PANEL_CLASS);
         javaType.setAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
               PredefinedConstants.PLAINJAVA_ACCESSPOINT_PROVIDER_CLASS);
         javaType.setAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT,
               PredefinedConstants.PLAINJAVA_INSTANCE_CLASS);
         javaType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.PLAINJAVA_ICON_LOCATION);
      }

      if (null == model.findApplicationType(PredefinedConstants.JMS_APPLICATION))
      {
         IApplicationType jmsType = model.createApplicationType(
               PredefinedConstants.JMS_APPLICATION, "JMS Application", true, false, 0);
         jmsType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.JMS_APPLICATION_VALIDATOR_CLASS);
         jmsType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.JMS_APPLICATION_PANEL_CLASS);
         jmsType.setAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT,
               PredefinedConstants.JMS_APPLICATION_INSTANCE_CLASS);
         jmsType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.JMS_APPLICATION_ICON_LOCATION);
      }

      if (null == model.findApplicationType(PredefinedConstants.WS_APPLICATION))
      {
         IApplicationType wsType = model.createApplicationType(
               PredefinedConstants.WS_APPLICATION, "Web Service Application", true, true,
               0);
         wsType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.WS_APPLICATION_VALIDATOR_CLASS);
         wsType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.WS_APPLICATION_PANEL_CLASS);
         wsType.setAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT,
               PredefinedConstants.WS_APPLICATION_INSTANCE_CLASS);
         wsType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.WS_APPLICATION_ICON_LOCATION);
      }
   }

   public static void createPredefinedTriggerTypes(IModel model)
   {
      if (null == model.findTriggerType(PredefinedConstants.MANUAL_TRIGGER))
      {
         ITriggerType manualTrigger = model.createTriggerType(
               PredefinedConstants.MANUAL_TRIGGER, "Manual Trigger", true, false, 0);
         manualTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.MANUAL_TRIGGER_PANEL_CLASS);
         manualTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.MANUAL_TRIGGER_VALIDATOR_CLASS);
         manualTrigger.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.MANUAL_TRIGGER_ICON_LOCATION);
      }

      if (null == model.findTriggerType(PredefinedConstants.JMS_TRIGGER))
      {
         ITriggerType jmsTrigger = model.createTriggerType(
               PredefinedConstants.JMS_TRIGGER, "JMS Trigger", true, false, 0);
         jmsTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.JMS_TRIGGER_PANEL_CLASS);
         jmsTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.JMS_TRIGGER_VALIDATOR_CLASS);
         jmsTrigger.setAttribute(PredefinedConstants.ACCEPTOR_CLASS_ATT,
               PredefinedConstants.JMS_TRIGGER_MESSAGEACCEPTOR_CLASS);
         jmsTrigger.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.JMS_TRIGGER_ICON_LOCATION);
      }

      if (null == model.findTriggerType(PredefinedConstants.MAIL_TRIGGER))
      {
         ITriggerType mailTrigger = model.createTriggerType(
               PredefinedConstants.MAIL_TRIGGER, "Mail Trigger", true, true, 0);
         mailTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.MAIL_TRIGGER_PANEL_CLASS);
         mailTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.MAIL_TRIGGER_VALIDATOR_CLASS);
         mailTrigger.setAttribute(PredefinedConstants.PULL_TRIGGER_EVALUATOR_ATT,
               PredefinedConstants.MAIL_TRIGGER_EVALUATOR_CLASS);
         mailTrigger.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.MAIL_TRIGGER_ICON_LOCATION);
      }

      if (null == model.findTriggerType(PredefinedConstants.TIMER_TRIGGER))
      {
         ITriggerType timerTrigger = model.createTriggerType(
               PredefinedConstants.TIMER_TRIGGER, "Timer Based Trigger", true, true, 0);
         timerTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.TIMER_TRIGGER_PANEL_CLASS);
         timerTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.TIMER_TRIGGER_VALIDATOR_CLASS);
         timerTrigger.setAttribute(PredefinedConstants.PULL_TRIGGER_EVALUATOR_ATT,
               PredefinedConstants.TIMER_TRIGGER_EVALUATOR_CLASS);
         timerTrigger.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.TIMER_TRIGGER_ICON_LOCATION);
      }
   }

   public static void createPredefinedEventConditionTypes(IModel model)
   {
      if (null == model.findEventConditionType(PredefinedConstants.TIMER_CONDITION))
      {
         IEventConditionType timerCondition = model.createEventConditionType(
               PredefinedConstants.TIMER_CONDITION, "Timer", true, EventType.Pull, true,
               true, 0);
         timerCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.TIMER_CONDITION_PANEL_CLASS);
         timerCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.TIMER_CONDITION_VALIDATOR_CLASS);
         timerCondition.setAttribute(PredefinedConstants.CONDITION_BINDER_CLASS_ATT,
               PredefinedConstants.TIMER_CONDITION_BINDER_CLASS);
         timerCondition.setAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
               PredefinedConstants.TIMER_CONDITION_RULE_CLASS);
         timerCondition.setAttribute(PredefinedConstants.RUNTIME_PANEL_ATT,
               PredefinedConstants.TIMER_CONDITION_RUNTIME_PANEL_CLASS);
         timerCondition.setAttribute(PredefinedConstants.PULL_EVENT_EMITTER_ATT,
               PredefinedConstants.TIMER_CONDITION_EMITTER_CLASS);
         timerCondition.setAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
               PredefinedConstants.TIMER_CONDITION_ACCESSPOINT_PROVIDER_CLASS);
         timerCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.TIMER_CONDITION_ICON_LOCATION);
      }

      if (null == model.findEventConditionType(PredefinedConstants.EXCEPTION_CONDITION))
      {
         IEventConditionType exceptionCondition = model.createEventConditionType(
               PredefinedConstants.EXCEPTION_CONDITION, "On Exception", true,
               EventType.Engine, false, true, 0);
         exceptionCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.EXCEPTION_CONDITION_PANEL_CLASS);
         exceptionCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.EXCEPTION_CONDITION_VALIDATOR_CLASS);
         exceptionCondition.setAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
               PredefinedConstants.EXCEPTION_CONDITION_ACCESS_POINT_PROVIDER_CLASS);
         exceptionCondition.setAttribute(
               PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
               PredefinedConstants.EXCEPTION_CONDITION_RULE_CLASS);
         exceptionCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.EXCEPTION_CONDITION_ICON_LOCATION);
      }

      if (null == model.findEventConditionType(PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION))
      {
         IEventConditionType activityStatechangeCondition = model.createEventConditionType(
               PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION,
               "On Activity State Change", true, EventType.Engine, false, true, 0);
         activityStatechangeCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION_PANEL_CLASS);
         activityStatechangeCondition.setAttribute(
               PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.STATECHANGE_CONDITION_VALIDATOR_CLASS);
         activityStatechangeCondition.setAttribute(
               PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
               PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION_RULE_CLASS);
         activityStatechangeCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION_ICON_LOCATION);
      }

      if (null == model.findEventConditionType(PredefinedConstants.PROCESS_STATECHANGE_CONDITION))
      {
         IEventConditionType processStatechangeCondition = model.createEventConditionType(
               PredefinedConstants.PROCESS_STATECHANGE_CONDITION,
               "On Process State Change", true, EventType.Engine, true, false, 0);
         processStatechangeCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.PROCESS_STATECHANGE_CONDITION_PANEL_CLASS);
         processStatechangeCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.STATECHANGE_CONDITION_VALIDATOR_CLASS);
         processStatechangeCondition.setAttribute(
               PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
               PredefinedConstants.PROCESS_STATECHANGE_CONDITION_RULE_CLASS);
         processStatechangeCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.PROCESS_STATECHANGE_CONDITION_ICON_LOCATION);
      }

      if (null == model.findEventConditionType(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
      {
         IEventConditionType onAssignmentCondition = model.createEventConditionType(
               PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, "On Assignment",
               true, EventType.Engine, false, true, 0);
         onAssignmentCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION_PANEL_CLASS);
         /* todo (rsauer)runtime code already provides accees point values
          onAssignmentCondition.setAttribute(
          PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
          PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION_ACCESS_POINT_PROVIDER_CLASS);
          */
         onAssignmentCondition.setAttribute(
               PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
               PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION_RULE_CLASS);
         onAssignmentCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION_ICON_LOCATION);
      }

/*      
      if (null == model.findEventConditionType(PredefinedConstants.EXPRESSION_CONDITION))
      {
         IEventConditionType expressionCondition = model.createEventConditionType(
         PredefinedConstants.EXPRESSION_CONDITION, "On Data Change", true,
         EventType.Engine, true, true, 0);
         expressionCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_PANEL_CLASS);
         expressionCondition.setAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_CLASS);
         expressionCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_VALIDATOR_CLASS);
         expressionCondition.setAttribute(PredefinedConstants.ICON_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_ICON_LOCATION);
      }
*/

/*
      if (null == model.findEventConditionType(PredefinedConstants.EXTERNAL_EVENT_CONDITION))
      {
         IEventConditionType externalCondition = model.createEventConditionType(
               PredefinedConstants.EXTERNAL_EVENT_CONDITION, "External Event", true,
               EventType.Push, true, true, 0);
         externalCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.EXTERNAL_CONDITION_CLASS);
         externalCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.EXTERNAL_CONDITION_ICON_LOCATION);
      }
*/
   }

   public static void createPredefinedEventActionTypes(IModel model)
   {
      if (null == model.findEventActionType(PredefinedConstants.TRIGGER_ACTION))
      {
         IEventActionType triggerActionType = model.createEventActionType(
               PredefinedConstants.TRIGGER_ACTION, "Process Trigger", true, true, true, 0);
         triggerActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.TRIGGER_ACTION_PANEL_CLASS);
         triggerActionType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.TRIGGER_ACTION_VALIDATOR_CLASS);
         triggerActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.TRIGGER_ACTION_CLASS);
         triggerActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.TRIGGER_ACTION_ICON_LOCATION);
         supportAllConditionTypes(model, triggerActionType);
      }

      if (null == model.findEventActionType(PredefinedConstants.MAIL_ACTION))
      {
         IEventActionType mailActionType = model.createEventActionType(
               PredefinedConstants.MAIL_ACTION, "Send Mail", true, true, true, 0);
         mailActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.MAIL_ACTION_PANEL_CLASS);
         mailActionType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.MAIL_ACTION_VALIDATOR_CLASS);
         mailActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.MAIL_ACTION_RULE_CLASS);
         mailActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.MAIL_ACTION_ICON_LOCATION);
         supportAllConditionTypes(model, mailActionType);
      }

      if (null == model.findEventActionType(PredefinedConstants.ABORT_PROCESS_ACTION))
      {
         IEventActionType abortActionType = model.createEventActionType(
               PredefinedConstants.ABORT_PROCESS_ACTION, "Abort Process", true, true,
               true, 0);
         abortActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.ABORT_PROCESS_ACTION_CLASS);
         abortActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.ABORT_PROCESS_ACTION_ICON_LOCATION);
         Set unsupportedAbortConditions = new HashSet();
         unsupportedAbortConditions.add(PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION);
         unsupportedAbortConditions.add(PredefinedConstants.PROCESS_STATECHANGE_CONDITION);
         unsupportedAbortConditions.add(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION);
         supportAllConditionTypes(model, abortActionType, unsupportedAbortConditions);
         abortActionType.addUnsupportedContext(EventActionContext.Bind);
      }

      if (null == model.findEventActionType(PredefinedConstants.COMPLETE_ACTIVITY_ACTION))
      {
         IEventActionType completeActionType = model.createEventActionType(
               PredefinedConstants.COMPLETE_ACTIVITY_ACTION, "Complete Activity", true,
               false, true, 0);
         completeActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.COMPLETE_ACTIVITY_ACTION_CLASS);
         completeActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.COMPLETE_ACTIVITY_ACTION_ICON_LOCATION);
         Set unsupportedCompleteConditions = new HashSet();
         unsupportedCompleteConditions.add(PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION);
         unsupportedCompleteConditions.add(PredefinedConstants.PROCESS_STATECHANGE_CONDITION);
         unsupportedCompleteConditions.add(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION);
         supportAllActivityConditionTypes(model, completeActionType,
               unsupportedCompleteConditions);
         completeActionType.addUnsupportedContext(EventActionContext.Bind);
      }

      if (null == model.findEventActionType(PredefinedConstants.ACTIVATE_ACTIVITY_ACTION))
      {
         IEventActionType activateActionType = model.createEventActionType(
               PredefinedConstants.ACTIVATE_ACTIVITY_ACTION, "Activate Activity", true,
               false, true, 0);
         activateActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.ACTIVATE_ACTIVITY_ACTION_CLASS);
         activateActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.ACTIVATE_ACTIVITY_ACTION_ICON_LOCATION);
         Set unsupportedActivateConditions = new HashSet();
         unsupportedActivateConditions.add(PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION);
         unsupportedActivateConditions.add(PredefinedConstants.PROCESS_STATECHANGE_CONDITION);
         supportAllActivityConditionTypes(model, activateActionType,
               unsupportedActivateConditions);
      }

      if (null == model.findEventActionType(PredefinedConstants.DELEGATE_ACTIVITY_ACTION))
      {
         IEventActionType delegateActionType = model.createEventActionType(
               PredefinedConstants.DELEGATE_ACTIVITY_ACTION, "Delegate Activity", true,
               false, true, 0);
         delegateActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.DELEGATE_ACTIVITY_ACTION_CLASS);
         delegateActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.DELEGATE_ACTIVITY_PANEL_CLASS);
         delegateActionType.setAttribute(PredefinedConstants.RUNTIME_PANEL_ATT,
               PredefinedConstants.DELEGATE_ACTIVITY_RUNTIME_PANEL_CLASS);
         delegateActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.DELEGATE_ACTIVITY_ACTION_ICON_LOCATION);

         Set exc = new HashSet();
         exc.add(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION);
         supportAllActivityConditionTypes(model, delegateActionType, exc);
      }

      if (null == model.findEventActionType(PredefinedConstants.SCHEDULE_ACTIVITY_ACTION))
      {
         IEventActionType scheduleActionType = model.createEventActionType(
               PredefinedConstants.SCHEDULE_ACTIVITY_ACTION, "Schedule Activity", true,
               false, true, 0);
         scheduleActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.SCHEDULE_ACTIVITY_ACTION_CLASS);
         scheduleActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.SCHEDULE_ACTIVITY_PANEL_CLASS);
         scheduleActionType.setAttribute(PredefinedConstants.RUNTIME_PANEL_ATT,
               PredefinedConstants.SCHEDULE_ACTIVITY_RUNTIME_PANEL_CLASS);
         scheduleActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.SCHEDULE_ACTIVITY_ACTION_ICON_LOCATION);

         Set exc = new HashSet();
         exc.add(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION);
         supportAllActivityConditionTypes(model, scheduleActionType, exc);
      }

      if (null == model.findEventActionType(PredefinedConstants.EXCLUDE_USER_ACTION))
      {
         IEventActionType excludeUserActionType = model.createEventActionType(
               PredefinedConstants.EXCLUDE_USER_ACTION, "Exclude User", true, false, true,
               0);
         excludeUserActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.EXCLUDE_USER_ACTION_CLASS);
         excludeUserActionType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.EXCLUDE_USER_ACTION_VALIDATOR_CLASS);
         excludeUserActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.EXCLUDE_USER_PANEL_CLASS);
         excludeUserActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.EXCLUDE_USER_ACTION_ICON_LOCATION);
         IEventConditionType onAssignmentCondition = model.findEventConditionType(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION);
         if (null != onAssignmentCondition)
         {
            excludeUserActionType.addSupportedConditionType(onAssignmentCondition);
         }
      }

      if (null == model.findEventActionType(PredefinedConstants.SET_DATA_ACTION))
      {
         IEventActionType setdataActionType = model.createEventActionType(
               PredefinedConstants.SET_DATA_ACTION, "Set Data", true, true, true, 0);
         setdataActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
               PredefinedConstants.SET_DATA_ACTION_CLASS);
         setdataActionType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
               PredefinedConstants.SET_DATA_ACTION_VALIDATOR_CLASS);
         setdataActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.SET_DATA_ACTION_PANEL_CLASS);
         setdataActionType.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.SET_DATA_ACTION_ICON_LOCATION);
         supportAllConditionTypes(model, setdataActionType);
      }
   }

   public static void createPredefinedData(IModel model)
   {
      IDataType entityBeanDataType = model.findDataType(PredefinedConstants.ENTITY_BEAN_DATA);
      if (null != entityBeanDataType)
      {
         if (null == model.findData(PredefinedConstants.LAST_ACTIVITY_PERFORMER))
         {
            model.createData(PredefinedConstants.LAST_ACTIVITY_PERFORMER,
                  entityBeanDataType, "Last Activity Performer",
                  "User performing the last activity of the current process.", true, 0,
                  EntityBeanDataTypeUtils.initEntityBeanAttributes(
                        "ag.carnot.workflow.runtime.beans.IUser",
                        "ag.carnot.workflow.runtime.UserHome",
                        "ag.carnot.workflow.runtime.UserPK",
                        "ag.carnot.workflow.runtime.User", true));
         }

         if (null == model.findData(PredefinedConstants.STARTING_USER))
         {
            model.createData(PredefinedConstants.STARTING_USER, entityBeanDataType,
                  "Starting User", "User starting the current process.", true, 0,
                  EntityBeanDataTypeUtils.initEntityBeanAttributes(
                        "ag.carnot.workflow.runtime.beans.IUser",
                        "ag.carnot.workflow.runtime.UserHome",
                        "ag.carnot.workflow.runtime.UserPK",
                        "ag.carnot.workflow.runtime.User", true));
         }

         if (null == model.findData(PredefinedConstants.CURRENT_USER))
         {
            model.createData(
                  PredefinedConstants.CURRENT_USER,
                  entityBeanDataType,
                  "Current User",
                  "The User currently attached to the activity thread. Usually only available for synchronously executed activity threads.",
                  true, 0, EntityBeanDataTypeUtils.initEntityBeanAttributes(
                        "ag.carnot.workflow.runtime.beans.IUser",
                        "ag.carnot.workflow.runtime.UserHome",
                        "ag.carnot.workflow.runtime.UserPK",
                        "ag.carnot.workflow.runtime.User", true));
         }
      }

      IDataType primitiveDataType = model.findDataType(PredefinedConstants.PRIMITIVE_DATA);
      if (null != primitiveDataType)
      {
         if (null == model.findData(PredefinedConstants.PROCESS_ID))
         {
            model.createData(PredefinedConstants.PROCESS_ID, primitiveDataType,
                  "Process OID", "ID assigned to the current process.", true, 0,
                  JavaDataTypeUtils.initPrimitiveAttributes(Type.Long, null));
         }

         if (null == model.findData(PredefinedConstants.PROCESS_PRIORITY))
         {
            model.createData(PredefinedConstants.PROCESS_PRIORITY, primitiveDataType,
                  "Process Priority", "Priority assigned to the current process.", true, 0,
                  JavaDataTypeUtils.initPrimitiveAttributes(Type.Integer, null));
         }

         if (null == model.findData(PredefinedConstants.ROOT_PROCESS_ID))
         {
            model.createData(PredefinedConstants.ROOT_PROCESS_ID, primitiveDataType,
                  "Root Process OID",
                  "ID assigned to the root process of the current process.", true, 0,
                  JavaDataTypeUtils.initPrimitiveAttributes(Type.Long, null));
         }

         if (null == model.findData(PredefinedConstants.CURRENT_DATE))
         {
            model.createData(PredefinedConstants.CURRENT_DATE, primitiveDataType,
                  "Current Date", "Current Date.", true, 0,
                  JavaDataTypeUtils.initPrimitiveAttributes(Type.Calendar, null));
         }

         if (null == model.findData(PredefinedConstants.CURRENT_LOCALE))
         {
            model.createData(PredefinedConstants.CURRENT_LOCALE, primitiveDataType,
                  "Current Locale", "String representing the current locale.", true, 0,
                  JavaDataTypeUtils.initPrimitiveAttributes(Type.String, null));
         }
      }

      IDataType serializableDataType = model.findDataType(PredefinedConstants.SERIALIZABLE_DATA);
      if (null != serializableDataType)
      {
         if (null == model.findData(PredefinedConstants.CURRENT_MODEL))
         {
            model.createData(PredefinedConstants.CURRENT_MODEL, serializableDataType,
                  "Current Model", "Current Model.", true, 0,
                  JavaDataTypeUtils.initSerializableBeanAttributes(
                        DeployedModelDescription.class.getName()));
         }
      }
   }

   public static void createPredefinedModelers(IModel model)
   {
      IModelParticipant motu = model.findParticipant(PredefinedConstants.MOTU);
      if ((null == motu) || !(motu instanceof IModeler))
      {
         model.createModeler(PredefinedConstants.MOTU, "motu",
               "Master for workflow modeling.", PredefinedConstants.MOTU, 0);
      }
   }

   public static void createPredefinedRoles(IModel model)
   {
      IModelParticipant adminRole = model.findParticipant(PredefinedConstants.ADMINISTRATOR_ROLE);
      if (null == adminRole)
      {
         model.createRole(PredefinedConstants.ADMINISTRATOR_ROLE, "Administrator",
               "In charge of all workflow administration activities.", 0);
      }
   }

/*   private void supportAllActivityConditionTypes(IModel model,
         IEventActionType actionType)
   {
      supportAllActivityConditionTypes(model, actionType, Collections.EMPTY_SET);
   }*/

   private static void supportAllActivityConditionTypes(IModel model,
         IEventActionType actionType, Set exceptions)
   {
      for (Iterator i = model.getAllEventConditionTypes(); i.hasNext();)
      {
         IEventConditionType type = (IEventConditionType)i.next();
         if (type.hasActivityInstanceScope()
               && ((null == exceptions) || !exceptions.contains(type.getId())))
         {
            actionType.addSupportedConditionType(type);
         }
      }
   }

   private static void supportAllConditionTypes(IModel model, IEventActionType actionType)
   {
      supportAllConditionTypes(model, actionType, Collections.EMPTY_SET);
   }

   private static void supportAllConditionTypes(IModel model, IEventActionType actionType,
         Set exceptions)
   {
      for (Iterator i = model.getAllEventConditionTypes(); i.hasNext();)
      {
         IEventConditionType type = (IEventConditionType)i.next();
         if ((null == exceptions) || !exceptions.contains(type.getId()))
         {
            actionType.addSupportedConditionType(type);
         }
      }
   }

   public IModel createModel(String id, String name, String description)
   {
      IModel result = new ModelBean(id, name, description);
      createPredefinedConstants(result);
      return result;
   }

   public IModel createModel(String id)
   {
      return createModel(id, id, null);
   }

   public IApplication createSessionBeanApplication(IModel model,
         String home, String remote, String jndiName, String createMethod,
         String completeMethod, boolean localBinding)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createSessionBeanApplication(model, id, name, home, remote, jndiName,
            createMethod, completeMethod, localBinding);
   }

   public IApplication createSessionBeanApplication(IModel model, String id,
         String name, String home, String remote, String jndiName,
         String createMethod, String completeMethod, boolean localBinding)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setApplicationType(
            model.findApplicationType(PredefinedConstants.SESSIONBEAN_APPLICATION));
      // @todo (france, ub): strange
      result.setInteractive(false);
      result.setAttribute(PredefinedConstants.HOME_INTERFACE_ATT, home);
      result.setAttribute(PredefinedConstants.REMOTE_INTERFACE_ATT, remote);
      result.setAttribute(PredefinedConstants.JNDI_PATH_ATT, jndiName);
      result.setAttribute(PredefinedConstants.IS_LOCAL_ATT, new Boolean(localBinding));
      result.setAttribute(PredefinedConstants.CREATE_METHOD_NAME_ATT, createMethod);
      result.setAttribute(PredefinedConstants.METHOD_NAME_ATT, completeMethod);
      return result;
   }

   public IApplication createPlainJavaApplication(IModel model, String id, String name,
         String className, String constructor, String method)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setApplicationType(
            model.findApplicationType(PredefinedConstants.PLAINJAVA_APPLICATION));
      // @todo (france, ub): strange
      result.setInteractive(false);
      result.setAttribute(PredefinedConstants.CLASS_NAME_ATT, className);
      result.setAttribute(PredefinedConstants.CONSTRUCTOR_NAME_ATT, constructor);
      result.setAttribute(PredefinedConstants.METHOD_NAME_ATT, method);
      return result;
   }

   public IApplication createPlainJavaApplication(IModel model, String className,
         String constructor, String method)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createPlainJavaApplication(model, id, name, className, constructor, method);
   }

   public IApplication createJFCApplication(IModel model,
         String className, String method)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createJFCApplication(model, id, name, className, method);
   }

   public IApplication createJFCApplication(IModel model, String id, String name,
         String className, String method)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setInteractive(true);
      IApplicationContext context = result.createContext(PredefinedConstants.JFC_CONTEXT,
            0);
      context.setAttribute(PredefinedConstants.CLASS_NAME_ATT, className);
      context.setAttribute(PredefinedConstants.METHOD_NAME_ATT, method);
      return result;
   }

   public IApplication createJSPApplication(IModel model, String url)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createJSPApplication(model, id, name, url);
   }

   public IApplication createJSPApplication(IModel model, String id, String name,
         String url)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setInteractive(true);
      IApplicationContext context = result.createContext(
            PredefinedConstants.JSP_CONTEXT, 0);
      context.setAttribute(PredefinedConstants.HTML_PATH_ATT, url);
      return result;
   }

   public IApplication createJMSRequestApplication(IModel model,
         String connectionFactoryJNDI, String queueJNDI,
         MessageProvider provider, MessageType type, boolean includeOIDs)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createJMSRequestApplication(model, id, name, connectionFactoryJNDI,
            queueJNDI, provider, type, includeOIDs);
   }

   public IApplication createJMSRequestApplication(IModel model, String id, String name,
                                                   String connectionFactoryJNDI, String queueJNDI,
                                                   MessageProvider provider, MessageType type, boolean includeOIDs)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setInteractive(false);
      result.setAttribute(PredefinedConstants.TYPE_ATT, JMSDirection.OUT);
      result.setAttribute(PredefinedConstants.QUEUE_CONNECTION_FACTORY_NAME_PROPERTY,
            connectionFactoryJNDI);
      result.setAttribute(PredefinedConstants.QUEUE_NAME_PROPERTY, queueJNDI);
      result.setAttribute(PredefinedConstants.MESSAGE_PROVIDER_PROPERTY,
            provider.getClass().getName());
      result.setAttribute(PredefinedConstants.REQUEST_MESSAGE_TYPE_PROPERTY, type);
      result.setAttribute(PredefinedConstants.INCLUDE_OID_HEADERS_PROPERTY,
            new Boolean(includeOIDs));
      return result;
   }

   public IApplication createJMSResponseApplication(IModel model,
         MessageAcceptor acceptor, MessageType type)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createJMSResponseApplication(model, id, name, acceptor, type);
   }

   public IApplication createJMSResponseApplication(IModel model, String id, String name,
         MessageAcceptor acceptor, MessageType type)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setInteractive(false);
      result.setAttribute(PredefinedConstants.TYPE_ATT, JMSDirection.IN);
      result.setAttribute(PredefinedConstants.MESSAGE_ACCEPTOR_PROPERTY,
            acceptor.getClass().getName());
      result.setAttribute(PredefinedConstants.RESPONSE_MESSAGE_TYPE_PROPERTY, type);
      return result;
   }

   public IApplication createJMSRequestResponseApplication(IModel model,
         String connectionFactoryJNDI, String queueJNDI,
         MessageProvider provider, MessageType requestType, boolean includeOIDs,
         MessageAcceptor acceptor, MessageType responseType)
   {
      String id = model.getDefaultApplicationId();
      String name = id.toUpperCase();
      return createJMSRequestResponseApplication(model, id, name, connectionFactoryJNDI,
            queueJNDI, provider, requestType, includeOIDs, acceptor, responseType);
   }

   public IApplication createJMSRequestResponseApplication(IModel model, String id, String name,
      String connectionFactoryJNDI, String queueJNDI,
      MessageProvider provider, MessageType requestType, boolean includeOIDs,
      MessageAcceptor acceptor, MessageType responseType)
   {
      IApplication result = model.createApplication(id, name, null, 0);
      result.setInteractive(false);
      result.setAttribute(PredefinedConstants.TYPE_ATT, JMSDirection.INOUT);
      result.setAttribute(PredefinedConstants.QUEUE_CONNECTION_FACTORY_NAME_PROPERTY,
            connectionFactoryJNDI);
      result.setAttribute(PredefinedConstants.QUEUE_NAME_PROPERTY, queueJNDI);
      result.setAttribute(PredefinedConstants.MESSAGE_PROVIDER_PROPERTY,
            provider.getClass().getName());
      result.setAttribute(PredefinedConstants.REQUEST_MESSAGE_TYPE_PROPERTY, requestType);
      result.setAttribute(PredefinedConstants.INCLUDE_OID_HEADERS_PROPERTY,
            new Boolean(includeOIDs));
      result.setAttribute(PredefinedConstants.MESSAGE_ACCEPTOR_PROPERTY,
            acceptor.getClass().getName());
      result.setAttribute(PredefinedConstants.RESPONSE_MESSAGE_TYPE_PROPERTY, responseType);
      return result;
   }

   public IProcessDefinition createProcessDefinition(IModel model, String id)
   {
      return model.createProcessDefinition(id, id, null);
   }

   public IOrganization createOrganization(IModel model)
   {
      String id = model.getDefaultOrganizationId();
      String name = id.toUpperCase();
      return createOrganization(model, id, name);
   }

   public IOrganization createOrganization(IModel model, String id, String name)
   {
      return model.createOrganization(id, name, null, 0);
   }

   public IRole createRole(IModel model)
   {
      String id = model.getDefaultRoleId();
      String name = id.toUpperCase();
      return createRole(model, id, name);
   }

   public IRole createRole(IModel model, String id, String name)
   {
      return model.createRole(id, name, null, 0);
   }

   public IActivity createRouteActivity(IProcessDefinition process)
   {
      String id = process.getDefaultActivityId();
      String name = id.toUpperCase();
      return createRouteActivity(process, id, name);
   }

   public IActivity createRouteActivity(IProcessDefinition process, String id,
         String name)
   {
      IActivity result = process.createActivity(id, name, null, 0);
      result.setImplementationType(ImplementationType.Route);
      return result;
   }

   public IActivity createManualActivity(IProcessDefinition processDefinition,
         IModelParticipant performer)
   {
      String id = (processDefinition).getDefaultActivityId();
      String name = id.toUpperCase();
      return createManualActivity(processDefinition, id, name, performer);
   }

   public IActivity createManualActivity(IProcessDefinition processDefinition,
         String id, String name, IModelParticipant performer)
   {
      IActivity result = processDefinition.createActivity(id, name, null, 0);
      result.setImplementationType(ImplementationType.Manual);
      result.setPerformer(performer);
      return result;
   }

   public IActivity createApplicationActivity(IProcessDefinition processDefinition,
         IApplication application)
   {
      String id = processDefinition.getDefaultActivityId();
      String name = id.toUpperCase();

      return createApplicationActivity(processDefinition, id, name, application);
   }

   public IActivity createApplicationActivity(IProcessDefinition processDefinition,
         String id, String name, IApplication application)
   {
      IActivity result = processDefinition.createActivity(id, name, null, 0);
      result.setImplementationType(ImplementationType.Application);
      result.setApplication(application);
      return result;
   }

   public IActivity createSubprocessActivity(IProcessDefinition processDefinition,
         IProcessDefinition subProcess)
   {
      String id = processDefinition.getDefaultActivityId();
      String name = id.toUpperCase();
      return createSubprocessActivity(processDefinition, id, name, subProcess);
   }

   public IActivity createSubprocessActivity(IProcessDefinition processDefinition,
         String id, String name, IProcessDefinition subProcess)
   {
      IActivity result = processDefinition.createActivity(id, name, null, 0);
      result.setImplementationType(ImplementationType.SubProcess);
      result.setImplementationProcessDefinition(subProcess);
      return result;
   }

   public ITransition createTransition(final IActivity source, IActivity target,
         String condition)
   {
      String id = source.getProcessDefinition().getDefaultTransitionId();
      String name = id.toUpperCase();

      return createTransition(id, name, source, target, condition);
   }

   public ITransition createJsTransition(IActivity source, IActivity target,
         String condition)
   {
      String id = source.getProcessDefinition().getDefaultTransitionId();
      String name = id.toUpperCase();

      return createJsTransition(id, name, source, target, condition);
   }

   public ITransition createTransition(String id, String name, IActivity source,
         IActivity target, String condition)
   {
      return createJsTransition(id, name, source, target,
          createJsConverter((IModel) source.getModel()).convert(condition));
   }

   private JsConverter createJsConverter(final IModel model)
   {
      JsConverter converter = new JsConverter(new DataTypeResolver()
      {
         public String resolveDataType(String dataId)
         {
            IData data = model.findData(dataId);
            if (data == null)
            {
               return null;
            }
            PluggableType type = data.getType();
            if (type == null)
            {
               return null;
            }
            return type.getId();
         }
      });
      return converter;
   }

   public ITransition createJsTransition(String id, String name, IActivity source,
         IActivity target, String condition)
   {
      ITransition result = source.getProcessDefinition().createTransition(id, name, null,
            source, target, 0);
      if (!StringUtils.isEmpty(condition))
      {
         result.setCondition(condition);
      }
      return result;
   }

   public ITransition[] split(IActivity source, IActivity[] targets, JoinSplitType type)
   {
      source.setSplitType(type);
      ITransition[] result = targets == null ?
            new ITransition[0]: new ITransition[targets.length];
      for (int i = 0; i < targets.length; i++)
      {
         createTransition(source, targets[i], null);
      }
      return result;
   }

   public ITransition[] join(IActivity[] sources, IActivity target, JoinSplitType type)
   {
      target.setJoinType(type);
      ITransition[] result = sources == null ?
            new ITransition[0]: new ITransition[sources.length];
      for (int i = 0; i < sources.length; i++)
      {
         createTransition(sources[i], target, null);
      }
      return result;
   }

   public IDataMapping createEngineMapping(IActivity activity,
         Direction direction, String applicationAP, String applicationPath, IData data,
         String dataPath)
   {
      String id = "dm" + ++dataMappings;
      IDataMapping result = activity.createDataMapping(id, id, data, direction,
            applicationAP, 0);
      result.setActivityPath(applicationPath);
      result.setDataPath(dataPath);
      result.setContext(PredefinedConstants.ENGINE_CONTEXT);
      return result;
   }

   public IDataMapping createNoninteractiveMapping(IActivity activity,
         Direction direction, String applicationAP, String applicationPath, IData data,
         String dataPath)
   {
      String id = "dm" + ++dataMappings;
      IDataMapping result = activity.createDataMapping(id, id, data, direction,
            applicationAP, 0);
      result.setActivityPath(applicationPath);
      result.setDataPath(dataPath);
      result.setContext(PredefinedConstants.APPLICATION_CONTEXT);
      return result;
   }

   public IDataMapping createManualMapping(IActivity activity, String id,
         Direction direction, IData data, String dataPath)
   {
      IDataMapping result = activity.createDataMapping(id, id, data, direction);
      result.setContext(PredefinedConstants.DEFAULT_CONTEXT);
      return result;
   }

   public IDataMapping createJFCMapping(IActivity activity, Direction direction,
         String applicationAP, String applicationPath, IData data, String dataPath)
   {

      String id = "dm" + ++dataMappings;
      IDataMapping result = activity.createDataMapping(id, id, data, direction,
            applicationAP, 0);
      result.setActivityPath(applicationPath);
      result.setDataPath(dataPath);
      result.setContext(PredefinedConstants.JFC_CONTEXT);
      return result;
   }

   public IData createSerializableData(IModel model, String className)
   {
      String id = model.getDefaultDataId();
      String name = id.toUpperCase();
      return createSerializableData(model, id, name, className);
   }

   public IData createSerializableData(IModel model, String id, String name,
         String className)
   {
      Map attributes = new HashMap();
      attributes.put(PredefinedConstants.CLASS_NAME_ATT, className);
      return model.createData(id,
            model.findDataType(PredefinedConstants.SERIALIZABLE_DATA), name, null, false,
            0, attributes);
   }

   public IData createEntityBeanData(IModel model, String beanClassName, String jndiName,
         String homeClassName, String pkClassName)
   {
      String id = model.getDefaultDataId();
      String name = id.toUpperCase();
      return createEntityBeanData(model, id, name, beanClassName, jndiName,
            homeClassName, pkClassName);
   }

   public IData createEntityBeanData(IModel model, String id, String name,
         String beanClassName, String jndiName, String homeClassName, String pkClassName)
   {
      Map attributes = new HashMap();

      attributes.put(PredefinedConstants.REMOTE_INTERFACE_ATT, beanClassName);
      attributes.put(PredefinedConstants.JNDI_PATH_ATT, jndiName);
      attributes.put(PredefinedConstants.HOME_INTERFACE_ATT, homeClassName);
      attributes.put(PredefinedConstants.PRIMARY_KEY_ATT, pkClassName);
      return model.createData(id,
            model.findDataType(PredefinedConstants.ENTITY_BEAN_DATA), name, null, false,
            0, attributes);
   }

   public IData createPrimitiveData(IModel model, Type type, String defaultValue)
   {
      String id = model.getDefaultDataId();
      String name = id.toUpperCase();
      return createPrimitiveData(model, id, name, type, defaultValue);
   }

   public IData createPrimitiveData(IModel model, String id, String name,
         Type type, String defaultValue)
   {
      Map atts = new HashMap();
      atts.put(PredefinedConstants.TYPE_ATT, type);
      atts.put(PredefinedConstants.DEFAULT_VALUE_ATT, defaultValue);
      IData result = model.createData(id,
            model.findDataType(PredefinedConstants.PRIMITIVE_DATA), name, null, false, 0,
            atts);
      return result;
   }

   public IData createPlainXMLData(IModel model)
   {
      String id = model.getDefaultDataId();
      String name = id.toUpperCase();
      return createPlainXMLData(model, id, name);
   }

   public IData createPlainXMLData(IModel model, String id, String name)
   {
      return model.createData(id,
            model.findDataType(PredefinedConstants.PLAIN_XML_DATA), name, null, false, 0,
            Collections.EMPTY_MAP);
   }

   public IEventHandler createExceptionEventHandler(IActivity activity, String exception)
   {
      // @todo (france, ub): unify with how id's are handled for e.g. activities
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      IEventHandler result = activity.createEventHandler(id, name, null,
            ((IModel) activity.getModel()).findEventConditionType(
                  PredefinedConstants.EXCEPTION_CONDITION),
            0);
      result.setAttribute(PredefinedConstants.EXCEPTION_CLASS_ATT, exception);
      return result;
   }

   public IEventHandler createTimerEventHandler(EventHandlerOwner owner, Period period)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      IEventHandler result = owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
                  PredefinedConstants.TIMER_CONDITION),
            0);
      result.setAttribute(PredefinedConstants.TIMER_PERIOD_ATT, period);
      return result;
   }

   public IEventHandler createTimerEventHandler(EventHandlerOwner owner, IData data,
         String dataPath)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      IEventHandler result = owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
                  PredefinedConstants.TIMER_CONDITION),
            0);
      result.setAttribute(PredefinedConstants.TIMER_CONDITION_USE_DATA_ATT, Boolean.TRUE);
      result.setAttribute(PredefinedConstants.TIMER_CONDITION_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.TIMER_CONDITION_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IEventHandler createStatechangeEventHandler(EventHandlerOwner owner,
         ActivityInstanceState source, ActivityInstanceState target)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      IEventHandler result = owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
                  PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION),
            0);
      result.setAttribute(PredefinedConstants.SOURCE_STATE_ATT, source);
      result.setAttribute(PredefinedConstants.TARGET_STATE_ATT, target);
      return result;
   }

   public IEventHandler createStatechangeEventHandler(EventHandlerOwner owner,
         ProcessInstanceState source, ProcessInstanceState target)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      IEventHandler result = owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
                  PredefinedConstants.PROCESS_STATECHANGE_CONDITION),
            0);
      result.setAttribute(PredefinedConstants.SOURCE_STATE_ATT, source);
      result.setAttribute(PredefinedConstants.TARGET_STATE_ATT, target);
      return result;
   }

   public IEventHandler createExternalEventHandler(EventHandlerOwner owner)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      return owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
            PredefinedConstants.EXTERNAL_EVENT_CONDITION), 0);
   }

   public IEventHandler createDataChangeEventHandler(EventHandlerOwner owner,
         String condition)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      IEventHandler result = owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
                  PredefinedConstants.EXPRESSION_CONDITION),0);
      result.setAttribute(PredefinedConstants.WORKFLOW_EXPRESSION_ATT, condition);
      return result;
   }

   public IEventHandler createAssignmentEventHandler(EventHandlerOwner owner)
   {
      String id = "handler" + ++handlerId;
      String name = id.toUpperCase();
      return owner.createEventHandler(id, name, null,
            ((IModel) owner.getModel()).findEventConditionType(
                  PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION),0);
   }

   public IEventAction createDelegateAction(IEventHandler handler,
         TargetWorklist targetWorklist, String targetParticipant)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.DELEGATE_ACTIVITY_ACTION), 0);
      result.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, targetWorklist);
      result.setAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT, targetParticipant);
      return result;
   }

   public IBindAction createDelegateBindAction(IEventHandler handler,
         TargetWorklist targetWorklist, String targetParticipant)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.DELEGATE_ACTIVITY_ACTION), 0);
      result.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, targetWorklist);
      result.setAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT, targetParticipant);
      return result;
   }

   public IUnbindAction createDelegateUnbindAction(IEventHandler handler, TargetWorklist targetWorklist, String targetParticipant)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.DELEGATE_ACTIVITY_ACTION), 0);
      result.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, targetWorklist);
      result.setAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT, targetParticipant);
      return result;
   }

   public IEventAction createScheduleAction(IEventHandler handler,
         ActivityInstanceState targetState
         )
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.SCHEDULE_ACTIVITY_ACTION), 0);
      result.setAttribute(PredefinedConstants.TARGET_STATE_ATT, targetState);
      return result;
   }

   public IBindAction createScheduleBindAction(IEventHandler handler,
         ActivityInstanceState targetState
         )
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.SCHEDULE_ACTIVITY_ACTION), 0);
      result.setAttribute(PredefinedConstants.TARGET_STATE_ATT, targetState);
      return result;
   }

   public IUnbindAction createScheduleUnbindAction(IEventHandler handler, ActivityInstanceState targetState)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.SCHEDULE_ACTIVITY_ACTION), 0);
      result.setAttribute(PredefinedConstants.TARGET_STATE_ATT, targetState);
      return result;
   }

   public IEventAction createAbortProcessAction(IEventHandler handler)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.ABORT_PROCESS_ACTION), 0);
      return result;
   }

   public IBindAction createAbortProcessBindAction(IEventHandler handler)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.ABORT_PROCESS_ACTION), 0);
      return result;
   }

   public IUnbindAction createAbortProcessUnbindAction(IEventHandler handler)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.ABORT_PROCESS_ACTION), 0);
      return result;
   }

   public IEventAction createCompleteActivityAction(IEventHandler handler)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      return handler.createEventAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.COMPLETE_ACTIVITY_ACTION),
            0);
   }

   public IBindAction createCompleteActivityBindAction(IEventHandler handler)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      return handler.createBindAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.COMPLETE_ACTIVITY_ACTION),
            0);
   }

   public IUnbindAction createCompleteActivityUnbindAction(IEventHandler handler)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      return handler.createUnbindAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.COMPLETE_ACTIVITY_ACTION),
            0);
   }

   public IEventAction createActivateActivityAction(IEventHandler handler)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      return handler.createEventAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.ACTIVATE_ACTIVITY_ACTION),
            0);
   }

   public IBindAction createActivateActivityBindAction(IEventHandler handler)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      return handler.createBindAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.ACTIVATE_ACTIVITY_ACTION),
            0);
   }

   public IUnbindAction createActivateActivityUnbindAction(IEventHandler handler)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      return handler.createUnbindAction(id, name, ((IModel) handler.
            getModel()).findEventActionType(PredefinedConstants.ACTIVATE_ACTIVITY_ACTION),
            0);
   }

   public IEventAction createTriggerProcessAction(IEventHandler handler, IProcessDefinition process)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.TRIGGER_ACTION), 0);
      result.setAttribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT, process.getId());
      return result;
   }

   public IBindAction createTriggerProcessBindAction(IEventHandler handler, IProcessDefinition process)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.TRIGGER_ACTION), 0);
      result.setAttribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT, process.getId());
      return result;
   }

   public IUnbindAction createTriggerProcessUnbindAction(IEventHandler handler, IProcessDefinition process)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.TRIGGER_ACTION), 0);
      result.setAttribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT, process.getId());
      return result;
   }

   public IEventAction createSetDataAction(IEventHandler handler, String ap,
         String attPath, String dataPath, IData data)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.SET_DATA_ACTION), 0);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, ap);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, attPath);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, dataPath);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, data.getId());
      return result;
   }

   public IBindAction createSetDataBindAction(IEventHandler handler, String ap, String attPath, String dataPath, IData data)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.SET_DATA_ACTION), 0);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, ap);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, attPath);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, dataPath);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, data.getId());
      return result;
   }

   public IUnbindAction createSetDataUnbindAction(IEventHandler handler, String ap, String attPath, String dataPath, IData data)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.SET_DATA_ACTION), 0);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, ap);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, attPath);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, dataPath);
      result.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, data.getId());
      return result;
   }

   public IEventAction createExcludeUserAction(IEventHandler handler, IData data, String dataPath)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.EXCLUDE_USER_ACTION), 0);
      result.setAttribute(PredefinedConstants.EXCLUDED_PERFORMER_DATA, data.getId());
      result.setAttribute(PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH, dataPath);
      return result;
   }

   public IEventAction createSendMailAction(IEventHandler handler, String email, String messageText)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.EMail);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT, email);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IBindAction createSendMailBindAction(IEventHandler handler, String email, String messageText)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.EMail);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT, email);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IUnbindAction createSendMailUnbindAction(IEventHandler handler, String email, String messageText)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.EMail);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT, email);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IEventAction createSendMailAction(IEventHandler handler, String email, IData data, String dataPath)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.EMail);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT, email);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IBindAction createSendMailBindAction(IEventHandler handler, String email, IData data, String dataPath)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.EMail);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT, email);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IUnbindAction createSendMailUnbindAction(IEventHandler handler, String email, IData data, String dataPath)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.EMail);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT, email);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IEventAction createSendMailAction(IEventHandler handler, String messageText)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.CurrentUserPerformer);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IBindAction createSendMailBindAction(IEventHandler handler, String messageText)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.CurrentUserPerformer);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IUnbindAction createSendMailUnbindAction(IEventHandler handler, String messageText)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.CurrentUserPerformer);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IEventAction createSendMailAction(IEventHandler handler, IData data, String dataPath)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.CurrentUserPerformer);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IBindAction createSendMailBindAction(IEventHandler handler, IData data, String dataPath)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.CurrentUserPerformer);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IUnbindAction createSendMailUnbindAction(IEventHandler handler, IData data, String dataPath)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.CurrentUserPerformer);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IEventAction createSendMailAction(IEventHandler handler, IModelParticipant receiver, IData data, String dataPath)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.Participant);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT, receiver.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IBindAction createSendMailBindAction(IEventHandler handler, IModelParticipant receiver, IData data, String dataPath)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.Participant);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT, receiver.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IUnbindAction createSendMailUnbindAction(IEventHandler handler, IModelParticipant receiver, IData data, String dataPath)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.Participant);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT, receiver.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT, data.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT, dataPath);
      return result;
   }

   public IEventAction createSendMailAction(IEventHandler handler, IModelParticipant receiver,
         String messageText)
   {
      String id = "action" + ++actionId;
      String name = id.toUpperCase();
      IEventAction result = handler.createEventAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.Participant);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT, receiver.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IBindAction createSendMailBindAction(IEventHandler handler, IModelParticipant receiver, String messageText)
   {
      String id = "bindaction" + ++bindActionId;
      String name = id.toUpperCase();
      IBindAction result = handler.createBindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.Participant);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT, receiver.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IUnbindAction createSendMailUnbindAction(IEventHandler handler, IModelParticipant receiver, String messageText)
   {
      String id = "unbindaction" + ++unbindActionId;
      String name = id.toUpperCase();
      IUnbindAction result = handler.createUnbindAction(id, name, ((IModel) handler.getModel()).findEventActionType(
            PredefinedConstants.MAIL_ACTION), 0);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT, ReceiverType.Participant);
      result.setAttribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT, receiver.getId());
      result.setAttribute(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT, messageText);
      return result;
   }

   public IDataPath createDataPath(IProcessDefinition process, String id, IData data,
         String path, Direction direction)
   {
      return process.createDataPath(id, id.toUpperCase(), data, path, direction, 0);
   }
   
   public ITrigger createManualTrigger(IProcessDefinition process, IModelParticipant participant)
   {
      String id = "trigger" + ++triggerId;
      String name = id.toUpperCase();
      ITrigger trigger = process.createTrigger(id, name, ((IModel) process.getModel()).
            findTriggerType(PredefinedConstants.MANUAL_TRIGGER), 0);
      
      trigger.setAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT, participant
            .getId());

      return trigger;
   }

   public ITrigger createTimerTrigger(IProcessDefinition process, Date startTime)
   {
      String id = "trigger" + ++triggerId;
      String name = id.toUpperCase();
      ITrigger trigger = process.createTrigger(id, name, ((IModel) process.getModel()).
            findTriggerType(PredefinedConstants.TIMER_TRIGGER), 0);

      if (startTime != null)
      {
         trigger.setAttribute(PredefinedConstants.TIMER_TRIGGER_START_TIMESTAMP_ATT,
            new Long(startTime.getTime()));
      }

      return trigger;
   }

   public ITrigger createTimerTrigger(IProcessDefinition process, Date startTime,
         Date stopTime, Period periodicity)
   {
      ITrigger trigger = createTimerTrigger(process, startTime);
      
      trigger.setAttribute(PredefinedConstants.TIMER_TRIGGER_PERIODICITY_ATT, periodicity);
      if (stopTime != null)
      {
         trigger.setAttribute(PredefinedConstants.TIMER_TRIGGER_STOP_TIMESTAMP_ATT, new Long(
            stopTime.getTime()));
      }

      return trigger;
   }

   public ITrigger createMailTrigger(IProcessDefinition process, String user,
         String password, String host, String selector, MailProtocol protocol)
   {
      String id = "trigger" + ++triggerId;
      String name = id.toUpperCase();
      ITrigger result = process.createTrigger(id, name, ((IModel) process.getModel()).
            findTriggerType(PredefinedConstants.MAIL_TRIGGER), 0);
      result.setAttribute(PredefinedConstants.MAIL_TRIGGER_USER_ATT, user);
      result.setAttribute(PredefinedConstants.MAIL_TRIGGER_PASSWORD_ATT, password);
      result.setAttribute(PredefinedConstants.MAIL_TRIGGER_SERVER_ATT, host);
      result.setAttribute(PredefinedConstants.MAIL_TRIGGER_PREDICATE_BODY_ATT, selector);
      result.setAttribute(PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT, protocol);

      // should be moved to some TriggerAccessPointProvider (see MailTriggerPanel)
      if (!result.getAllAccessPoints().hasNext())
      {
         result.addToPersistentAccessPoints(new AccessPointTemplate(
            JavaDataTypeUtils
               .createIntrinsicAccessPoint(MAIL_PARAMETER,
                     MAIL_PARAMETER, Mail.class.getName(),
                     Direction.OUT, false, null)));
      }

      return result;
   }

   public IParameterMapping createParameterMapping(ITrigger trigger,
         String parameter, String parameterPath, IData data)
   {
      return trigger.createParameterMapping(data, null, parameter, parameterPath, 0);
   }
   
   public static class Producer
   {
      public String create()
      {
         String result = "<a>\n" + "  <b>\n" + "    <c>abcd</c>\n" + "    <d>345</d>\n"
               + "    <e>3.14</e>\n" + "  </b>\n" + "</a>";

         trace.debug("in method: create()");
         trace.debug("generated XML String: " + result);

         return result;
      }
   }
/*   
   public static void main(String[] args)
   {
      ModelBuilder builder = new DefaultModelBuilder();
      IModel model = builder.createModel("plainXMLdata");

      IProcessDefinition p1 = builder.createProcessDefinition(model, "p1");

      IData xml = builder.createPlainXMLData(model, "xml", "xml");

      IApplication app1 = builder.createPlainJavaApplication(model, Producer.class
            .getName(), "Producer()", "create()");

      IActivity a1 = builder.createApplicationActivity(p1, app1);

      builder.createNoninteractiveMapping(a1, Direction.OUT, "returnValue", null, xml,
            null);

      builder.createDataPath(p1, "string", xml, "string(/a/b/c)", Direction.IN);
      builder.createDataPath(p1, "double", xml, "number(/a/b/e)", Direction.IN);
      builder.createDataPath(p1, "bool", xml, "boolean(/a/b/c)", Direction.IN);

      builder.createDataPath(p1, "string", xml, "/a/b/c", Direction.OUT);
      builder.createDataPath(p1, "double", xml, "/a/b/e", Direction.OUT);

      IProcessDefinition p2 = builder.createProcessDefinition(model, "p2");
      IActivity a2 = builder.createApplicationActivity(p2, app1);
      builder.createNoninteractiveMapping(a2, Direction.OUT, "returnValue", null, xml,
            null);
      a2.setSplitType(JoinSplitType.And);

      IActivity a3 = builder.createRouteActivity(p2);
      builder.createTransition(a2, a3, "xml.string(/a/b/c)=\"abcd\"");

      IActivity a4 = builder.createRouteActivity(p2);
      builder.createTransition(a2, a4, "xml.string(/a/b/c)=\"wrong value\"");

      IActivity a5 = builder.createRouteActivity(p2);
      builder.createTransition(a2, a5, "xml.boolean(/a/b/c)=true");

      IActivity a6 = builder.createRouteActivity(p2);
      builder.createTransition(a2, a6, "xml.boolean(/a/b/c)=false");

      IActivity a7 = builder.createRouteActivity(p2);
      builder.createTransition(a2, a7, "xml.number(/a/b/e)=3.14");

      IActivity a8 = builder.createRouteActivity(p2);
      builder.createTransition(a2, a8, "xml.number(/a/b/e)=4444555");

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      new DefaultXMLWriter(true).exportAsXML(model, out);
      String modelXml = out.toString();
      
      System.out.println(modelXml);
   }
*/
}