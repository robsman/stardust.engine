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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler.Representation;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;


public class DataClusterHelper
{
   private static final Logger trace = LogManager.getLogger(DataClusterHelper.class);
   
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
         DataCluster[] clusterSetup = RuntimeSetup.instance().getDataClusterSetup();

         for (DataCluster dataCluster : clusterSetup)
         {
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
