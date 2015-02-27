package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import com.google.gson.annotations.Expose;

public class ExportIndex implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Expose
   private Map<ExportProcess, List<ExportProcess>> processes;

   @Expose
   private String archiveManagerId;

   public ExportIndex()
   {
      this.processes = new HashMap<ExportProcess, List<ExportProcess>>();
   }

   public ExportIndex(String archiveManagerId)
   {
      this.archiveManagerId = archiveManagerId;
      this.processes = new HashMap<ExportProcess, List<ExportProcess>>();
   }

   public ExportIndex(String archiveManagerId, 
         Map<ExportProcess, List<ExportProcess>> rootProcessToSubProcesses)
   {
      this.archiveManagerId = archiveManagerId;
      this.processes = rootProcessToSubProcesses;
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

   public Map<ExportProcess, List<ExportProcess>> getRootProcessToSubProcesses()
   {
      return processes;
   }

   public String getArchiveManagerId()
   {
      return archiveManagerId;
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

}
