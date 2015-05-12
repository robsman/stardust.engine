/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
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
 * Defines constants for parameterized error codes with messages prepared for I18N.
 *
 * @author sauer
 * @version $Revision: $
 */
public class BpmRuntimeError extends ErrorCase
{

   private static final long serialVersionUID = 1L;

   //// Model related

   public static final Args0 MDL_MODEL_MANAGER_UNAVAILABLE = newArgs0("MDL01000", BpmRuntimeErrorMessages.getString("MDL01000")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 MDL_NO_MODEL = newArgs0("MDL01001", BpmRuntimeErrorMessages.getString("MDL01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_NO_ACTIVE_MODEL = newArgs0("MDL01002", BpmRuntimeErrorMessages.getString("MDL01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_MODEL_OID = newArgs1("MDL01003", BpmRuntimeErrorMessages.getString("MDL01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_NO_ACTIVE_MODEL_WITH_ID = newArgs1("MDL01004", BpmRuntimeErrorMessages.getString("MDL01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_NO_MATCHING_MODEL_WITH_ID = newArgs1("MDL01005", BpmRuntimeErrorMessages.getString("MDL01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_INVALID_IPP_MODEL_FILE = newArgs1("MDL01008", BpmRuntimeErrorMessages.getString("MDL01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_INVALID_IPP_MODEL_FILE_GENERAL_PARSE_ERROR = newArgs1("MDL01009", BpmRuntimeErrorMessages.getString("MDL01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_IPP_VERSION = newArgs2("MDL01006", BpmRuntimeErrorMessages.getString("MDL01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNSUPPORTED_IPP_VERSION = newArgs2("MDL01007", BpmRuntimeErrorMessages.getString("MDL01007")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 MDL_UNKNOWN_MODEL_ELEMENT = newArgs0("MDL01100", BpmRuntimeErrorMessages.getString("MDL01100")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 MDL_NO_MATCHING_PROCESS_DEFINITION = newArgs0("MDL01101", BpmRuntimeErrorMessages.getString("MDL01101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_PROCESS_DEFINITION_ID = newArgs1("MDL_01102", BpmRuntimeErrorMessages.getString("MDL_01102")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_PROCESS_FOR_PI = newArgs2("MDL_01103", BpmRuntimeErrorMessages.getString("MDL_01103")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_DATA_FOR_FORMAL_PARAMETER = newArgs2("MDL_01104", BpmRuntimeErrorMessages.getString("MDL_01104")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_NO_IMPLEMENTATION_PROCESS = newArgs2("MDL01105", BpmRuntimeErrorMessages.getString("MDL01105")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 MDL_NO_MATCHING_ACTIVITY_DEFINITION = newArgs0("MDL01111", BpmRuntimeErrorMessages.getString("MDL01111")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_ACTIVITY_DEFINITION = newArgs1("MDL_01112", BpmRuntimeErrorMessages.getString("MDL_01112")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_ACTIVITY_FOR_AI = newArgs2("MDL_01113", BpmRuntimeErrorMessages.getString("MDL_01113")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_ACTIVITY_FOR_WORK_ITEM = newArgs2("MDL_01114", BpmRuntimeErrorMessages.getString("MDL_01114")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_ACTIVITY_IN_MODEL = newArgs2("MDL_01115"); //$NON-NLS-1$

   public static final Args1 MDL_UNKNOWN_DATA_ID = newArgs1("MDL01122", BpmRuntimeErrorMessages.getString("MDL01122")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_XPATH = newArgs1("MDL01123", BpmRuntimeErrorMessages.getString("MDL01123")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 MDL_UNKNOWN_XPATH_FOR_DATA_ID = newArgs3("MDL01124", BpmRuntimeErrorMessages.getString("MDL01124")); //$NON-NLS-1$ //$NON-NLS-2$


   public static final Args2 MDL_UNKNOWN_DATA_PATH = newArgs2("MDL01132", BpmRuntimeErrorMessages.getString("MDL01132")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_IN_DATA_PATH = newArgs2("MDL01134", BpmRuntimeErrorMessages.getString("MDL01134")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_OUT_DATA_PATH = newArgs2("MDL01136", BpmRuntimeErrorMessages.getString("MDL01136")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args3 MDL_UNKNOWN_DATA_MAPPING = newArgs3("MDL01142", BpmRuntimeErrorMessages.getString("MDL01142")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 MDL_UNKNOWN_IN_DATA_MAPPING = newArgs3("MDL01144", BpmRuntimeErrorMessages.getString("MDL01144")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 MDL_UNKNOWN_OUT_DATA_MAPPING = newArgs3("MDL01146", BpmRuntimeErrorMessages.getString("MDL01146")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_UNKNOWN_APP_CONTEXT = newArgs1("MDL01152", BpmRuntimeErrorMessages.getString("MDL01152")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_UNKNOWN_PARTICIPANT_RUNTIME_OID = newArgs1("MDL01161", BpmRuntimeErrorMessages.getString("MDL01161")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_PARTICIPANT_ID = newArgs1("MDL01162", BpmRuntimeErrorMessages.getString("MDL01162")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_UNKNOWN_PARTICIPANT_ID_FOR_MODEL = newArgs2("MDL01163", BpmRuntimeErrorMessages.getString("MDL01163")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_UNKNOWN_EVENT_HANDLER_ID = newArgs1("MDL01172", BpmRuntimeErrorMessages.getString("MDL01172")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_UNKNOWN_TYPE_DECLARATION_ID = newArgs1("MDL01182", BpmRuntimeErrorMessages.getString("MDL01182")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_DATA_TYPE_ID = newArgs1("MDL01183", BpmRuntimeErrorMessages.getString("MDL01183")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 MDL_NO_JAVA_APPLICATION_CLASS_PATH = newArgs0("MDL01191", BpmRuntimeErrorMessages.getString("MDL01191")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_NO_JAVA_APPLICATION_METHOD = newArgs0("MDL01192", BpmRuntimeErrorMessages.getString("MDL01192")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_DANGLING_DATA_PATH = newArgs1("MDL02021", BpmRuntimeErrorMessages.getString("MDL02021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_DANGLING_IN_DATA_PATH = newArgs1("MDL02022", BpmRuntimeErrorMessages.getString("MDL02022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_DANGLING_OUT_DATA_PATH = newArgs1("MDL02023", BpmRuntimeErrorMessages.getString("MDL02023")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_DTD_VALIDATION_FAILED = newArgs1("MDL03001", BpmRuntimeErrorMessages.getString("MDL03001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_XSD_VALIDATION_FAILED = newArgs1("MDL03002", BpmRuntimeErrorMessages.getString("MDL03002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_INVALID_DOCUMENT_ROOT_TYPE = newArgs1("MDL03003", BpmRuntimeErrorMessages.getString("MDL03003")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_INVALID_QA_CODE_ID = newArgs1("MDL03004", BpmRuntimeErrorMessages.getString("MDL03004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_DUPLICATE_QA_CODE = newArgs2("MDL03005", BpmRuntimeErrorMessages.getString("MDL03005")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_CONTEXT_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04001", BpmRuntimeErrorMessages.getString("MDL04001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_EXCEPTION_DURING_CONSISTENCY_CHECK_OF_CONDITIONAL_PERFORMER = newArgs1("MDL04002", BpmRuntimeErrorMessages.getString("MDL04002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNSUPPORTED_CONDITIONAL_PERFORMER_KIND = newArgs1("MDL04003", BpmRuntimeErrorMessages.getString("MDL04003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_FAILED_RESOLVING_CONDITIONAL_PERFORMER_IDENTITY = newArgs0("MDL04004", BpmRuntimeErrorMessages.getString("MDL04004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_CONDITIONAL_PERFORMER_WAS_RESOLVED_AS = newArgs1("MDL04005", BpmRuntimeErrorMessages.getString("MDL04005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_CANNOT_RETRIEVE_CONDITIONAL_PARTICIPANT_PERFORMER_FOR_HANDLE = newArgs1("MDL04006", BpmRuntimeErrorMessages.getString("MDL04006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_CANNOT_CREATE_MODEL_FROM_FILE = newArgs1("MDL04007", BpmRuntimeErrorMessages.getString("MDL04007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_CANNOT_WRITE_TO_FILE = newArgs1("MDL04008", BpmRuntimeErrorMessages.getString("MDL04008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_USER_DOES_NOT_EXIST_OR_PASSWORD_INCORRECT = newArgs1("MDL04009", BpmRuntimeErrorMessages.getString("MDL04009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_TYPEDECLARATION_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04010", BpmRuntimeErrorMessages.getString("MDL04010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_APPLICATION_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04011", BpmRuntimeErrorMessages.getString("MDL04011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_WORKFLOW_DATA_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04012", BpmRuntimeErrorMessages.getString("MDL04012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_PROCESS_DEFINITION_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04013", BpmRuntimeErrorMessages.getString("MDL04013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_INVALID_SYMBOL = newArgs1("MDL04014", BpmRuntimeErrorMessages.getString("MDL04014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_ORGANIZATION_CANNOT_BE_IST_OWN_SUB_SUPERORGANIZATION = newArgs1("MDL04015", BpmRuntimeErrorMessages.getString("MDL04015")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_ORGANIZATION_IS_ALREADY_SUBORGANIZATION_OF_ORGANIZATION = newArgs1("MDL04016", BpmRuntimeErrorMessages.getString("MDL04016")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_PARTITION_NOT_INITIALIZED = newArgs0("MDL04017", BpmRuntimeErrorMessages.getString("MDL04017")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_ACTIVITY_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04018", BpmRuntimeErrorMessages.getString("MDL04018")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_TRANSITION_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04019", BpmRuntimeErrorMessages.getString("MDL04019")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_RELOCATION_TRANSITION_MUST_NOT_HAVE_ANY_SOURCE_OR_TARGET_ACTIVITY_ATTACHED = newArgs1("MDL04020", BpmRuntimeErrorMessages.getString("MDL04020")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_FROM_ACTIVITY_DOES_NOT_BELONG_TO = newArgs2("MDL04021", BpmRuntimeErrorMessages.getString("MDL04021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_TO_ACTIVITY_DOES_NOT_BELONG_TO = newArgs2("MDL04022", BpmRuntimeErrorMessages.getString("MDL04022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_MULTIPLE_INCOMING_TRANSITIONS_ARE_ONLY_ALLOWED_FOR_AND_OR_XOR_ACTIVITY_JOINS = newArgs2("MDL04023", BpmRuntimeErrorMessages.getString("MDL04023")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_MULTIPLE_OUTGOING_TRANSITIONS_ARE_ONLY_ALLOWED_FOR_AND_OR_XOR_ACTIVITY_SPLITS = newArgs2("MDL04024", BpmRuntimeErrorMessages.getString("MDL04024")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_THE_SELECTED_SYMBOL_DOES_NOT_REPRESENT_AN_ACTIVITY = newArgs0("MDL04025", BpmRuntimeErrorMessages.getString("MDL04025")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_CONNECTION_BETWEEN_SYMBOLS_ALREADY_EXIST = newArgs0("MDL04026", BpmRuntimeErrorMessages.getString("MDL04026")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_ROUTE_ACTIVITY_DOES_NOT_PARTICIPATE_IN_DATA_FLOW = newArgs0("MDL04027", BpmRuntimeErrorMessages.getString("MDL04027")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_USEROBJECT_OF_THE_FIRST_SYMBOL_IS_NOT_VALID_FOR_THE_LINKTYPE = newArgs0("MDL04028", BpmRuntimeErrorMessages.getString("MDL04028")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_SELECTED_SYMBOL_DOES_NOT_REPRESENT_AN_ORGANIZATION = newArgs0("MDL04029", BpmRuntimeErrorMessages.getString("MDL04029")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_ONE_OF_THE_CONNECTED_SYMBOLS_MUST_BE_AN_ANNOTATION = newArgs0("MDL04030", BpmRuntimeErrorMessages.getString("MDL04030")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_ANNOTATION_CANNOT_REFER_TO_ITSELF = newArgs0("MDL04031", BpmRuntimeErrorMessages.getString("MDL04031")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_ROUTE_ACTIVITY_CANNOT_HAVE_A_PERFORMER = newArgs0("MDL04032", BpmRuntimeErrorMessages.getString("MDL04032")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_SUBPROCESS_ACTIVITY_CANNOT_HAVE_A_PERFORMER = newArgs0("MDL04033", BpmRuntimeErrorMessages.getString("MDL04033")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_ACTIVITY_PERFORMING_NON_INTERACTIVE_APPLICATION_CANNOT_HAVE_PERFORMER = newArgs0("MDL04034", BpmRuntimeErrorMessages.getString("MDL04034")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_SELECTED_SYMBOL_DOES_NOT_REPRESENT_A_PROCESS_DEFINITION = newArgs0("MDL04035", BpmRuntimeErrorMessages.getString("MDL04035")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_BOTH_CONNECTED_SYMBOLS_MUST_BE_ACTIVITIES = newArgs0("MDL04036", BpmRuntimeErrorMessages.getString("MDL04036")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_ROOT_MODEL_WITH_ID_ALREADY_EXISTS = newArgs1("MDL04037", BpmRuntimeErrorMessages.getString("MDL04037")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_UNABLE_TO_DELETE_MODEL_IT_PROVIDES_A_PRIMARY_IMPLEMENTATION = newArgs0("MDL04038", BpmRuntimeErrorMessages.getString("MDL04038")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_UNABLE_TO_DELETE_MODEL_IT_IS_REFERENCED_BY_AT_LEAST_ONE_OTHER_MODEL = newArgs0("MDL04039", BpmRuntimeErrorMessages.getString("MDL04039")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_NULL_VALUES_ARE_NOT_SUPPORTED_WITH_OPERATOR = newArgs1("MDL04040", BpmRuntimeErrorMessages.getString("MDL04040")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_INCONSISTENT_OPERATOR_USE = newArgs2("MDL04041", BpmRuntimeErrorMessages.getString("MDL04041")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_NO_WORKFLOW_DATA_DEFINED_WITH_ID_IN_THIS_MODEL_VERSION = newArgs1("MDL04042", BpmRuntimeErrorMessages.getString("MDL04042")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_FAILED_EVALUATING_XPATH_EXPRESSION = newArgs1("MDL04043", BpmRuntimeErrorMessages.getString("MDL04043")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_CONTEXT_ELEMENT_NOT_INITIALIZED = newArgs0("MDL04044", BpmRuntimeErrorMessages.getString("MDL04044")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_XPATH_EXPRESSION_UNABLE_TO_FIND_ANY_SUITABLE_NODE = newArgs1("MDL04045", BpmRuntimeErrorMessages.getString("MDL04045")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_XPATH_EXPRESSION_EVALUATED_TO_MULTIPLE_NODES = newArgs1("MDL04046", BpmRuntimeErrorMessages.getString("MDL04046")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_XPATH_EXPRESSION_EVALUATED_TO_A_NON_NODE_VALUE = newArgs1("MDL04047", BpmRuntimeErrorMessages.getString("MDL04047")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 MDL_INVALID_XML_ACCESS_POINT = newArgs0("MDL04048", BpmRuntimeErrorMessages.getString("MDL04048")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 MDL_DEPARTMENT_HIERARCHY_ENTRY_ALREADY_EXISTS = newArgs2("MDL04049", BpmRuntimeErrorMessages.getString("MDL04049")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_EVENT_TYPE = newArgs1("MDL04050", BpmRuntimeErrorMessages.getString("MDL04050")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_NO_PROPERTY_SPECIFIED = newArgs1("MDL04051", BpmRuntimeErrorMessages.getString("MDL04051")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_CANNOT_SEND_NOTIFICATION_MESSAGE = newArgs1("MDL04052", BpmRuntimeErrorMessages.getString("MDL04052")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Audittrail related

   public static final Args0 ATDB_AUDIT_TRAIL_UNAVAILABLE = newArgs0("ATDB00001", BpmRuntimeErrorMessages.getString("ATDB00001")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_ARCHIVE_AUDIT_TRAIL_WRITE_PROTECTED = newArgs0("ATDB00011", BpmRuntimeErrorMessages.getString("ATDB00011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_ARCHIVE_UNABLE_TO_ARCHIVE_NON_ROOT_PI = newArgs1("ATDB00012", BpmRuntimeErrorMessages.getString("ATDB00012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_ARCHIVE_UNABLE_TO_DELETE_NON_ROOT_PI = newArgs1("ATDB00013", BpmRuntimeErrorMessages.getString("ATDB00013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_ARCHIVE_UNABLE_TO_DELETE_NON_TERMINATED_PI = newArgs1("ATDB00014", BpmRuntimeErrorMessages.getString("ATDB00014")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 ATDB_INVALID_PRODUCTION_AUDIT_TRAIL_OPERATION = newArgs1("ATDB00021", BpmRuntimeErrorMessages.getString("ATDB00021")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_PARTITION = newArgs0("ATDB00111", BpmRuntimeErrorMessages.getString("ATDB00111")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_PARTITION_OID = newArgs1("ATDB00112", BpmRuntimeErrorMessages.getString("ATDB00112")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_PARTITION_ID = newArgs1("ATDB00113", BpmRuntimeErrorMessages.getString("ATDB00113")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_UNKNOWN_AUDIT_TRAIL_RECORD = newArgs0("ATDB01000", BpmRuntimeErrorMessages.getString("ATDB01000")); //$NON-NLS-1$ //$NON-NLS-2$

   // TODO should be "No matching xxx instance found"
   public static final Args0 ATDB_NO_MATCHING_PROCESS_INSTANCE = newArgs0("ATDB01101", BpmRuntimeErrorMessages.getString("ATDB01101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_PROCESS_INSTANCE_OID = newArgs1("ATDB01102", BpmRuntimeErrorMessages.getString("ATDB01102")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_PROCESS_INSTANCE_NOT_TERMINATED = newArgs1("ATDB01103", BpmRuntimeErrorMessages.getString("ATDB01103")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_PROCESS_INSTANCE_TERMINATED = newArgs1("ATDB01104", BpmRuntimeErrorMessages.getString("ATDB01104")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_PROCESS_INSTANCE_ABORTING = newArgs1("ATDB01105", BpmRuntimeErrorMessages.getString("ATDB01105")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_PROCESS_INSTANCE_NOT_SPAWNED = newArgs1("ATDB01106", BpmRuntimeErrorMessages.getString("ATDB01106")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_PROCESS_INSTANCE_NOT_ROOT = newArgs1("ATDB01107", BpmRuntimeErrorMessages.getString("ATDB01107")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_ACTIVITY_INSTANCE = newArgs0("ATDB01111", BpmRuntimeErrorMessages.getString("ATDB01111")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_ACTIVITY_INSTANCE_OID = newArgs1("ATDB01112", BpmRuntimeErrorMessages.getString("ATDB01112")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_LOG_ENTRY = newArgs0("ATDB01121", BpmRuntimeErrorMessages.getString("ATDB01121")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_WORK_ITEM = newArgs0("ATDB01131", BpmRuntimeErrorMessages.getString("ATDB01131")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_WORK_ITEM_OID = newArgs1("ATDB01132", BpmRuntimeErrorMessages.getString("ATDB01132")); //$NON-NLS-1$ //$NON-NLS-2$

   //// DMS related

   public static final Args0 DMS_GENERIC_ERROR = newArgs0("DMS00000", BpmRuntimeErrorMessages.getString("DMS00000")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 DMS_FILE_STORE_UNAVAILABLE = newArgs0("DMS00001", BpmRuntimeErrorMessages.getString("DMS00001")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 DMS_UNKNOWN_FOLDER_ID = newArgs1("DMS01103", BpmRuntimeErrorMessages.getString("DMS01103")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_UNKNOWN_FILE_ID = newArgs1("DMS01113", BpmRuntimeErrorMessages.getString("DMS01113")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_EMPTY_FOLDER_NAME = newArgs0("DMS01110", BpmRuntimeErrorMessages.getString("DMS01110")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_EMPTY_FILE_NAME = newArgs0("DMS01111", BpmRuntimeErrorMessages.getString("DMS01111")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 DMS_NO_MATCHING_DOC_FOUND = newArgs0("DMS01122", BpmRuntimeErrorMessages.getString("DMS01122")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_ITEM_EXISTS = newArgs0("DMS01123", BpmRuntimeErrorMessages.getString("DMS01123")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 DMS_FAILED_PATH_RESOLVE = newArgs1("DMS01131", "Failed to resolve path. Repository message: {0}");

   public static final Args0 DMS_UNKNOWN_FILE_VERSION_ID = newArgs0("DMS01153", "Document version not found.");
   public static final Args0 DMS_CANNOT_REMOVE_ROOT_FILE_VERSION = newArgs0("DMS01156", "Root document version cannot be removed.");

   public static final Args1 DMS_DOCUMENT_TYPE_INVALID = newArgs1("DMS01303", BpmRuntimeErrorMessages.getString("DMS01303")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 DMS_DOCUMENT_TYPE_DEPLOY_ERROR = newArgs0("DMS01313", BpmRuntimeErrorMessages.getString("DMS01313")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 DMS_GENERIC_SECURITY_ERROR = newArgs0("DMS02000", BpmRuntimeErrorMessages.getString("DMS02000")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_SECURITY_ERROR_ADMIN_REQUIRED = newArgs0("DMS02001", BpmRuntimeErrorMessages.getString("DMS02001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_SECURITY_ERROR_DMS_READONLY_FOR_PREFERENCES = newArgs0("DMS02002", BpmRuntimeErrorMessages.getString("DMS02002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_SECURITY_ERROR_WRITE_IN_ARCHIVE_MODE = newArgs0("DMS02003", BpmRuntimeErrorMessages.getString("DMS02003")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 DMS_SECURITY_ERROR_ACCESS_DENIED_ON_FOLDER = newArgs1("DMS02101", BpmRuntimeErrorMessages.getString("DMS02101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_SECURITY_ERROR_ACCESS_DENIED_ON_DOCUMENT = newArgs1("DMS02102", BpmRuntimeErrorMessages.getString("DMS02102")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 DMS_ANNOTATIONS_ID_PRESENT = newArgs1("DMS03001", BpmRuntimeErrorMessages.getString("DMS03001")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 DMS_REPOSITORY_DEFAULT_LOAD_FAILED = new Args2("DMS03101",  BpmRuntimeErrorMessages.getString("DMS03101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_REPOSITORY_CONFIGURATION_PARAMETER_IS_NULL = new Args1("DMS03102",  BpmRuntimeErrorMessages.getString("DMS03102")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_REPOSITORY_PROVIDER_NOT_FOUND = new Args1("DMS03103",  BpmRuntimeErrorMessages.getString("DMS03103")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_REPOSITORY_INSTANCE_ALREADY_BOUND = new Args1("DMS03104",  BpmRuntimeErrorMessages.getString("DMS03104")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_REPOSITORY_DEFAULT_UNBIND = new Args0("DMS03105",  BpmRuntimeErrorMessages.getString("DMS03105")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_REPOSITORY_SYSTEM_UNBIND = new Args0("DMS03106",  BpmRuntimeErrorMessages.getString("DMS03106")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_REPOSITORY_INSTANCE_NOT_FOUND = new Args1("DMS03107",  BpmRuntimeErrorMessages.getString("DMS03107")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_REPOSITORY_NOT_FOUND_FOR_JNDI_NAME = newArgs1("DMS03108", BpmRuntimeErrorMessages.getString("DMS03108")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 DMS_FAILED_RETRIEVING_CONTENT_FOR_DOCUMENT = newArgs1("DMS04001", BpmRuntimeErrorMessages.getString("DMS04001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_FAILED_UPDATING_CONTENT_FOR_DOCUMENT = newArgs1("DMS04002", BpmRuntimeErrorMessages.getString("DMS04002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_FAILED_READING_ENTITY_BEAN_ATTRIBUTE = newArgs0("DMS04003", BpmRuntimeErrorMessages.getString("DMS04003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_FAILED_SETTING_DOCUMENT_ATTRIBUTE = newArgs0("DMS04004", BpmRuntimeErrorMessages.getString("DMS04004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_INVALID_HANLDE = newArgs1("DMS04005", BpmRuntimeErrorMessages.getString("DMS04005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_INVALID_REPOSITORY_SPACE = newArgs1("DMS04006", BpmRuntimeErrorMessages.getString("DMS04006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_UNSUPPORTED_VALUE = newArgs1("DMS04009", BpmRuntimeErrorMessages.getString("DMS04009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_SETTING_EMPTY_NAME_IN_DOCUMENTS_OR_FOLDERS_NOT_POSSIBLE = newArgs0("DMS04011", BpmRuntimeErrorMessages.getString("DMS04011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_NO_VALUE_FOR_MANDATORY_IN_ACCESS_POINT_SUPPLIED = newArgs1("DMS04012", BpmRuntimeErrorMessages.getString("DMS04012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_NO_DUCUMENTMANAGEMENTSERVICE_AVAILABLE = newArgs0("DMS04013", BpmRuntimeErrorMessages.getString("DMS04013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DMS_ELEMENT_IS_NOT_FOUND = newArgs1("DMS04014", BpmRuntimeErrorMessages.getString("DMS04014")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_USER_REALM = newArgs0("ATDB02101", BpmRuntimeErrorMessages.getString("ATDB02101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_REALM_OID = newArgs1("ATDB02102", BpmRuntimeErrorMessages.getString("ATDB02102")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_UNKNOWN_USER_REALM_ID = newArgs2("ATDB02103", BpmRuntimeErrorMessages.getString("ATDB02103")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_USER_REALM_ID_EXISTS = newArgs1("ATDB02104", BpmRuntimeErrorMessages.getString("ATDB02104")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_DELETION_FAILED_USER_REALM_ID_DANGLING_REFERENCE = newArgs1("ATDB02105", BpmRuntimeErrorMessages.getString("ATDB02105")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_USER = newArgs0("ATDB02111", BpmRuntimeErrorMessages.getString("ATDB02111")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_OID = newArgs1("ATDB02112", BpmRuntimeErrorMessages.getString("ATDB02112")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_UNKNOWN_USER_ID = newArgs2("ATDB02113", BpmRuntimeErrorMessages.getString("ATDB02113")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_USER_ID_EXISTS = newArgs2("ATDB02114", BpmRuntimeErrorMessages.getString("ATDB02114")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 ATDB_DEPUTY_EXISTS = newArgs2("ATDB02115", BpmRuntimeErrorMessages.getString("ATDB02115")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_DEPUTY_DOES_NOT_EXISTS = newArgs2("ATDB02116", BpmRuntimeErrorMessages.getString("ATDB02116")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_DEPUTY_SELF_REFERENCE_NOT_ALLOWED = newArgs1("ATDB02117", BpmRuntimeErrorMessages.getString("ATDB02117")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_ADDING_DEPUTY_FORBIDDEN = new Args2("ATDB02118", BpmRuntimeErrorMessages.getString("ATDB02118")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_MODIFYING_DEPUTY_FORBIDDEN = new Args2("ATDB02119", BpmRuntimeErrorMessages.getString("ATDB02119")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_REMOVING_DEPUTY_FORBIDDEN = new Args2("ATDB02120", BpmRuntimeErrorMessages.getString("ATDB02120")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_USER_DOMAIN = newArgs0("ATDB02121", BpmRuntimeErrorMessages.getString("ATDB02121")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_DOMAIN_OID = newArgs1("ATDB02122", BpmRuntimeErrorMessages.getString("ATDB02122")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_DOMAIN_ID = newArgs1("ATDB02123", BpmRuntimeErrorMessages.getString("ATDB02123")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_DEFAULT_DOMAIN_FOR_PARTITION_ID = newArgs1("ATDB02124", BpmRuntimeErrorMessages.getString("ATDB02124")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_DOMAIN_HIERARCHY = newArgs0("ATDB02131", BpmRuntimeErrorMessages.getString("ATDB02131")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_DOMAIN_HIERARCHY_OID = newArgs1("ATDB02132", BpmRuntimeErrorMessages.getString("ATDB02132")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_DOMAIN_LINK = newArgs0("ATDB02141", BpmRuntimeErrorMessages.getString("ATDB02141")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_DOMAIN_LINK_OID = newArgs1("ATDB02142", BpmRuntimeErrorMessages.getString("ATDB02142")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_USER_GROUP = newArgs0("ATDB02151", BpmRuntimeErrorMessages.getString("ATDB02151")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_GROUP_OID = newArgs1("ATDB02152", BpmRuntimeErrorMessages.getString("ATDB02152")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_GROUP_ID = newArgs1("ATDB02153", BpmRuntimeErrorMessages.getString("ATDB02153")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_USER_GROUP_ID_EXISTS = newArgs1("ATDB02154", BpmRuntimeErrorMessages.getString("ATDB02154")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_SESSION = newArgs0("ATDB02191", BpmRuntimeErrorMessages.getString("ATDB02191")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_SESSION_OID = newArgs1("ATDB02192", BpmRuntimeErrorMessages.getString("ATDB02192")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 ATDB_UNKNOWN_DEPARTMENT_OID = newArgs1("ATDB02201", BpmRuntimeErrorMessages.getString("ATDB02201")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_UNKNOWN_DEPARTMENT_ID = newArgs2("ATDB02202", BpmRuntimeErrorMessages.getString("ATDB02202")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_DEPARTMENT_EXISTS = newArgs1("ATDB02203", BpmRuntimeErrorMessages.getString("ATDB02203")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_UNKNOWN_DEPARTMENT_ID2 = newArgs2("ATDB02204", BpmRuntimeErrorMessages.getString("ATDB02204")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 ATDB_UNKNOWN_DEPARTMENT_HIERARCHY_OID = newArgs1("ATDB02211", BpmRuntimeErrorMessages.getString("ATDB02211")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 ATDB_UNKNOWN_LINK_TYPE_OID = newArgs1("ATDB02301", "Link type with OID ''{0}'' not found.");
   public static final Args2 ATDB_UNKNOWN_LINK_TYPE_ID  = newArgs2("ATDB02302", "Link type ''{0}'' for partition with oid ''{1}'' not found.");
   public static final Args1 ATDB_LINK_TYPE_ID_EXISTS   = newArgs1("ATDB02303", "Link type ''{0}'' exists.");

   public static final Args0 ATDB_FAILED_EVALUATING_RECOVERY_STATUS = newArgs0("ATDB03001", BpmRuntimeErrorMessages.getString("ATDB03001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_PARTITION_WITH_ID_ALREADY_EXISTS = newArgs1("ATDB03002", BpmRuntimeErrorMessages.getString("ATDB03002")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_PASSWORD_SET_FOR_SYSOP_THOUGH_USER_AUTHORIZATION_IS_REQUIRED = newArgs0("ATDB03003", BpmRuntimeErrorMessages.getString("ATDB03003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_PLEASE_SPECIFY_APPROPRIATE_PASSWORD_FOR_SYSOP_USER = newArgs0("ATDB03004", BpmRuntimeErrorMessages.getString("ATDB03004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_PLEASE_SPECIFY_APPROPRIATE_PASSWORD_FOR_SYSOP_USER_BEFORE_TRYING_TO_CHANGE_PASSWORD = newArgs0("ATDB03005", BpmRuntimeErrorMessages.getString("ATDB03005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNABLE_TO_SET_VALUE_OF_AUDIT_TRAIL_PROPERTY = newArgs1("ATDB03006", BpmRuntimeErrorMessages.getString("ATDB03006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_FAILED_DURING_SYSOP_USER_AUTHORIZATION = newArgs0("ATDB03007", BpmRuntimeErrorMessages.getString("ATDB03007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_INVALID_RUNTIME_SETUP_CONFIGURATION_FILE = newArgs0("ATDB03008", BpmRuntimeErrorMessages.getString("ATDB03008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_CLUSTER_CONFIGURATION_ALREADY_EXIST_USE_OPTION_DROP_OR_UPDATEDATACLUSTERS_FIRST = newArgs0("ATDB03009", BpmRuntimeErrorMessages.getString("ATDB03009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_CLUSTER_CONFIGURATION_DOES_NOT_EXIST_PROVIDE_VALID_CONFIGURATION_FILE = newArgs0("ATDB03010", BpmRuntimeErrorMessages.getString("ATDB03010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNABLE_TO_DELETE_VALUE_OF_AUDIT_TRAIL_PROPERTY = newArgs1("ATDB03011", BpmRuntimeErrorMessages.getString("ATDB03011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ATDB_FAILED_TO_UPDATE_CRITICALITIES_IN_AUDITTRAIL = newArgs0("ATDB03012", BpmRuntimeErrorMessages.getString("ATDB03012")); //$NON-NLS-1$ //$NON-NLS-2$




   //// Authentication / Authorization

   public static final Args0 AUTHx_NOT_LOGGED_IN = newArgs0("AUTHx00101", BpmRuntimeErrorMessages.getString("AUTHx00101")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 AUTHx_USER_NOT_VALID = newArgs1("AUTHx00121", BpmRuntimeErrorMessages.getString("AUTHx00121")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_USER_PASSWORD_EXPIRED = newArgs1("AUTHx00122", BpmRuntimeErrorMessages.getString("AUTHx00122")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_USER_PASSWORD_NOT_VALID = newArgs1("AUTHx00123", BpmRuntimeErrorMessages.getString("AUTHx00123")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_USER_PASSWORD_NOT_VALID_TRY_AGAIN = newArgs1("AUTHx00124", BpmRuntimeErrorMessages.getString("AUTHx00124")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_USER_TEMPORARILY_INVALIDATED = newArgs1("AUTHx00125", BpmRuntimeErrorMessages.getString("AUTHx00125")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_USER_DISABLED_BY_PW_RULES = newArgs1("AUTHx00126", BpmRuntimeErrorMessages.getString("AUTHx00126")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_CHANGE_PASSWORD_OLD_PW_VERIFICATION_FAILED = newArgs0("AUTHx00127", BpmRuntimeErrorMessages.getString("AUTHx00127")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_CHANGE_PASSWORD_NEW_PW_VERIFICATION_FAILED = newArgs0("AUTHx00128", BpmRuntimeErrorMessages.getString("AUTHx00128")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_CHANGE_PASSWORD_NEW_PW_MISSING = newArgs0("AUTHx00129", BpmRuntimeErrorMessages.getString("AUTHx00129")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_USER_ID_PASSWORD_EXPIRED = newArgs1("AUTHx00130", BpmRuntimeErrorMessages.getString("AUTHx00130")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_CHANGE_PASSWORD_IVALID_TOKEN = newArgs0("AUTHx00131", BpmRuntimeErrorMessages.getString("AUTHx00131")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_OPERATION_FAILED_USER_OID_NOT_FULLY_INITIALIZED = newArgs1("AUTHx00301", BpmRuntimeErrorMessages.getString("AUTHx00301")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_OPERATION_FAILED_USER_GROUP_OID_NOT_FULLY_INITIALIZED = newArgs1("AUTHx00302", BpmRuntimeErrorMessages.getString("AUTHx00302")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_OPERATION_FAILED_REQUIRES_INTERNAL_AUTH = newArgs0("AUTHx00303", BpmRuntimeErrorMessages.getString("AUTHx00303")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 AUTHx_SYNC_MISSING_SYNCHRONIZATION_PROVIDER = newArgs0("AUTHx00501", BpmRuntimeErrorMessages.getString("AUTHx00501")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 AUTHx_SYNC_UNKNOWN_USER = newArgs1("AUTHx00542", BpmRuntimeErrorMessages.getString("AUTHx00542")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_SYNC_FAILED_IMPORTING_USER = newArgs1("AUTHx00545", BpmRuntimeErrorMessages.getString("AUTHx00545")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_SYNC_IMPORTING_USERS_NOT_ALLOWED = newArgs0("AUTHx00548", BpmRuntimeErrorMessages.getString("AUTHx00548")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 AUTHx_SYNC_UNKNOWN_USER_GROUP = newArgs1("AUTHx00562", BpmRuntimeErrorMessages.getString("AUTHx00562")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 AUTHx_SYNC_FAILED_IMPORTING_USER_GROUP = newArgs1("AUTHx00565", BpmRuntimeErrorMessages.getString("AUTHx00565")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 AUTHx_SYNC_FAILED_IMPORTING_DEPARTMENT = newArgs2("AUTHx00585", BpmRuntimeErrorMessages.getString("AUTHx00585")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args AUTHx_AUTH_MISSING_GRANTS = newArgs("AUTHx01000"); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 AUTHx_AUTH_INVALID_GRANT = newArgs2("AUTHx01100", BpmRuntimeErrorMessages.getString("AUTHx01100")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 AUTHx_USER_CANNOT_JOIN_INVALID_USER_GROUP = newArgs2("AUTHx01101", BpmRuntimeErrorMessages.getString("AUTHx01101")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 AUTHx_AUTH_SAVING_OWN_PREFERENCES_FAILED = newArgs0("AUTHx01200", BpmRuntimeErrorMessages.getString("AUTHx01200")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 AUTHx_EXP_ACCOUNT_EXPIRED = newArgs1("AUTHx02001", BpmRuntimeErrorMessages.getString("AUTHx02001")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 AUTHx_AUTH_PARTITION_NOT_SPECIFIED = newArgs0("AUTHx03001", BpmRuntimeErrorMessages.getString("AUTHx03001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_AUTH_DOMAIN_NOT_SPECIFIED = newArgs0("AUTHx03002", BpmRuntimeErrorMessages.getString("AUTHx03002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_AUTH_REALM_NOT_SPECIFIED = newArgs0("AUTHx03003", BpmRuntimeErrorMessages.getString("AUTHx03003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 AUTHx_AUTH_CANCEL_BY_USER = newArgs0("AUTHx03004", BpmRuntimeErrorMessages.getString("AUTHx03004")); //$NON-NLS-1$ //$NON-NLS-2$




   //// Runtime data related

   public static final Args0 BPMRT_GENERIC_ERROR = newArgs0("BPMRT01000", BpmRuntimeErrorMessages.getString("BPMRT01000")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_UNKNOWN_DAEMON = newArgs1("BPMRT01501", BpmRuntimeErrorMessages.getString("BPMRT01501")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_DAEMON_IS_RUNNING = newArgs1("BPMRT01502", BpmRuntimeErrorMessages.getString("BPMRT01502")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_DAEMON_IS_NOT_RESPONDING = newArgs1("BPMRT01503", BpmRuntimeErrorMessages.getString("BPMRT01503")); //$NON-NLS-1$ //$NON-NLS-2$

   /**
    * Data value for given data ID cannot be created due to concurrency issues.
    */
   public static final Args1 BPMRT_FAILED_CREATING_DATA_VALUE = newArgs1("BPMRT02121", BpmRuntimeErrorMessages.getString("BPMRT02121")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_DAEMON_ALREADY_RUNNING = newArgs1("CONC03100", BpmRuntimeErrorMessages.getString("CONC03100")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_CURRENTLY_ACTIVATED_BY_SELF = newArgs1("BPMRT03101", BpmRuntimeErrorMessages.getString("BPMRT03101")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args BPMRT_AI_CURRENTLY_ACTIVATED_BY_OTHER = newArgs("CONC03102"); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_IS_ALREADY_TERMINATED = newArgs1("BPMRT03103", BpmRuntimeErrorMessages.getString("BPMRT03103")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_AI_IS_NOT_GRANTED_TO_USER = newArgs2("BPMRT03104", BpmRuntimeErrorMessages.getString("BPMRT03104")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_MUST_NOT_BE_SUBPROCESS_INVOCATION = newArgs1("BPMRT03105", BpmRuntimeErrorMessages.getString("BPMRT03105")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_CAN_NOT_BE_ABORTED_BY_USER = newArgs1("BPMRT03106", BpmRuntimeErrorMessages.getString("BPMRT03106")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_USER_IS_NOT_AUTHORIZED_TO_PERFORM_AI = newArgs2("BPMRT03107", BpmRuntimeErrorMessages.getString("BPMRT03107")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_MODEL_PARTICIPANT_IS_NOT_AUTHORIZED_TO_PERFORM_AI = newArgs2("BPMRT03108", BpmRuntimeErrorMessages.getString("BPMRT03108")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_USER_GROUP_IS_NOT_AUTHORIZED_TO_PERFORM_AI = newArgs2("BPMRT03109", BpmRuntimeErrorMessages.getString("BPMRT03109")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_AI_CAN_NOT_BE_DELEGATED_IN_CURRENT_STATE = newArgs2("BPMRT03110", BpmRuntimeErrorMessages.getString("BPMRT03110")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_AI_MUST_NOT_BE_ON_OTHER_USER_WORKLIST = newArgs2("BPMRT03111", BpmRuntimeErrorMessages.getString("BPMRT03111")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_USER_IS_EXLUDED_TO_PERFORM_AI = newArgs2("BPMRT03112", BpmRuntimeErrorMessages.getString("BPMRT03112")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_IS_IN_ABORTING_PROCESS = newArgs1("BPMRT03113", BpmRuntimeErrorMessages.getString("BPMRT03113")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_NON_INTERACTIVE_AI_CAN_NOT_BE_DELEGATED = newArgs1("BPMRT03114", BpmRuntimeErrorMessages.getString("BPMRT03114")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_INTERACTIVE_AI_CAN_NOT_BE_FORCED_TO_COMPLETION = newArgs1("BPMRT03115", BpmRuntimeErrorMessages.getString("BPMRT03115")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_CAN_NOT_BE_DELEGATED_TO_NON_USERGROUP_MEMBER = newArgs1("BPMRT03116", BpmRuntimeErrorMessages.getString("BPMRT03116")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_AND_HANDLER_BINDING_FRM_DIFF_MODELS = newArgs1("BPMRT03201", BpmRuntimeErrorMessages.getString("BPMRT03201")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_PI_AND_SPAWN_PROCESS_FRM_DIFF_MODELS = newArgs1("BPMRT03251", "Process instance with OID {0} and the process to spawn are from a different model.");

   public static final Args1 BPMRT_PI_NOT_ROOT = newArgs1("BPMRT03252", "Process instance with OID {0} is not a root process instance.");

   public static final Args1 BPMRT_PI_SWITCH_TO_SAME_PROCESS = newArgs1("BPMRT03253", "Cannot switch to the same process ''{0}''.");

   public static final Args1 BPMRT_PI_IS_ALREADY_TERMINATED = newArgs1("BPMRT03254", "Process instance with OID {0} is terminated.");

   public static final Args1 BPMRT_PI_JOIN_TO_CHILD_INSTANCE = newArgs1("BPMRT03255", "Process instance with OID {0} is not allowed to join child process.");

   public static final Args1 BPMRT_PI_JOIN_TO_SAME_PROCESS_INSTANCE = newArgs1("BPMRT03257", "Cannot join to the same process instance ''{0}''.");

   public static final Args1 BPMRT_PI_AND_HANDLER_BINDING_FRM_DIFF_MODELS = newArgs1("BPMRT03202", BpmRuntimeErrorMessages.getString("BPMRT03202")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_TIMEOUT_DURING_COUNT = newArgs1("BPMRT03501", BpmRuntimeErrorMessages.getString("BPMRT03501")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_LOCK_CONFLICT = newArgs2("BPMRT03601", BpmRuntimeErrorMessages.getString("BPMRT03601")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args3 BPMRT_LOCK_CONFLICT_FOR_HANDLE = newArgs3("BPMRT03602", BpmRuntimeErrorMessages.getString("BPMRT03602")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_ROLLING_BACK_ACTIVITY_THREAD = newArgs0("BPMRT03701", BpmRuntimeErrorMessages.getString("BPMRT03701")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_ROLLED_BACK_ACTIVITY_THREAD_AT_ACTIVITY = newArgs2("BPMRT03702", BpmRuntimeErrorMessages.getString("BPMRT03702")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_START_ACTIVITY_THREAD_MISSING_ACTIVITY = newArgs0("BPMRT03703", BpmRuntimeErrorMessages.getString("BPMRT03703")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_START_ACTIVITY_THREAD_MISSING_PI = newArgs0("BPMRT03704", BpmRuntimeErrorMessages.getString("BPMRT03704")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_ADHOC_ASYNC_START_ACTIVITY_THREAD = newArgs0("BPMRT03705"); //$NON-NLS-1$

   public static final Args1 BPMRT_AI_NOT_ADHOC_TRANSITION_SOURCE = newArgs1("BPMRT03706"); //$NON-NLS-1$

   public static final Args1 BPMRT_NULL_ARGUMENT = newArgs1("BPMRT03810", BpmRuntimeErrorMessages.getString("BPMRT03810")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_NULL_ELEMENT_IN_COLLECTION = newArgs1(
         "BPMRT03815", BpmRuntimeErrorMessages.getString("BPMRT03815")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_EMPTY_COLLECTION = newArgs1(
         "BPMRT03816", BpmRuntimeErrorMessages.getString("BPMRT03816")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_NULL_ATTRIBUTE = newArgs1(
         "BPMRT03817", BpmRuntimeErrorMessages.getString("BPMRT03817")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_INVALID_TYPE = newArgs2("BPMRT03811", BpmRuntimeErrorMessages.getString("BPMRT03811")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_INVALID_SPEC = newArgs2("BPMRT03812", BpmRuntimeErrorMessages.getString("BPMRT03812")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_INVALID_ARGUMENT = newArgs2("BPMRT03820", "Argument ''{0}'' must not be ''{1}''.");
   public static final Args1 BPMRT_INVALID_VALUE = newArgs1("BPMRT03825", "Invalid Value for Argument ''{0}''."); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_INVALID_ENUM_VALUE = newArgs1("BPMRT03830", "Invalid enumeration value: ''{0}''"); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_INVALID_ORGANIZATION_HIERARCHY = newArgs0("BPMRT03813", BpmRuntimeErrorMessages.getString("BPMRT03813")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_DEPARTMENT_HAS_ACTIVE_ACTIVITY_INSTANCES = newArgs1("BPMRT03814", BpmRuntimeErrorMessages.getString("BPMRT03814")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_INVALID_INDEXED_XPATH = newArgs0("BPMRT03815", "Invalid XPath, indexed XPath not supported.");

   public static final Args2 BPMRT_ILLEGAL_AI_STATE_CHANGE = newArgs2("BPMRT03901", BpmRuntimeErrorMessages.getString("BPMRT03901")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 BPMRT_ILLEGAL_AI_STATE_CHANGE_FOR_AI = newArgs3("BPMRT03902", BpmRuntimeErrorMessages.getString("BPMRT03902")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args4 BPMRT_ILLEGAL_AI_STATE_CHANGE_FOR_AI_WITH_PI_STATE = newArgs4("BPMRT03903", BpmRuntimeErrorMessages.getString("BPMRT03903")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_CANNOT_RUN_AI_INVALID_PI_STATE = newArgs2("BPMRT03904", BpmRuntimeErrorMessages.getString("BPMRT03904")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_CANNOT_RUN_A_INVALID_PI_STATE = newArgs2("BPMRT03905", BpmRuntimeErrorMessages.getString("BPMRT03905")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_INCOMPATIBLE_TYPE_FOR_DATA = newArgs1("BPMRT04002", BpmRuntimeErrorMessages.getString("BPMRT04002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_INCOMPATIBLE_TYPE_FOR_DATA_WITH_PATH = newArgs2("BPMRT04003", BpmRuntimeErrorMessages.getString("BPMRT04003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_INVALID_PROBABILIY = newArgs1("BPMRT04004", BpmRuntimeErrorMessages.getString("BPMRT04004"));
   public static final Args2 BPMRT_USER_NOT_ALLOWED_ACTIVATE_QA_INSTANCE = newArgs2("BPMRT04005", BpmRuntimeErrorMessages.getString("BPMRT04005"));
   public static final Args2 BPMRT_DELEGATE_QA_INSTANCE_NOT_ALLOWED = newArgs2("BPMRT04006", BpmRuntimeErrorMessages.getString("BPMRT04006"));
   public static final Args0 BPMRT_MODIFY_DATA_QA_INSTANCE_NOT_ALLOWED = newArgs0("BPMRT04007", BpmRuntimeErrorMessages.getString("BPMRT04007"));
   public static final Args1 BPMRT_COMPLETE_QA_NO_ATTRIBUTES_SET = newArgs1("BPMRT04008", BpmRuntimeErrorMessages.getString("BPMRT04008"));
   public static final Args0 BPMRT_NO_ERROR_CODE_SET = newArgs0("BPMRT04009", BpmRuntimeErrorMessages.getString("BPMRT04009"));

   public static final Args2 BPMRT_DATA_NOT_USED_BY_PROCESS = newArgs2("BPMRT04101", "Data ''{0}'' is not used by process definition ''{1}''.");

   public static final Args1 BPMRT_PI_IS_CASE = newArgs1("BPMRT03831"); //$NON-NLS-1$
   public static final Args1 BPMRT_PI_IS_MEMBER = newArgs1("BPMRT03832"); //$NON-NLS-1$
   public static final Args1 BPMRT_PI_NOT_ACTIVE = newArgs1("BPMRT03833"); //$NON-NLS-1$
   public static final Args1 BPMRT_PI_NOT_CASE = newArgs1("BPMRT03834"); //$NON-NLS-1$
   public static final Args2 BPMRT_PI_NOT_MEMBER = newArgs2("BPMRT03835"); //$NON-NLS-1$

   public static final Args1 BPMRT_PI_IS_TRANSIENT = newArgs1("BPMRT03840", BpmRuntimeErrorMessages.getString("BPMRT03840")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_NO_CHANGES_TO_MODEL = newArgs1("BPMRT03850"); //$NON-NLS-1$

   public static final Args1 BPMRT_DMS_DOCUMENT_DATA_SYNC_FAILED = newArgs1("BPMRT05001", BpmRuntimeErrorMessages.getString("BPMRT05001")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_INVALID_CREDENTIAL_PROVIDER_CONFIGURATION = newArgs1("BPMRT06001", BpmRuntimeErrorMessages.getString("BPMRT06001")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_MODEL_REPOSITORY_IS_NO_DIRECTORY = newArgs1("BPMRT07001", BpmRuntimeErrorMessages.getString("BPMRT07001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_IO_ERROR_DURING_SAVE = newArgs0("BPMRT07002", BpmRuntimeErrorMessages.getString("BPMRT07002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_CANNOT_LOCK_FILE = newArgs1("BPMRT07003", BpmRuntimeErrorMessages.getString("BPMRT07003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_UNABLE_TO_LOAD_XPDL_EXPORT_STYLESHEET = newArgs0("BPMRT07004", BpmRuntimeErrorMessages.getString("BPMRT07004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_INVALID_JAXP_SETUP = newArgs0("BPMRT07005", BpmRuntimeErrorMessages.getString("BPMRT07005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_FAILED_READING_XPDL_MODEL_FILE = newArgs0("BPMRT07006", BpmRuntimeErrorMessages.getString("BPMRT07006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_COULD_NOT_RETRIEVE_ACTIVITY_INSTANCE_FOR_CRITICALITY_UPDATE = newArgs0("BPMRT07007", BpmRuntimeErrorMessages.getString("BPMRT07007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_FAILED_RETRIEVING_NONTERMINATED_PROCESS_INSTANCES_FOR_MODEL = newArgs1("BPMRT07008", BpmRuntimeErrorMessages.getString("BPMRT07008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_UNABLE_TO_DELETE_MODEL_WITH_OPEN_PROCESS_INSTANCES = newArgs0("BPMRT07009", BpmRuntimeErrorMessages.getString("BPMRT07009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_FAILED_VERIFIYING_PRECONDITIONS = newArgs0("BPMRT07010", BpmRuntimeErrorMessages.getString("BPMRT07010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_FAILED_RESOLVING_PROCESS_INSTANCE_CLOSURE = newArgs0("BPMRT07011", BpmRuntimeErrorMessages.getString("BPMRT07011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_ACTIVITY_INSTANCE_WAS_DELETED = newArgs0("BPMRT07012", BpmRuntimeErrorMessages.getString("BPMRT07012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_USER_IS_NOT_ALLOWED_TO_CHANGE_PROFILE_FOR_SCOPE = newArgs2("BPMRT07013", BpmRuntimeErrorMessages.getString("BPMRT07013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_INCONSISTENT_PAIR_VALUES = newArgs2("BPMRT07014", BpmRuntimeErrorMessages.getString("BPMRT07014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_INCONSISTENT_COLLECTION_VALUES = newArgs2("BPMRT07015", BpmRuntimeErrorMessages.getString("BPMRT07015")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_INPUT_NOT_SERIALIZABLE = newArgs0("BPMRT07016", BpmRuntimeErrorMessages.getString("BPMRT07016")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 BPMRT_DEPLOYED_OBJECT_DIFFERS_IN_ITS_IDFROM_ITS_DEFINED_VALUE = newArgs3("BPMRT07017", BpmRuntimeErrorMessages.getString("BPMRT07017")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_NO_VALID_EVALUATOR_CLASS_PROVIDED = newArgs0("BPMRT07018", BpmRuntimeErrorMessages.getString("BPMRT07018")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_CANNOT_ASSIGN_MORE_USERS_TO_PARTICIPANT_CARDINALITY_EXCEEDED = newArgs1("BPMRT07019", BpmRuntimeErrorMessages.getString("BPMRT07019")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_USER_WITH_ACCOUNT_ALREADY_EXISTS_IN_USER_REALM = newArgs2("BPMRT07020", BpmRuntimeErrorMessages.getString("BPMRT07020")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_DOMAIN_WITH_ID_ALREADY_EXISTS = newArgs1("BPMRT07021", BpmRuntimeErrorMessages.getString("BPMRT07021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_ID_FOR_A_PARTITIONS_DEFAULT_DOMAIN_IS_NOT_ALLOWED_TO_BE_CHANGED = newArgs0("BPMRT07022", BpmRuntimeErrorMessages.getString("BPMRT07022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_DOMAIN_HIERARCHY_ENTRY_ALREADY_EXISTS = newArgs2("BPMRT07023", BpmRuntimeErrorMessages.getString("BPMRT07023")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_USER_DOMAIN_LINK_ALREADY_EXISTS = newArgs2("BPMRT07024", BpmRuntimeErrorMessages.getString("BPMRT07024")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_USER_REALM_WITH_ID_ALREADY_EXISTS = newArgs1("BPMRT07025", BpmRuntimeErrorMessages.getString("BPMRT07025")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_PROCESS_INSTANCE_REFERENCED_BY_OID_HAS_TO_BE_A_SCOPE_PROCESS_INSTANCES = newArgs1("BPMRT07026", BpmRuntimeErrorMessages.getString("BPMRT07026")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_A_SINGLE_DATA_SLOT_MUST_NOT_CONTAIN_BOTH_STORAGES_TYPES_SVALUECOLUMN_AND_NVALUECOLUMN = newArgs0("BPMRT07027", BpmRuntimeErrorMessages.getString("BPMRT07027")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_A_NUMERIC_DATA_SLOT_MUST_NOT_CONTAIN_BOTH_STORAGES_TYPES_NVALUECOLUMN_AND_DVALUECOLUMN = newArgs0("BPMRT07028", BpmRuntimeErrorMessages.getString("BPMRT07028")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_A_DATA_SLOT_MUST_NOT_CONTAIN_STORAGE_TYPE_DVALUECOLUMN_WITHOUT_STORAGE_TYPE_SVALUECOLUMN = newArgs0("BPMRT07029", BpmRuntimeErrorMessages.getString("BPMRT07029")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_INVALID_RUNTIME_SETUP_CONFIGURATION = newArgs0("BPMRT07030", BpmRuntimeErrorMessages.getString("BPMRT07030")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_USER_GROUP_WITH_ID_ALREADY_EXISTS_FOR = newArgs2("BPMRT07031", BpmRuntimeErrorMessages.getString("BPMRT07031")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 BPMRT_UNABLE_TO_USE_XML_SCHEMA_MODEL_VALIDATION = newArgs0("BPMRT07032", BpmRuntimeErrorMessages.getString("BPMRT07032")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_UNABLE_TO_SET_DEFAULT_XML_SCHEMA_URI_FOR_MODEL_VALIDATION = newArgs0("BPMRT07033", BpmRuntimeErrorMessages.getString("BPMRT07033")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_ERROR_DURING_XML_SERIALIZATION = newArgs0("BPMRT07034", BpmRuntimeErrorMessages.getString("BPMRT07034")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 BPMRT_ERROR_READING_XML = newArgs0("BPMRT07035", BpmRuntimeErrorMessages.getString("BPMRT07035")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_FILE_NOT_FOUND = newArgs1("BPMRT07036", BpmRuntimeErrorMessages.getString("BPMRT07036")); //$NON-NLS-1$ //$NON-NLS-2$





   //// JMS related

   public static final Args1 JMS_NO_MESSAGE_ACCEPTORS_FOUND = newArgs1("JMS01001", BpmRuntimeErrorMessages.getString("JMS01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_CLOSING_BLOB_AFTER_READING = newArgs0("JMS01002", BpmRuntimeErrorMessages.getString("JMS01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_CONNECTING_TO_JMS_AUDITTRAIL_QUEUE = newArgs0("JMS01003", BpmRuntimeErrorMessages.getString("JMS01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_READING_PROCESS_BLOB_FROM_JMS_AUDITTRAIL_QUEUE = newArgs0("JMS01004", BpmRuntimeErrorMessages.getString("JMS01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_INITIALIZING_JMS_BLOB_BUILDER = newArgs0("JMS01005", BpmRuntimeErrorMessages.getString("JMS01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_WRITING_BLOB_TO_JMS = newArgs0("JMS01006", BpmRuntimeErrorMessages.getString("JMS01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JMS_FAILED_PERSISTING_PROCESS_BLOB = newArgs1("JMS01007", BpmRuntimeErrorMessages.getString("JMS01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JMS_FAILED_PERSISTING_BLOB_AT_TABLE = newArgs1("JMS01008", BpmRuntimeErrorMessages.getString("JMS01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JMS_UNEXPECTED_SECTION_MARKER = newArgs1("JMS01009", BpmRuntimeErrorMessages.getString("JMS01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JMS_MESSAGE_TYPE_FOR_ID_NOT_SUPPORTED = newArgs1("JMS01010", BpmRuntimeErrorMessages.getString("JMS01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JMS_MATCHING_AI_FOUND_BUT_IT_IS_NOT_OF_RECEIVING_NATURE = newArgs1("JMS01011", BpmRuntimeErrorMessages.getString("JMS01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_INITIALIZING_JMS_EXPORTQUEUE_SENDER = newArgs0("JMS01012", BpmRuntimeErrorMessages.getString("JMS01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JMS_FAILED_SEND_EXPORTQUEUE_SENDER = newArgs0("JMS01013", BpmRuntimeErrorMessages.getString("JMS01013")); //$NON-NLS-1$ //$NON-NLS-2$
   
   //// Preference Store related

   public static final Args0 PREF_PREF_STORE_READONLY = newArgs0("PREF01001", BpmRuntimeErrorMessages.getString("PREF01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_EMPTY_PREF_STORE_READONLY = newArgs0("PREF01002", BpmRuntimeErrorMessages.getString("PREF01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_NO_USER_SPECIFIED_PREFSCOPE_USER_AND_REALM_NOT_AVAILABLE = newArgs0("PREF01003", BpmRuntimeErrorMessages.getString("PREF01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_NO_CURRENT_PARTITION_FOUND = newArgs0("PREF01004", BpmRuntimeErrorMessages.getString("PREF01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 PREF_AUDITTRAIL_PERSISTENCE_NOT_SUPPORTED_FOR_PREFERENCESSCOPE = newArgs1("PREF01005", BpmRuntimeErrorMessages.getString("PREF01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_NO_CURRENT_USER_FOUND = newArgs0("PREF01006", BpmRuntimeErrorMessages.getString("PREF01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 PREF_QUERYING_NOT_SUPPORTED_FOR_SCOPE = newArgs1("PREF01007", BpmRuntimeErrorMessages.getString("PREF01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 PREF_PREFERENCESSCOPE_NOT_SUPPORTED = newArgs1("PREF01008", BpmRuntimeErrorMessages.getString("PREF01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_PREFERENCESSCOPE_DEFAULT_IS_READ_ONLY = newArgs0("PREF01009", BpmRuntimeErrorMessages.getString("PREF01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 PREF_NOT_A_VALID_PREFERENCES_PATH = newArgs1("PREF01010", BpmRuntimeErrorMessages.getString("PREF01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_UNKNOWN_VALUE_FOR_PROPERTY_INFINITY_PREFERENCE_STORE = newArgs0("PREF01011", BpmRuntimeErrorMessages.getString("PREF01011")); //$NON-NLS-1$ //$NON-NLS-2$



   //// Query related

   public static final Args1 QUERY_XPATH_ON_NON_STRUCT_DATA = newArgs1("QUERY01001", BpmRuntimeErrorMessages.getString("QUERY01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_MISSING_XPATH_ON_NON_STRUCT_DATA = newArgs2("QUERY01002", BpmRuntimeErrorMessages.getString("QUERY01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_XPATH_ON_STRUCT_DATA_MUST_POINT_TO_PRIMITIVE = newArgs2("QUERY01003", BpmRuntimeErrorMessages.getString("QUERY01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_XPATH_ON_STRUCT_DATA_ORDER_BY_MUST_POINT_TO_PRIMITIVE = newArgs2("QUERY01004", BpmRuntimeErrorMessages.getString("QUERY01004")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 QUERY_FILTER_IS_XXX_FOR_QUERY = newArgs2("QUERY02001", BpmRuntimeErrorMessages.getString("QUERY02001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 QUERY_FILTER_IS_NOT_AVAILABLE_WITH_DISABLED_AI_HISTORY = newArgs1("QUERY02010", BpmRuntimeErrorMessages.getString("QUERY02010")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 QUERY_DATA_FILTER_EMPTY_VALUE_LIST_FOR_XXX_OPERATOR = newArgs1("QUERY02020", BpmRuntimeErrorMessages.getString("QUERY02020")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 QUERY_DATA_FILTER_VALUE_TYPES_ARE_INHOMOGENEOUS = newArgs1("QUERY02021", BpmRuntimeErrorMessages.getString("QUERY02021")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 QUERY_NULL_VALUES_NOT_SUPPORTED_WITH_OPERATOR = newArgs1("QUERY03001", BpmRuntimeErrorMessages.getString("QUERY03001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_INCONSISTENT_OPERATOR_USE = newArgs2("QUERY03002", BpmRuntimeErrorMessages.getString("QUERY03002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_TYPES_OF_LOWER_AND_UPPER_BOUND_ARE_INHOMOGENEOUS = newArgs2("QUERY03003", BpmRuntimeErrorMessages.getString("QUERY03003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_ATTRIBUTE_NOT_SUPPORTED_FOR_ORDER_TERM = newArgs0("QUERY03004", BpmRuntimeErrorMessages.getString("QUERY03004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_DOCUMENTQUERY_METADATA_ANY_ONLY_SUPPORTS_LIKE_OPERATOR = newArgs0("QUERY03005", BpmRuntimeErrorMessages.getString("QUERY03005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 QUERY_OPERATOR_NOT_SUPPORTED = newArgs1("QUERY03006", BpmRuntimeErrorMessages.getString("QUERY03006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_ATTRIBUTE_METADATA_ANY_ONLY_SUPPORTS_LIKE_OPERATOR = newArgs0("QUERY03007", BpmRuntimeErrorMessages.getString("QUERY03007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_ATTRIBUTE_NOT_SUPPORTED = newArgs0("QUERY03008", BpmRuntimeErrorMessages.getString("QUERY03008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 QUERY_ONLY_LONG_OR_STRING_REPRESENTATION_OF_DATE_SUPPORTED = newArgs1("QUERY03009", BpmRuntimeErrorMessages.getString("QUERY03009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 QUERY_UNSUPPORTED_DATAFILTER_OPERATOR_FOR_BIG_DATA_VALUE = newArgs1("QUERY03010", BpmRuntimeErrorMessages.getString("QUERY03010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_FAILED_EVALUATING_PROCESS_INSTANCE_CLOSURE = newArgs0("QUERY03011", BpmRuntimeErrorMessages.getString("QUERY03011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_VALUE_OF_SKIPPEDENTRIES_MUST_NOT_BE_LESS_THAN_ZERO = newArgs0("QUERY03012", BpmRuntimeErrorMessages.getString("QUERY03012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_FAILED_EXECUTING_QUERY = newArgs0("QUERY03013", BpmRuntimeErrorMessages.getString("QUERY03013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 QUERY_FAILED_EVALUATING_PROCESS_INSTANCE_CLOSURE_RESULTSET = newArgs0("QUERY03014", BpmRuntimeErrorMessages.getString("QUERY03014")); //$NON-NLS-1$ //$NON-NLS-2$

   //// IPP WS related

   public static final Args3 IPPWS_FACTORY_WS_PROPERTIES_MISSING = newArgs3("IPPWS01001", BpmRuntimeErrorMessages.getString("IPPWS01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_ADDING_SESSION_PROP_TO_SOAP_HEADER = newArgs0("IPPWS01002", BpmRuntimeErrorMessages.getString("IPPWS01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_ADDING_SESSION_PROP_TO_NONEXISTING_SOAP_HEADER = newArgs0("IPPWS01003", BpmRuntimeErrorMessages.getString("IPPWS01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_INIT_MALFORMED_URL = newArgs0("IPPWS01004", BpmRuntimeErrorMessages.getString("IPPWS01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_RESOLVING_WS_PROPERTIES = newArgs0("IPPWS01005", BpmRuntimeErrorMessages.getString("IPPWS01005")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 IPPWS_ENV_CREATION_FAILED_USER_NULL = newArgs0("IPPWS02001", BpmRuntimeErrorMessages.getString("IPPWS02001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_ENV_CREATION_FAILED_SESSION_PROPS_NULL = newArgs0("IPPWS02002", BpmRuntimeErrorMessages.getString("IPPWS02002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_ENV_CREATION_FAILED_ALL_NULL = newArgs0("IPPWS02003", BpmRuntimeErrorMessages.getString("IPPWS02003")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 IPPWS_META_DATA_TYPE_INVALID = newArgs2("IPPWS03001", BpmRuntimeErrorMessages.getString("IPPWS03001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 IPPWS_DATA_VALUE_INVALID = newArgs2("IPPWS03101", BpmRuntimeErrorMessages.getString("IPPWS03101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_WS_SECURITY_AUTHENTICATION_REQUIRES_USERNAME = newArgs0("IPPWS03102", BpmRuntimeErrorMessages.getString("IPPWS03102")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Login related
   public static final Args0 LOGIN_LDAP_INVALID_USER_PASSWORD = newArgs0("LOGIN01001", BpmRuntimeErrorMessages.getString("LOGIN01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 LOGIN_LDAP_UNABLE_TO_CONNECT = newArgs0("LOGIN01002", BpmRuntimeErrorMessages.getString("LOGIN01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 LOGIN_LDAP_NAMING_EXCEPTION = newArgs2("LOGIN01003", BpmRuntimeErrorMessages.getString("LOGIN01003")); //$NON-NLS-1$ //$NON-NLS-2$

   //// EJB related
   public static final Args1 EJB_UNKNOWN_CARRIER_MESSAGE_TYPE = newArgs1("EJB01001", BpmRuntimeErrorMessages.getString("EJB01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 EJB_INVALID_SERVICE_FACTORY_CONFIGURATION = newArgs1("EJB01002", BpmRuntimeErrorMessages.getString("EJB01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 EJB_MISSING_DATA_SOURCE = newArgs1("EJB01003", BpmRuntimeErrorMessages.getString("EJB01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_INVALID_TUNNELING_SERVICE_ENDPOINT = newArgs0("EJB01004", BpmRuntimeErrorMessages.getString("EJB01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_LOADING_SERVICE_INTERFACE_CLASS = newArgs0("EJB01005", BpmRuntimeErrorMessages.getString("EJB01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_OBTAINING_ENTITY_BEAN_PK = newArgs0("EJB01006", BpmRuntimeErrorMessages.getString("EJB01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 EJB_FAILED_OBTAINING_ENTITY_BEAN_NO_GETPK_METHOD = newArgs1("EJB01007", BpmRuntimeErrorMessages.getString("EJB01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_LOOKING_UP_ENTITY_BEAN_VIA_PK = newArgs0("EJB01008", BpmRuntimeErrorMessages.getString("EJB01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 EJB_INVALID_ID_ACCESS = newArgs1("EJB01009", BpmRuntimeErrorMessages.getString("EJB01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_RETRIEVING_ENTITY_BEAN = newArgs0("EJB01010", BpmRuntimeErrorMessages.getString("EJB01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_NO_ENTITYMANAGER_COULD_BE_RETRIEVED = newArgs0("EJB01011", BpmRuntimeErrorMessages.getString("EJB01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_READING_ENTITY_BEAN_ATTRIBUTE = newArgs0("EJB01012", BpmRuntimeErrorMessages.getString("EJB01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_TRANSLATING_ENTITY_BEAN_HANLDE_TO_BEAN_REFERENCE = newArgs0("EJB01013", BpmRuntimeErrorMessages.getString("EJB01013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_FAILED_SETTING_ENTITY_BEAN_ATTRIBUTE = newArgs0("EJB01014", BpmRuntimeErrorMessages.getString("EJB01014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 EJB_CANNOT_CREATE_SESSION_BEAN = newArgs0("EJB01015", BpmRuntimeErrorMessages.getString("EJB01015")); //$NON-NLS-1$ //$NON-NLS-2$



   //// General CLI related

   public static final Args0 CLI_INVALID_DEPARTMENT_PATH_PROVIDED = newArgs0("CLI01001", BpmRuntimeErrorMessages.getString("CLI01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_DEPRECATED_PROCESS_MODEL_ONLY_ONE_PARENT_ORG_ALLOWED = newArgs0("CLI01002", BpmRuntimeErrorMessages.getString("CLI01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_NO_MODEL_ACTIVE = newArgs0("CLI01003", BpmRuntimeErrorMessages.getString("CLI01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_ORGANIZATION_NOT_FOUND = newArgs1("CLI01004", BpmRuntimeErrorMessages.getString("CLI01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_INTERFACE_MODEL_OID_NOT_PROVIDED = newArgs0("CLI01005", BpmRuntimeErrorMessages.getString("CLI01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_PROCESS_ID_NOT_PROVIDED = newArgs0("CLI01006", BpmRuntimeErrorMessages.getString("CLI01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_IMPLEMENTATION_MODEL_ID_NOT_PROVIDED = newArgs0("CLI01007", BpmRuntimeErrorMessages.getString("CLI01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_SQL_EXCEPTION_OCCURED = newArgs1("CLI01008", BpmRuntimeErrorMessages.getString("CLI01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_COULD_NOT_INITIALIZE_DDL_SPOOL_FILE = newArgs1("CLI01009", BpmRuntimeErrorMessages.getString("CLI01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TIMESTAMP = newArgs1("CLI01010", BpmRuntimeErrorMessages.getString("CLI01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_NO_ARCHIVE_AUDITTRAIL_SCHEMA_SPECIFIED = newArgs0("CLI01011", BpmRuntimeErrorMessages.getString("CLI01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_NO_AUDITTRAIL_PARTITION_SPECIFIED = newArgs0("CLI01012", BpmRuntimeErrorMessages.getString("CLI01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 CLI_INTERNAL_VALUE_FOR_OPTION_IS_NOT_IN_CORRECT_FORMAT = newArgs2("CLI01013", BpmRuntimeErrorMessages.getString("CLI01013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_NO_FILE_NAME_PROVIDED = newArgs0("CLI01014", BpmRuntimeErrorMessages.getString("CLI01014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_NO_SCHEMA_NAME_PROVIDED = newArgs0("CLI01015", BpmRuntimeErrorMessages.getString("CLI01015")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_DRIVER_NOT_FOUND = newArgs0("CLI01016", BpmRuntimeErrorMessages.getString("CLI01016")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_PLEASE_PROVIDE_TARGET_FILENAME = newArgs0("CLI01017", BpmRuntimeErrorMessages.getString("CLI01017")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_NEITHER_REPOSITORY_NOR_MODEL_FILE_PROVIDED = newArgs0("CLI01018", BpmRuntimeErrorMessages.getString("CLI01018")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 CLI_FAILED_RESOLVING_PARTITION_OIDS = newArgs0("CLI01019", BpmRuntimeErrorMessages.getString("CLI01019")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 CLI_INVALID_DEPARTMENT_PATH_CREATE_IT_MANUALLY_OR_SPECIFIY_OPTION = newArgs2("CLI01020", BpmRuntimeErrorMessages.getString("CLI01020")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_DATEDESCRIPTOR = newArgs1("CLI01021", BpmRuntimeErrorMessages.getString("CLI01021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_FROMDATE = newArgs1("CLI01022", BpmRuntimeErrorMessages.getString("CLI01022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_UNSUPPORTED_DATE_FORMAT_FOR_OPTION_TODATE = newArgs1("CLI01023", BpmRuntimeErrorMessages.getString("CLI01023")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_UNSUPPORTED_FORMAT_FOR_OPTION_PROCESSINSTANCEOID = newArgs1("CLI01024", BpmRuntimeErrorMessages.getString("CLI01024")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_INVALID_OPTION_DUMP = newArgs1("CLI01025", BpmRuntimeErrorMessages.getString("CLI01025")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_INVALID_OPTION_PREFERENCES  = newArgs1("CLI01026", BpmRuntimeErrorMessages.getString("CLI01026")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 CLI_INVALID_CONTENT_FOR_OPTION_PREFERENCES  = newArgs1("CLI01027", BpmRuntimeErrorMessages.getString("CLI01027")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Archiver related
   public static final Args0 ARCH_FAILED_VERIFYING_PRECONDITIONS = newArgs0("ARCH01001", BpmRuntimeErrorMessages.getString("ARCH01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_ARCHIVING_LOG_ENTRIES = newArgs0("ARCH01002", BpmRuntimeErrorMessages.getString("ARCH01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_ARCHIVING_ENTRIES_FROM_TABLE_INCLUDED_IN_TRANSITIVE_CLOSURE_FOR_ALREADY_ARCHIVED_LOG_ENTRIES = newArgs1("ARCH01003", BpmRuntimeErrorMessages.getString("ARCH01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_TO_FIND_STARTING_TIME = newArgs0("ARCH01004", BpmRuntimeErrorMessages.getString("ARCH01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_RESOLVING_PROCESS_INSTANCE_CLOSURE = newArgs0("ARCH01005", BpmRuntimeErrorMessages.getString("ARCH01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_ARCHIVING_PROCESSES = newArgs0("ARCH01006", BpmRuntimeErrorMessages.getString("ARCH01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_ARCHIVING_PROCESSES_TERMINATED_BEFORE = newArgs1("ARCH01007", BpmRuntimeErrorMessages.getString("ARCH01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_ARCHIVING_PROCESSES_FOR_MODEL_WITH_OID = newArgs1("ARCH01008", BpmRuntimeErrorMessages.getString("ARCH01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ARCH_FAILED_ARCHIVING_PROCESSES_FOR_MODEL_WITH_OID_TERMINATED_BEFORE = newArgs2("ARCH01009", BpmRuntimeErrorMessages.getString("ARCH01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_DELETING_MODEL_WITH_OID = newArgs1("ARCH01010", BpmRuntimeErrorMessages.getString("ARCH01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_DELETING_LOG_ENTRIES_BEFORE = newArgs1("ARCH01011", BpmRuntimeErrorMessages.getString("ARCH01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_INSERTING_USER_SESSION_ENTRIES_EXPIRED_BEFORE = newArgs1("ARCH01012", BpmRuntimeErrorMessages.getString("ARCH01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_DELETING_USER_SESSION_ENTRIES_EXPIRED_BEFORE = newArgs1("ARCH01013", BpmRuntimeErrorMessages.getString("ARCH01013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_FINDING_MINIMUM_VALUE_FOR_ATTRIBUTE = newArgs1("ARCH01014", BpmRuntimeErrorMessages.getString("ARCH01014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_FINDING_MAXIMUM_VALUE_FOR_ATTRIBUTE = newArgs1("ARCH01015", BpmRuntimeErrorMessages.getString("ARCH01015")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_OBTAINING_JDBC_CONNECTION_TO_AUDIT_TRAIL_DB = newArgs0("ARCH01016", BpmRuntimeErrorMessages.getString("ARCH01016")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_INVALID_PARTITION_ID = newArgs1("ARCH01017", BpmRuntimeErrorMessages.getString("ARCH01017")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_RESOLVING_PARTITION_ID = newArgs1("ARCH01018", BpmRuntimeErrorMessages.getString("ARCH01018")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_CANNOT_ARCHIVE_MODELS_WITH_NONTERMINATED_PROCESS_INSTANCES = newArgs1("ARCH01019", BpmRuntimeErrorMessages.getString("ARCH01019")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ARCH_UNABLE_TO_DELETE_CLOSURE_OF_MODEL_WITH_OID = newArgs2("ARCH01020", BpmRuntimeErrorMessages.getString("ARCH01020")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_CANNOT_ARCHIVE_PROCESS_INSTANCES = newArgs1("ARCH01021", BpmRuntimeErrorMessages.getString("ARCH01021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_CANNOT_DELETE_PROCESS_INSTANCES = newArgs1("ARCH01022", BpmRuntimeErrorMessages.getString("ARCH01022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_DELETING_USER_SESSIONS = newArgs0("ARCH01023", BpmRuntimeErrorMessages.getString("ARCH01023")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_DATA_CAN_ONLY_BE_SELECTED_STANDALONE_ARCHIVING_NOT_SUPPORTED = newArgs0("ARCH01024", BpmRuntimeErrorMessages.getString("ARCH01024")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_RETRIEVING_NUMBER_OF_NONTERMINATED_PROCESSES_FOR_MODEL_WTH_OID = newArgs1("ARCH01025", BpmRuntimeErrorMessages.getString("ARCH01025")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_RETRIEVING_MODELS = newArgs0("ARCH01026", BpmRuntimeErrorMessages.getString("ARCH01026")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_DELETING_DATA_FOR_TERMINATED_PROCESSES = newArgs1("ARCH01027", BpmRuntimeErrorMessages.getString("ARCH01027")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_FINDING_UNUSED_MODELS = newArgs0("ARCH01028", BpmRuntimeErrorMessages.getString("ARCH01028")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_FINDING_MODELS = newArgs0("ARCH01029", BpmRuntimeErrorMessages.getString("ARCH01029")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_SYNCHRONIZING_ARCHIVED_UTILITY_TABLES = newArgs0("ARCH01030", BpmRuntimeErrorMessages.getString("ARCH01030")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_SYNCHRONIZING_MODEL_TABLE_ARCHIVE = newArgs0("ARCH01031", BpmRuntimeErrorMessages.getString("ARCH01031")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_SYNCHRONIZING_ORGANIZATIONAL_TABLE_ARCHIVE = newArgs0("ARCH01032", BpmRuntimeErrorMessages.getString("ARCH01032")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ARCH_FAILED_DELETING_ENTRIES_FROM_DATA_CLUSTER_TABLE = newArgs2("ARCH01033", BpmRuntimeErrorMessages.getString("ARCH01033")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_NO_MODEL_WITH_ID = newArgs1("ARCH01034", BpmRuntimeErrorMessages.getString("ARCH01034")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_CANNOT_DELETE_DATA_FOR_NONEXISTING_DATA_ID = newArgs1("ARCH01035", BpmRuntimeErrorMessages.getString("ARCH01035")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ARCH_COULD_NOT_SYNCHRONIZE_DATA_CLUSTER_TABLE = newArgs2("ARCH01036", BpmRuntimeErrorMessages.getString("ARCH01036")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_ARCHIVING_ENTRIES_FROM_TABLE_INCLUDED_IN_TRANSITIVE_CLOSURE_FOR_ALREADY_ARCHIVED_PROCESS_INSTAMCES = newArgs1("ARCH01037", BpmRuntimeErrorMessages.getString("ARCH01037")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_FAILED_SYNCHRONIZING_PK_STABLE_TABLE = newArgs1("ARCH01038", BpmRuntimeErrorMessages.getString("ARCH01038")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ARCH_QUALIFIED_ID_NEEDED = newArgs1("ARCH01039", BpmRuntimeErrorMessages.getString("ARCH01039")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_SYNCHRONIZING_STRING_DATA_TABLE_ARCHIVE = newArgs0("ARCH01040", BpmRuntimeErrorMessages.getString("ARCH01040")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 ARCH_FAILED_PATCHING_ARCHIVE = newArgs0("ARCH01041", BpmRuntimeErrorMessages.getString("ARCH01041")); //$NON-NLS-1$ //$NON-NLS-2$

   ////Hazlecast related

   public static final Args0 HZLC_FAILES_ENLISTING_HAZLECAST_CACHE_IN_CURRENT_TRANSACTION = newArgs0("HZLC01001", BpmRuntimeErrorMessages.getString("HZLC01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 HZLC_FAILED_RETRIEVING_HAZLECAST_CONNECTION_FACTORY_FROM_JNDI = newArgs0("HZLC01002", BpmRuntimeErrorMessages.getString("HZLC01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 HZLC_FAILES_ENLISTING_HAZLECAST_OBJECTS_IN_CURRENT_TRANSACTION = newArgs0("HZLC01001", BpmRuntimeErrorMessages.getString("HZLC01001")); //$NON-NLS-1$ //$NON-NLS-2$

   ////Diagram related

   public static final Args1 DIAG_CANNOT_LOAD_RESOURCE = newArgs1("DIAG01001", BpmRuntimeErrorMessages.getString("DIAG01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DIAG_UNEXPECTED_ARROW_TYPE = newArgs0("DIAG01002", BpmRuntimeErrorMessages.getString("DIAG01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 DIAG_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_INTEGER = newArgs2("DIAG01003", BpmRuntimeErrorMessages.getString("DIAG01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 DIAG_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_TRUE_OR_FALSE = newArgs2("DIAG01004", BpmRuntimeErrorMessages.getString("DIAG01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 DIAG_FAILED_TO_CREATE_STROKE_THE_PROPERTY_SET = newArgs1("DIAG01005", BpmRuntimeErrorMessages.getString("DIAG01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 DIAG_FAILED_TO_CREATE_FONT_FOR_THE_PROPERTY_MALFORMED_SIZE_STRING = newArgs2("DIAG01006", BpmRuntimeErrorMessages.getString("DIAG01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DIAG_NO_TAG_SPECIFED_BEFORE_CURLY_BRACE = newArgs0("DIAG01007", BpmRuntimeErrorMessages.getString("DIAG01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DIAG_NO_TAG_SPECIFED_BEFORE_COLON = newArgs0("DIAG01008", BpmRuntimeErrorMessages.getString("DIAG01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DIAG_CANNOT_LOAD_IMAGE_ICON = newArgs0("DIAG01009", BpmRuntimeErrorMessages.getString("DIAG01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 DIAG_RESOURCE_FOR_OBJECT_COULD_NOT_BE_LOADED = newArgs2("DIAG01010", BpmRuntimeErrorMessages.getString("DIAG01010")); //$NON-NLS-1$ //$NON-NLS-2$

   ////JDBC related

   public static final Args1 JDBC_CANNOT_WRITE_TO_FILE = newArgs1("JDBC01001", BpmRuntimeErrorMessages.getString("JDBC01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_COULD_NOT_VERIFY_LOCK_TABLE = newArgs1("JDBC01002", BpmRuntimeErrorMessages.getString("JDBC01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_DATA_CLUSTER_TABLE_NOT_ALLOWED_BECAUSE_NAME_PREDEFINED_BY_IPP_ENGINE = newArgs1("JDBC01003", BpmRuntimeErrorMessages.getString("JDBC01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_ERROR_CREATING_DATA_CLUSTER_TABLE = newArgs1("JDBC01004", BpmRuntimeErrorMessages.getString("JDBC01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 JDBC_CANNOT_CREATE_DATA_VALUE_FIELD_FOR_SLOT_COLUMN = newArgs2("JDBC01005", BpmRuntimeErrorMessages.getString("JDBC01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_DATABASE_HAS_TO_SUPPORT_SEQUENCES_OR_AUTOMATIC_IDENTITY_COLUMNS = newArgs0("JDBC01006", BpmRuntimeErrorMessages.getString("JDBC01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_INSERT_VALUES_NOT_YET_IMPLEMENTED = newArgs0("JDBC01007", BpmRuntimeErrorMessages.getString("JDBC01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_FAILED_TO_LOAD_JDBC_DRIVER = newArgs1("JDBC01008", BpmRuntimeErrorMessages.getString("JDBC01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_MAXIMUM_NUMBER_OF_CONNECTIONS_IN_CONNECTION_POOL_EXCEEDED = newArgs1("JDBC01009", BpmRuntimeErrorMessages.getString("JDBC01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_ARCHIVE_AUDITTRAIL_DOES_NOT_ALLOW_CHANGES = newArgs0("JDBC01010", BpmRuntimeErrorMessages.getString("JDBC01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 JDBC_INVALID_TX_ISOLATION_LEVEL = newArgs2("JDBC01011", BpmRuntimeErrorMessages.getString("JDBC01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_DERBY_SESSION_ROLLED_BACK = newArgs0("JDBC01012", BpmRuntimeErrorMessages.getString("JDBC01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_FAILED_OBTAINING_NEW_SEQUECMCE_VALUES_RESULT_SET_EMPTY = newArgs0("JDBC01013", BpmRuntimeErrorMessages.getString("JDBC01013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_FAILED_DELETING_ENRIES_FROM_DATA_CLUSTER_TABLE = newArgs1("JDBC01014", BpmRuntimeErrorMessages.getString("JDBC01014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 JDBC_UNKNOWN_COLUMN_MODIFICATION_TYPE = newArgs1("JDBC01015", BpmRuntimeErrorMessages.getString("JDBC01015")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_PREPARATION_OF_STRING_TO_VALUE_MIGRATION_FAILED = newArgs0("JDBC01016", BpmRuntimeErrorMessages.getString("JDBC01016")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 JDBC_DATABASE_DOES_NEITHER_SUPPORT_SEQUENCES_NOR_IDENTITY_COLUMNS = newArgs0("JDBC01017", BpmRuntimeErrorMessages.getString("JDBC01017")); //$NON-NLS-1$ //$NON-NLS-2$

   ////POJO related

   public static final Args0 POJO_CANNOT_CREATE_OBJECT = newArgs0("POJO01001", BpmRuntimeErrorMessages.getString("POJO01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 POJO_ACCESS_POINT_DOES_NOT_EXIST = newArgs2("POJO01002", BpmRuntimeErrorMessages.getString("POJO01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 POJO_FAILED_READING_BEAN_ATTRIBUTE = newArgs0("POJO01003", BpmRuntimeErrorMessages.getString("POJO01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 POJO_FAILED_SETTING_BEAN_ATTRIBUTE = newArgs0("POJO01004", BpmRuntimeErrorMessages.getString("POJO01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 POJO_CANNOT_CONVERT_VALUT_TO_TYPE = newArgs2("POJO01005", BpmRuntimeErrorMessages.getString("POJO01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 POJO_NOT_A_JAVA_DATA_TYPE = newArgs0("POJO01006", BpmRuntimeErrorMessages.getString("POJO01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 POJO_INVALID_JAVA_BEAN_ACCESS_PATH_TYPE = newArgs1("POJO01007", BpmRuntimeErrorMessages.getString("POJO01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 POJO_METHOD_FROM_CLASS_DOES_NOT_EXIST_OR_IS_NOT_ACCESSIBLE = newArgs2("POJO01008", BpmRuntimeErrorMessages.getString("POJO01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 POJO_METHOD_NOT_ACCESSIBLE_IN_CLASS = newArgs2("POJO01009", BpmRuntimeErrorMessages.getString("POJO01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 POJO_ILLEGAL_ARGUMENT_FOR_METHOD_IN_CLASS = newArgs2("POJO01010", BpmRuntimeErrorMessages.getString("POJO01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 POJO_SETTER_FOR_IN_PATH_DOES_NOT_ACCEPT_SINGLE_PARAMETER = newArgs1("POJO01011", BpmRuntimeErrorMessages.getString("POJO01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 POJO_FINAL_SETTER_FOR_IN_PATH_DOES_NOT_ACCEPT_SINGLE_PARAMETER = newArgs1("POJO01012", BpmRuntimeErrorMessages.getString("POJO01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 POJO_FAILED_READING_JAVA_VALUE = newArgs0("POJO01013", BpmRuntimeErrorMessages.getString("POJO01013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 POJO_FAILED_SETTING_JAVA_VALUE = newArgs0("POJO01014", BpmRuntimeErrorMessages.getString("POJO01014")); //$NON-NLS-1$ //$NON-NLS-2$


   ////Structured Type related

   public static final Args0 SDT_FAILED_GENERATING_XML = newArgs0("SDT01001", BpmRuntimeErrorMessages.getString("SDT01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 SDT_ATTRIBUTE_MUST_BE_DETACHED = newArgs0("SDT01002", BpmRuntimeErrorMessages.getString("SDT01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_NO_SUCH_ATTRIBUTE = newArgs1("SDT01003", BpmRuntimeErrorMessages.getString("SDT01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 SDT_FAILED_READING_XML_INPUT = newArgs0("SDT01004", BpmRuntimeErrorMessages.getString("SDT01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 SDT_EXPRESSION_WAS_EXPECTED_TO_RETURN_0_OR_1_HITS = newArgs2("SDT01005", BpmRuntimeErrorMessages.getString("SDT01005")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args5 SDT_COULD_NOT_PARSE_DATE_TIME_USING_STANDARD_XSD_FORMATS = newArgs5("SDT01006", BpmRuntimeErrorMessages.getString("SDT01006")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_BIGDATA_TYPE_IS_NOT_SUPPORTED_YET = newArgs1("SDT01007", BpmRuntimeErrorMessages.getString("SDT01007")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 SDT_BOOLEAN_VALUE_MUST_BE_TRUE_OR_FALSE = newArgs0("SDT01008", BpmRuntimeErrorMessages.getString("SDT01008")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 SDT_ENUM_VALUE_IS_NOT_ALLOWED_FOR_ELEMENT = newArgs2("SDT01009", BpmRuntimeErrorMessages.getString("SDT01009")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_XPATH_CANNOT_BE_ASSIGNED_MULTIPLE_VALUES = newArgs1("SDT01010", BpmRuntimeErrorMessages.getString("SDT01010")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_XPATH_CANNOT_BE_USED_TO_SET_DATA_VALUE = newArgs1("SDT01011", BpmRuntimeErrorMessages.getString("SDT01011")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_NO_DATA_FOUND_FOR_XPATH = newArgs1("SDT01012", BpmRuntimeErrorMessages.getString("SDT01012")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_XPATH_CANNOT_BE_USED_TO_SET_DATA_VALUE_SINCE_IT_RETURNS_NODES_FROM_DIFFERENT_ORIGIN = newArgs1("SDT01013", BpmRuntimeErrorMessages.getString("SDT01013")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_INPATH_CANNOT_BE_USED_TO_SET_DATA_VALUE = newArgs1("SDT01014", BpmRuntimeErrorMessages.getString("SDT01014")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_COULD_NOT_CREATE_QUALIFIED_XPATH_FROM_XPATH = newArgs1("SDT01015", BpmRuntimeErrorMessages.getString("SDT01015")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_COULD_NOT_FIND_XSD_IN_CLASSPATH = newArgs1("SDT01016", BpmRuntimeErrorMessages.getString("SDT01016")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_NULL_VALUES_NOT_SUPPORTED_WITH_OPERATOR = newArgs1("SDT01017", BpmRuntimeErrorMessages.getString("SDT01017")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_CANNOT_DETERMINE_BRIDGEOBJECT_FROM_ACCESSPOINT = newArgs1("SDT01018", BpmRuntimeErrorMessages.getString("SDT01018")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_XPATH_CANNOT_BE_USED_FOR_IN_DATA_MAPPING_SINCE_IT_CAN_RETURN_SEVERAL_ITEMS_FROM_DIFFERENT_LEVELS = newArgs1("SDT01019", BpmRuntimeErrorMessages.getString("SDT01019")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 SDT_FAILED_PARSING_XML_DOCUMENT = newArgs0("SDT01020", BpmRuntimeErrorMessages.getString("SDT01020")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 SDT_NODE_MUST_NOT_BE_DETACHED = newArgs0("SDT01021", BpmRuntimeErrorMessages.getString("SDT01021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_INVALID_CHILD_ELEMENT = newArgs1("SDT01022", BpmRuntimeErrorMessages.getString("SDT01022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_NO_SUCH_CHILD = newArgs1("SDT01023", BpmRuntimeErrorMessages.getString("SDT01023")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 SDT_COULD_NOT_ANALYSE_STRUCTURED_DATA_FOR_XPATH_OID = newArgs2("SDT01024", BpmRuntimeErrorMessages.getString("SDT01024")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 SDT_XPATH_IS_NOT_DEFINED = newArgs1("SDT01025", BpmRuntimeErrorMessages.getString("SDT01025")); //$NON-NLS-1$ //$NON-NLS-2$



   ////Generic Exception message

   public static final Args0 GEN_AN_EXCEPTION_OCCURED = newArgs0("GEN01001", BpmRuntimeErrorMessages.getString("GEN01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE = newArgs1("GEN01002", BpmRuntimeErrorMessages.getString("GEN01002")); //$NON-NLS-1$ //$NON-NLS-2$

   private static final Object[] NONE = {};

   private final String defaultMessage;

   private final Object[] args;

   private BpmRuntimeError(String id)
   {
      this(id, null);
   }

   private BpmRuntimeError(String id, String defaultMessage)
   {
      this(id, defaultMessage, NONE);
   }

   private BpmRuntimeError(String code, String defaultMessage, Object msgArgs[])
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
      return new Args0(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
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
      return new Args1(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
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
      return new Args2(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
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

   /**
    * Static factory to prepare for future generification.
    */
   public static Args5 newArgs5(String errorCode, String defaultMessage)
   {
      return new Args5(errorCode, defaultMessage);
   }

   public static class Args0 extends AbstractErrorFactory
   {
      private Args0(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmRuntimeError raise()
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

      public BpmRuntimeError raise(Object arg)
      {
         return buildError(new Object[] {arg});
      }

      public BpmRuntimeError raise(long arg)
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

      public BpmRuntimeError raise(Object arg1, Object arg2)
      {
         return buildError(new Object[] {arg1, arg2});
      }

      public BpmRuntimeError raise(long arg1, long arg2)
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

      public BpmRuntimeError raise(Object arg1, Object arg2, Object arg3)
      {
         return buildError(new Object[] { arg1, arg2, arg3 });
      }

      public BpmRuntimeError raise(long arg1, long arg2, long arg3)
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

      public BpmRuntimeError raise(Object arg1, Object arg2, Object arg3, Object arg4)
      {
         return buildError(new Object[] { arg1, arg2, arg3, arg4 });
      }

      public BpmRuntimeError raise(long arg1, long arg2, long arg3, long arg4)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3), new Long(arg4) });
      }
   }

   public static class Args5 extends AbstractErrorFactory
   {
      private Args5(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmRuntimeError raise(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5)
      {
         return buildError(new Object[] { arg1, arg2, arg3, arg4, arg5 });
      }

      public BpmRuntimeError raise(long arg1, long arg2, long arg3, long arg4, long arg5)
      {
         return buildError(new Object[] { new Long(arg1), new Long(arg2), new Long(arg3), new Long(arg4), new Long(arg5) });
      }
   }

   public static Args newArgs(String errorCode)
   {
      return new Args(errorCode, BpmRuntimeErrorMessages.getString(errorCode));
   }

   public static class Args extends AbstractErrorFactory
   {
      private Args(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public BpmRuntimeError raise(Object ... arg)
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

      protected BpmRuntimeError buildError(Object[] args)
      {
         return new BpmRuntimeError(errorCode, defaultMessage, args);
      }

      public String getErrorCode()
      {
         return errorCode;
      }
   }

   static BpmRuntimeError createError(String id, String defaultMessage, Object arg0)
   {
      return new BpmRuntimeError(id, defaultMessage, new Object[] {arg0});
   }

}
