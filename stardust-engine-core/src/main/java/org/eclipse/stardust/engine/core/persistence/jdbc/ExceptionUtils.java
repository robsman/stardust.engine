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

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.TransactionFreezedException;
import org.eclipse.stardust.common.error.UniqueConstraintViolatedException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class ExceptionUtils
{
   private static final Logger trace = LogManager.getLogger(ExceptionUtils.class);

   /*
    * DB2/Derby:
    *
    * 57014 - Processing was canceled as requested.
    * 57033 - deadlock or timeout w/o automatic rollback (DB2). 

    * 40001 - deadlock or timeout w/ automatic rollback
    * 40506 - The current transaction was rolled back because of an SQL error.

    * 51021 - SQL statements cannot be executed until the application process executes a rollback operation.
    * 
    * Sybase:
    * 23000 - Attempt to insert duplicate key row in object 'xxx' with unique index 'xxx_idx'.
    *  
    * foreign key violation: 23503
    * check constraint violation: 23513
    * duplicate value violating unique index or primary key constraint: 23505
    * duplicate key or integrity constraint violating: 23000 
    * truncation error: 22001
    */
   
   public static ApplicationException transformException(DBDescriptor dbDescriptor,
         SQLException cause)
   {
      return transformException(dbDescriptor, cause, trace);
   }

   public static ApplicationException transformException(DBDescriptor dbDescriptor,
         SQLException cause, Logger logger)
   {
      ApplicationException result = null;

      String sqlState = cause.getSQLState();
      DBMSKey dbmsKey = dbDescriptor.getDbmsKey();

      if ("57033".equals(sqlState))
      {
         logger.warn("Caught deadlock.");
         result = new TransactionFreezedException(dbmsKey
               + " session needs to be rolled back as soon as possible.", false, cause);
      }
      else if ("40001".equals(sqlState))
      {
         logger.warn("Caught deadlock.");
         result = new TransactionFreezedException(dbmsKey + " session rolled back.",
               true, cause);
      }
      else if ("23505".equals(sqlState))
      {
         result = createUniqueConstraintViolationException(cause, logger);
      }
      else if ("23000".equals(sqlState))
      {
         logger.warn("Unique constraint violated.");
         result = new UniqueConstraintViolatedException("Unique constraint violated.",
               cause);
      }
      else if (DBMSKey.SYBASE == dbmsKey)
      {
         SQLException currentException = cause;
         while (null != currentException)
         {
            if ("23000".equals(currentException.getSQLState()))
            {
               result = createUniqueConstraintViolationException(cause, logger);
               break;
            }

            currentException = currentException.getNextException();
         }
      }

      return result;
   }
   
   public static UniqueConstraintViolatedException createUniqueConstraintViolationException(
         SQLException cause, Logger logger)
   {
      logger.warn("Unique constraint violated.");
      return new UniqueConstraintViolatedException("Unique constraint violated.", cause);
   }

   public static void logAllBatchExceptions(SQLException exception)
   {
      if (exception != null && exception instanceof BatchUpdateException)
      {
         BatchUpdateException batchUpdateException = (BatchUpdateException)exception;
         trace.error("Got batch insert/update exception. Batch insert/update counts: "+updateCountsToString(batchUpdateException.getUpdateCounts()));
         int exceptionNumber = 0; 
         for (SQLException embeddedException = batchUpdateException.getNextException(); null != embeddedException; exceptionNumber++)
         {
            trace.error("Batch exception #"+exceptionNumber, embeddedException);
            embeddedException = embeddedException.getNextException();
         }
      }

   }
   
   public static String updateCountsToString(int[] a)
   {
      if (a == null)
         return "null";
      if (a.length == 0)
         return "[]";

      StringBuffer buf = new StringBuffer();
      buf.append('[');
      appendUpdateCount(buf, a[0]);

      for (int i = 1; i < a.length; i++ )
      {
         buf.append(", ");
         appendUpdateCount(buf, a[i]);
      }

      buf.append("]");
      return buf.toString();
   }

   private static void appendUpdateCount(StringBuffer buf, int updateCount)
   {
      if (updateCount == Statement.SUCCESS_NO_INFO)
      {
         // success but that the number of rows affected is unknown 
         buf.append("SUCCESS_NO_INFO");
      }
      else if (updateCount == Statement.EXECUTE_FAILED)
      {
         // failed to execute successfully and occurs only if a driver 
         // continues to process commands after a command fails 
         buf.append("EXECUTE_FAILED");
      }
      else
      {
         // regular update count or unknown error code
         buf.append(updateCount);
      }
   }

}
