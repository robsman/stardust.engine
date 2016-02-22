/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Procedure;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserSessionBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class SyncBenchmarksToDiskAction extends Procedure
{
   private static final Logger trace = LogManager.getLogger(SynchUserSessionToDiskAction.class);

   private static final String AI_TABLE_NAME = ActivityInstanceBean.TABLE_NAME;

   private static final String WI_TABLE_NAME = WorkItemBean.TABLE_NAME;

   private static final String PI_TABLE_NAME = ProcessInstanceBean.TABLE_NAME;

   private static final String AI_F_OID = ActivityInstanceBean.FIELD__OID;

   private static final String AI_F_BENCHMARK_VALUE = ActivityInstanceBean.FIELD__BENCHMAKRK_VALUE;

   private static final String WI_F_BENCHMARK_VALUE = WorkItemBean.FIELD__BENCHMARK_VALUE;

   private static final String WI_F_AI = WorkItemBean.FIELD__ACTIVITY_INSTANCE;

   private static final String PI_F_OID = ProcessInstanceBean.FIELD__OID;

   private static final String PI_F_BENCHMARK_VALUE = ProcessInstanceBean.FIELD__BENCHMARK_VALUE;

   private final Map piUpdateMap;

   private final Map aiUpdateMap;

   public SyncBenchmarksToDiskAction(Map<Long, Integer> piUpdateMap,
         Map<Long, Integer> aiUpdateMap)
   {
      this.piUpdateMap = piUpdateMap;
      this.aiUpdateMap = aiUpdateMap;
   }

   @Override
   protected void invoke()
   {
      final int maxBatchSize = 100;

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;

         String schemaPrefix = StringUtils.isEmpty(jdbcSession.getSchemaName())
               ? ""
               : jdbcSession.getSchemaName() + ".";

         List<String> updateAiTableList = CollectionUtils.newArrayList();

         String updatePiTableSql = "UPDATE " + schemaPrefix + PI_TABLE_NAME + "   SET "
               + PI_F_BENCHMARK_VALUE + " = {0}" + " WHERE " + PI_F_OID + "= {1}";

         String updateAiTableSql = "UPDATE " + schemaPrefix + AI_TABLE_NAME + "   SET "
               + AI_F_BENCHMARK_VALUE + " = {0}" + " WHERE " + AI_F_OID + " = {1}";

         String updateWiTableSql = "UPDATE " + schemaPrefix + WI_TABLE_NAME + "   SET "
               + WI_F_BENCHMARK_VALUE + " = {0}" + " WHERE " + WI_F_AI + " = {1}";

         updateAiTableList.add(updateAiTableSql);
         updateAiTableList.add(updateWiTableSql);

         try
         {
            final Connection con = jdbcSession.getConnection();

            Statement stmt = null;

            try
            {

               // Update all AI Instances
               int batchSize = 0;
               for (String updateTableSql : updateAiTableList)
               {

                  Iterator i = aiUpdateMap.entrySet().iterator();

                  while (i.hasNext())
                  {
                     batchSize++ ;

                     Map.Entry entry = (Map.Entry) i.next();

                     final Long aiOid = (Long) entry.getKey();
                     final int benchmarkValue = (Integer) entry.getValue();

                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Update benchmark value for AI <" + aiOid + "> to <"
                              + benchmarkValue + ">");
                     }

                     // Execute update for all defined tables
                     if (jdbcSession.isUsingPreparedStatements(UserSessionBean.class))
                     {
                        if (null == stmt)
                        {
                           stmt = con.prepareStatement(MessageFormat.format(
                                 updateTableSql, new Object[] {"?", "?"}));
                        }
                        ((PreparedStatement) stmt).setInt(1, benchmarkValue);
                        ((PreparedStatement) stmt).setLong(2, aiOid);
                        ((PreparedStatement) stmt).addBatch();
                     }
                     else
                     {
                        if (null == stmt)
                        {
                           stmt = con.createStatement();
                        }

                        stmt.addBatch(MessageFormat.format(
                              updateTableSql,
                              new Object[] {
                                    DmlManager.getSQLValue(Integer.TYPE, new Integer(
                                          benchmarkValue), jdbcSession.getDBDescriptor()),
                                    DmlManager.getSQLValue(Long.TYPE, new Long(aiOid),
                                          jdbcSession.getDBDescriptor())}));
                     }

                     if ((batchSize >= maxBatchSize) || !i.hasNext())
                     {
                        stmt.executeBatch();
                        QueryUtils.closeStatement(stmt);

                        stmt = null;
                        batchSize = 0;
                     }
                  }
               }

               // Update all PI Instances
               for (Iterator i = piUpdateMap.entrySet().iterator(); i.hasNext();)
               {

                  Map.Entry entry = (Map.Entry) i.next();

                  final Long piOid = (Long) entry.getKey();
                  final int benchmarkValue = (Integer) entry.getValue();

                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Update benchmark value for PI <" + piOid + "> to <"
                           + benchmarkValue + ">");
                  }

                  if (jdbcSession.isUsingPreparedStatements(UserSessionBean.class))
                  {
                     if (null == stmt)
                     {
                        stmt = con.prepareStatement(MessageFormat.format(
                              updatePiTableSql, new Object[] {"?", "?"}));
                     }
                     ((PreparedStatement) stmt).setInt(1, benchmarkValue);
                     ((PreparedStatement) stmt).setLong(2, piOid);
                     ((PreparedStatement) stmt).addBatch();
                  }
                  else
                  {
                     if (null == stmt)
                     {
                        stmt = con.createStatement();
                     }

                     stmt.addBatch(MessageFormat.format(
                           updatePiTableSql,
                           new Object[] {
                                 DmlManager.getSQLValue(Integer.TYPE, new Integer(
                                       benchmarkValue), jdbcSession.getDBDescriptor()),
                                 DmlManager.getSQLValue(Long.TYPE, new Long(piOid),
                                       jdbcSession.getDBDescriptor())}));
                  }

                  if ((batchSize >= maxBatchSize) || !i.hasNext())
                  {
                     stmt.executeBatch();
                     QueryUtils.closeStatement(stmt);

                     stmt = null;
                     batchSize = 0;
                  }

               }
            }
            finally
            {
               QueryUtils.closeStatement(stmt);
               // con.commit();
               // con.close();
            }
         }
         catch (SQLException e)
         {
            throw new PublicException(
                  BpmRuntimeError.ATDB_FAILED_TO_UPDATE_CRITICALITIES_IN_AUDITTRAIL.raise(),
                  e);
         }
      }
   }

}
