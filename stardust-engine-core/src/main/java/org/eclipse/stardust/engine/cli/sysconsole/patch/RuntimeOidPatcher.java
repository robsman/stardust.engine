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
package org.eclipse.stardust.engine.cli.sysconsole.patch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session.RuntimeDmlManagerProvider;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry.ElementType;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

public class RuntimeOidPatcher
{
   private static final String KEY_GLOBAL_DML_MANAGER_CACHE_PREFIX = RuntimeDmlManagerProvider.class.getName()
      + ".GlobalCache.";

   private static final String KEY_AUDIT_TRAIL_DML_MANAGER_CACHE = KEY_GLOBAL_DML_MANAGER_CACHE_PREFIX
      + SessionProperties.DS_NAME_AUDIT_TRAIL;
   
   
   private static final Logger trace = LogManager.getLogger(RuntimeOidPatcher.class);

   private SqlUtils sqlUtils;

   private Session session;

   private final boolean logOnly;

   private final boolean noLog;

   public RuntimeOidPatcher(boolean logOnly, boolean noLog)
   {
      this.logOnly = logOnly;
      this.noLog = noLog;
    
   }
   
   private Session obtainSession() throws SQLException
   {
      String schemaKey = SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX;
      String archiveSchema = Parameters.instance().getString(schemaKey); 
      if(StringUtils.isEmpty(archiveSchema))
      {
         throw new RuntimeException("Archive schema not specified");
      }
      
      Session s = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      // obtain connection to test session validity
      s.getConnection();

      return s;
   }
   
   public void patch()
   {
      boolean layerPushed = false;
      boolean globalPushed = false;
      Object dmlManagerCache = null; 
      
      try
      {
         //cleanup dml manager cache from any previous run (for example when this command after the archive command)
         //do the persistent framework don't messes up - only setting the session is not enough!
         GlobalParameters globalParameters = GlobalParameters.globals();
         dmlManagerCache = globalParameters.get(KEY_AUDIT_TRAIL_DML_MANAGER_CACHE);
         globalParameters.set(KEY_AUDIT_TRAIL_DML_MANAGER_CACHE, null);
         globalPushed = true;
         
         session = obtainSession();

         //push session to parameters so its available in the persistence framework
         Map layerProps = new HashMap();       
         layerProps.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);
         ParametersFacade.pushLayer(layerProps);
         layerPushed =true;
         
         sqlUtils = new SqlUtils(session.getSchemaName(), session.getDBDescriptor());
         //do the actual patching
         doPatch();
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         if(layerPushed)
         {
            ParametersFacade.popLayer();  
         }
         
         if(globalPushed)
         {
            GlobalParameters globalParameters = GlobalParameters.globals();
            globalParameters.set(KEY_AUDIT_TRAIL_DML_MANAGER_CACHE, dmlManagerCache);
         }
      }
   }
   
   
   private void doPatch()
   {      
      //get the available partition oids
      Statement stmt = null;
      ResultSet rsPartition = null;
      List<Short> partitionOids;
      try
      {
         DBDescriptor dbDescriptor = session.getDBDescriptor();
         stmt = session.getConnection().createStatement();
         partitionOids = new ArrayList<Short>();
         
         rsPartition = stmt.executeQuery("SELECT " + IdentifiablePersistentBean.FIELD__OID
               + "  FROM " + getQualifiedName(session.getSchemaName(), dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)));
         while(rsPartition.next())
         {
            partitionOids.add(rsPartition.getShort(1));
         }
      }
      catch (SQLException sqle)
      {
         session.closeAndClearPersistenceControllers();
         
         final String message = "Failed resolving partition oids";
         throw new PublicException(message, sqle);
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(stmt, rsPartition);
      }
      
      Map<Short, Map<ElementType, List<RuntimeOidPatch>>> patchesByPartitionOid
         = new HashMap<Short, Map<ElementType,List<RuntimeOidPatch>>>();
      
      //find invalid runtime oids
      for (Short partitionOid : partitionOids) {
         RuntimeOidRegistry rtOidRegistry = new RuntimeOidRegistry(partitionOid);
         PatchAwareRuntimeModelLoader loader = new PatchAwareRuntimeModelLoader(partitionOid);
         loader.loadRuntimeOidRegistry(rtOidRegistry);
         Map<ElementType, List<RuntimeOidPatch>> runtimeOidPatches = loader.getRuntimeOidPatches();
         if (!runtimeOidPatches.isEmpty()) 
         {
            patchesByPartitionOid.put(partitionOid, runtimeOidPatches);
         }

      }
      
      if(!noLog)
      {
         //log what the program is about to do
         logOperations(patchesByPartitionOid);  
      }
      
      if(!logOnly)
      {
        List<String> allUpdateStatements = new ArrayList<String>();
          //correct the invalid runtime oids and all depending tables pointing to them
          try
          {
             allUpdateStatements = getAllUpdateStatements(patchesByPartitionOid);
             if(!allUpdateStatements.isEmpty())
             {
                stmt = session.getConnection().createStatement();
                for(String updateStatement: allUpdateStatements)
                {
                   stmt.addBatch(updateStatement);  
                }
                
                stmt.executeBatch();
                session.save(true);
             }
          }
          catch(BatchUpdateException e)
          {
             List<String> failedStatements = new ArrayList<String>();
             int[] updateInfo = e.getUpdateCounts();
             for(int i = 0; i <updateInfo.length; i++)
             {
                int state = updateInfo[i];
                if(state == Statement.EXECUTE_FAILED)
                {
                   failedStatements.add(allUpdateStatements.get(i));
                }
             }
             
             trace.error("Exeception during batch update, the following statements failed");
             for(String failedStatement: failedStatements)
             {
                trace.error(failedStatement);
             }
             
             session.rollback(true);
          }
          catch (SQLException sqle)
          {         
             session.rollback(true);
             final String message = "Failed patching archive "+sqle.getMessage();
             throw new PublicException(message);
          }
          finally
          {
             QueryUtils.closeStatementAndResultSet(stmt, rsPartition);
          }
      }
   }
   
   private List<String> getUpdateStatements(ElementType type, List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      if(type.equals(IRuntimeOidRegistry.PARTICIPANT))
      {
         updateStatements = getParticipantUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.DATA))
      {
         updateStatements = getDataUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.PROCESS))
      {
         updateStatements = getProcessDefinitionUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.TRIGGER))
      {
         updateStatements = getTriggerUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.ACTIVITY))
      {
         updateStatements = getActivityUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.TRANSITION))
      {
         updateStatements = getTransitionUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.EVENT_HANDLER))
      {
         updateStatements = getEventHandlerUpdates(patchesForType);
      }
      else if(type.equals(IRuntimeOidRegistry.STRUCTURED_DATA_XPATH))
      {
         updateStatements = getStructuredDataUpdates(patchesForType);
      }
      else
      {
         throw new RuntimeException("Unsupported Runtime Oid Type: "+type);
      }
      
      return updateStatements;
   }
   
   private List<String> getStructuredDataUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      {
         //update reference from structured_data_value
         String structuredDataValueRef = getUpdateStatement(patch, StructuredDataValueBean.class,
               null, StructuredDataValueBean.FR__XPATH);   
          
         //update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               StructuredDataBean.class, StructuredDataBean.FR__MODEL,
               StructuredDataBean.FR__OID);
         
         updateStatements.add(structuredDataValueRef);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements; 
   }
   
   private List<String> getDataUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      {
         //update reference from data_value
         String dataValueRef = getUpdateStatement(patch, DataValueBean.class,
               DataValueBean.FR__MODEL, DataValueBean.FR__DATA);   
         
         //update reference from structured_data
         String structuredDataRef = getUpdateStatement(patch, StructuredDataBean.class,
               StructuredDataBean.FR__MODEL, StructuredDataBean.FR__DATA);
         
         //update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailDataBean.class, AuditTrailDataBean.FR__MODEL,
               AuditTrailDataBean.FR__OID);
         
         updateStatements.add(dataValueRef);
         updateStatements.add(structuredDataRef);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements; 
   }
   
   private List<String> getEventHandlerUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      {
         //update references from event_binding table
         String eventBindingRef = getUpdateStatement(patch,
               EventBindingBean.class, EventBindingBean.FR__MODEL,
               EventBindingBean.FR__HANDLER_OID);
                  
         //update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailEventHandlerBean.class, AuditTrailEventHandlerBean.FR__MODEL,
               AuditTrailEventHandlerBean.FR__OID);
         
         updateStatements.add(eventBindingRef);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements; 
   }
   
   private List<String> getTransitionUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      {
         //update references from trans_inst table
         String transitionInstanceRef = getUpdateStatement(patch,
               TransitionInstanceBean.class, TransitionInstanceBean.FR__MODEL,
               TransitionInstanceBean.FR__TRANSITION);
         
         //update references from trans_token table
         String transitionTokenRef = getUpdateStatement(patch,
               TransitionTokenBean.class, TransitionTokenBean.FR__MODEL,
               TransitionTokenBean.FR__TRANSITION);
         
         //update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailTransitionBean.class, AuditTrailTransitionBean.FR__MODEL,
               AuditTrailTransitionBean.FR__OID);
         
         updateStatements.add(transitionInstanceRef);
         updateStatements.add(transitionTokenRef);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements; 
   }
   
   private List<String> getTriggerUpdates(List<RuntimeOidPatch> patchesForType)
   {
      
      List<String> updateStatements = new ArrayList<String>();
      
      //no fks are pointing to process_trigger table, only fix runtime oid itself
      for(RuntimeOidPatch patch: patchesForType)
      {
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailTriggerBean.class, AuditTrailTriggerBean.FR__MODEL,
               AuditTrailTriggerBean.FR__OID);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;   
   }
   
   private List<String> getParticipantUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      {
         // update references from act_inst_history table
         String aihPerformerRefBase = getUpdateStatement(patch,
               ActivityInstanceHistoryBean.class, null,
               ActivityInstanceHistoryBean.FR__PERFORMER);
         String aihPerformerRefParticipantCondition = getParticipantCondition(ActivityInstanceHistoryBean.FR__PERFORMER_KIND);
         StringBuffer aihPerformerRefBuffer = new StringBuffer();
         aihPerformerRefBuffer.append(aihPerformerRefBase);
         aihPerformerRefBuffer.append(" ");
         aihPerformerRefBuffer.append(aihPerformerRefParticipantCondition); 

         // update references from act_inst_history table
         String aihOnBehalfOfRefBase = getUpdateStatement(patch,
               ActivityInstanceHistoryBean.class, null,
               ActivityInstanceHistoryBean.FR__ON_BEHALF_OF);
         String aihOnBehalfOfRefCondition = getParticipantCondition(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND);
         StringBuffer aihOnBehalfRefBuffer = new StringBuffer();
         aihOnBehalfRefBuffer.append(aihOnBehalfOfRefBase);
         aihOnBehalfRefBuffer.append(" ");
         aihOnBehalfRefBuffer.append(aihOnBehalfOfRefCondition);
         
         String workItemRefBase = getUpdateStatement(patch, WorkItemBean.class,
               WorkItemBean.FR__MODEL, WorkItemBean.FR__PERFORMER);
         String workItemPerformerCondition = getParticipantCondition(WorkItemBean.FR__PERFORMER_KIND);
         StringBuffer workItemRefBuffer = new StringBuffer();
         workItemRefBuffer.append(workItemRefBase);
         workItemRefBuffer.append(" ");
         workItemRefBuffer.append(workItemPerformerCondition);
         
         // update references from activity_inst_log
         String aiLogRef = getUpdateStatement(patch, ActivityInstanceLogBean.class,
               ActivityInstanceLogBean.FR__MODEL, ActivityInstanceLogBean.FR__PARTICIPANT);
          
         //update references from activity instance
         String aiRef = getUpdateStatement(patch, ActivityInstanceBean.class,
               ActivityInstanceBean.FR__MODEL, ActivityInstanceBean.FR__CURRENT_PERFORMER); 
         
         // update references from user_participant
         throw new UnsupportedOperationException("CRNT-26260: Merge needs to be fixed");
//         String uplRef = getUpdateStatement(patch, UserParticipantLink.class,
//               UserParticipantLink.FR__MODEL, UserParticipantLink.FR__PARTICIPANT);
//                 
//         // update references from department
//         String departmentRef = getUpdateStatement(patch, DepartmentBean.class,
//               null, DepartmentBean.FR__ORGANIZATION);
//
//         //update the runtime oids
//         String fixRuntimeOidStatement = getUpdateStatement(patch,
//               AuditTrailParticipantBean.class, AuditTrailParticipantBean.FR__MODEL,
//               AuditTrailParticipantBean.FR__OID);
//       
//         updateStatements.add(aihPerformerRefBuffer.toString());
//         updateStatements.add(aihOnBehalfRefBuffer.toString());
//         updateStatements.add(workItemRefBuffer.toString());
//         updateStatements.add(aiLogRef);
//         updateStatements.add(aiRef);
//         updateStatements.add(uplRef);
//         updateStatements.add(departmentRef);
//         updateStatements.add(fixRuntimeOidStatement);
         
      }
      return updateStatements;
   }
   
   private List<String> getActivityUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      { 
         //update references from activity_instance table
         String referencingActivityInstances
            = getUpdateStatement(patch, 
                  ActivityInstanceBean.class, 
                  ActivityInstanceBean.FR__MODEL, 
                  ActivityInstanceBean.FR__ACTIVITY);
         
         //update references from event_handler table
         String referencingEventHandler
            = getUpdateStatement(patch, 
                  AuditTrailEventHandlerBean.class, 
                  AuditTrailEventHandlerBean.FR__MODEL, 
                  AuditTrailEventHandlerBean.FR__ACTIVITY);
         
         //update reference from work_item table
         String referencingWorkItems
            = getUpdateStatement(patch, 
                  WorkItemBean.class, 
                  WorkItemBean.FR__MODEL, 
                  WorkItemBean.FR__ACTIVITY);
         
         //update reference from transition table
         String referencingSourceTransition
            = getUpdateStatement(patch, 
                  AuditTrailTransitionBean.class, 
                  AuditTrailTransitionBean.FR__MODEL, 
                  AuditTrailTransitionBean.FR__SRC_ACTIVITY);
         
         //update reference from transition table
         String referencingTargetTransition
            = getUpdateStatement(patch, 
                  AuditTrailTransitionBean.class, 
                  AuditTrailTransitionBean.FR__MODEL, 
                  AuditTrailTransitionBean.FR__TGT_ACTIVITY);
         
         //update activity table itself
         String fixRuntimeOidStatement
            = getUpdateStatement(patch, 
                  AuditTrailActivityBean.class, 
                  AuditTrailActivityBean.FR__MODEL, 
                  AuditTrailActivityBean.FR__OID);
         
         updateStatements.add(referencingActivityInstances);
         updateStatements.add(referencingEventHandler);
         updateStatements.add(referencingWorkItems);
         updateStatements.add(referencingSourceTransition);
         updateStatements.add(referencingTargetTransition); 
         updateStatements.add(fixRuntimeOidStatement);
      }
      
      return updateStatements;
   }
   
   private List<String> getProcessDefinitionUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for(RuntimeOidPatch patch: patchesForType)
      {                   
         //update references from activity_table
         String referencingActivities
            = getUpdateStatement(patch, 
                  AuditTrailActivityBean.class, 
                  AuditTrailActivityBean.FR__MODEL, 
                  AuditTrailActivityBean.FR__PROCESS_DEFINITION);
         
         //update references from event_handler table
         String referencingEventHandler
            = getUpdateStatement(patch, 
                  AuditTrailEventHandlerBean.class, 
                  AuditTrailEventHandlerBean.FR__MODEL, 
                  AuditTrailEventHandlerBean.FR__PROCESS_DEFINITION);
         
         //update references from process_instance table
         String referencingProcessInstances
            = getUpdateStatement(patch, 
                  ProcessInstanceBean.class, 
                  ProcessInstanceBean.FR__MODEL, 
                  ProcessInstanceBean.FR__PROCESS_DEFINITION);
         
         //update references from process_trigger table
         String referencingProcessTriggers
            = getUpdateStatement(patch, 
               AuditTrailTriggerBean.class, 
               AuditTrailTriggerBean.FR__MODEL, 
               AuditTrailTriggerBean.FR__PROCESS_DEFINITION);
         
         //update references from transition table
         String referencingTransitions
            = getUpdateStatement(patch, 
                  AuditTrailTransitionBean.class, 
                  AuditTrailTransitionBean.FR__MODEL, 
                  AuditTrailTransitionBean.FR__PROCESS_DEFINITION);
         
         //update the process definition table itself
         String fixRuntimeOidStatement
            = getUpdateStatement(patch, 
                  AuditTrailProcessDefinitionBean.class, 
                  AuditTrailProcessDefinitionBean.FR__MODEL, 
                  AuditTrailProcessDefinitionBean.FR__OID);
         
         updateStatements.add(referencingActivities);
         updateStatements.add(referencingEventHandler);
         updateStatements.add(referencingProcessInstances);
         updateStatements.add(referencingProcessTriggers);
         updateStatements.add(referencingTransitions);
         updateStatements.add(fixRuntimeOidStatement);
      }
      
      return updateStatements;
   }
      
   private String getUpdateStatement(RuntimeOidPatch patch, Class<? extends Persistent> persistentClass, FieldRef modelFkField, FieldRef updateField)
   {
      TypeDescriptorRegistry registry = TypeDescriptorRegistry.current();
      TypeDescriptor typeDescriptor = registry.getDescriptor(persistentClass);
     
      StringBuffer buffer = new StringBuffer();
      buffer.append("UPDATE ");
      sqlUtils.appendTableRef(buffer, typeDescriptor, true);
      buffer.append(" SET "); 
      sqlUtils.appendFieldRef(buffer, updateField); 
      buffer.append(" = ");
      buffer.append(patch.getFixedRuntimeOid());
      buffer.append(" WHERE ");
      sqlUtils.appendFieldRef(buffer, updateField); 
      buffer.append(" = ");
      buffer.append(patch.getIncorrectRuntimeOid());
      buffer.append(getPartitionCondition(patch.getPartitionOid(), persistentClass, modelFkField));
      return buffer.toString();
   }

   private String getPartitionCondition(short partitionOid,
         Class<? extends Persistent> persistentClass, FieldRef modelFkField) {      
      TypeDescriptorRegistry registry = TypeDescriptorRegistry.current();
      TypeDescriptor modelDescriptor = registry.getDescriptor(ModelPersistorBean.class);
      
      String result = "";
      // act_inst_history table has no model fk, join a table which has one
      if (persistentClass.equals(ActivityInstanceHistoryBean.class)) {
         
         TypeDescriptor activityInstanceDescriptor = registry
               .getDescriptor(ActivityInstanceBean.class);

         StringBuffer buffer = new StringBuffer();
         buffer.append(" AND ");
         sqlUtils.appendFieldRef(buffer, ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE);
         buffer.append(" IN(");
         buffer.append("SELECT ");
         sqlUtils.appendFieldRef(buffer, ActivityInstanceBean.FR__OID);
         buffer.append(" FROM "); 
         sqlUtils.appendTableRef(buffer, activityInstanceDescriptor, true);
         buffer.append(" JOIN ");
         sqlUtils.appendTableRef(buffer, modelDescriptor, true);
         buffer.append(" ON(");
         sqlUtils.appendFieldRef(buffer, ActivityInstanceBean.FR__MODEL);
         buffer.append(" = ");
         sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__OID);
         buffer.append(" AND ");
         sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__PARTITION);
         buffer.append(" = ");
         buffer.append(partitionOid);
         buffer.append(")) ");

         result = buffer.toString();
      }   
      //department table has no model fk, but a partition fk, use it directly
      //in the where clause
      else if(persistentClass.equals(DepartmentBean.class))
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append(" AND ");
         sqlUtils.appendFieldRef(buffer, DepartmentBean.FR__PARTITION);
         buffer.append(" = ");
         buffer.append(partitionOid);
         
         result = buffer.toString();
      }
      
      
      // structured_data_value has no model fk - join process_instance which
      // has one
      else if (persistentClass.equals(StructuredDataValueBean.class)) {
         TypeDescriptor processInstanceDescriptor = registry
               .getDescriptor(ProcessInstanceBean.class);

         StringBuffer buffer = new StringBuffer();
         buffer.append(" AND ");
         sqlUtils.appendFieldRef(buffer, StructuredDataValueBean.FR__PROCESS_INSTANCE);
         buffer.append(" IN(");
         buffer.append("SELECT ");
         sqlUtils.appendFieldRef(buffer, ProcessInstanceBean.FR__OID);
         buffer.append(" FROM "); 
         sqlUtils.appendTableRef(buffer, processInstanceDescriptor, true);
         buffer.append(" JOIN ");
         sqlUtils.appendTableRef(buffer, modelDescriptor, true);
         buffer.append(" ON(");
         sqlUtils.appendFieldRef(buffer, ProcessInstanceBean.FR__MODEL);
         buffer.append(" = ");
         sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__OID);
         buffer.append(" AND ");
         sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__PARTITION);
         buffer.append(" = ");
         buffer.append(partitionOid);
         buffer.append(")) ");
         
         result = buffer.toString();
      } 
      else {
         result = getPartitionCondition(partitionOid, modelFkField);
      }
      
      return result;
   }
   
   private String getPartitionCondition(short partitionOid,
         FieldRef modelFkField) {
      if (modelFkField == null) {
         throw new RuntimeException(
               "Foreign key field for joining model(and partition) is null but must be provided");
      }

      // join class dont supports multiple join condition in on clause - do it
      // yourself
      TypeDescriptorRegistry registry = TypeDescriptorRegistry.current();
      TypeDescriptor typeDescriptor = registry
            .getDescriptor(ModelPersistorBean.class);

      StringBuffer buffer = new StringBuffer();
      buffer.append(" AND ");
      sqlUtils.appendFieldRef(buffer, modelFkField);
      buffer.append(" IN(");
      buffer.append("SELECT ");
      sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__OID);
      buffer.append(" FROM ");
      sqlUtils.appendTableRef(buffer, typeDescriptor, true);
      buffer.append(" WHERE ");
      sqlUtils.appendFieldRef(buffer, modelFkField);
      buffer.append(" = ");
      sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__OID);
      buffer.append(" AND ");
      sqlUtils.appendFieldRef(buffer, ModelPersistorBean.FR__PARTITION);
      buffer.append(" = ");
      buffer.append(partitionOid);
      buffer.append(")");

      return buffer.toString();
   }
   
   private String getParticipantCondition(FieldRef participantTypeField)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("AND ");
      sqlUtils.appendFieldRef(buffer, participantTypeField);
      buffer.append(" = ");
      buffer.append(PerformerType.MODEL_PARTICIPANT);
      return buffer.toString();
   }

   private static String getQualifiedName(String qualifier, String objectName)
   {
      StringBuffer result = new StringBuffer(100);
      if ( !StringUtils.isEmpty(qualifier))
      {
         result.append(qualifier).append(".");
      }
      result.append(objectName);

      return result.toString();
   }
   
   private List<String> getAllUpdateStatements(Map<Short, Map<ElementType, List<RuntimeOidPatch>>> patchesByPartitionOid)
   {
      List<String> allUpdateStatements = new ArrayList<String>();
      for(Short partitionOid: patchesByPartitionOid.keySet())
      {   
         Map<ElementType, List<RuntimeOidPatch>> patchesByType
            = patchesByPartitionOid.get(partitionOid);
         for(ElementType type: patchesByType.keySet())
         {
            List<RuntimeOidPatch> patchesForType = patchesByType.get(type);
            allUpdateStatements.addAll(getUpdateStatements(type, patchesForType));
         }
      }
      
      return allUpdateStatements;
   }
   
   private void logOperations(Map<Short, Map<ElementType, List<RuntimeOidPatch>>> patchesByPartitionOid)
   {
      try
      {  
         String fileName = "fix_runtime_oid_log_"+System.currentTimeMillis();
         String path = "./"+fileName;
         BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
         for(Short partitionOid: patchesByPartitionOid.keySet())
         {            
            writer.write("patches for partition: ");
            writer.write(partitionOid.toString());
            writer.newLine(); 
            
            Map<ElementType, List<RuntimeOidPatch>> patchesByType
               = patchesByPartitionOid.get(partitionOid);
            for(ElementType type: patchesByType.keySet())
            {
               List<RuntimeOidPatch> oidPatches = patchesByType.get(type); 
               writer.write("patching type: ");
               writer.write(type.toString());
               writer.newLine();
               for(RuntimeOidPatch patch: oidPatches)
               {
                  writer.write("changing oid from: "+patch.getIncorrectRuntimeOid());
                  writer.write(" -> to ");
                  writer.write(new Long(patch.getFixedRuntimeOid()).toString());
                  writer.newLine();
               }
               
               writer.newLine();
               writer.write("Attempting to execute: ");
               writer.newLine();
               
               List<String> fixStatements = getUpdateStatements(type, oidPatches);
               for(String fixStatement: fixStatements)
               {
                  writer.write(fixStatement);
                  writer.write(";");
                  writer.newLine();
               }
               writer.newLine();
            }
            writer.newLine();
         }
      
         writer.close();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }

   }
}