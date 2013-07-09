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
package org.eclipse.stardust.engine.core.runtime.setup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
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
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler;
import org.eclipse.stardust.engine.core.runtime.beans.PropertyPersistor;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler.Representation;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataClusterMetaInfoRetriever;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterSetupAnalyzer.DataClusterSynchronizationInfo;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;


public class DataClusterHelper
{
   private static final Logger trace = LogManager.getLogger(DataClusterHelper.class);

   
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
   
   private static DataClusterSynchronizationInfo getDataClusterSynchronizationInfo(
         DataCluster clusterToSynchronize, 
         IProcessInstance scopeProcessInstance)
   {
      Map<DataClusterKey, Set<DataSlot>> clusterToSlotMapping = new HashMap<DataClusterKey, Set<DataSlot>>();
      Map<DataSlotKey, Set<DataSlotFieldInfo>> slotToColumnMapping = new HashMap<DataSlotKey, Set<DataSlotFieldInfo>>();

      DataClusterKey clusterKey = new DataClusterKey(clusterToSynchronize);
      for (DataSlot ds : clusterToSynchronize.getAllSlots())
      {
         DataSlotKey slotKey = new DataSlotKey(ds);
         Set<DataSlot> slots = clusterToSlotMapping.get(clusterKey);
         if (slots == null)
         {
            slots = new HashSet<DataSlot>();
            clusterToSlotMapping.put(clusterKey, slots);
         }
         slots.add(ds);

         List<DataSlotFieldInfo> slotColumnFields = DataClusterMetaInfoRetriever
               .getDataSlotFields(ds);
         slotToColumnMapping.put(slotKey, new HashSet<DataSlotFieldInfo>(slotColumnFields));
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
         ddlManager.synchronizeDataCluster(false, synchronizationInfo, session.getConnection(), schemaName, null, null);
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
            long startTime = System.currentTimeMillis();
            
            ResultSet rs = stmt.executeQuery(sqlString);
            rs.next();
            int count = rs.getInt(1);
            if(count > 0)
            {
               return true;
            }
            
            long stopTime = System.currentTimeMillis();
            sessionImpl.monitorSqlExecution(sqlString, startTime, stopTime);
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
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate(sqlString);
            long stopTime = System.currentTimeMillis();
            sessionImpl.monitorSqlExecution(sqlString, startTime, stopTime);
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
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, DataSlot>>> piToDv,
         Session jdbcSession, boolean isDeleteModification)
   {
      for (Pair<Long, DataCluster> key : piToDv.keySet())
      {
         List<Pair<PersistenceController, DataSlot>> slotValues = piToDv.get(key);
         
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
            for (Pair<PersistenceController, DataSlot> compoundValue : slotValues)
            {
               PersistenceController dpc = compoundValue.getFirst();
               DataValueBean dataValue = (DataValueBean) dpc.getPersistent();
               DataSlot dataSlot = compoundValue.getSecond();
               
               buffer.append(appendToken);
               appendToken = ",";
               
               buffer.append(dataSlot.getOidColumn()).append("=?,");
               buffer.append(dataSlot.getTypeColumn()).append("=?");
               
               if (StringUtils.isNotEmpty(dataSlot.getSValueColumn()))
               {
                  buffer.append(',').append(dataSlot.getSValueColumn()).append("=?");
               }
               if (StringUtils.isNotEmpty(dataSlot.getNValueColumn()))
               {
                  buffer.append(',').append(dataSlot.getNValueColumn()).append("=?");
               }
               
               bindValueList.add(new Pair(Long.class, Long.valueOf(dataValue.getOID())));
               
               String dataTypeId = dataValue.getData().getType().getId();
               if ( !StringUtils.isEmpty(dataSlot.getSValueColumn()))
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
                     if (StringUtils.isNotEmpty(dataSlot.getDValueColumn()))
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
                     if (StringUtils.isNotEmpty(dataSlot.getDValueColumn()))
                     {
                        dValue = dataValue.getDoubleValue();
                        upadteDValue = true;
                     }
                  }
                  bindValueList.add(new Pair(String.class, sValue));
                  
                  if (upadteDValue)
                  {
                     buffer.append(',').append(dataSlot.getDValueColumn()).append("=?");
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
               
               long startTime = System.currentTimeMillis();
               stmt.executeUpdate();
               jdbcSession.monitorSqlExecution(stmtString, startTime, System
                     .currentTimeMillis());
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
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, DataSlot>>> piToDv,
         Set dpcDelayedCloseSet)
   {
      prepareDataValueUpdate(dpc, piToDv, dpcDelayedCloseSet, false);
   }
   
   public static void prepareDataValueUpdate(
         PersistenceController dpc,
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, DataSlot>>> piToDv,
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
            
            // there can be several data slots for one data (e.g. in case of structured data)
            String qualifiedDataId = ModelUtils.getQualifiedId(dataDefinition);
            for (DataSlot slot : dataCluster.getSlots(qualifiedDataId).values())
            {
               Pair<Long, DataCluster> key = new Pair(new Long(dataValue
                     .getProcessInstance().getOID()), dataCluster);
               List<Pair<PersistenceController, DataSlot>> dvList = piToDv.get(key);
               if (null == dvList)
               {
                  dvList = CollectionUtils.newArrayList();
                  piToDv.put(key, dvList);
               }

               dvList.add(new Pair(dpc, slot));

               if (null != dpcDelayedCloseSet)
               {
                  dpcDelayedCloseSet.add(dpc);
               }
            }
         }
      }
   }
   
   public static void completeDataValueUpdate(
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, DataSlot>>> piToDv,
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
         Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, DataSlot>>> piToDv,
         Session jdbcSession)
   {
      completeDataValueModification(piToDv, jdbcSession, true);
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
