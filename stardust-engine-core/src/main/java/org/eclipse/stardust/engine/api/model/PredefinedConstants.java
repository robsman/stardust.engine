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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A collection of constants containing predefined IDs and attribute names.
 *
 * @author purang
 */
public final class PredefinedConstants
{
   // attribute scopes
   public static final String MODEL_SCOPE = "carnot:model:";
   public static final String XPDL_SCOPE = "carnot:model:xpdl:";
   public static final String DD_SCOPE = "carnot:defdesk:"; // TODO: remove
   public static final String ED_SCOPE = "carnot:exdesk:";
   public static final String WEBEX_SCOPE = "carnot:webex:";
   public static final String ENGINE_SCOPE = "carnot:engine:";
   public static final String PWH_SCOPE = "carnot:pwh:";

   // predefined application type IDs
   public static final String SESSIONBEAN_APPLICATION = "sessionBean";
   public static final String PLAINJAVA_APPLICATION = "plainJava";
   public static final String JMS_APPLICATION = "jms";
   public static final String SPRINGBEAN_APPLICATION = "springBean";
   public static final String WS_APPLICATION = "webservice";

   // retry synchronous application
   public static final String SYNCHRONOUS_APPLICATION_RETRY_ENABLE = "synchronous:retry:enable";
   public static final String SYNCHRONOUS_APPLICATION_RETRY_NUMBER = "synchronous:retry:number";
   public static final String SYNCHRONOUS_APPLICATION_RETRY_TIME = "synchronous:retry:time";

   // predefined invocation types for process interface usage
   public static final String PROCESSINTERFACE_INVOCATION_SOAP = "SOAP";
   public static final String PROCESSINTERFACE_INVOCATION_REST = "REST";
   public static final String PROCESSINTERFACE_INVOCATION_BOTH = "BOTH";
   public static final String PROCESSINTERFACE_INVOCATION_TYPE = ENGINE_SCOPE + "externalInvocationType";

   // predefined data type IDs
   public static final String PRIMITIVE_DATA = "primitive";
   public static final String SERIALIZABLE_DATA = "serializable";
   public static final String ENTITY_BEAN_DATA = "entity";
   public static final String PLAIN_XML_DATA = "plainXML";
   public static final String HIBERNATE_DATA = "hibernate";
   public static final String STRUCTURED_DATA = "struct";
   // DMS
   public static final String DMS_DOCUMENT_DATA = "dms-document"; //$NON-NLS-1$
   public static final String DMS_DOCUMENT_SET_DATA = "dms-document-set"; //$NON-NLS-1$
   public static final String DOCUMENT_DATA = "dmsDocument"; //$NON-NLS-1$
   public static final String DOCUMENT_LIST_DATA = "dmsDocumentList"; //$NON-NLS-1$
   public static final String FOLDER_DATA = "dmsFolder";    //$NON-NLS-1$

   // predefined context type IDs
   public static final String DEFAULT_CONTEXT = "default";
   public static final String ENGINE_CONTEXT = "engine";
   public static final String JFC_CONTEXT = "jfc";
   public static final String JSP_CONTEXT = "jsp";
   public static final String JSF_CONTEXT = "jsf";
   public static final String APPLICATION_CONTEXT = "application";
   public static final String PROCESSINTERFACE_CONTEXT = "processInterface";
   public static final String EXTERNALWEBAPP_CONTEXT = "externalWebApp";
   public static final String EVENT_CONTEXT = "event-";

   // predefined action type Ids
   public static final String MAIL_ACTION = "mail";
   public static final String TRIGGER_ACTION = "trigger";
   public static final String EXCEPTION_ACTION = "exception";
   public static final String DELEGATE_ACTIVITY_ACTION = "delegateActivity";
   public static final String SCHEDULE_ACTIVITY_ACTION = "scheduleActivity";
   public static final String ABORT_PROCESS_ACTION = "abortProcess";
   public static final String ABORT_ACTIVITY_ACTION = "abortActivity";
   public static final String COMPLETE_ACTIVITY_ACTION = "completeActivity";
   public static final String ACTIVATE_ACTIVITY_ACTION = "activateActivity";
   public static final String SET_DATA_ACTION = "setData";
   public static final String EXCLUDE_USER_ACTION = "excludeUser";

   // predefined trigger type IDs
   public static final String MANUAL_TRIGGER = "manual";
   public static final String SCAN_TRIGGER = "scan";
   public static final String MAIL_TRIGGER = "mail";
   public static final String JMS_TRIGGER = "jms";
   public static final String SIGNAL_TRIGGER = "signal";
   public static final String TIMER_TRIGGER = "timer";

   // predefined condition type IDs
   public static final String TIMER_CONDITION = "timer";
   public static final String ACTIVITY_ON_ASSIGNMENT_CONDITION = "onAssignment";
   public static final String ESCALATION_CONDITION = "escalation";
   public static final String EXPRESSION_CONDITION = "expression";
   public static final String EXCEPTION_CONDITION = "exception";
   public static final String ACTIVITY_STATECHANGE_CONDITION = "statechange";
   public static final String PROCESS_STATECHANGE_CONDITION = "processStatechange";
   public static final String EXTERNAL_EVENT_CONDITION = "external";
   public static final String OBSERVER_EVENT_CONDITION = "observer";
   public static final String SUBPROCESS_EXCEPTION = "subprocessException";
   public static final String SIGNAL_CONDITION = "signal";

   // predefined data
   public static final String LAST_ACTIVITY_PERFORMER = "LAST_ACTIVITY_PERFORMER";
   public static final String STARTING_USER = "STARTING_USER";
   public static final String CURRENT_USER = "CURRENT_USER";
   public static final String PROCESS_ID = "PROCESS_ID";
   public static final String PROCESS_PRIORITY = "PROCESS_PRIORITY";
   public static final String ROOT_PROCESS_ID = "ROOT_PROCESS_ID";
   public static final String CURRENT_DATE = "CURRENT_DATE";
   public static final String CURRENT_LOCALE = "CURRENT_LOCALE";
   public static final String CURRENT_MODEL = "CURRENT_MODEL";

   // predefined role and user
   public static final String ADMINISTRATOR_ROLE = "Administrator";
   public static final String MOTU = "motu";
   public static final String MOTU_FIRST_NAME = "Master";
   public static final String MOTU_LAST_NAME = "Of the Universe";

   public static final String SYSTEM = "system_carnot_engine";
   public static final String SYSTEM_FIRST_NAME = SYSTEM;
   public static final String SYSTEM_LAST_NAME = SYSTEM;

   public static final String SYSTEM_REALM = SYSTEM;

   // predefined transition
   public static final String RELOCATION_TRANSITION_ID = "__internal_relocate_transition__";

   // predefined default partition
   public static final String DEFAULT_PARTITION_ID = "default";

   // predefined default user realm
   public static final String DEFAULT_REALM_ID = "carnot";
   public static final String DEFAULT_REALM_NAME = "CARNOT";

   public static final String ACTIVITY_IS_AUXILIARY_ATT = "isAuxiliaryActivity";
   public static final String PROCESS_IS_AUXILIARY_ATT = "isAuxiliaryProcess";

   // Forward and Rewind
   public static final String ACTIVITY_IS_RELOCATE_SOURCE_ATT = ENGINE_SCOPE + "relocate:source";
   public static final String ACTIVITY_IS_RELOCATE_TARGET_ATT = ENGINE_SCOPE + "relocate:target";

   // Multi-Instance
   public static final String ACTIVITY_MI_BATCH_SIZE_ATT = ENGINE_SCOPE + "mi:size";

   // Quality Control
   public static final String ACTIVITY_IS_QUALITY_ASSURANCE_ATT = "isQualityControlActivity";
   public static final String QUALITY_ASSURANCE_PROBABILITY_ATT = "qualityControlProbability";
   public static final String QUALITY_ASSURANCE_FORMULA_ATT = "qualityControlFormula";

   // model attribute names
   public static final String XPDL_EXTENDED_ATTRIBUTES = XPDL_SCOPE + "extendedAttributes";

   // engine attribute names
   public static final String ACCEPTOR_CLASS_ATT = ENGINE_SCOPE + "jmsAcceptor";

   public static final String CLASS_NAME_ATT = ENGINE_SCOPE + "className";
   public static final String AUTO_INSTANTIATE_ATT = ENGINE_SCOPE + "autoInstantiate";
   public static final String PRIMARY_KEY_ATT = ENGINE_SCOPE + "primaryKey";
   public static final String IS_LOCAL_ATT = ENGINE_SCOPE + "isLocal";
   public static final String JNDI_PATH_ATT = ENGINE_SCOPE + "jndiPath";
   public static final String ASYNCHRONOUS_ATT = ENGINE_SCOPE + "asynchronous";

   public static final String HOME_INTERFACE_ATT = ENGINE_SCOPE + "homeInterface";
   public static final String REMOTE_INTERFACE_ATT = ENGINE_SCOPE + "remoteInterface";
   public static final String METHOD_NAME_ATT = ENGINE_SCOPE + "methodName";
   public static final String CREATE_METHOD_NAME_ATT = ENGINE_SCOPE + "createMethodName";

   public static final String BINDING_ATT = ENGINE_SCOPE + "bound";
   public static final String BINDING_DATA_ID_ATT = ENGINE_SCOPE + "dataId";
   public static final String BINDING_DATA_PATH_ATT = ENGINE_SCOPE + "dataPath";

   public static final String CONDITIONAL_PERFORMER_KIND = ENGINE_SCOPE + "conditionalPerformer:kind";
   public static final String CONDITIONAL_PERFORMER_KIND_USER = "user";
   public static final String CONDITIONAL_PERFORMER_KIND_MODEL_PARTICIPANT = "modelParticipant";
   public static final String CONDITIONAL_PERFORMER_KIND_USER_GROUP = "userGroup";
   public static final String CONDITIONAL_PERFORMER_KIND_MODEL_PARTICIPANT_OR_USER_GROUP = "modelParticipantOrUserGroup";

   public static final String CONDITIONAL_PERFORMER_REALM_DATA = ENGINE_SCOPE + "conditionalPerformer:realmData";
   public static final String CONDITIONAL_PERFORMER_REALM_DATA_PATH = ENGINE_SCOPE + "conditionalPerformer:realmDataPath";

   public static final String MODELELEMENT_VISIBILITY = ENGINE_SCOPE + "visibility";

   // common for generic WebService application types.
   public static final String AUTHENTICATION_ATT = ENGINE_SCOPE + "wsAuthentication";
   public static final String BASIC_AUTHENTICATION = "basic";
   public static final String WS_SECURITY_AUTHENTICATION = "ws-security";
   public static final String AUTHENTICATION_VARIANT_ATT = ENGINE_SCOPE + "wsAuthenticationVariant";
   public static final String WS_SECURITY_VARIANT_PASSWORD_TEXT = "passwordText";
   public static final String WS_SECURITY_VARIANT_PASSWORD_DIGEST = "passwordDigest";
   public static final String AUTHENTICATION_ID = ENGINE_SCOPE + "authentication";

   public static final String ENDPOINT_REFERENCE_ID = ENGINE_SCOPE + "endpointReference";
   public static final String WS_ENDPOINT_REFERENCE_ID = ENGINE_SCOPE + "endpointReference";   // deprecated
   public static final String WS_AUTHENTICATION_ID = ENGINE_SCOPE + "authentication"; // deprecated

   public static final String WS_WSDL_URL_ATT = ENGINE_SCOPE + "wsdlUrl";
   public static final String WS_SERVICE_NAME_ATT = ENGINE_SCOPE + "wsServiceName";
   public static final String WS_PORT_NAME_ATT = ENGINE_SCOPE + "wsPortName";
   public static final String WS_OPERATION_NAME_ATT = ENGINE_SCOPE + "wsOperationName";
   public static final String WS_OPERATION_STYLE_ATT = ENGINE_SCOPE + "wsStyle";
   public static final String WS_OPERATION_USE_ATT = ENGINE_SCOPE + "wsUse";
   public static final String WS_IMPLEMENTATION_ATT = ENGINE_SCOPE + "wsImplementation";
   public static final String WS_GENERIC_IMPLEMENTATION = "generic";
   public static final String WS_CARNOT_IMPLEMENTATION = "carnot";
   public static final String WS_MAPPING_ATTR_PREFIX = ENGINE_SCOPE + "mapping:";
   public static final String WS_TEMPLATE_ATTR_PREFIX = ENGINE_SCOPE + "template:";
   public static final String WS_SERVICE_ENDPOINT_ATT = ENGINE_SCOPE + "wsEndpointAddress";
   public static final String WS_SOAP_ACTION_ATT = ENGINE_SCOPE + "wsSoapAction";
   public static final String WS_PARAMETER_NAME_ATT = ENGINE_SCOPE + "wsParameterName:";
   public static final String WS_PARAMETER_TYPE_ATT = ENGINE_SCOPE + "wsParameterType:";
   public static final String WS_PARAMETER_STYLE_ATT = ENGINE_SCOPE + "wsParameterStyle:";
   public static final String WS_RESULT_SUFFIX = "result";
   public static final String WS_FAULT_NAME_ATT = ENGINE_SCOPE + "wsFaultName:";
   public static final String WS_FAULT_TYPE_ATT = ENGINE_SCOPE + "wsFaultType:";
   public static final String WS_FAULT_STYLE_ATT = ENGINE_SCOPE + "wsFaultStyle:";

   public static final String FLAVOR_ATT = ENGINE_SCOPE + "flavor";
   public static final String BROWSABLE_ATT = ENGINE_SCOPE + "browsable";

   public static final String TARGET_TIMESTAMP_ATT = ENGINE_SCOPE + "targetTime";
   public static final String WORKFLOW_EXPRESSION_ATT = ENGINE_SCOPE + "expression";
   public static final String TRIGGER_ACTION_PROCESS_ATT = ENGINE_SCOPE + "processDefinition";

   public static final String PARTICIPANT_ATT = ENGINE_SCOPE + "participant";
   public static final String MANUAL_TRIGGER_PARTICIPANT_ATT = PARTICIPANT_ATT;
   public static final String TIMER_PERIOD_ATT = ENGINE_SCOPE + "period";

   public static final String ABORT_ACTION_SCOPE_ATT = ENGINE_SCOPE + "abort:scope";

   public static final String MAIL_ACTION_RECEIVER_TYPE_ATT = ENGINE_SCOPE + "receiverType";
   public static final String MAIL_ACTION_RECEIVER_ATT = ENGINE_SCOPE + "receiver";
   public static final String MAIL_ACTION_ADDRESS_ATT = ENGINE_SCOPE + "emailAddress";
   public static final String MAIL_ACTION_BODY_TEMPLATE_ATT = ENGINE_SCOPE + "mailBodyTemplate";
   public static final String MAIL_ACTION_BODY_DATA_ATT = ENGINE_SCOPE + "mailBodyData";
   public static final String MAIL_ACTION_BODY_DATA_PATH_ATT = ENGINE_SCOPE + "mailBodyDataPath";
   public static final String MAIL_ACTION_SUBJECT = ENGINE_SCOPE + "mailSubject";

   public static final String TARGET_STATE_ATT = ENGINE_SCOPE + "targetState";
   public static final String TARGET_WORKLIST_ATT = ENGINE_SCOPE + "targetWorklist";
   public static final String TARGET_PARTICIPANT_ATT = ENGINE_SCOPE + "target";
   public static final String PULL_EVENT_EMITTER_ATT = ENGINE_SCOPE + "pullEventEmitter";
   public static final String CONDITION_CONDITION_CLASS_ATT = ENGINE_SCOPE + "condition";
   public static final String CONDITION_BINDER_CLASS_ATT = ENGINE_SCOPE + "binder";

   public static final String ACTION_CLASS_ATT = ENGINE_SCOPE + "action";
   public static final String VALIDATOR_CLASS_ATT = ENGINE_SCOPE + "validator";
   public static final String EVALUATOR_CLASS_ATT = ENGINE_SCOPE + "evaluator";
   public static final String RUNTIME_VALIDATOR_CLASS_ATT = ENGINE_SCOPE + "runtimeValidator"; // TODO: remove
   public static final String DATA_FILTER_EXTENSION_ATT = ENGINE_SCOPE + "dataFilterExtension";
   public static final String DATA_LOADER_ATT = ENGINE_SCOPE + "dataLoader";
   public static final String ACCESSPOINT_PROVIDER_ATT = ENGINE_SCOPE + "accessPointProvider";
   public static final String APPLICATION_INSTANCE_CLASS_ATT = ENGINE_SCOPE + "applicationInstance";
   public static final String PULL_TRIGGER_EVALUATOR_ATT = ENGINE_SCOPE + "pullTriggerEvaluator";
   public static final String SET_DATA_ACTION_ATTRIBUTE_NAME_ATT = ENGINE_SCOPE + "attributeName";
   public static final String SET_DATA_ACTION_ATTRIBUTE_PATH_ATT = ENGINE_SCOPE + "attributePath";
   public static final String SET_DATA_ACTION_DATA_ID_ATT = ENGINE_SCOPE + "dataId";
   public static final String SET_DATA_ACTION_DATA_PATH_ATT = ENGINE_SCOPE + "dataPath";
   public static final String EXCEPTION_CLASS_ATT = ENGINE_SCOPE + "exceptionName";
   public static final String TYPE_ATT = ENGINE_SCOPE + "type";
   public static final String DEFAULT_VALUE_ATT = ENGINE_SCOPE + "defaultValue";

   // attributes for mail application type
   public static final String MAIL_SERVER = "mailServer";
   public static final String FROM_ADDRESS = "fromAddress";
   public static final String TO_ADDRESS = "toAddress";
   public static final String CC_ADDRESS = "ccAddress";
   public static final String BCC_ADDRESS = "bccAddress";
   public static final String MAIL_PRIORITY = "mailPriority";
   public static final String SUBJECT = "subject";
   public static final String TEMPLATE_VARIABLE = "templateVariable";
   public static final String RETURN_VALUE = "returnValue";
   public static final String RESPONSE_MAIL = "responseMail";
   public static final String ATTACHMENTS = "Attachments";
   public static final String JNDI_SESSION = "jndiSession";

   // @todo (france, ub): should be converted to a period
   public static final String TIMER_TRIGGER_START_TIMESTAMP_ATT = ENGINE_SCOPE + "startTime";
   public static final String TIMER_TRIGGER_PERIODICITY_ATT = ENGINE_SCOPE + "periodicity";
   public static final String TIMER_TRIGGER_STOP_TIMESTAMP_ATT = ENGINE_SCOPE + "stopTime";

   public static final String MAIL_TRIGGER_USER_ATT = ENGINE_SCOPE + "user";
   public static final String MAIL_TRIGGER_SERVER_ATT = ENGINE_SCOPE + "host";
   public static final String MAIL_TRIGGER_PASSWORD_ATT = ENGINE_SCOPE + "password";
   public static final String MAIL_TRIGGER_PROTOCOL_ATT = ENGINE_SCOPE + "protocol";
   public static final String MAIL_TRIGGER_FLAGS_ATT = ENGINE_SCOPE + "mailFlags";
   public static final String MAIL_TRIGGER_PREDICATE_SENDER_ATT = ENGINE_SCOPE + "mailSenderPredicate";
   public static final String MAIL_TRIGGER_PREDICATE_SUBJECT_ATT = ENGINE_SCOPE + "mailSubjectPredicate";
   public static final String MAIL_TRIGGER_PREDICATE_BODY_ATT = ENGINE_SCOPE + "selectorPredicate";
   public static final String MAIL_TRIGGER_MAILBOX_ACTION_ATT = ENGINE_SCOPE + "mailboxAction";

   public static final String EXCEPTION_ATT = ENGINE_SCOPE + "exception";

   public static final String SOURCE_STATE_ATT = ENGINE_SCOPE + "sourceState";

   public static final String OBSERVER_STATE_CHANGE_EVENT_ATT = ENGINE_SCOPE + "stateChange";
   public static final String ENDPOINT_REFERENCE_ATT = ENGINE_SCOPE + "endpointReference";
   public static final String RESULT_DATA_ATT = ENGINE_SCOPE + "resultData";
   public static final String OBSERVER_NOTIFICATION_TYPE = ENGINE_SCOPE + "notificationType";

   // exdesk attribute names
   public static final String RUNTIME_PANEL_ATT = ED_SCOPE + "runtimePanel";
   // @todo (france, ub): very questionable -->
   public static final String JFC_CONTEXT_INSTANCE_CLASS_ATT = ED_SCOPE + "instance";

   // webex attribute names
   public static final String HTML_PATH_ATT = WEBEX_SCOPE + "htmlPath";


   // defdesk attribute names
   public static final String ACCESSPATH_EDITOR_ATT = DD_SCOPE + "accessPathEditor";// TODO: remove
   public static final String PANEL_CLASS_ATT = DD_SCOPE + "panel";// TODO: remove

   // predefined implementation classes and icons
   public static final String PRIMITIVE_PANEL_CLASS = "ag.carnot.workflow.spi.providers.data.java.PrimitivePropertiesEditor";
   public static final String PRIMITIVE_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.PrimitiveAccessPathEvaluator";
   public static final String PRIMITIVE_ACCESSPATH_EDITOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.POJOAccessPathEditor";
   public static final String PRIMITIVE_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.PrimitiveValidator";
   public static final String PRIMITIVE_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/data/java/images/primitive_data.gif";

   public static final String SERIALIZABLE_PANEL_CLASS = "ag.carnot.workflow.spi.providers.data.java.SerializablePropertiesEditor";
   public static final String SERIALIZABLE_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.JavaBeanAccessPathEvaluator";
   public static final String SERIALIZABLE_ACCESSPATH_EDITOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.POJOAccessPathEditor";
   public static final String SERIALIZABLE_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.SerializableValidator";
   public static final String SERIALIZABLE_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/data/java/images/serializable_data.gif";

   public static final String ENTITYBEAN_PANEL_CLASS = "ag.carnot.workflow.spi.providers.data.entitybean.EntityBeanPropertiesEditor";
   public static final String ENTITYBEAN_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.data.entitybean.EntityBeanEvaluator";
   public static final String ENTITYBEAN_ACCESSPATH_EDITOR_CLASS = "ag.carnot.workflow.spi.providers.data.java.POJOAccessPathEditor";
   public static final String ENTITYBEAN_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.data.entitybean.EntityBeanValidator";
   public static final String ENTITYBEAN_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/data/entitybean/icon.gif";

   public static final String PLAINXML_PANEL_CLASS = "ag.carnot.workflow.spi.providers.data.plainxml.XMLDocumentPropertiesEditor";
   public static final String PLAINXML_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.data.plainxml.XPathEvaluator";
   public static final String PLAINXML_ACCESSPATH_EDITOR_CLASS = "ag.carnot.workflow.spi.providers.data.plainxml.XPathEditor";
   public static final String PLAINXML_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.data.plainxml.XMLValidator";
   public static final String PLAINXML_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/data/plainxml/icon.gif";

   public static final String SESSIONBEAN_INSTANCE_CLASS = "ag.carnot.workflow.spi.providers.applications.sessionbean.SessionBeanApplicationInstance";
   public static final String SESSIONBEAN_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.sessionbean.SessionBeanValidator";
   public static final String SESSIONBEAN_PANEL_CLASS = "ag.carnot.workflow.spi.providers.applications.sessionbean.SessionBeanApplicationPanel";
   public static final String SESSIONBEAN_ACCESSPOINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.applications.sessionbean.SessionBeanAccessPointProvider";
   public static final String SESSIONBEAN_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/applications/sessionbean/icon.gif";

   public static final String PLAINJAVA_INSTANCE_CLASS = "ag.carnot.workflow.spi.providers.applications.plainjava.PlainJavaApplicationInstance";
   public static final String PLAINJAVA_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.plainjava.PlainJavaValidator";
   public static final String PLAINJAVA_PANEL_CLASS = "ag.carnot.workflow.spi.providers.applications.plainjava.PlainJavaApplicationPanel";
   public static final String PLAINJAVA_ACCESSPOINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.applications.plainjava.PlainJavaAccessPointProvider";
   public static final String PLAINJAVA_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/applications/plainjava/icon.gif";

   public static final String JMS_APPLICATION_INSTANCE_CLASS = "ag.carnot.workflow.spi.providers.applications.jms.JMSApplicationInstance";
   public static final String JMS_APPLICATION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.jms.JMSValidator";
   public static final String JMS_APPLICATION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.applications.jms.JMSApplicationPanel";
   public static final String JMS_APPLICATION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/applications/jms/icon.gif";

   public static final String WS_APPLICATION_INSTANCE_CLASS = "ag.carnot.workflow.spi.providers.applications.ws.WebserviceApplicationInstance";
   public static final String WS_APPLICATION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.ws.WebserviceApplicationValidator";
   public static final String WS_APPLICATION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.applications.ws.gui.WebserviceApplicationPanel";
   public static final String WS_APPLICATION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/applications/ws/gui/icon.gif";

   public static final String JFC_CONTEXT_ACCESSPOINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.contexts.jfc.JFCAccessPointProvider";
   public static final String JFC_CONTEXT_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.contexts.jfc.JFCValidator";
   public static final String JFC_CONTEXT_PANEL_CLASS = "ag.carnot.workflow.spi.providers.contexts.jfc.JFCContextTypePanel";
   public static final String JFC_CONTEXT_INSTANCE_CLASS = "ag.carnot.workflow.spi.providers.contexts.jfc.JFCApplicationInstance";
   public static final String JFC_CONTEXT_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/contexts/jfc/icon.gif";

   public static final String DEFAULT_EVENT_EMITTER_CLASS = "ag.carnot.workflow.spi.runtime.DefaultPullEventEmitter";
   public static final String DEFAULT_EVENT_BINDER_CLASS = "ag.carnot.workflow.spi.runtime.DefaultEventBinder";

   public static final String EXCEPTION_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.exception.SetExceptionAction";
   public static final String EXCEPTION_ACTION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.exception.SetExceptionActionPropertiesPanel";

   public static final String SET_DATA_ACTION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.setdata.SetDataActionPropertiesPanel";
   public static final String SET_DATA_ACTION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.actions.setdata.SetDataActionValidator";
   public static final String SET_DATA_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.setdata.SetDataAction";
   public static final String SET_DATA_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/setdata/icon.gif";

   public static final String JSP_CONTEXT_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.contexts.jsp.JSPValidator";
   public static final String JSP_CONTEXT_PANEL_CLASS = "ag.carnot.workflow.spi.providers.contexts.jsp.JSPContextTypePanel";
   public static final String JSP_CONTEXT_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/contexts/jsp/icon.gif";

   public static final String MANUAL_TRIGGER_PANEL_CLASS = "ag.carnot.workflow.spi.providers.triggers.manual.ManualTriggerPanel";
   public static final String MANUAL_TRIGGER_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.manual.ManualTriggerValidator";
   public static final String MANUAL_TRIGGER_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/triggers/manual/icon.gif";

   public static final String JMS_TRIGGER_PANEL_CLASS = "ag.carnot.workflow.spi.providers.triggers.jms.JMSTriggerPanel";
   public static final String JMS_TRIGGER_MESSAGEACCEPTOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.jms.DefaultTriggerMessageAcceptor";
   public static final String JMS_TRIGGER_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.jms.JMSTriggerValidator";
   public static final String JMS_TRIGGER_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/triggers/jms/icon.gif";

   public static final String MAIL_TRIGGER_PANEL_CLASS = "ag.carnot.workflow.spi.providers.triggers.mail.MailTriggerPanel";
   public static final String MAIL_TRIGGER_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.mail.MailTriggerValidator";
   public static final String MAIL_TRIGGER_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.mail.MailTriggerEvaluator";
   public static final String MAIL_TRIGGER_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/triggers/mail/icon.gif";

   public static final String TIMER_TRIGGER_PANEL_CLASS = "ag.carnot.workflow.spi.providers.triggers.timer.TimerTriggerPanel";
   public static final String TIMER_TRIGGER_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.timer.TimerTriggerEvaluator";
   public static final String TIMER_TRIGGER_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.triggers.timer.TimerTriggerValidator";
   public static final String TIMER_TRIGGER_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/triggers/timer/icon.gif";

   public static final String TIMER_CONDITION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.PeriodPropertiesPanel";
   public static final String TIMER_CONDITION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.TimerValidator";
   public static final String TIMER_CONDITION_RULE_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.TimeStampCondition";
   public static final String TIMER_CONDITION_RUNTIME_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.TimerbasedRuntimeBindPanel";
   public static final String TIMER_CONDITION_BINDER_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.TimeStampBinder";
   public static final String TIMER_CONDITION_EMITTER_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.TimeStampEmitter";
   public static final String TIMER_CONDITION_ACCESSPOINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.conditions.timer.TimerAccessPointProvider";
   public static final String TIMER_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/timer/icon.gif";

   public static final String TRIGGER_ACTION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.trigger.TriggerProcessActionPanel";
   public static final String TRIGGER_ACTION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.actions.trigger.TriggerActionValidator";
   public static final String TRIGGER_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.trigger.TriggerProcessAction";
   public static final String TRIGGER_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/trigger/icon.gif";

   public static final String MAIL_ACTION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.mail.SendmailActionPanel";
   public static final String MAIL_ACTION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.actions.mail.MailActionValidator";
   public static final String MAIL_ACTION_RULE_CLASS = "ag.carnot.workflow.spi.providers.actions.mail.SendmailAction";
   public static final String MAIL_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/mail/icon.gif";

   public static final String EXCEPTION_CONDITION_ACCESS_POINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.conditions.exception.ExceptionConditionAccessPointProvider";
   public static final String EXCEPTION_CONDITION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.exception.ExceptionConditionPropertiesPanel";
   public static final String EXCEPTION_CONDITION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.conditions.exception.ExceptionConditionValidator";
   public static final String EXCEPTION_CONDITION_RULE_CLASS = "ag.carnot.workflow.spi.providers.conditions.exception.ExceptionCondition";
   public static final String EXCEPTION_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/exception/icon.gif";

   public static final String DELEGATE_ACTIVITY_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.delegate.DelegateEventAction";
   public static final String DELEGATE_ACTIVITY_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.delegate.DelegateEventActionPanel";
   public static final String DELEGATE_ACTIVITY_RUNTIME_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.delegate.DelegateEventActionRuntimePanel";
   public static final String DELEGATE_ACTIVITY_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/delegate/icon.gif";

   public static final String SCHEDULE_ACTIVITY_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.schedule.ScheduleEventAction";
   public static final String SCHEDULE_ACTIVITY_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.schedule.ScheduleEventActionPanel";
   public static final String SCHEDULE_ACTIVITY_RUNTIME_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.schedule.ScheduleEventActionRuntimePanel";
   public static final String SCHEDULE_ACTIVITY_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/schedule/icon.gif";

   public static final String EXPRESSION_CONDITION_CLASS = "ag.carnot.workflow.spi.providers.conditions.expression.ExpressionCondition";
   public static final String EXPRESSION_CONDITION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.expression.ExpressionConditionPropertiesPanel";
   public static final String EXPRESSION_CONDITION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.conditions.expression.ExpressionConditionValidator";
   public static final String EXPRESSION_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/expression/icon.gif";

   public static final String STATECHANGE_CONDITION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.conditions.statechange.StatechangeConditionValidator";
   public static final String ACTIVITY_STATECHANGE_CONDITION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.statechange.StatechangeConditionPropertiesPanel";
   public static final String ACTIVITY_STATECHANGE_CONDITION_RULE_CLASS = "ag.carnot.workflow.spi.providers.conditions.statechange.StatechangeCondition";
   public static final String ACTIVITY_STATECHANGE_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/statechange/icon.gif";

   public static final String PROCESS_STATECHANGE_CONDITION_RULE_CLASS = "ag.carnot.workflow.spi.providers.conditions.statechange.ProcessStatechangeCondition";
   public static final String PROCESS_STATECHANGE_CONDITION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.statechange.ProcessStatechangeConditionPropertiesPanel";
   public static final String PROCESS_STATECHANGE_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/statechange/icon.gif";

   public static final String ACTIVITY_ON_ASSIGNMENT_CONDITION_PANEL_CLASS = "ag.carnot.workflow.spi.providers.conditions.assignment.AssignmentConditionPropertiesPanel";
   public static final String ACTIVITY_ON_ASSIGNMENT_CONDITION_ACCESS_POINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.conditions.assignment.AssignmentConditionAccessPointProvider";
   public static final String ACTIVITY_ON_ASSIGNMENT_CONDITION_RULE_CLASS = "ag.carnot.workflow.spi.providers.conditions.assignment.AssignmentCondition";
   public static final String ACTIVITY_ON_ASSIGNMENT_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/assignment/icon.gif";

   public static final String ACTIVATE_ACTIVITY_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.awake.AwakeActivityEventAction";;
   public static final String ACTIVATE_ACTIVITY_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/awake/icon.gif";

   public static final String ABORT_PROCESS_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.abort.AbortProcessEventAction";
   public static final String ABORT_PROCESS_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/abort/icon.gif";

   public static final String COMPLETE_ACTIVITY_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.complete.CompleteActivityEventAction";
   public static final String COMPLETE_ACTIVITY_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/complete/icon.gif";

   public static final String EXCLUDE_USER_ACTION_CLASS = "ag.carnot.workflow.spi.providers.actions.excludeuser.ExcludeUserAction";
   public static final String EXCLUDE_USER_PANEL_CLASS = "ag.carnot.workflow.spi.providers.actions.excludeuser.ExcludeUserActionPanel";
   public static final String EXCLUDE_USER_ACTION_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.actions.excludeuser.ExcludeUserActionValidator";
   public static final String EXCLUDE_USER_ACTION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/actions/excludeuser/icon.gif";

   public static final String EXTERNAL_CONDITION_CLASS = "ag.carnot.workflow.spi.providers.conditions.simplepush.PushCondition";
   public static final String EXTERNAL_CONDITION_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/conditions/simplepush/icon.gif";

   // random constants
   // @todo (france, ub): exploit in the queryservice
   public static final int ACTIVE_MODEL = -10;
   public static final int LAST_DEPLOYED_MODEL = -20;
   public static final int ALIVE_MODELS = -30;
   public static final int ALL_MODELS = -40;
   public static final int ANY_MODEL = -50;

   // unsorted

   public static final String CONSTRUCTOR_NAME_ATT = ENGINE_SCOPE + "constructorName";
   public static final String ICON_ATT = DD_SCOPE + "icon";// TODO: remove
   public static final String SAP_R3_DATA = "sapr3data";
   public static final String SAP_DATA_EVALUATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.sap.data.SapAccessPathEvaluator";
   public static final String SAP_DATA_ACCESSPATH_EDITOR_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.sap.data.SapAccessPathEditor";
   public static final String SAP_DATA_PANEL_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.sap.data.SapDataTypePropertiesEditor";
   public static final String SAP_DATA_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.sap.data.SapDataValidator";
   public static final String SAP_DATA_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/applications/jca/sap/data/icon.gif";
   public static final String SAP_R3_APPLICATION = "sapr3application";
   public static final String CCI_VALIDATOR_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.DefaultCCIValidator";
   public static final String CCI_PANEL_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.gui.DefaultCCIApplicationPropertiesPanel";
   public static final String CCI_APPLICATION_INSTANCE_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.DefaultCCIApplicationInstance";
   public static final String CCI_WRAPPER_CLASS_ATT = ENGINE_SCOPE + "CCIWrapper";
   public static final String SAP_CCI_WRAPPER_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.sap.application.SapCCIWrapper";
   public static final String SAP_CCI_ICON_LOCATION = "/ag/carnot/workflow/spi/providers/applications/jca/sap/application/icon.gif";
   public static final String CCI_ACCESSPOINT_PROVIDER_CLASS = "ag.carnot.workflow.spi.providers.applications.jca.DefaultCCIAccessPointProvider";
   public static final String TIMER_CONDITION_USE_DATA_ATT = ENGINE_SCOPE + "useData";
   public static final String TIMER_CONDITION_DATA_ATT = ENGINE_SCOPE + "data";
   public static final String TIMER_CONDITION_DATA_PATH_ATT = ENGINE_SCOPE + "dataPath";
   public static final String VALID_FROM_ATT = ENGINE_SCOPE + "validFrom";
   public static final String VALID_TO_ATT = ENGINE_SCOPE + "validTo";
   public static final String DEPLOYMENT_COMMENT_ATT = ENGINE_SCOPE + "deploymentComment";
   public static final String MODEL_UUID = ENGINE_SCOPE + "modelUUID";

   public static final String EVENT_ACCESS_POINT = ENGINE_SCOPE + "eventScope";
   public static final String MESSAGE_TYPE_ATT = ENGINE_SCOPE + "messageType";

   public static final String QUEUE_CONNECTION_FACTORY_NAME_PROPERTY = ENGINE_SCOPE + "queueConnectionFactory.jndiName";
   public static final String QUEUE_NAME_PROPERTY = ENGINE_SCOPE + "queue.jndiName";
   public static final String MESSAGE_PROVIDER_PROPERTY = ENGINE_SCOPE + "messageProvider";
   // @todo (france, ub): usage of this property is probably a misuse because the acceptor is hardwired to the application type??
   public static final String MESSAGE_ACCEPTOR_PROPERTY = ENGINE_SCOPE + "messageAcceptor";
   public static final String JMS_LOCATION_PROPERTY = ENGINE_SCOPE + "jms.location";
   public static final String REQUEST_MESSAGE_TYPE_PROPERTY = ENGINE_SCOPE + "requestMessageType";
   public static final String RESPONSE_MESSAGE_TYPE_PROPERTY = ENGINE_SCOPE + "responseMessageType";
   public static final String INCLUDE_OID_HEADERS_PROPERTY = ENGINE_SCOPE + "includeOidHeaders";
   public static final String EXCLUDE_PERFORMER = ENGINE_SCOPE + "excludePerformer";
   public static final String EXCLUDED_PERFORMER_DATA = ENGINE_SCOPE + "excludedPerformerData";
   public static final String EXCLUDED_PERFORMER_DATAPATH = ENGINE_SCOPE + "excludedPerformerDataPath";;
   public static final String ACTIVITY_INSTANCE_ACCESSPOINT = "activityInstance";
   public static final String PROCESS_INSTANCE_ACCESSPOINT = "processInstance";

   public static final String SOURCE_USER_ATT = ENGINE_SCOPE + "sourceUser";
   public static final String TARGET_USER_ATT = ENGINE_SCOPE + "targetUser";
   public static final String VERSION_ATT = ENGINE_SCOPE + "version";
   public static final String REVISION_ATT = ENGINE_SCOPE + "revision";
   public static final String IS_RELEASED_ATT = ENGINE_SCOPE + "released";
   public static final String RELEASE_STAMP = ENGINE_SCOPE + "releaseStamp";
   public static final String DEPLOYMENT_TIME_ATT = ENGINE_SCOPE + "deploymentStamp";
   public static final String PREDECESSOR_ATT = ENGINE_SCOPE + "predecessor";
   public static final String IS_DISABLED_ATT = ENGINE_SCOPE + "disabled";
   public static final String PLAIN_WEB_SERVICEFACTORY_CLASS = "org.eclipse.stardust.engine.api.web.PlainWebServiceFactory";
   public static final String REMOTE_WEB_SERVICEFACTORY_CLASS = "org.eclipse.stardust.engine.api.web.RemoteWebServiceFactory";
   public static final String POJO_SERVICEFACTORY_CLASS = "org.eclipse.stardust.engine.core.runtime.beans.POJOServiceFactory";
   public static final String INTERNAL_CREDENTIALPROVIDER_CLASS = "org.eclipse.stardust.engine.api.runtime.InternalCredentialProvider";
//   public static final String DEFAULT_SERVICEFACTORY_POOL_CLASS = "org.eclipse.stardust.engine.core.runtime.beans.ThreadLocalServiceFactoryPool";
   public static final String DEFAULT_SERVICEFACTORY_POOL_CLASS = "org.eclipse.stardust.engine.core.runtime.beans.DefaultServiceFactoryPool";

   public static final String SUBPROCESS_ACTIVITY_COPY_ALL_DATA_ATT = ENGINE_SCOPE + "subprocess:copyAllData";

   // Controlling / Warehouse
   public static final String PWH_MEASURE = PWH_SCOPE + "measure";
   public static final String PWH_TARGET_MEASURE_QUANTITY = PWH_SCOPE + "targetMeasureQuantity";
   public static final String PWH_DIFFICULTY = PWH_SCOPE + "difficulty";
   public static final String PWH_TARGET_PROCESSING_TIME = PWH_SCOPE + "targetProcessingTime";
   public static final String PWH_TARGET_EXECUTION_TIME = PWH_SCOPE + "targetExecutionTime";
   public static final String PWH_OVERDUE_THRESHOLD = PWH_SCOPE + "overdueThreshold";
   public static final String PWH_TARGET_IDLE_TIME = PWH_SCOPE + "targetIdleTime";
   public static final String PWH_TARGET_WAITING_TIME = PWH_SCOPE + "targetWaitingTime";
   public static final String PWH_TARGET_QUEUE_DEPTH = PWH_SCOPE + "targetQueueDepth";
   public static final String PWH_TARGET_COST_PER_EXECUTION = PWH_SCOPE + "targetCostPerExecution";
   public static final String PWH_TARGET_COST_PER_SECOND = PWH_SCOPE + "targetCostPerSecond";
   public static final String PWH_INCLUDE_TIME = PWH_SCOPE + "includeTime";
   public static final String PWH_COST_DRIVER = PWH_SCOPE + "costDriver";
   public static final String PWH_TARGET_COST_DRIVER_QUANTITY = PWH_SCOPE + "costDriverQuantity";
//   public static final String PWH_PROCESS_CATEGORY = PWH_SCOPE + "processCategory";
   public static final String PWH_WORKING_WEEKS_PER_YEAR = PWH_SCOPE + "workingWeeksPerYear";
   public static final String PWH_ACTUAL_COST_PER_MINUTE = PWH_SCOPE + "actualCostPerMinute";
   public static final String PWH_TARGET_WORK_TIME_PER_DAY = PWH_SCOPE + "targetWorkTimePerDay";
   public static final String PWH_TARGET_WORK_TIME_PER_WEEK = PWH_SCOPE + "targetWorkTimePerWeek";
   public static final String PWH_COST_CENTER = PWH_SCOPE + "costCenter";
   public static final String PWH_ACTUAL_COST_PER_SECOND = PWH_SCOPE + "actualCostPerSecond";

   public static final String PLAINXML_SCHEMA_TYPE_ATT = ENGINE_SCOPE + "schemaType";
   public static final String PLAINXML_SCHEMA_TYPE_NONE = "none";
   public static final String PLAINXML_SCHEMA_TYPE_DTD = "dtd";
   public static final String PLAINXML_SCHEMA_TYPE_XSD = "xsd";
   public static final String PLAINXML_SCHEMA_TYPE_WSDL = "wsdl";

   public static final String PLAINXML_SCHEMA_URL_ATT = ENGINE_SCOPE + "schemaURL";
   public static final String PLAINXML_TYPE_ID_ATT = ENGINE_SCOPE + "typeId";
   public static final String XML_ENCODING = "carnot.engine.xml.encoding";

   private static final String[] META_DATA_IDS_ARRAY = {STARTING_USER, ROOT_PROCESS_ID, CURRENT_USER,
      LAST_ACTIVITY_PERFORMER, CURRENT_DATE, PROCESS_ID, PROCESS_PRIORITY, CURRENT_LOCALE, CURRENT_MODEL};

   public static final List META_DATA_IDS = Collections.unmodifiableList(Arrays.asList(META_DATA_IDS_ARRAY));

   public static final String CARNOT_EL_PREFIX = "carnotEL: ";

   public static final String CASE_PROCESS_ID = "CaseProcess";
   public static final String DEFAULT_CASE_ACTIVITY_ID = "DefaultCaseActivity";
   public static final String PREDEFINED_MODEL_ID = "PredefinedModel";
   public static final String CASE_DATA_ID = "CaseInfo";
   public static final String CASE_NAME_ELEMENT = "CaseName";
   public static final String CASE_DESCRIPTION_ELEMENT = "CaseDescription";
   public static final String CASE_DESCRIPTORS_ELEMENT = "Descriptors";
   public static final String CASE_PERFORMER_ID = "CasePerformer";
   public static final String QUALIFIED_CASE_DATA_ID = '{' + PREDEFINED_MODEL_ID + '}' + CASE_DATA_ID;
   public static final String CASE_DESCRIPTOR_VALUE_XPATH = CASE_DESCRIPTORS_ELEMENT + '/' + "value";

   // transient process
   public static final String TRANSIENT_PROCESS_AUDIT_TRAIL_PERSISTENCE = ENGINE_SCOPE + "auditTrailPersistence";

   // web modeler - hash attribute (read only models)
   public static final String READ_ONLY_HASH = "stardust:security:hash";

   public static final String BUSINESS_OBJECT_MANAGEDORGANIZATIONS = ENGINE_SCOPE + "managedOrganizations"; //$NON-NLS-1$
   public static final String BUSINESS_OBJECT_NAMEEXPRESSION = ENGINE_SCOPE + "nameExpression";
   
   public static final String BUSINESS_OBJECTS_DATAREF = "stardust:model:businessObjects";
   
   // volatile data attribute
   public static final String VOLATILE_DATA = ENGINE_SCOPE + "volatile";
   
   private PredefinedConstants() {
      //disallow instance creation
   }
}
