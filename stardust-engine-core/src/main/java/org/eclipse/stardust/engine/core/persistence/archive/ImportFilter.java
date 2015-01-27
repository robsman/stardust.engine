package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.LinkDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class ImportFilter
{
   private final Date fromDate;

   private final Date toDate;

   private final List<Long> processInstanceOids;

   private final Map<Long, Boolean> processMap = new HashMap<Long, Boolean>();
   
   private static final Logger LOGGER = LogManager.getLogger(ImportFilter.class);

   public ImportFilter(Date fromDate, Date toDate)
   {
      super();
      this.processInstanceOids = null;
      this.fromDate = fromDate;
      this.toDate = toDate;
   }

   public ImportFilter(List<Long> processInstanceOids)
   {
      super();
      this.processInstanceOids = processInstanceOids;
      this.fromDate = null;
      this.toDate = null;
   }

   public ImportFilter()
   {
      super();
      this.processInstanceOids = null;
      this.fromDate = null;
      this.toDate = null;
   }

   public boolean isInFilter(ProcessInstanceBean process, Object[] linkBuffer)
   {
      Boolean isInFilter = processMap.get(process.getOID());

      if (isInFilter == null)
      {
         TypeDescriptor typeDescriptor = TypeDescriptor.get(ProcessInstanceBean.class);
         if (processInstanceOids != null)
         {
            final int linkIdx = typeDescriptor.getLinkIdx(ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE);
            Number rootProcessInstanceOID =  (Number) linkBuffer[linkIdx];
            
            if (process.getOID() == rootProcessInstanceOID.longValue()) {
               isInFilter = processInstanceOids.contains(process.getOID());
            } else {
               isInFilter = processInstanceOids.contains(rootProcessInstanceOID);
            }
         }
         else if (fromDate != null && toDate != null)
         {
            isInFilter = (fromDate.compareTo(process.getStartTime()) < 1)
                  && (toDate.compareTo(process.getTerminationTime()) > -1);
         }
         else 
         {
            isInFilter = true;
         }
            
         // validate that we are importing a process instance with a valid 
         // process definition
         if (isInFilter)
         {
            try 
            {
               process.getProcessDefinition();
            }
            catch (ObjectNotFoundException e)
            {
               isInFilter = false;
               LOGGER.error("Failed to import process instance: " + process.getOID()
                     + ". Model: " + process.getModelOID() + ".", e);
            }
         }
         processMap.put(process.getOID(), isInFilter);
      }
      return isInFilter;
   }

   public boolean isInFilter(Persistent persistent, Object[] linkBuffer)
   {
      if (persistent instanceof ProcessInstanceBean)
      {
         return isInFilter((ProcessInstanceBean) persistent, linkBuffer);
      }
      if (linkBuffer == null) {
         return true;
      }
      TypeDescriptor typeDescriptor = TypeDescriptor.get(persistent.getClass());
      Boolean isInFilter = true;
      Boolean filtered = false;
      final List links = typeDescriptor.getLinks();
      // find downstream links to ProcessInstance
      for (int j = 0; j < links.size(); ++j)
      {
         LinkDescriptor link = (LinkDescriptor) links.get(j);
         final int linkIdx = typeDescriptor.getLinkIdx(link.getField().getName());
         Number linkOID =  (Number) linkBuffer[linkIdx];
         
         if (link.getField().getType() == ProcessInstanceBean.class)
         {
            isInFilter = processMap.get(linkOID);
            filtered = true;
            break;
         }
         
      }
      // find upstream links to ProcessInstance - if needed
      if (!filtered) 
      {
         for (int i = 0; i < typeDescriptor.getParents().size(); ++i)
         {
            LinkDescriptor link = (LinkDescriptor) typeDescriptor.getParents().get(i);
            final int linkIdx = typeDescriptor.getLinkIdx(link.getField().getName());
            Number linkOID =  (Number) linkBuffer[linkIdx];
   
            if (link.getField().getType() == ProcessInstanceBean.class)
            {
               isInFilter = processMap.get(linkOID);
               filtered = true;
               break;
            }
         }
      }
      if (isInFilter == null)
      {
         throw new IllegalStateException(
               "ProcessInstanceBean has not yet been filtered, make sure it is filtered first");
      }
      return isInFilter;
   }

}
