package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportIndex implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String archiveManagerId;

   private String dateFormat;
   
   private boolean isDump;
   
   private Map<ExportProcess, List<ExportProcess>> processes;

   public ExportIndex()
   {
   }

   public ExportIndex(String archiveManagerId, String dateFormat, boolean isDump)
   {
      this(archiveManagerId, dateFormat, new HashMap<ExportProcess, List<ExportProcess>>(), isDump);
   }

   public ExportIndex(String archiveManagerId, String dateFormat, 
         Map<ExportProcess, List<ExportProcess>> rootProcessToSubProcesses, boolean isDump)
   {
      this.archiveManagerId = archiveManagerId;
      this.processes = rootProcessToSubProcesses;
      this.isDump = isDump;
      this.dateFormat = dateFormat;
   }

   public boolean contains(Long processInstanceOid)
   {
      for (ExportProcess rootProcess : getRootProcessToSubProcesses().keySet())
      {
         if (rootProcess.getOid() == processInstanceOid)
         {
            return true;
         }
         List<ExportProcess> subProcesses = getRootProcessToSubProcesses().get(
               rootProcess);
         for (ExportProcess subProcess : subProcesses)
         {
            if (subProcess.getOid() == processInstanceOid)
            {
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Returns true if any of the processes in the ExportIndex contains any of the specified descriptors
    * @param descriptors
    * @return
    */
   public boolean contains( Map<String, Object> descriptors)
   {
      if (descriptors == null || descriptors.isEmpty())
      {
         return true;
      }
      for (ExportProcess rootProcess : getRootProcessToSubProcesses().keySet())
      {
         if (descriptorMatch(rootProcess.getDescriptors(), descriptors))
         {
            return true;
         }
         List<ExportProcess> subProcesses = getRootProcessToSubProcesses().get(
               rootProcess);
         for (ExportProcess subProcess : subProcesses)
         {
            if (descriptorMatch(subProcess.getDescriptors(), descriptors))
            {
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Returns map of RootProcessToSubProcesses where the root process or one of it's subprocesses matches one of the descriptors
    * @param descriptors
    * @param toDate 
    * @param fromDate 
    * @return
    */
   public Map<ExportProcess, List<ExportProcess>> getProcesses(Map<String, Object> descriptors, List<Long> processInstanceOids, 
         Date startDate, Date endDate)
   {
      if ((descriptors == null || descriptors.isEmpty()) && (processInstanceOids == null && (startDate == null || endDate == null)))
      {
         return getRootProcessToSubProcesses();
      }
      DateFormat df = new SimpleDateFormat(dateFormat);
      Map<ExportProcess, List<ExportProcess>> result = new HashMap<ExportProcess, List<ExportProcess>>();
      for (ExportProcess rootProcess : getRootProcessToSubProcesses().keySet())
      {
         if (dateMatch(df, rootProcess, startDate, endDate) 
               && processInstanceOidMatch(processInstanceOids, rootProcess.getOid()) 
               && descriptorMatch(rootProcess.getDescriptors(), descriptors))
         {
            result.put(rootProcess, getRootProcessToSubProcesses().get(rootProcess));
         }
         else
         {
            List<ExportProcess> subProcesses = getRootProcessToSubProcesses().get(
                  rootProcess);
            for (ExportProcess subProcess : subProcesses)
            {
               if (dateMatch(df, subProcess, startDate, endDate) 
                     && processInstanceOidMatch(processInstanceOids, subProcess.getOid()) 
                     && descriptorMatch(subProcess.getDescriptors(), descriptors))
               {
                  result.put(rootProcess, getRootProcessToSubProcesses().get(rootProcess));
                  break;
               }
            }
         }
      }
      return result;
   }

   private boolean dateMatch(DateFormat df, ExportProcess process, Date startDate, Date endDate)
   {
      if (startDate == null || endDate == null)
      {
         return true;
      }
      try
      {
         Date processStart = df.parse(process.getStartDate());
         Date processEnd = df.parse(process.getEndDate());
         return (startDate.compareTo(processStart) < 1)
               && (endDate.compareTo(processEnd) > -1);
      }
      catch (ParseException e)
      {
         throw new IllegalStateException("Failed to parse date in archive " + e.getMessage(), e);
      }
   }
   
   private boolean processInstanceOidMatch(List<Long> processInstanceOids, Long oid)
   {
      if (processInstanceOids == null)
      {
         return true;
      }
      return processInstanceOids.contains(oid);
   }
   
   private boolean descriptorMatch(Map<String, String> descriptors, 
         Map<String, Object> searchDescriptors)
   {
      if (searchDescriptors == null)
      {
         return true;
      }
      for (String key : searchDescriptors.keySet())
      {
         if (descriptors.containsKey(key))
         {
            Object value = searchDescriptors.get(key);
            String stringValue;
            if (value instanceof Date)
            {
               DateFormat df = new SimpleDateFormat(dateFormat);
               stringValue = df.format((Date) value);
            }
            else
            {
               stringValue = value.toString();
            }
                  
            if (descriptors.get(key).equals(stringValue))
            {
               return true;
            }
         }
      }
      return false;
   }

   public Map<ExportProcess, List<ExportProcess>> getRootProcessToSubProcesses()
   {
      return processes;
   }

   public String getArchiveManagerId()
   {
      return archiveManagerId;
   }
   

   /**
    * Returns dateformat used for descri
    * @return
    */
   public String getDateFormat()
   {
      return dateFormat;
   }

   public void setRootProcessToSubProcesses(
         Map<ExportProcess, List<ExportProcess>> rootProcessToSubProcesses)
   {
      this.processes = rootProcessToSubProcesses;
   }

   public void setArchiveManagerId(String archiveManagerId)
   {
      this.archiveManagerId = archiveManagerId;
   }

   public boolean isDump()
   {
      return isDump;
   }

}
