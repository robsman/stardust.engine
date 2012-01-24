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
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Procedure;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.ClosableIterator;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.UserSessionBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SynchUserSessionToDiskAction extends Procedure
{
   private static final Logger trace = LogManager.getLogger(SynchUserSessionToDiskAction.class);

   private static final String US_TABLE_NAME = UserSessionBean.TABLE_NAME;
   private static final String US_F_USER = UserSessionBean.FIELD__USER;
   private static final String US_F_LAST_MODIFICATION_TIME = UserSessionBean.FIELD__LAST_MODIFICATION_TIME;
   private static final String US_F_EXPIRATION_TIME = UserSessionBean.FIELD__EXPIRATION_TIME;

   private final Map/*<Long, Date>*/ timestamps;

   public SynchUserSessionToDiskAction(Map timestamps)
   {
      this.timestamps = timestamps;
   }

   protected void invoke()
   {
      final SessionManager sessionManager = SessionManager.instance();

      // TODO retrieve from parameters
      final int maxBatchSize = 100;

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         // optimize updates via JDBC batches
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;

         String schemaPrefix = StringUtils.isEmpty(jdbcSession.getSchemaName())
               ? ""
               : jdbcSession.getSchemaName() + ".";

         String updateSql
               = "UPDATE " + schemaPrefix + US_TABLE_NAME
               + "   SET " + US_F_LAST_MODIFICATION_TIME + " = {0},"
               + "       " + US_F_EXPIRATION_TIME + " = {1}"
               + " WHERE " + US_F_USER + " = {2}"
               + "   AND " + US_F_LAST_MODIFICATION_TIME + " < " + US_F_EXPIRATION_TIME
               + "   AND {3} BETWEEN " + US_F_LAST_MODIFICATION_TIME + " AND " + US_F_EXPIRATION_TIME;

         try
         {
            final Connection con = jdbcSession.getConnection();

            Statement stmt = null;

            try
            {
               int batchSize = 0;
               for (Iterator i = timestamps.entrySet().iterator(); i.hasNext();)
               {
                  Map.Entry entry = (Map.Entry) i.next();

                  final Long userOid = (Long) entry.getKey();
                  final long lastModificationTime = ((Date) entry.getValue()).getTime();

                  final long expirationTime = sessionManager.getExpirationTime(lastModificationTime);

                  if (jdbcSession.isUsingPreparedStatements(UserSessionBean.class))
                  {
                     if (null == stmt)
                     {
                        stmt = con.prepareStatement(MessageFormat.format(updateSql,
                              new Object[] {"?", "?", "?", "?"}));
                     }

                     ((PreparedStatement) stmt).setLong(1, lastModificationTime);
                     ((PreparedStatement) stmt).setLong(2, expirationTime);
                     ((PreparedStatement) stmt).setLong(3, userOid.longValue());
                     ((PreparedStatement) stmt).setLong(4, lastModificationTime);
                     ((PreparedStatement) stmt).addBatch();
                  }
                  else
                  {
                     if (null == stmt)
                     {
                        stmt = con.createStatement();
                     }

                     stmt.addBatch(MessageFormat.format(updateSql, new Object[] {
                           DmlManager.getSQLValue(Long.TYPE, new Long(lastModificationTime), jdbcSession.getDBDescriptor()),
                           DmlManager.getSQLValue(Long.TYPE, new Long(expirationTime), jdbcSession.getDBDescriptor()),
                           DmlManager.getSQLValue(Long.TYPE, userOid, jdbcSession.getDBDescriptor()),
                           DmlManager.getSQLValue(Long.TYPE, new Long(lastModificationTime), jdbcSession.getDBDescriptor())}));
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
            }
         }
         catch (SQLException e)
         {
            trace.warn("Failed writing user session updates to the audit trail.", e);
         }
      }
      else
      {
         // traditionally update bean by bean

         for (Iterator i = timestamps.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();

            final Long userOid = (Long) entry.getKey();
            final long lastModificationTime = ((Date) entry.getValue()).getTime();

            final long expirationTime = sessionManager.getExpirationTime(lastModificationTime);

            ClosableIterator openSessions = session.getIterator(
                  UserSessionBean.class,
                  QueryExtension.where(Predicates.isEqual(
                              UserSessionBean.FR__USER, userOid.longValue())));
            try
            {
               while (openSessions.hasNext())
               {
                  UserSessionBean openSession = (UserSessionBean) openSessions.next();

                  // check if session was already terminated
                  if (openSession.getLastModificationTime().getTime() <= lastModificationTime &&
                        openSession.getExpirationTime().getTime() >= lastModificationTime &&
                        openSession.getLastModificationTime().before(openSession.getExpirationTime()))
                  {
                     openSession.setLastModificationTime(new Date(lastModificationTime));
                     openSession.setExpirationTime(new Date(expirationTime));
                  }
               }
            }
            finally
            {
               openSessions.close();
            }
         }
      }
   }
}
