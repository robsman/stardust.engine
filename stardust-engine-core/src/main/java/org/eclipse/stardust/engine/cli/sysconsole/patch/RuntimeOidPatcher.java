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
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptorRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailActivityBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailEventHandlerBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailParticipantBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailProcessDefinitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTransitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTriggerBean;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.EventBindingBean;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry.ElementType;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserParticipantLink;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class RuntimeOidPatcher
{
   private static final Logger trace = LogManager.getLogger(RuntimeOidPatcher.class);

   private SqlUtils sqlUtils;

   private Session session;

   private final boolean logOnly;

   private boolean useNewOid;

   private final boolean noLog;

   public RuntimeOidPatcher(boolean logOnly, boolean useNewOid, boolean noLog)
   {
      this.logOnly = logOnly;
      this.useNewOid = useNewOid;
      this.noLog = noLog;
   }

   private Session obtainSession() throws SQLException
   {
      String schemaKey = SessionProperties.DS_NAME_AUDIT_TRAIL
            + SessionProperties.DS_SCHEMA_SUFFIX;
      String archiveSchema = Parameters.instance().getString(schemaKey);
      if (StringUtils.isEmpty(archiveSchema))
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
      try
      {
         session = obtainSession();
         // push session to parameters so its available in the persistence
         // framework
         Map layerProps = new HashMap();
         layerProps.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX,
               session);
         ParametersFacade.pushLayer(layerProps);
         layerPushed = true;

         sqlUtils = new SqlUtils(session.getSchemaName(), session.getDBDescriptor());

         // do the actual patching
         doPatch();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         if (layerPushed)
         {
            ParametersFacade.popLayer();
         }
      }
   }

   private void doPatch()
   {

      // get the available partition oids
      Statement stmt = null;
      ResultSet rsPartition = null;
      List<Short> partitionOids;
      try
      {
         DBDescriptor dbDescriptor = session.getDBDescriptor();
         stmt = session.getConnection().createStatement();
         partitionOids = new ArrayList<Short>();

         rsPartition = stmt.executeQuery("SELECT "
               + IdentifiablePersistentBean.FIELD__OID
               + "  FROM "
               + getQualifiedName(session.getSchemaName(),
                     dbDescriptor.quoteIdentifier(AuditTrailPartitionBean.TABLE_NAME)));
         while (rsPartition.next())
         {
            partitionOids.add(rsPartition.getShort(1));
         }
      }
      catch (SQLException sqle)
      {
         session.closeAndClearPersistenceControllers();
         throw new PublicException(
               BpmRuntimeError.CLI_FAILED_RESOLVING_PARTITION_OIDS.raise(), sqle);
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(stmt, rsPartition);
      }

      Map<Short, Map<ElementType, List<RuntimeOidPatch>>> patchesByPartitionOid = new HashMap<Short, Map<ElementType, List<RuntimeOidPatch>>>();

      // find invalid runtime oids
      for (Short partitionOid : partitionOids)
      {
         RuntimeOidRegistry rtOidRegistry = new RuntimeOidRegistry(partitionOid);
         PatchAwareRuntimeModelLoader loader = new PatchAwareRuntimeModelLoader(
               partitionOid, useNewOid);
         loader.loadRuntimeOidRegistry(rtOidRegistry);
         Map<ElementType, List<RuntimeOidPatch>> runtimeOidPatches = loader
               .getRuntimeOidPatches();
         if (!runtimeOidPatches.isEmpty())
         {
            patchesByPartitionOid.put(partitionOid, runtimeOidPatches);
         }

      }

      if (!noLog)
      {
         // log what the program is about to do
         logOperations(patchesByPartitionOid);
      }

      if (!logOnly)
      {
         List<String> allUpdateStatements = new ArrayList<String>();
         // correct the invalid runtime oids and all depending tables
         // pointing to them
         try
         {
            allUpdateStatements = getAllUpdateStatements(patchesByPartitionOid);
            if (!allUpdateStatements.isEmpty())
            {
               stmt = session.getConnection().createStatement();
               for (String updateStatement : allUpdateStatements)
               {
                  stmt.addBatch(updateStatement);
               }

               stmt.executeBatch();
               session.save(true);
            }
         }
         catch (BatchUpdateException e)
         {
            List<String> failedStatements = new ArrayList<String>();
            int[] updateInfo = e.getUpdateCounts();
            for (int i = 0; i < updateInfo.length; i++)
            {
               int state = updateInfo[i];
               if (state == Statement.EXECUTE_FAILED)
               {
                  failedStatements.add(allUpdateStatements.get(i));
               }
            }

            trace.error("Exeception during batch update, the following statements failed");
            for (String failedStatement : failedStatements)
            {
               trace.error(failedStatement);
            }

            session.rollback(true);
         }
         catch (SQLException sqle)
         {
            session.rollback(true);
            throw new PublicException(
                  BpmRuntimeError.ARCH_FAILED_PATCHING_ARCHIVE.raise(), sqle);
         }
         finally
         {
            QueryUtils.closeStatementAndResultSet(stmt, rsPartition);
         }
      }
   }

   private List<String> getUpdateStatements(ElementType type,
         List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      if (type.equals(IRuntimeOidRegistry.PARTICIPANT))
      {
         updateStatements = getParticipantUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.DATA))
      {
         updateStatements = getDataUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.PROCESS))
      {
         updateStatements = getProcessDefinitionUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.TRIGGER))
      {
         updateStatements = getTriggerUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.ACTIVITY))
      {
         updateStatements = getActivityUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.TRANSITION))
      {
         updateStatements = getTransitionUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.EVENT_HANDLER))
      {
         updateStatements = getEventHandlerUpdates(patchesForType);
      }
      else if (type.equals(IRuntimeOidRegistry.STRUCTURED_DATA_XPATH))
      {
         updateStatements = getStructuredDataUpdates(patchesForType);
      }
      else
      {
         throw new RuntimeException("Unsupported Runtime Oid Type: " + type);
      }

      return updateStatements;
   }

   private List<String> getStructuredDataUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
      {
         // update reference from structured_data_value
         String structuredDataValueRef = getUpdateStatement(patch,
               StructuredDataValueBean.class, null, StructuredDataValueBean.FR__XPATH);

         // update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               StructuredDataBean.class, StructuredDataBean.FR__MODEL,
               StructuredDataBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               StructuredDataBean.class, StructuredDataBean.FR__MODEL,
               StructuredDataBean.FR__OID);

         updateStatements.add(structuredDataValueRef);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;
   }

   private List<String> getDataUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
      {
         // update reference from data_value
         String dataValueRef = getUpdateStatement(patch, DataValueBean.class,
               DataValueBean.FR__MODEL, DataValueBean.FR__DATA);

         // update reference from structured_data
         String structuredDataRef = getUpdateStatement(patch, StructuredDataBean.class,
               StructuredDataBean.FR__MODEL, StructuredDataBean.FR__DATA);

         // update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailDataBean.class, AuditTrailDataBean.FR__MODEL,
               AuditTrailDataBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailDataBean.class, AuditTrailDataBean.FR__MODEL,
               AuditTrailDataBean.FR__OID);

         updateStatements.add(dataValueRef);
         updateStatements.add(structuredDataRef);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;
   }

   private List<String> getEventHandlerUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
      {
         // update references from event_binding table
         String eventBindingRef = getUpdateStatement(patch, EventBindingBean.class,
               EventBindingBean.FR__MODEL, EventBindingBean.FR__HANDLER_OID);

         // update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailEventHandlerBean.class, AuditTrailEventHandlerBean.FR__MODEL,
               AuditTrailEventHandlerBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailEventHandlerBean.class, AuditTrailEventHandlerBean.FR__MODEL,
               AuditTrailEventHandlerBean.FR__OID);

         updateStatements.add(eventBindingRef);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;
   }

   private List<String> getTransitionUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
      {
         // update references from trans_inst table
         String transitionInstanceRef = getUpdateStatement(patch,
               TransitionInstanceBean.class, TransitionInstanceBean.FR__MODEL,
               TransitionInstanceBean.FR__TRANSITION);

         // update references from trans_token table
         String transitionTokenRef = getUpdateStatement(patch, TransitionTokenBean.class,
               TransitionTokenBean.FR__MODEL, TransitionTokenBean.FR__TRANSITION);

         // update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailTransitionBean.class, AuditTrailTransitionBean.FR__MODEL,
               AuditTrailTransitionBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailTransitionBean.class, AuditTrailTransitionBean.FR__MODEL,
               AuditTrailTransitionBean.FR__OID);

         updateStatements.add(transitionInstanceRef);
         updateStatements.add(transitionTokenRef);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;
   }

   private List<String> getTriggerUpdates(List<RuntimeOidPatch> patchesForType)
   {

      List<String> updateStatements = new ArrayList<String>();

      // no fks are pointing to process_trigger table, only fix runtime oid
      // itself
      for (RuntimeOidPatch patch : patchesForType)
      {
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailTriggerBean.class, AuditTrailTriggerBean.FR__MODEL,
               AuditTrailTriggerBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailTriggerBean.class, AuditTrailTriggerBean.FR__MODEL,
               AuditTrailTriggerBean.FR__OID);

         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;
   }

   private List<String> getParticipantUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
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

         // update references from activity instance
         String aiRef = getUpdateStatement(patch, ActivityInstanceBean.class,
               ActivityInstanceBean.FR__MODEL, ActivityInstanceBean.FR__CURRENT_PERFORMER);

         // update references from user_participant
         String uplRef = getUpdateStatement(patch, UserParticipantLink.class,
               null, UserParticipantLink.FR__PARTICIPANT);

         // update the runtime oids
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailParticipantBean.class, AuditTrailParticipantBean.FR__MODEL,
               AuditTrailParticipantBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailParticipantBean.class, AuditTrailParticipantBean.FR__MODEL,
               AuditTrailParticipantBean.FR__OID);

         updateStatements.add(aihPerformerRefBuffer.toString());
         updateStatements.add(aihOnBehalfRefBuffer.toString());
         updateStatements.add(workItemRefBuffer.toString());
         updateStatements.add(aiRef);
         updateStatements.add(uplRef);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }
      return updateStatements;
   }

   private String getDeleteDuplicatesStatement(RuntimeOidPatch patch, Class persistent,
         FieldRef frModel, FieldRef frOid)
   {
      TypeDescriptorRegistry registry = TypeDescriptorRegistry.current();
      TypeDescriptor descriptor = registry.getDescriptor(persistent);

      StringBuffer buffer = new StringBuffer();

      buffer.append("DELETE FROM ");
      sqlUtils.appendTableRef(buffer, descriptor, false);
      buffer.append(" WHERE ");
      sqlUtils.appendFieldRef(buffer, frOid, false);
      buffer.append(" = ");
      buffer.append(patch.getFixedRuntimeOid());
      buffer.append(" AND ");
      appendModelClause(buffer, patch.getModelOid(), frModel, false);

      return buffer.toString();
   }

   private List<String> getActivityUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
      {
         // update references from activity_instance table
         String referencingActivityInstances = getUpdateStatement(patch,
               ActivityInstanceBean.class, ActivityInstanceBean.FR__MODEL,
               ActivityInstanceBean.FR__ACTIVITY);

         // update references from event_handler table
         String referencingEventHandler = getUpdateStatement(patch,
               AuditTrailEventHandlerBean.class, AuditTrailEventHandlerBean.FR__MODEL,
               AuditTrailEventHandlerBean.FR__ACTIVITY);

         // update reference from work_item table
         String referencingWorkItems = getUpdateStatement(patch, WorkItemBean.class,
               WorkItemBean.FR__MODEL, WorkItemBean.FR__ACTIVITY);

         // update reference from transition table
         String referencingSourceTransition = getUpdateStatement(patch,
               AuditTrailTransitionBean.class, AuditTrailTransitionBean.FR__MODEL,
               AuditTrailTransitionBean.FR__SRC_ACTIVITY);

         // update reference from transition table
         String referencingTargetTransition = getUpdateStatement(patch,
               AuditTrailTransitionBean.class, AuditTrailTransitionBean.FR__MODEL,
               AuditTrailTransitionBean.FR__TGT_ACTIVITY);

         // update activity table itself
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailActivityBean.class, AuditTrailActivityBean.FR__MODEL,
               AuditTrailActivityBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailActivityBean.class, AuditTrailActivityBean.FR__MODEL,
               AuditTrailActivityBean.FR__OID);

         updateStatements.add(referencingActivityInstances);
         updateStatements.add(referencingEventHandler);
         updateStatements.add(referencingWorkItems);
         updateStatements.add(referencingSourceTransition);
         updateStatements.add(referencingTargetTransition);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }

      return updateStatements;
   }

   private List<String> getProcessDefinitionUpdates(List<RuntimeOidPatch> patchesForType)
   {
      List<String> updateStatements = new ArrayList<String>();
      for (RuntimeOidPatch patch : patchesForType)
      {
         // update references from activity_table
         String referencingActivities = getUpdateStatement(patch,
               AuditTrailActivityBean.class, AuditTrailActivityBean.FR__MODEL,
               AuditTrailActivityBean.FR__PROCESS_DEFINITION);

         // update references from event_handler table
         String referencingEventHandler = getUpdateStatement(patch,
               AuditTrailEventHandlerBean.class, AuditTrailEventHandlerBean.FR__MODEL,
               AuditTrailEventHandlerBean.FR__PROCESS_DEFINITION);

         // update references from process_instance table
         String referencingProcessInstances = getUpdateStatement(patch,
               ProcessInstanceBean.class, ProcessInstanceBean.FR__MODEL,
               ProcessInstanceBean.FR__PROCESS_DEFINITION);

         // update references from process_trigger table
         String referencingProcessTriggers = getUpdateStatement(patch,
               AuditTrailTriggerBean.class, AuditTrailTriggerBean.FR__MODEL,
               AuditTrailTriggerBean.FR__PROCESS_DEFINITION);

         // update references from transition table
         String referencingTransitions = getUpdateStatement(patch,
               AuditTrailTransitionBean.class, AuditTrailTransitionBean.FR__MODEL,
               AuditTrailTransitionBean.FR__PROCESS_DEFINITION);

         // update the process definition table itself
         String fixRuntimeOidStatement = getUpdateStatement(patch,
               AuditTrailProcessDefinitionBean.class,
               AuditTrailProcessDefinitionBean.FR__MODEL,
               AuditTrailProcessDefinitionBean.FR__OID);

         String deleteDuplicatesStatement = getDeleteDuplicatesStatement(patch,
               AuditTrailProcessDefinitionBean.class,
               AuditTrailParticipantBean.FR__MODEL, AuditTrailParticipantBean.FR__OID);

         updateStatements.add(referencingActivities);
         updateStatements.add(referencingEventHandler);
         updateStatements.add(referencingProcessInstances);
         updateStatements.add(referencingProcessTriggers);
         updateStatements.add(referencingTransitions);
         updateStatements.add(deleteDuplicatesStatement);
         updateStatements.add(fixRuntimeOidStatement);
      }

      return updateStatements;
   }

   private String getUpdateStatement(RuntimeOidPatch patch, Class persistentClass,
         FieldRef modelFkField, FieldRef updateField)
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
      buffer.append(" AND ");
      appendModelCondition(persistentClass, modelFkField, buffer, patch.getModelOid());

      return buffer.toString();
   }

   private void appendModelCondition(Class persistentClass, FieldRef modelFkField,
         StringBuffer buffer, long modelOid)
   {
      // act_inst_history table has no model fk, join a table which has one
      if (persistentClass.equals(ActivityInstanceHistoryBean.class))
      {
         appendJoinClause(buffer, modelOid, ActivityInstanceBean.class,
               ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE,
               ActivityInstanceBean.FR__OID, ActivityInstanceBean.FR__MODEL);
      }
      // structured_data_value table has no model fk, join a table which has one
      else if (persistentClass.equals(StructuredDataValueBean.class))
      {
         appendJoinClause(buffer, modelOid, ProcessInstanceBean.class,
               StructuredDataValueBean.FR__PROCESS_INSTANCE, ProcessInstanceBean.FR__OID,
               ProcessInstanceBean.FR__MODEL);
      }
      //user_participant table has no model fk - join participant
      else if (persistentClass.equals(UserParticipantLink.class))
      {
         appendJoinClause(buffer, modelOid, AuditTrailParticipantBean.class,
               UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FR__OID,
               AuditTrailParticipantBean.FR__MODEL);
      }
      else
      {
         appendModelClause(buffer, modelOid, modelFkField);
      }
   }

   private void appendJoinClause(StringBuffer buffer, long modelOid, Class type,
         FieldRef frTarget, FieldRef frOid, FieldRef frModel)
   {
      TypeDescriptorRegistry registry = TypeDescriptorRegistry.current();
      TypeDescriptor descriptor = registry.getDescriptor(type);

      sqlUtils.appendFieldRef(buffer, frTarget);
      buffer.append(" IN (SELECT ");
      sqlUtils.appendFieldRef(buffer, frOid);
      buffer.append(" FROM ");
      sqlUtils.appendTableRef(buffer, descriptor, true);
      buffer.append(" WHERE ");
      appendModelClause(buffer, modelOid, frModel);
      buffer.append(')');
   }

   private void appendModelClause(StringBuffer buffer, long modelOid,
         FieldRef modelFkField)
   {
      appendModelClause(buffer, modelOid, modelFkField, true);
   }

   private void appendModelClause(StringBuffer buffer, long modelOid,
         FieldRef modelFkField, boolean useAlias)
   {
      sqlUtils.appendFieldRef(buffer, modelFkField, useAlias);
      buffer.append(" = ");
      buffer.append(modelOid);
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
      if (!StringUtils.isEmpty(qualifier))
      {
         result.append(qualifier).append(".");
      }
      result.append(objectName);

      return result.toString();
   }

   private List<String> getAllUpdateStatements(
         Map<Short, Map<ElementType, List<RuntimeOidPatch>>> patchesByPartitionOid)
   {
      List<String> allUpdateStatements = new ArrayList<String>();
      for (Short partitionOid : patchesByPartitionOid.keySet())
      {
         Map<ElementType, List<RuntimeOidPatch>> patchesByType = patchesByPartitionOid
               .get(partitionOid);
         for (ElementType type : patchesByType.keySet())
         {
            List<RuntimeOidPatch> patchesForType = patchesByType.get(type);
            allUpdateStatements.addAll(getUpdateStatements(type, patchesForType));
         }
      }

      return allUpdateStatements;
   }

   private void logOperations(
         Map<Short, Map<ElementType, List<RuntimeOidPatch>>> patchesByPartitionOid)
   {
      try
      {
         String fileName = "fix_runtime_oid_log_" + TimestampProviderUtils.getTimeStampValue();
         String path = "./" + fileName;
         BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
         for (Short partitionOid : patchesByPartitionOid.keySet())
         {
            writer.write("patches for partition: ");
            writer.write(partitionOid.toString());
            writer.newLine();

            Map<ElementType, List<RuntimeOidPatch>> patchesByType = patchesByPartitionOid
                  .get(partitionOid);
            for (ElementType type : patchesByType.keySet())
            {
               List<RuntimeOidPatch> oidPatches = patchesByType.get(type);
               writer.write("patching type: ");
               writer.write(type.toString());
               writer.newLine();
               for (RuntimeOidPatch patch : oidPatches)
               {
                  writer.write("changing oid from: ");
                  writer.write(Long.toString(patch.getIncorrectRuntimeOid()));
                  writer.write(" -> to ");
                  writer.write(Long.toString(patch.getFixedRuntimeOid()));
                  writer.write(" for model ");
                  writer.write(Long.toString(patch.getModelOid()));
                  writer.newLine();
               }

               writer.newLine();
               writer.write("Attempting to execute: ");
               writer.newLine();

               List<String> fixStatements = getUpdateStatements(type, oidPatches);
               for (String fixStatement : fixStatements)
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
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }
}