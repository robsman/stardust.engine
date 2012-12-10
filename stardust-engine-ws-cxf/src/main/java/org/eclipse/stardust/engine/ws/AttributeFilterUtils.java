/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.core.runtime.beans.ModelAwareQueryPredicate;

public class AttributeFilterUtils
{
   private static final Logger trace = LogManager.getLogger(AttributeFilterUtils.class);

   // WorklistQuery Attributes
   public static final String WORKLIST_QUERY_START_TIME = "startTime";

   public static final String WORKLIST_QUERY_LAST_MODIFICATION_TIME = "lastModificationTime";

   public static final String WORKLIST_QUERY_ACTIVITY_OID = "activityOid";
   
   public static final String WORKLIST_QUERY_ACTIVITY_INSTANCE_OID = "activityInstanceOid";

   public static final String WORKLIST_QUERY_PROCESS_INSTANCE_OID = "processOid";

   public static final String WORKLIST_QUERY_PROCESS_INSTANCE_PRIORITY = "processPriority";

   // ActivityInstanceQuery Attributes
   public static final String ACTIVITY_INSTANCE_QUERY_START_TIME = "startTime";

   public static final String ACTIVITY_INSTANCE_QUERY_LAST_MODIFICATION_TIME = "lastModificationTime";

   public static final String ACTIVITY_INSTANCE_QUERY_ACTIVITY_OID = "activityOid";

   public static final String ACTIVITY_INSTANCE_QUERY_PROCESS_INSTANCE_OID = "processOid";

   public static final String ACTIVITY_INSTANCE_QUERY_PROCESS_INSTANCE_PRIORITY = "processPriority";

   public static final String ACTIVITY_INSTANCE_QUERY_OID = "oid";

   public static final String ACTIVITY_INSTANCE_QUERY_CURRENT_PERFORMER_OID = "currentPerformerOid";

   public static final String ACTIVITY_INSTANCE_QUERY_CURRENT_USER_PERFORMER_OID = "currentUserPerformerOid";

   public static final String ACTIVITY_INSTANCE_QUERY_PERFORMED_BY_OID = "performedByOid";

   public static final String ACTIVITY_INSTANCE_QUERY_STATE = "state";

   // ProcessInstanceQuery Attributes
   public static final String PROCESS_INSTANCE_QUERY_OID = "oid";

   public static final String PROCESS_INSTANCE_QUERY_START_TIME = "startTime";

   public static final String PROCESS_INSTANCE_QUERY_TERMINATION_TIME = "terminationTime";

   public static final String PROCESS_INSTANCE_QUERY_STATE = "state";

   public static final String PROCESS_INSTANCE_QUERY_PROCESS_DEFINITION_OID = "processDefinitionOid";

   public static final String PROCESS_INSTANCE_QUERY_ROOT_PROCESS_INSTANCE_OID = "rootProcessOid";

   public static final String PROCESS_INSTANCE_QUERY_STARTING_USER_OID = "startingUserOid";

   public static final String PROCESS_INSTANCE_QUERY_STARTING_ACTIVITY_INSTANCE_OID = "startingActivityInstanceOid";

   public static final String PROCESS_INSTANCE_QUERY_PRIORITY = "priority";

   // UserQuery Attributes
   public static final String USER_QUERY_OID = "oid";

   public static final String USER_QUERY_ACCOUNT = "accountId";

   public static final String USER_QUERY_FIRST_NAME = "firstName";

   public static final String USER_QUERY_LAST_NAME = "lastName";

   public static final String USER_QUERY_EMAIL = "eMail";

   public static final String USER_QUERY_VALID_FROM = "validFrom";

   public static final String USER_QUERY_VALID_TO = "validTo";

   public static final String USER_QUERY_DESCRIPTION = "description";

   public static final String USER_QUERY_FAILED_LOGIN_COUNT = "failedLoginCount"; // ?

   public static final String USER_QUERY_LAST_LOGIN_TIME = "lastLoginTime"; // ?

   public static final String USER_QUERY_REALM_ID = "realmId";

   // UserGroupQuery Attributes
   public static final String USER_GROUP_QUERY_OID = "oid";

   public static final String USER_GROUP_QUERY_ID = "id";

   public static final String USER_GROUP_QUERY_NAME = "name";

   public static final String USER_GROUP_QUERY_VALID_FROM = "validFrom";

   public static final String USER_GROUP_QUERY_VALID_TO = "validTo";

   public static final String USER_GROUP_QUERY_DESCRIPTION = "description";

   // LogEntryQuery Attributes
   public static final String LOG_ENTRY_QUERY_OID = "oid";

   public static final String LOG_ENTRY_QUERY_TYPE = "type";

   public static final String LOG_ENTRY_QUERY_CODE = "code";

   public static final String LOG_ENTRY_QUERY_SUBJECT = "subject";

   public static final String LOG_ENTRY_QUERY_TIMESTAMP = "timestamp";

   public static final String LOG_ENTRY_QUERY_PROCESS_OID = "processOid";

   public static final String LOG_ENTRY_QUERY_ACTIVITY_OID = "activityOid";

   // ProcessDefinitionQuery Attributes
   public static final String PROCESS_DEFINITION_QUERY_TRIGGER_TYPE = "triggerType";
   
   public static final String PROCESS_DEFINITION_QUERY_MODEL_OID = ModelAwareQueryPredicate.INTERNAL_MODEL_OID_ATTRIBUTE;

   // DocumentQuery Attributes
   public static final String DOCUMENT_QUERY_CONTENT = "content";

   public static final String DOCUMENT_QUERY_CONTENT_TYPE = "contentType";

   public static final String DOCUMENT_QUERY_DATE_CREATED = "dateCreated";

   public static final String DOCUMENT_QUERY_DATE_LAST_MODIFIED = "dateLastModified";

   public static final String DOCUMENT_QUERY_ID = "id";

   public static final String DOCUMENT_QUERY_NAME = "name";

   public static final String DOCUMENT_QUERY_OWNER = "owner";

   public static final String DOCUMENT_QUERY_META_DATA = "documentQuery:metaDataFilter:";

   // DataQuery Attributes
   public static final String DATA_QUERY_PROCESS_ID = "processId";

   public static final String DATA_QUERY_DATA_TYPE_ID = "dataTypeId";

   public static final String DATA_QUERY_DECLARED_TYPE_ID = "declaredTypeId";

   public static final String DATA_QUERY_MODEL_OID = "modelOid";
   
   // Preferences Query
   public static final String PREFERENCES_QUERY_SCOPE = "scope";
   
   public static final String PREFERENCES_QUERY_MODULE_ID = "moduleId";
   
   public static final String PREFERENCES_QUERY_PREFERENCES_ID = "preferencesId";
   
   public static final String PREFERENCES_QUERY_REALM_ID = "realmId";
   
   public static final String PREFERENCES_QUERY_USER_ID = "userId";
   
   // Deployed Model Query OID, ID, STATE, PROVIDER, CONSUMER
   public static final String DEPLOYED_MODEL_QUERY_OID = "oid";
         
   public static final String DEPLOYED_MODEL_QUERY_ID =  "id";
   
   public static final String DEPLOYED_MODEL_QUERY_STATE = "state"; 
   
   public static final String DEPLOYED_MODEL_QUERY_PROVIDER = "provider";
   
   public static final String DEPLOYED_MODEL_QUERY_CONSUMER =  "consumer";

   public static FilterableAttribute unmarshalFilterableAttribute(String attribute,
         Class< ? > clazz)
   {
      if (WorklistQuery.class.equals(clazz))
      {
         if (WORKLIST_QUERY_START_TIME.equals(attribute))
         {
            return WorklistQuery.START_TIME;
         }
         else if (WORKLIST_QUERY_LAST_MODIFICATION_TIME.equals(attribute))
         {
            return WorklistQuery.LAST_MODIFICATION_TIME;
         }
         else if (WORKLIST_QUERY_ACTIVITY_OID.equals(attribute))
         {
            return WorklistQuery.ACTIVITY_OID;
         }
         else if (WORKLIST_QUERY_PROCESS_INSTANCE_OID.equals(attribute))
         {
            return WorklistQuery.PROCESS_INSTANCE_OID;
         }
         else if (WORKLIST_QUERY_PROCESS_INSTANCE_PRIORITY.equals(attribute))
         {
            return WorklistQuery.PROCESS_INSTANCE_PRIORITY;
         }
         else if (WORKLIST_QUERY_ACTIVITY_INSTANCE_OID.equals(attribute))
         {
            return  WorklistQuery.ACTIVITY_INSTANCE_OID;
         }
      }
      if (ActivityInstanceQuery.class.equals(clazz))
      {
         if (ACTIVITY_INSTANCE_QUERY_START_TIME.equals(attribute))
         {
            return ActivityInstanceQuery.START_TIME;
         }
         else if (ACTIVITY_INSTANCE_QUERY_LAST_MODIFICATION_TIME.equals(attribute))
         {
            return ActivityInstanceQuery.LAST_MODIFICATION_TIME;
         }
         else if (ACTIVITY_INSTANCE_QUERY_ACTIVITY_OID.equals(attribute))
         {
            return ActivityInstanceQuery.ACTIVITY_OID;
         }
         else if (ACTIVITY_INSTANCE_QUERY_PROCESS_INSTANCE_OID.equals(attribute))
         {
            return ActivityInstanceQuery.PROCESS_INSTANCE_OID;
         }
         else if (ACTIVITY_INSTANCE_QUERY_PROCESS_INSTANCE_PRIORITY.equals(attribute))
         {
            return ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY;
         }
         else if (ACTIVITY_INSTANCE_QUERY_OID.equals(attribute))
         {
            return ActivityInstanceQuery.OID;
         }
         else if (ACTIVITY_INSTANCE_QUERY_CURRENT_PERFORMER_OID.equals(attribute))
         {
            return ActivityInstanceQuery.CURRENT_PERFORMER_OID;
         }
         else if (ACTIVITY_INSTANCE_QUERY_CURRENT_USER_PERFORMER_OID.equals(attribute))
         {
            return ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID;
         }
         else if (ACTIVITY_INSTANCE_QUERY_PERFORMED_BY_OID.equals(attribute))
         {
            return ActivityInstanceQuery.PERFORMED_BY_OID;
         }
         else if (ACTIVITY_INSTANCE_QUERY_STATE.equals(attribute))
         {
            return ActivityInstanceQuery.STATE;
         }
      }
      if (ProcessInstanceQuery.class.equals(clazz))
      {
         if (PROCESS_INSTANCE_QUERY_OID.equals(attribute))
         {
            return ProcessInstanceQuery.OID;
         }
         if (PROCESS_INSTANCE_QUERY_START_TIME.equals(attribute))
         {
            return ProcessInstanceQuery.START_TIME;
         }
         if (PROCESS_INSTANCE_QUERY_TERMINATION_TIME.equals(attribute))
         {
            return ProcessInstanceQuery.TERMINATION_TIME;
         }
         if (PROCESS_INSTANCE_QUERY_STATE.equals(attribute))
         {
            return ProcessInstanceQuery.STATE;
         }
         if (PROCESS_INSTANCE_QUERY_PROCESS_DEFINITION_OID.equals(attribute))
         {
            return ProcessInstanceQuery.PROCESS_DEFINITION_OID;
         }
         if (PROCESS_INSTANCE_QUERY_ROOT_PROCESS_INSTANCE_OID.equals(attribute))
         {
            return ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID;
         }
         if (PROCESS_INSTANCE_QUERY_STARTING_USER_OID.equals(attribute))
         {
            return ProcessInstanceQuery.STARTING_USER_OID;
         }
         if (PROCESS_INSTANCE_QUERY_STARTING_ACTIVITY_INSTANCE_OID.equals(attribute))
         {
            return ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID;
         }
         if (PROCESS_INSTANCE_QUERY_PRIORITY.equals(attribute))
         {
            return ProcessInstanceQuery.PRIORITY;
         }
      }
      if (UserQuery.class.equals(clazz))
      {
         if (USER_QUERY_ACCOUNT.equals(attribute))
         {
            return UserQuery.ACCOUNT;
         }
         else if (USER_QUERY_DESCRIPTION.equals(attribute))
         {
            return UserQuery.DESCRIPTION;
         }
         else if (USER_QUERY_EMAIL.equals(attribute))
         {
            return UserQuery.EMAIL;
         }
         else if (USER_QUERY_FAILED_LOGIN_COUNT.equals(attribute))
         {
            return UserQuery.FAILED_LOGIN_COUNT;
         }
         else if (USER_QUERY_LAST_LOGIN_TIME.equals(attribute))
         {
            return UserQuery.LAST_LOGIN_TIME;
         }
         else if (USER_QUERY_FIRST_NAME.equals(attribute))
         {
            return UserQuery.FIRST_NAME;
         }
         else if (USER_QUERY_LAST_NAME.equals(attribute))
         {
            return UserQuery.LAST_NAME;
         }
         else if (USER_QUERY_OID.equals(attribute))
         {
            return UserQuery.OID;
         }
         else if (USER_QUERY_REALM_ID.equals(attribute))
         {
            return UserQuery.REALM_ID;
         }
         else if (USER_QUERY_VALID_FROM.equals(attribute))
         {
            return UserQuery.VALID_FROM;
         }
         else if (USER_QUERY_VALID_TO.equals(attribute))
         {
            return UserQuery.VALID_TO;
         }
      }
      if (UserGroupQuery.class.equals(clazz))
      {
         if (USER_GROUP_QUERY_DESCRIPTION.equals(attribute))
         {
            return UserGroupQuery.DESCRIPTION;
         }
         else if (USER_GROUP_QUERY_ID.equals(attribute))
         {
            return UserGroupQuery.ID;
         }
         else if (USER_GROUP_QUERY_NAME.equals(attribute))
         {
            return UserGroupQuery.NAME;
         }
         else if (USER_GROUP_QUERY_OID.equals(attribute))
         {
            return UserGroupQuery.OID;
         }
         else if (USER_GROUP_QUERY_VALID_FROM.equals(attribute))
         {
            return UserGroupQuery.VALID_FROM;
         }
         else if (USER_GROUP_QUERY_VALID_TO.equals(attribute))
         {
            return UserGroupQuery.VALID_TO;
         }
      }
      if (LogEntryQuery.class.equals(clazz))
      {
         if (LOG_ENTRY_QUERY_ACTIVITY_OID.equals(attribute))
         {
            return LogEntryQuery.ACTIVITY_INSTANCE_OID;
         }
         else if (LOG_ENTRY_QUERY_CODE.equals(attribute))
         {
            return LogEntryQuery.CODE;
         }
         else if (LOG_ENTRY_QUERY_OID.equals(attribute))
         {
            return LogEntryQuery.OID;
         }
         else if (LOG_ENTRY_QUERY_PROCESS_OID.equals(attribute))
         {
            return LogEntryQuery.PROCESS_INSTANCE_OID;
         }
         else if (LOG_ENTRY_QUERY_SUBJECT.equals(attribute))
         {
            return LogEntryQuery.SUBJECT;
         }
         else if (LOG_ENTRY_QUERY_TIMESTAMP.equals(attribute))
         {
            return LogEntryQuery.STAMP;
         }
         else if (LOG_ENTRY_QUERY_TYPE.equals(attribute))
         {
            return LogEntryQuery.TYPE;
         }
      }
      if (ProcessDefinitionQuery.class.equals(clazz))
      {
         if (PROCESS_DEFINITION_QUERY_TRIGGER_TYPE.equals(attribute))
         {
            return ProcessDefinitionQuery.TRIGGER_TYPE;
         }
         else if (PROCESS_DEFINITION_QUERY_MODEL_OID.equals(attribute))
         {
        	return (FilterableAttribute) Reflect.getStaticFieldValue(ProcessDefinitionQuery.class, "MODEL_OID");
         }
      }
      if (DocumentQuery.class.equals(clazz))
      {
         if (DOCUMENT_QUERY_CONTENT.equals(attribute))
         {
            return DocumentQuery.CONTENT;
         }
         else if (DOCUMENT_QUERY_CONTENT_TYPE.equals(attribute))
         {
            return DocumentQuery.CONTENT_TYPE;
         }
         else if (DOCUMENT_QUERY_DATE_CREATED.equals(attribute))
         {
            return DocumentQuery.DATE_CREATED;
         }
         else if (DOCUMENT_QUERY_DATE_LAST_MODIFIED.equals(attribute))
         {
            return DocumentQuery.DATE_LAST_MODIFIED;
         }
         else if (DOCUMENT_QUERY_ID.equals(attribute))
         {
            return DocumentQuery.ID;
         }
         else if (DOCUMENT_QUERY_NAME.equals(attribute))
         {
            return DocumentQuery.NAME;
         }
         else if (DOCUMENT_QUERY_OWNER.equals(attribute))
         {
            return DocumentQuery.OWNER;
         }
         else if (attribute.startsWith(DOCUMENT_QUERY_META_DATA))
         {
            if (attribute.equals(DOCUMENT_QUERY_META_DATA + "any"))
            {
               return DocumentQuery.META_DATA.any();
            }
            else
            {
               return DocumentQuery.META_DATA.withName(attribute.substring((DOCUMENT_QUERY_META_DATA + "named:").length()));
            }
         }
      }
      if (DataQuery.class.equals(clazz))
      {
         if (DATA_QUERY_PROCESS_ID.equals(attribute))
         {
            return DataQuery.PROCESS_ID;
         }
         else if (DATA_QUERY_DATA_TYPE_ID.equals(attribute))
         {
            return DataQuery.DATA_TYPE_ID;
         }
         else if (DATA_QUERY_DECLARED_TYPE_ID.equals(attribute))
         {
            return DataQuery.DECLARED_TYPE_ID;
         }
         else if (DATA_QUERY_MODEL_OID.equals(attribute))
         {
            return DataQuery.MODEL_OID;
         }         
      }
      if (PreferenceQuery.class.equals(clazz))
      {
    	  if (PREFERENCES_QUERY_MODULE_ID.equals(attribute))
    	  {
    		  return PreferenceQuery.MODULE_ID;
    	  }
    	  else if (PREFERENCES_QUERY_PREFERENCES_ID.equals(attribute))
    	  {
    		  return PreferenceQuery.PREFERENCES_ID;
    	  }
    	  else if (PREFERENCES_QUERY_REALM_ID.equals(attribute))
    	  {
    		  return PreferenceQuery.REALM_ID;
    	  }
    	  else if (PREFERENCES_QUERY_SCOPE.equals(attribute))
    	  {
    		  return PreferenceQuery.SCOPE;
    	  }
    	  else if (PREFERENCES_QUERY_USER_ID.equals(attribute))
    	  {
    		  return PreferenceQuery.USER_ID;
    	  }
      }
      if (DeployedModelQuery.class.equals(clazz))
      {
         if (DEPLOYED_MODEL_QUERY_OID.equals(attribute))
         {
            return DeployedModelQuery.OID;
         }
         else if (DEPLOYED_MODEL_QUERY_ID.equals(attribute))
         {
            return DeployedModelQuery.ID;
         }
         else if (DEPLOYED_MODEL_QUERY_STATE.equals(attribute))
         {
            return DeployedModelQuery.STATE;
         }
         else if (DEPLOYED_MODEL_QUERY_PROVIDER.equals(attribute))
         {
            return DeployedModelQuery.PROVIDER;
         }
         else if (DEPLOYED_MODEL_QUERY_CONSUMER.equals(attribute))
         {
            return DeployedModelQuery.CONSUMER;
         }
               
      }

      trace.error("FilterableAttribute could not be unmarshaled: Attribute not supported ("
            + attribute + ") for Class " + clazz.getName());
      throw new UnsupportedOperationException(
            "Error unmarshaling FilterableAttribute: Attribute not supported ("
                  + attribute + ") for Class " + clazz.getName());
   }

   public static String marshalFilterableAttribute(String attribute, Class< ? > clazz)
   {
      if (attribute != null)
      {
         if (WorklistQuery.class.equals(clazz))
         {
            if (WorklistQuery.START_TIME.getAttributeName().equals(attribute))
            {
               return WORKLIST_QUERY_START_TIME;
            }
            else if (WorklistQuery.LAST_MODIFICATION_TIME.getAttributeName().equals(
                  attribute))
            {
               return WORKLIST_QUERY_LAST_MODIFICATION_TIME;
            }
            else if (WorklistQuery.ACTIVITY_OID.getAttributeName().equals(attribute))
            {
               return WORKLIST_QUERY_ACTIVITY_OID;
            }
            else if (WorklistQuery.PROCESS_INSTANCE_OID.getAttributeName().equals(
                  attribute))
            {
               return WORKLIST_QUERY_PROCESS_INSTANCE_OID;
            }
            else if (WorklistQuery.PROCESS_INSTANCE_PRIORITY.getAttributeName().equals(
                  attribute))
            {
               return WORKLIST_QUERY_PROCESS_INSTANCE_PRIORITY;
            }
            else if (WorklistQuery.ACTIVITY_INSTANCE_OID.getAttributeName().equals(attribute))
            {
               return WORKLIST_QUERY_ACTIVITY_INSTANCE_OID;
            }
         }
         else if (ActivityInstanceQuery.class.equals(clazz))
         {
            if (ActivityInstanceQuery.START_TIME.getAttributeName().equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_START_TIME;
            }
            else if (ActivityInstanceQuery.LAST_MODIFICATION_TIME.getAttributeName()
                  .equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_LAST_MODIFICATION_TIME;
            }
            else if (ActivityInstanceQuery.ACTIVITY_OID.getAttributeName().equals(
                  attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_ACTIVITY_OID;
            }
            else if (ActivityInstanceQuery.PROCESS_INSTANCE_OID.getAttributeName()
                  .equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_PROCESS_INSTANCE_OID;
            }
            else if (ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.getAttributeName()
                  .equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_PROCESS_INSTANCE_PRIORITY;
            }
            else if (ActivityInstanceQuery.OID.getAttributeName().equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_OID;
            }
            else if (ActivityInstanceQuery.CURRENT_PERFORMER_OID.getAttributeName()
                  .equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_CURRENT_PERFORMER_OID;
            }
            else if (ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID.getAttributeName()
                  .equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_CURRENT_USER_PERFORMER_OID;
            }
            else if (ActivityInstanceQuery.PERFORMED_BY_OID.getAttributeName().equals(
                  attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_PERFORMED_BY_OID;
            }
            else if (ActivityInstanceQuery.STATE.getAttributeName().equals(attribute))
            {
               return ACTIVITY_INSTANCE_QUERY_STATE;
            }
         }
         else if (ProcessInstanceQuery.class.equals(clazz))
         {
            if (ProcessInstanceQuery.OID.getAttributeName().equals(attribute))
            {
               return PROCESS_INSTANCE_QUERY_OID;
            }
            if (ProcessInstanceQuery.START_TIME.getAttributeName().equals(attribute))
            {
               return PROCESS_INSTANCE_QUERY_START_TIME;
            }
            if (ProcessInstanceQuery.TERMINATION_TIME.getAttributeName()
                  .equals(attribute))
            {
               return PROCESS_INSTANCE_QUERY_TERMINATION_TIME;
            }
            if (ProcessInstanceQuery.STATE.getAttributeName().equals(attribute))
            {
               return PROCESS_INSTANCE_QUERY_STATE;
            }
            if (ProcessInstanceQuery.PROCESS_DEFINITION_OID.getAttributeName().equals(
                  attribute))
            {
               return PROCESS_INSTANCE_QUERY_PROCESS_DEFINITION_OID;
            }
            if (ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.getAttributeName().equals(
                  attribute))
            {
               return PROCESS_INSTANCE_QUERY_ROOT_PROCESS_INSTANCE_OID;
            }
            if (ProcessInstanceQuery.STARTING_USER_OID.getAttributeName().equals(
                  attribute))
            {
               return PROCESS_INSTANCE_QUERY_STARTING_USER_OID;
            }
            if (ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID.getAttributeName()
                  .equals(attribute))
            {
               return PROCESS_INSTANCE_QUERY_STARTING_ACTIVITY_INSTANCE_OID;
            }
            if (ProcessInstanceQuery.PRIORITY.getAttributeName().equals(attribute))
            {
               return PROCESS_INSTANCE_QUERY_PRIORITY;
            }
         }
         else if (UserQuery.class.equals(clazz))
         {
            if (UserQuery.ACCOUNT.getAttributeName().equals(attribute))
            {
               return USER_QUERY_ACCOUNT;
            }
            else if (UserQuery.DESCRIPTION.getAttributeName().equals(attribute))
            {
               return USER_QUERY_DESCRIPTION;
            }
            else if (UserQuery.EMAIL.getAttributeName().equals(attribute))
            {
               return USER_QUERY_EMAIL;
            }
            else if (UserQuery.FAILED_LOGIN_COUNT.getAttributeName().equals(attribute))
            {
               return USER_QUERY_FAILED_LOGIN_COUNT;
            }
            else if (UserQuery.LAST_LOGIN_TIME.getAttributeName().equals(attribute))
            {
               return USER_QUERY_LAST_LOGIN_TIME;
            }
            else if (UserQuery.FIRST_NAME.getAttributeName().equals(attribute))
            {
               return USER_QUERY_FIRST_NAME;
            }
            else if (UserQuery.LAST_NAME.getAttributeName().equals(attribute))
            {
               return USER_QUERY_LAST_NAME;
            }
            else if (UserQuery.OID.getAttributeName().equals(attribute))
            {
               return USER_QUERY_OID;
            }
            else if (UserQuery.REALM_ID.getAttributeName().equals(attribute))
            {
               return USER_QUERY_REALM_ID;
            }
            else if (UserQuery.VALID_FROM.getAttributeName().equals(attribute))
            {
               return USER_QUERY_VALID_FROM;
            }
            else if (UserQuery.VALID_TO.getAttributeName().equals(attribute))
            {
               return USER_QUERY_VALID_TO;
            }
         }
         else if (UserGroupQuery.class.equals(clazz))
         {
            if (UserGroupQuery.DESCRIPTION.getAttributeName().equals(attribute))
            {
               return USER_GROUP_QUERY_DESCRIPTION;
            }
            else if (UserGroupQuery.ID.getAttributeName().equals(attribute))
            {
               return USER_GROUP_QUERY_ID;
            }
            else if (UserGroupQuery.NAME.getAttributeName().equals(attribute))
            {
               return USER_GROUP_QUERY_NAME;
            }
            else if (UserGroupQuery.OID.getAttributeName().equals(attribute))
            {
               return USER_GROUP_QUERY_OID;
            }
            else if (UserGroupQuery.VALID_FROM.getAttributeName().equals(attribute))
            {
               return USER_GROUP_QUERY_VALID_FROM;
            }
            else if (UserGroupQuery.VALID_TO.getAttributeName().equals(attribute))
            {
               return USER_GROUP_QUERY_VALID_TO;
            }
         }
         else if (LogEntryQuery.class.equals(clazz))
         {
            if (LogEntryQuery.ACTIVITY_INSTANCE_OID.getAttributeName().equals(attribute))
            {
               return LOG_ENTRY_QUERY_ACTIVITY_OID;
            }
            else if (LogEntryQuery.CODE.getAttributeName().equals(attribute))
            {
               return LOG_ENTRY_QUERY_CODE;
            }
            else if (LogEntryQuery.OID.getAttributeName().equals(attribute))
            {
               return LOG_ENTRY_QUERY_OID;
            }
            else if (LogEntryQuery.PROCESS_INSTANCE_OID.getAttributeName().equals(
                  attribute))
            {
               return LOG_ENTRY_QUERY_PROCESS_OID;
            }
            else if (LogEntryQuery.STAMP.getAttributeName().equals(attribute))
            {
               return LOG_ENTRY_QUERY_TIMESTAMP;
            }
            else if (LogEntryQuery.SUBJECT.getAttributeName().equals(attribute))
            {
               return LOG_ENTRY_QUERY_SUBJECT;
            }
            else if (LogEntryQuery.TYPE.getAttributeName().equals(attribute))
            {
               return LOG_ENTRY_QUERY_TYPE;
            }
         }
         if (ProcessDefinitionQuery.class.equals(clazz))
         {
            if (ProcessDefinitionQuery.TRIGGER_TYPE.getAttributeName().equals(attribute))
            {
               return PROCESS_DEFINITION_QUERY_TRIGGER_TYPE;
            }
            if (((FilterableAttribute) Reflect.getStaticFieldValue(ProcessDefinitionQuery.class, "MODEL_OID")).getAttributeName().equals(attribute))
            {
            	return PROCESS_DEFINITION_QUERY_MODEL_OID;
            }
         }
         if (DataQuery.class.equals(clazz))
         {
            if (DataQuery.PROCESS_ID.getAttributeName().equals(attribute))
            {
               return DATA_QUERY_PROCESS_ID;
            }
            else if (DataQuery.DATA_TYPE_ID.getAttributeName().equals(attribute))
            {
               return DATA_QUERY_DATA_TYPE_ID;
            }
            else if (DataQuery.DECLARED_TYPE_ID.getAttributeName().equals(attribute))
            {
               return DATA_QUERY_DECLARED_TYPE_ID;
            }
            else if (DataQuery.MODEL_OID.getAttributeName().equals(attribute))
            {
               return DATA_QUERY_MODEL_OID;
            }
         }
         if (PreferenceQuery.class.equals(clazz))
         {
            if (PreferenceQuery.MODULE_ID.getAttributeName().equals(attribute))
            {
               return PREFERENCES_QUERY_MODULE_ID;
            }
            else if (PreferenceQuery.PREFERENCES_ID.getAttributeName().equals(attribute))
            {
               return PREFERENCES_QUERY_PREFERENCES_ID;
            }
            else if (PreferenceQuery.REALM_ID.getAttributeName().equals(attribute))
            {
               return PREFERENCES_QUERY_REALM_ID;
            }
            else if (PreferenceQuery.SCOPE.getAttributeName().equals(attribute))
            {
               return PREFERENCES_QUERY_SCOPE;
            }
            else if (PreferenceQuery.USER_ID.getAttributeName().equals(attribute))
            {
               return PREFERENCES_QUERY_USER_ID;
            }
         }
         if (DocumentQuery.class.equals(clazz))
         {
            if (DocumentQuery.CONTENT.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_CONTENT;
            }
            else if (DocumentQuery.CONTENT_TYPE.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_CONTENT_TYPE;
            }
            else if (DocumentQuery.DATE_CREATED.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_DATE_CREATED;
            }
            else if (DocumentQuery.DATE_LAST_MODIFIED.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_DATE_LAST_MODIFIED;
            }
            else if (DocumentQuery.ID.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_ID;
            }
            else if (DocumentQuery.NAME.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_NAME;
            }
            else if (DocumentQuery.OWNER.getAttributeName().equals(attribute))
            {
               return DOCUMENT_QUERY_OWNER;
            }
            else if (attribute.startsWith(DOCUMENT_QUERY_META_DATA))
            {
               return attribute;
            }
         }
         if (DeployedModelQuery.class.equals(clazz))
         {
            if (DeployedModelQuery.OID.getAttributeName().equals(attribute))
            {
               return DEPLOYED_MODEL_QUERY_OID;
            }
            else if (DeployedModelQuery.ID.getAttributeName().equals(attribute))
            {
               return DEPLOYED_MODEL_QUERY_ID;
            }
            else if (DeployedModelQuery.STATE.getAttributeName().equals(attribute))
            {
               return DEPLOYED_MODEL_QUERY_STATE;
            }
            else if (DeployedModelQuery.PROVIDER.getAttributeName().equals(attribute))
            {
               return DEPLOYED_MODEL_QUERY_PROVIDER;
            }
            else if (DeployedModelQuery.CONSUMER.getAttributeName().equals(attribute))
            {
               return DEPLOYED_MODEL_QUERY_CONSUMER;
            }
         }
         trace.error("FilterableAttribute could not be marshaled: Attribute not supported ("
               + attribute + ") for Class" + clazz.getName());
         throw new UnsupportedOperationException(
               "Error marshaling FilterableAttribute: Attribute not supported ("
                     + attribute + ") for Class" + clazz.getName());
      }
      else
      {
         return null;
      }
   }
}
