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

   @Expose
   private boolean isDump;

   public ExportIndex()
   {
   }

   public ExportIndex(String archiveManagerId, boolean isDump)
   {
      this(archiveManagerId, new HashMap<ExportProcess, List<ExportProcess>>(), isDump);
   }

   public ExportIndex(String archiveManagerId, 
         Map<ExportProcess, List<ExportProcess>> rootProcessToSubProcesses, boolean isDump)
   {
      this.archiveManagerId = archiveManagerId;
      this.processes = rootProcessToSubProcesses;
      this.isDump = isDump;
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

   public boolean isDump()
   {
      return isDump;
   }

}
