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
package org.eclipse.stardust.engine.api.runtime;

import java.text.MessageFormat;

import org.eclipse.stardust.common.error.ErrorCase;


/**
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

   public static final Args1 MDL_UNKNOWN_EVENT_HANDLER_ID = newArgs1("MDL01172", BpmRuntimeErrorMessages.getString("MDL01172")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_UNKNOWN_TYPE_DECLARATION_ID = newArgs1("MDL01182", BpmRuntimeErrorMessages.getString("MDL01182")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_UNKNOWN_DATA_TYPE_ID = newArgs1("MDL01183", BpmRuntimeErrorMessages.getString("MDL01183")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_DANGLING_DATA_PATH = newArgs1("MDL02021", BpmRuntimeErrorMessages.getString("MDL02021")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_DANGLING_IN_DATA_PATH = newArgs1("MDL02022", BpmRuntimeErrorMessages.getString("MDL02022")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_DANGLING_OUT_DATA_PATH = newArgs1("MDL02023", BpmRuntimeErrorMessages.getString("MDL02023")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 MDL_DTD_VALIDATION_FAILED = newArgs1("MDL03001", BpmRuntimeErrorMessages.getString("MDL03001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_XSD_VALIDATION_FAILED = newArgs1("MDL03002", BpmRuntimeErrorMessages.getString("MDL03002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 MDL_INVALID_DOCUMENT_ROOT_TYPE = newArgs1("MDL03003", BpmRuntimeErrorMessages.getString("MDL03003")); //$NON-NLS-1$ //$NON-NLS-2$

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
   public static final Args1 ATDB_PROCESS_INSTANCE_NOT_SPAWNED = newArgs1("ATDB01106", "Process instance with OID {0} is not spawned.");

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

   public static final Args0 DMS_UNKNOWN_FILE_VERSION_ID = newArgs0("DMS01153", "Document version not found.");
   public static final Args0 DMS_CANNOT_REMOVE_ROOT_FILE_VERSION = newArgs0("DMS01156", "Root document version cannot be removed.");

   public static final Args1 DMS_DOCUMENT_TYPE_INVALID = newArgs1("DMS01303", BpmRuntimeErrorMessages.getString("DMS01303")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 DMS_DOCUMENT_TYPE_DEPLOY_ERROR = newArgs0("DMS01313", BpmRuntimeErrorMessages.getString("DMS01313")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 DMS_GENERIC_SECURITY_ERROR = newArgs0("DMS02000", BpmRuntimeErrorMessages.getString("DMS02000")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_SECURITY_ERROR_ADMIN_REQUIRED = newArgs0("DMS02001", BpmRuntimeErrorMessages.getString("DMS02001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 DMS_SECURITY_ERROR_DMS_READONLY_FOR_PREFERENCES = newArgs0("DMS02002", BpmRuntimeErrorMessages.getString("DMS02002")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 DMS_ANNOTATIONS_ID_PRESENT = newArgs1("DMS03001", BpmRuntimeErrorMessages.getString("DMS03001")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Audittrail related

   public static final Args0 ATDB_NO_MATCHING_USER_REALM = newArgs0("ATDB02101", BpmRuntimeErrorMessages.getString("ATDB02101")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_REALM_OID = newArgs1("ATDB02102", BpmRuntimeErrorMessages.getString("ATDB02102")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_UNKNOWN_USER_REALM_ID = newArgs2("ATDB02103", BpmRuntimeErrorMessages.getString("ATDB02103")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_USER_REALM_ID_EXISTS = newArgs1("ATDB02104", BpmRuntimeErrorMessages.getString("ATDB02104")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_DELETION_FAILED_USER_REALM_ID_DANGLING_REFERENCE = newArgs1("ATDB02105", BpmRuntimeErrorMessages.getString("ATDB02105")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 ATDB_NO_MATCHING_USER = newArgs0("ATDB02111", BpmRuntimeErrorMessages.getString("ATDB02111")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 ATDB_UNKNOWN_USER_OID = newArgs1("ATDB02112", BpmRuntimeErrorMessages.getString("ATDB02112")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_UNKNOWN_USER_ID = newArgs2("ATDB02113", BpmRuntimeErrorMessages.getString("ATDB02113")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 ATDB_USER_ID_EXISTS = newArgs2("ATDB02114", BpmRuntimeErrorMessages.getString("ATDB02114")); //$NON-NLS-1$ //$NON-NLS-2$

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

   public static final Args2 AUTHx_AUTH_MISSING_GRANTS = newArgs2("AUTHx01000", BpmRuntimeErrorMessages.getString("AUTHx01000")); //$NON-NLS-1$ //$NON-NLS-2$

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

   public static final Args1 BPMRT_FAILED_CREATING_DATA_VALUE = newArgs1("BPMRT02121", BpmRuntimeErrorMessages.getString("BPMRT02121")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_DAEMON_ALREADY_RUNNING = newArgs1("CONC03100", BpmRuntimeErrorMessages.getString("CONC03100")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args1 BPMRT_AI_CURRENTLY_ACTIVATED_BY_SELF = newArgs1("BPMRT03101", BpmRuntimeErrorMessages.getString("BPMRT03101")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_AI_CURRENTLY_ACTIVATED_BY_OTHER = newArgs2("CONC03102", BpmRuntimeErrorMessages.getString("CONC03102")); //$NON-NLS-1$ //$NON-NLS-2$

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

   public static final Args0 BPMRT_INVALID_ORGANIZATION_HIERARCHY = newArgs0("BPMRT03813", BpmRuntimeErrorMessages.getString("BPMRT03813")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_DEPARTMENT_HAS_ACTIVE_ACTIVITY_INSTANCES = newArgs1("BPMRT03814", BpmRuntimeErrorMessages.getString("BPMRT03814")); //$NON-NLS-1$ //$NON-NLS-2$
   
   public static final Args0 BPMRT_INVALID_INDEXED_XPATH = newArgs0("BPMRT03815", "Invalid XPath, indexed XPath not supported.");

   public static final Args2 BPMRT_ILLEGAL_AI_STATE_CHANGE = newArgs2("BPMRT03901", BpmRuntimeErrorMessages.getString("BPMRT03901")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args3 BPMRT_ILLEGAL_AI_STATE_CHANGE_FOR_AI = newArgs3("BPMRT03902", BpmRuntimeErrorMessages.getString("BPMRT03902")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args4 BPMRT_ILLEGAL_AI_STATE_CHANGE_FOR_AI_WITH_PI_STATE = newArgs4("BPMRT03903", BpmRuntimeErrorMessages.getString("BPMRT03903")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 BPMRT_GENERAL_INCOMPATIBLE_TYPE = newArgs2("BPMRT04001", BpmRuntimeErrorMessages.getString("BPMRT04001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_INCOMPATIBLE_TYPE_FOR_DATA = newArgs1("BPMRT04002", BpmRuntimeErrorMessages.getString("BPMRT04002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 BPMRT_INCOMPATIBLE_TYPE_FOR_DATA_WITH_PATH = newArgs2("BPMRT04003", BpmRuntimeErrorMessages.getString("BPMRT04003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args1 BPMRT_INVALID_PROBABILIY = newArgs1("BPMRT04004", BpmRuntimeErrorMessages.getString("BPMRT04004"));
   public static final Args2 BPMRT_USER_NOT_ALLOWED_ACTIVATE_QA_INSTANCE = newArgs2("BPMRT04005", BpmRuntimeErrorMessages.getString("BPMRT04005"));
   public static final Args2 BPMRT_DELEGATE_QA_INSTANCE_NOT_ALLOWED = newArgs2("BPMRT04006", BpmRuntimeErrorMessages.getString("BPMRT04006"));
   public static final Args0 BPMRT_MODIFY_DATA_QA_INSTANCE_NOT_ALLOWED = newArgs0("BPMRT04007", BpmRuntimeErrorMessages.getString("BPMRT04007"));
   public static final Args1 BPMRT_COMPLETE_QA_NO_ATTRIBUTES_SET = newArgs1("BPMRT04008", BpmRuntimeErrorMessages.getString("BPMRT04008"));
   public static final Args0 BPMRT_NO_ERROR_CODE_SET = newArgs0("BPMRT04009", BpmRuntimeErrorMessages.getString("BPMRT04009"));

   
   
   public static final Args1 BPMRT_PI_IS_CASE = newArgs1("BPMRT03831"); //$NON-NLS-1$
   public static final Args1 BPMRT_PI_IS_MEMBER = newArgs1("BPMRT03832"); //$NON-NLS-1$
   public static final Args1 BPMRT_PI_NOT_ACTIVE = newArgs1("BPMRT03833"); //$NON-NLS-1$
   public static final Args1 BPMRT_PI_NOT_CASE = newArgs1("BPMRT03834"); //$NON-NLS-1$
   public static final Args2 BPMRT_PI_NOT_MEMBER = newArgs2("BPMRT03835"); //$NON-NLS-1$

   public static final Args1 BPMRT_NO_CHANGES_TO_MODEL = newArgs1("BPMRT03850"); //$NON-NLS-1$

   //// JMS related

   public static final Args1 JMS_NO_MESSAGE_ACCEPTORS_FOUND = newArgs1("JMS01001", BpmRuntimeErrorMessages.getString("JMS01001")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Preference Store related

   public static final Args0 PREF_PREF_STORE_READONLY = newArgs0("PREF01001", BpmRuntimeErrorMessages.getString("PREF01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 PREF_EMPTY_PREF_STORE_READONLY = newArgs0("PREF01002", BpmRuntimeErrorMessages.getString("PREF01002")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Query related

   public static final Args1 QUERY_XPATH_ON_NON_STRUCT_DATA = newArgs1("QUERY01001", BpmRuntimeErrorMessages.getString("QUERY01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_MISSING_XPATH_ON_NON_STRUCT_DATA = newArgs2("QUERY01002", BpmRuntimeErrorMessages.getString("QUERY01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_XPATH_ON_STRUCT_DATA_MUST_POINT_TO_PRIMITIVE = newArgs2("QUERY01003", BpmRuntimeErrorMessages.getString("QUERY01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 QUERY_XPATH_ON_STRUCT_DATA_ORDER_BY_MUST_POINT_TO_PRIMITIVE = newArgs2("QUERY01004", BpmRuntimeErrorMessages.getString("QUERY01004")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args2 QUERY_FILTER_IS_XXX_FOR_QUERY = newArgs2("QUERY02001", BpmRuntimeErrorMessages.getString("QUERY02001")); //$NON-NLS-1$ //$NON-NLS-2$


   //// IPP WS related

   public static final Args3 IPPWS_FACTORY_WS_PROPERTIES_MISSING = newArgs3("IPPWS01001", BpmRuntimeErrorMessages.getString("IPPWS01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_ADDING_SESSION_PROP_TO_SOAP_HEADER = newArgs0("IPPWS01002", BpmRuntimeErrorMessages.getString("IPPWS01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_ADDING_SESSION_PROP_TO_NONEXISTING_SOAP_HEADER = newArgs0("IPPWS01003", BpmRuntimeErrorMessages.getString("IPPWS01003")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_INIT_MALFORMED_URL = newArgs0("IPPWS01004", BpmRuntimeErrorMessages.getString("IPPWS01004")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_FACTORY_FAILED_RESOLVING_WS_PROPERTIES = newArgs0("IPPWS01005", BpmRuntimeErrorMessages.getString("IPPWS01005")); //$NON-NLS-1$ //$NON-NLS-2$

   public static final Args0 IPPWS_ENV_CREATION_FAILED_USER_NULL = newArgs0("IPPWS02001", BpmRuntimeErrorMessages.getString("IPPWS02001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_ENV_CREATION_FAILED_SESSION_PROPS_NULL = newArgs0("IPPWS02002", BpmRuntimeErrorMessages.getString("IPPWS02002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 IPPWS_ENV_CREATION_FAILED_ALL_NULL = newArgs0("IPPWS02003", BpmRuntimeErrorMessages.getString("IPPWS02003")); //$NON-NLS-1$ //$NON-NLS-2$

   //// Login related
   public static final Args0 LOGIN_LDAP_INVALID_USER_PASSWORD = newArgs0("LOGIN01001", BpmRuntimeErrorMessages.getString("LOGIN01001")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args0 LOGIN_LDAP_UNABLE_TO_CONNECT = newArgs0("LOGIN01002", BpmRuntimeErrorMessages.getString("LOGIN01002")); //$NON-NLS-1$ //$NON-NLS-2$
   public static final Args2 LOGIN_LDAP_NAMING_EXCEPTION = newArgs2("LOGIN01003", BpmRuntimeErrorMessages.getString("LOGIN01003")); //$NON-NLS-1$ //$NON-NLS-2$

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
   }

   static BpmRuntimeError createError(String id, String defaultMessage, Object arg0)
   {
      return new BpmRuntimeError(id, defaultMessage, new Object[] {arg0});
   }

}
