/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc.sequence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.upgrade.utils.sql.LoggingPreparedStatement;

public class ArchiveAuditTrailSequenceGenerator implements SequenceGenerator
{
   private DBDescriptor dbDescriptor;
   private SqlUtils sqlUtils;

   @Override
   public void init(DBDescriptor dbDescriptor, SqlUtils sqlUtils)
   {
      this.dbDescriptor = dbDescriptor;
      this.sqlUtils = sqlUtils;
   }

   @Override
   public long getNextSequence(TypeDescriptor typeDescriptor, Session session)
   {
      long nextSeq = 0;
      StringBuilder selectPkMaxSql = new StringBuilder();
      selectPkMaxSql.append("SELECT MAX(").append(IdentifiablePersistentBean.FIELD__OID);
      selectPkMaxSql.append(") FROM ");

      String schemaName = session.getSchemaName();
      if (StringUtils.isNotEmpty(schemaName))
      {
         selectPkMaxSql.append(schemaName).append(".");
      }

      selectPkMaxSql.append(typeDescriptor.getTableName());

      try
      {
         PreparedStatement selectPkMinStatement = prepareLoggingStatement(
               session.getConnection(), selectPkMaxSql.toString());
         nextSeq = getFirstOid(selectPkMinStatement) + 1;
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }

      return nextSeq;
   }

   public static LoggingPreparedStatement prepareLoggingStatement(Connection connection,
         String sql) throws SQLException
   {
      PreparedStatement ps = connection.prepareStatement(sql);
      return new LoggingPreparedStatement(ps, sql);
   }

   private static long getFirstOid(PreparedStatement ps) throws SQLException
   {
      ResultSet rs = null;
      try
      {
         rs = ps.executeQuery();
         boolean hasNext = rs.next();
         if (hasNext)
         {
            return rs.getLong(1);
         }
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
      return -1;
   }
}
