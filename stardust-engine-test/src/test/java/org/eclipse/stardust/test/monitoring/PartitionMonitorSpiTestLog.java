package org.eclipse.stardust.test.monitoring;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * This class provides a singleton to store information about 
 * method execution during SPI Execution
 * @author Thomas.Wolfram
 *
 */
public class PartitionMonitorSpiTestLog
{

   private static PartitionMonitorSpiTestLog instance = new PartitionMonitorSpiTestLog();

   private List<LogEntry> logList = CollectionUtils.newArrayList();

   private PartitionMonitorSpiTestLog()
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

   public static PartitionMonitorSpiTestLog getInstance()
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
