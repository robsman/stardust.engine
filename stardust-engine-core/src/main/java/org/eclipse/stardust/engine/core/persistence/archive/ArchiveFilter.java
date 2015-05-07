package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class ArchiveFilter implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private Collection<Long> processInstanceOids;
   private Collection<String> processDefinitionIds;
   private Collection<String> modelIds;

   private List<Integer> modelOids;

   private Date fromDate;

   private Date toDate;
   
   private HashMap<String, Object> descriptors;
   
   public ArchiveFilter(Collection<String> modelIds,
         Collection<String> processDefinitionIds, Collection<Long> processInstanceOids,
         List<Integer> modelOids, Date fromDate, Date toDate,
         HashMap<String, Object> descriptors)
   {
      super();
      if (modelOids == null)
      {
         modelOids = new ArrayList<Integer>();
      }
      this.processDefinitionIds = processDefinitionIds;
      this.modelIds = modelIds;
      this.processInstanceOids = processInstanceOids;
      this.modelOids = modelOids;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.descriptors = descriptors;
   }

   public void validateDates()
   {
      if (fromDate != null || toDate != null)
      {
         if (fromDate == null)
         {
            this.fromDate = new Date(0);
         }
         if (toDate == null)
         {
            this.toDate = TimestampProviderUtils.getTimeStamp();
         }
         if (toDate.before(fromDate))
         {
            throw new IllegalArgumentException(
                  "From date can not be before to date");
         }
      }
   }

   public static long getSerialversionuid()
   {
      return serialVersionUID;
   }

   public Collection<String> getProcessDefinitionIds()
   {
      return processDefinitionIds;
   }

   public Collection<String> getModelIds()
   {
      return modelIds;
   }

   public Collection<Long> getProcessInstanceOids()
   {
      return processInstanceOids;
   }

   public void setProcessInstanceOids(Collection<Long> processInstanceOids)
   {
      this.processInstanceOids = processInstanceOids;
   }

   public List<Integer> getModelOids()
   {
      return modelOids;
   }

   public Date getFromDate()
   {
      return fromDate;
   }

   public Date getToDate()
   {
      return toDate;
   }

   public HashMap<String, Object> getDescriptors()
   {
      return descriptors;
   }

}
