/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.spi.monitoring;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * This class provides a singleton to store information about
 * method execution during SPI Execution
 * @author Thomas.Wolfram
 *
 */
public class TestPartitionMonitorLog
{

   private static TestPartitionMonitorLog instance = new TestPartitionMonitorLog();

   private List<LogEntry> logList = CollectionUtils.newArrayList();

   private TestPartitionMonitorLog()
   {

   }

   public void addLogEntry(String methodName, String messageContent)
   {
      logList.add(new LogEntry(methodName, messageContent));
   }

   public List<LogEntry> getLogList()
   {
      return this.logList;
   }

   public LogEntry findLogEntryForMethod(String methodName)
   {
      for (LogEntry entry : this.logList)
      {
         if (entry.getMethodName().equalsIgnoreCase(methodName))
         {
            return entry;
         }
      }

      return null;
   }

   public static TestPartitionMonitorLog getInstance()
   {
      return instance;
   }

   public class LogEntry
   {

      private String methodName;

      private String messageContent;

      public LogEntry(String methodName, String messageContent)
      {
         this.messageContent = messageContent;
         this.methodName = methodName;
      }

      public String getMethodName()
      {
         return methodName;
      }

      public String getMessageContent()
      {
         return messageContent;
      }


   }

}
