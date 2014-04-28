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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.DataTypeDetails;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IExternalPackage;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IModeler;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ParsedDeploymentUnit;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistentModelElement;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry.ElementType;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataLoader;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RuntimeModelLoader implements ModelLoader
{
   public static final Logger trace = LogManager.getLogger(RuntimeModelLoader.class);

   private final short partitionOid;

   public RuntimeModelLoader(short partitionOid)
   {
      this.partitionOid = partitionOid;
   }

   public void loadRuntimeOidRegistry(IRuntimeOidRegistry rtOidRegistry)
   {
      // register models with cache
      RuntimeOidUtils.IdCache cache = new RuntimeOidUtils.IdCache();
      for (Iterator i = ModelPersistorBean.findAll(partitionOid); i.hasNext();)
      {
         cache.register((ModelPersistorBean) i.next());
      }

      // register runtime oids
      registerOids(rtOidRegistry, cache, AuditTrailDataBean.findAll(partitionOid),
            IRuntimeOidRegistry.DATA, true);
      registerOids(rtOidRegistry, cache, StructuredDataBean.findAll(partitionOid),
            IRuntimeOidRegistry.STRUCTURED_DATA_XPATH, false);
      registerOids(rtOidRegistry, cache, AuditTrailParticipantBean.findAll(partitionOid),
            IRuntimeOidRegistry.PARTICIPANT, false);
      registerOids(rtOidRegistry, cache, AuditTrailProcessDefinitionBean.findAll(partitionOid),
            IRuntimeOidRegistry.PROCESS, true);
      registerOids(rtOidRegistry, cache, AuditTrailTriggerBean.findAll(partitionOid),
            IRuntimeOidRegistry.TRIGGER, false);
      registerOids(rtOidRegistry, cache, AuditTrailActivityBean.findAll(partitionOid),
            IRuntimeOidRegistry.ACTIVITY, true);
      registerOids(rtOidRegistry, cache, AuditTrailTransitionBean.findAll(partitionOid),
            IRuntimeOidRegistry.TRANSITION, false);
      registerOids(rtOidRegistry, cache, AuditTrailEventHandlerBean.findAll(partitionOid),
            IRuntimeOidRegistry.EVENT_HANDLER, false);
   }

   public short getPartitionOid()
   {
      return partitionOid;
   }

   protected void registerOids(IRuntimeOidRegistry rtOidRegistry,
         RuntimeOidUtils.IdCache cache, Iterator<? extends PersistentModelElement> itr,
         ElementType type, boolean register)
   {
      while (itr.hasNext())
      {
         PersistentModelElement persistent = itr.next();
         if (register)
         {
            cache.register(persistent);
         }
         rtOidRegistry.registerRuntimeOid(type,
               RuntimeOidUtils.getFqId(persistent, cache), persistent.getOID());
      }
   }

   /**
    * Reads all models from a JDBC data source. We currently assume, that all
    * loaded model versions have the same name.
    * <p/>
    * Asserts, that the runtime database driver is already initialized and
    * bound.
    */
   public Iterator loadModels()
   {
      return ModelPersistorBean.findAll(partitionOid);
   }

   public void deployModel(List<ParsedDeploymentUnit> units, DeploymentOptions options,
         IRuntimeOidRegistry rtOidRegistry)
   {
      SchemaHelper.validateBaseProperties();

      AuditTrailPartitionBean partition = (AuditTrailPartitionBean) SecurityProperties
            .getPartition(false);

      for (ParsedDeploymentUnit unit : units)
      {
         IModel model = unit.getModel();
         Date validFrom = options.getValidFrom();
         ModelPersistorBean modelPersistor = new ModelPersistorBean(
               validFrom == null ? (Date) model.getAttribute(PredefinedConstants.VALID_FROM_ATT) : validFrom,
               options.getComment(), 0, partition);
         // TODO: use BLOB
         modelPersistor.setModel(model, XmlUtils.getXMLString(unit.getContent()));
      }

      ModelDeploymentBean deployment = new ModelDeploymentBean(options.getValidFrom(), options.getComment());
      for (ParsedDeploymentUnit unit : units)
      {
         IModel model = unit.getModel();
         // (fh) each model has a reference to self!
         ModelRefBean.setResolvedModel(null, model, deployment.getOID());
         for (IExternalPackage externalPackage : model.getExternalPackages())
         {
            IModel referencedModel = externalPackage.getReferencedModel();
            if (referencedModel != null)
            {
               trace.info(model.getName() + " references " + referencedModel.getName());
               ModelRefBean.setResolvedModel(externalPackage, referencedModel, deployment.getOID());
            }
         }
         updateModelTables(model, null, rtOidRegistry);
      }
   }

   public void modifyModel(ParsedDeploymentUnit unit, DeploymentOptions options, IRuntimeOidRegistry rtOidRegistry)
   {
      IModel model = unit.getModel();
      IModel lastModel  = ModelManagerFactory.getCurrent().findActiveModel(model.getId());

      SchemaHelper.validateBaseProperties();

      ModelPersistorBean modelPersistor = ModelPersistorBean.findByModelOID(unit.getReferencedModelOid());

      modelPersistor.modify(options.getComment());
      modelPersistor.incrementRevision();
      modelPersistor.setModel(model, XmlUtils.getXMLString(unit.getContent()));

      //TODO: (fh) update deployment references...

      updateModelTables(model, lastModel, rtOidRegistry);
   }

   private static void validateModelElemetId(Class type, long rtOid,
         String modelFieldName, long modelOid, String idFieldName,
         String expectedModelElemetId)
   {
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;

         QueryDescriptor desc = QueryDescriptor.from(type);
         desc.select(desc.fieldRef(idFieldName)) //
               .setPredicateTerm(Predicates.andTerm( //
                     Predicates.isEqual(desc.fieldRef(modelFieldName), modelOid), //
                     Predicates.isEqual(desc.fieldRef(IdentifiablePersistentBean.FIELD__OID), rtOid)));

         ResultSet resultSet = null;
         try
         {
            resultSet = jdbcSession.executeQuery(desc);

            if ( !resultSet.next())
            {
               throw new InternalException(MessageFormat
                     .format("Deployed object of type {0} does not exist for "
                           + "modelOid = {1} and runtimeOid = {2}.", //
                           new Object[] { type.getName(), new Long(modelOid),
                                 new Long(rtOid) }));
            }

            String dbIdValue = resultSet.getString(idFieldName);
            if ( !CompareHelper.areEqual(dbIdValue, expectedModelElemetId))
            {
               throw new PublicException(
                     BpmRuntimeError.BPMRT_DEPLOYED_OBJECT_DIFFERS_IN_ITS_IDFROM_ITS_DEFINED_VALUE
                           .raise(type.getName(), expectedModelElemetId, dbIdValue));
            }
         }
         catch (SQLException e)
         {
            throw new InternalException(e);
         }
         finally
         {
            QueryUtils.closeResultSet(resultSet);
         }
      }
   }

   private static void updateModelTables(IModel model, IModel lastModel, IRuntimeOidRegistry rtOidRegistry)
   {
      // @todo need cleanup for existing items in case of overwrite
      // @todo (france, ub): revise for overwriting case

      Session driver = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      final int modelOID = model.getModelOID();

      // prefetching all model element definitions into cache, thus speeding up eventual lookup
      Map<Long, AuditTrailProcessDefinitionBean> processDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailProcessDefinitionBean.class,
            AuditTrailProcessDefinitionBean.FR__MODEL);

      Map<Long, AuditTrailEventHandlerBean> eventHandlerDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailEventHandlerBean.class,
            AuditTrailEventHandlerBean.FR__MODEL);

      Map<Long, AuditTrailTriggerBean> triggerDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailTriggerBean.class,
            AuditTrailTriggerBean.FR__MODEL);

      Map<Long, AuditTrailActivityBean> activityDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailActivityBean.class,
            AuditTrailActivityBean.FR__MODEL);

      Map<Long, AuditTrailTransitionBean> transitionDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailTransitionBean.class,
            AuditTrailTransitionBean.FR__MODEL);

      Map<Long, AuditTrailDataBean> dataDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailDataBean.class,
            AuditTrailDataBean.FR__MODEL);

      Map<Long, AuditTrailParticipantBean> modelParticipantDefRecords = loadModelElementDefinitions(
            modelOID, AuditTrailParticipantBean.class,
            AuditTrailParticipantBean.FR__MODEL);

      for (IProcessDefinition process : model.getProcessDefinitions())
      {
         AuditTrailProcessDefinitionBean auditTrailProcess = null;

         long rtProcOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.PROCESS,
               RuntimeOidUtils.getFqId(process));
         if (0 != rtProcOid)
         {
            auditTrailProcess = (AuditTrailProcessDefinitionBean) processDefRecords.get(Long.valueOf(rtProcOid));
         }

         if (null != auditTrailProcess)
         {
            auditTrailProcess.update(process);
         }
         else
         {
            if (0 == rtProcOid)
            {
               rtProcOid = rtOidRegistry.registerNewRuntimeOid(
                     IRuntimeOidRegistry.PROCESS, RuntimeOidUtils.getFqId(process));
            }
            auditTrailProcess = new AuditTrailProcessDefinitionBean(rtProcOid,
                  modelOID, process);
            driver.cluster(auditTrailProcess);
         }

         validateModelElemetId(AuditTrailProcessDefinitionBean.class, rtProcOid,
               AuditTrailProcessDefinitionBean.FIELD__MODEL, modelOID,
               AuditTrailProcessDefinitionBean.FIELD__ID, process.getId());

         // Triggers
         for (Iterator triggers = process.getAllTriggers(); triggers.hasNext();)
         {
            ITrigger trigger = (ITrigger) triggers.next();
            AuditTrailTriggerBean auditTrailTrigger = null;

            long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.TRIGGER,
                  RuntimeOidUtils.getFqId(trigger));
            if (0 != rtOid)
            {
               auditTrailTrigger = (AuditTrailTriggerBean) triggerDefRecords.get(Long.valueOf(rtOid));
            }

            if (null != auditTrailTrigger)
            {
               auditTrailTrigger.update(trigger);
               if (lastModel != null)
               {
                  IProcessDefinition processDefinition = lastModel
                        .findProcessDefinition(process.getId());
                  if (processDefinition != null)
                  {
                     ITrigger lastTrigger = processDefinition
                           .findTrigger(trigger.getId());
                     if (lastTrigger != null)
                     {
                        Long newStartTimestamp = (Long) trigger
                              .getAttribute(PredefinedConstants.TIMER_TRIGGER_START_TIMESTAMP_ATT);
                        Long oldStartTimestamp = (Long) lastTrigger
                              .getAttribute(PredefinedConstants.TIMER_TRIGGER_START_TIMESTAMP_ATT);
                        if (newStartTimestamp != null && oldStartTimestamp != null
                              && !newStartTimestamp.equals(oldStartTimestamp))
                        {
                           TimerLog timerLog = TimerLog.findOrCreate(lastTrigger);
                           timerLog.setTimeStamp(Unknown.LONG);
                        }
                     }
                  }
               }
            }
            else
            {
               if (0 == rtOid)
               {
                  rtOid = rtOidRegistry.registerNewRuntimeOid(IRuntimeOidRegistry.TRIGGER,
                        RuntimeOidUtils.getFqId(trigger));
               }
               auditTrailTrigger = new AuditTrailTriggerBean(rtOid, modelOID,
                     trigger);
               driver.cluster(auditTrailTrigger);
            }

            validateModelElemetId(AuditTrailTriggerBean.class, rtOid,
                  AuditTrailTriggerBean.FIELD__MODEL, modelOID,
                  AuditTrailTriggerBean.FIELD__ID, trigger.getId());
         }

         // process level event handlers
         for (Iterator handlers = process.getAllEventHandlers(); handlers.hasNext();)
         {
            IEventHandler handler = (IEventHandler) handlers.next();

            AuditTrailEventHandlerBean auditTrailHandler = null;

            long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.EVENT_HANDLER,
                  RuntimeOidUtils.getFqId(handler));
            if (0 != rtOid)
            {
               auditTrailHandler = (AuditTrailEventHandlerBean) eventHandlerDefRecords.get(Long.valueOf(rtOid));
            }

            if (null != auditTrailHandler)
            {
               auditTrailHandler.update(handler);
            }
            else
            {
               if (0 == rtOid)
               {
                  rtOid = rtOidRegistry.registerNewRuntimeOid(
                        IRuntimeOidRegistry.EVENT_HANDLER, RuntimeOidUtils.getFqId(handler));
               }
               auditTrailHandler = new AuditTrailEventHandlerBean(rtOid, modelOID,
                     handler);
               driver.cluster(auditTrailHandler);
            }

            validateModelElemetId(AuditTrailEventHandlerBean.class, rtOid,
                  AuditTrailEventHandlerBean.FIELD__MODEL, modelOID,
                  AuditTrailEventHandlerBean.FIELD__ID, handler.getId());
            }

         // Activities
         for (Iterator activities = process.getAllActivities(); activities.hasNext();)
         {
            IActivity activity = (IActivity) activities.next();

            AuditTrailActivityBean auditTrailActivity = null;

            long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.ACTIVITY,
                  RuntimeOidUtils.getFqId(activity));
            if (0 != rtOid)
            {
               auditTrailActivity = (AuditTrailActivityBean) activityDefRecords.get(Long.valueOf(rtOid));
            }

            if (null != auditTrailActivity)
            {
               auditTrailActivity.update(activity);
            }
            else
            {
               if (0 == rtOid)
               {
                  rtOid = rtOidRegistry.registerNewRuntimeOid(IRuntimeOidRegistry.ACTIVITY,
                        RuntimeOidUtils.getFqId(activity));
               }
               auditTrailActivity = new AuditTrailActivityBean(rtOid, modelOID,
                     activity);
               driver.cluster(auditTrailActivity);
            }

            validateModelElemetId(AuditTrailActivityBean.class, rtOid,
                  AuditTrailActivityBean.FIELD__MODEL, modelOID,
                  AuditTrailActivityBean.FIELD__ID, activity.getId());

            // activity level event handlers
            for (Iterator handlers = activity.getAllEventHandlers(); handlers.hasNext();)
            {
               IEventHandler handler = (IEventHandler) handlers.next();

               AuditTrailEventHandlerBean auditTrailHandler = null;

               long rtHandlerOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.EVENT_HANDLER,
                     RuntimeOidUtils.getFqId(handler));
               if (0 != rtHandlerOid)
               {
                  auditTrailHandler = (AuditTrailEventHandlerBean) eventHandlerDefRecords.get(Long.valueOf(rtHandlerOid));
               }

               if (null != auditTrailHandler)
               {
                  auditTrailHandler.update(handler);
               }
               else
               {
                  if (0 == rtHandlerOid)
                  {
                     rtHandlerOid = rtOidRegistry.registerNewRuntimeOid(
                           IRuntimeOidRegistry.EVENT_HANDLER, RuntimeOidUtils.getFqId(handler));
                  }
                  auditTrailHandler = new AuditTrailEventHandlerBean(rtHandlerOid,
                        modelOID, handler);
                  driver.cluster(auditTrailHandler);
               }

               validateModelElemetId(AuditTrailEventHandlerBean.class, rtHandlerOid,
                     AuditTrailEventHandlerBean.FIELD__MODEL, modelOID,
                     AuditTrailEventHandlerBean.FIELD__ID, handler.getId());
            }
         }

         // Transitions
         for (Iterator transitions = process.getAllTransitions(); transitions.hasNext();)
         {
            ITransition transition = (ITransition) transitions.next();

            AuditTrailTransitionBean auditTrailTransition = null;

            long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.TRANSITION,
                  RuntimeOidUtils.getFqId(transition));
            if (0 != rtOid)
            {
               auditTrailTransition = (AuditTrailTransitionBean) transitionDefRecords.get(Long.valueOf(rtOid));
            }

            if (null != auditTrailTransition)
            {
               auditTrailTransition.update(transition);
            }
            else
            {
               if (0 == rtOid)
               {
                  rtOid = rtOidRegistry.registerNewRuntimeOid(
                        IRuntimeOidRegistry.TRANSITION, RuntimeOidUtils.getFqId(transition));
               }
               auditTrailTransition = new AuditTrailTransitionBean(rtOid, modelOID,
                     transition);
               driver.cluster(auditTrailTransition);
            }

            validateModelElemetId(AuditTrailTransitionBean.class, rtOid,
                  AuditTrailTransitionBean.FIELD__MODEL, modelOID,
                  AuditTrailTransitionBean.FIELD__ID, transition.getId());
         }
      }

      // Data
      for (Iterator allData = model.getAllData(); allData.hasNext();)
      {
         IData data = (IData) allData.next();
         if (model != data.getModel())
         {
            continue;
         }

         AuditTrailDataBean auditTrailData = null;
         long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.DATA,
               RuntimeOidUtils.getFqId(data));
         if (0 != rtOid)
         {
            auditTrailData = (AuditTrailDataBean) dataDefRecords.get(Long.valueOf(rtOid));
         }

         DataTypeDetails dataTypeDetails = (DataTypeDetails)DetailsFactory.create(data.getType(), IDataType.class,
               DataTypeDetails.class);
         DataLoader dataTypeLoader = (DataLoader) Reflect.createInstance(dataTypeDetails.getDataTypeLoaderClass());
         if (null != auditTrailData)
         {
            auditTrailData.update(data);
         }
         else
         {
            if (0 == rtOid)
            {
               rtOid = rtOidRegistry.registerNewRuntimeOid(IRuntimeOidRegistry.DATA,
                     RuntimeOidUtils.getFqId(data));
            }
            auditTrailData = new AuditTrailDataBean(rtOid, modelOID, data);
            driver.cluster(auditTrailData);
         }
         dataTypeLoader.deployData(rtOidRegistry, data, rtOid, modelOID, model);

         validateModelElemetId(AuditTrailDataBean.class, rtOid,
               AuditTrailDataBean.FIELD__MODEL, modelOID, //
               AuditTrailDataBean.FIELD__ID, data.getId());
      }

      // Participants
      for (Iterator i = model.getAllParticipants(); i.hasNext();)
      {
         IModelParticipant participant = (IModelParticipant) i.next();
         if (model != participant.getModel())
         {
            continue;
         }

         if (!(participant instanceof IModeler))
         {
            AuditTrailParticipantBean auditTrailParticipant = null;

            long rtOid = rtOidRegistry.getRuntimeOid(IRuntimeOidRegistry.PARTICIPANT,
                  RuntimeOidUtils.getFqId(participant));
            if (0 != rtOid)
            {
               auditTrailParticipant = (AuditTrailParticipantBean) modelParticipantDefRecords.get(Long.valueOf(rtOid));
            }

            if (null != auditTrailParticipant)
            {
               auditTrailParticipant.update(participant);
            }
            else
            {
               if (0 == rtOid)
               {
                  rtOid = rtOidRegistry.registerNewRuntimeOid(
                        IRuntimeOidRegistry.PARTICIPANT,
                        RuntimeOidUtils.getFqId(participant));
               }
               auditTrailParticipant = new AuditTrailParticipantBean(rtOid,
                     modelOID, participant);
               driver.cluster(auditTrailParticipant);
            }

            validateModelElemetId(AuditTrailParticipantBean.class, rtOid,
                  AuditTrailParticipantBean.FIELD__MODEL, modelOID,
                  AuditTrailParticipantBean.FIELD__ID, auditTrailParticipant.getId());
         }
      }
   }

   private static <T extends IdentifiablePersistent> Map<Long, T> loadModelElementDefinitions(
         int modelOid, Class<T> type, FieldRef frModel)
   {
      Map<Long, T> result = CollectionUtils.newHashMap();

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         for (Iterator i = session.getIterator(type, //
               QueryExtension.where(Predicates.isEqual(frModel, modelOid))); i.hasNext();)
         {
            IdentifiablePersistent element = (IdentifiablePersistent) i.next();
            result.put(Long.valueOf(element.getOID()), (T) element);
         }
      }

      return result;
   }
}
