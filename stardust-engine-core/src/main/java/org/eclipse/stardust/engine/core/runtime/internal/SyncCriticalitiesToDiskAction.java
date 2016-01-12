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
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;

/**
 *
 * @author thomas.wolfram
 *
 */
public class SyncCriticalitiesToDiskAction extends Procedure
{

   private static final Logger trace = LogManager.getLogger(SyncCriticalitiesToDiskAction.class);

   private static final String AI_TABLE_NAME = ActivityInstanceBean.TABLE_NAME;

   private static final String WI_TABLE_NAME = WorkItemBean.TABLE_NAME;

   private static final String AI_F_OID = ActivityInstanceBean.FIELD__OID;

   private static final String AI_F_CRITICALITY = ActivityInstanceBean.FIELD__CRITICALITY;

   private static final String WI_F_CRITICALITY = WorkItemBean.FIELD__CRITICALITY;

   private static final String WI_F_AI = WorkItemBean.FIELD__ACTIVITY_INSTANCE;

   private final Map criticalityMap;

   public SyncCriticalitiesToDiskAction(Map criticalityMap)
   {
      this.criticalityMap = criticalityMap;
   }

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

         List<String> updateList = CollectionUtils.newArrayList();

         String updateAiTableSql = "UPDATE " + schemaPrefix + AI_TABLE_NAME + "   SET "
               + AI_F_CRITICALITY + " = {0}" + " WHERE " + AI_F_OID + " = {1}";

         String updateWiTableSql = "UPDATE " + schemaPrefix + WI_TABLE_NAME + "   SET "
               + WI_F_CRITICALITY + " = {0}" + " WHERE " + WI_F_AI + " = {1}";

         updateList.add(updateAiTableSql);
         updateList.add(updateWiTableSql);

         try
         {
            final Connection con = jdbcSession.getConnection();

            Statement stmt = null;

            try
            {

               int batchSize = 0;
               for (String updateTableSql : updateList)
               {
                  Iterator i = criticalityMap.entrySet().iterator();

                  while (i.hasNext())
                  {
                     batchSize++;
                     Map.Entry entry = (Map.Entry) i.next();

                     final Long aiOid = (Long) entry.getKey();
                     final Double criticality = (Double) entry.getValue();

                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Update criticality for AI <" + aiOid + "> to <"
                              + criticality + ">");
                     }

                     // Execute update for all defined tables
                     if (jdbcSession.isUsingPreparedStatements(ActivityInstanceBean.class))
                     {

                        if (null == stmt)
                        {
                           stmt = con.prepareStatement(MessageFormat.format(
                                 updateTableSql, new Object[] {"?", "?"}));
                        }
                        ((PreparedStatement) stmt).setDouble(1, criticality);
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
                                    DmlManager.getSQLValue(Double.TYPE, new Double(
                                          criticality), jdbcSession.getDBDescriptor()),
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
            }
            finally
            {
               QueryUtils.closeStatement(stmt);
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
