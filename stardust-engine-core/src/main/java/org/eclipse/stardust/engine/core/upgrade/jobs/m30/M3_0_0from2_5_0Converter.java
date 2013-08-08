/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradableItem;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


// @todo (france, ub):
/*
- new access point format
- last minute removal of predefined elements reference

/**
 * @author ubirkemeyer
 * @version $Revision$
 */

public class M3_0_0from2_5_0Converter extends ModelUpgradeJob
{
   private static final Version CURRENT_VERSION = Version.createFixedVersion(3, 0, 0);

   private static final String MODEL_ENCODING = "ISO-8859-1";
   private static final String MODEL_DTD = "WorkflowModel.dtd";
   private static final String PULL_CONDITION = "pull";
   private static final String ENGINE_CONDITION = "engine";
   private static final String PUSH_CONDITION = "push";

   public UpgradableItem run(UpgradableItem item, boolean recover)
   {
      String modelLocation = ((ModelItem) item).getModel();
      InputSource inputSource = new InputSource(new StringReader(modelLocation));
      URL dtd = ModelItem.class.getResource(MODEL_DTD);
      inputSource.setSystemId(dtd.toString());
      DocumentBuilder domBuilder = XmlUtils.newDomBuilder(true);

      Document source;
      try
      {
         source = domBuilder.parse(inputSource);
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }

      Model model = new V25Reader().read(source);
      addPredefinedConstants(model);
      migrate4EyesHandling(model);
      Document result = new V30Writer().write(model);

      StringWriter writer = new StringWriter();
      XmlUtils.serialize(result, new StreamResult(writer), MODEL_ENCODING, 2, null,
            MODEL_DTD);
      return new ModelItem(writer.getBuffer().toString());
   }

   private void migrate4EyesHandling(Model model)
   {
      // convert legacy 4eyes datamapping
      for (Iterator i = model.getAllProcessDefinitions(); i.hasNext(); )
      {
         final ProcessDefinition process = (ProcessDefinition) i.next();
         for (Iterator j = process.getAllActivities(); j.hasNext();)
         {
            EventHandler handler4eyes = null;

            final Activity activity = (Activity) j.next();
            for (Iterator k = activity.getAllDataMappings(); k.hasNext();)
            {
               DataMapping mapping = (DataMapping) k.next();
               if ("IN".equalsIgnoreCase(mapping.getDirection())
                     && V25Reader.ENGINE_CONTEXT.equals(mapping.getContext())
                     && V25Reader.EXCLUDED_PERFORMER_ACCESSPOINT.equals(mapping
                           .getApplicationAccessPointId()))
               {
                  if (null == handler4eyes)
                  {
                     handler4eyes = activity.createEventHandler("4eyes",
                           PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, 0);
                     handler4eyes.setName(handler4eyes.getId());
                     handler4eyes.setAutobind(false);
                     handler4eyes.setUnbindOnMatch(false);
                  }

                  final EventAction action = handler4eyes.createEventAction(
                        PredefinedConstants.EXCLUDE_USER_ACTION, mapping.getId(), mapping
                              .getId());
                  
                  action.setAttribute(PredefinedConstants.EXCLUDED_PERFORMER_DATA, mapping
                        .getDataID());
                  action.setAttribute(PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH,
                        mapping.getDataPath());
                  
                  k.remove();
               }
            }
         }
      }
   }

   // @todo (france, ub): last minute check to keep this in sync with the main line
   private void addPredefinedConstants(Model model)
   {
      DataType primitive = model.createDataType(PredefinedConstants.PRIMITIVE_DATA,
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

      DataType serializable = model.createDataType(PredefinedConstants.SERIALIZABLE_DATA,
            "Serializable", true, 0);
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

      DataType entity = model.createDataType(PredefinedConstants.ENTITY_BEAN_DATA,
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

      DataType xml = model.createDataType(PredefinedConstants.PLAIN_XML_DATA,
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

      ApplicationType sbType = model.createApplicationType(
            PredefinedConstants.SESSIONBEAN_APPLICATION,
            "Session Bean Application", true, true, 0);
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

      ApplicationType javaType = model.createApplicationType(
            PredefinedConstants.PLAINJAVA_APPLICATION,
            "Plain Java Application", true, true, 0);
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

      ApplicationType jmsType = model.createApplicationType(
            PredefinedConstants.JMS_APPLICATION, "JMS Application",
            true, false, 0);
      jmsType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.JMS_APPLICATION_VALIDATOR_CLASS);
      jmsType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.JMS_APPLICATION_PANEL_CLASS);
      jmsType.setAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT,
            PredefinedConstants.JMS_APPLICATION_INSTANCE_CLASS);
      jmsType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.JMS_APPLICATION_ICON_LOCATION);

      model.createApplicationContextType(PredefinedConstants.DEFAULT_CONTEXT,
            "Default Context", true, true, false, 0);

      model.createApplicationContextType(PredefinedConstants.ENGINE_CONTEXT, "Engine Context",
            true, false, true, 0);

      model.createApplicationContextType(PredefinedConstants.APPLICATION_CONTEXT,
            "Noninteractive Application Context",
            true, false, true, 0);

      ApplicationContextType jfcType = model.createApplicationContextType(
            PredefinedConstants.JFC_CONTEXT, "JFC Application",
            true, false, true, 0);
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

      ApplicationContextType jspType = model.createApplicationContextType(
            PredefinedConstants.JSP_CONTEXT, "JSP Application",
            true, true, false, 0);
      jspType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.JSP_CONTEXT_VALIDATOR_CLASS);
      jspType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.JSP_CONTEXT_PANEL_CLASS);
      jspType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.JSP_CONTEXT_ICON_LOCATION);

      TriggerType manualTrigger = model.createTriggerType(
            PredefinedConstants.MANUAL_TRIGGER,
            "Manual Trigger", true, false, 0);
      manualTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.MANUAL_TRIGGER_PANEL_CLASS);
      manualTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.MANUAL_TRIGGER_VALIDATOR_CLASS);
      manualTrigger.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.MANUAL_TRIGGER_ICON_LOCATION);

      TriggerType jmsTrigger = model.createTriggerType(PredefinedConstants.JMS_TRIGGER,
            "JMS Trigger", true, false, 0);
      jmsTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.JMS_TRIGGER_PANEL_CLASS);
      jmsTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.JMS_TRIGGER_VALIDATOR_CLASS);
      jmsTrigger.setAttribute(PredefinedConstants.ACCEPTOR_CLASS_ATT,
            PredefinedConstants.JMS_TRIGGER_MESSAGEACCEPTOR_CLASS);
      jmsTrigger.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.JMS_TRIGGER_ICON_LOCATION);

      TriggerType mailTrigger = model.createTriggerType(PredefinedConstants.MAIL_TRIGGER,
            "Mail Trigger", true, true, 0);
      mailTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.MAIL_TRIGGER_PANEL_CLASS);
      mailTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.MAIL_TRIGGER_VALIDATOR_CLASS);
      mailTrigger.setAttribute(PredefinedConstants.PULL_TRIGGER_EVALUATOR_ATT,
            PredefinedConstants.MAIL_TRIGGER_EVALUATOR_CLASS);
      mailTrigger.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.MAIL_TRIGGER_ICON_LOCATION);

      TriggerType timerTrigger = model.createTriggerType(
            PredefinedConstants.TIMER_TRIGGER,
            "Timer Based Trigger", true, true, 0);
      timerTrigger.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.TIMER_TRIGGER_PANEL_CLASS);
      timerTrigger.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.TIMER_TRIGGER_VALIDATOR_CLASS);
      timerTrigger.setAttribute(PredefinedConstants.PULL_TRIGGER_EVALUATOR_ATT,
            PredefinedConstants.TIMER_TRIGGER_EVALUATOR_CLASS);
      timerTrigger.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.TIMER_TRIGGER_ICON_LOCATION);

      EventConditionType timerCondition = model.createEventConditionType(
            PredefinedConstants.TIMER_CONDITION, "Timer",
            PULL_CONDITION, true, true, true, 0);
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

      EventConditionType exceptionCondition = model.createEventConditionType(
            PredefinedConstants.EXCEPTION_CONDITION, "On Exception",
            ENGINE_CONDITION, false, true, true, 0);
      exceptionCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.EXCEPTION_CONDITION_PANEL_CLASS);
      exceptionCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.EXCEPTION_CONDITION_VALIDATOR_CLASS);
      exceptionCondition.setAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT,
            PredefinedConstants.EXCEPTION_CONDITION_ACCESS_POINT_PROVIDER_CLASS);
      exceptionCondition.setAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
            PredefinedConstants.EXCEPTION_CONDITION_RULE_CLASS);
      exceptionCondition.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.EXCEPTION_CONDITION_ICON_LOCATION);

      EventConditionType activityStatechangeCondition = model.createEventConditionType(
            PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION, "On Activity State Change",
            ENGINE_CONDITION, false, true, true, 0);
      activityStatechangeCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION_PANEL_CLASS);
      activityStatechangeCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.STATECHANGE_CONDITION_VALIDATOR_CLASS);
      activityStatechangeCondition.setAttribute(
            PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
            PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION_RULE_CLASS);
      activityStatechangeCondition.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION_ICON_LOCATION);

      EventConditionType processStatechangeCondition = model.createEventConditionType(
            PredefinedConstants.PROCESS_STATECHANGE_CONDITION, "On Process State Change",
            ENGINE_CONDITION, true, false, true, 0);
      processStatechangeCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.PROCESS_STATECHANGE_CONDITION_PANEL_CLASS);
      processStatechangeCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.STATECHANGE_CONDITION_VALIDATOR_CLASS);
      processStatechangeCondition.setAttribute(
            PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
            PredefinedConstants.PROCESS_STATECHANGE_CONDITION_RULE_CLASS);
      processStatechangeCondition.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.PROCESS_STATECHANGE_CONDITION_ICON_LOCATION);

      EventConditionType onAssignmentCondition = model.createEventConditionType(
            PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, "On Assignment",
            ENGINE_CONDITION, false, true, true, 0);
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

/*      
      EventConditionType expressionCondition = model.createEventConditionType(
            PredefinedConstants.EXPRESSION_CONDITION, "On Data Change",
            ENGINE_CONDITION, true, true, true, 0);
      expressionCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.EXPRESSION_CONDITION_PANEL_CLASS);
      expressionCondition.setAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
            PredefinedConstants.EXPRESSION_CONDITION_CLASS);
      expressionCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.EXPRESSION_CONDITION_VALIDATOR_CLASS);
      expressionCondition.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.EXPRESSION_CONDITION_ICON_LOCATION);
*/

/*      
      EventConditionType externalCondition = model.createEventConditionType(
            PredefinedConstants.EXTERNAL_EVENT_CONDITION, "External Event", PUSH_CONDITION,
            true, true, true, 0);
      externalCondition.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.EXTERNAL_CONDITION_CLASS);
      externalCondition.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.EXTERNAL_CONDITION_ICON_LOCATION);
*/
      
      EventActionType triggerActionType = model.createEventActionType(
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

      EventActionType mailActionType = model.createEventActionType(
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

      EventActionType abortActionType = model.createEventActionType(
            PredefinedConstants.ABORT_PROCESS_ACTION, "Abort Process", true, true, true,
            0);
      abortActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.ABORT_PROCESS_ACTION_CLASS);
      abortActionType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.ABORT_PROCESS_ACTION_ICON_LOCATION);
      Set unsupportedAbortConditions = new HashSet();
      unsupportedAbortConditions.add(activityStatechangeCondition.getId());
      unsupportedAbortConditions.add(processStatechangeCondition.getId());
      unsupportedAbortConditions.add(onAssignmentCondition.getId());
      supportAllConditionTypes(model, abortActionType, unsupportedAbortConditions);

      EventActionType completeActionType = model.createEventActionType(
            PredefinedConstants.COMPLETE_ACTIVITY_ACTION, "Complete Activity", true,
            false, true, 0);
      completeActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.COMPLETE_ACTIVITY_ACTION_CLASS);
      completeActionType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.COMPLETE_ACTIVITY_ACTION_ICON_LOCATION);
      Set unsupportedCompleteConditions = new HashSet();
      unsupportedCompleteConditions.add(activityStatechangeCondition.getId());
      unsupportedCompleteConditions.add(processStatechangeCondition.getId());
      unsupportedCompleteConditions.add(onAssignmentCondition.getId());
      supportAllActivityConditionTypes(model, completeActionType,
            unsupportedCompleteConditions);

      EventActionType activateActionType = model.createEventActionType(
            PredefinedConstants.ACTIVATE_ACTIVITY_ACTION,
            "Activate Activity", true, false, true, 0);
      activateActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.ACTIVATE_ACTIVITY_ACTION_CLASS);
      activateActionType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.ACTIVATE_ACTIVITY_ACTION_ICON_LOCATION);
      Set unsupportedActivateConditions = new HashSet();
      unsupportedActivateConditions.add(activityStatechangeCondition.getId());
      unsupportedActivateConditions.add(processStatechangeCondition.getId());
      supportAllActivityConditionTypes(model, activateActionType,
            unsupportedActivateConditions);

      EventActionType delegateActionType = model.createEventActionType(
            PredefinedConstants.DELEGATE_ACTIVITY_ACTION, "Delegate Activity",
            true, false, true, 0);
      delegateActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.DELEGATE_ACTIVITY_ACTION_CLASS);
      delegateActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.DELEGATE_ACTIVITY_PANEL_CLASS);
      delegateActionType.setAttribute(PredefinedConstants.RUNTIME_PANEL_ATT,
            PredefinedConstants.DELEGATE_ACTIVITY_RUNTIME_PANEL_CLASS);
      delegateActionType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.DELEGATE_ACTIVITY_ACTION_ICON_LOCATION);

      EventActionType scheduleActionType = model.createEventActionType(
            PredefinedConstants.SCHEDULE_ACTIVITY_ACTION, "Schedule Activity",
            true, false, true, 0);
      scheduleActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.SCHEDULE_ACTIVITY_ACTION_CLASS);
      scheduleActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.SCHEDULE_ACTIVITY_PANEL_CLASS);
      scheduleActionType.setAttribute(PredefinedConstants.RUNTIME_PANEL_ATT,
            PredefinedConstants.SCHEDULE_ACTIVITY_RUNTIME_PANEL_CLASS);
      scheduleActionType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.SCHEDULE_ACTIVITY_ACTION_ICON_LOCATION);

      Set exc = new HashSet();
      exc.add(onAssignmentCondition.getId());
      supportAllActivityConditionTypes(model, delegateActionType, exc);
      supportAllActivityConditionTypes(model, scheduleActionType, exc);

      EventActionType excludeUserActionType = model.createEventActionType(
            PredefinedConstants.EXCLUDE_USER_ACTION, "Exclude User",
            true, false, true, 0);
      excludeUserActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.EXCLUDE_USER_ACTION_CLASS);
      excludeUserActionType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.EXCLUDE_USER_ACTION_VALIDATOR_CLASS);
      excludeUserActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
            PredefinedConstants.EXCLUDE_USER_PANEL_CLASS);
      excludeUserActionType.setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.EXCLUDE_USER_ACTION_ICON_LOCATION);
      excludeUserActionType.addSupportedConditionType(onAssignmentCondition.getId());

      EventActionType setdataActionType = model.createEventActionType(
            PredefinedConstants.SET_DATA_ACTION, "Set Data",
            true, true, true, 0);
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

   private void supportAllActivityConditionTypes(Model model,
            EventActionType actionType, Set exceptions)
      {
         for (Iterator i = model.getAllEventConditionTypes(); i.hasNext();)
         {
            EventConditionType type = (EventConditionType)i.next();
            if (type.isActivityCondition()
                  && !(exceptions != null && exceptions.contains(type.getId())))
            {
               actionType.addSupportedConditionType(type.getId());
            }
         }
      }

   private void supportAllConditionTypes(Model model, EventActionType actionType)
   {
      supportAllConditionTypes(model, actionType, Collections.EMPTY_SET);
   }
   
   private void supportAllConditionTypes(Model model, EventActionType actionType,
         Set exceptions)
   {
      for (Iterator i = model.getAllEventConditionTypes(); i.hasNext();)
      {
         EventConditionType type = (EventConditionType)i.next();
         if ((null == exceptions) || !exceptions.contains(type.getId()))
         {
            actionType.addSupportedConditionType(type.getId());
         }
      }
   }

   public Version getVersion()
   {
      return CURRENT_VERSION;
   }

}
