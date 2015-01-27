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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LocalOIDGenerator implements OIDGenerator
{
   public static final Logger trace = LogManager.getLogger(LocalOIDGenerator.class);

   private DataSource dataSource;

   public LocalOIDGenerator(String name)
   {
      dataSource = (DataSource) Parameters.instance().get("jdbc/" + name + ".DataSource");
   }

   public long getNextOID(String schemaName, String sequenceName)
   {
      Connection connection = null;
      try
      {
         connection = dataSource.getConnection();

         Statement statement = null;
         ResultSet resultSet = null;
         long nextOID;
         try
         {
            statement = connection.createStatement();
            resultSet = StatementClosingResultSet.createManagedResultSet(statement,
                  statement.executeQuery(
                          "SELECT value"
                        + "  FROM " + DDLManager.getQualifiedName(schemaName, DBDescriptor.SEQUENCE_HELPER_TABLE_NAME)
                        + " WHERE name = '" + sequenceName + "'"));

            resultSet.next();

            nextOID = resultSet.getLong(1) + 1;
         }
         finally
         {
            QueryUtils.closeResultSet(resultSet);
         }

         Statement updateStatement = null;
         try
         {
            updateStatement = connection.createStatement();
            updateStatement.executeUpdate(
                    "UPDATE " + DDLManager.getQualifiedName(schemaName, DBDescriptor.SEQUENCE_HELPER_TABLE_NAME)
                  + "   SET value = " + nextOID
                  + " WHERE name = '" + sequenceName + "'");

            connection.commit();
         }
         finally
         {
            QueryUtils.closeStatement(updateStatement);
         }

         return nextOID;
      }
      catch (Exception x)
      {
         throw new InternalException("Failed to increment sequence.", x);
      }
      finally
      {
         if (null != connection)
         {
            try
            {
               connection.close();
            }
            catch (SQLException e)
            {
               trace.warn("", e);
            }
         }
      }
   }
}
