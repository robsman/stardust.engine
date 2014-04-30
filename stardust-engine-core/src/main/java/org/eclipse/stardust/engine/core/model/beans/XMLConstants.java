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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.common.config.CurrentVersion;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface XMLConstants
{
   String ENCODING_ISO_8859_1 = "ISO-8859-1";
   String XMLNS_ATTR = "xmlns";
   String XPDL = "xpdl";
   String XMLNS_XPDL_ATTR = XMLNS_ATTR + ":" + XPDL;
   
   String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
   String NS_CARNOT_WORKFLOWMODEL_30 = "http://www.carnot.ag/workflowmodel/3.0";
   String NS_CARNOT_WORKFLOWMODEL_31 = "http://www.carnot.ag/workflowmodel/3.1";
   String NS_CARNOT_XPDL_31 = "http://www.carnot.ag/xpdl/3.1";
   String NS_XPDL_1_0 = "http://www.wfmc.org/2002/XPDL1.0";
   String NS_XPDL_2_0 = "http://www.wfmc.org/2004/XPDL2.0alpha";
   String NS_XPDL_2_1 = "http://www.wfmc.org/2008/XPDL2.1";
   String NS_XPDL_2_2 = "http://www.wfmc.org/2009/XPDL2.2";
   String NS_XPDL = NS_XPDL_2_1;
   String NS_XSD_2001 = "http://www.w3.org/2001/XMLSchema";
   String NS_WSDL_1_1 = "http://schemas.xmlsoap.org/wsdl/";

   String DTD_NAME = "WorkflowModel.dtd";
   String WORKFLOWMODEL_XSD = "WorkflowModel.xsd";
   String USER_NAME = "user.name";

   String WORKFLOWMODEL_30_DTD_URL = NS_CARNOT_WORKFLOWMODEL_30 + "/" + DTD_NAME;
   String WORKFLOWMODEL_31_DTD_URL = NS_CARNOT_WORKFLOWMODEL_31 + "/" + DTD_NAME;
   String WORKFLOWMODEL_31_XSD_URL = NS_CARNOT_WORKFLOWMODEL_31 + "/" + WORKFLOWMODEL_XSD;

   // Element names
   
   String CARNOT_FORMAL_PARAMETER_MAPPINGS = "FormalParameterMappings";
   String CARNOT_FORMAL_PARAMETER_MAPPING = "FormalParameterMapping";
   
   String QUALITY_ASSURANCE = "qualityControl";
   String QUALITY_ASSURANCE_CODE = "code";
   String QUALITY_ASSURANCE_CODES = "validQualityCodes";
   
   String XPDL_BASIC_TYPE = "BasicType";
   String XPDL_DECLARED_TYPE = "DeclaredType";
   String XPDL_EXTENDED_ATTRIBUTE = "ExtendedAttribute";
   String XPDL_EXTENDED_ATTRIBUTE_NAME = "Name";
   String XPDL_EXTENDED_ATTRIBUTE_VALUE = "Value";
   String XPDL_EXTENDED_ATTRIBUTES = "ExtendedAttributes";
   String XPDL_EXTERNAL_PACKAGE = "ExternalPackage";
   String XPDL_EXTERNAL_PACKAGES = "ExternalPackages";
   String XPDL_EXTERNAL_REFERENCE = "ExternalReference";
   String XPDL_FORMAL_PARAMETER = "FormalParameter";
   String XPDL_FORMAL_PARAMETERS = "FormalParameters";
   String XPDL_LOOP = "Loop";
   String XPDL_SCHEMA_TYPE = "SchemaType";
   String XPDL_SCRIPT = "Script";
   String XPDL_TYPE_DECLARATION = "TypeDeclaration";
   String XPDL_TYPE_DECLARATIONS = "TypeDeclarations";

   String ACCESS_POINT = "accessPoint";
   String ACTIVITY = "activity";
   String APPLICATION = "application";
   String APPLICATION_CONTEXT_TYPE = "applicationContextType";
   String APPLICATION_TYPE = "applicationType";
   String PARTICIPANT = "participant";
   String CONDITIONAL_PERFORMER = "conditionalPerformer";
   String DATA = "data";
   String DATA_MAPPING = "dataMapping";
   String DATA_PATH = "dataPath";
   String DESCRIPTION = "description";
   String DIAGRAM = "diagram";
   String EXPRESSION = "expression";
   String EXTERNAL_REFERENCE = "externalReference";
   String IMPLEMENTS = "Implements";
   String MODELER = "modeler";
   String LINK_TYPE = "linkType";
   String MODEL = "model";
   String ORGANIZATION = "organization";
   // @todo (france, ub): rename to process
   // check wfmc doc and user doc
   String PROCESS = "processDefinition";
   String ROLE = "role";
   String TEXT = "text";
   String TRANSITION = "transition";
   String TRIGGER = "trigger";
   String PARAMETER_MAPPING = "parameterMapping";
   String CONTEXT = "context";
   String ATTRIBUTE = "attribute";

   String ACTIVITY_SYMBOL = "activitySymbol";
   String ANNOTATION_SYMBOL = "annotationSymbol";
   String APPLICATION_SYMBOL = "applicationSymbol";
   String CONDITIONAL_PERFORMER_SYMBOL = "conditionalPerformerSymbol";
   String DATA_SYMBOL = "dataSymbol";
   String GROUP_SYMBOL = "groupSymbol";
   String MODELER_SYMBOL = "modelerSymbol";
   String ORGANIZATION_SYMBOL = "organizationSymbol";
   String PROCESS_SYMBOL = "processSymbol";
   String ROLE_SYMBOL = "roleSymbol";

   String DATA_MAPPING_CONNECTION = "dataMappingConnection";
   String EXECUTED_BY_CONNECTION = "executedByConnection";
   String GENERIC_LINK_CONNECTION = "genericLinkConnection";
   String PART_OF_CONNECTION = "partOfConnection";
   String PERFORMS_CONNECTION = "performsConnection";
   String REFERS_TO_CONNECTION = "refersToConnection";
   String SUBPROCESS_OF_CONNECTION = "subprocessOfConnection";
   String TEAM_LEAD_CONNECTION = "teamLeadConnection";
   String TRANSITION_CONNECTION = "transitionConnection";
   String WORKS_FOR_CONNECTION = "worksForConnection";
   String VALUE="value";
   String VIEW = "view";
   String VIEWABLE = "viewable";

   String DATA_TYPE = "dataType";
   String TRIGGER_TYPE = "triggerType";
   String EVENT_CONDITION_TYPE = "eventConditionType";
   String EVENT_ACTION_TYPE = "eventActionType";
   String EVENT_HANDLER = "eventHandler";
   String EVENT_ACTION = "eventAction";
   String BIND_ACTION ="bindAction";
   String UNBIND_ACTION="unbindAction";

   // Attributes names

   String XPDL_ID_ATT = "Id";
   String XPDL_NAME_ATT = "Name";
   String XPDL_TYPE_ATT = "Type";

   String XPDL_LOCATION_ATT = "location";
   String XPDL_NAMESPACE_ATT = "namespace";
   String XPDL_XREF_ATT = "xref";
   String XPDL_HREF_ATT = "href";
   String XPDL_SCRIPT_TYPE_ATT = "Type";
   String XPDL_SCRIPT_VERSION_ATT = "Version";
   String XPDL_SCRIPT_GRAMMAR_ATT = "Grammar";
   
   String ACTIVITY_REF_ATT = "activity";
   String ALLOWS_ABORT_BY_PERFORMER_ATT = "allowsAbortByPerformer";
   String APPLICATION_REF_ATT = "application";
   String APPLICATION_PATH_ATT = "applicationPath";
   String AUTHOR_ATT = "author";
   String CARDINALITY_ATT = "cardinality";
   String CARNOT_VERSION_ATT = "carnotVersion";
   String CLASS_ATT = "type";
   String CONDITION = "condition";
   String CONDITION_VALUE = "CONDITION";
   String CONDITION_OTHERWISE_VALUE = "OTHERWISE";
   String CREATED = "created";
   String DATA_PATH_ATT = "dataPath";
   String DATA_REF_ATT = "data";
   String DEFAULT_PRIORITY_ATT = "defaultPriority";
   String FROM_ATT = "from";
   String FORK_ON_TRAVERSAL_ATT = "forkOnTraversal";
   String ID_ATT = "id";
   String IMPLEMENTATION_ATT = "implementation";
   String IS_USER_ATT = "is_user";
   String JOIN_ATT = "join";
   String LINE_COLOR_ATT = "lineColor";
   String LINE_TYPE_ATT = "lineType";
   String LOOP_CONDITION_ATT = "loopCondition";
   String LOOP_TYPE = "loopType";
   String MODEL_OID = "modelOID";
   String NAME_ATT = "name";
   String HIBERNATE_ON_CREATION = "hibernateOnCreation";
   String OID_ATT = "oid";
   String PARAMETER = "parameter";
   String PARAMETER_PATH = "parameterPath";
   String PASSWORD = "password";
   String PERFORMER = "performer";
   String QUALITY_ASSURANCE_PERFORMER = "qualityControlPerformer";   
   String POINTS = "points";
   String PREDEFINED_ATT = "predefined";
   String PROCESS_ID_REF = "implementationProcess";
   String SUB_PROCESS_MODE = "subProcessMode";
   String SHOW_LINKTYPE_NAME = "show_linktype_name";
   String SHOW_ROLE_NAMES = "show_role_names";
   String SOURCE_CLASS = "source_classname";
   String SOURCE_CARDINALITY = "source_cardinality";
   String SOURCE_ROLE = "source_rolename";
   String SOURCE_SYMBOL = "source_symbol";
   String SPLIT = "split";
   String VALUE_ATT = "value";
   String TARGET_CLASS = "target_classname";
   String TARGET_CARDINALITY = "target_cardinality";
   String TARGET_ROLE = "target_rolename";
   String TARGET_SYMBOL = "target_symbol";
   String TEAM_LEAD = "teamLead";
   String TEAM_LEAD_SYMBOL = "teamLeadSymbol";
   String TEAM_SYMBOL = "teamSymbol";
   String TO = "to";
   String TRANSITION_REF = "transition";
   String TYPE_ATT = "type";
   String USEROBJECT = "refer";
   String VENDOR = "vendor";
   String X = "x";
   String Y = "y";

   String ACTIVITY_SYMBOL_REF = "activitySymbol";
   String APPLICATION_SYMBOL_ID = "applicationSymbol";
   String DATA_SYMBOL_ID = "dataSymbol";
   String LINK_TYPE_REF = "linkType";
   String ORGANIZATION_SYMBOL_ID = "organizationSymbol";
   String PARTICIPANT_SYMBOL_ID = "participantSymbol";
   String PROCESS_SYMBOL_ID = "processSymbol";
   String SOURCE_ACTIVITY_SYMBOL_ID = "sourceActivitySymbol";
   String SOURCE_SYMBOL_ID = "sourceSymbol";
   String SUB_ORGANIZATION_SYMBOL_ID = "suborganizationSymbol";
   String SUB_PROCESS_SYMBOL_ID = "subprocessSymbol";
   String TARGET_ACTIVITY_SYMBOL_ID = "targetActivitySymbol";
   String TARGET_SYMBOL_ID = "targetSymbol";
   String DIRECTION_ATT = "direction";
   String INTERACTIVE_ATT = "interactive";
   String CONTEXT_ATT = "context";

   String APPLICATION_ACCESS_POINT_ATT = "applicationAccessPoint";

   // attribute values
   String VENDOR_NAME = CurrentVersion.VENDOR_NAME + ", " + CurrentVersion.PRODUCT_NAME;

   // <--

   String SYNCHRONOUS_ATT = "synchronous";

   String DESCRIPTOR_ATT = "descriptor";
   String KEY_ATT = "key";
   String PULL_TRIGGER_ATT = "pullTrigger";

   String PROCESS_ACTION_ATT = "processAction";
   String ACTIVITY_ACTION_ATT = "activityAction";
   String PROCESS_CONDITION_ATT = "processCondition";
   String ACTIVITY_CONDITION_ATT = "activityCondition";
   String SUPPORTED_CONDITION_TYPES_ATT = "supportedConditionTypes";
   String UNSUPPORTED_CONTEXTS_ATT = "unsupportedContexts";

   String VIEWABLE_ATT = "viewable";
   String PARTICIPANT_ATT = "participant";
   String HAS_MAPPING_ID_ATT = "hasMappingId";
   String HAS_APPLICATION_PATH_ATT = "hasApplicationPath";

   String AUTO_BIND_ATT = "autoBind";
   String UNBIND_ON_MATCH_ATT = "unbindOnMatch";
   String LOG_HANDLER_ATT = "logHandler";
   String CONSUME_ON_MATCH_ATT ="consumeOnMatch";
}
