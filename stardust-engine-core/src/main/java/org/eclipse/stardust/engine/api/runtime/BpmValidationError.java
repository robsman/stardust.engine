/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.text.MessageFormat;

import org.eclipse.stardust.common.error.ErrorCase;


/**
 * @author pielmann
 * @version $Revision: $
 */
public class BpmValidationError extends ErrorCase
{

   private static final long serialVersionUID = 1L;


   //General validation
   public static final Args1 VAL_DUPLICATE_IDENTIFIER = newArgs1("VAL01000", BpmValidationErrorMessages.getString("VAL01000"));
   public static final Args1 VAL_INVALID_IDENTIFIER = newArgs1("VAL01001", BpmValidationErrorMessages.getString("VAL01001"));
   public static final Args1 VAL_CANNOT_RETRIEVE_CLASS_FOR_VALIDATION = newArgs1("VAL01002", BpmValidationErrorMessages.getString("VAL01002"));
   public static final Args1 VAL_HAS_NO_ID = newArgs1("VAL01003", BpmValidationErrorMessages.getString("VAL01003"));
   public static final Args1 VAL_HAS_INVALID_ID = newArgs1("VAL01004", BpmValidationErrorMessages.getString("VAL01004"));
   public static final Args0 VAL_DMS_OPERATION_NOT_SET = newArgs0("VAL01005", BpmValidationErrorMessages.getString("VAL01005"));

   //Model related
   public static final Args1 MDL_INVALID_QA_CODE_ID = newArgs1("MDL01001", BpmValidationErrorMessages.getString("MDL01001"));
   public static final Args2 MDL_DUPLICATE_QA_CODE = newArgs2("MDL01002", BpmValidationErrorMessages.getString("MDL01002"));
   public static final Args1 MDL_UNSUPPORTED_SCRIPT_LANGUAGE = newArgs1("MDL01003", BpmValidationErrorMessages.getString("MDL01003"));
   public static final Args1 MDL_NO_DEFAULT_VALUE_FOR_CONFIGURATION_VARIABLE = newArgs1("MDL01004", BpmValidationErrorMessages.getString("MDL01004"));
   public static final Args1 MDL_CONFIGURATION_VARIABLE_NEVER_USED = newArgs1("MDL01005", BpmValidationErrorMessages.getString("MDL01005"));
   public static final Args1 MDL_CONFIGURATION_VARIABLE_DOES_NOT_EXIST = newArgs1("MDL01006", BpmValidationErrorMessages.getString("MDL01006"));
   public static final Args1 MDL_CIRCULAR_REFERENCES_TO = newArgs1("MDL01007", BpmValidationErrorMessages.getString("MDL01007"));
   public static final Args2 MDL_REFERENCE_TO_MODEL_IS_INALID_UNTIL = newArgs2("MDL01008", BpmValidationErrorMessages.getString("MDL01008"));
   public static final Args1 MDL_REFERENCE_NOT_RESOLVED_TO_LAST_DEPLOYED_VERSION = newArgs1("MDL01009", BpmValidationErrorMessages.getString("MDL01009"));
   public static final Args1 MDL_REFERENCE_IS_RESOLVED_TO_MULTIPLE_MODEL_VERSION = newArgs1("MDL01010", BpmValidationErrorMessages.getString("MDL01010"));
   public static final Args1 MDL_NO_MODEL_WITH_OID_FOUND = newArgs1("MDL01011", BpmValidationErrorMessages.getString("MDL01011"));
   public static final Args0 MDL_NO_MODEL_ACTIVE = newArgs0("MDL01012", BpmValidationErrorMessages.getString("MDL01012"));
   public static final Args0 MDL_NO_MODEL_DEPLOYED = newArgs0("MDL01013", BpmValidationErrorMessages.getString("MDL01013"));
   public static final Args1 MDL_REFERENCED_PACKAGE_WITH_NAMESPACE_NOT_FOUND = newArgs1("MDL01014", BpmValidationErrorMessages.getString("MDL01014"));
   public static final Args2 MDL_AUDITTRAIL_CONTAINS_MODEL_WHICH_DIFFERS_FROM_THIS_MODEL = newArgs2("MDL01015", BpmValidationErrorMessages.getString("MDL01015"));
   public static final Args1 MDL_PREDECESSOR_MODEL_NOT_FOUND = newArgs1("MDL01016", BpmValidationErrorMessages.getString("MDL01016"));
   public static final Args0 MDL_UNKNOWN_MODEL_VERSION = newArgs0("MDL01017", BpmValidationErrorMessages.getString("MDL01017"));
   public static final Args1 MDL_MODEL_VERSION = newArgs1("MDL01018", BpmValidationErrorMessages.getString("MDL01018"));

   //Process Definiton related
   public static final Args0 PD_NO_START_ACTIVITY = newArgs0("PD01001", BpmValidationErrorMessages.getString("PD01001"));
   public static final Args1 PD_DUPLICATE_ID = newArgs1("PD01002", BpmValidationErrorMessages.getString("PD01002"));
   public static final Args2 PD_ID_EXCEEDS_LENGTH = newArgs2("PD01003", BpmValidationErrorMessages.getString("PD01003"));
   public static final Args1 PD_FORMAL_PARAMETER_NO_DATA_SET = newArgs1("PD01004", BpmValidationErrorMessages.getString("PD01004"));
   public static final Args2 PD_DUPLICATE_TRANSITION_SAME_SOURCE_OR_TARGET = newArgs2("PD01005", BpmValidationErrorMessages.getString("PD01005"));
   public static final Args2 PD_MULTIPLE_START_ACTIVYTIES = newArgs2("PD01006", BpmValidationErrorMessages.getString("PD01006"));
   public static final Args1 PD_NO_ACTIVITIES_DEFINED = newArgs1("PD01007", BpmValidationErrorMessages.getString("PD01007"));
   public static final Args1 PD_PROCESS_INTERFACE_NOT_RESOLVED = newArgs1("PD01008", BpmValidationErrorMessages.getString("PD01008"));

   //Application related
   public static final Args1 APP_TYPE_NO_LONGER_SUPPORTED = newArgs1("APP01001", BpmValidationErrorMessages.getString("APP01001"));
   public static final Args0 APP_UNSPECIFIED_CLASS_FOR_JFC_APPLICATION = newArgs0("APP01002", BpmValidationErrorMessages.getString("APP01002"));
   public static final Args0 APP_UNSPECIFIED_COMPLETION_METHOD_FOR_JFC_APPLICATION = newArgs0("APP01003", BpmValidationErrorMessages.getString("APP01003"));
   public static final Args1 APP_COMPLETION_METHOD_NOT_FOUND = newArgs1("APP01004", BpmValidationErrorMessages.getString("APP01004"));
   public static final Args1 APP_DUPLICATE_ID = newArgs1("APP01005", BpmValidationErrorMessages.getString("APP01005"));
   public static final Args2 APP_NO_TYPE_MAPPING_DEFINED_FOR_XML_TYPE = newArgs2("APP01006", BpmValidationErrorMessages.getString("APP01006"));
   public static final Args3 APP_XML_TYPE_HAS_INVALID_TYPE_MAPPING = newArgs3("APP01007", BpmValidationErrorMessages.getString("APP01007"));
   public static final Args3 APP_INVALID_TEMPLATE = newArgs3("APP01008", BpmValidationErrorMessages.getString("APP01008"));
   public static final Args3 APP_INVALID_WSDL_URL = newArgs3("APP01009", BpmValidationErrorMessages.getString("APP01009"));
   public static final Args2 APP_WS_PROPERTY_NOT_SET = newArgs2("APP01010", BpmValidationErrorMessages.getString("APP01010"));
   public static final Args1 APP_PROPERTY_NOT_SET = newArgs1("APP01011", BpmValidationErrorMessages.getString("APP01011"));
   public static final Args0 APP_PARAMETER_HAS_NO_ID_DEFINED = newArgs0("APP01012", BpmValidationErrorMessages.getString("APP01012"));
   public static final Args0 APP_PARAMETER_HAS_INVALID_ID_DEFINED = newArgs0("APP01013", BpmValidationErrorMessages.getString("APP01013"));
   public static final Args1 APP_NO_LOCATION_DEFINED_FOR_PARAMETER = newArgs1("APP01014", BpmValidationErrorMessages.getString("APP01014"));
   public static final Args2 APP_NO_VALID_TYPE_FOR_PARAMETER_CLASS_CANNOT_BE_FOUND = newArgs2("APP01015", BpmValidationErrorMessages.getString("APP01015"));
   public static final Args2 APP_NO_VALID_TYPE_FOR_PARAMETER_CLASS_COULD_NOT_BE_LOADED = newArgs2("APP01016", BpmValidationErrorMessages.getString("APP01016"));
   public static final Args1 APP_DUPLICATE_ID_USED = newArgs1("APP01017", BpmValidationErrorMessages.getString("APP01017"));
   public static final Args0 APP_INVALID_MAIL_ADDRESS = newArgs0("APP01018", BpmValidationErrorMessages.getString("APP01018"));
   public static final Args0 APP_UNDEFINED_HTML_PATH_FOR_JSP_APPLICATION = newArgs0("APP01019", BpmValidationErrorMessages.getString("APP01019"));

   //Actions related
   public static final Args0 ACTN_NO_DATA_DEFINED = newArgs0("ACTN01001", BpmValidationErrorMessages.getString("ACTN01001"));
   public static final Args0 ACTN_NO_ACCESS_POINT_DEFINED = newArgs0("ACTN01002", BpmValidationErrorMessages.getString("ACTN01002"));
   public static final Args0 ACTN_NO_PROCESS_SELECTED = newArgs0("ACTN01003", BpmValidationErrorMessages.getString("ACT01003"));
   public static final Args1 ACTN_NO_TYPE = newArgs1("ACTN01004", BpmValidationErrorMessages.getString("ACTN01004"));
   public static final Args1 ACTN_NO_NAME = newArgs1("ACTN01005", BpmValidationErrorMessages.getString("ACTN01005"));
   public static final Args0 ACTN_NO_RECEIVER_TYPE_SPECIFIED = newArgs0("ACTN01006", BpmValidationErrorMessages.getString("ACTN01006"));
   public static final Args0 ACTN_NO_RECEIVING_PARTICIPANT_SPECIFIED = newArgs0("ACTN01007", BpmValidationErrorMessages.getString("ACTN01007"));
   public static final Args0 ACTN_NO_EMAIL_ADDRESS_SPECIFIED = newArgs0("ACTN01008", BpmValidationErrorMessages.getString("ACTN01008"));

   //Activity related
   public static final Args1 ACTY_DUPLICATE_ID = newArgs1("ACTY01001", BpmValidationErrorMessages.getString("ACTY01001"));
   public static final Args2 ACTY_ID_EXCEEDS_MAXIMUM_LENGTH = newArgs2("ACTY01002", BpmValidationErrorMessages.getString("ACTY01002"));
   public static final Args2 ACTY_NO_PERFORMER = newArgs2("ACTY01003", BpmValidationErrorMessages.getString("ACTY01003"));
   public static final Args2 ACTY_PERFORMER_DOES_NOT_EXIST = newArgs2("ACTY01004", BpmValidationErrorMessages.getString("ACTY01004"));
   public static final Args2 ACTY_PERFORMER_SHOULD_NOT_BE_CONDITIONAL_PERFORMER = newArgs2("ACTY01005", BpmValidationErrorMessages.getString("ACTY01005"));
   public static final Args2 ACTY_NO_QA_PERFORMER_SET = newArgs2("ACTY01006", BpmValidationErrorMessages.getString("ACTY01006"));
   public static final Args2 ACTY_QA_PERFORMER_SHOULD_NOT_BE_CONDITIONAL_PERFORMER = newArgs2("ACTY01007", BpmValidationErrorMessages.getString("ACTY01007"));
   public static final Args1 ACTY_NO_IMPLEMENTATION_PROCESS_SET_FOR_SUBPROCESS_ACTIVITY = newArgs1("ACTY01008", BpmValidationErrorMessages.getString("ACTY01008"));
   public static final Args1 ACTY_SUBPROCESSMODE_NOT_SET = newArgs1("ACTY01009", BpmValidationErrorMessages.getString("ACTY01009"));
   public static final Args1 ACTY_NO_APPLICATION_SET_FOR_APPLICATION_ACTIVITY = newArgs1("ACTY01010", BpmValidationErrorMessages.getString("ACTY01010"));
   public static final Args1 ACTY_NO_ACCESS_POINT_FOR_APPLICATION = newArgs1("ACTY01011", BpmValidationErrorMessages.getString("ACTY01011"));
   public static final Args1 ACTY_NO_EXCEPTION_FLOW_TRANSITION_FOR_EVENT_HANDLER = newArgs1("ACTY01012", BpmValidationErrorMessages.getString("ACTY01012"));
   public static final Args2 ACTY_BOUNDARY_EVENTS_WITH_UNDISJUNCT_TYPE_HIERARCHIES = newArgs2("ACTY01013", BpmValidationErrorMessages.getString("ACTY01013"));
   public static final Args0 ACTY_INTERMEDIATE_EVENTS_MUST_HAVE_ONE_IN_AND_OUTBOUND_SEQUENCE_FLOW = newArgs0("ACTY01014", BpmValidationErrorMessages.getString("ACTY01014"));

   //Conditions related
   public static final Args1 COND_NOT_AN_EXCEPTION_CLASS = newArgs1("COND01001", BpmValidationErrorMessages.getString("COND01001"));
   public static final Args0 COND_NO_CONDITION_SPECIFIED = newArgs0("COND01002", BpmValidationErrorMessages.getString("COND01002"));
   public static final Args0 COND_TARGET_STATE_IN_SAME_STATE_AS_SOURCE_STATE = newArgs0("COND01003", BpmValidationErrorMessages.getString("COND01003"));
   public static final Args0 COND_INVALID_DATA_MAPPING = newArgs0("COND01004", BpmValidationErrorMessages.getString("COND01004"));
   public static final Args0 COND_INVALID_DATA_PATH = newArgs0("COND01005", BpmValidationErrorMessages.getString("COND01005"));
   public static final Args0 COND_INVALID_DATA_SPECIFIED = newArgs0("COND01006", BpmValidationErrorMessages.getString("COND01006"));
   public static final Args0 COND_NO_PROCESS_OR_ACTIVITY_CONTEXT = newArgs0("COND01007", BpmValidationErrorMessages.getString("COND01007"));
   public static final Args0 COND_NO_PERIOD_SPECIFIED = newArgs0("COND01008", BpmValidationErrorMessages.getString("COND01008"));
   public static final Args0 COND_NO_DATA_SPECIFIED = newArgs0("COND01009", BpmValidationErrorMessages.getString("COND01009"));

   //Trigger related
   public static final Args1 TRIGG_UNSPECIFIED_PARTICIPANT_FOR_TRIGGER = newArgs1("TRIGG01001", BpmValidationErrorMessages.getString("TRIGG01001"));
   public static final Args2 TRIGG_INVALID_PARTICIPANT_FOR_TRIGGER = newArgs2("TRIGG01002", BpmValidationErrorMessages.getString("TRIGG01002"));
   public static final Args0 TRIGG_UNSPECIFIED_START_TIME_FOR_TRIGGER = newArgs0("TRIGG01003", BpmValidationErrorMessages.getString("TRIGG01003"));
   public static final Args1 TRIGG_DUPLICATE_ID_FOR_PROCESS_DEFINITION = newArgs1("TRIGG01004", BpmValidationErrorMessages.getString("TRIGG01004"));
   public static final Args3 TRIGG_ID_EXCEEDS_MAXIMUM_LENGTH = newArgs3("TRIGG01005", BpmValidationErrorMessages.getString("TRIGG01005"));
   public static final Args0 TRIGG_NO_NAME_SET = newArgs0("TRIGG01006", BpmValidationErrorMessages.getString("TRIGG01006"));
   public static final Args0 TRIGG_NO_TYPE_SET = newArgs0("TRIGG01007", BpmValidationErrorMessages.getString("TRIGG01007"));
   public static final Args0 TRIGG_PARAMETER_MAPPING_DOES_NOT_SPECIFY_DATA = newArgs0("TRIGG01008", BpmValidationErrorMessages.getString("TRIGG01008"));
   public static final Args0 TRIGG_PARAMETER_MAPPING_DOES_NOT_SPECIFY_PARAMETER = newArgs0("TRIGG01009", BpmValidationErrorMessages.getString("TRIGG01009"));
   public static final Args1 TRIGG_PARAMETER_FOR_PARAMETER_MAPPING_INVALID = newArgs1("TRIGG01010", BpmValidationErrorMessages.getString("TRIGG01010"));
   public static final Args0 TRIGG_ACCESSPOINT_HAS_INVALID_ID = newArgs0("TRIGG01011", BpmValidationErrorMessages.getString("TRIGG01011"));
   public static final Args1 TRIGG_SCAN_TRIGGERS_DO_NOT_SUPPORT_ACCESS_POINT_TYPE = newArgs1("TRIGG01012", BpmValidationErrorMessages.getString("TRIGG01012"));
   public static final Args1 TRIGG_PARAMETER_MAPPING_CONTAINS_AN_INVALID_TYPE_CONVERSION = newArgs1("TRIGG01013", BpmValidationErrorMessages.getString("TRIGG01013"));
   public static final Args0 TRIGG_UNSPECIFIED_MESSAGE_TYPE_FOR_JMS_TRIGGER = newArgs0("TRIGG01014", BpmValidationErrorMessages.getString("TRIGG01014"));
   public static final Args0 TRIGG_PARAMETER_HAS_NO_ID = newArgs0("TRIGG01015", BpmValidationErrorMessages.getString("TRIGG01015"));
   public static final Args0 TRIGG_PARAMETER_HAS_INVALID_ID_DEFINED = newArgs0("TRIGG01016", BpmValidationErrorMessages.getString("TRIGG01016"));
   public static final Args1 TRIGG_NO_LOCATION_FOR_PARAMETER_SPECIFIED = newArgs1("TRIGG01017", BpmValidationErrorMessages.getString("TRIGG01017"));
   public static final Args2 TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_CANNOT_BE_FOUND = newArgs2("TRIGG01018", BpmValidationErrorMessages.getString("TRIGG01018"));
   public static final Args2 TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_COULD_NOT_BE_LOADED = newArgs2("TRIGG01019", BpmValidationErrorMessages.getString("TRIGG01019"));
   public static final Args0 TRIGG_UNSPECIFIED_USER_NAME_FOR_MAIL_TRIGGER = newArgs0("TRIGG01020", BpmValidationErrorMessages.getString("TRIGG01020"));
   public static final Args0 TRIGG_UNSPECIFIED_PASSWORD_FOR_MAIL_TRIGGER = newArgs0("TRIGG01021", BpmValidationErrorMessages.getString("TRIGG01021"));
   public static final Args0 TRIGG_UNSPECIFIED_SERVER_NAME_FOR_MAIL_TRIGGER = newArgs0("TRIGG01022", BpmValidationErrorMessages.getString("TRIGG01022"));
   public static final Args0 TRIGG_UNSPECIFIED_PROTOCOL_FOR_MAIL_TRIGGER = newArgs0("TRIGG01023", BpmValidationErrorMessages.getString("TRIGG01023"));

   //Transition related
   public static final Args2 TRAN_ID_EXCEEDS_MAXIMUM_LENGTH = newArgs2("TRAN01001", BpmValidationErrorMessages.getString("TRAN01001"));
   public static final Args2 TRAN_NO_BOUNDARY_EVENT_HANDLER_WITH_ID_FOR_EXCEPTION_TRANSITION_FOUND = newArgs2("TRAN01002", BpmValidationErrorMessages.getString("TRAN01002"));
   public static final Args1 TRAN_EXPRESSION_SCRIPTING_LANGUAGE_DO_NOT_MATCH = newArgs1("TRAN01003", BpmValidationErrorMessages.getString("TRAN01003"));
   public static final Args1 TRAN_UNSUPPORTED_SCRIPTING_LANGUAGE = newArgs1("TRAN01004", BpmValidationErrorMessages.getString("TRAN01004"));

   //Structured Types related
   public static final Args1 SDT_DUPLICATE_ID_FOR_TYPE_DECLARATION = newArgs1("SDT01000", BpmValidationErrorMessages.getString("SDT01000"));
   public static final Args1 SDT_TYPE_DECLARATION_NOT_ALLOWED_TO_CONTAIN_VARIABLES = newArgs1("SDT01001", BpmValidationErrorMessages.getString("SDT01001"));
   public static final Args2 SDT_REFERENCED_PARENT_TYPE_NOT_FOUND = newArgs2("SDT01002", BpmValidationErrorMessages.getString("SDT01002"));

   //Participant related
   public static final Args1 PART_NO_DATA_ASSOCIATED_TO_CONDITIONAL_PERFORMER = newArgs1("PART01001", BpmValidationErrorMessages.getString("PART01001"));
   public static final Args1 PART_DATA_EXPRESSION_OF_UNSUPPORTED_TYPE = newArgs1("PART01002", BpmValidationErrorMessages.getString("PART01002"));
   public static final Args1 PART_DATA_REALM_EXPRESSION_OF_UNSUPPORTED_TYPE = newArgs1("PART01003", BpmValidationErrorMessages.getString("PART01003"));
   public static final Args0 PART_MISSING_ADMINISTRATOR_PARTICIPANT = newArgs0("PART01004", BpmValidationErrorMessages.getString("PART01004"));
   public static final Args0 PART_ADMINISTRATOR_PARTICIPANT_MUST_BE_A_ROLE = newArgs0("PART01005", BpmValidationErrorMessages.getString("PART01005"));
   public static final Args0 PART_ADMINISTRATOR_IS_NOT_ALLOWED_TO_HAVE_RELATIONSHIPS_TO_ANY_ORGANIZATION = newArgs0("PART01006", BpmValidationErrorMessages.getString("PART01006"));
   public static final Args0 PART_SCOPED_PARTICIPANTS_NOT_ALLOWED_FOR_MODEL_LEVEL_GRANTS = newArgs0("PART01007", BpmValidationErrorMessages.getString("PART01007"));
   public static final Args1 PART_DUPLICATE_ID = newArgs1("PART01008", BpmValidationErrorMessages.getString("PART01008"));
   public static final Args2 PART_ID_EXCEEDS_MAXIMUM_LENGTH = newArgs2("PART01009", BpmValidationErrorMessages.getString("PART01009"));
   public static final Args2 PART_ASSOCIATED_ORGANIZATION_SET_FOR_PARTICIPANT_DOES_NOT_EXIST = newArgs2("PART01010", BpmValidationErrorMessages.getString("PART01010"));
   public static final Args1 PART_DATA_FOR_SCOPED_ORGANIZATION_MUST_EXIST = newArgs1("PART01011", BpmValidationErrorMessages.getString("PART01011"));
   public static final Args1 PART_DATA_OF_SCOPED_ORGANIZATION_CAN_ONLY_BE_PRIM_OR_STRUCT = newArgs1("PART01012", BpmValidationErrorMessages.getString("PART01012"));
   public static final Args1 PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL = newArgs1("PART01013", BpmValidationErrorMessages.getString("PART01013"));
   public static final Args1 PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL_WHEN_SDT_IS_USED = newArgs1("PART01014", BpmValidationErrorMessages.getString("PART01014"));
   public static final Args3 PART_TYPE_OF_DATA_OF_SCOPED_ORGANIZATION_IS_NOT = newArgs3("PART01015", BpmValidationErrorMessages.getString("PART01015"));
   public static final Args1 PART_ORGANIZATION_IS_SCOPED_BUT_IN_AUDITTRAIL_UNSCOPED = newArgs1("PART01016", BpmValidationErrorMessages.getString("PART01016"));
   public static final Args1 PART_ORGANIZATION_IS_UNSCOPED_BUT_IN_AUDITTRAIL_SCOPED = newArgs1("PART01017", BpmValidationErrorMessages.getString("PART01017"));
   public static final Args3 PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_ID_IN_AUDIT_TRAIL = newArgs3("PART01018", BpmValidationErrorMessages.getString("PART01018"));
   public static final Args3 PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_PATH_IN_AUDIT_TRAIL = newArgs3("PART01019", BpmValidationErrorMessages.getString("PART01019"));
   public static final Args0 PART_MULTIPLE_SOPER_ORGANIZATIONS_ARE_NOT_ALLOWED = newArgs0("PART01020", BpmValidationErrorMessages.getString("PART01020"));
   public static final Args1 PART_MODEL_CONTAINS_DIFFERENT_MANAGER_OF_ASSOCIATION_THAN_DEPLOYED_MODEL = newArgs1("PART01021", BpmValidationErrorMessages.getString("PART01021"));
   public static final Args1 PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL = newArgs1("PART01022", BpmValidationErrorMessages.getString("PART01022"));

   //Data/Datamapping/Datapath related
   public static final Args1 DATA_DUPLICATE_ID_FOR_DATA = newArgs1("DATA01001", BpmValidationErrorMessages.getString("DATA01001"));
   public static final Args2 DATA_ID_FOR_DATA_EXCEEDS_MAXIMUM_LENGTH_OF_CHARACTERS = newArgs2("DATA01002", BpmValidationErrorMessages.getString("DATA01002"));
   public static final Args1 DATA_NO_ACTIVITY_SET_FOR_DATAMAPPING = newArgs1("DATA01003", BpmValidationErrorMessages.getString("DATA01003"));
   public static final Args1 DATA_NO_DATA_SET_FOR_DATAMAPPING = newArgs1("DATA01004", BpmValidationErrorMessages.getString("DATA01004"));
   public static final Args1 DATA_NO_USEFUL_ID_SET_FOR_DATAMAPPING = newArgs1("DATA01005", BpmValidationErrorMessages.getString("DATA01005"));
   public static final Args1 DATA_NO_USEFUL_NAME_SET_FOR_DATAMAPPING = newArgs1("DATA01006", BpmValidationErrorMessages.getString("DATA01006"));
   public static final Args2 DATA_DATAMAPPING_HAS_NO_UNIQUE_ID_FOR_DIRECTION = newArgs2("DATA01007", BpmValidationErrorMessages.getString("DATA01007"));
   public static final Args1 DATA_CANNOT_RESOLVE_ACCESSPOINTPROVIDER_FOR_DATAMAPPING = newArgs1("DATA01008", BpmValidationErrorMessages.getString("DATA01008"));
   public static final Args2 DATA_INVALID_CONTEXT_FOR_DATAMAPPING = newArgs2("DATA01009", BpmValidationErrorMessages.getString("DATA01009"));
   public static final Args1 DATA_INVALID_APPLICATION_FOR_DATAMAPPING = newArgs1("DATA01010", BpmValidationErrorMessages.getString("DATA01010"));
   public static final Args2 DATA_INVALID_DATATYPE_FOR_DATAMAPPING = newArgs2("DATA01011", BpmValidationErrorMessages.getString("DATA01011"));
   public static final Args2 DATA_FORMAL_PARAMETER_NOT_RESOLVABLE_FOR_DATAMAPPING = newArgs2("DATA01012", BpmValidationErrorMessages.getString("DATA01012"));
   public static final Args2 DATA_APPLICATION_ACCESS_POINT_NOT_RESOLVABLE_FOR_DATAMAPPING = newArgs2("DATA01013", BpmValidationErrorMessages.getString("DATA01013"));
   public static final Args1 DATA_NO_APPLICATION_ACCESS_POINT_SET_FOR_DATAMAPPING = newArgs1("DATA01014", BpmValidationErrorMessages.getString("DATA01014"));
   public static final Args1 DATA_NO_CONTEXT_SET_FOR_DATAMAPPING = newArgs1("DATA01015", BpmValidationErrorMessages.getString("DATA01015"));
   public static final Args2 DATA_CONTEXT_FOR_DATAMAPPING_UNDEFINED = newArgs2("DATA01016", BpmValidationErrorMessages.getString("DATA01016"));
   public static final Args1 DATA_INVALID_DATAPATH_FOR_DATAMAPPING = newArgs1("DATA01017", BpmValidationErrorMessages.getString("DATA01017"));
   public static final Args1 DATA_DUPLICATE_ID_FOR_DATAPATH = newArgs1("DATA01018", BpmValidationErrorMessages.getString("DATA01018"));
   public static final Args0 DATA_NO_NAME_SPECIFIED_FOR_DATAPATH = newArgs0("DATA01019", BpmValidationErrorMessages.getString("DATA01019"));
   public static final Args0 DATA_NO_DATA_SPECIFIED_FOR_DATAPATH = newArgs0("DATA01020", BpmValidationErrorMessages.getString("DATA01020"));
   public static final Args0 DATA_KEY_DESCRIPTORS_MUST_BE_PRIMITIVE_OR_STRUCTURED_TYPES = newArgs0("DATA01021", BpmValidationErrorMessages.getString("DATA01021"));
   public static final Args0 DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_HAVE_PRIMITIVE_TYPE = newArgs0("DATA01022", BpmValidationErrorMessages.getString("DATA01022"));
   public static final Args0 DATA_NO_SCHEMA_FOUND_FOR_STRUCTURED_KEY_DESCRIPTOR = newArgs0("DATA01023", BpmValidationErrorMessages.getString("DATA01023"));
   public static final Args0 DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_BE_INDEXED_AND_PERSISTENT = newArgs0("DATA01024", BpmValidationErrorMessages.getString("DATA01024"));
   public static final Args0 DATA_DATAPATH_IS_NOT_A_DESCRIPTOR = newArgs0("DATA01025", BpmValidationErrorMessages.getString("DATA01025"));
   public static final Args0 DATA_UNSPECIFIED_TYPE_FOR_PRIMITIVE_DATA = newArgs0("DATA01026", BpmValidationErrorMessages.getString("DATA01026"));

   //Event related
   public static final Args1 EVEN_DUPLICATE_ID_FOR_EVENT_HANDLER = newArgs1("EVEN01000", BpmValidationErrorMessages.getString("EVEN01000"));
   public static final Args2 EVEN_ID_FOR_EVENT_HANDLER_EXCEEDS_MAXIMUM_LENGTH_OF_CHARACTERS = newArgs2("EVEN01001", BpmValidationErrorMessages.getString("EVEN01001"));
   public static final Args0 EVEN_HANDLER_DOES_NOT_HAVE_CONDITION_TYPE = newArgs0("EVEN01002", BpmValidationErrorMessages.getString("EVEN01002"));
   public static final Args1 EVEN_DUPLICATE_ID_FOR_EVENT_ACTION = newArgs1("EVEN01003", BpmValidationErrorMessages.getString("EVEN01003"));

   //Plain Java / EJB / Serializable related
   public static final Args0 JAVA_CLASS_NOT_SPECIFIED = newArgs0("JAVA01001", BpmValidationErrorMessages.getString("JAVA01001"));
   public static final Args0 JAVA_COMPLETION_METHOD_NOT_SPECIFIED = newArgs0("JAVA01002", BpmValidationErrorMessages.getString("JAVA01002"));
   public static final Args2 JAVA_COULD_NOT_FIND_METHOD_IN_CLASS = newArgs2("JAVA01003", BpmValidationErrorMessages.getString("JAVA01003"));
   public static final Args0 JAVA_CONSTRUCTOR_NOT_SPECIFIED = newArgs0("JAVA01004", BpmValidationErrorMessages.getString("JAVA01004"));
   public static final Args2 JAVA_COULD_NOT_FIND_CONSTRUCTOR_IN_CLASS = newArgs2("JAVA01005", BpmValidationErrorMessages.getString("JAVA01005"));
   public static final Args1 JAVA_CLASS_NOT_FOUND = newArgs1("JAVA01006", BpmValidationErrorMessages.getString("JAVA01006"));
   public static final Args1 JAVA_CLASS_COULD_NOT_BE_LOADED = newArgs1("JAVA01007", BpmValidationErrorMessages.getString("JAVA01007"));
   public static final Args0 JAVA_EMPTY_CLASSNAME = newArgs0("JAVA01008", BpmValidationErrorMessages.getString("JAVA01008"));
   public static final Args1 JAVA_CLASS_HAS_NO_DEFAULT_CONSTRUCTOR = newArgs1("JAVA01009", BpmValidationErrorMessages.getString("JAVA01009"));
   public static final Args2 JAVA_CANNOT_LOAD_CLASS = newArgs2("JAVA01010", BpmValidationErrorMessages.getString("JAVA01010"));
   public static final Args1 JAVA_INTERFACE_NOT_SPECIFIED = newArgs1("JAVA01011", BpmValidationErrorMessages.getString("JAVA01011"));
   public static final Args1 JAVA_METHOD_NOT_SPECIFIED = newArgs1("JAVA01012", BpmValidationErrorMessages.getString("JAVA01012"));
   public static final Args0 JAVA_BUSINESS_INTERFACE_NOT_SPECIFIED = newArgs0("JAVA01013", BpmValidationErrorMessages.getString("JAVA01013"));
   public static final Args0 JAVA_BEAN_TYPE_NOT_SPECIFIED = newArgs0("JAVA01014", BpmValidationErrorMessages.getString("JAVA01014"));
   public static final Args0 JAVA_BEAN_ID_NOT_SPECIFIED = newArgs0("JAVA01015", BpmValidationErrorMessages.getString("JAVA01015"));

   private static final Object[] NONE = {};

   private final String defaultMessage;

   private final Object[] args;

   private BpmValidationError(String id)
   {
      this(id, null);
   }

   private BpmValidationError(String id, String defaultMessage)
   {
      this(id, defaultMessage, NONE);
   }

   private BpmValidationError(String code, String defaultMessage, Object msgArgs[])
   {
      super(code);

      this.defaultMessage = defaultMessage;
      this.args = msgArgs;
   }

   public String getDefaultMessage()
   {
      return defaultMessage;
   }

   public Object[] getMessageArgs()
   {
      return args;
   }

   public String toString()
   {
      return getId() + " - " + MessageFormat.format(getDefaultMessage(), args); //$NON-NLS-1$
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args0 newArgs0(String errorCode)
   {
      return new Args0(errorCode, BpmValidationErrorMessages.getString(errorCode));
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args0 newArgs0(String errorCode, String defaultMessage)
   {
      return new Args0(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args1 newArgs1(String errorCode)
   {
      return new Args1(errorCode, BpmValidationErrorMessages.getString(errorCode));
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args1 newArgs1(String errorCode, String defaultMessage)
   {
      return new Args1(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args2 newArgs2(String errorCode)
   {
      return new Args2(errorCode, BpmValidationErrorMessages.getString(errorCode));
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args2 newArgs2(String errorCode, String defaultMessage)
   {
      return new Args2(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args3 newArgs3(String errorCode, String defaultMessage)
   {
      return new Args3(errorCode, defaultMessage);
   }

   /**
    * Static factory to prepare for future generification.
    */
   public static Args4 newArgs4(String errorCode, String defaultMessage)
   {
      return new Args4(errorCode, defaultMessage);
   }

   public static class Args0 extends AbstractErrorFactory
   {
      private Args0(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise()
      {
         return buildError(NONE);
      }
   }

   public static class Args1 extends AbstractErrorFactory
   {
      private Args1(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg)
      {
         return buildError(new Object[] {arg});
      }

      public BpmValidationError raise(long arg)
      {
         return buildError(new Object[] {new Long(arg)});
      }
   }

   public static class Args2 extends AbstractErrorFactory
   {
      private Args2(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg1, Object arg2)
      {
         return buildError(new Object[] {arg1, arg2});
      }

      public BpmValidationError raise(long arg1, long arg2)
      {
         return buildError(new Object[] {new Long(arg1), new Long(arg2)});
      }
   }

   public static class Args3 extends AbstractErrorFactory
   {
      private Args3(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg1, Object arg2, Object arg3)
      {
         return buildError(new Object[] { arg1, arg2, arg3 });
      }

      public BpmValidationError raise(long arg1, long arg2, long arg3)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3) });
      }
   }

   public static class Args4 extends AbstractErrorFactory
   {
      private Args4(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object arg1, Object arg2, Object arg3, Object arg4)
      {
         return buildError(new Object[] { arg1, arg2, arg3, arg4 });
      }

      public BpmValidationError raise(long arg1, long arg2, long arg3, long arg4)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3), new Long(arg4) });
      }
   }

   public static Args newArgs(String errorCode)
   {
      return new Args(errorCode, BpmValidationErrorMessages.getString(errorCode));
   }

   public static class Args extends AbstractErrorFactory
   {
      private Args(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmValidationError raise(Object ... arg)
      {
         return buildError(arg);
      }
   }

   static abstract class AbstractErrorFactory
   {
      private final String errorCode;

      private final String defaultMessage;

      protected AbstractErrorFactory(String errorCode, String defaultMessage)
      {
         this.errorCode = errorCode;
         this.defaultMessage = defaultMessage;
      }

      protected BpmValidationError buildError(Object[] args)
      {
         return new BpmValidationError(errorCode, defaultMessage, args);
      }
   }

   static BpmValidationError createError(String id, String defaultMessage, Object arg0)
   {
      return new BpmValidationError(id, defaultMessage, new Object[] {arg0});
   }

}
