package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.DefaultPersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.LinkDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class ImportFilter
{
   private final Date fromDate;

   private final Date toDate;

   private final List<Long> processInstanceOids;

   private final Map<Long, Boolean> processMap = new HashMap<Long, Boolean>();

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

   public boolean isInFilter(ProcessInstanceBean process)
   {
      Boolean isInFilter = processMap.get(process.getOID());

      if (isInFilter == null)
      {
         if (processInstanceOids != null)
         {
            if (process.getOID() == process.getRootProcessInstanceOID()) {
               isInFilter = processInstanceOids.contains(process.getOID());
            } else {
               isInFilter = processInstanceOids.contains(process.getRootProcessInstanceOID());
            }
         }
         else if (fromDate != null && toDate != null)
         {
            isInFilter = (fromDate.compareTo(process.getStartTime()) < 1)
                  && (toDate.compareTo(process.getTerminationTime()) > -1);
         }
         else
         {
            isInFilter = false;
         }
         processMap.put(process.getOID(), isInFilter);
      }
      return isInFilter;
   }

   public boolean isInFilter(Persistent persistent)
   {
      TypeDescriptor typeDescriptor = TypeDescriptor.get(persistent.getClass());
      DefaultPersistenceController cntrl = (DefaultPersistenceController) persistent
            .getPersistenceController();
      Boolean isInFilter = true;
      Boolean filtered = false;
      final List links = typeDescriptor.getLinks();
      // find downstream links to ProcessInstance
      for (int j = 0; j < links.size(); ++j)
      {
         LinkDescriptor link = (LinkDescriptor) links.get(j);
         final int linkIdx = typeDescriptor.getLinkIdx(link.getField().getName());
         Number linkOID = (Number) cntrl.getLinkBuffer()[linkIdx];
         
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
            Number linkOID = (Number) cntrl.getLinkBuffer()[linkIdx];
   
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
