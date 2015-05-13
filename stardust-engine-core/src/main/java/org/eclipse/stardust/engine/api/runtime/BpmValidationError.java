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
   public static final Args VAL_DUPLICATE_IDENTIFIER = new Args("VAL01000");
   public static final Args VAL_INVALID_IDENTIFIER = new Args("VAL01001");
   public static final Args VAL_CANNOT_RETRIEVE_CLASS_FOR_VALIDATION = new Args("VAL01002");
   public static final Args VAL_HAS_NO_ID = new Args("VAL01003");
   public static final Args VAL_HAS_INVALID_ID = new Args("VAL01004");
   public static final Args VAL_DMS_OPERATION_NOT_SET = new Args("VAL01005");

   //Model related
   public static final Args MDL_INVALID_QA_CODE_ID = new Args("MDL01001");
   public static final Args MDL_DUPLICATE_QA_CODE = new Args("MDL01002");
   public static final Args MDL_UNSUPPORTED_SCRIPT_LANGUAGE = new Args("MDL01003");
   public static final Args MDL_NO_DEFAULT_VALUE_FOR_CONFIGURATION_VARIABLE = new Args("MDL01004");
   public static final Args MDL_CONFIGURATION_VARIABLE_NEVER_USED = new Args("MDL01005");
   public static final Args MDL_CONFIGURATION_VARIABLE_DOES_NOT_EXIST = new Args("MDL01006");
   public static final Args MDL_CIRCULAR_REFERENCES_TO = new Args("MDL01007");
   public static final Args MDL_REFERENCE_TO_MODEL_IS_INALID_UNTIL = new Args("MDL01008");
   public static final Args MDL_REFERENCE_NOT_RESOLVED_TO_LAST_DEPLOYED_VERSION = new Args("MDL01009");
   public static final Args MDL_REFERENCE_IS_RESOLVED_TO_MULTIPLE_MODEL_VERSION = new Args("MDL01010");
   public static final Args MDL_NO_MODEL_WITH_OID_FOUND = new Args("MDL01011");
   public static final Args MDL_NO_MODEL_ACTIVE = new Args("MDL01012");
   public static final Args MDL_NO_MODEL_DEPLOYED = new Args("MDL01013");
   public static final Args MDL_REFERENCED_PACKAGE_WITH_NAMESPACE_NOT_FOUND = new Args("MDL01014");
   public static final Args MDL_AUDITTRAIL_CONTAINS_MODEL_WHICH_DIFFERS_FROM_THIS_MODEL = new Args("MDL01015");
   public static final Args MDL_PREDECESSOR_MODEL_NOT_FOUND = new Args("MDL01016");
   public static final Args MDL_UNKNOWN_MODEL_VERSION = new Args("MDL01017");
   public static final Args MDL_MODEL_VERSION = new Args("MDL01018");
   public static final Args MDL_CONFIGURATION_VARIABLE_IS_INVALID = new Args("MDL01019");

   //Process Definition related
   public static final Args PD_NO_START_ACTIVITY = new Args("PD01001");
   public static final Args PD_DUPLICATE_ID = new Args("PD01002");
   public static final Args PD_ID_EXCEEDS_LENGTH = new Args("PD01003");
   public static final Args PD_FORMAL_PARAMETER_NO_DATA_SET = new Args("PD01004");
   public static final Args PD_DUPLICATE_TRANSITION_SAME_SOURCE_OR_TARGET = new Args("PD01005");
   public static final Args PD_MULTIPLE_START_ACTIVYTIES = new Args("PD01006");
   public static final Args PD_NO_ACTIVITIES_DEFINED = new Args("PD01007");
   public static final Args PD_PROCESS_INTERFACE_NOT_RESOLVED = new Args("PD01008");
   public static final Args PD_POTENTIAL_DEADLOCKS = new Args("PD01009");
   public static final Args PD_FORMAL_PARAMETER_INCOMPATIBLE_DATA_FOR_EXTERNAL_INVOCATION = new Args("PD01010");

   //Application related
   public static final Args APP_TYPE_NO_LONGER_SUPPORTED = new Args("APP01001");
   public static final Args APP_UNSPECIFIED_CLASS_FOR_JFC_APPLICATION = new Args("APP01002");
   public static final Args APP_UNSPECIFIED_COMPLETION_METHOD_FOR_JFC_APPLICATION = new Args("APP01003");
   public static final Args APP_COMPLETION_METHOD_NOT_FOUND = new Args("APP01004");
   public static final Args APP_DUPLICATE_ID = new Args("APP01005");
   public static final Args APP_NO_TYPE_MAPPING_DEFINED_FOR_XML_TYPE = new Args("APP01006");
   public static final Args APP_XML_TYPE_HAS_INVALID_TYPE_MAPPING = new Args("APP01007");
   public static final Args APP_INVALID_TEMPLATE = new Args("APP01008");
   public static final Args APP_INVALID_WSDL_URL = new Args("APP01009");
   public static final Args APP_WS_PROPERTY_NOT_SET = new Args("APP01010");
   public static final Args APP_PROPERTY_NOT_SET = new Args("APP01011");
   public static final Args APP_PARAMETER_HAS_NO_ID_DEFINED = new Args("APP01012");
   public static final Args APP_PARAMETER_HAS_INVALID_ID_DEFINED = new Args("APP01013");
   public static final Args APP_NO_LOCATION_DEFINED_FOR_PARAMETER = new Args("APP01014");
   public static final Args APP_NO_VALID_TYPE_FOR_PARAMETER_CLASS_CANNOT_BE_FOUND = new Args("APP01015");
   public static final Args APP_NO_VALID_TYPE_FOR_PARAMETER_CLASS_COULD_NOT_BE_LOADED = new Args("APP01016");
   public static final Args APP_DUPLICATE_ID_USED = new Args("APP01017");
   public static final Args APP_INVALID_MAIL_ADDRESS = new Args("APP01018");
   public static final Args APP_UNDEFINED_HTML_PATH_FOR_JSP_APPLICATION = new Args("APP01019");

   //Actions related
   public static final Args ACTN_NO_DATA_DEFINED = new Args("ACTN01001");
   public static final Args ACTN_NO_ACCESS_POINT_DEFINED = new Args("ACTN01002");
   public static final Args ACTN_NO_PROCESS_SELECTED = new Args("ACTN01003");
   public static final Args ACTN_NO_TYPE = new Args("ACTN01004");
   public static final Args ACTN_NO_NAME = new Args("ACTN01005");
   public static final Args ACTN_NO_RECEIVER_TYPE_SPECIFIED = new Args("ACTN01006");
   public static final Args ACTN_NO_RECEIVING_PARTICIPANT_SPECIFIED = new Args("ACTN01007");
   public static final Args ACTN_NO_EMAIL_ADDRESS_SPECIFIED = new Args("ACTN01008");

   //Activity related
   public static final Args ACTY_DUPLICATE_ID = new Args("ACTY01001");
   public static final Args ACTY_ID_EXCEEDS_MAXIMUM_LENGTH = new Args("ACTY01002");
   public static final Args ACTY_NO_PERFORMER = new Args("ACTY01003");
   public static final Args ACTY_PERFORMER_DOES_NOT_EXIST = new Args("ACTY01004");
   public static final Args ACTY_PERFORMER_SHOULD_NOT_BE_CONDITIONAL_PERFORMER = new Args("ACTY01005");
   public static final Args ACTY_NO_QA_PERFORMER_SET = new Args("ACTY01006");
   public static final Args ACTY_QA_PERFORMER_SHOULD_NOT_BE_CONDITIONAL_PERFORMER = new Args("ACTY01007");
   public static final Args ACTY_NO_IMPLEMENTATION_PROCESS_SET_FOR_SUBPROCESS_ACTIVITY = new Args("ACTY01008");
   public static final Args ACTY_SUBPROCESSMODE_NOT_SET = new Args("ACTY01009");
   public static final Args ACTY_NO_APPLICATION_SET_FOR_APPLICATION_ACTIVITY = new Args("ACTY01010");
   public static final Args ACTY_NO_ACCESS_POINT_FOR_APPLICATION = new Args("ACTY01011");
   public static final Args ACTY_NO_EXCEPTION_FLOW_TRANSITION_FOR_EVENT_HANDLER = new Args("ACTY01012");
   public static final Args ACTY_BOUNDARY_EVENTS_WITH_UNDISJUNCT_TYPE_HIERARCHIES = new Args("ACTY01013");
   public static final Args ACTY_INTERMEDIATE_EVENTS_MUST_HAVE_ONE_IN_AND_OUTBOUND_SEQUENCE_FLOW = new Args("ACTY01014");
   public static final Args ACTY_INCOMPATIBLE_SUBPROCESSMODE = new Args("ACTY01015");
   public static final Args ACTY_NO_LOOP_INPUT_DATA = new Args("ACTY01016");


   //Conditions related
   public static final Args COND_NOT_AN_EXCEPTION_CLASS = new Args("COND01001");
   public static final Args COND_NO_CONDITION_SPECIFIED = new Args("COND01002");
   public static final Args COND_TARGET_STATE_IN_SAME_STATE_AS_SOURCE_STATE = new Args("COND01003");
   public static final Args COND_INVALID_DATA_MAPPING = new Args("COND01004");
   public static final Args COND_INVALID_DATA_PATH = new Args("COND01005");
   public static final Args COND_INVALID_DATA_SPECIFIED = new Args("COND01006");
   public static final Args COND_NO_PROCESS_OR_ACTIVITY_CONTEXT = new Args("COND01007");
   public static final Args COND_NO_PERIOD_SPECIFIED = new Args("COND01008");
   public static final Args COND_NO_DATA_SPECIFIED = new Args("COND01009");

   //Trigger related
   public static final Args TRIGG_UNSPECIFIED_PARTICIPANT_FOR_TRIGGER = new Args("TRIGG01001");
   public static final Args TRIGG_INVALID_PARTICIPANT_FOR_TRIGGER = new Args("TRIGG01002");
   public static final Args TRIGG_UNSPECIFIED_START_TIME_FOR_TRIGGER = new Args("TRIGG01003");
   public static final Args TRIGG_DUPLICATE_ID_FOR_PROCESS_DEFINITION = new Args("TRIGG01004");
   public static final Args TRIGG_ID_EXCEEDS_MAXIMUM_LENGTH = new Args("TRIGG01005");
   public static final Args TRIGG_NO_NAME_SET = new Args("TRIGG01006");
   public static final Args TRIGG_NO_TYPE_SET = new Args("TRIGG01007");
   public static final Args TRIGG_PARAMETER_MAPPING_DOES_NOT_SPECIFY_DATA = new Args("TRIGG01008");
   public static final Args TRIGG_PARAMETER_MAPPING_DOES_NOT_SPECIFY_PARAMETER = new Args("TRIGG01009");
   public static final Args TRIGG_PARAMETER_FOR_PARAMETER_MAPPING_INVALID = new Args("TRIGG01010");
   public static final Args TRIGG_ACCESSPOINT_HAS_INVALID_ID = new Args("TRIGG01011");
   public static final Args TRIGG_SCAN_TRIGGERS_DO_NOT_SUPPORT_ACCESS_POINT_TYPE = new Args("TRIGG01012");
   public static final Args TRIGG_PARAMETER_MAPPING_CONTAINS_AN_INVALID_TYPE_CONVERSION = new Args("TRIGG01013");
   public static final Args TRIGG_UNSPECIFIED_MESSAGE_TYPE_FOR_JMS_TRIGGER = new Args("TRIGG01014");
   public static final Args TRIGG_PARAMETER_HAS_NO_ID = new Args("TRIGG01015");
   public static final Args TRIGG_PARAMETER_HAS_INVALID_ID_DEFINED = new Args("TRIGG01016");
   public static final Args TRIGG_NO_LOCATION_FOR_PARAMETER_SPECIFIED = new Args("TRIGG01017");
   public static final Args TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_CANNOT_BE_FOUND = new Args("TRIGG01018");
   public static final Args TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_COULD_NOT_BE_LOADED = new Args("TRIGG01019");
   public static final Args TRIGG_UNSPECIFIED_USER_NAME_FOR_MAIL_TRIGGER = new Args("TRIGG01020");
   public static final Args TRIGG_UNSPECIFIED_PASSWORD_FOR_MAIL_TRIGGER = new Args("TRIGG01021");
   public static final Args TRIGG_UNSPECIFIED_SERVER_NAME_FOR_MAIL_TRIGGER = new Args("TRIGG01022");
   public static final Args TRIGG_UNSPECIFIED_PROTOCOL_FOR_MAIL_TRIGGER = new Args("TRIGG01023");

   //Transition related
   public static final Args TRAN_ID_EXCEEDS_MAXIMUM_LENGTH = new Args("TRAN01001");
   public static final Args TRAN_NO_BOUNDARY_EVENT_HANDLER_WITH_ID_FOR_EXCEPTION_TRANSITION_FOUND = new Args("TRAN01002");
   public static final Args TRAN_EXPRESSION_SCRIPTING_LANGUAGE_DO_NOT_MATCH = new Args("TRAN01003");
   public static final Args TRAN_UNSUPPORTED_SCRIPTING_LANGUAGE = new Args("TRAN01004");

   //Structured Types related
   public static final Args SDT_DUPLICATE_ID_FOR_TYPE_DECLARATION = new Args("SDT01000");
   public static final Args SDT_TYPE_DECLARATION_NOT_ALLOWED_TO_CONTAIN_VARIABLES = new Args("SDT01001");
   public static final Args SDT_REFERENCED_PARENT_TYPE_NOT_FOUND = new Args("SDT01002");
   public static final Args SDT_XSD_SCHEMA_NOT_FOUND = new Args("SDT01003");
   public static final Args SDT_XSD_IMPORT_NOT_RESOLVABLE = new Args("SDT01004");
   public static final Args SDT_XSD_INCLUDE_NOT_RESOLVABLE = new Args("SDT01005");
   public static final Args SDT_XSD_TYPE_DEFINITION_NOT_RESOLVABLE = new Args("SDT01006");


   //Participant related
   public static final Args PART_NO_DATA_ASSOCIATED_TO_CONDITIONAL_PERFORMER = new Args("PART01001");
   public static final Args PART_DATA_EXPRESSION_OF_UNSUPPORTED_TYPE = new Args("PART01002");
   public static final Args PART_DATA_REALM_EXPRESSION_OF_UNSUPPORTED_TYPE = new Args("PART01003");
   public static final Args PART_MISSING_ADMINISTRATOR_PARTICIPANT = new Args("PART01004");
   public static final Args PART_ADMINISTRATOR_PARTICIPANT_MUST_BE_A_ROLE = new Args("PART01005");
   public static final Args PART_ADMINISTRATOR_IS_NOT_ALLOWED_TO_HAVE_RELATIONSHIPS_TO_ANY_ORGANIZATION = new Args("PART01006");
   public static final Args PART_SCOPED_PARTICIPANTS_NOT_ALLOWED_FOR_MODEL_LEVEL_GRANTS = new Args("PART01007");
   public static final Args PART_DUPLICATE_ID = new Args("PART01008");
   public static final Args PART_ID_EXCEEDS_MAXIMUM_LENGTH = new Args("PART01009");
   public static final Args PART_ASSOCIATED_ORGANIZATION_SET_FOR_PARTICIPANT_DOES_NOT_EXIST = new Args("PART01010");
   public static final Args PART_DATA_FOR_SCOPED_ORGANIZATION_MUST_EXIST = new Args("PART01011");
   public static final Args PART_DATA_OF_SCOPED_ORGANIZATION_CAN_ONLY_BE_PRIM_OR_STRUCT = new Args("PART01012");
   public static final Args PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL = new Args("PART01013");
   public static final Args PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL_WHEN_SDT_IS_USED = new Args("PART01014");
   public static final Args PART_TYPE_OF_DATA_OF_SCOPED_ORGANIZATION_IS_NOT = new Args("PART01015");
   public static final Args PART_ORGANIZATION_IS_SCOPED_BUT_IN_AUDITTRAIL_UNSCOPED = new Args("PART01016");
   public static final Args PART_ORGANIZATION_IS_UNSCOPED_BUT_IN_AUDITTRAIL_SCOPED = new Args("PART01017");
   public static final Args PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_ID_IN_AUDIT_TRAIL = new Args("PART01018");
   public static final Args PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_PATH_IN_AUDIT_TRAIL = new Args("PART01019");
   public static final Args PART_MULTIPLE_SOPER_ORGANIZATIONS_ARE_NOT_ALLOWED = new Args("PART01020");
   public static final Args PART_MODEL_CONTAINS_DIFFERENT_MANAGER_OF_ASSOCIATION_THAN_DEPLOYED_MODEL = new Args("PART01021");
   public static final Args PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL = new Args("PART01022");

   //Data/Datamapping/Datapath related
   public static final Args DATA_DUPLICATE_ID_FOR_DATA = new Args("DATA01001");
   public static final Args DATA_ID_FOR_DATA_EXCEEDS_MAXIMUM_LENGTH_OF_CHARACTERS = new Args("DATA01002");
   public static final Args DATA_NO_ACTIVITY_SET_FOR_DATAMAPPING = new Args("DATA01003");
   public static final Args DATA_NO_DATA_SET_FOR_DATAMAPPING = new Args("DATA01004");
   public static final Args DATA_NO_USEFUL_ID_SET_FOR_DATAMAPPING = new Args("DATA01005");
   public static final Args DATA_NO_USEFUL_NAME_SET_FOR_DATAMAPPING = new Args("DATA01006");
   public static final Args DATA_DATAMAPPING_HAS_NO_UNIQUE_ID_FOR_DIRECTION = new Args("DATA01007");
   public static final Args DATA_CANNOT_RESOLVE_ACCESSPOINTPROVIDER_FOR_DATAMAPPING = new Args("DATA01008");
   public static final Args DATA_INVALID_CONTEXT_FOR_DATAMAPPING = new Args("DATA01009");
   public static final Args DATA_INVALID_APPLICATION_FOR_DATAMAPPING = new Args("DATA01010");
   public static final Args DATA_INVALID_DATATYPE_FOR_DATAMAPPING = new Args("DATA01011");
   public static final Args DATA_FORMAL_PARAMETER_NOT_RESOLVABLE_FOR_DATAMAPPING = new Args("DATA01012");
   public static final Args DATA_APPLICATION_ACCESS_POINT_NOT_RESOLVABLE_FOR_DATAMAPPING = new Args("DATA01013");
   public static final Args DATA_NO_APPLICATION_ACCESS_POINT_SET_FOR_DATAMAPPING = new Args("DATA01014");
   public static final Args DATA_NO_CONTEXT_SET_FOR_DATAMAPPING = new Args("DATA01015");
   public static final Args DATA_CONTEXT_FOR_DATAMAPPING_UNDEFINED = new Args("DATA01016");
   public static final Args DATA_INVALID_DATAPATH_FOR_DATAMAPPING = new Args("DATA01017");
   public static final Args DATA_DUPLICATE_ID_FOR_DATAPATH = new Args("DATA01018");
   public static final Args DATA_NO_NAME_SPECIFIED_FOR_DATAPATH = new Args("DATA01019");
   public static final Args DATA_NO_DATA_SPECIFIED_FOR_DATAPATH = new Args("DATA01020");
   public static final Args DATA_KEY_DESCRIPTORS_MUST_BE_PRIMITIVE_OR_STRUCTURED_TYPES = new Args("DATA01021");
   public static final Args DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_HAVE_PRIMITIVE_TYPE = new Args("DATA01022");
   public static final Args DATA_NO_SCHEMA_FOUND_FOR_STRUCTURED_KEY_DESCRIPTOR = new Args("DATA01023");
   public static final Args DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_BE_INDEXED_AND_PERSISTENT = new Args("DATA01024");
   public static final Args DATA_DATAPATH_IS_NOT_A_DESCRIPTOR = new Args("DATA01025");
   public static final Args DATA_UNSPECIFIED_TYPE_FOR_PRIMITIVE_DATA = new Args("DATA01026");
   public static final Args DATA_NO_SCHEMA_FOUND_FOR_STRUCTURED_DATA = new Args("DATA01027");
   public static final Args ACCESSPATH_INVALID_FOR_DATAPATH = new Args("DATA01028");

   //Event related
   public static final Args EVEN_DUPLICATE_ID_FOR_EVENT_HANDLER = new Args("EVEN01000");
   public static final Args EVEN_ID_FOR_EVENT_HANDLER_EXCEEDS_MAXIMUM_LENGTH_OF_CHARACTERS = new Args("EVEN01001");
   public static final Args EVEN_HANDLER_DOES_NOT_HAVE_CONDITION_TYPE = new Args("EVEN01002");
   public static final Args EVEN_DUPLICATE_ID_FOR_EVENT_ACTION = new Args("EVEN01003");

   //Plain Java / EJB / Serializable related
   public static final Args JAVA_CLASS_NOT_SPECIFIED = new Args("JAVA01001");
   public static final Args JAVA_COMPLETION_METHOD_NOT_SPECIFIED = new Args("JAVA01002");
   public static final Args JAVA_COULD_NOT_FIND_METHOD_IN_CLASS = new Args("JAVA01003");
   public static final Args JAVA_CONSTRUCTOR_NOT_SPECIFIED = new Args("JAVA01004");
   public static final Args JAVA_COULD_NOT_FIND_CONSTRUCTOR_IN_CLASS = new Args("JAVA01005");
   public static final Args JAVA_CLASS_NOT_FOUND = new Args("JAVA01006");
   public static final Args JAVA_CLASS_COULD_NOT_BE_LOADED = new Args("JAVA01007");
   public static final Args JAVA_EMPTY_CLASSNAME = new Args("JAVA01008");
   public static final Args JAVA_CLASS_HAS_NO_DEFAULT_CONSTRUCTOR = new Args("JAVA01009");
   public static final Args JAVA_CANNOT_LOAD_CLASS = new Args("JAVA01010");
   public static final Args JAVA_INTERFACE_NOT_SPECIFIED = new Args("JAVA01011");
   public static final Args JAVA_METHOD_NOT_SPECIFIED = new Args("JAVA01012");
   public static final Args JAVA_BUSINESS_INTERFACE_NOT_SPECIFIED = new Args("JAVA01013");
   public static final Args JAVA_BEAN_TYPE_NOT_SPECIFIED = new Args("JAVA01014");
   public static final Args JAVA_BEAN_ID_NOT_SPECIFIED = new Args("JAVA01015");

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

   public static class Args extends AbstractErrorFactory
   {
      private Args(String errorCode)
      {
         super(errorCode, BpmValidationErrorMessages.getString(errorCode));
      }

      public BpmValidationError raise(Object... args)
      {
         return buildError(args);
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
         return new BpmValidationError(errorCode, defaultMessage, args == null ? NONE : args);
      }
   }

   static BpmValidationError createError(String id, String defaultMessage, Object arg0)
   {
      return new BpmValidationError(id, defaultMessage, new Object[] {arg0});
   }

}
