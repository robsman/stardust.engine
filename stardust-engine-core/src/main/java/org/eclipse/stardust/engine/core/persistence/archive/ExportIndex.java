package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import com.google.gson.annotations.Expose;

public class ExportIndex implements Serializable
{

   public static final String FIELD_START_DATE = "startDate";
   public static final String FIELD_END_DATE = "endDate";
   public static final String FIELD_MODEL_ID = "modelId";
   public static final String FIELD_PROCESS_DEFINITION_ID = "procDefId";
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Expose
   private String archiveManagerId;

   @Expose
   private String dateFormat;

   @Expose
   private String dumpLocation;

   // map of root oid to subprocess process oids.
   // if a root process has no subprocesses the sublist will be empty, it will not be null
   @Expose
   private Map<Long, List<Long>> rootProcessToSubProcesses;

   @Expose
   private Map<Long, String> oidsToUuids;

   // map of fields to values. values is map of values to processInstanceOids that contains them
   // e.g. fields.get("name").get("John") returns oids of all processes that has a field "name" with value "John"
   @Expose
   private Map<String, Map<String, List<Long>>> fields;

   public ExportIndex()
   {
   }

   public ExportIndex(String archiveManagerId, String dateFormat, String dumpLocation)
   {
      this(archiveManagerId, dateFormat, new HashMap<Long, List<Long>>(), 
            new HashMap<String, Map<String, List<Long>>>(), new HashMap<Long, String>(), dumpLocation);
   }

   public ExportIndex(String archiveManagerId, String dateFormat, 
         Map<Long, List<Long>> processes,
         Map<String, Map<String, List<Long>>> fields, Map<Long, String> oidsToUuids, String dumpLocation)
   {
      this.archiveManagerId = archiveManagerId;
      this.rootProcessToSubProcesses = processes;
      this.fields = fields;
      this.dumpLocation = dumpLocation;
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

   public boolean contains(String key, Collection<? extends Object> values, boolean onlyRoots)
   {
      if (key == null || values.isEmpty())
      {
         return true;
      }
      Set<Long> result = new HashSet<Long>();
      for (Object value : values)
      {
         List<Long> oids = descriptorMatch(key, value);
         for (Long oid : oids)
         {
            addProcessAndSubs(result, oid, onlyRoots);
            if (!result.isEmpty())
            {
               return true;
            }
         }
      }
      return false;
   }

   private Set<Long> descriptorMatch(String key, Collection<? extends Object> values, boolean onlyRoots)
   {
      Set<Long> result = new HashSet<Long>();
      for (Object value : values)
      {
         List<Long> oids = descriptorMatch(key, value);
         for (Long oid : oids)
         {
            addProcessAndSubs(result, oid, onlyRoots);
         }
      }
      return result;
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
   public Set<Long> getProcesses(ArchiveFilter filter)
   {
      if ((filter.getDescriptors() == null || filter.getDescriptors().isEmpty()) 
            && (filter.getProcessInstanceOids() == null && (filter.getFromDate() == null || filter.getToDate() == null))
            && CollectionUtils.isEmpty(filter.getModelIds())
            && CollectionUtils.isEmpty(filter.getProcessDefinitionIds()))
      {
         return oidsToUuids.keySet();
      }
      Set<Long> result = null;
      Set<Long> descriptorMatch = null;
      Set<Long> modelMatch = null;
      Set<Long> procDefMatch = null;
      Set<Long> oidMatch = null;
      Set<Long> dateMatch = null;
      DateFormat df = new SimpleDateFormat(dateFormat);
      
      if (filter.getDescriptors() != null)
      {
         descriptorMatch = new HashSet<Long>();
         for (String key : filter.getDescriptors().keySet())
         {
            descriptorMatch.addAll(descriptorMatch(key, Arrays.asList(filter.getDescriptors().get(key)), false));
         }
      }
      result = intersect(result, descriptorMatch);
      if (isResultPossible(result) && CollectionUtils.isNotEmpty(filter.getModelIds()))
      {
         modelMatch = new HashSet<Long>();
         modelMatch.addAll(descriptorMatch(FIELD_MODEL_ID, filter.getModelIds(), true));
      }
      result =  intersect(result, modelMatch);
      if (isResultPossible(result) && CollectionUtils.isNotEmpty(filter.getProcessDefinitionIds()))
      {
         procDefMatch = new HashSet<Long>();
         procDefMatch.addAll(descriptorMatch(FIELD_PROCESS_DEFINITION_ID, filter.getProcessDefinitionIds(), true));
      }
      result = intersect(result, procDefMatch); 
      if (isResultPossible(result) && CollectionUtils.isNotEmpty(filter.getProcessInstanceOids()))
      {
         oidMatch = new HashSet<Long>();
         oidMatch.addAll(processInstanceOidMatch(filter.getProcessInstanceOids()));
      }
      result = intersect(result, oidMatch);
      if (isResultPossible(result) && filter.getFromDate() != null)
      {
         dateMatch = new HashSet<Long>();
         dateMatch.addAll(dateMatch(df, filter.getFromDate(), filter.getToDate()));
      }
      result = intersect(result, dateMatch);
      return result;
   }
   
   private boolean isResultPossible(Set<Long> result)
   {
      return result == null || !result.isEmpty();
   }

   private Set<Long> intersect(Set<Long> result, Set<Long> match)
   {
      Set<Long> intersection;
      // match is null if no criteria was provided, return existing result
      if (match == null)
      {
         intersection = result;
      }
      // no match was made, return the empty set
      else if (match.isEmpty())
      {
         intersection = match;
      }
      else
      {
         // no intersect was done before so current match is current result
         if (result == null)
         {
            intersection = match;
         }
         else 
         {
            // we had a match before and we have a current match, 
            // since we use AND conjunction out result is the intersection of the two
            result.retainAll(match);
            intersection = result;
         }
      }
      return intersection;
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
         //Map<String, List<Long>> endDates = fields.get(FIELD_END_DATE);
         //List<Long> matchedStartDates = new ArrayList<Long>();
               
         
         for (String startString : startDates.keySet())
         {
            Date processStart = df.parse(startString);
            if (startDate.compareTo(processStart) < 1 && endDate.compareTo(processStart) > -1)
            {
               //matchedStartDates.addAll(startDates.get(startString));
               result.addAll(startDates.get(startString));
            }
         }
         //add this uncommented logic back in if we want to add parameter for terminatedBefore
//         for (String endString : endDates.keySet())
//         {
//            Date processEnd = df.parse(endString);
//            if (endDate.compareTo(processEnd) > -1)
//            {
//               for (Long oid : endDates.get(endString))
//               {
//                  if (matchedStartDates.contains(oid))
//                  {
//                     result.add(oid);
//                  }
//               }
//                  
//            }
//         }
         return result;
      }
      catch (ParseException e)
      {
         throw new IllegalStateException("Failed to parse date in archive " + e.getMessage(), e);
      }
   }
   
   private Set<Long> processInstanceOidMatch(Collection<Long> processInstanceOids)
   {
      Set<Long> result = new HashSet<Long>();
      if (processInstanceOids != null)
      {
         for (Long oid : processInstanceOids)
         {
            if (oidsToUuids.containsKey(oid))
            {
               addProcessAndSubs(result, oid, false);
            }
            
         }
      }
      return result;
   }

   private void addProcessAndSubs(Set<Long> result, Long oid, boolean onlyAddIfRoot)
   {
      List<Long> subProcesses = rootProcessToSubProcesses.get(oid);
      // we have a root process
      if (subProcesses != null)
      {
         result.add(oid);
         result.addAll(subProcesses);
      }
      else if (!onlyAddIfRoot)
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

   public String getDumpLocation()
   {
      return dumpLocation;
   }

}
