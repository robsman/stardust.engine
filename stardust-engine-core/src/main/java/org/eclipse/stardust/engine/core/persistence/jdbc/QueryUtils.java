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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 *
 */
public class QueryUtils
{
   private static final Logger trace = LogManager.getLogger(QueryUtils.class);

   /**
    * Closes the passed statement
    *
    * @param connection
    */
   public static void closeConnection(Connection connection)
   {
      if (connection != null)
      {
         try
         {
            connection.close();
         }
         catch (SQLException e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Ignoring error during JDBC connection close: "
                     + e.getMessage(), e);
            }
         }
      }
   }

   /**
    * Closes the passed statement
    *
    * @param statement
    */
   public static void closeStatement(Statement statement)
   {
      if (statement != null)
      {
         try
         {
            statement.close();
         }
         catch (SQLException e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Ignoring error during JDBC statement close: "
                     + e.getMessage(), e);
            }
         }
      }
   }

   /**
    * Closes the result, but not the statement it was generated with.
    */
   public static void closeResultSet(ResultSet resultSet)
   {
      if (resultSet != null)
      {
         try
         {
            resultSet.close();
         }
         catch (SQLException e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Ignoring error during JDBC result set close: "
                     + e.getMessage(), e);
            }
         }
      }
   }

   /**
    * Closes the result and the statement it was generated with.
    */
   public static void closeStatementAndResultSet(Statement statement, ResultSet resultSet)
   {
         closeResultSet(resultSet);
         closeStatement(statement);
   }

   /**
    * Disconnects session
    *
    * @param session The session object
    */
   public static void disconnectSession(Session session)
   {
      if (session != null)
      {
         try
         {
            session.disconnect();
         }
         catch (SQLException e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug(
                     "Ignoring error during disconnect of Session: " + e.getMessage(), e);
            }
         }
      }
   }
}
