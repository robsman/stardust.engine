/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.test.api.setup.TestRtEnvException;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;
import org.h2.Driver;
import org.h2.tools.Server;

/**
 * <p>
 * This class wraps a <a href="http://www.h2database.com">H2 Database Engine</a> instance 
 * and provides convenient methods to
 * <ul>
 *   <li>start, and</li>
 *   <li>stop</li>
 * </ul>
 * the same as well as a means to create the Audit Trail Schema.
 * </p>
 * 
 * <p>
 * When started the database is not only available within the same JVM,
 * but can also be accessed via TCP in order to inspect the DB's content
 * during debugging, for example. Furthermore, the DBMS is running in H2's
 * Oracle compatibility mode, which supports the most features of the Oracle
 * dialect, and using Multi-Version Concurrency Control (MVCC).
 * </p>
 * 
 * <p>
 * The database's <i>url</i>, <i>username</i> and <i>password</i> will be
 * obtained from the file <code>carnot.properties</code>.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: 54310 $
 */
public class H2Server
{
   private static final Log LOG = LogFactory.getLog(H2Server.class);

   private static final String ORACLE_MODE_URL_SUFFIX = ";MODE=ORACLE";
   private static final String MVCC_MODE_URL_SUFFIX = ";MVCC=TRUE";
   
   private static final String DBMS_URL;
   private static final String DB_USER;
   private static final String DB_PASSWORD;
   
   private final Server server;
   private Connection initialConnection;
   
   static
   {
      final Parameters parameters = Parameters.instance();
      DBMS_URL = (String) parameters.getString(SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_URL_SUFFIX);
      DB_USER = (String) parameters.getString(SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX);
      DB_PASSWORD = (String) parameters.getString(SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_PASSWORD_SUFFIX);
   }
   
   public H2Server() throws TestRtEnvException
   {
      try
      {
         server = Server.createTcpServer();
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to create H2 server.";
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.DB_SETUP);
      }
      
      server.setOut(new PrintStream(new LogOutputStream(), true));
   }
   
   public void start() throws TestRtEnvException
   {
      final String errorMsg = "Unable to start H2 server.";
      
      try
      {
         server.start();
         Class.forName(Driver.class.getName());
         final String completeDbmsUrl = DBMS_URL + ORACLE_MODE_URL_SUFFIX + MVCC_MODE_URL_SUFFIX;
         initialConnection = DriverManager.getConnection(completeDbmsUrl, DB_USER, DB_PASSWORD);
      }
      catch (final Exception e)
      {
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.DB_SETUP);
      }
      
      final boolean isRunning = server.isRunning(false);
      if ( !isRunning)
      {
         LOG.error(errorMsg);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.DB_SETUP);
      }
      
   }
   
   public void stop() throws TestRtEnvException
   {
      ensureServerIsRunning();
      
      try
      {
         initialConnection.close();
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to close connection to H2 server.";
         LOG.error(errorMsg);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.DB_TEARDOWN);
      }
      
      try
      {
         server.stop();
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to stop H2 server.";
         LOG.error(errorMsg);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.DB_TEARDOWN);
      }
      
      final boolean isRunning = server.isRunning(false);
      if (isRunning)
      {
         final String errorMsg = "Unable to stop H2 server.";
         LOG.error(errorMsg);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.DB_TEARDOWN);
      }
   }
   
   public void createSchema() throws TestRtEnvException
   {
      ensureServerIsRunning();

      try
      {
         SchemaHelper.createSchema();
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to create schema.";
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, e, TestRtEnvAction.DB_SETUP);
      }
   }
   
   private void ensureServerIsRunning()
   {
      final boolean isRunning = server.isRunning(false);
      if ( !isRunning)
      {
         final String errorMsg = "DB Server is NOT running.";
         LOG.error(errorMsg);
         throw new IllegalStateException(errorMsg);
      }
   }
   
   private static final class LogOutputStream extends OutputStream
   {
      private StringBuilder sb = new StringBuilder();
      
      @Override
      public void write(final int b)
      {
         final char charToWrite = (char) (0x000000FF & b);
         sb.append(charToWrite);
      }
      
      @Override
      public void flush()
      {
         String stringToLog = sb.toString();

         if (stringToLog.isEmpty())
         {
            return;
         }
         if (stringToLog.equals("\r\n") || stringToLog.equals("\n"))
         {
            return;
         }
         if (stringToLog.contains("\r"))
         {
            stringToLog = stringToLog.replaceAll("\r", "");
         }
         if (stringToLog.contains("\n"))
         {
            stringToLog = stringToLog.replaceAll("\n", "");
         }
         
         LOG.info(stringToLog);
         sb = new StringBuilder();
      }
   }
}
