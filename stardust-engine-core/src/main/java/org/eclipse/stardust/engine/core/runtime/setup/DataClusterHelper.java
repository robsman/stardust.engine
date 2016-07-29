/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.setup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TimeMeasure;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler;
import org.eclipse.stardust.engine.core.runtime.beans.PropertyPersistor;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler.Representation;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.setup.ClusterSlotFieldInfo.SLOT_TYPE;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataClusterMetaInfoRetriever;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataClusterSynchronizationInfo;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;


public class DataClusterHelper
{
   private static final Logger trace = LogManager.getLogger(DataClusterHelper.class);

   private static Set<ProcessInstanceState> getRequiredPiStates(ProcessStateFilter stateFilter)
   {
      Set<ProcessInstanceState> restrictedStates = new HashSet<ProcessInstanceState>();
      Set<ProcessInstanceState> allStates = ProcessInstanceState.getAllStates();
      Set<ProcessInstanceState> filterStates = new HashSet<ProcessInstanceState>(
            Arrays.asList(stateFilter.getStates()));

      if(stateFilter.isInclusive())
      {
         restrictedStates.addAll(filterStates);
      }
      else
      {
         restrictedStates.addAll(allStates);
         restrictedStates.removeAll(filterStates);
      }

      if(restrictedStates.isEmpty())
      {
         restrictedStates.addAll(ProcessInstanceState.getAllStates());
      }

      return restrictedStates;
   }

   private static Set<ProcessInstanceState> getRequiredPiStates(ActivityInstanceState state)
   {
      Set<ProcessInstanceState> restrictedStates
         = new HashSet<ProcessInstanceState>();
      switch (state.getValue())
      {
         case ActivityInstanceState.CREATED:
         case ActivityInstanceState.APPLICATION:
         case ActivityInstanceState.INTERRUPTED:
         case ActivityInstanceState.SUSPENDED:
         case ActivityInstanceState.HIBERNATED:
            restrictedStates.add(ProcessInstanceState.Active);
            restrictedStates.add(ProcessInstanceState.Interrupted);
            break;

         default:
            restrictedStates.addAll(ProcessInstanceState.getAllStates());
            break;
      }

      return restrictedStates;
   }

   private static void collectRequiredClusterPiStates(FilterTerm filterTerm, Set<ProcessInstanceState> restrictions)
   {
      for(Object part: filterTerm.getParts())
      {
         FilterCriterion criterion = (FilterCriterion) part;
         if(criterion instanceof FilterTerm)
         {
            FilterTerm tmpFilterTerm = (FilterTerm) criterion;
            collectRequiredClusterPiStates(tmpFilterTerm, restrictions);
         }

         if(criterion instanceof ProcessStateFilter)
         {
            ProcessStateFilter stateFilter = (ProcessStateFilter) criterion;
            restrictions.addAll(getRequiredPiStates(stateFilter));
         }

         if(criterion instanceof ActivityStateFilter)
         {
            ActivityStateFilter stateFilter = (ActivityStateFilter) criterion;
            for(ActivityInstanceState aiState: stateFilter.getStates())
            {
               restrictions.addAll(getRequiredPiStates(aiState));
            }
         }
      }
   }

   private static Set<ProcessInstanceState> getRequiredClusterPiStates(Query query)
   {
      Set<ProcessInstanceState> requiredPiStates = new HashSet<ProcessInstanceState>();
      collectRequiredClusterPiStates(query.getFilter(), requiredPiStates);
      return requiredPiStates;
   }

   public static void setRequiredClusterPiStates(Query query)
   {
      DataClusterRuntimeInfo clusterRuntimeInfo = DataClusterHelper.getDataClusterRuntimeInfo();
      if (clusterRuntimeInfo != null && !clusterRuntimeInfo.isRequiredClusterPiStatesSet())
      {
         Set<ProcessInstanceState> requiredPiStates = DataClusterHelper.getRequiredClusterPiStates(query);
         clusterRuntimeInfo.setRequiredClusterPiStates(requiredPiStates);
      }
   }

   /**
    * Returns a Set of {@link ProcessInstanceState} which needs to be supported by a {@link DataCluster}
    * to be able to fetch data value from it
    *
    * @param query - the query to be analyized
    * @return a Set of {@link ProcessInstanceState} which needs to be supported by a {@link DataCluster}
    * to be able to fetch data value from it
    */
   public static Set<ProcessInstanceState> getRequiredClusterPiStates()
   {
      Set<ProcessInstanceState> requiredClusterPiStates = null;
      DataClusterRuntimeInfo clusterRuntimeInfo = DataClusterHelper.getDataClusterRuntimeInfo();
      if(clusterRuntimeInfo != null)
      {
         requiredClusterPiStates = clusterRuntimeInfo.getRequiredClusterPiStates();
      }
      else
      {
         requiredClusterPiStates = ProcessInstanceState.getAllStates();
      }

      return requiredClusterPiStates;
   }

   public static DataClusterRuntimeInfo getDataClusterRuntimeInfo()
   {
      if(DataClusterHelper.isDataClusterPresent())
      {
         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         DataClusterRuntimeInfo rtEnvClusterInfo = rtEnv.getDataClusterRuntimeInfo();
         if(rtEnvClusterInfo == null)
         {
            rtEnvClusterInfo = new DataClusterRuntimeInfo();
            rtEnv.setDataClusterRuntimeInfo(rtEnvClusterInfo);
         }

         return rtEnvClusterInfo;
      }

      return null;
   }

   public static boolean isDataClusterPresent()
   {
      RuntimeSetup setup = RuntimeSetup.instance();
      if(setup != null && setup.hasDataClusterSetup())
      {
         return true;
      }

      return false;
   }

   public static void deleteDataClusterSetup()
   {
      try
      {
         org.eclipse.stardust.engine.core.persistence.Session session
            = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

         PropertyPersistor setupPersistor
            = getDataClusterPersistor();
         if(setupPersistor != null)
         {
            LargeStringHolder.deleteAllForOID(setupPersistor.getOID(), PropertyPersistor.class);
            setupPersistor.delete(true);
            session.save();
         }
      }
      finally
      {
         Parameters.instance().set(RuntimeSetup.RUNTIME_SETUP_PROPERTY, null);
      }
   }

   private static PropertyPersistor getDataClusterPersistor()
   {
      PropertyPersistor prop = PropertyPersistor
         .findByName(RuntimeSetup.RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);
      //try to load old style definition as fallback
      if(prop == null)
      {
         prop = PropertyPersistor.findByName(RuntimeSetup.PRE_STARDUST_RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);
      }

      return prop;
   }

   public static void synchronizeDataCluster(IProcessInstance scopeProcessInstance)
   {
      RuntimeSetup setup = RuntimeSetup.instance();
      if (setup.hasDataClusterSetup())
      {
         org.eclipse.stardust.engine.core.persistence.Session auditTrailSession = SessionFactory
               .getSession(SessionFactory.AUDIT_TRAIL);
         if (auditTrailSession instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
         {
            org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) auditTrailSession;
            long scopePiOid = scopeProcessInstance.getOID();
            ProcessInstanceState scopePiState = scopeProcessInstance.getState();
            DataCluster[] clusters = setup.getDataClusterSetup();
            for (DataCluster dc : clusters)
            {
               if (dc.isEnabledFor(scopePiState))
               {
                  if (!clusterHasProcessInstance(dc, scopePiOid))
                  {
                     // create the missing entry for scope pi
                     new DataClusterInstance(dc, scopePiOid);
                     // update that entry with the data values
                     DataClusterSynchronizationInfo syncInfo = getDataClusterSynchronizationInfo(
                           dc, scopeProcessInstance);
                     syncInfo.setPerformClusterVerification(false);
                     syncInfo.setScopePiOid(scopePiOid);
                     synchronizeDataCluster(syncInfo, jdbcSession);
                  }
               }
               else
               {
                  deleteFromCluster(dc, scopePiOid);
               }
            }
         }
      }
   }

   public static DataClusterSynchronizationInfo getDataClusterSynchronizationInfo(
         DataCluster clusterToSynchronize,
         IProcessInstance scopeProcessInstance)
   {
      Map<DataClusterKey, Set<AbstractDataClusterSlot>> clusterToSlotMapping = CollectionUtils.newHashMap();
      Map<DataSlotKey, Map<ClusterSlotFieldInfo.SLOT_TYPE, ClusterSlotFieldInfo>> slotToColumnMapping = CollectionUtils.newHashMap();

      DataClusterKey clusterKey = new DataClusterKey(clusterToSynchronize);
      for (AbstractDataClusterSlot ds : clusterToSynchronize.getAllSlots())
      {
         DataSlotKey slotKey = new DataSlotKey(ds);
         Set<AbstractDataClusterSlot> slots = clusterToSlotMapping.get(clusterKey);
         if (slots == null)
         {
            slots = CollectionUtils.<AbstractDataClusterSlot>newHashSet();
            clusterToSlotMapping.put(clusterKey, slots);
         }
         slots.add(ds);

         List<ClusterSlotFieldInfo> slotColumnFields = DataClusterMetaInfoRetriever
               .getDataSlotFields(ds);
         HashMap<SLOT_TYPE, ClusterSlotFieldInfo> typeToFieldMap = CollectionUtils.newHashMap(slotColumnFields.size());
         for (ClusterSlotFieldInfo fieldInfo : DataClusterMetaInfoRetriever
               .getDataSlotFields(ds))
         {
            typeToFieldMap.put(fieldInfo.getSlotType(), fieldInfo);
         }
         slotToColumnMapping.put(slotKey, typeToFieldMap);
      }

      DataClusterSynchronizationInfo syncInfo = new DataClusterSynchronizationInfo(
            clusterToSlotMapping, slotToColumnMapping, null);
      return syncInfo;
   }

   private static void synchronizeDataCluster(DataClusterSynchronizationInfo synchronizationInfo, org.eclipse.stardust.engine.core.persistence.jdbc.Session session)
   {
      DBDescriptor dbDescriptor = session.getDBDescriptor();
      DDLManager ddlManager = new DDLManager(dbDescriptor);
      String schemaName = session.getSchemaName();
      try
      {
         ddlManager.synchronizeDataCluster(false, synchronizationInfo, session.getConnection(), schemaName, null, null, null);
      }
      catch (SQLException e)
      {
         String errorMsg = "Error while synchronizing data cluster: ";
         trace.error(errorMsg, e);
         throw new InternalException(errorMsg, e);
      }
   }

   private static boolean clusterHasProcessInstance(DataCluster dc, long piOid)
   {
      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session sessionImpl = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
         StringBuilder builder = new StringBuilder();
         builder.append("SELECT");
         builder.append(" COUNT(");
         builder.append(dc.getProcessInstanceColumn());
         builder.append(") FROM ");
         builder.append(dc.getQualifiedTableName());
         builder.append(" WHERE ");
         builder.append(dc.getProcessInstanceColumn());
         builder.append(" = ");
         builder.append(piOid);

         String sqlString = builder.toString();
         Statement stmt = null;
         try
         {
            stmt = sessionImpl.getConnection().createStatement();
            final TimeMeasure timer = new TimeMeasure();

            ResultSet rs = stmt.executeQuery(sqlString);
            rs.next();
            int count = rs.getInt(1);
            if(count > 0)
            {
               return true;
            }

            sessionImpl.monitorSqlExecution(sqlString, timer.stop());
         }
         catch (SQLException x)
         {
            trace.warn("Error while executing statement: " + sqlString, x);
            throw new InternalException("Error while executing statement: " + sqlString,
                  x);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }

      return false;
   }

   public static void deleteFromCluster(DataCluster dc, long piOid)
   {
      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session sessionImpl = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
         StringBuilder builder = new StringBuilder();
         builder.append("DELETE FROM ");
         builder.append(dc.getQualifiedTableName());
         builder.append(" WHERE ");
         builder.append(dc.getProcessInstanceColumn());
         builder.append(" = ");
         builder.append(piOid);

         String sqlString = builder.toString();
         Statement stmt = null;
         try
         {
            stmt = sessionImpl.getConnection().createStatement();
            final TimeMeasure timer = new TimeMeasure();
            stmt.executeUpdate(sqlString);
            sessionImpl.monitorSqlExecution(sqlString, timer.stop());
         }
         catch (SQLException x)
         {
            trace.warn("Error while executing statement: " + sqlString, x);
            throw new InternalException("Error while executing statement: " + sqlString,
                  x);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   private static void completeDataValueModification(
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, ClusterSlotData>>> piToDv,
         Session jdbcSession, boolean isDeleteModification)
   {
      for (Pair<Long, DataCluster> key : piToDv.keySet())
      {
         List<Pair<PersistenceController, ClusterSlotData>> slotValues = piToDv.get(key);

         if (null != slotValues && slotValues.size() != 0)
         {
            long piOid = key.getFirst().longValue();
            AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
                  (IProcessInstance) jdbcSession.findByOID(ProcessInstanceBean.class,
                        piOid), null);
            DataCluster dataCluster = key.getSecond();

            StringBuffer buffer = new StringBuffer(100);

            buffer.append("UPDATE ").append(dataCluster.getQualifiedTableName());
            buffer.append(" SET ");

            List<Pair<Class, ? >> bindValueList = CollectionUtils.newArrayList(slotValues.size() * 3 + 1);
            String appendToken = "";
            for (Pair<PersistenceController, ClusterSlotData> compoundValue : slotValues)
            {
               PersistenceController dpc = compoundValue.getFirst();
               DataValueBean dataValue = (DataValueBean) dpc.getPersistent();
               ClusterSlotData dataSlot = compoundValue.getSecond();

               String dataTypeId = dataValue.getData().getType().getId();
               if (!StringUtils.isEmpty(dataSlot.getParent().getSValueColumn())
                     && (StructuredTypeRtUtils.isDmsType(dataTypeId) || StructuredTypeRtUtils
                           .isStructuredType(dataTypeId)))
               {
                  IXPathMap xPathMap = DataXPathMap.getXPathMap(dataValue.getData());
                  if (!xPathMap.containsXPath(dataSlot.getAttributeName()))
                  {
                     continue;
                  }
               }

               buffer.append(appendToken);
               appendToken = ",";

               buffer.append(dataSlot.getParent().getOidColumn()).append("=?,");
               buffer.append(dataSlot.getParent().getTypeColumn()).append("=?");

               if (StringUtils.isNotEmpty(dataSlot.getParent().getSValueColumn()))
               {
                  buffer.append(',').append(dataSlot.getParent().getSValueColumn()).append("=?");
               }
               if (StringUtils.isNotEmpty(dataSlot.getParent().getNValueColumn()))
               {
                  buffer.append(',').append(dataSlot.getParent().getNValueColumn()).append("=?");
               }

               bindValueList.add(new Pair(Long.class, Long.valueOf(dataValue.getOID())));


               if ( !StringUtils.isEmpty(dataSlot.getParent().getSValueColumn()))
               {
                  String sValue;

                  boolean upadteDValue = false;
                  Double dValue = null;

                  // TODO (ab) SPI?
                  if (StructuredTypeRtUtils.isDmsType(dataTypeId)
                        || StructuredTypeRtUtils.isStructuredType(dataTypeId))
                  {
                     Representation representation = getRepresentationForDataValue(
                           dataValue, dataSlot.getAttributeName(), evaluationContext);

                     sValue = (String) representation.getRepresentation();
                     int attributeType = DataXPathMap.getXPathMap(dataValue.getData())
                           .getXPath(dataSlot.getAttributeName()).getType();
                     bindValueList
                           .add(new Pair(Integer.class, new Integer(attributeType)));

                     // update dValue column is requested
                     if (StringUtils.isNotEmpty(dataSlot.getParent().getDValueColumn()))
                     {
                        IProcessInstance rawPi = dataValue.getProcessInstance();

                        // should always be a PIBean
                        if (rawPi instanceof ProcessInstanceBean)
                        {
                           ProcessInstanceBean pi = (ProcessInstanceBean) rawPi;

                           Long xPathOID = DataXPathMap.getXPathMap(dataValue.getData())
                                 .getXPathOID(dataSlot.getAttributeName());
                           IStructuredDataValue cachedSdv = pi
                                 .getCachedStructuredDataValue(xPathOID);

                           // if cached value is available then it has been changed potentially
                           if (cachedSdv instanceof BigData)
                           {
                              dValue = ((BigData) cachedSdv).getDoubleValue();
                              upadteDValue = true;
                           }
                        }

                     }
                  }
                  else
                  {
                     sValue = dataValue.getShortStringValue();
                     bindValueList.add(new Pair(Integer.class, new Integer(dataValue
                           .getType())));

                     // update dValue column is requested
                     if (StringUtils.isNotEmpty(dataSlot.getParent().getDValueColumn()))
                     {
                        dValue = dataValue.getDoubleValue();
                        upadteDValue = true;
                     }
                  }
                  bindValueList.add(new Pair(String.class, sValue));

                  if (upadteDValue)
                  {
                     buffer.append(',').append(dataSlot.getParent().getDValueColumn()).append("=?");
                     bindValueList.add(new Pair(Double.class, dValue));
                  }
               }
               else
               {
                  Long value;
                  // TODO (ab) SPI?
                  if (StructuredTypeRtUtils.isDmsType(dataTypeId)
                        || StructuredTypeRtUtils.isStructuredType(dataTypeId))
                  {
                     Representation representation = getRepresentationForDataValue(
                           dataValue, dataSlot.getAttributeName(), evaluationContext);
                     value = (Long) representation.getRepresentation();

                     int attributeType = DataXPathMap.getXPathMap(dataValue.getData())
                           .getXPath(dataSlot.getAttributeName()).getType();
                     bindValueList
                           .add(new Pair(Integer.class, new Integer(attributeType)));
                  }
                  else
                  {
                     value = new Long(dataValue.getLongValue());
                     bindValueList.add(new Pair(Integer.class, new Integer(dataValue
                           .getType())));
                  }
                  bindValueList.add(new Pair(Long.class, value));
               }
            }

            buffer.append(" WHERE ").append(dataCluster.getProcessInstanceColumn())
                  .append(" = ?");

            PreparedStatement stmt = null;
            String stmtString = null;

            try
            {
               stmtString = buffer.toString();
               stmt = jdbcSession.getConnection().prepareStatement(stmtString);

               int pos = 1;
               for (Pair<Class, ? > compoundBindValue : bindValueList)
               {
                  Class type = compoundBindValue.getFirst();
                  Object bindValue = compoundBindValue.getSecond();

                  if (isDeleteModification)
                  {
                     bindValue = null;
                  }

                  DmlManager.setSQLValue(stmt, pos, type, bindValue, jdbcSession.getDBDescriptor());
                  ++pos;
               }

               stmt.setLong(pos, piOid);

               final TimeMeasure timer = new TimeMeasure();
               stmt.executeUpdate();
               jdbcSession.monitorSqlExecution(stmtString, timer.stop());
            }
            catch(SQLException x)
            {
               trace.warn("Error while executing statement: " + stmtString, x);
               throw new InternalException("Error while executing statement: "
                     + stmtString, x);
            }
            finally
            {
               QueryUtils.closeStatement(stmt);
            }
         }
      }
   }

   public static void prepareDataValueUpdate(
         PersistenceController dpc,
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, ClusterSlotData>>> piToDv,
         Set dpcDelayedCloseSet)
   {
      prepareDataValueUpdate(dpc, piToDv, dpcDelayedCloseSet, false);
   }

   public static void prepareDataValueUpdate(
         PersistenceController dpc,
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, ClusterSlotData>>> piToDv,
         Set dpcDelayedCloseSet, boolean force)
   {
      if (force || dpc.isCreated() || dpc.isModified())
      {
         DataValueBean dataValue = (DataValueBean) dpc.getPersistent();
         IData dataDefinition = dataValue.getData();
         ProcessInstanceState scopePiState
            = dataValue.getProcessInstance().getScopeProcessInstance().getState();
         DataCluster[] clusterSetup = RuntimeSetup.instance().getDataClusterSetup();

         for (DataCluster dataCluster : clusterSetup)
         {
            if(!dataCluster.isEnabledFor(scopePiState))
            {
               continue;
            }

            Pair<Long, DataCluster> key = new Pair(
                  new Long(dataValue.getProcessInstance().getOID()), dataCluster);
            String qualifiedDataId = ModelUtils.getQualifiedId(dataDefinition);

            // there can be several data slots for one data (e.g. in case of structured data)
            for (DataSlot slot : dataCluster.getDataSlots(qualifiedDataId).values())
            {
               addClusterSlotData(dpc, piToDv, dpcDelayedCloseSet, key, slot.getClusterSlotData());
            }

            // search for descriptor slots based on data ID
            for (DescriptorSlot slot : dataCluster.getDescriptorSlotsByDataId(qualifiedDataId).values())
            {
               for (ClusterSlotData slotData : slot.getClusterSlotDatas())
               {
                  if (qualifiedDataId.equals(slotData.getQualifiedDataId()))
                  {
                     addClusterSlotData(dpc, piToDv, dpcDelayedCloseSet, key, slotData);
                     continue;
                  }
               }
            }
         }
      }
   }

   private static void addClusterSlotData(PersistenceController dpc,
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, ClusterSlotData>>> piToDv,
         Set dpcDelayedCloseSet, Pair<Long, DataCluster> key,
         final ClusterSlotData slotData)
   {
      List<Pair<PersistenceController, ClusterSlotData>> dvList = piToDv.get(key);
      if (null == dvList)
      {
         dvList = CollectionUtils.<Pair<PersistenceController, ClusterSlotData>>newArrayList();
         piToDv.put(key, dvList);
      }

      // put only one slot data per slot!!!!
      // TODO: do it by using a map with slot as key?
      for (Pair<PersistenceController, ClusterSlotData> pair : dvList)
      {
         AbstractDataClusterSlot clusterSlot = pair.getSecond().getParent();
         if(clusterSlot.equals(slotData.getParent()))
         {
            // the slot already exists - leave
            return;
         }
      }
      dvList.add(new Pair<PersistenceController, ClusterSlotData>(dpc, slotData));

      if (null != dpcDelayedCloseSet)
      {
         dpcDelayedCloseSet.add(dpc);
      }
   }

   public static void completeDataValueUpdate(
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, ClusterSlotData>>> piToDv,
         Session jdbcSession)
   {
      completeDataValueModification(piToDv, jdbcSession, false);
   }

   public static void prepareDataValueDelete(PersistenceController dpc,
         Map piToDv, Set dpcDelayedCloseSet)
   {
      prepareDataValueUpdate(dpc, piToDv, dpcDelayedCloseSet);
   }

   public static void completeDataValueDelete(
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, ClusterSlotData>>> piToDv,
         Session jdbcSession)
   {
      completeDataValueModification(piToDv, jdbcSession, true);
   }

   public static Map<Long, IData> findAllPrimitiveDataRtOids(AbstractDataClusterSlot slot)
   {
      if (slot instanceof DataSlot)
      {
         DataSlot dataSlot = (DataSlot) slot;
         return dataSlot.getClusterSlotData().findAllPrimitiveDataRtOids();
      }
      else if (slot instanceof DescriptorSlot)
      {
         Map<Long, IData> result = CollectionUtils.newHashMap();
         DescriptorSlot descriptorSlot = (DescriptorSlot) slot;
         for (ClusterSlotData slotData : descriptorSlot.getClusterSlotDatas())
         {
            result.putAll(slotData.findAllPrimitiveDataRtOids());
         }
         return result;
      }

      return Collections.emptyMap();
   }

   public static Map<Long, Pair<IData, String>> findAllStructuredDataRtOids(AbstractDataClusterSlot slot)
   {
      if (slot instanceof DataSlot)
      {
         DataSlot dataSlot = (DataSlot) slot;
         return dataSlot.getClusterSlotData().findAllStructuredDataRtOids();
      }
      else if (slot instanceof DescriptorSlot)
      {
         Map<Long, Pair<IData, String>> result = CollectionUtils.newHashMap();
         DescriptorSlot descriptorSlot = (DescriptorSlot) slot;
         for (ClusterSlotData slotData : descriptorSlot.getClusterSlotDatas())
         {
            result.putAll(slotData.findAllStructuredDataRtOids());
         }
         return result;
      }

      return Collections.emptyMap();
   }

   private static Representation getRepresentationForDataValue(DataValueBean dataValue,
         String attributeName, AccessPathEvaluationContext evaluationContext)
   {
      Object rawValue = new StructuredDataXPathEvaluator().evaluate(dataValue.getData(),
            dataValue.getValue(), attributeName, evaluationContext);
      Representation representation = LargeStringHolderBigDataHandler
            .canonicalizeAtomicDataValue(DataValueBean.getStringValueMaxLength(),
                  rawValue);

      return representation;
   }

   private DataClusterHelper()
   {

   }
}
