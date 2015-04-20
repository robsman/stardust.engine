package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExportIndex implements Serializable
{

   public static final String FIELD_START_DATE = "startDate";
   public static final String FIELD_END_DATE = "endDate";
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String archiveManagerId;

   private String dateFormat;
   
   private boolean isDump;

   // map of root oid to subprocess process oids.
   // if a root process has no subprocesses the sublist will be empty, it will not be null
   private Map<Long, List<Long>> rootProcessToSubProcesses;
   
   private Map<Long, String> oidsToUuids;

   // map of fields to values. values is map of values to processInstanceOids that contains them
   // e.g. fields.get("name").get("John") returns oids of all processes that has a field "name" with value "John"
   private Map<String, Map<String, List<Long>>> fields;

   public ExportIndex()
   {
   }

   public ExportIndex(String archiveManagerId, String dateFormat, boolean isDump)
   {
      this(archiveManagerId, dateFormat, new HashMap<Long, List<Long>>(), 
            new HashMap<String, Map<String, List<Long>>>(), new HashMap<Long, String>(), isDump);
   }

   public ExportIndex(String archiveManagerId, String dateFormat, 
         Map<Long, List<Long>> processes,
         Map<String, Map<String, List<Long>>> fields, Map<Long, String> oidsToUuids, boolean isDump)
   {
      this.archiveManagerId = archiveManagerId;
      this.rootProcessToSubProcesses = processes;
      this.fields = fields;
      this.isDump = isDump;
      this.dateFormat = dateFormat;
      this.oidsToUuids = oidsToUuids;
   }

   public boolean contains(Long processInstanceOid)
   {
      return oidsToUuids.get(processInstanceOid) != null;
   }
   
   /**
    * Returns true if any of the processes in the ExportIndex contains any of the specified descriptors
    * @param descriptors
    * @return
    */
   public boolean contains(Map<String, Object> descriptors)
   {
      if (descriptors == null || descriptors.isEmpty())
      {
         return true;
      }
      for (String key : descriptors.keySet())
      {
         if (!descriptorMatch(key, descriptors.get(key)).isEmpty())
         {
            return true;
         }
      }
      return false;
   }
   
   private List<Long> descriptorMatch(String key, Object value)
   {
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
      Map<String, List<Long>> valuesToProcesses = fields.get(key); 
      List<Long> result;
      if (valuesToProcesses != null)
      {
         result = valuesToProcesses.get(stringValue);
      }
      else
      {
         result = null;
      }
      if (result == null)
      {
         result = new ArrayList<Long>();
      }
      return result;
   }

   /**
    * Returns map of RootProcessToSubProcesses where the root process or one of it's subprocesses matches one of the descriptors, 
    * processInstanceOids or started after startDate and ended before endDate
    * @param descriptors
    * @param processInstanceOids
    * @param startDate 
    * @param endDate 
    * @return
    */
   public Set<Long> getProcesses(Map<String, Object> descriptors, List<Long> processInstanceOids, 
         Date startDate, Date endDate)
   {
      if ((descriptors == null || descriptors.isEmpty()) && (processInstanceOids == null && (startDate == null || endDate == null)))
      {
         return oidsToUuids.keySet();
      }
      Set<Long> result = new HashSet<Long>();
      DateFormat df = new SimpleDateFormat(dateFormat);
      
      if (descriptors != null)
      {
         for (String key : descriptors.keySet())
         {
            result.addAll(descriptorMatch(key, descriptors.get(key)));
         }
      }
      result.addAll(processInstanceOidMatch(processInstanceOids));
      result.addAll(dateMatch(df, startDate, endDate));
      
      return result;
   }

   private List<Long> dateMatch(DateFormat df, Date startDate, Date endDate)
   {
      List<Long> result = new ArrayList<Long>();
      if (startDate == null || endDate == null)
      {
         return result;
      }
      try
      {
         Map<String, List<Long>> startDates = fields.get(FIELD_START_DATE);
         Map<String, List<Long>> endDates = fields.get(FIELD_END_DATE);
         List<Long> matchedStartDates = new ArrayList<Long>();
               
         
         for (String startString : startDates.keySet())
         {
            Date processStart = df.parse(startString);
            if (startDate.compareTo(processStart) < 1)
            {
               matchedStartDates.addAll(startDates.get(startString));
            }
         }
         for (String endString : endDates.keySet())
         {
            Date processEnd = df.parse(endString);
            if (endDate.compareTo(processEnd) > -1)
            {
               for (Long oid : endDates.get(endString))
               {
                  if (matchedStartDates.contains(oid))
                  {
                     result.add(oid);
                  }
               }
                  
            }
         }
         return result;
      }
      catch (ParseException e)
      {
         throw new IllegalStateException("Failed to parse date in archive " + e.getMessage(), e);
      }
   }
   
   private Set<Long> processInstanceOidMatch(List<Long> processInstanceOids)
   {
      Set<Long> result = new HashSet<Long>();
      if (processInstanceOids != null)
      {
         for (Long oid : processInstanceOids)
         {
            if (oidsToUuids.containsKey(oid))
            {
               result.add(oid);
               List<Long> subProcesses = rootProcessToSubProcesses.get(oid);
               if (subProcesses != null)
               {
                  result.addAll(subProcesses);
               }
               else
               {
                  // if subprocesses is null it means we are dealing with a subprocess
                  // add it parent and all its siblings
                  for (Long rootOid : rootProcessToSubProcesses.keySet())
                  {
                     if (rootProcessToSubProcesses.get(rootOid).contains(oid))
                     {
                        result.add(rootOid);
                        result.addAll(rootProcessToSubProcesses.get(rootOid));
                        break;
                     }
                  }
               }
            }
            
         }
      }
      return result;
   }

   public String getUuid(Long processInstanceOid)
   {
      return oidsToUuids.get(processInstanceOid);
   }

   public void setUuid(long processInstanceOid, String uuid)
   {
      oidsToUuids.put(processInstanceOid, uuid);
   }
   
   public Map<Long, String> getOidsToUuids()
   {
      return oidsToUuids;
   }

   public void addField(Long processInstanceOid, String field, String value)
   {
      Map<String, List<Long>> values = fields.get(field);
      if (values == null)
      {
         values = new HashMap<String, List<Long>>();
         fields.put(field, values);
      }
      List<Long> processes = values.get(value);
      if (processes == null)
      {
         processes = new ArrayList<Long>();
         values.put(value, processes);
      }
      processes.add(processInstanceOid);
   }
   
   public Set<Long> getProcessInstanceOids()
   {
      return oidsToUuids.keySet();
   }
   
   public Map<Long, List<Long>> getRootProcessToSubProcesses()
   {
      return rootProcessToSubProcesses;
   }

   public String getArchiveManagerId()
   {
      return archiveManagerId;
   }

   public Map<String, Map<String, List<Long>>> getFields()
   {
      return fields;
   }

   public void setFields(Map<String, Map<String, List<Long>>> fields)
   {
      this.fields = fields;
   }

   /**
    * Returns dateformat used for descri
    * @return
    */
   public String getDateFormat()
   {
      return dateFormat;
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
