package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Date;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;

public class DaemonExecutionLog
{
   private TreeSet<ExecutionLogEntry> logEntries;
   
   public DaemonExecutionLog()
   {
      this.logEntries = CollectionUtils.newTreeSet(); 
   }
   
   public void log(String message)
   {
      ExecutionLogEntry logEntry = new ExecutionLogEntry(new Date(), message);
      logEntries.add(logEntry);
   }
   
   public TreeSet<ExecutionLogEntry> getLogEntries()
   {
      return this.logEntries;
   }
   
   public void reset()
   {
      if (this.logEntries != null)
      {
         this.logEntries.clear();
      }
   }
   
   public class ExecutionLogEntry implements Comparable<ExecutionLogEntry>
   {
      private Date timestamp;
      
      private String message;
      
      public ExecutionLogEntry(Date timestamp, String message)
      {
         this.timestamp = timestamp;
         this.message = message;
      }
      
      public Date getTimestamp()
      {
         return this.timestamp;
      }
      
      public String getMessage()
      {
         return this.message;
      }

      @Override
      public int compareTo(ExecutionLogEntry o)
      {
         if (this.timestamp.getTime() < o.getTimestamp().getTime())
         {
            return -1;
         }
         if (this.timestamp.getTime() >= o.getTimestamp().getTime())
         {
            return 1;
         }         
         return 0;
      }
   }
}
